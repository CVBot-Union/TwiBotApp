package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RTGroup implements Parcelable {
    public String id;
    public String name;
    public String avatarURL;
    public ArrayList<TwitterUser> following;

    @Nullable public Bitmap avatar;

    @Nullable public ArrayList<User> members = new ArrayList<>();

    public static class Job implements Parcelable{
        public String job;
        private WeakReference<RTGroup> groupRef;
        private int priority;  //权限级别

        public Job(String job, RTGroup group){
            this.job = job;
            this.groupRef = new WeakReference<>(group);
            this.priority = 0;
        }

        public Job(String job, RTGroup group, int priority){
            this(job, group);
            this.priority = priority;
        }

        protected Job(Parcel in) {
            job = in.readString();
            groupRef = new WeakReference<>((RTGroup) in.readParcelable(RTGroup.class.getClassLoader()));
            priority = in.readInt();
        }

        public RTGroup getGroup() {
            return groupRef.get();
        }

        public int getPriority() {
            return priority;
        }

        public void setGroup(RTGroup group) {
            this.groupRef = new WeakReference<>(group);
        }

        public void setPriority(int priority) {
            this.priority = priority;
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
            dest.writeParcelable(groupRef.get(), flags);
            dest.writeInt(priority);
        }
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following) {
        this.id = id;
        this.name = name;
        this.avatarURL = avatarURL;
        this.following = following;
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following,Bitmap avatar){
        this(id,name,avatarURL,following);
        if(avatar != null) {
            this.avatar = avatar;
        }
    }

    public RTGroup(String id, String name, String avatarURL,ArrayList<TwitterUser> following, Bitmap avatar, ArrayList<User> members){
        this(id,name,avatarURL,following,avatar);
        if(members != null){
            this.members = members;
        }
    }

    protected RTGroup(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatarURL = in.readString();
        following = in.createTypedArrayList(TwitterUser.CREATOR);
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        members = in.createTypedArrayList(User.CREATOR);
    }

    public void addMember(User user) {
        if(members != null)
            members.add(user);
    }

    public void addFollowing(TwitterUser twitterUser) {
        if(following != null)
            following.add(twitterUser);
    }

    public void deleteMember(User user) {
        if(members != null && !members.isEmpty())
            for(int i=members.size()-1;i>=0;i--)
                if(members.get(i).id.equals(user.id))
                    members.remove(i);
    }

    public void deleteFollowing(TwitterUser twitterUser) {
        if(following != null && !following.isEmpty())
            for(int i=following.size()-1;i>=0;i--)
                if(following.get(i).screen_name.equals(twitterUser.screen_name))
                    following.remove(i);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatarURL);
        dest.writeTypedList(following);
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
