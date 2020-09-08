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

    public DBTwitterUser(TwitterUser user) {
        this.tid = user.id;
        this.name = user.name;
        this.screen_name = user.screen_name;
        this.name_in_group = user.name_in_group;
        this.location = user.location;
        this.followers_count = user.followers_count;
        this.friends_count = user.friends_count;
        this.statuses_count = user.statuses_count;
        this.profile_image_url = user.profile_image_url;
    }

    public TwitterUser toTwitterUser() {
        return new TwitterUser(tid, name, screen_name, name_in_group, profile_image_url);
    }
}
