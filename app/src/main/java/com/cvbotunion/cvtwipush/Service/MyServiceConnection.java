package com.cvbotunion.cvtwipush.Service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MyServiceConnection implements ServiceConnection {
    public WebService webService;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        WebService.WebBinder binder = (WebService.WebBinder) iBinder;
        webService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        webService = null;
    }
}
