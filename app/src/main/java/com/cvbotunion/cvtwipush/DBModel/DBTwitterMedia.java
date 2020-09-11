package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, which corresponds to class TwitterMedia.
 */
public class DBTwitterMedia extends LitePalSupport {
    @Column(nullable = false)
    private String tid;  //见DBTwitterStatus的tid注释

    @Column(nullable = false)
    private String statusId;

    private String url;

    private String previewImageURL;

    @Column(nullable = false)
    private int type;

    public DBTwitterMedia() {}

    public DBTwitterMedia(TwitterMedia twitterMedia) {
        this.tid = twitterMedia.id;
        this.url = twitterMedia.url;
        this.previewImageURL = twitterMedia.previewImageURL;
        this.type = twitterMedia.type;
    }

    public TwitterMedia toTwitterMedia() {
        return new TwitterMedia(tid, url, type, previewImageURL);
    }

    public String getTid() {
        return tid;
    }

    public String getStatusId() {
        return statusId;
    }

    public String getUrl() {
        return url;
    }

    public String getPreviewImageURL() {
        return previewImageURL;
    }

    public int getType() {
        return type;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPreviewImageURL(String previewImageURL) {
        this.previewImageURL = previewImageURL;
    }

    public void setType(int type) {
        this.type = type;
    }
}
