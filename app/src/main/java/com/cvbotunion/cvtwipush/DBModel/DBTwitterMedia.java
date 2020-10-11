package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterMedia;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, which corresponds to class TwitterMedia.
 */
public class DBTwitterMedia extends LitePalSupport {
    @Column(nullable = false)
    private long tmid;  // TwitterMedia.id

    @Column(nullable = false)
    private long tsid;  // TwitterStatus.id

    private String url;

    private String previewImageURL;

    @Column(nullable = false)
    private int type;

    public DBTwitterMedia() {}

    public DBTwitterMedia(TwitterMedia twitterMedia) {
        this.tmid = Long.parseLong(twitterMedia.id);
        this.url = twitterMedia.url;
        this.previewImageURL = twitterMedia.previewImageURL;
        this.type = twitterMedia.type;
    }

    public TwitterMedia toTwitterMedia() {
        return new TwitterMedia(String.valueOf(tmid), url, type, previewImageURL);
    }

    public long getTmid() {
        return tmid;
    }

    public long getTsid() {
        return tsid;
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

    public void setTmid(long tmid) {
        this.tmid = tmid;
    }

    public void setTsid(long tsid) {
        this.tsid = tsid;
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
