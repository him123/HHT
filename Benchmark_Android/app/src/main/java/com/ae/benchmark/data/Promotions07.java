package com.ae.benchmark.data;
import android.content.Context;

import java.util.HashMap;

import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.DownloadData;
/**
 * Created by Rakshit on 21-Dec-16.
 */
public class Promotions07 {

    private static final String COLLECTION_NAME = "Promotion07Set";
    private static final String DRIVER = "Driver";

    public static void load(Context context,String driver, DatabaseHandler db){
        HashMap<String, String> params = new HashMap<>();
        params.put(DRIVER,driver);
        HashMap<String,String>expansion = new HashMap<>();

        new DownloadData(context,COLLECTION_NAME,params,expansion,db);
    }
}
