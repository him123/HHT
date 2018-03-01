package com.ae.benchmark.data;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.models.Bank;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.DownloadDataJSON;

import static com.ae.benchmark.App.BASE_URL;

/**
 * Created by Rakshit on 23-Jan-17.
 */
public class Banks {
    private static final String COLLECTION_NAME = BASE_URL+"banklist.php";
    private static final String TRIP_ID = "Country";

    private static ArrayList<Bank> data = new ArrayList<>();

    public static void load(Context context,String tripId, DatabaseHandler db){
        HashMap<String, String> params = new HashMap<>();
//        params.put(TRIP_ID,"SA");

        HashMap<String,String>expansion = new HashMap<>();

        new DownloadDataJSON(context,COLLECTION_NAME,params,expansion,db);
    }

    public static void loadData(Context context){
        data.clear();
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_BANK_CODE, "");
        map.put(db.KEY_BANK_NAME, "");
        HashMap<String, String> filter = new HashMap<>();
        Cursor cursor = db.getData(db.BANKS, map, filter);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Bank bank = new Bank();
                bank.setBankCode(cursor.getString(cursor.getColumnIndex(db.KEY_BANK_CODE)));
                bank.setBankName(cursor.getString(cursor.getColumnIndex(db.KEY_BANK_NAME)));
                data.add(bank);
            }
            while (cursor.moveToNext());
        }
    }

    public static ArrayList<Bank> get() {
        return data;
    }
}
