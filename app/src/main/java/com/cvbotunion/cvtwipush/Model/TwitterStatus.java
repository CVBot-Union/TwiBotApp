package com.cvbotunion.cvtwipush.Model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cvbotunion.cvtwipush.DBModel.DBTwitterStatus;

import org.litepal.LitePal;

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
        hashtags = new ArrayList<>();
        user_mentions = new ArrayList<>();
        media = new ArrayList<>();
    }

    public TwitterStatus(String created_at, String id, @Nullable String text, TwitterUser user){
        this();
        this.created_at = created_at;
        this.id = id;
        this.user = user;
        if(text != null) {
            this.text = text;
        }
    }

    public TwitterStatus(String created_at, String id, @Nullable String text, TwitterUser user,int type, @Nullable String parent_status_id){
        this(created_at,id,text,user);
        switch(type) {
            case REPLY:
                this.in_reply_to_status_id = parent_status_id;
                break;
            case QUOTE:
                this.quoted_status_id = parent_status_id;
                break;
            default:
                Log.i("info", "未指定父推文类型");
                break;
        }
    }

    public TwitterStatus(String created_at, String id, @Nullable String text, TwitterUser user, @Nullable ArrayList<TwitterMedia> media){
        this(created_at,id,text,user);
        this.media = media;
        if(text != null) {
            this.text = text;
        }
    }

    public TwitterStatus(String created_at, String id, @Nullable String text, TwitterUser user, ArrayList<TwitterMedia> media, int type, @Nullable String parent_status_id){
        this(created_at,id,text,user,media);
        if(text != null) {
            this.text = text;
        }
        switch(type){
            case REPLY:
                this.in_reply_to_status_id = parent_status_id;
                break;
            case QUOTE:
                this.quoted_status_id = parent_status_id;
                break;
            default:
                Log.i("info","未指定父推文类型");
                break;
        }
    }

    // TODO 为Model中的类添加JSONObject -> Object的构造器

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

    public String getFullText() {
        return getFullText(RTGroup.DEFAULT_FORMAT, "");
    }

    public String getFullText(String format) {
        return getFullText(format, "");
    }

    //有待修改
    public String getFullText(String format, String translatedText) {
        if(!translatedText.equals(""))
            translatedText = translatedText+"\n\n";
        String typeString = "";
        TwitterStatus parentStatus = null;
        switch (getTweetType()){
            case REPLY:
                typeString = "回复：\n\n";
                parentStatus = LitePal.where("tid = ?", in_reply_to_status_id)
                        .findFirst(DBTwitterStatus.class).toTwitterStatus();
                break;
            case QUOTE:
                typeString = "转推：\n\n";
                parentStatus = LitePal.where("tid = ?", quoted_status_id)
                        .findFirst(DBTwitterStatus.class).toTwitterStatus();
                break;
            default:
                break;
        }
        return String.format(format,
                user.name_in_group,
                user.screen_name,
                created_at,
                text==null?"":text+"\n\n",
                text==null?"":translatedText,
                typeString,
                parentStatus==null?"":"#"+parentStatus.user.name_in_group+"#",
                parentStatus==null?"":parentStatus.user.screen_name,
                parentStatus==null?"":parentStatus.text);
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

    public void clearMediaCache(){
        if(media != null){
            for(TwitterMedia singleMedia:media){
                singleMedia.cached_image_preview=null;
                singleMedia.cached_image=null;
            }
        }
    }
}

