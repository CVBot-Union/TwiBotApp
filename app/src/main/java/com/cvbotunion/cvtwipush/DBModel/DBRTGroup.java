package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.TwitterUser;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class of the database, which corresponds to class RTGroup.
 */
public class DBRTGroup extends LitePalSupport {
    @Column(unique = true, nullable = false)
    private String gid;
    @Column(nullable = false)
    private String name;
    private String avatarURL;
    private String tweetFormat;

    public DBRTGroup() {}

    public DBRTGroup(RTGroup group) {
        this.gid = group.id;
        this.name = group.name;
        this.avatarURL = group.avatarURL;
        this.tweetFormat = group.tweetFormat;
        for(TwitterUser u : group.following) {
            if(LitePal.where("gid = ? AND tuid = ?",gid,u.id).find(DBFollow.class).isEmpty()) {
                DBFollow dbFollow = new DBFollow(gid,u.id);
                dbFollow.save();
            }
            if(LitePal.where("tuid = ?",u.id).find(DBTwitterUser.class).isEmpty()) {
                DBTwitterUser dbTwitterUser = new DBTwitterUser(u);
                dbTwitterUser.save();
            }
        }
    }

    public RTGroup toRTGroup() {
        return new RTGroup(gid,name,avatarURL,getFollowingList(),null, tweetFormat);
    }

    public ArrayList<TwitterUser> getFollowingList() {
        List<DBFollow> dbFollowList = LitePal.where("gid = ?", gid).find(DBFollow.class);
        ArrayList<TwitterUser> followingList = new ArrayList<>();
        for(DBFollow f : dbFollowList)
            followingList.add(f.getTwitterUser());
        return followingList;
    }
}
