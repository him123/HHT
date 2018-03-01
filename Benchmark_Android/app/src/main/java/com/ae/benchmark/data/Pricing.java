package com.ae.benchmark.data;
import android.content.Context;

import java.util.HashMap;

import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.DownloadDataJSON;

import static com.ae.benchmark.App.BASE_URL;

/**
 * Created by Rakshit on 21-Dec-16.
 */
public class Pricing {

    private static final String COLLECTION_NAME = BASE_URL+"pricing.php";
    private static final String DRIVER = "Itripid";

    public static void load(Context context,String driver, DatabaseHandler db){
        HashMap<String, String> params = new HashMap<>();
        driver = driver.toUpperCase();
        params.put(DRIVER,driver);
        HashMap<String,String>expansion = new HashMap<>();

        new DownloadDataJSON(context,COLLECTION_NAME,params,expansion,db);
    }
}
