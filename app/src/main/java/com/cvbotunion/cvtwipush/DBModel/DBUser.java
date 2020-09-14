package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.User;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Model class of the database, which corresponds to class User.
 */
public class DBUser extends LitePalSupport {
    @Column(unique = true,nullable = false)
    private String uid;
    @Column(nullable = false)
    private String name;
    private String avatarURL;

    public DBUser() {}

    public DBUser(User user) {
        this.uid = user.id;
        this.name = user.name;
        this.avatarURL = user.avatarURL;
        for(String gid : user.jobs.keySet()) {
            if(LitePal.where("gid = ? AND uid = ?",gid,uid).find(DBJob.class).isEmpty()) {
                DBJob dbJob = new DBJob(gid,uid,user.jobs.get(gid));
                dbJob.save();
            }
        }
    }

    public User toUser() {
        List<DBJob> dbJobList = LitePal.where("uid = ?",uid).find(DBJob.class);
        HashMap<String, RTGroup.Job> jobs = new HashMap<>();
        for(DBJob j : dbJobList) {
            jobs.put(j.getGid(), j.toJob());
        }
        return new User(uid,name,avatarURL,null,jobs);
    }
}
