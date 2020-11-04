package com.cvbotunion.cvtwipush.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebService extends Service {
    // 备用 https://cn.api.cvbot.powerlayout.com/
    public static final String SERVER_API = "https://api.cvbot.powerlayout.com/";
    public static final String SERVER_IMAGE = "https://cdn.cvbot.powerlayout.com/images/";
    public static final String SERVER_VIDEO = "https://cdn.cvbot.powerlayout.com/videos/";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType PLAIN = MediaType.get("text/plain; charset=utf-8");
    public static final MediaType FORM_URLENCODED = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");

    public Pattern keyPattern = Pattern.compile("-\\s+(.+?)\\s+-", Pattern.DOTALL);

    private static String auth;
    private final IBinder mBinder = new WebBinder();
    private final OkHttpClient mClient = new OkHttpClient.Builder()
            //.retryOnConnectionFailure(false)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public class WebBinder extends Binder {
        public WebService getService() {
            return WebService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public OkHttpClient getClient() {
        return mClient;
    }

    public String getAuth() {
        return auth;
    }

    public static void setAuth(String newAuth) {
        auth = newAuth;
    }

    public String queryPublicKey() throws Exception {
        Log.i("web connection", SERVER_API+"auth/public-key");
        Request request = new Request.Builder()
                .url(SERVER_API+"auth/public-key")
                .get()
                .build();
        Response response = mClient.newCall(request).execute();
        if(response.code()==200) {
            String key = response.body().string();
            response.close();
            Matcher matcher = keyPattern.matcher(key);
            matcher.find();
            key = matcher.group(1);
            return key;
        } else {
            Log.e("WebService.queryPublicKey", response.message());
            response.close();
            throw new Exception("连接失败");
        }
    }

    public String encryptPassword(String password) throws Exception {
        Log.i("web connection", SERVER_API+"auth/encrypt");
        RequestBody body = RequestBody.create("password="+password, FORM_URLENCODED);
        Request request = new Request.Builder()
                .url(SERVER_API+"auth/encrypt")
                .post(body)
                .build();
        Response response = mClient.newCall(request).execute();
        if(response.code()==200) {
            String pwd = response.body().string();
            response.close();
            return pwd;
        } else {
            Log.e("WebService.encryptPassword", response.code()+" "+response.message());
            response.close();
            throw new Exception("encryptPassword connection failed");
        }
    }

    public String login(String username, String password) throws Exception {
        Log.i("web connection", SERVER_API+"auth/login");
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);
        RequestBody body = RequestBody.create(data.toString(), JSON);
        Request request = new Request.Builder()
                .url(SERVER_API+"auth/login")
                .header("x-tian-wang-gai-di-hu", "bao-ta-zhen-he-yao")
                .method("POST", body)
                .build();
        Response response = mClient.newCall(request).execute();
        if(response.code()==200) {
            JSONObject resJson = new JSONObject(response.body().string());
            response.close();
            if(!resJson.getBoolean("success")) {
                Log.e("WebService.login", resJson.toString());
                throw new Exception("login failed");
            }
            String token = resJson.getJSONObject("response").getString("token");
            auth = "Bearer "+token;
            return auth;
        } else {
            String msg = response.code()+" "+response.message();
            response.close();
            throw new Exception(msg);
        }
    }

    public Response request(String method, String url, String data, MediaType contentType) throws IOException {
        Log.i("web connection", url);
        RequestBody body = (data!=null)? RequestBody.create(data, contentType) : null;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", auth)
                .header("Connection", "Keep-Alive")
                .method(method, body)
                .build();
        return mClient.newCall(request).execute();
    }

    public Response get(String url) throws IOException {
        return request("GET", url, null, null);
    }

    public Response post(String url, String data, MediaType contentType) throws IOException {
        return request("POST", url, data, contentType);
    }
}
