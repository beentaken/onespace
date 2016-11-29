package com.sesame.onespace.models;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by chongos on 9/30/15 AD.
 */
public class ProgressedFileRequestBody extends RequestBody {

    private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

    private final File file;
    private final Listener listener;
    private final String contentType;
    private long totalSize = 0;

    public ProgressedFileRequestBody(String contentType, File file, Listener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
        totalSize = file.length();
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;

            while (!this.listener.isCancel() && (read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                int percentage = (int) ((total / (float) totalSize) * 100);
                sink.flush();
                this.listener.onUpdateProgress(percentage);

            }
        } finally {
            Util.closeQuietly(source);
        }
    }

    public interface Listener {
        void onUpdateProgress(int percentage);
        boolean isCancel();
    }

}