package com.example.testapp.Model;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TwitterStatus{
    //推文类型
    public final static int NORMAL=0;
    public final static int REPLY=1;
    public final static int QUOTE=2;

    public String created_at;
    public String id;
    @Nullable public String text;
    public TwitterUser user;
    @Nullable public String in_reply_to_status_id;
    @Nullable public String in_reply_to_user_id;
    @Nullable public String in_reply_to_screen_name;
    @Nullable public String quoted_status_id;
    @Nullable public String location;
    @Nullable public ArrayList<String> hashtags;
    @Nullable public ArrayList<TwitterUser> user_mentions;
    @Nullable public ArrayList<TwitterMedia> media;

    public TwitterStatus(){

    }

    public TwitterStatus(String created_at, String id, String text, TwitterUser user){
        this.created_at = created_at;
        this.id = id;
        this.user = user;
        this.text = text;
    }

    public TwitterStatus(String created_at, String id, String text, TwitterUser user, ArrayList<TwitterMedia> media){
        this.created_at = created_at;
        this.id = id;
        this.user = user;
        this.media = media;
        this.text = text;
    }

    public String getCreated_at(){
        return created_at;
    }

    public String getId(){
        return id;
    }

    public String getText(){
        return text;
    }

    public TwitterUser getUser(){
        return user;
    }

    public int getTweetType(){
        if(in_reply_to_status_id != null){
            return REPLY;
        } else if(quoted_status_id != null){
            return QUOTE;
        } else {
            return NORMAL;
        }
    }

    @Nullable
    public String getIn_reply_to_status_id(){
        return in_reply_to_status_id;
    }

    @Nullable
    public String getIn_reply_to_screen_name(){
        return in_reply_to_screen_name;
    }

    @Nullable
    public String getQuoted_status_id(){
        return quoted_status_id;
    }

    @Nullable
    public String getLocation(){
        return location;
    }

    @Nullable
    public ArrayList<String> getHashtags(){
        return hashtags;
    }

    @Nullable
    public ArrayList<TwitterUser> getUser_mentions(){
        return user_mentions;
    }

    @Nullable
    public ArrayList<TwitterMedia> getMedia(){
        return media;
    }
}

