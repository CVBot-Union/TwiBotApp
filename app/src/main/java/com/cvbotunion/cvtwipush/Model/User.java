package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cvbotunion.cvtwipush.TwiPush;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class User implements Parcelable, Serializable, Updatable {
    public static final String serialPath = TwiPush.getContext().getFilesDir().getAbsolutePath();
    public static final String serialFileName = "logged_in_user.ser";
    private static final long serialVersionUID = 1603572591191L;

    public String id;
    public String name;
    private String password;  // 推荐RSA加密过的字符串，不建议存储白文
    private String auth;
    @Nullable public String avatarURL;
    @Nullable public transient Bitmap avatar;  // 不序列化
    public ArrayList<Job> jobs;

    public User(String id, String name, @Nullable String avatarURL,@Nullable Bitmap avatar,ArrayList<Job> jobs){
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

    public User(JSONObject userJson) throws JSONException {
        this.id = userJson.getString("id");
        this.name = userJson.getString("name");
        this.avatarURL = userJson.getString("avatarURL");
        JSONArray jobArray = userJson.getJSONArray("jobs");
        for(int i=0;i<jobArray.length();i++) {
            this.jobs.add(new Job(jobArray.getJSONObject(i)));
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

    public void setPassword(String password) {
        this.password = password;
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

    /**
     * Serialization method for User object.
     * @return {@code boolean} Whether successful or not.
     */
    public boolean writeToDisk() {
        try {
            File userFile = new File(serialPath, serialFileName);
            if (!userFile.exists()) {
                userFile.mkdirs();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userFile));
            oos.writeObject(this);
            oos.close();
            return true;
        } catch (Exception e) {
            Log.w("User.writeToDisk", e.toString());
            return false;
        }
    }

    public static User readFromDisk() {
        try {
            File userFile = new File(serialPath, serialFileName);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFile));
            User savedUser = (User)ois.readObject();
            ois.close();
            return savedUser;
        } catch (Exception e) {
            Log.w("User.readFromDisk", e.toString());
            return null;
        }
    }

    @Override
    public boolean update() {
        // TODO User更新操作
        return false;
    }
}
