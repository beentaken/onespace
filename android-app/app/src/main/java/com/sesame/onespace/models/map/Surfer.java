package com.sesame.onespace.models.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 9/1/15 AD.
 */
public class Surfer implements ClusterItem {

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
    @SerializedName("place_name")
    @Expose
    private String placeName;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("vloc")
    @Expose
    private String vloc;
    @SerializedName("website")
    @Expose
    private String website;

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
     * The placeName
     */
    public String getPlaceName() {
        return placeName;
    }

    /**
     *
     * @param placeName
     * The place_name
     */
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
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

    /**
     *
     * @return
     * The vloc
     */
    public String getVloc() {
        return vloc;
    }

    /**
     *
     * @param vloc
     * The vloc
     */
    public void setVloc(String vloc) {
        this.vloc = vloc;
    }

    /**
     *
     * @return
     * The website
     */
    public String getWebsite() {
        return website;
    }

    /**
     *
     * @param website
     * The website
     */
    public void setWebsite(String website) {
        this.website = website;
    }


    @Override
    public LatLng getPosition() {
        return new LatLng(getLat(), getLng());
    }
}
