package com.ae.benchmark.sap;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.NetworkUtil;
/**
 * Created by Rakshit on 11-Jan-17.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        Toast.makeText(context,status,Toast.LENGTH_LONG).show();
        if(!status.equals(ConfigStore.NO_CONNECTION)){
            context.startService(new Intent(context, SyncData.class));
        }
    }
}
