package com.sesame.onespace.models.dashboard;

/**
 * Created by Thian on 8/11/2559.
 */

public class Instagram extends DashboardObject {

    private String dom;
    private String title;
    private String url;

    public Instagram(String dom, String title, String url){

        this.dom = dom;
        this.title = title;
        this.url = url;

    }

    public void setDom(String dom){

        this.dom = dom;

    }

    public void setTitle(String title){

        this.title = title;

    }

    public void setUrl(String url){

        this.url = url;

    }

    public String getDom(){

        return this.dom;

    }

    public String getTitle(){

        return this.title;

    }

    public String getUrl(){

        return  this.url;

    }

}
