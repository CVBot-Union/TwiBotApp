package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TwitterUser implements Parcelable{
    public String id;
    public String name;
    public String screen_name;
    public String name_in_group;
    @Nullable public String location;
    @Nullable public String followers_count;
    @Nullable public String friends_count;
    @Nullable public String statuses_count;
    @Nullable public String profile_image_url;
    @Nullable public Bitmap cached_profile_image_preview;
    @Nullable public Bitmap cached_profile_image;

    public TwitterUser(String id,String name,String screen_name) {
        this.id = id;
        this.name = name;
        this.screen_name = screen_name;
        this.name_in_group = name;
    }

    public TwitterUser(String id,String name,String screen_name,String nameInGroup){
        this.id = id;
        this.name = name;
        this.screen_name = screen_name;
        this.name_in_group = nameInGroup;
    }

    public TwitterUser(String id,String name,String screen_name,String nameInGroup,String profile_image_url){
        this(id,name,screen_name,nameInGroup);
        this.profile_image_url = profile_image_url;
    }

    public TwitterUser(String id,String name,String screen_name,String nameInGroup,String profile_image_url,Bitmap cached_profile_image_preview){
        this(id,name,screen_name,nameInGroup,profile_image_url);
        this.cached_profile_image_preview = cached_profile_image_preview;
    }

    public TwitterUser(String id,String name,String screen_name,String nameInGroup,String profile_image_url,Bitmap cached_profile_image_preview, Bitmap cached_profile_image){
        this(id,name,screen_name,nameInGroup,profile_image_url,cached_profile_image_preview);
        this.cached_profile_image = cached_profile_image;
    }

    protected TwitterUser(Parcel in) {
        id = in.readString();
        name = in.readString();
        screen_name = in.readString();
        name_in_group = in.readString();
        location = in.readString();
        followers_count = in.readString();
        friends_count = in.readString();
        statuses_count = in.readString();
        profile_image_url = in.readString();
        cached_profile_image_preview = in.readParcelable(Bitmap.class.getClassLoader());
        cached_profile_image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public void setNameInGroup(String name_in_group) {
        this.name_in_group = name_in_group;
    }

    public void downloadAvatar(final RecyclerView.Adapter tAdapter, final Handler handler, @Nullable final Integer position) {
        if(profile_image_url != null) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        URL url0 = new URL(profile_image_url);
                        HttpURLConnection connection = (HttpURLConnection) url0.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        int code = connection.getResponseCode();
                        if (code == 200) {
                            InputStream inputStream = connection.getInputStream();
                            cached_profile_image_preview = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(position != null)
                                        tAdapter.notifyItemChanged(position);
                                    else
                                        tAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(screen_name);
        dest.writeString(name_in_group);
        dest.writeString(location);
        dest.writeString(followers_count);
        dest.writeString(friends_count);
        dest.writeString(statuses_count);
        dest.writeString(profile_image_url);
        dest.writeParcelable(cached_profile_image_preview, flags);
        dest.writeParcelable(cached_profile_image, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TwitterUser> CREATOR = new Creator<TwitterUser>() {
        @Override
        public TwitterUser createFromParcel(Parcel in) {
            return new TwitterUser(in);
        }

        @Override
        public TwitterUser[] newArray(int size) {
            return new TwitterUser[size];
        }
    };
}
