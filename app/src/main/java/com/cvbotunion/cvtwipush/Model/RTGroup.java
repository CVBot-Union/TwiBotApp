package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class RTGroup implements Parcelable {
    /*默认形如
    #用户名#
    09-26 12:34

    翻译

    正文

    回复/转推：

    #父推文用户#
    父推文正文
     */
    public static final String DEFAULT_FORMAT = "＃%1$s＃\n%3$s\n\n%5$s%4$s%6$s%7$s\n%9$s";

    public String id;
    public String name;
    public String avatarURL;
    public ArrayList<TwitterUser> following;
    public String tweetFormat;

    @Nullable public Bitmap avatar;

    public static class Job implements Parcelable{
        public String jobName;
        private int priority;  //权限级别

        public Job(String jobName){
            this.jobName = jobName;
            this.priority = 0;
        }

        public Job(String jobName, int priority){
            this.jobName = jobName;
            this.priority = priority;
        }

        protected Job(Parcel in) {
            jobName = in.readString();
            priority = in.readInt();
        }

        public int getPriority() {
            return priority;
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
            dest.writeString(jobName);
            dest.writeInt(priority);
        }
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following) {
        this.id = id;
        this.name = name;
        this.avatarURL = avatarURL;
        this.following = following;
        initDefaultTweetFormat();
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following,Bitmap avatar){
        this(id,name,avatarURL,following);
        if(avatar != null) {
            this.avatar = avatar;
        }
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following,Bitmap avatar,String tweetFormat){
        this(id,name,avatarURL,following);
        if(avatar != null) {
            this.avatar = avatar;
        }
        if(tweetFormat != null){
            this.tweetFormat = tweetFormat;
        }
    }

    protected RTGroup(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatarURL = in.readString();
        following = in.createTypedArrayList(TwitterUser.CREATOR);
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public void initDefaultTweetFormat(){
        /*
        以下4、5、6三项须自主提供换行符。tweetFormat不提供，以避免值为""时出现多余换行
        %1$s：用户名 name
        %2$s：用户 screen_name
        %3$s：时间
        %4$s：推文正文
        %5$s：翻译文本
        %6$s：推文类型名（转推/回复）
        %7$s：父推文用户 name
        %8$s：父推文用户 screen_name
        %9$s: 父推文正文
         */
        /*
        用户设定格式例子：#[名字] [用户名] [yyyy-mm-dd hh:mm] 翻译都给👴起来干活 [推文正文]
         */
        this.tweetFormat = DEFAULT_FORMAT;
    }

    public void setTweetFormat(String tweetFormat) {
        this.tweetFormat = tweetFormat;
    }

    public void addFollowing(TwitterUser twitterUser) {
        if(following != null)
            following.add(twitterUser);
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
