package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterUser;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, for RTGroup.following.
 */
public class DBFollow extends LitePalSupport {
    private String gid;  // RTGroup.id
    private String twitterUid;  // TwitterUser.id

    public DBFollow(String gid, String twitterUid) {
        this.gid = gid;
        this.twitterUid = twitterUid;
    }

    public TwitterUser getTwitterUser() {
        return LitePal.where("tid = ?", twitterUid).findFirst(DBTwitterUser.class).toTwitterUser();
    }

    public String getGid() {
        return gid;
    }

    public String getTwitterUid() {
        return twitterUid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public void setTwitterUid(String twitterUid) {
        this.twitterUid = twitterUid;
    }
}
