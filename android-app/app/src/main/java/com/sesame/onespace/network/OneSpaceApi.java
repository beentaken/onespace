package com.sesame.onespace.network;

import android.content.Context;

import com.sesame.onespace.activities.SettingsActivity;
import com.sesame.onespace.constant.Constant;
import com.sesame.onespace.managers.SettingsManager;
import com.sesame.onespace.models.chat.HistoryMessages;
import com.sesame.onespace.models.map.Corner;
import com.sesame.onespace.models.map.Place;
import com.sesame.onespace.models.map.Surfer;
import com.sesame.onespace.models.map.Vloc;
import com.sesame.onespace.models.map.Walker;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.CallAdapter;
import retrofit.Converter;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by chongos on 9/18/15 AD.
 */
public class OneSpaceApi {

    public interface Service {

        @POST("user/login")
        Call<String> login(@Query("name") String name,
                   @Query("password") String password,
                   @Query("xmpphost") String xmpphost,
                   @Query("xmppresource") String xmppresource);

        @POST("user/login")
        Observable<String> loginRx(@Query("name") String name,
                           @Query("password") String password,
                           @Query("jid") String jid,
                           @Query("jidresource") String jidresource);

        @PUT("user/ploc")
        Call<String> updateGeoLocation(@Query("userid") String userid,
                               @Query("lat") double lat,
                               @Query("lng") double lng);

        @PUT("user/ploc")
        Observable<String> updateGeoLocationRx(@Query("userid") String userid,
                                         @Query("lat") double lat,
                                         @Query("lng") double lng);

        @GET("places/box")
        Call<ArrayList<Place>> getPlaces(@Query("lat1") double lat1,
                                         @Query("lng1") double lng1,
                                         @Query("lat2") double lat2,
                                         @Query("lng2") double lng2,
                                         @Query("limit") int limit);

        @GET("surfers/box")
        Call<ArrayList<Surfer>> getSurfers(@Query("lat1") double lat1,
                        @Query("lng1") double lng1,
                        @Query("lat2") double lat2,
                        @Query("lng2") double lng2,
                        @Query("limit") int limit);

        @GET("walkers/box")
        Call<ArrayList<Walker>> getWalkers(@Query("lat1") double lat1,
                        @Query("lng1") double lng1,
                        @Query("lat2") double lat2,
                        @Query("lng2") double lng2,
                        @Query("limit") int limit);

        @GET("url/map/")
        Observable<Vloc> getVlocRx(@Query("url") String url,
                                 @Query("unshorten") int unshorten);

        @POST("corners/add")
        Observable<String> addCorner(@Query("creatorid") String creatorid,
                                     @Query("creatorname") String creatorname,
                                     @Query("creatorjid") String creatorjid,
                                     @Query("creatorjidresource") String creatorjidresource,
                                     @Query("name") String name,
                                     @Query("description") String description,
                                     @Query("lat") double lat,
                                     @Query("lng") double lng);

        @POST("corners/delete")
        Observable<String> deleteCorner(@Query("creatorid") String creatorid);

        @POST("corners/delete")
        Observable<String> deleteCorner(@Query("creatorid") String creatorid,
                                        @Query("roomjid") String roomjid);

        @GET("corners/box")
        Call<ArrayList<Corner>> getCorners(@Query("lat1") double lat1,
                                           @Query("lng1") double lng1,
                                           @Query("lat2") double lat2,
                                           @Query("lng2") double lng2,
                                           @Query("limit") int limit);

        @GET("corners/user/{id}")
        Call<ArrayList<Corner>> getCorners(@Path("id") String creatorid);


        @GET("messages/history")
        Call<HistoryMessages> messagesHistory(@Query("fromjid") String fromJid,
                                                      @Query("fromjidresource") String fromJidResource,
                                                      @Query("tojid") String toJid,
                                                      @Query("tojidresource") String toJidResource,
                                                      @Query("lastsentdate") String lastsentdate,
                                                      @Query("limit") int limit);

        @GET("messages/history")
        Observable<HistoryMessages> messagesHistoryRx(@Query("fromjid") String fromJid,
                                                    @Query("fromjidresource") String fromJidResource,
                                                    @Query("tojid") String toJid,
                                                    @Query("tojidresource") String toJidResource,
                                                    @Query("lastsentdate") String lastsentdate,
                                                    @Query("limit") int limit);
    }


    public static class Builder {

        private Retrofit.Builder builder;

        public Builder(Context context) {
            SettingsManager settingManager = SettingsManager.getSettingsManager(context);
            this.builder = new Retrofit.Builder()
                    .addConverter(String.class, new ToStringConverter())
                    .baseUrl(settingManager.getOnespaceServerURL());
        }

        public Builder baseUrl(String url) {
            this.builder.baseUrl(url);
            return this;
        }

        public Builder addConverter(Type type, Converter converter) {
            this.builder.addConverter(type, converter);
            return this;
        }

        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            this.builder.addCallAdapterFactory(factory);
            return this;
        }

        public Builder addConverterFactory(Converter.Factory factory) {
            this.builder.addConverterFactory(factory);
            return this;
        }

        public Service build() {
            Retrofit retrofit = this.builder.build();
            return retrofit.create(OneSpaceApi.Service.class);
        }

    }


    public static final class ToStringConverter implements Converter<String> {

        @Override
        public String fromBody(ResponseBody body) throws IOException {
            return body.string();
        }

        @Override
        public RequestBody toBody(String value) {
            return RequestBody.create(MediaType.parse("text/plain"), value);
        }

    }

}
