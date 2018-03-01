package com.ae.benchmark.data;

import android.content.Context;

import java.util.HashMap;

import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.DownloadData;

/**
 * Created by Muhammad Umair on 27-09-2017.
 */

public class CustomerDeliveryItems {
    private static final String COLLECTION_NAME = "CustomerDelHDSet";
    private static final String CUST_DEL_ITEMS = "CustDelHD";
    private static final String TRIP_ID = "ITripId";

    public static void load(Context context,String tripId, DatabaseHandler db){

        HashMap<String, String> params = new HashMap<>();
        params.put(TRIP_ID,tripId);

        HashMap<String,String>expansion = new HashMap<>();
        expansion.put(CUST_DEL_ITEMS,CUST_DEL_ITEMS);

        new DownloadData(context,COLLECTION_NAME,params,expansion,db);
    }
}
