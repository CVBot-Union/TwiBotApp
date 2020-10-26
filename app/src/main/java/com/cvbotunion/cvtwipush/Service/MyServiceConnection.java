package com.cvbotunion.cvtwipush.Service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MyServiceConnection implements ServiceConnection {
    public final Object flag = new Object();
    public WebService webService;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        WebService.WebBinder binder = (WebService.WebBinder) iBinder;
        webService = binder.getService();
        synchronized (flag) {
            flag.notifyAll();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        webService = null;
    }
}
