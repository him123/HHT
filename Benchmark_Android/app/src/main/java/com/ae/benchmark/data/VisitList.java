package com.ae.benchmark.data;
import android.content.Context;

import java.util.HashMap;

import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.DownloadData;
import com.ae.benchmark.utils.DownloadDataJSON;

import static com.ae.benchmark.App.BASE_URL;

/**
 * Created by Rakshit on 21-Dec-16.
 */
public class VisitList {

    private static final String COLLECTION_NAME = BASE_URL+"visitlist.php";
    private static final String TRIP_ID = "ITripId";

    public static void load(Context context,String tripId, DatabaseHandler db){
        HashMap<String, String> params = new HashMap<>();
        params.put(TRIP_ID,tripId);

        HashMap<String,String>expansion = new HashMap<>();

        new DownloadDataJSON(context,COLLECTION_NAME,params,expansion,db);
    }
}
