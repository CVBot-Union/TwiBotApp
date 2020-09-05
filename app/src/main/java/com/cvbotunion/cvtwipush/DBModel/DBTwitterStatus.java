package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;
import com.cvbotunion.cvtwipush.Model.TwitterStatus;

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

    public TwitterStatus toTwitterStatus() {
        if(media.isEmpty())
            return new TwitterStatus(created_at, tid, text, user.toTwitterUser());
        else {
            int i=0;
            ArrayList<TwitterMedia> media1 = new ArrayList<>();
            for(DBTwitterMedia m:media) {
                media1.set(i, m.toTwitterMedia());
                i++;
            }
            return new TwitterStatus(created_at, tid, text, user.toTwitterUser(), media1);
        }
    }
}
