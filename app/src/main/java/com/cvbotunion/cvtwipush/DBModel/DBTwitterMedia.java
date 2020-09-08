package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, which corresponds to class TwitterMedia.
 */
public class DBTwitterMedia extends LitePalSupport {
    @Column(unique = true)
    private String tid;  //见DBTwitterStatus的tid注释

    private String url;

    private String previewImageURL;

    @Column(nullable = false)
    private int type;

    public DBTwitterMedia(TwitterMedia twitterMedia) {
        this.tid = twitterMedia.id;
        this.url = twitterMedia.url;
        this.previewImageURL = twitterMedia.previewImageURL;
        this.type = twitterMedia.type;
    }

    public TwitterMedia toTwitterMedia() {
        return new TwitterMedia(tid, url, type, previewImageURL);
    }
}
