package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.cvbotunion.cvtwipush.Activities.Timeline;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class User implements Parcelable, Serializable {

    public String id;
    public String name;
    private transient String password;
    private String auth;
    @Nullable public String avatarURL;
    @Nullable public Bitmap avatar;
    public transient ArrayList<Job> jobs;

    public User(String id, String name, @Nullable String avatarURL,@Nullable Bitmap avatar, ArrayList<Job> jobs){
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
        } else {
            this.jobs = new ArrayList<>();
        }
    }

    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        password = in.readString();
        auth = in.readString();
        avatarURL = in.readString();
        // avatar = in.readParcelable(Bitmap.class.getClassLoader());
        jobs = in.createTypedArrayList(Job.CREATOR);
    }

    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj instanceof User) {
            User anotherUser = (User)obj;
            if((jobs==null&&anotherUser.jobs!=null) || (jobs!=null&&anotherUser.jobs==null))
                return false;
            if(jobs.size()!=anotherUser.jobs.size())
                return false;
            for(int i=0;i<jobs.size();i++) {
                if(!jobs.get(i).equals(anotherUser.jobs.get(i)))
                    return false;
            }
            return id.equals(anotherUser.id) && name.equals(anotherUser.name) && auth.equals(anotherUser.auth)
                    && password!=null?password.equals(anotherUser.password):anotherUser.password==null
                    && avatarURL!=null?avatarURL.equals(anotherUser.avatarURL):anotherUser.avatarURL==null;
        }
        return false;
    }

    public String getPassword() {
        return password;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public void addJob(Job job) {
        if(this.jobs==null)
            this.jobs = new ArrayList<>();
        this.jobs.add(job);
    }

    /**
     * DO NOT use this method in main Thread, as it will block the current Thread.
     */
    public String login() throws IOException, JSONException, InterruptedException {
        while(Timeline.connection.webService==null) {
            Thread.sleep(10);
        }
        this.auth = Timeline.connection.webService.login(this.name, this.password);
        return auth;
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
        dest.writeString(password);
        dest.writeString(auth);
        dest.writeString(avatarURL);
        // dest.writeParcelable(avatar, flags);
        dest.writeTypedList(jobs);
    }
}
