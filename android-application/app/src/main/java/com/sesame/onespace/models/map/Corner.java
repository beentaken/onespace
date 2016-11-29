package com.sesame.onespace.models.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;
import com.sesame.onespace.R;

/**
 * Created by chongos on 11/16/15 AD.
 */
public class Corner implements ClusterItem {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("creator_id")
    @Expose
    private String creatorId;
    @SerializedName("creator_jid")
    @Expose
    private String creatorJid;
    @SerializedName("creator_jid_resource")
    @Expose
    private String creatorJidResource;
    @SerializedName("room_jid")
    @Expose
    private String roomJid;
    @SerializedName("room_jid_resource")
    @Expose
    private String roomJidResource;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("created")
    @Expose
    private String created;
    private boolean isMine;

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
     * The creatorId
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     *
     * @param creatorId
     * The creator_id
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     *
     * @return
     * The creatorJid
     */
    public String getCreatorJid() {
        return creatorJid;
    }

    /**
     *
     * @param creatorJid
     * The creator_jid
     */
    public void setCreatorJid(String creatorJid) {
        this.creatorJid = creatorJid;
    }

    /**
     *
     * @return
     * The creatorJidResource
     */
    public String getCreatorJidResource() {
        return creatorJidResource;
    }

    /**
     *
     * @param creatorJidResource
     * The creator_jid_resource
     */
    public void setCreatorJidResource(String creatorJidResource) {
        this.creatorJidResource = creatorJidResource;
    }

    /**
     *
     * @return
     * The roomJid
     */
    public String getRoomJid() {
        return roomJid;
    }

    /**
     *
     * @param roomJid
     * The room_jid
     */
    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    /**
     *
     * @return
     * The roomJidResource
     */
    public String getRoomJidResource() {
        return roomJidResource;
    }

    /**
     *
     * @param roomJidResource
     * The room_jid_resource
     */
    public void setRoomJidResource(String roomJidResource) {
        this.roomJidResource = roomJidResource;
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
     * The description
     */
    public String getDescription() {
        return description.equals("") ? "No Description" : description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
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
     * The created
     */
    public String getCreated() {
        return created;
    }

    /**
     *
     * @param created
     * The created
     */
    public void setCreated(String created) {
        this.created = created;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    @Override
    public LatLng getPosition() {
        return new LatLng(getLat(), getLng());
    }
}
