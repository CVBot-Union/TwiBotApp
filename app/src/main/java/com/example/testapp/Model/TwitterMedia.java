package com.example.testapp.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TwitterMedia{
    public final static int AVATAR=0;
    public final static int IMAGE=1;
    public final static int VIDEO=2;

    public String id;
    public String url;
    public String reviewImageURL;
    public int type;
    @Nullable public Bitmap cached_image_preview;
    @Nullable public Bitmap cached_image;

    public TwitterMedia(){

    }

    public TwitterMedia(String id,String url,int type,String reviewImageURL){
        this.id=id;
        this.url=url;
        this.type=type;
        this.reviewImageURL=reviewImageURL;
    }
}
