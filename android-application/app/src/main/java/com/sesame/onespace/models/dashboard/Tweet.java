package com.sesame.onespace.models.dashboard;

/**
 * Created by Thian on 5/11/2559.
 */

public class Tweet extends DashboardObject{

    private String tweet_timestamp;
    private String tweet_screen_name;
    private String tweet_text;

    public Tweet(String tweet_timestamp, String tweet_screen_name, String tweet_text){

        this.tweet_timestamp = tweet_timestamp;
        this.tweet_screen_name = tweet_screen_name;
        this.tweet_text = tweet_text;

    }

    public void setTweet_timestamp(String tweet_timestamp){

        this.tweet_timestamp = tweet_timestamp;

    }

    public void setTweet_screen_name(String tweet_screen_name){

        this.tweet_screen_name = tweet_screen_name;

    }

    public  void setTweet_text(String tweet_text){

        this.tweet_text = tweet_text;

    }

    public String getTweet_timestamp(){

        return  this.tweet_timestamp;

    }

    public String getTweet_screen_name(){

        return  this.tweet_screen_name;

    }

    public String getTweet_text(){

        return this.tweet_text;

    }

}
