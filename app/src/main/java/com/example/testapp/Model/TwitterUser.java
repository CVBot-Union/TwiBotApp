package com.example.testapp.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class TwitterUser{
    public String id;
    public String name;
    public String screen_name;
    public String nameInGroup;
    @Nullable public String location;
    @Nullable public String followers_count;
    @Nullable public String friends_count;
    @Nullable public String statuses_count;
    @Nullable public String profile_image_url;
    @Nullable public Bitmap cached_profile_image_preview;
    @Nullable public Bitmap cached_profile_image;

    public TwitterUser(String id,String name,String screen_name,String nameInGroup){
        this.id = id;
        this.name = name;
        this.screen_name = screen_name;
        this.nameInGroup = nameInGroup;
    }

    public TwitterUser(String id,String name,String screen_name,String nameInGroup,String profile_image_url){
        this.id = id;
        this.name = name;
        this.screen_name = screen_name;
        this.nameInGroup = nameInGroup;
        this.nameInGroup = nameInGroup;
        this.profile_image_url = profile_image_url;
    }
}
