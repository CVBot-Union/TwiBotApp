package com.cvbotunion.cvtwipush.DBModel;

import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.User;

import org.litepal.LitePal;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * Model class of the database, which corresponds to class RTGroup.Job.
 */
public class DBJob extends LitePalSupport {
    @Column(nullable = false)
    private String jobName;
    @Column(nullable = false)
    private String gid;  // RTGroup.id
    @Column(nullable = false)
    private String uid;  // User.id
    @Column(nullable = false)
    private int priority;

    public DBJob(String gid, String uid, RTGroup.Job job) {
        this.jobName = job.jobName;
        this.gid = gid;
        this.uid = uid;
        this.priority = job.getPriority();
    }

    public RTGroup.Job toJob() {
        return new RTGroup.Job(jobName,priority);
    }

    public String getGid() {
        return gid;
    }

    public String getUid() {
        return uid;
    }
}
