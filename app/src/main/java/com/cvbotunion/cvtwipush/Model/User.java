package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class User implements Parcelable{

    public String id;
    public String name;
    @Nullable public String avatarURL;
    @Nullable public Bitmap avatar;
    public ArrayList<RTGroup.Job> jobs;

    User(String id, String name, @Nullable String avatarURL,@Nullable Bitmap avatar, @Nullable ArrayList<RTGroup.Job> jobs){
        this.id = id;
        this.name = name;
        if (avatarURL != null){
            this.avatarURL = avatarURL;
        }
        if(avatar != null){
            this.avatar = avatar;
        }
        if(jobs != null) {
            this.jobs = jobs;
        }
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatarURL = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        jobs = in.createTypedArrayList(RTGroup.Job.CREATOR);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatarURL);
        dest.writeParcelable(avatar, flags);
        dest.writeTypedList(jobs);
    }
}
