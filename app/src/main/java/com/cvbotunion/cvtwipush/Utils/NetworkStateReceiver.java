package com.cvbotunion.cvtwipush.Utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


/**
 * Created by Carson_Ho on 16/10/31.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //网络状态检测
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isAvailable()) {
            Toast.makeText(context, "无网络连接", Toast.LENGTH_LONG).show();
        }
    }
}