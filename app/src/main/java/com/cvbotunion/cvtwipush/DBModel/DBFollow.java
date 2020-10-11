package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.TwitterUser;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, for RTGroup.following.
 */
public class DBFollow extends LitePalSupport {
    private String gid;  // RTGroup.id
    private long tuid;  // TwitterUser.id

    public DBFollow(String gid, String tuid) {
        this.gid = gid;
        this.tuid = Long.valueOf(tuid);
    }

    public TwitterUser getTwitterUser() {
        return LitePal.where("tuid = ?", String.valueOf(tuid)).findFirst(DBTwitterUser.class).toTwitterUser();
    }

    public String getGid() {
        return gid;
    }

    public Long getTuid() {
        return tuid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public void setTuid(Long tuid) {
        this.tuid = tuid;
    }
}
