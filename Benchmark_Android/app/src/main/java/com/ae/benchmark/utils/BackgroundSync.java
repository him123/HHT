package com.ae.benchmark.utils;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.ae.benchmark.data.Const;
import com.ae.benchmark.sap.SyncData;

/**
 * Created by eheuristic9 on 04/11/17.
 */

public class BackgroundSync extends Service {

    Context ctx;
    private static Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {



        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        ctx = this;
        startService();
    }

    private void startService()
    {
        timer.scheduleAtFixedRate(new mainTask(), 0, 30000);
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            if(isAppRunning()) {
                if (Const.isInternet()) {
                    //if(!Const.isPostAudit) {
                        startService(new Intent(ctx, SyncData.class));
                    //}
                }
            }else {
                stopSelf();
            }
        }
    }


    public boolean isAppRunning() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
