package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.cvbotunion.cvtwipush.Service.WebService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class TwitterUser implements Parcelable, Serializable, Updatable {
    public boolean avatarUnderProcessing = false;

    public String id;
    public String name;
    public String screen_name;
    public String name_in_group;
    @Nullable public String location;
    public int followers_count;
    public int friends_count;
    public int statuses_count;
    public String profile_image_url;
    // TODO 考虑合并以下两者
    @Nullable public transient Bitmap cached_profile_image_preview;
    @Nullable public transient Bitmap cached_profile_image;

    public TwitterUser(String id,String name,String screen_name,String name_in_group){
        this.id = id;
        this.name = name;
        this.screen_name = screen_name;
        this.name_in_group = name_in_group!=null ? name_in_group : name;
    }

    public TwitterUser(String id,String name,String screen_name,String name_in_group,String profile_image_url){
        this(id,name,screen_name,name_in_group);
        this.profile_image_url = profile_image_url;
    }

    public TwitterUser(String id,String name,String screen_name,String name_in_group,String profile_image_url,Bitmap cached_profile_image_preview){
        this(id,name,screen_name,name_in_group,profile_image_url);
        this.cached_profile_image_preview = cached_profile_image_preview;
    }

    public TwitterUser(String id,String name,String screen_name,String name_in_group,String profile_image_url,Bitmap cached_profile_image_preview, Bitmap cached_profile_image){
        this(id,name,screen_name,name_in_group,profile_image_url,cached_profile_image_preview);
        this.cached_profile_image = cached_profile_image;
    }

    public TwitterUser(JSONObject twitterUser) throws JSONException {
        this.id = twitterUser.getString("id_str");
        this.name = twitterUser.getString("name");
        this.screen_name = twitterUser.getString("screen_name");
        this.name_in_group = name;
        this.location = twitterUser.isNull("location") ? null : twitterUser.getString("location");
        this.followers_count = twitterUser.getInt("followers_count");
        this.friends_count = twitterUser.getInt("friends_count");
        this.statuses_count = twitterUser.getInt("statuses_count");
        this.profile_image_url = WebService.SERVER_API+"lookup/avatar/id/"+id;
    }

    protected TwitterUser(Parcel in) {
        id = in.readString();
        name = in.readString();
        screen_name = in.readString();
        name_in_group = in.readString();
        location = in.readString();
        followers_count = in.readInt();
        friends_count = in.readInt();
        statuses_count = in.readInt();
        profile_image_url = in.readString();
        cached_profile_image_preview = in.readParcelable(Bitmap.class.getClassLoader());
        cached_profile_image = in.readParcelable(Bitmap.class.getClassLoader());
    }

    /**
     * Notice that this method will not check {@code name_in_group}.<br>
     * 注意该方法不检查{@code name_in_group}属性
     * @param checkCounts
     *        whether check {@code followers_count}, {@code friends_count} and {@code statuses_count}<br>
     *        是否检查{@code followers_count}、{@code friends_count}和{@code statuses_count}
     * @return {@code true} if the given object represents a {@code TwitterUser}
     *         equivalent to this twitterUser, {@code false} otherwise
     */
    public boolean equals(Object obj, boolean checkCounts) {
        if(this==obj) return true;
        if(obj instanceof TwitterUser) {
            TwitterUser anotherTwitterUser = (TwitterUser)obj;
            if(checkCounts) {
                if(followers_count!=anotherTwitterUser.followers_count || friends_count!=anotherTwitterUser.friends_count || statuses_count!=anotherTwitterUser.statuses_count)
                    return false;
            }
            return id.equals(anotherTwitterUser.id) && name.equals(anotherTwitterUser.name) && screen_name.equals(anotherTwitterUser.screen_name)
                    && location!=null?location.equals(anotherTwitterUser.location):anotherTwitterUser.location==null
                    && profile_image_url!=null?profile_image_url.equals(anotherTwitterUser.profile_image_url):anotherTwitterUser.profile_image_url==null;
        }
        return false;
    }

    /**
     * @see #equals(Object, boolean)
     */
    public boolean equals(Object obj) {
        return equals(obj, false);
    }

    public void setNameInGroup(String name_in_group) {
        this.name_in_group = name_in_group;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(screen_name);
        dest.writeString(name_in_group);
        dest.writeString(location);
        dest.writeInt(followers_count);
        dest.writeInt(friends_count);
        dest.writeInt(statuses_count);
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

    @Override
    public boolean update() {
        // TODO TwitterUser更新操作
        return false;
    }
}
