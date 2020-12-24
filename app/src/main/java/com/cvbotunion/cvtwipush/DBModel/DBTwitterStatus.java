package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class of the database, which corresponds to class TwitterStatus.
 */

public class DBTwitterStatus extends LitePalSupport implements Comparable<DBTwitterStatus> {
    @Column(unique = true, nullable = false)
    private long tsid;  //即为TwitterStatus.id，避免与数据库自动创建的primary key "id"重名

    @Column(nullable = false)
    private String created_at;

    private String text;

    @Column(nullable = false)
    private long tuid;  // TwitterUser.id

    private String in_reply_to_status_id;
    private String in_reply_to_user_id;
    private String in_reply_to_screen_name;
    private String quoted_status_id;
    private String location;

    public DBTwitterStatus() {}

    public DBTwitterStatus(TwitterStatus tweet) {
        this.tsid = Long.parseLong(tweet.id);
        this.created_at = tweet.created_at;
        this.text = tweet.text;
        this.tuid = Long.parseLong(tweet.user.id);
        if(LitePal.where("tuid = ?", String.valueOf(tuid)).find(DBTwitterUser.class).isEmpty()) {
            DBTwitterUser dbTwitterUser = new DBTwitterUser(tweet.user);
            dbTwitterUser.save();
        }
        this.in_reply_to_status_id = tweet.in_reply_to_status_id;
        this.quoted_status_id = tweet.quoted_status_id;
        this.location = tweet.location;
        if(tweet.media != null && !tweet.media.isEmpty()) {
            if(LitePal.where("tsid = ?", tweet.id).find(DBTwitterMedia.class).isEmpty()) {
                for (TwitterMedia m : tweet.media) {
                    DBTwitterMedia media = new DBTwitterMedia(m);
                    media.setTsid(tsid);
                    media.save();
                }
            }
        }
    }

    public TwitterStatus toTwitterStatus() {
        DBTwitterUser user = LitePal.where("tuid = ?", String.valueOf(tuid)).findFirst(DBTwitterUser.class);
        List<DBTwitterMedia> mediaList = LitePal.where("tsid = ?", String.valueOf(tsid)).find(DBTwitterMedia.class);
        if((mediaList == null) || mediaList.isEmpty()){
            if(in_reply_to_status_id != null) {
                return new TwitterStatus(created_at, String.valueOf(tsid), text, user.toTwitterUser(), TwitterStatus.REPLY, in_reply_to_status_id);
            } else if (quoted_status_id != null){
                return new TwitterStatus(created_at, String.valueOf(tsid), text, user.toTwitterUser(), TwitterStatus.QUOTE, quoted_status_id);
            } else {
                return new TwitterStatus(created_at, String.valueOf(tsid), text, user.toTwitterUser());
            }
        } else {
            ArrayList<TwitterMedia> mediaList1 = new ArrayList<>();
            for (DBTwitterMedia m : mediaList) {
                mediaList1.add(m.toTwitterMedia());
            }
            if(in_reply_to_status_id != null) {
                return new TwitterStatus(created_at, String.valueOf(tsid), text, user.toTwitterUser(),mediaList1,TwitterStatus.REPLY, in_reply_to_status_id);
            } else if (quoted_status_id != null){
                return new TwitterStatus(created_at, String.valueOf(tsid), text, user.toTwitterUser(),mediaList1,TwitterStatus.QUOTE, quoted_status_id);
            } else {
                return new TwitterStatus(created_at, String.valueOf(tsid), text, user.toTwitterUser(),mediaList1);
            }
        }
    }

    public long getTsid() {
        return tsid;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getText() {
        return text;
    }

    public TwitterUser getUser() {
        return LitePal.where("tuid = ?", String.valueOf(tuid)).findFirst(DBTwitterUser.class).toTwitterUser();
    }

    public String getIn_reply_to_status_id() {
        return in_reply_to_status_id;
    }

    public String getIn_reply_to_user_id() {
        return in_reply_to_user_id;
    }

    public String getIn_reply_to_screen_name() {
        return in_reply_to_screen_name;
    }

    public String getQuoted_status_id() {
        return quoted_status_id;
    }

    public String getLocation() {
        return location;
    }

    public ArrayList<TwitterMedia> getMedia() {
        List<DBTwitterMedia> dbMediaList = LitePal.where("tsid = ?", String.valueOf(tsid)).find(DBTwitterMedia.class);
        if(!dbMediaList.isEmpty()) {
            ArrayList<TwitterMedia> mediaList = new ArrayList<>();
            for(DBTwitterMedia m:dbMediaList) {
                mediaList.add(m.toTwitterMedia());
            }
            return mediaList;
        }
        else {
            return null;
        }
    }

    public void setTsid(long tsid) {
        this.tsid = tsid;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(TwitterUser user) {
        this.tuid = Long.parseLong(user.id);
        if(LitePal.where("tuid = ?", String.valueOf(tuid)).find(DBTwitterUser.class).isEmpty()) {
            DBTwitterUser dbTwitterUser = new DBTwitterUser(user);
            dbTwitterUser.save();
        }
    }

    public void setIn_reply_to_status_id(String in_reply_to_status_id) {
        this.in_reply_to_status_id = in_reply_to_status_id;
    }

    public void setIn_reply_to_user_id(String in_reply_to_user_id) {
        this.in_reply_to_user_id = in_reply_to_user_id;
    }

    public void setIn_reply_to_screen_name(String in_reply_to_screen_name) {
        this.in_reply_to_screen_name = in_reply_to_screen_name;
    }

    public void setQuoted_status_id(String quoted_status_id) {
        this.quoted_status_id = quoted_status_id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMedia(ArrayList<TwitterMedia> mediaList) {
        if(mediaList != null && !mediaList.isEmpty()) {
            for (TwitterMedia m : mediaList) {
                if(LitePal.where("tsid = ? AND tmid = ?", String.valueOf(tsid), m.id).find(DBTwitterMedia.class).isEmpty()) {
                    DBTwitterMedia media = new DBTwitterMedia(m);
                    media.setTsid(tsid);
                    media.save();
                }
            }
        }
    }

    /**
     * 注意：为自动实现倒序，返回结果与compareTo的默认规定相反
     */
    @Override
    public int compareTo(DBTwitterStatus o) {
        if(o==null) throw new NullPointerException();
        return Long.compare(o.tsid,tsid);
    }
}