package com.cvbotunion.cvtwipush;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.LogManager;

public class TwiPush extends Application {

    public static final String APP_ID = "2882303761518650494";
    public static final String APP_KEY = "5831865030494";
    public static final String TAG = "com.cvbotunion.cvtwipush";
    private static WeakReference<Context> contextRef;

    @Override
    public void onCreate() {
        super.onCreate();
        contextRef = new WeakReference<>(getApplicationContext());
        //初始化push推送服务
        if(shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }
        //打开Log
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };
        LogManager.getLogManager().getLogger(newLogger.getClass().getName());
        LitePal.initialize(this);
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        assert am != null;
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getApplicationInfo().processName;
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public static Context getContext() {
        return contextRef.get();
    }
}