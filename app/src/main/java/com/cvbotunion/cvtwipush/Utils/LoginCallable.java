package com.cvbotunion.cvtwipush.Utils;

import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Model.Job;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.Model.User;
import com.cvbotunion.cvtwipush.Service.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import okhttp3.Response;

public class LoginCallable implements Callable<User> {
    private final String username;
    private String password;

    public LoginCallable(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public User call() throws Exception {
        String publicKey;
        User user;
        if(Timeline.connection.webService==null) {
            synchronized (Timeline.connection.flag) {
                Timeline.connection.flag.wait();
            }
        }
        publicKey = Timeline.connection.webService.queryPublicKey();
        password = RSACrypto.getInstance().encrypt(password, publicKey);
        String auth = Timeline.connection.webService.login(username, password);
        Response response = Timeline.connection.webService.get(WebService.SERVER_API+"v2/user/");
        if(response.code()==200) {
            JSONObject resJson = new JSONObject(response.body().string());
            response.close();
            if(resJson.getBoolean("success")) {
                user = new User(resJson.getJSONObject("response"));
                user.setAuth(auth);
                user.setPassword(password);
            } else {
                throw new Exception(resJson.toString());
            }
        } else {
            response.close();
            if(response.code()==403) {
                throw new Exception("403");
            } else {
                throw new Exception("fail to connect while getting current user");
            }
        }
        for(Job job : user.jobs) {
            Response groupResponse = Timeline.connection.webService.get(WebService.SERVER_API+"v2/rtgroup/"+job.group.id);
            if(groupResponse.code()==200) {
                JSONObject resJson = new JSONObject(groupResponse.body().string());
                groupResponse.close();
                if(resJson.getBoolean("success")) {
                    JSONObject groupJson = resJson.getJSONObject("response");
                    job.group.name = groupJson.getString("name");
                    job.group.avatarURL = groupJson.getString("avatarURL");
                    job.group.tweetFormat = groupJson.getString("tweetFormat");
                    JSONArray followingJson = groupJson.getJSONArray("following");
                    for(int j=0;j<followingJson.length();j++) {
                        JSONObject item = followingJson.getJSONObject(j);
                        TwitterUser singleTU = new TwitterUser(
                                item.getString("uid"),
                                item.getString("nickname"),
                                null,
                                item.getString("nickname"),
                                WebService.SERVER_API+"lookup/avatar/id/"+item.getString("uid"));
                        job.group.following.add(singleTU);
                    }
                    JSONArray members = groupJson.getJSONArray("members");
                    for(int k=0;k<members.length();k++) {
                        job.group.members.add(members.getString(k));
                    }
                } else {
                    throw new Exception("fail to get groups");
                }
            } else {
                groupResponse.close();
                throw new Exception("fail to connect while getting groups");
            }
        }
        return user;
    }
}
