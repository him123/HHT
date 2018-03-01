package com.ae.benchmark.sap;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.NetworkUtil;
/**
 * Created by Rakshit on 08-Feb-17.
 */
public class BackgroundJobLower extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        if(!status.equals(ConfigStore.NO_CONNECTION)){
            context.startService(new Intent(context, SyncData.class));
        }
    }
}
