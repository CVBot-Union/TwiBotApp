package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Map;

public class RTGroup implements Parcelable {
    public String id;
    public String name;
    public String avatarURL;

    @Nullable public Bitmap avatar;

    @Nullable public ArrayList<User> members;

    public static class Job implements Parcelable{
        public String job;
        public RTGroup group;
        public User user;

        public Job(String job, RTGroup group, User user){
            this.job = job;
            this.group = group;
            this.user = user;
        }

        protected Job(Parcel in) {
            job = in.readString();
            group = in.readParcelable(RTGroup.class.getClassLoader());
            user = in.readParcelable(User.class.getClassLoader());
        }

        public static final Creator<Job> CREATOR = new Creator<Job>() {
            @Override
            public Job createFromParcel(Parcel in) {
                return new Job(in);
            }

            @Override
            public Job[] newArray(int size) {
                return new Job[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(job);
            dest.writeParcelable(group, flags);
            dest.writeParcelable(user, flags);
        }
    }

    public RTGroup(String id,String name,@Nullable String avatarURL){
        this.id = id;
        this.name = name;
        if(avatarURL != null) {
            this.avatarURL = avatarURL;
        }
    }

    public RTGroup(String id,String name,String avatarURL,@Nullable Bitmap avatar){
        this(id,name,avatarURL);
        this.avatarURL = avatarURL;
        if(avatar != null) {
            this.avatar = avatar;
        }
    }

    public RTGroup(String id,String name,String avatarURL,@Nullable Bitmap avatar,ArrayList<User> members){
        this(id,name,avatarURL,avatar);
        if(members != null){
            this.members = members;
        }
    }

    protected RTGroup(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatarURL = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        members = in.createTypedArrayList(User.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatarURL);
        dest.writeParcelable(avatar, flags);
        dest.writeTypedList(members);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RTGroup> CREATOR = new Creator<RTGroup>() {
        @Override
        public RTGroup createFromParcel(Parcel in) {
            return new RTGroup(in);
        }

        @Override
        public RTGroup[] newArray(int size) {
            return new RTGroup[size];
        }
    };
}
