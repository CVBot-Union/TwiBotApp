package com.cvbotunion.cvtwipush.Utils;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.cvbotunion.cvtwipush.Activities.LoginActivity;
import com.cvbotunion.cvtwipush.Activities.Timeline;
import com.cvbotunion.cvtwipush.Model.Job;
import com.cvbotunion.cvtwipush.Model.RTGroup;
import com.cvbotunion.cvtwipush.Model.TwitterUser;
import com.cvbotunion.cvtwipush.Model.User;
import com.cvbotunion.cvtwipush.Service.WebService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import okhttp3.Response;

public class LoginRunnable implements Runnable {
    private static final Handler handler = new Handler();

    private final WeakReference<LoginActivity> activityRef;
    private final String username;
    private String password;

    public LoginRunnable(LoginActivity activity, String username, String password) {
        this.activityRef = new WeakReference<>(activity);
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        // String publicKey;
        User user;
        try {
            if (Timeline.connection.webService == null) {
                synchronized (Timeline.connection.flag) {
                    Timeline.connection.flag.wait();
                }
            }
            // publicKey = Timeline.connection.webService.queryPublicKey();
            // password = RSACrypto.getInstance().encrypt(password, publicKey);
            password = Timeline.connection.webService.encryptPassword(password);
            String auth = Timeline.connection.webService.login(username, password);
            Response response = Timeline.connection.webService.get(WebService.SERVER_API + "v2/user/");
            if (response.code() == 200) {
                JSONObject resJson = new JSONObject(response.body().string());
                response.close();
                if (resJson.getBoolean("success")) {
                    user = new User(resJson.getJSONObject("response"));
                    user.setAuth(auth);
                    user.setPassword(password);
                } else {
                    throw new Exception(resJson.toString());
                }
            } else {
                response.close();
                throw new Exception("fail to connect while getting current user");
            }
            for (int i=0;i<user.jobs.size();i++) {
                Response groupResponse = Timeline.connection.webService.get(WebService.SERVER_API + "v2/rtgroup/" + user.jobs.get(i).group.id);
                if (groupResponse.code() == 200) {
                    JSONObject resJson = new JSONObject(groupResponse.body().string());
                    groupResponse.close();
                    if (resJson.getBoolean("success")) {
                        JSONObject groupJson = resJson.getJSONObject("response");
                        user.jobs.get(i).group.name = groupJson.getString("name");
                        user.jobs.get(i).group.avatarURL = groupJson.getString("avatarURL");
                        // TODO
                        user.jobs.get(i).group.tweetFormat = RTGroup.DEFAULT_FORMAT;
                        // user.jobs.get(i).group.tweetFormat = groupJson.getString("tweetFormat");
                        JSONArray followingJson = groupJson.getJSONArray("following");
                        for (int j = 0; j < followingJson.length(); j++) {
                            JSONObject item = followingJson.getJSONObject(j);
                            TwitterUser singleTU = new TwitterUser(
                                    item.getString("uid"),
                                    item.getString("nickname"),
                                    null,
                                    item.getString("nickname"),
                                    WebService.SERVER_API + "lookup/avatar/id/" + item.getString("uid")+".png");
                            user.jobs.get(i).group.following.add(singleTU);
                        }
                        JSONArray members = groupJson.getJSONArray("members");
                        for (int k = 0; k < members.length(); k++) {
                            user.jobs.get(i).group.members.add(members.getString(k));
                        }
                    } else {
                        throw new Exception("fail to get groups");
                    }
                } else {
                    groupResponse.close();
                    throw new Exception("fail to connect while getting groups");
                }
            }
            handler.post(() -> {
                activityRef.get().progressBar.setVisibility(View.GONE);
                Timeline.setCurrentUser(user);
                user.writeToDisk();
                activityRef.get().setResult(123);
                activityRef.get().onBackPressed();
            });
        } catch (Exception e) {
            Log.e("LoginRunnable", e.toString());
            handler.post(() -> {
                activityRef.get().progressBar.setVisibility(View.GONE);
                if (e.getMessage().contains("403")) {
                    activityRef.get().messageView.setText("用户名或密码错误");
                } else {
                    activityRef.get().messageView.setText("登录失败，请检查网络连接");
                }
            });
        }
    }
}
