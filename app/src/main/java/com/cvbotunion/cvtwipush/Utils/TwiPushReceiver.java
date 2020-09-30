package com.cvbotunion.cvtwipush.Utils;

import android.content.Context;

import com.mixpush.core.MixPushMessage;
import com.mixpush.core.MixPushPlatform;
import com.mixpush.core.MixPushReceiver;

public class TwiPushReceiver extends MixPushReceiver {
    @Override
    public void onRegisterSucceed(Context context, MixPushPlatform mixPushPlatform) {
        // 这里需要实现上传regId和推送平台信息到服务端保存，
        //也可以通过MixPushClient.getInstance().getRegisterId的方式实现
        System.out.println("测试id：");
        System.out.println(mixPushPlatform.getPlatformName());
        System.out.println(mixPushPlatform.getRegId());
    }

    @Override
    public void onNotificationMessageClicked(Context context, MixPushMessage message) {
        // TODO 通知栏消息点击触发，实现打开具体页面，打开浏览器等。
        System.out.print(message.getPayload());
    }
}
