package com.sesame.onespace.managers.chat;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.ProgressedFileRequestBody;
import com.sesame.onespace.models.chat.UploadImage;
import com.sesame.onespace.utils.ImageRotateUtil;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by chongos on 9/23/15 AD.
 */
public class MediaUploadManager {

    private static final String TAG = MediaUploadManager.class.getName();

    private Context context;
    private UploadImage uploadImage;

    public MediaUploadManager(Context context, UploadImage uploadImage) {
        this.context = context;
        this.uploadImage = uploadImage;
    }

    public void upload(String fromID, String toID) {
        SettingsManager settingsManager = SettingsManager.getSettingsManager(context);
        final String serverURL = settingsManager.getOnespaceServerURL()
                + "/media/upload/?fromjid=" + fromID
                + "&fromjidresource=" + settingsManager.xmppRecource
                + "&tojid=" + toID
                + "&tojidresource=" + settingsManager.xmppRecource;
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return ImageRotateUtil.getRightAngleImage(uploadImage.getFilename());
            }

            @Override
            protected void onPostExecute(String path) {
                super.onPostExecute(path);
                uploadImage.rotateCompleted(path);
                File file = new File(path);
                uploadToServer(serverURL, file);
            }
        }.execute();
    }

    private void uploadToServer(String url, File file) {
        Log.i(TAG, "Upload url: " + url);
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", file.getName(),
                            new ProgressedFileRequestBody(uploadImage.getMimetype(), file, new ProgressedFileRequestBody.Listener() {
                                @Override
                                public void onUpdateProgress(int percentage) {
                                    uploadImage.setProgress(percentage);
                                    Log.i(TAG, "upload progress -> " + percentage + "%");
                                }

                                @Override
                                public boolean isCancel() {
                                    return uploadImage.isCanceled();
                                }
                            }))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.setConnectTimeout(600, TimeUnit.SECONDS);
            client.setReadTimeout(600, TimeUnit.SECONDS);

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Request request, IOException e) {
                    if(!uploadImage.isCanceled()) {
                        uploadImage.error(e);
                        Log.i(TAG, "upload fail");
                    }
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        uploadImage.completed(response.body().string());
                        Log.i(TAG, "upload completed");
                    } else if(!uploadImage.isCanceled()) {
                        uploadImage.error(new Exception());
                        Log.i(TAG, "upload fail");
                    }
                }
            });

        } catch (Exception ex) {
            uploadImage.error(ex);
            Log.i(TAG, "upload fail");
        }
    }


}
