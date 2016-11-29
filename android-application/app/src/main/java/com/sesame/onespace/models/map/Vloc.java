package com.sesame.onespace.models.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 11/4/15 AD.
 */
public class Vloc {
    @Expose
    private String tabid;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("vloc")
    @Expose
    private String vloc;
    @SerializedName("vloc-sha1")
    @Expose
    private String vlocSha1;
    @SerializedName("vplaces")
    @Expose
    private List<Vplace> vplaces = new ArrayList<Vplace>();

    /**
     *
     * @return
     * The tabid
     */
    public String getTabid() {
        return tabid;
    }

    /**
     *
     * @param tabid
     * The tabid
     */
    public void setTabid(String tabid) {
        this.tabid = tabid;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
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
     * The vlocSha1
     */
    public String getVlocSha1() {
        return vlocSha1;
    }

    /**
     *
     * @param vlocSha1
     * The vloc-sha1
     */
    public void setVlocSha1(String vlocSha1) {
        this.vlocSha1 = vlocSha1;
    }

    /**
     *
     * @return
     * The vplaces
     */
    public List<Vplace> getVplaces() {
        return vplaces;
    }

    /**
     *
     * @param vplaces
     * The vplaces
     */
    public void setVplaces(List<Vplace> vplaces) {
        this.vplaces = vplaces;
    }

    public class Vplace {

        @SerializedName("vplace_id")
        @Expose
        private Integer vplaceId;
        @SerializedName("name")
        @Expose
        private String name;

        /**
         *
         * @return
         * The vplaceId
         */
        public Integer getVplaceId() {
            return vplaceId;
        }

        /**
         *
         * @param vplaceId
         * The vplace_id
         */
        public void setVplaceId(Integer vplaceId) {
            this.vplaceId = vplaceId;
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

    }

}