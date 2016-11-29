package com.sesame.onespace.models.dashboard;

/**
 * Created by Thian on 5/11/2559.
 */

public class MyLink {

    private DashboardObject currentObject;
    private MyLink nextLink;

    public MyLink(DashboardObject currentLink){

        this.currentObject = currentLink;

        this.init();

    }

    private void init(){

        this.nextLink = null;

    }

    public void setObject(DashboardObject currentObject){

        this.currentObject = currentObject;

    }

    public void setNextLink(MyLink nextLink){

        this.nextLink = nextLink;

    }

    public DashboardObject getObject(){

        return this.currentObject;

    }

    public MyLink getNextLink(){

        return this.nextLink;

    }

}
