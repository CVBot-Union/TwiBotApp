package com.cvbotunion.cvtwipush.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import org.litepal.LitePal;

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
    public ArrayList<String> members; // String -> User.id
    public String tweetFormat;
    @Nullable public Bitmap avatar;

    public RTGroup(){
        this.following = new ArrayList<>();
        this.members = new ArrayList<>();
        initDefaultTweetFormat();
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following,ArrayList<String> members) {
        this();
        this.id = id;
        this.name = name;
        this.avatarURL = avatarURL;
        if(following != null) {
            this.following = following;
        }
        if(members != null) {
            this.members = members;
        }
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following,ArrayList<String> members,Bitmap avatar){
        this(id,name,avatarURL,following,members);
        if(avatar != null) {
            this.avatar = avatar;
        }
    }

    public RTGroup(String id,String name,String avatarURL,ArrayList<TwitterUser> following,ArrayList<String> members,Bitmap avatar,String tweetFormat){
        this(id,name,avatarURL,following,members,avatar);
        if(tweetFormat != null){
            this.tweetFormat = tweetFormat;
        }
    }

    protected RTGroup(Parcel in) {
        id = in.readString();
        name = in.readString();
        avatarURL = in.readString();
        following = in.createTypedArrayList(TwitterUser.CREATOR);
        members = in.createStringArrayList();
        tweetFormat = in.readString();
        // avatar = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj instanceof RTGroup) {
            RTGroup anotherGroup = (RTGroup)obj;
            if(following.size()!=anotherGroup.following.size() || members.size()!=anotherGroup.members.size())
                return false;
            for(int i=0;i<following.size();i++) {
                if(!following.get(i).equals(anotherGroup.following.get(i)))
                    return false;
            }
            for(int j=0;j<members.size();j++) {
                if(!members.get(j).equals(anotherGroup.members.get(j)))
                    return false;
            }
            return id.equals(anotherGroup.id) && name.equals(anotherGroup.name)
                    && avatarURL!=null?avatarURL.equals(anotherGroup.avatarURL):anotherGroup.avatarURL==null
                    && tweetFormat.equals(anotherGroup.tweetFormat);
        }
        return false;
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
        if(following == null) { following = new ArrayList<>(); }
        following.add(twitterUser);
    }

    public void deleteFollowing(TwitterUser twitterUser) {
        deleteFollowing(twitterUser.id);
    }

    public void deleteFollowing(String twitterUserId) {
        if(following != null && !following.isEmpty())
            for(int i=following.size()-1;i>=0;i--)
                if(following.get(i).id.equals(twitterUserId))
                    following.remove(i);
    }

    public void addMember(User user) {
        if(members==null) { members = new ArrayList<>(); }
        members.add(user.id);
    }

    public void deleteMember(User user) {
        deleteMember(user.id);
    }

    public void deleteMember(String userId) {
        if(members != null && !members.isEmpty())
            for(int i=members.size()-1;i>=0;i--)
                if(members.get(i).equals(userId))
                    members.remove(i);
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(avatarURL);
        dest.writeTypedList(following);
        dest.writeStringList(members);
        dest.writeString(tweetFormat);
        // dest.writeParcelable(avatar, flags);
    }
}
