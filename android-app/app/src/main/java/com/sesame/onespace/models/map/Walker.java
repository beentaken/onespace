package com.sesame.onespace.models.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chongos on 8/26/15 AD.
 */
public class Walker implements ClusterItem {

    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("jid")
    @Expose
    private String jid;
    @SerializedName("resource")
    @Expose
    private String resource;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;

    /**
     *
     * @return
     * The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     * The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     *
     * @return
     * The userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @param userName
     * The user_name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @return
     * The jid
     */
    public String getJid() {
        return jid;
    }

    /**
     *
     * @param jid
     * The jid
     */
    public void setJid(String jid) {
        this.jid = jid;
    }

    /**
     *
     * @return
     * The resource
     */
    public String getResource() {
        return resource;
    }

    /**
     *
     * @param resource
     * The resource
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     *
     * @return
     * The lat
     */
    public Double getLat() {
        return lat;
    }

    /**
     *
     * @param lat
     * The lat
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     *
     * @return
     * The lng
     */
    public Double getLng() {
        return lng;
    }

    /**
     *
     * @param lng
     * The lng
     */
    public void setLng(Double lng) {
        this.lng = lng;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getLat(), getLng());
    }


}
