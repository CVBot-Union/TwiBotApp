package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;
import com.cvbotunion.cvtwipush.Model.TwitterUser;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;

/**
 * Model class of the database, which corresponds to class TwitterStatus.
 */

public class DBTwitterStatus extends LitePalSupport {
    private String tid;  //即为TwitterStatus的id，避免与数据库自动创建的primary key "id"重名

    @Column(nullable = false)
    private String created_at;

    private String text;

    @Column(nullable = false)
    private DBTwitterUser user;

    private String in_reply_to_status_id;
    private String in_reply_to_user_id;
    private String in_reply_to_screen_name;
    private String quoted_status_id;
    private String location;
    private ArrayList<String> hashtags;
    private ArrayList<DBTwitterUser> user_mentions=new ArrayList<>();
    private ArrayList<DBTwitterMedia> media=new ArrayList<>();

    public DBTwitterStatus(TwitterStatus twitter) {
        this.created_at = twitter.created_at;
        this.text = twitter.text;
        this.user = new DBTwitterUser(twitter.user);
        this.in_reply_to_status_id = twitter.in_reply_to_status_id;
        this.quoted_status_id = twitter.quoted_status_id;
        this.location = twitter.location;
        this.hashtags = twitter.hashtags;
        if(twitter.user_mentions != null && !twitter.user_mentions.isEmpty()) {
            int j=0;
            for(TwitterUser u : twitter.user_mentions) {
                this.user_mentions.set(j, new DBTwitterUser(u));
                j++;
            }
        }
        if(twitter.media != null && !twitter.media.isEmpty()) {
            int k=0;
            for(TwitterMedia m : twitter.media) {
                this.media.set(k, new DBTwitterMedia(m));
                k++;
            }
        }
    }

    public TwitterStatus toTwitterStatus() {
        if((media == null) || media.isEmpty()){
            if(in_reply_to_status_id != null) {
                return new TwitterStatus(created_at, tid, text, user.toTwitterUser(), TwitterStatus.REPLY, in_reply_to_status_id);
            } else if (quoted_status_id != null){
                return new TwitterStatus(created_at, tid, text, user.toTwitterUser(), TwitterStatus.QUOTE, quoted_status_id);
            } else {
                return new TwitterStatus(created_at, tid, text, user.toTwitterUser());
            }
        } else {
            int i = 0;
            ArrayList<TwitterMedia> media1 = new ArrayList<>();
            for (DBTwitterMedia m : media) {
                media1.set(i, m.toTwitterMedia());
                i++;
            }
            if(in_reply_to_status_id != null) {
                return new TwitterStatus(created_at, tid, text, user.toTwitterUser(),media1,TwitterStatus.REPLY, in_reply_to_status_id);
            } else if (quoted_status_id != null){
                return new TwitterStatus(created_at, tid, text, user.toTwitterUser(),media1,TwitterStatus.QUOTE, quoted_status_id);
            } else {
                return new TwitterStatus(created_at, tid, text, user.toTwitterUser(),media1);
            }
        }
    }
}