package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterUser;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, which corresponds to class TwitterUser.
 */
public class DBTwitterUser extends LitePalSupport {
    private String tid;  //见DBTwitterStatus的tid注释
    private String name;

    @Column(unique = true, nullable = false)
    private String screen_name;

    private String name_in_group;
    private String location;
    private String followers_count;
    private String friends_count;
    private String statuses_count;
    private String profile_image_url;

    public TwitterUser toTwitterUser() {
        return new TwitterUser(tid, name, screen_name, name_in_group, profile_image_url);
    }
}
