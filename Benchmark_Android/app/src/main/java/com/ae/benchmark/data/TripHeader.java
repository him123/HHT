package com.ae.benchmark.data;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.DashboardActivity;
import com.ae.benchmark.sap.IntegrationService;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.DownloadData;
import com.ae.benchmark.utils.DownloadDataJSON;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

import static com.ae.benchmark.App.BASE_URL;

/**
 * Created by Rakshit on 17-Dec-16.
 */
public class TripHeader {
    private static final String COLLECTION_NAME = BASE_URL+"route.php";
//    private static final String TRIP_SALES_AREA = "TripSalesArea";
    private static final String TRIP_ID = "ITripId";

    public static void load(Context context,String tripId, DatabaseHandler db){
       // Log.e("Inside TH","TH");
        HashMap<String, String>params = new HashMap<>();
        params.put(TRIP_ID,tripId);

        //params.put(TRIP_ID,"0000040000000044");
        HashMap<String,String>expansion = new HashMap<>();
//        expansion.put(TRIP_SALES_AREA,TRIP_SALES_AREA);

        new DownloadDataJSON(context,COLLECTION_NAME,params,expansion,db);
        // new downloadData("GBC012000000001");
    }
}
