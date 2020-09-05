package com.example.testapp.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TwitterMedia implements Parcelable{
    public final static int AVATAR=0;
    public final static int IMAGE=1;
    public final static int VIDEO=2;

    public String id;
    public String url;
    public String previewImageURL;
    public int type;
    @Nullable public Bitmap cached_image_preview;
    @Nullable public Bitmap cached_image;

    public TwitterMedia(){

    }

    public TwitterMedia(String id,String url,int type,String previewImageURL){
        this.id=id;
        this.url=url;
        this.type=type;
        this.previewImageURL=previewImageURL;
    }

    protected TwitterMedia(Parcel in) {
        id = in.readString();
        url = in.readString();
        previewImageURL = in.readString();
        type = in.readInt();
        cached_image_preview = in.readParcelable(Bitmap.class.getClassLoader());
        cached_image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(previewImageURL);
        dest.writeInt(type);
        dest.writeParcelable(cached_image_preview, flags);
        dest.writeParcelable(cached_image, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TwitterMedia> CREATOR = new Creator<TwitterMedia>() {
        @Override
        public TwitterMedia createFromParcel(Parcel in) {
            return new TwitterMedia(in);
        }

        @Override
        public TwitterMedia[] newArray(int size) {
            return new TwitterMedia[size];
        }
    };
}
