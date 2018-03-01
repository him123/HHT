package com.ae.benchmark.activities;
/**
 * Created by Muhammad Umair on 05/12/2016.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;

import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.sap.IntegrationServiceJSON;
import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Unload;
import com.ae.benchmark.models.UnloadSummaryPrint;
import com.ae.benchmark.sap.IntegrationService;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.PrinterHelper;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
public class UnloadActivity extends AppCompatActivity {
    GridView gridView;
    Button processUnloadInventory;
    Button btn_bad_return;
    Button btn_bad_return_variance;
    Button btn_fresh_unload;
    Button btn_ending_inventory;
    Button btn_inventory_variance;
    Button btn_truck_damage;
    DatabaseHandler db = new DatabaseHandler(this);
    LoadingSpinner loadingSpinner;
    String purchaseNumber="";
    int referenceCount = 0;
    int postCount = 0;
    private ArrayList<Unload>arrayList = new ArrayList<>();
    public ArrayList<ArticleHeader> articles;
    private ArrayList<Unload> dataStoreList = new ArrayList<>();
    boolean isPrint = false;
    static final String[] badReturnitems = new String[]{
            "Bad Return", "Truck Damage", "Fresh Unload", "Ending Inventory", "INV. Variance", "Bad RTN. Variance"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unload);
        setTitle(getString(R.string.unload_inventory));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        articles = ArticleHeaders.get();
        loadingSpinner = new LoadingSpinner(this);
        btn_bad_return = (Button)findViewById(R.id.btn_bad_return);
        btn_bad_return_variance = (Button)findViewById(R.id.btn_bad_return_variance);
        btn_fresh_unload = (Button)findViewById(R.id.btn_fresh_unload);
        btn_ending_inventory = (Button)findViewById(R.id.btn_ending_inventory);
        btn_inventory_variance = (Button)findViewById(R.id.btn_inventory_variance);
        btn_truck_damage = (Button)findViewById(R.id.btn_truck_damage);

        btn_bad_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent intent = new Intent(UnloadActivity.this,UnloadDetailActivity.class);
                Helpers.logData(UnloadActivity.this,"Going for Bad Return Details");
                Intent intent = new Intent(UnloadActivity.this,UnloadActivityBadReturnList.class);
                intent.putExtra("context","badreturn");
                startActivity(intent);
            }
        });

        btn_bad_return_variance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(UnloadActivity.this,"Going for Unload Detail(Bad Return Variance)");
                Intent intent = new Intent(UnloadActivity.this,UnloadDetailActivity.class);
                intent.putExtra("context","badreturnvariance");
                startActivity(intent);
            }
        });

        btn_fresh_unload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(UnloadActivity.this,"Going for Unload Detail(Fresh Unload)");
                Intent intent = new Intent(UnloadActivity.this,UnloadDetailActivity.class);
                intent.putExtra("context","freshunload");
                startActivity(intent);
            }
        });

        btn_ending_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(UnloadActivity.this,"Going for Unload Detail(Ending Inventory)");
                Intent intent = new Intent(UnloadActivity.this,UnloadDetailActivity.class);
                intent.putExtra("context","endinginventory");
                startActivity(intent);
            }
        });

        btn_inventory_variance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(UnloadActivity.this,"Going for Unload Detail(Inventory Variance)");
                Intent intent = new Intent(UnloadActivity.this,UnloadDetailActivity.class);
                intent.putExtra("context","inventoryvariance");
                startActivity(intent);
            }
        });

        btn_truck_damage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(UnloadActivity.this,"Going for Unload Detail(Truck Damage)");
                Intent intent = new Intent(UnloadActivity.this,UnloadDetailActivity.class);
                intent.putExtra("context","truckdamage");
                startActivity(intent);
            }
        });

       // gridView = (GridView) findViewById(R.id.gridView1);
        processUnloadInventory = (Button) findViewById(R.id.btnUnloadInventory);
        processUnloadInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UnloadActivity.this);
                alertDialogBuilder.setTitle(getString(R.string.message))
                        .setMessage(getString(R.string.unload_msg))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Helpers.logData(UnloadActivity.this, "Posting unload");
                                final Dialog pd = new Dialog(UnloadActivity.this);
                                pd.setContentView(R.layout.dialog_doprint);
                                pd.setCancelable(false);
                                pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                LinearLayout btn_print = (LinearLayout) pd.findViewById(R.id.ll_print);
                                LinearLayout btn_notprint = (LinearLayout) pd.findViewById(R.id.ll_notprint);
                                pd.show();
                                btn_print.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //new loadDataforPrint().execute();
                                        Helpers.logData(UnloadActivity.this,"Clicked on print");
                                        if (unloadVarianceExist(App.ENDING_INVENTORY)||unloadVarianceExist(App.THEFT) || unloadVarianceExist(App.TRUCK_DAMAGE)||unloadVarianceExist(App.EXCESS)) {
                                            Helpers.logData(UnloadActivity.this,"Ending Inventory Exist");
                                            isPrint = true;
                                            new postDataNew().execute();
                                            pd.dismiss();
                                        } else {
                                            Helpers.logData(UnloadActivity.this,"No ending inventory or theft or any variance.Directly clearing vanstock");
                                            clearVanStock();   //For development purpose.
                                            HashMap<String, String> altMap = new HashMap<>();
                                            altMap.put(db.KEY_IS_UNLOAD, "true");
                                            HashMap<String, String> filterMap = new HashMap<>();
                                            filterMap.put(db.KEY_IS_UNLOAD, "false");
                                            db.updateData(db.LOCK_FLAGS, altMap, filterMap);
                                            pd.dismiss();
                                            Intent intent = new Intent(UnloadActivity.this, DashboardActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                });
                                btn_notprint.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Helpers.logData(UnloadActivity.this,"Clicked on do not print");
                                        if (unloadVarianceExist(App.ENDING_INVENTORY)||unloadVarianceExist(App.THEFT) || unloadVarianceExist(App.TRUCK_DAMAGE)||unloadVarianceExist(App.EXCESS)) {
                                            isPrint=false;
                                            Helpers.logData(UnloadActivity.this,"Variance Exist");
                                            new postDataNew().execute();
                                            pd.dismiss();
                                        } else {
                                            clearVanStock(); //for testing purpose
                                            HashMap<String, String> altMap = new HashMap<>();
                                            altMap.put(db.KEY_IS_UNLOAD, "true");
                                            HashMap<String, String> filterMap = new HashMap<>();
                                            filterMap.put(db.KEY_IS_UNLOAD, "false");
                                            db.updateData(db.LOCK_FLAGS, altMap, filterMap);
                                            pd.dismiss();
                                            Intent intent = new Intent(UnloadActivity.this, DashboardActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                });



                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();

            }
        });
        /*processUnloadInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(UnloadActivity.this);
                dialog.setContentView(R.layout.dialog_doprint);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
                LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);
                dialog.show();
                btn_print.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Variances are recorded(Ending Inventory/Theft or Missing/Truck Damage
                        try {
                            if (unloadVarianceExist("")) {
                                //Checking any record exist for ZDRX
                                if (unloadVarianceExist(App.THEFT) || unloadVarianceExist(App.TRUCK_DAMAGE)) {
                                    //Checking does it exist for both
                                    if (unloadVarianceExist(App.THEFT) && unloadVarianceExist(App.TRUCK_DAMAGE)) {
                                        referenceCount++;
                                        new postData(App.THEFT, App.TRUCK_DAMAGE);
                                    }
                                    //Check if it exists only for Theft
                                    else if (unloadVarianceExist(App.THEFT)) {
                                        referenceCount++;
                                        new postData(App.THEFT);
                                    }
                                    //It only exist for Truck Damage
                                    else {
                                        referenceCount++;
                                        new postData(App.TRUCK_DAMAGE);
                                    }
                                }
                                //Checking if  Any excess product exist
                                if (unloadVarianceExist(App.EXCESS)) {
                                    referenceCount++;
                                    new postData(App.EXCESS);
                                }
                                //Check if any ending inventory is present
                                if (unloadVarianceExist(App.ENDING_INVENTORY)) {
                                    referenceCount++;
                                    new postData(App.ENDING_INVENTORY);
                                }
                                //Finally unloading remainder quantity
                            } else {
                                referenceCount++;
                                new postData(App.FRESHUNLOAD);
                                dialog.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                btn_notprint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (unloadVarianceExist("")) {
                                //Checking any record exist for ZDRX
                                if (unloadVarianceExist(App.THEFT) || unloadVarianceExist(App.TRUCK_DAMAGE)) {
                                    //Checking does it exist for both
                                    if (unloadVarianceExist(App.THEFT) && unloadVarianceExist(App.TRUCK_DAMAGE)) {
                                        referenceCount++;
                                        new postData(App.THEFT, App.TRUCK_DAMAGE);
                                    }
                                    //Check if it exists only for Theft
                                    else if (unloadVarianceExist(App.THEFT)) {
                                        referenceCount++;
                                        new postData(App.THEFT);
                                    }
                                    //It only exist for Truck Damage
                                    else {
                                        referenceCount++;
                                        new postData(App.TRUCK_DAMAGE);
                                    }
                                }
                                //Checking if  Any excess product exist
                                if (unloadVarianceExist(App.EXCESS)) {
                                    referenceCount++;
                                    new postData(App.EXCESS);
                                }
                                //Check if any ending inventory is present
                                if (unloadVarianceExist(App.ENDING_INVENTORY)) {
                                    referenceCount++;
                                    new postData(App.ENDING_INVENTORY);
                                }
                                //Finally unloading remainder quantity
                            } else {
                                referenceCount++;
                                new postData(App.FRESHUNLOAD);
                                dialog.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });*/
    }
    private void navigation() {
        Intent i = new Intent(UnloadActivity.this, ManageInventory.class);
        startActivity(i);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //  navigation();
                finish();
                //onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public class postDataNew extends AsyncTask<String,String,String>{


        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String,String>map = new HashMap<>();
            map.put(db.KEY_TIME_STAMP,"");
            map.put(db.KEY_VARIANCE_TYPE,"");
            map.put(db.KEY_TRIP_ID,"");
            map.put(db.KEY_ITEM_NO,"");
            map.put(db.KEY_MATERIAL_DESC1,"");
            map.put(db.KEY_MATERIAL_NO,"");
            map.put(db.KEY_IS_POSTED,"");
            map.put(db.KEY_CASE,"");
            map.put(db.KEY_UNIT,"");
            map.put(db.KEY_ORDER_ID,"");
            HashMap<String,String>filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
            Cursor c = db.getData(db.UNLOAD_VARIANCE,map,filter);
            if(c.getCount()>0){
                c.moveToFirst();
                setUnloadData(c);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            //clearVanStock();
            try{

                if(loadingSpinner.isShowing()){
                    loadingSpinner.hide();
                }
                if(Helpers.isNetworkAvailable(UnloadActivity.this)){
                    Helpers.createBackgroundJob(UnloadActivity.this);
                }
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_IS_UNLOAD, "true");
                        HashMap<String, String> filterMap = new HashMap<>();
                        filterMap.put(db.KEY_IS_UNLOAD, "false");
                        db.updateData(db.LOCK_FLAGS, altMap, filterMap);


                        HashMap<String,String> logMap = new HashMap<>();
                        logMap.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                        logMap.put(db.KEY_ORDER_ID,purchaseNumber);
                        logMap.put(db.KEY_PURCHASE_NUMBER,purchaseNumber);
                        logMap.put(db.KEY_IS_POSTED,App.DATA_MARKED_FOR_POST);
                        logMap.put(db.KEY_IS_PRINTED, App.DATA_MARKED_FOR_POST);
                        db.addData(db.UNLOAD_TRANSACTION,logMap);
                    }
                });
                Intent intent = new Intent(UnloadActivity.this,DashboardActivity.class);
                startActivity(intent);
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }
    public void setUnloadData(Cursor c){
        try{

            if(c.getCount()>0){
                c.moveToFirst();

                do{
                    try {
                        purchaseNumber = c.getString(c.getColumnIndex(db.KEY_ORDER_ID));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    HashMap<String,String>map = new HashMap<>();
                    map.put(db.KEY_IS_POSTED,App.DATA_MARKED_FOR_POST);
                    HashMap<String,String>filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                    db.updateData(db.UNLOAD_VARIANCE,map,filter);


                    String strCase=c.getString(c.getColumnIndex(db.KEY_CASE));
                    String strPic=c.getString(c.getColumnIndex(db.KEY_UNIT));



                    HashMap<String, String> unloadmap = new HashMap<>();
                    unloadmap.put(db.KEY_MATERIAL_NO, "");
                    unloadmap.put(db.KEY_REMAINING_QTY_CASE, "");
                    unloadmap.put(db.KEY_REMAINING_QTY_UNIT, "");
                    HashMap<String, String> unloadfilter = new HashMap<>();
                    unloadfilter.put(db.KEY_MATERIAL_NO,c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, unloadmap, unloadfilter);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            HashMap<String, String> updateDataMap = new HashMap<>();
                            float remainingCase = 0;
                            float remainingUnit = 0;
                            remainingCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                            remainingUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));

                            String deno = "0";
                            HashMap<String, String> altMap = new HashMap<>();
                            altMap.put(db.KEY_UOM, "");
                            altMap.put(db.KEY_DENOMINATOR, "");
                            HashMap<String, String> filtera = new HashMap<>();
                            filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                            if (altUOMCursor.getCount() > 0) {
                                altUOMCursor.moveToFirst();
                                deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                            }
                            if (!(strCase.isEmpty() || strCase.equals("") || strCase == null || strCase.equals("0")) && !(strPic.isEmpty() || strPic.equals("") || strPic == null || strPic.equals("0"))) {
                                remainingCase = remainingCase - (Float.parseFloat(strCase) + Float.parseFloat(strPic) / Float.parseFloat(deno));

                                remainingUnit = remainingUnit - (Float.parseFloat(strPic) + Float.parseFloat(strCase) * Float.parseFloat(deno));
                            } else {
                                if (!(strCase.isEmpty() || strCase.equals("") || strCase == null || strCase.equals("0"))) {
                                    remainingCase = remainingCase - Float.parseFloat(strCase);
                                    remainingUnit = remainingUnit - (Float.parseFloat(strCase) * Float.parseFloat(deno));
                                }

                                if (!(strPic.isEmpty() || strPic.equals("") || strPic == null || strPic.equals("0"))) {
                                    remainingUnit = remainingUnit - Float.parseFloat(strPic);
                                    remainingCase = remainingCase - (Float.parseFloat(strPic) / Float.parseFloat(deno));
                                }
                            }
                            updateDataMap.put(db.KEY_REMAINING_QTY_CASE, String.format("%.2f", remainingCase));
                            updateDataMap.put(db.KEY_REMAINING_QTY_UNIT, String.format("%.2f", remainingUnit));


                            HashMap<String, String> filterInter = new HashMap<>();
                            filterInter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            db.updateData(db.VAN_STOCK_ITEMS, updateDataMap, filterInter);
                        }
                        while (cursor.moveToNext());
                    }
                }
                while (c.moveToNext());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    public class postData extends AsyncTask<String,String,String>{

        private String param1;
        private String param2;
        private String orderID;
        private String[] tokens = new String[2];
        private postData(String param1,String param2){
            this.param1 = param1;
            this.param2 = param2;
            execute();
        }

        private postData(String param1){
            this.param1 = param1;
            this.param2 = "";
            execute();
        }

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected String doInBackground(String... params) {
            if(param2.equals("")){
                this.orderID = postData(this.param1,"");
            }
            else{
                this.orderID = postData(this.param1,this.param2);
            }
            this.tokens = orderID.split(",");
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            postCount++;
            Log.e("Refernce Count", "" + postCount + referenceCount);
            if(postCount==referenceCount){
                if(loadingSpinner.isShowing()){
                    loadingSpinner.hide();
                }
                if(this.tokens[0].toString().equals(this.tokens[1].toString())){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UnloadActivity.this);
                    alertDialogBuilder.setTitle("Message")
                            //.setMessage("Request with reference " + tokens[0].toString() + " has been saved")
                            .setMessage(getString(R.string.request_created))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clearVanStock(); //This is for testing. Uncomment it later
                                    dialog.dismiss();
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_IS_UNLOAD, "true");
                                    HashMap<String, String> filterMap = new HashMap<>();
                                    filterMap.put(db.KEY_IS_UNLOAD, "false");
                                    db.updateData(db.LOCK_FLAGS, altMap, filterMap);
                                    Intent intent = new Intent(UnloadActivity.this,DashboardActivity.class);
                                    startActivity(intent);
                                }
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                else{
                    if(this.orderID.isEmpty()||this.orderID.equals("")||this.orderID==null){
                        Toast.makeText(getApplicationContext(),getString(R.string.request_timeout),Toast.LENGTH_SHORT ).show();
                    }
                    else if(this.orderID.contains("Error")){
                        Toast.makeText(getApplicationContext(), this.orderID.replaceAll("Error","").trim(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UnloadActivity.this);
                        alertDialogBuilder.setTitle("Message")
                                .setMessage("Request " + tokens[1].toString() + " has been created")
                                        // .setMessage("Request " + tokens[0].toString() + " has been created")
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        /*dialog.dismiss();
                                        finish();*/
                                        clearVanStock();

                                        HashMap<String, String> altMap = new HashMap<>();
                                        altMap.put(db.KEY_IS_UNLOAD, "true");
                                        HashMap<String, String> filterMap = new HashMap<>();
                                        filterMap.put(db.KEY_IS_UNLOAD, "false");
                                        db.updateData(db.LOCK_FLAGS, altMap, filterMap);

                                        dialog.dismiss();
                                        Intent intent = new Intent(UnloadActivity.this,DashboardActivity.class);
                                        startActivity(intent);
                                    }
                                });
                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();
                    }
                }
            }
        }
    }
    public String getDocumentType(String param){
        String docType = null;
        switch (param){
            case App.THEFT:
                docType = ConfigStore.TheftorTruckDocumentType;
                break;
            case App.TRUCK_DAMAGE:
                docType = ConfigStore.TheftorTruckDocumentType;
                break;
            case App.EXCESS:
                docType = ConfigStore.ExcessDocumentType;
                break;
            case App.ENDING_INVENTORY:
                docType = ConfigStore.EndingInventory;
                break;
            case App.FRESHUNLOAD:
                docType = ConfigStore.FreshUnload;
                break;
            default:
                break;
        }
        return docType;
    }
    private void fetchTruckTheftData(){
        try{
            for(int i=0;i<articles.size();i++){
                HashMap<String, String> itemMap = new HashMap<>();
                itemMap.put(db.KEY_ITEM_NO,"");
                itemMap.put(db.KEY_MATERIAL_DESC1,"");
                itemMap.put(db.KEY_MATERIAL_NO,"");
                itemMap.put(db.KEY_MATERIAL_GROUP,"");
                itemMap.put(db.KEY_CASE,"");
                itemMap.put(db.KEY_UNIT,"");
                itemMap.put(db.KEY_UOM,"");
                itemMap.put(db.KEY_PRICE,"");
                itemMap.put(db.KEY_ORDER_ID,"");
                itemMap.put(db.KEY_PURCHASE_NUMBER,"");
                itemMap.put(db.KEY_IS_POSTED,"");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                filter.put(db.KEY_VARIANCE_TYPE,App.TRUCK_DAMAGE);
                filter.put(db.KEY_MATERIAL_NO,articles.get(i).getMaterialNo());
                HashMap<String, String> filter2 = new HashMap<>();
                filter2.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                filter2.put(db.KEY_VARIANCE_TYPE,App.THEFT);
                filter2.put(db.KEY_MATERIAL_NO,articles.get(i).getMaterialNo());


                Cursor c1 = db.getData(db.UNLOAD_VARIANCE,itemMap,filter);
                Cursor c2 = db.getData(db.UNLOAD_VARIANCE,itemMap,filter2);
                if(c1.getCount()>0 && c2.getCount()>0){
                    c1.moveToFirst();
                    c2.moveToFirst();
                    Unload unload = new Unload();
                    unload.setItem_code(c1.getString(c1.getColumnIndex(db.KEY_ITEM_NO)));
                    unload.setName(c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                    unload.setMaterial_no(c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));

                    float cases = Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_CASE))) + Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_CASE)));
                    float units = Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_UNIT))) + Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_UNIT)));

                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,articles.get(i).getMaterialNo());
                    unload.setCases(String.valueOf(cases));
                    unload.setPic(String.valueOf(units));
                    unload.setUom(articleHeader.getBaseUOM());
                    unload.setPrice(c1.getString(c1.getColumnIndex(db.KEY_PRICE)));
                    arrayList.add(unload);

                }
                else if(c1.getCount()>0){
                    c1.moveToFirst();
                    Unload unload = new Unload();
                    unload.setItem_code(c1.getString(c1.getColumnIndex(db.KEY_ITEM_NO)));
                    unload.setName(c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                    unload.setMaterial_no(c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));

                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,articles.get(i).getMaterialNo());
                    unload.setCases(c1.getString(c1.getColumnIndex(db.KEY_CASE)));
                    unload.setPic(c1.getString(c1.getColumnIndex(db.KEY_UNIT)));
                    unload.setUom(articleHeader.getBaseUOM());
                    unload.setPrice(c1.getString(c1.getColumnIndex(db.KEY_PRICE)));
                    arrayList.add(unload);
                }
                else if(c2.getCount()>0) {
                    c2.moveToFirst();
                    Unload unload = new Unload();
                    unload.setItem_code(c2.getString(c2.getColumnIndex(db.KEY_ITEM_NO)));
                    unload.setName(c2.getString(c2.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                    unload.setMaterial_no(c2.getString(c2.getColumnIndex(db.KEY_MATERIAL_NO)));

                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,articles.get(i).getMaterialNo());
                    unload.setCases(c2.getString(c2.getColumnIndex(db.KEY_CASE)));
                    unload.setPic(c2.getString(c2.getColumnIndex(db.KEY_UNIT)));
                    unload.setUom(articleHeader.getBaseUOM());
                    unload.setPrice(c2.getString(c2.getColumnIndex(db.KEY_PRICE)));
                    arrayList.add(unload);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public String postData(final String param1, final String param2){
        //There is no post for truck and theft
        final String[] orderID = {""};
        final String[] purchaseNumber = {""};

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(!param2.equals("")){
                        try{
                            fetchTruckTheftData();
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Function", ConfigStore.ReturnsFunction);
                            map.put("OrderId", "");
                            map.put("DocumentType",getDocumentType(param1));
                            // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
                            // map.put("DocumentDate", null);
                            map.put("CustomerId", Settings.getString(App.DRIVER));
                            map.put("SalesOrg", Settings.getString(App.SALES_ORG));
                            map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
                            map.put("Division", Settings.getString(App.DIVISION));
                            map.put("OrderValue", "00");
                            map.put("Currency", App.CURRENCY);
                            purchaseNumber[0] = Helpers.generateNumber(db,ConfigStore.TheftorTruck_PR_Type);
                            JSONArray deepEntity = new JSONArray();

                            int itemno = 10;
                            for(Unload unload:arrayList){
                                if(unload.getUom().equals(App.CASE_UOM)||unload.getUom().equals(App.BOTTLES_UOM)){
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("Material", unload.getMaterial_no());
                                    jo.put("Description", unload.getName());
                                    jo.put("Plant", App.PLANT);
                                    jo.put("Quantity", unload.getCases());
                                    jo.put("ItemValue", unload.getPrice());
                                    jo.put("UoM", unload.getUom());
                                    jo.put("Value", unload.getPrice());
                                    jo.put("Storagelocation", App.STORAGE_LOCATION);
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                }
                                else{
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("Material", unload.getMaterial_no());
                                    jo.put("Description", unload.getName());
                                    jo.put("Plant", App.PLANT);
                                    jo.put("Quantity", unload.getPic());
                                    jo.put("ItemValue", unload.getPrice());
                                    jo.put("UoM", unload.getUom());
                                    jo.put("Value", unload.getPrice());
                                    jo.put("Storagelocation", App.STORAGE_LOCATION);
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                }

                            }
                            orderID[0] = IntegrationServiceJSON.postDataBackup(UnloadActivity.this, App.POST_COLLECTION, map, deepEntity);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    //This means there is no post for both truck and theft
                    else{
                        try{
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Function", ConfigStore.UnloadFunction);
                            map.put("OrderId", "");
                            map.put("DocumentType",getDocumentType(param1));
                            // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
                            // map.put("DocumentDate", null);
                            map.put("CustomerId", Settings.getString(App.DRIVER));
                            map.put("SalesOrg", Settings.getString(App.SALES_ORG));
                            map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
                            map.put("Division", Settings.getString(App.DIVISION));
                            map.put("OrderValue", "00");
                            map.put("Currency", App.CURRENCY);

                            if(param1.equals(App.THEFT)||param1.equals(App.THEFT)){
                                purchaseNumber[0] = Helpers.generateNumber(db,ConfigStore.TheftorTruck_PR_Type);
                            }
                            else if(param1.equals(App.EXCESS)){
                                purchaseNumber[0] = Helpers.generateNumber(db,ConfigStore.Excess_PR_Type);
                            }
                            else if(param1.equals(App.ENDING_INVENTORY)){
                                purchaseNumber[0] = Helpers.generateNumber(db,ConfigStore.EndingInventory_PR_Type);
                            }
                            else if(param1.equals(App.FRESHUNLOAD)){
                                purchaseNumber[0] = Helpers.generateNumber(db,ConfigStore.FreshUnload_PR_Type);
                            }
                            map.put("PurchaseNum", purchaseNumber[0]);
                            JSONArray deepEntity = new JSONArray();
                            //Apart from Fresh unload read everything from Load Variance Table
                            if(!param1.equals(App.FRESHUNLOAD)){
                                HashMap<String, String> itemMap = new HashMap<>();
                                itemMap.put(db.KEY_ITEM_NO,"");
                                itemMap.put(db.KEY_MATERIAL_DESC1,"");
                                itemMap.put(db.KEY_MATERIAL_NO,"");
                                itemMap.put(db.KEY_MATERIAL_GROUP,"");
                                itemMap.put(db.KEY_CASE,"");
                                itemMap.put(db.KEY_UNIT,"");
                                itemMap.put(db.KEY_UOM,"");
                                itemMap.put(db.KEY_PRICE,"");
                                itemMap.put(db.KEY_ORDER_ID,"");
                                itemMap.put(db.KEY_PURCHASE_NUMBER,"");
                                itemMap.put(db.KEY_IS_POSTED,"");
                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                                filter.put(db.KEY_VARIANCE_TYPE,param1);
                                Cursor cursor = db.getData(db.UNLOAD_VARIANCE,itemMap,filter);
                                if(cursor.getCount()>0){
                                    cursor.moveToFirst();
                                    int itemno = 10;
                                    do{
                                        ArticleHeader articleHeader = ArticleHeader.getArticle(articles,cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                                        if (articleHeader.getBaseUOM().equals(App.CASE_UOM)||articleHeader.getBaseUOM().equals(App.BOTTLES_UOM)) {
                                            JSONObject jo = new JSONObject();
                                            jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                            jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                            jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                                            jo.put("Plant", App.PLANT);
                                            jo.put("Quantity", param1.equals(App.EXCESS) ? String.valueOf(Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)))*-1) : cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                                            jo.put("ItemValue", "0");
                                            jo.put("UoM", articleHeader.getBaseUOM());
                                            jo.put("Value", "0");
                                            jo.put("Storagelocation", App.STORAGE_LOCATION);
                                            jo.put("Route", Settings.getString(App.ROUTE));
                                            itemno = itemno + 10;
                                            deepEntity.put(jo);
                                        }
                                        else {
                                            JSONObject jo = new JSONObject();
                                            jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                            jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                            jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                                            jo.put("Plant", App.PLANT);
                                            jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                                            jo.put("ItemValue", "0");
                                            jo.put("UoM", articleHeader.getBaseUOM());
                                            jo.put("Value", "0");
                                            jo.put("Storagelocation", App.STORAGE_LOCATION);
                                            jo.put("Route", Settings.getString(App.ROUTE));
                                            itemno = itemno + 10;
                                            deepEntity.put(jo);
                                        }
                                    }
                                    while (cursor.moveToNext());

                                }
                                orderID[0] = IntegrationServiceJSON.postDataBackup(UnloadActivity.this, App.POST_COLLECTION, map, deepEntity);
                            }
                            else if(param1.equals(App.FRESHUNLOAD)){
                                HashMap<String,String>itemMap = new HashMap<>();
                                itemMap.put(db.KEY_ITEM_NO,"");
                                itemMap.put(db.KEY_ITEM_CATEGORY,"");
                                itemMap.put(db.KEY_CREATED_BY,"");
                                itemMap.put(db.KEY_ENTRY_TIME,"");
                                itemMap.put(db.KEY_DATE,"");
                                itemMap.put(db.KEY_MATERIAL_NO,"");
                                itemMap.put(db.KEY_MATERIAL_DESC1,"");
                                itemMap.put(db.KEY_MATERIAL_ENTERED,"");
                                itemMap.put(db.KEY_MATERIAL_GROUP,"");
                                itemMap.put(db.KEY_PLANT,"");
                                itemMap.put(db.KEY_STORAGE_LOCATION,"");
                                itemMap.put(db.KEY_BATCH,"");
                                itemMap.put(db.KEY_ACTUAL_QTY_CASE,"");
                                itemMap.put(db.KEY_ACTUAL_QTY_UNIT,"");
                                itemMap.put(db.KEY_RESERVED_QTY_CASE,"");
                                itemMap.put(db.KEY_RESERVED_QTY_UNIT,"");
                                itemMap.put(db.KEY_REMAINING_QTY_CASE,"");
                                itemMap.put(db.KEY_REMAINING_QTY_UNIT,"");
                                itemMap.put(db.KEY_UOM_CASE,"");
                                itemMap.put(db.KEY_UOM_UNIT,"");
                                itemMap.put(db.KEY_DIST_CHANNEL,"");
                                HashMap<String,String>filter=new HashMap<>();
                                Cursor c = db.getData(db.VAN_STOCK_ITEMS,itemMap,filter);
                                int itemno = 10;
                                if(c.getCount()>0){
                                    c.moveToFirst();
                                    do{
                                        Unload unload = new Unload();
                                        unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                        unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                                        unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                                        String uomCase = c.getString(c.getColumnIndex(db.KEY_UOM_CASE));
                                        String uomUnit = c.getString(c.getColumnIndex(db.KEY_UOM_UNIT));
                                        unload.setUom((uomCase == null || uomCase.equals("")) ? uomUnit : uomCase);
                                        unload.setCases(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                                        unload.setPic(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                                        dataStoreList.add(unload);

                                    }
                                    while (c.moveToNext());
                                    recalculateFreshUnload(dataStoreList);
                                }

                                for(Unload unload:dataStoreList){

                                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,unload.getMaterial_no());
                                    if(articleHeader.getBaseUOM().equals(App.CASE_UOM)||articleHeader.getBaseUOM().equals(App.BOTTLES_UOM)){
                                        JSONObject jo = new JSONObject();
                                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                        jo.put("Material", unload.getMaterial_no());
                                        jo.put("Description", unload.getName());
                                        jo.put("Plant", App.PLANT);
                                        jo.put("Quantity", unload.getCases());
                                        jo.put("ItemValue", unload.getPrice());
                                        jo.put("UoM",articleHeader.getBaseUOM());
                                        jo.put("Value", unload.getPrice());
                                        jo.put("Storagelocation", App.STORAGE_LOCATION);
                                        jo.put("Route", Settings.getString(App.ROUTE));
                                        itemno = itemno + 10;
                                        deepEntity.put(jo);
                                    }
                                    else{
                                        JSONObject jo = new JSONObject();
                                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                        jo.put("Material", unload.getMaterial_no());
                                        jo.put("Description", unload.getName());
                                        jo.put("Plant", App.PLANT);
                                        jo.put("Quantity", unload.getPic());
                                        jo.put("ItemValue", unload.getPrice());
                                        jo.put("UoM", articleHeader.getBaseUOM());
                                        jo.put("Value", unload.getPrice());
                                        jo.put("Storagelocation", App.STORAGE_LOCATION);
                                        jo.put("Route", Settings.getString(App.ROUTE));
                                        itemno = itemno + 10;
                                        deepEntity.put(jo);
                                    }
                                    //Log.e("Got here","" + map + deepEntity);
                                }
                                orderID[0] = IntegrationServiceJSON.postDataBackup(UnloadActivity.this, App.POST_COLLECTION, map, deepEntity);

                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        //Log.e("Step","Step1" + orderID + purchaseNumber);
                       // return orderID[0].toString() + "," + purchaseNumber[0].toString();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        //Log.e("Step","Step2" + orderID + purchaseNumber);
        return orderID[0].toString() + "," + purchaseNumber[0].toString();
        //return null;
    }
    private boolean unloadVarianceExist(String varianceType){
        HashMap<String,String> map = new HashMap<>();
        map.put(db.KEY_TIME_STAMP,"");
        HashMap<String,String>filter = new HashMap<>();
        //filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
        if(varianceType.equals("")||varianceType==null){

        }
        else{
            filter.put(db.KEY_VARIANCE_TYPE,varianceType);
        }

        if(db.checkData(db.UNLOAD_VARIANCE,filter)){
            return true;
        }
        else{
            return false;
        }
    }
    private void recalculateFreshUnload(ArrayList<Unload>data){
        try{
            ArrayList<Unload>vanData = data;
            for(int i=0;i<vanData.size();i++){
                Unload unload = vanData.get(i);
                HashMap<String,String>map = new HashMap<>();
                map.put(db.KEY_CASE,"");
                map.put(db.KEY_UNIT,"");
                HashMap<String,String>checkInventoryFilter = new HashMap<>();
                checkInventoryFilter.put(db.KEY_VARIANCE_TYPE,App.ENDING_INVENTORY);
                checkInventoryFilter.put(db.KEY_MATERIAL_NO,unload.getMaterial_no());
                HashMap<String,String>truckDamageFilter = new HashMap<>();
                truckDamageFilter.put(db.KEY_VARIANCE_TYPE,App.TRUCK_DAMAGE);
                truckDamageFilter.put(db.KEY_MATERIAL_NO,unload.getMaterial_no());
                HashMap<String,String>theftFilter = new HashMap<>();
                theftFilter.put(db.KEY_VARIANCE_TYPE,App.THEFT);
                theftFilter.put(db.KEY_MATERIAL_NO,unload.getMaterial_no());
                HashMap<String,String>excessFilter = new HashMap<>();
                excessFilter.put(db.KEY_VARIANCE_TYPE,App.EXCESS);
                excessFilter.put(db.KEY_MATERIAL_NO,unload.getMaterial_no());
                //Inventory Exists
                float cases = 0;
                float units = 0;
                float excessCases = 0;
                float excessUnits = 0;

                if(db.checkData(db.UNLOAD_VARIANCE,checkInventoryFilter)){
                    Cursor c = db.getData(db.UNLOAD_VARIANCE,map,checkInventoryFilter);
                    if(c.getCount()>0){
                        c.moveToFirst();

                        do{
                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                if(db.checkData(db.UNLOAD_VARIANCE,truckDamageFilter)){
                    Cursor c = db.getData(db.UNLOAD_VARIANCE,map,truckDamageFilter);
                    if(c.getCount()>0){
                        c.moveToFirst();

                        do{
                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                if(db.checkData(db.UNLOAD_VARIANCE,theftFilter)){
                    Cursor c = db.getData(db.UNLOAD_VARIANCE,map,theftFilter);
                    if(c.getCount()>0){
                        c.moveToFirst();

                        do{
                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                if(db.checkData(db.UNLOAD_VARIANCE,excessFilter)){
                    Cursor c = db.getData(db.UNLOAD_VARIANCE,map,excessFilter);
                    if(c.getCount()>0){
                        c.moveToFirst();

                        do{
                            excessCases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            excessUnits += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                float finalCases = Float.parseFloat(unload.getCases())-cases+(excessCases*-1);
                float finalUnits = Float.parseFloat(unload.getPic())-units+(excessUnits*-1);
                unload.setCases(String.valueOf(finalCases));
                unload.setPic(String.valueOf(finalUnits));
                vanData.remove(i);
                vanData.add(i,unload);
            }
            arrayList = vanData;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //dataStoreList = vanData;
    }
    private void clearVanStock(){
        for(int i=0;i<articles.size();i++){
            HashMap<String,String>map = new HashMap<>();
            map.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
            db.deleteData(db.VAN_STOCK_ITEMS, map);
        }
    }



    public void callbackFunction(){
        Intent intent = new Intent(UnloadActivity.this,DashboardActivity.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
//        finish();
        Intent intent = new Intent(UnloadActivity.this,DashboardActivity.class);
        startActivity(intent);
    }

}