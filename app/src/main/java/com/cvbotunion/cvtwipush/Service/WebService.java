package com.cvbotunion.cvtwipush.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebService extends Service {
    public static final String SERVER_API = "https://api.cvbot.powerlayout.com/";
    public static final String SERVER_IMAGE = "https://cdn.cvbot.powerlayout.com/images/";
    public static final String SERVER_VIDEO = "https://cdn.cvbot.powerlayout.com/videos/";

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType PLAIN = MediaType.get("text/plain; charset=utf-8");
    public static final MediaType FORM_URLENCODED = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");

    private String auth;
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

    public String login(String username, String password) throws IOException, JSONException {
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);
        RequestBody body = RequestBody.create(data.toString(), JSON);
        Request request = new Request.Builder()
                .url(SERVER_API+"auth/login")
                .method("POST", body)
                .build();
        Response response = mClient.newCall(request).execute();
        if(response.code()==200) {
            JSONObject resJson = new JSONObject(response.body().string());
            response.close();
            if(!resJson.getBoolean("success")) {
                Log.e("WebService.login", resJson.toString());
                return null;
            }
            String token = resJson.getJSONObject("response").getString("token");
            auth = "Bearer "+token;
            return token;
        } else {
            Log.e("WebService.login", response.message());
            response.close();
            return null;
        }
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String token) {
        this.auth = "Bearer "+token;
    }

    public Response request(String method, String url, String data, MediaType contentType) throws IOException {
        //TODO 删掉下一行
        auth = "Bearer xxxxxxxxx";
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
