package com.example.testapp.DBModel;

import com.example.testapp.Model.TwitterMedia;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, which corresponds to class TwitterMedia.
 */
public class DBTwitterMedia extends LitePalSupport {
    private String tid;  //见DBTwitterStatus的tid注释

    @Column(unique = true)
    private String url;

    private String previewImageURL;

    @Column(nullable = false)
    private int type;

    public TwitterMedia toTwitterMedia() {
        return new TwitterMedia(tid, url, type, previewImageURL);
    }
}
