package com.sesame.onespace.models.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by chongos on 8/25/15 AD.
 */
public class Place implements ClusterItem {


    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("formatted_address")
    @Expose
    private String formattedAddress;
    @SerializedName("formatted_phone_nr")
    @Expose
    private String formattedPhoneNr;
    @SerializedName("primary_category")
    @Expose
    private String primaryCategory;
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
    @SerializedName("category_class")
    @Expose
    private String categoryClass;

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The formattedAddress
     */
    public String getFormattedAddress() {
        return formattedAddress;
    }

    /**
     *
     * @param formattedAddress
     * The formatted_address
     */
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    /**
     *
     * @return
     * The formattedPhoneNr
     */
    public String getFormattedPhoneNr() {
        return formattedPhoneNr;
    }

    /**
     *
     * @param formattedPhoneNr
     * The formatted_phone_nr
     */
    public void setFormattedPhoneNr(String formattedPhoneNr) {
        this.formattedPhoneNr = formattedPhoneNr;
    }

    /**
     *
     * @return
     * The primaryCategory
     */
    public String getPrimaryCategory() {
        return primaryCategory;
    }

    /**
     *
     * @param primaryCategory
     * The primary_category
     */
    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
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

    /**
     *
     * @return
     * The categoryClass
     */
    public String getCategoryClass() {
        return categoryClass;
    }

    /**
     *
     * @param categoryClass
     * The category_class
     */
    public void setCategoryClass(String categoryClass) {
        this.categoryClass = categoryClass;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getLat(), getLng());
    }


}
