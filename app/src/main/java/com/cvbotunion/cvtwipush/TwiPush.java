package com.cvbotunion.cvtwipush;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.cvbotunion.cvtwipush.Utils.TwiPushReceiver;
import com.dueeeke.videoplayer.ijk.IjkPlayerFactory;
import com.dueeeke.videoplayer.player.VideoViewConfig;
import com.dueeeke.videoplayer.player.VideoViewManager;
import com.mixpush.core.MixPushClient;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
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
        //使用IjkPlayer解码
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                .setPlayerFactory(IjkPlayerFactory.create())
                .build());

        //推送初始化
        //MixPushClient.getInstance().setLogger(new PushLogger(){});
        MixPushClient.getInstance().setPushReceiver(new TwiPushReceiver());
        // 默认初始化5个推送平台（小米推送、华为推送、魅族推送、OPPO推送、VIVO推送），以小米推荐作为默认平台
        MixPushClient.getInstance().register(this);

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

    public static Context getContext() {
        return contextRef.get();
    }
}