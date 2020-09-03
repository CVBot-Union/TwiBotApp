package com.example.testapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TwitterStatus implements Parcelable {
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

    protected TwitterStatus(Parcel in) {
        created_at = in.readString();
        id = in.readString();
        text = in.readString();
        user = in.readParcelable(TwitterUser.class.getClassLoader());
        in_reply_to_status_id = in.readString();
        in_reply_to_user_id = in.readString();
        in_reply_to_screen_name = in.readString();
        quoted_status_id = in.readString();
        location = in.readString();
        hashtags = in.createStringArrayList();
        user_mentions = in.createTypedArrayList(TwitterUser.CREATOR);
        media = in.createTypedArrayList(TwitterMedia.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(created_at);
        dest.writeString(id);
        dest.writeString(text);
        dest.writeParcelable(user, flags);
        dest.writeString(in_reply_to_status_id);
        dest.writeString(in_reply_to_user_id);
        dest.writeString(in_reply_to_screen_name);
        dest.writeString(quoted_status_id);
        dest.writeString(location);
        dest.writeStringList(hashtags);
        dest.writeTypedList(user_mentions);
        dest.writeTypedList(media);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TwitterStatus> CREATOR = new Creator<TwitterStatus>() {
        @Override
        public TwitterStatus createFromParcel(Parcel in) {
            return new TwitterStatus(in);
        }

        @Override
        public TwitterStatus[] newArray(int size) {
            return new TwitterStatus[size];
        }
    };

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

