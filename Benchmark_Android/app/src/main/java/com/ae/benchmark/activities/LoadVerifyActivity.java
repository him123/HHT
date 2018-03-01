package com.ae.benchmark.activities;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.adapters.LoadSummaryBadgeAdapter;
import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.LoadVerifyBadgeAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.LoadDeliveryHeader;
import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.sap.IntegrationService;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.PrinterHelper;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by Rakshit on 19-Nov-16.
 */
/*********************************************************
 @ Once variance are captured, this activity will be launched
 @ and user will see the quantity and their variance
 *********************************************************/
public class LoadVerifyActivity extends AppCompatActivity {
    ArrayList<LoadSummary> loadSummaryList;
    ArrayList<LoadSummary> varianceLoadSummaryList;
    LoadVerifyBadgeAdapter adapter;
    private ListView listView;
    LoadDeliveryHeader object;
    DatabaseHandler db;
    private static final String TRIP_ID = "ITripId";
    ArrayList<LoadSummary> dataNew;
    ArrayList<LoadSummary> dataOld;
    LoadingSpinner loadingSpinner;
    private String tempOrderID = "";
    private boolean print = false;
    private boolean isStockAdded = false;

    EditText et_search;
    ImageView iv_search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_verify);



        iv_search = (ImageView) findViewById(R.id.iv_search);
        iv_search.setVisibility(View.VISIBLE);
        et_search = (EditText) findViewById(R.id.et_search_customer);
        et_search.setHint("Select Product..");

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_search.setVisibility(View.GONE);
                et_search.setVisibility(View.VISIBLE);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle("");
            }
        });

        et_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (et_search.getRight() - et_search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        et_search.setVisibility(View.GONE);
                        iv_search.setVisibility(View.VISIBLE);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setTitle("Verify Load");
                        return true;
                    }
                }
                return false;
            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.v("addtext", "change");
                adapter.getFilter().filter(s.toString());
            }
        });

        db= DatabaseHandler.getInstance(getApplicationContext());
        try{
            loadingSpinner = new LoadingSpinner(this);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Intent intent = getIntent();
            dataNew = new ArrayList<>();
            dataOld = new ArrayList<>();
            /*********************************************************
             @ dataNew will contain the changed data that the user must have
             @ recorded.
             @ dataOld will contain the unchanged data that was originally loaded
             *********************************************************/
            dataNew = Const.dataNew;
            dataOld = Const.dataOld;
            Const.dataNew = new ArrayList<>();
            Const.dataOld= new ArrayList<>();
            object = (LoadDeliveryHeader) intent.getParcelableExtra("headerObj");
            // Log.e("****",""+dataOld.size());
            loadSummaryList = new ArrayList<>();
            varianceLoadSummaryList = new ArrayList<>();
            Helpers.logData(LoadVerifyActivity.this, "Driver is now on load Verification SCreen");
            //  loadSummaryList = dataNew;
            adapter = new LoadVerifyBadgeAdapter(this, loadSummaryList);
//            loadSummaryList = generateData(dataNew, dataOld);
            listView = (ListView) findViewById(R.id.loadSummaryList);
            listView.setAdapter(adapter);
            new loadSummary().execute();
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private class loadSummary extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(String... strings) {
            try{
                Helpers.logData(LoadVerifyActivity.this, "Generating data to show on screen");
                for (int i = 0; i < dataNew.size(); i++) {
                    LoadSummary loadSummary = new LoadSummary();
                    loadSummary.setItemCode(dataNew.get(i).getItemCode());
                    Helpers.logData(LoadVerifyActivity.this, "Item Code" + loadSummary.getItemCode());
                    loadSummary.setMaterialNo(dataNew.get(i).getMaterialNo());
                    Helpers.logData(LoadVerifyActivity.this, "Material No" + loadSummary.getMaterialNo());
                    loadSummary.setItemDescription(dataNew.get(i).getItemDescription());
                    Helpers.logData(LoadVerifyActivity.this, "Item Description" + loadSummary.getItemDescription());
                    if (dataNew.get(i).getItemCode().equals(dataOld.get(i).getItemCode())) {
                        loadSummary.setQuantityCases( dataNew.get(i).getQuantityCases());
                        Helpers.logData(LoadVerifyActivity.this, "Item Cases Old" + dataOld.get(i).getQuantityCases() + "-" + "Item Cases New" + dataNew.get(i).getQuantityCases());
                    }
                    if (dataNew.get(i).getItemCode().equals(dataOld.get(i).getItemCode())) {
                        loadSummary.setQuantityUnits( dataNew.get(i).getQuantityUnits());
                        Helpers.logData(LoadVerifyActivity.this, "Item Units Old" + dataOld.get(i).getQuantityUnits() + "-" + "Item Units New" + dataNew.get(i).getQuantityUnits());
                    }
                    loadSummary.setPrice(dataNew.get(i).getPrice());
                    Helpers.logData(LoadVerifyActivity.this, "Item Price"  + dataNew.get(i).getPrice());
                    loadSummaryList.add(loadSummary);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            calculateCost();
            adapter.notifyDataSetChanged();
        }
    }

    /*********************************************************
     @ Calculating total cost of the changed load
     *********************************************************/
    private void calculateCost() {
        try{
            int salesTotal = 0;
            int pcsTotal = 0;
            double total = 0;
            for (LoadSummary item : loadSummaryList) {
                double itemPrice = 0;
                String[] cases = item.getQuantityUnits().split("\\|");
//                if (!item.isAltUOM()) {
                    itemPrice = Double.parseDouble(item.getQuantityUnits()) * Double.parseDouble(item.getPrice());
//                }
                total += itemPrice;
            }
            TextView tv = (TextView) findViewById(R.id.tv_amt);
            tv.setText(String.format("%.2f",total));
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    /*********************************************************
     @ Confirming the load the user had entered
     *********************************************************/
    public void confirmLoad(View v) {
        Helpers.logData(LoadVerifyActivity.this, "Confirm clicked on load verify screen");
        try{
            new addItemToVan();
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
    private class addItemToVan extends AsyncTask<String, Void, Void> {

        private addItemToVan() {
            execute();
        }
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(String... params) {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put(db.KEY_IS_VERIFIED, "true");
            HashMap<String, String> filters = new HashMap<>();
            filters.put(db.KEY_DELIVERY_NO, object.getDeliveryNo());
            db.updateData(db.LOAD_DELIVERY_HEADER, parameters, filters);
            if(!isStockAdded){
                //Checking if driver did blunder and again coming for load verification
                HashMap<String,String>map = new HashMap<>();
                map.put(db.KEY_DELIVERY_NO,object.getDeliveryNo().toString());
                boolean isAlreadyAdded = db.checkData(db.VAN_STOCK_ITEMS,map);
                if(!isAlreadyAdded){
                    addItemstoVan(dataNew);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {

            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }

            /*********************************************************
             @ If the user has reached to the last of the load,
             @ then create a post request and submit it.
             *********************************************************/
            if (!checkIfLoadExists()) {
                Helpers.logData(LoadVerifyActivity.this, "Reached to the last load and preparing data for post");
                if (createDataForPost(dataNew, dataOld)){
                    HashMap<String,String> altMap = new HashMap<>();
                    altMap.put(db.KEY_IS_LOAD_VERIFIED, "true");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_LOAD_VERIFIED, "false");
                    db.updateData(db.LOCK_FLAGS, altMap, filter);
                    final Dialog dialog = new Dialog(LoadVerifyActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_doprint);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
                    LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);
                    HashMap<String, String> searchMap = new HashMap<>();
                    searchMap.put(db.KEY_ORDER_ID, "");
                    HashMap<String, String> filterMap = new HashMap<>();
                    filterMap.put(db.KEY_DELIVERY_NO, object.getDeliveryNo());
                    Cursor cursor = db.getData(db.LOAD_DELIVERY_ITEMS, searchMap, filterMap);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        tempOrderID = cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID));
                    }
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    map.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                    map.put(db.KEY_FUNCTION, ConfigStore.LoadConfirmationFunction);
                    map.put(db.KEY_ORDER_ID, tempOrderID);
                    map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    map.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                    HashMap<String,String>checkFilter = new HashMap<>();
                    checkFilter.put(db.KEY_ORDER_ID,tempOrderID);
                    checkFilter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    checkFilter.put(db.KEY_CUSTOMER_NO, Settings.getString(App.DRIVER));
                    if(!db.checkData(db.LOAD_CONFIRMATION_HEADER,checkFilter)){
                        Log.e("Adding DATA","LCON");
                        db.addData(db.LOAD_CONFIRMATION_HEADER, map);
                    }
                    btn_print.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            print = true;
                            Helpers.logData(LoadVerifyActivity.this, "Posting Data with Print");
                            new postData(tempOrderID);
                            dialog.dismiss();
                            //finish();
                        }
                    });
                    btn_notprint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            print = false;
                            Helpers.logData(LoadVerifyActivity.this, "Posting Data with Print");
                            new postData(tempOrderID);
                            dialog.dismiss();
                        }
                    });
                    dialog.setCancelable(false);
                    //dialog.show();

                    // Added to Disable Print option
                    //Changes on 15/05/17
                    //dialog.show();
                    print = false;
                    try{
                        JSONArray jsonArray = createPrintData(object.getLoadingDate(),object.getDeliveryNo());
                        JSONObject data = new JSONObject();
                        data.put("data",(JSONArray)jsonArray);
                        HashMap<String,String>printMap = new HashMap<>();
                        printMap.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                        printMap.put(db.KEY_ORDER_ID,object.getDeliveryNo());
                        printMap.put(db.KEY_DOC_TYPE,ConfigStore.LoadConfirmation_TR);
                        printMap.put(db.KEY_DATA, data.toString());
                        //map.put(db.KEY_DATA,jsonArray.toString());
                        db.addDataPrint(db.DELAY_PRINT, printMap);
                        dialog.dismiss();
                        Intent intent = new Intent(LoadVerifyActivity.this, LoadActivity.class);
                        startActivity(intent);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    Helpers.logData(LoadVerifyActivity.this, "Posting Data with Print");
                    new postData(tempOrderID);
                    dialog.dismiss();
                    //End of Changes on 15/05/17
                /*Intent intent = new Intent(LoadVerifyActivity.this,MyCalendarActivity.class);
                startActivity(intent);*/
                }
            }
            /*********************************************************
             @ If there are more loads for the user, create a post request
             @ and go back to the load screen.
             *********************************************************/
            else {
                Helpers.logData(LoadVerifyActivity.this, "Adding Data to mark as post and going back for another load");
                HashMap<String, String> searchMap = new HashMap<>();
                searchMap.put(db.KEY_ORDER_ID, "");
                HashMap<String, String> filterMap = new HashMap<>();
                filterMap.put(db.KEY_DELIVERY_NO, object.getDeliveryNo());
                Cursor cursor = db.getData(db.LOAD_DELIVERY_ITEMS, searchMap, filterMap);
                String orderID = "";
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    tempOrderID = cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID));
                    orderID = cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID));
                }
                Log.e("ORDER ID","" + orderID);
                //tempOrderID = "0000000075";
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                map.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                map.put(db.KEY_FUNCTION, ConfigStore.LoadConfirmationFunction);
                map.put(db.KEY_ORDER_ID, orderID);
                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                map.put(db.KEY_IS_PRINTED, App.DATA_MARKED_FOR_POST);
                HashMap<String,String>checkFilter = new HashMap<>();
                checkFilter.put(db.KEY_ORDER_ID,orderID);
                checkFilter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                checkFilter.put(db.KEY_CUSTOMER_NO, Settings.getString(App.DRIVER));
                if(!db.checkData(db.LOAD_CONFIRMATION_HEADER,checkFilter)){
                    Log.e("Adding DATA","LCON");
                    db.addData(db.LOAD_CONFIRMATION_HEADER, map);
                }
                //db.addData(db.LOAD_CONFIRMATION_HEADER, map);

                final Dialog dialog = new Dialog(LoadVerifyActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_doprint);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
                LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);



                btn_print.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        print = true;
                        try{
                            JSONArray jsonArray = createPrintData(object.getLoadingDate(),object.getDeliveryNo());
                            JSONObject data = new JSONObject();
                            data.put("data",(JSONArray)jsonArray);
                            HashMap<String,String>printMap = new HashMap<>();
                            printMap.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                            printMap.put(db.KEY_ORDER_ID,object.getDeliveryNo());
                            printMap.put(db.KEY_DOC_TYPE,ConfigStore.LoadConfirmation_TR);
                            printMap.put(db.KEY_DATA, data.toString());
                            //map.put(db.KEY_DATA,jsonArray.toString());
                            db.addDataPrint(db.DELAY_PRINT, printMap);
                            PrinterHelper object = new PrinterHelper(LoadVerifyActivity.this,LoadVerifyActivity.this);
                            object.execute("", jsonArray);
                            dialog.dismiss();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        //finish();
                    }
                });
                btn_notprint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        print = false;
                        try {
                            JSONArray jsonArray = createPrintData(object.getLoadingDate(), object.getDeliveryNo());
                            JSONObject data = new JSONObject();
                            data.put("data", (JSONArray) jsonArray);
                            HashMap<String, String> printMap = new HashMap<>();
                            printMap.put(db.KEY_CUSTOMER_NO, Settings.getString(App.DRIVER));
                            printMap.put(db.KEY_ORDER_ID, object.getDeliveryNo());
                            printMap.put(db.KEY_DOC_TYPE, ConfigStore.LoadConfirmation_TR);
                            printMap.put(db.KEY_DATA, data.toString());
                            //map.put(db.KEY_DATA,jsonArray.toString());
                            db.addDataPrint(db.DELAY_PRINT, printMap);
                            dialog.dismiss();
                            Intent intent = new Intent(LoadVerifyActivity.this, LoadActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);

                //dialog.show();

                // Added to Disable Print option
                //Changes on 15/05/17
                //dialog.show();
                print = false;
                try{
                    JSONArray jsonArray = createPrintData(object.getLoadingDate(),object.getDeliveryNo());
                    JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);
                    HashMap<String,String>printMap = new HashMap<>();
                    printMap.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                    printMap.put(db.KEY_ORDER_ID,object.getDeliveryNo());
                    printMap.put(db.KEY_DOC_TYPE,ConfigStore.LoadConfirmation_TR);
                    printMap.put(db.KEY_DATA, data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT, printMap);
                    dialog.dismiss();
                    Intent intent = new Intent(LoadVerifyActivity.this, LoadActivity.class);
                    startActivity(intent);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                //End of Changes on 15/05/17

                /*Intent intent = new Intent(LoadVerifyActivity.this, LoadActivity.class);
                startActivity(intent);*/
            }
        }
    }

    /*********************************************************
     @ If user clicks on cancel button, ask the user for confirmation
     @ and take the user back to the previous screen
     *********************************************************/
    public void cancel(View v) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoadVerifyActivity.this);
        alertDialogBuilder.setTitle(getString(R.string.message))
                .setMessage(getString(R.string.cancel_msg))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Helpers.logData(LoadVerifyActivity.this, "Yes clicked on load verify screen on cancellation dialog prompt");
                        Intent intent = new Intent(LoadVerifyActivity.this, LoadSummaryActivity.class);
                        intent.putExtra("headerObj", object);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Helpers.logData(LoadVerifyActivity.this, "No clicked on load verify screen on cancellation dialog prompt");
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();


    }
    /*********************************************************
     @ Check if there are any loads for the user that is not verified
     *********************************************************/
    private boolean checkIfLoadExists() {
        try{
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_TRIP_ID, "");
            map.put(db.KEY_DELIVERY_NO, "");
            map.put(db.KEY_DELIVERY_DATE, "");
            map.put(db.KEY_DELIVERY_TYPE, "");
            HashMap<String, String> filters = new HashMap<>();
            filters.put(db.KEY_TRIP_ID, Settings.getString(TRIP_ID));
            filters.put(db.KEY_IS_VERIFIED, "false");
            Cursor cursor = db.getData(db.LOAD_DELIVERY_HEADER, map, filters);
            if (cursor.getCount() > 0) {
                return true;
            } else {
                return false;
            }

        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
            return false;
        }

    }
    /*********************************************************
     @ Check if the material already exists in the van or not.
     *********************************************************/
    private boolean checkMaterialExists(String materialno, String uom) {
        try{
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_MATERIAL_NO, "");
            if (uom.equals(App.CASE_UOM)) {
                map.put(db.KEY_UOM_CASE, "");
            } else if (uom.equals(App.PC_UOM+"%20")) {
                map.put(db.KEY_UOM_UNIT, "");
            }
            HashMap<String, String> filters = new HashMap<>();
            filters.put(db.KEY_MATERIAL_NO, materialno);
            if (uom.equals(App.CASE_UOM)) {
                filters.put(db.KEY_UOM_CASE, uom);
            } else if (uom.equals(App.PC_UOM+"%20")) {
                filters.put(db.KEY_UOM_UNIT, uom);
            }
            Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, map, filters);
            //Log.e("Cursorcountcheck", "" + materialno + uom + cursor.getCount());
            if (cursor.getCount() > 0) {
                return true;
            } else {
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
            return false;
        }

    }
    /*********************************************************
     @ Adding the verified load to the Van.
     *********************************************************/
    private void addItemstoVan(ArrayList<LoadSummary> dataNew) {
        isStockAdded = true;
        Helpers.logData(LoadVerifyActivity.this,"Adding items to van");
        try{
            for (int i = 0; i < dataNew.size(); i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_ENTRY_TIME, Helpers.getCurrentTimeStamp());
                map.put(db.KEY_DELIVERY_NO, object.getDeliveryNo().toString());
                map.put(db.KEY_MATERIAL_NO, dataNew.get(i).getMaterialNo().toString());
                map.put(db.KEY_ITEM_NO, dataNew.get(i).getItemCode().toString());
                map.put(db.KEY_MATERIAL_DESC1, dataNew.get(i).getItemDescription().toString());
                if (!((dataNew.get(i).getQuantityCases().isEmpty()) || (dataNew.get(i).getQuantityCases().equals(""))
                        || (dataNew.get(i).getQuantityCases().equals("0")) || (dataNew.get(i).getQuantityCases().equals("")))) {
                    //Check if old data exists for case
                    if (checkMaterialExists(dataNew.get(i).getMaterialNo().toString(), dataNew.get(i).getUom()/*App.CASE_UOM*/)) {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        Helpers.logData(LoadVerifyActivity.this,"Material" + dataNew.get(i).getMaterialNo().toString() + "found on van");
                        int reserved = 0;
                        //Getting old data
                        HashMap<String, String> oldData = new HashMap<>();
                        oldData.put(db.KEY_MATERIAL_NO, "");
                        oldData.put(db.KEY_UOM_CASE, "");
                        oldData.put(db.KEY_ACTUAL_QTY_CASE, "");
                        oldData.put(db.KEY_RESERVED_QTY_CASE, "");
                        oldData.put(db.KEY_REMAINING_QTY_CASE, "");
                        HashMap<String, String> filterOldData = new HashMap<>();
                        filterOldData.put(db.KEY_MATERIAL_NO, dataNew.get(i).getMaterialNo().toString());
                        //filterOldData.put(db.KEY_UOM_CASE, App.CASE_UOM);
                        filterOldData.put(db.KEY_UOM_CASE, dataNew.get(i).getUom());
                        Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, oldData, filterOldData);
                        float actualQtyCase = 0;
                        float reservedQtyCase = 0;
                        float remainingQtyCase = 0;
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                actualQtyCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY_CASE)));
                                reservedQtyCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_CASE)));
                                remainingQtyCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                                Helpers.logData(LoadVerifyActivity.this,"Quantity already on van" + actualQtyCase + "-" + reservedQtyCase + "-" + remainingQtyCase);
                                actualQtyCase += Float.parseFloat(dataNew.get(i).getQuantityCases().toString());
                                remainingQtyCase += Float.parseFloat(dataNew.get(i).getQuantityCases().toString());
                                Helpers.logData(LoadVerifyActivity.this,"Quantity new on van" + actualQtyCase + "-" + reservedQtyCase + "-" + remainingQtyCase);
                            }
                            while (cursor.moveToNext());
                            map.put(db.KEY_ACTUAL_QTY_CASE, String.valueOf(actualQtyCase));
                            map.put(db.KEY_RESERVED_QTY_CASE, String.valueOf(reservedQtyCase));
                            map.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(remainingQtyCase));
                            //map.put(db.KEY_UOM_CASE, App.CASE_UOM);
                            map.put(db.KEY_UOM_CASE, dataNew.get(i).getUom());
                            // db.updateData(db.VAN_STOCK_ITEMS, map, filterOldData);
                        }
                    } else {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        Helpers.logData(LoadVerifyActivity.this,"Material" + dataNew.get(i).getMaterialNo().toString() + "not found on van");
                        int reservedQty = 0;
                        map.put(db.KEY_ACTUAL_QTY_CASE, dataNew.get(i).getQuantityCases().toString());
                        map.put(db.KEY_RESERVED_QTY_CASE, String.valueOf(reservedQty));
                        map.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityCases().toString()) - reservedQty));
                        //map.put(db.KEY_UOM_CASE, App.CASE_UOM);
                        map.put(db.KEY_UOM_CASE, dataNew.get(i).getUom());
                        Helpers.logData(LoadVerifyActivity.this, "Material" + dataNew.get(i).getMaterialNo().toString() + "with quantity adding to van"
                                + dataNew.get(i).getQuantityCases().toString() + "-" + String.valueOf(reservedQty) + String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityCases().toString()) - reservedQty));
                        // db.addData(db.VAN_STOCK_ITEMS, map);
                    }

                /*map.put(db.KEY_ACTUAL_QTY_UNIT,dataNew.get(i).getQuantityUnits().toString());
                map.put(db.KEY_RESERVED_QTY_UNIT,String.valueOf("0"));
                map.put(db.KEY_REMAINING_QTY_UNIT,String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityUnits().toString())-0));
                map.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);*/
                } else {
                    if (checkMaterialExists(dataNew.get(i).getMaterialNo().toString(), dataNew.get(i).getUom()/*App.CASE_UOM*/)) {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        Helpers.logData(LoadVerifyActivity.this,"Material" + dataNew.get(i).getMaterialNo().toString() + "found on van");
                        int reserved = 0;
                        //Getting old data
                        HashMap<String, String> oldData = new HashMap<>();
                        oldData.put(db.KEY_MATERIAL_NO, "");
                        oldData.put(db.KEY_UOM_CASE, "");
                        oldData.put(db.KEY_ACTUAL_QTY_CASE, "");
                        oldData.put(db.KEY_RESERVED_QTY_CASE, "");
                        oldData.put(db.KEY_REMAINING_QTY_CASE, "");
                        HashMap<String, String> filterOldData = new HashMap<>();
                        filterOldData.put(db.KEY_MATERIAL_NO, dataNew.get(i).getMaterialNo().toString());
                        //filterOldData.put(db.KEY_UOM_CASE, App.CASE_UOM);
                        filterOldData.put(db.KEY_UOM_CASE, dataNew.get(i).getUom());
                        Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, oldData, filterOldData);
                        float actualQtyCase = 0;
                        float reservedQtyCase = 0;
                        float remainingQtyCase = 0;
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                actualQtyCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY_CASE)));
                                reservedQtyCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_CASE)));
                                remainingQtyCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                                Helpers.logData(LoadVerifyActivity.this,"Quantity already on van" + actualQtyCase + "-" + reservedQtyCase + "-" + remainingQtyCase);
                                actualQtyCase += Float.parseFloat(dataNew.get(i).getQuantityCases().toString());
                                remainingQtyCase += Float.parseFloat(dataNew.get(i).getQuantityCases().toString());
                                Helpers.logData(LoadVerifyActivity.this,"Quantity new on van" + actualQtyCase + "-" + reservedQtyCase + "-" + remainingQtyCase);
                            }
                            while (cursor.moveToNext());
                            map.put(db.KEY_ACTUAL_QTY_CASE, String.valueOf(actualQtyCase));
                            map.put(db.KEY_RESERVED_QTY_CASE, String.valueOf(reservedQtyCase));
                            map.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(remainingQtyCase));
                            //map.put(db.KEY_UOM_CASE, App.CASE_UOM);
                            map.put(db.KEY_UOM_CASE, dataNew.get(i).getUom());
                            // db.updateData(db.VAN_STOCK_ITEMS, map, filterOldData);
                        }
                    } else {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        Helpers.logData(LoadVerifyActivity.this,"Material" + dataNew.get(i).getMaterialNo().toString() + "not found on van");
                        int reservedQty = 0;
                        map.put(db.KEY_ACTUAL_QTY_CASE, dataNew.get(i).getQuantityCases().toString());
                        map.put(db.KEY_RESERVED_QTY_CASE, String.valueOf(reservedQty));
                        map.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityCases().toString()) - reservedQty));
                        //map.put(db.KEY_UOM_CASE, App.CASE_UOM);
                        map.put(db.KEY_UOM_CASE, dataNew.get(i).getUom());
                        Helpers.logData(LoadVerifyActivity.this, "Material" + dataNew.get(i).getMaterialNo().toString() + "with quantity adding to van"
                                + dataNew.get(i).getQuantityCases().toString() + "-" + String.valueOf(reservedQty) + String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityCases().toString()) - reservedQty));
                        // db.addData(db.VAN_STOCK_ITEMS, map);
                    }
                    /*int reservedQty = 0;
                    map.put(db.KEY_ACTUAL_QTY_CASE, dataNew.get(i).getQuantityCases().toString());
                    map.put(db.KEY_RESERVED_QTY_CASE, String.valueOf(reservedQty));
                    map.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityCases().toString()) - reservedQty));
                   // map.put(db.KEY_UOM_CASE, App.CASE_UOM);
                    map.put(db.KEY_UOM_CASE, dataNew.get(i).getUom());*/
                }
                if (!((dataNew.get(i).getQuantityUnits().isEmpty()) || (dataNew.get(i).getQuantityUnits().equals(""))
                        || (dataNew.get(i).getQuantityUnits().equals("0")) || (dataNew.get(i).getQuantityUnits().equals("")))) {
                    //Log.e("Step 0","Step 0");
                    if (checkMaterialExists(dataNew.get(i).getMaterialNo().toString(), dataNew.get(i).getUom()/*App.BOTTLES_UOM*/)) {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        int reserved = 0;
                        //Log.e("Step 1","Step 1");
                        //Getting old data
                        HashMap<String, String> oldData = new HashMap<>();
                        oldData.put(db.KEY_MATERIAL_NO, "");
                        oldData.put(db.KEY_UOM_UNIT, "");
                        oldData.put(db.KEY_ACTUAL_QTY_UNIT, "");
                        oldData.put(db.KEY_RESERVED_QTY_UNIT, "");
                        oldData.put(db.KEY_REMAINING_QTY_UNIT, "");
                        HashMap<String, String> filterOldData = new HashMap<>();
                        filterOldData.put(db.KEY_MATERIAL_NO, dataNew.get(i).getMaterialNo().toString());
                        //filterOldData.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                        filterOldData.put(db.KEY_UOM_UNIT,dataNew.get(i).getUom());
                        Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, oldData, filterOldData);
                        //Log.e("Step 2","Step 2" + cursor.getCount());
                        float actualQtyUnit;
                        float reservedQtyUnit;
                        float remainingQtyUnit;
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                actualQtyUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY_UNIT)));
                                reservedQtyUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_UNIT)));
                                remainingQtyUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                                //Log.e("Actual Qty Unit1","" + actualQtyUnit);
                                //Log.e("REmaining Qty Unit1","" + remainingQtyUnit);
                                actualQtyUnit += Float.parseFloat(dataNew.get(i).getQuantityUnits().toString());
                                remainingQtyUnit += Float.parseFloat(dataNew.get(i).getQuantityUnits().toString());
                                //Log.e("Actual Qty Unit2","" + actualQtyUnit);
                                //Log.e("REmaining Qty Unit2","" + remainingQtyUnit);
                            }
                            while (cursor.moveToNext());
                            map.put(db.KEY_ACTUAL_QTY_UNIT, String.valueOf(actualQtyUnit));
                            map.put(db.KEY_RESERVED_QTY_UNIT, String.valueOf(reservedQtyUnit));
                            map.put(db.KEY_REMAINING_QTY_UNIT, String.valueOf(remainingQtyUnit));
                            // map.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                            map.put(db.KEY_UOM_UNIT, dataNew.get(i).getUom());
                            //db.updateData(db.VAN_STOCK_ITEMS, map, filterOldData);
                        }
                    } else {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        int reservedQty = 0;
                        map.put(db.KEY_ACTUAL_QTY_UNIT, dataNew.get(i).getQuantityUnits().toString());
                        map.put(db.KEY_RESERVED_QTY_UNIT, String.valueOf(reservedQty));
                        map.put(db.KEY_REMAINING_QTY_UNIT, String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityUnits().toString()) - reservedQty));
                        //map.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                        map.put(db.KEY_UOM_UNIT, dataNew.get(i).getUom());
                    }

               /* map.put(db.KEY_ACTUAL_QTY_CASE,dataNew.get(i).getQuantityCases().toString());
                map.put(db.KEY_RESERVED_QTY_CASE,String.valueOf("0"));
                map.put(db.KEY_REMAINING_QTY_CASE,String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityCases().toString())-0));
                map.put(db.KEY_UOM_CASE, App.CASE_UOM);*/
                } else {
                    if (checkMaterialExists(dataNew.get(i).getMaterialNo().toString(), dataNew.get(i).getUom()/*App.BOTTLES_UOM*/)) {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        int reserved = 0;
                        //Log.e("Step 1","Step 1");
                        //Getting old data
                        HashMap<String, String> oldData = new HashMap<>();
                        oldData.put(db.KEY_MATERIAL_NO, "");
                        oldData.put(db.KEY_UOM_UNIT, "");
                        oldData.put(db.KEY_ACTUAL_QTY_UNIT, "");
                        oldData.put(db.KEY_RESERVED_QTY_UNIT, "");
                        oldData.put(db.KEY_REMAINING_QTY_UNIT, "");
                        HashMap<String, String> filterOldData = new HashMap<>();
                        filterOldData.put(db.KEY_MATERIAL_NO, dataNew.get(i).getMaterialNo().toString());
                        //filterOldData.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                        filterOldData.put(db.KEY_UOM_UNIT,dataNew.get(i).getUom());
                        Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, oldData, filterOldData);
                        //Log.e("Step 2","Step 2" + cursor.getCount());
                        float actualQtyUnit;
                        float reservedQtyUnit;
                        float remainingQtyUnit;
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                actualQtyUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY_UNIT)));
                                reservedQtyUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_UNIT)));
                                remainingQtyUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                                //Log.e("Actual Qty Unit1","" + actualQtyUnit);
                                //Log.e("REmaining Qty Unit1","" + remainingQtyUnit);
                                actualQtyUnit += Float.parseFloat(dataNew.get(i).getQuantityUnits().toString());
                                remainingQtyUnit += Float.parseFloat(dataNew.get(i).getQuantityUnits().toString());
                                //Log.e("Actual Qty Unit2","" + actualQtyUnit);
                                //Log.e("REmaining Qty Unit2","" + remainingQtyUnit);
                            }
                            while (cursor.moveToNext());
                            map.put(db.KEY_ACTUAL_QTY_UNIT, String.valueOf(actualQtyUnit));
                            map.put(db.KEY_RESERVED_QTY_UNIT, String.valueOf(reservedQtyUnit));
                            map.put(db.KEY_REMAINING_QTY_UNIT, String.valueOf(remainingQtyUnit));
                            // map.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                            map.put(db.KEY_UOM_UNIT, dataNew.get(i).getUom());
                            //db.updateData(db.VAN_STOCK_ITEMS, map, filterOldData);
                        }
                    } else {
                        //Logic to read Customer Delivery Item and block material quantity based on UOM
                        int reservedQty = 0;
                        map.put(db.KEY_ACTUAL_QTY_UNIT, dataNew.get(i).getQuantityUnits().toString());
                        map.put(db.KEY_RESERVED_QTY_UNIT, String.valueOf(reservedQty));
                        map.put(db.KEY_REMAINING_QTY_UNIT, String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityUnits().toString()) - reservedQty));
                        //map.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                        map.put(db.KEY_UOM_UNIT, dataNew.get(i).getUom());
                    }
                    /*int reservedQty = 0;
                    map.put(db.KEY_ACTUAL_QTY_UNIT, dataNew.get(i).getQuantityUnits().toString());
                    map.put(db.KEY_RESERVED_QTY_UNIT, String.valueOf(reservedQty));
                    map.put(db.KEY_REMAINING_QTY_UNIT, String.valueOf(Float.parseFloat(dataNew.get(i).getQuantityUnits().toString()) - reservedQty));
                    //map.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                    map.put(db.KEY_UOM_UNIT, dataNew.get(i).getUom());*/
                }
                //Log.e("ITEM MAP", "" + map);
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_MATERIAL_NO, dataNew.get(i).getMaterialNo().toString());
                //Log.e("Step 3", "Step 3");
                if ((checkMaterialExists(dataNew.get(i).getMaterialNo().toString(), App.CASE_UOM)) || (checkMaterialExists(dataNew.get(i).getMaterialNo().toString(), App.PC_UOM+"%20"))) {
                    db.updateData(db.VAN_STOCK_ITEMS, map, filter);
                } else {
                    db.addData(db.VAN_STOCK_ITEMS, map);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private boolean createDataForPost(final ArrayList<LoadSummary> dataNew, final ArrayList<LoadSummary> dataOld) {
        try{
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < dataNew.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ENTRY_TIME, Helpers.getCurrentTimeStamp());
                        map.put(db.KEY_DELIVERY_NO, object.getDeliveryNo().toString());
                        map.put(db.KEY_MATERIAL_NO, dataNew.get(i).getMaterialNo().toString());
                        map.put(db.KEY_ITEM_NO, dataNew.get(i).getItemCode().toString());
                        map.put(db.KEY_MATERIAL_DESC1, dataNew.get(i).getItemDescription().toString());
                        if (dataNew.get(i).getItemCode().equals(dataOld.get(i).getItemCode())) {
                            // Log.e("I m here","here")
                            map.put(db.KEY_ORG_CASE, dataOld.get(i).getQuantityCases());
                            map.put(db.KEY_VAR_CASE, dataNew.get(i).getQuantityCases());
                            map.put(db.KEY_ORG_UNITS, dataOld.get(i).getQuantityUnits());
                            map.put(db.KEY_VAR_UNITS, dataNew.get(i).getQuantityUnits());
                        }
                        db.addData(db.LOAD_DELIVERY_ITEMS_POST, map);
                    }
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }
    /*********************************************************
     @ Generating data to show on screen
     *********************************************************/
    public ArrayList<LoadSummary> generateData(ArrayList<LoadSummary> dataNew, ArrayList<LoadSummary> dataOld) {



        return loadSummaryList;
    }
    /*****************************************************************************************
     @ Recording variance and creating post request. If there is any
     @ variance a driver debit/credit note is created.
     @ Debit Note : If the actual quantity recorded for item is less than the loaded quantity
     @ Credit Note : If the actual quantity recorded for item is more than the loaded quantity
     ****************************************************************************************/
    private boolean addVarianceforPost(ArrayList<LoadSummary> dataNew, ArrayList<LoadSummary> dataOld) {
        try{
            for (int i = 0; i < dataNew.size(); i++) {
                LoadSummary loadSummary = new LoadSummary();
                loadSummary.setItemCode(dataNew.get(i).getItemCode());
                loadSummary.setMaterialNo(dataNew.get(i).getMaterialNo());
                loadSummary.setItemDescription(dataNew.get(i).getItemDescription());
                if (dataNew.get(i).getItemCode().equals(dataOld.get(i).getItemCode())) {
                    if (dataNew.get(i).getQuantityCases().equals(dataOld.get(i).getQuantityCases())) {
                        loadSummary.setQuantityCases("0");
                        Helpers.logData(LoadVerifyActivity.this, "Variance for Load not found" + loadSummary.getMaterialNo());
                    } else {
                        String code = Double.parseDouble(dataNew.get(i).getQuantityCases()) > Double.parseDouble(dataOld.get(i).getQuantityCases()) ? "C" : "D";
                        String quantity = code.equals("C")
                                ? String.valueOf(Double.parseDouble(dataNew.get(i).getQuantityCases()) - Double.parseDouble(dataOld.get(i).getQuantityCases()))
                                : String.valueOf(Double.parseDouble(dataOld.get(i).getQuantityCases()) - Double.parseDouble(dataNew.get(i).getQuantityCases()));
                        Helpers.logData(LoadVerifyActivity.this, "Variance for Load found" + loadSummary.getMaterialNo() + "For Code" + code + "with variance Quantity (Cases)" + quantity);
                        loadSummary.setQuantityCases(quantity + code);
                    }
                    //loadSummary.setQuantityCases(dataOld.get(i).getQuantityCases() + "|" + dataNew.get(i).getQuantityCases());
                }
                if (dataNew.get(i).getItemCode().equals(dataOld.get(i).getItemCode())) {
                    if (dataNew.get(i).getQuantityUnits().equals(dataOld.get(i).getQuantityUnits())) {
                        loadSummary.setQuantityUnits("0");
                    } else {
                        String code = Double.parseDouble(dataNew.get(i).getQuantityUnits()) > Double.parseDouble(dataOld.get(i).getQuantityUnits()) ? "C" : "D";
                        String quantity = code.equals("C")
                                ? String.valueOf(Double.parseDouble(dataNew.get(i).getQuantityUnits()) - Double.parseDouble(dataOld.get(i).getQuantityUnits()))
                                : String.valueOf(Double.parseDouble(dataOld.get(i).getQuantityUnits()) - Double.parseDouble(dataNew.get(i).getQuantityUnits()));
                        Helpers.logData(LoadVerifyActivity.this, "Variance for Load found" + loadSummary.getMaterialNo() + "For Code" + code + "with variance Quantity (Units)" + quantity);
                        loadSummary.setQuantityUnits(quantity + code);
                    }
                    //loadSummary.setQuantityUnits(dataOld.get(i).getQuantityUnits() + "|" + dataNew.get(i).getQuantityUnits());
                }
                loadSummary.setPrice(dataNew.get(i).getPrice());
                loadSummary.setReasonCode(dataNew.get(i).getReasonCode());
                loadSummary.setUom(dataNew.get(i).getUom());
                if (loadSummary.getQuantityCases().equals("0") && loadSummary.getQuantityUnits().equals("0")) {
                } else {
                    varianceLoadSummaryList.add(loadSummary);
                }
                //loadSummaryList.add(loadSummary);
            }
            if (varianceLoadSummaryList.size() > 0) {
                Helpers.logData(LoadVerifyActivity.this, "Variance for Load Found. Preparing for POST");
                String debitPRNo = "";
                String creditPRNo = "";
                for (int i = 0; i < varianceLoadSummaryList.size(); i++) {
                    LoadSummary loadSummary = varianceLoadSummaryList.get(i);
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_DATE, Helpers.formatDate(new Date(), App.DATE_FORMAT));
                    map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    map.put(db.KEY_CUSTOMER_NO, Settings.getString(App.DRIVER));
                    map.put(db.KEY_ITEM_NO, loadSummary.getItemCode());
                    map.put(db.KEY_MATERIAL_DESC1, loadSummary.getItemDescription());
                    map.put(db.KEY_MATERIAL_NO, loadSummary.getMaterialNo());
                    map.put(db.KEY_MATERIAL_GROUP, "");
                    map.put(db.KEY_CASE, loadSummary.getQuantityCases().contains("D")
                            ? loadSummary.getQuantityCases().replaceAll("D", "").trim()
                            : loadSummary.getQuantityCases().replaceAll("C", "").trim());
                    // map.put(db.KEY_CASE_DIFF,"");
                    map.put(db.KEY_UNIT, loadSummary.getQuantityUnits().contains("D")
                            ? loadSummary.getQuantityUnits().replaceAll("D", "").trim()
                            : loadSummary.getQuantityUnits().replaceAll("C", "").trim());
                    // map.put(db.KEY_UNIT,"");
                    // map.put(db.KEY_UNIT_DIFF,"");
                    if (loadSummary.getQuantityCases().contains("D") || loadSummary.getQuantityUnits().contains("D")) {
                        if (debitPRNo.equals("")) {
                            debitPRNo = Helpers.generateNumber(db, ConfigStore.LoadVarianceDebit_PR_Type);
                            Helpers.logData(LoadVerifyActivity.this, "Debit Variance Found. Reference no for debit post" + debitPRNo);
                        }
                    }
                    if (loadSummary.getQuantityCases().contains("C") || loadSummary.getQuantityUnits().contains("C")) {
                        if (creditPRNo.equals("")) {
                            creditPRNo = Helpers.generateNumber(db, ConfigStore.LoadVarianceCredit_PR_Type);
                            Helpers.logData(LoadVerifyActivity.this, "Credit Variance Found. Reference no for credit post" + creditPRNo);
                        }
                    }
                    map.put(db.KEY_UOM, loadSummary.getUom());
                    map.put(db.KEY_PRICE, loadSummary.getPrice());
                    map.put(db.KEY_ORDER_ID, loadSummary.getQuantityCases().contains("D") || loadSummary.getQuantityUnits().contains("D") ? debitPRNo : creditPRNo);
                    map.put(db.KEY_REASON_CODE, loadSummary.getReasonCode());
                    map.put(db.KEY_REASON_DESCRIPTION, "");
                    map.put(db.KEY_DOCUMENT_TYPE, loadSummary.getQuantityCases().contains("D") || loadSummary.getQuantityUnits().contains("D") ? ConfigStore.LoadVarianceDebit : ConfigStore.LoadVarianceCredit);
                    map.put(db.KEY_PURCHASE_NUMBER, loadSummary.getQuantityCases().contains("D") || loadSummary.getQuantityUnits().contains("D") ? debitPRNo : creditPRNo);
                    map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    map.put(db.KEY_IS_PRINTED, "");
                    db.addData(db.LOAD_VARIANCE_ITEMS_POST, map);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return true;
    }
    private String postData(String tempOrderID) {
        String orderID = "";
        /*HashMap<String, String> searchMap = new HashMap<>();
        searchMap.put(db.KEY_ORDER_ID, "");
        HashMap<String, String> filterMap = new HashMap<>();
        filterMap.put(db.KEY_DELIVERY_NO, object.getDeliveryNo());
        Cursor cursor = db.getData(db.LOAD_DELIVERY_ITEMS, searchMap, filterMap);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            tempOrderID = cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID));
        }*/
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("Function", ConfigStore.LoadConfirmationFunction);
            // map.put("OrderId", "0000000022");  //For testing purpose
            map.put("OrderId", tempOrderID);
            map.put("CustomerId", Settings.getString(App.DRIVER));
            JSONArray deepEntity = new JSONArray();
            JSONObject obj = new JSONObject();
            deepEntity.put(obj);
            orderID = IntegrationService.postData(LoadVerifyActivity.this, App.POST_COLLECTION, map, deepEntity);
            // orderID = IntegrationService.postDataBackup(LoadVerifyActivity.this, App.POST_COLLECTION, map, deepEntity);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return orderID + "," + tempOrderID;
    }
    private String postDataVariance(String varianceType) {
        String orderID = "";
        String purchaseNumber = "";
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put(db.KEY_DATE, "");
        dataMap.put(db.KEY_TIME_STAMP, "");
        dataMap.put(db.KEY_TRIP_ID, "");
        dataMap.put(db.KEY_CUSTOMER_NO, "");
        dataMap.put(db.KEY_ITEM_NO, "");
        dataMap.put(db.KEY_MATERIAL_DESC1, "");
        dataMap.put(db.KEY_MATERIAL_NO, "");
        dataMap.put(db.KEY_MATERIAL_GROUP, "");
        dataMap.put(db.KEY_CASE, "");
        dataMap.put(db.KEY_UNIT, "");
        dataMap.put(db.KEY_UOM, "");
        dataMap.put(db.KEY_PRICE, "");
        dataMap.put(db.KEY_ORDER_ID, "");
        dataMap.put(db.KEY_REASON_CODE, "");
        dataMap.put(db.KEY_REASON_DESCRIPTION, "");
        dataMap.put(db.KEY_DOCUMENT_TYPE, "");
        dataMap.put(db.KEY_PURCHASE_NUMBER, "");
        dataMap.put(db.KEY_IS_POSTED, "");
        dataMap.put(db.KEY_IS_PRINTED, "");
        HashMap<String, String> filter = new HashMap<>();
        filter.put(db.KEY_DOCUMENT_TYPE, varianceType);
        Cursor cursor = db.getData(db.LOAD_VARIANCE_ITEMS_POST, dataMap, filter);
        Log.e("Cursor count", "" + cursor.getCount());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            try {
                HashMap<String, String> map = new HashMap<>();
                map.put("Function", ConfigStore.LoadVarianceFunction);
                map.put("OrderId", "");
                map.put("DocumentType", varianceType);
                // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
                // map.put("DocumentDate", null);
           /* map.put("PurchaseNum", Helpers.generateNumber(db,ConfigStore.LoadRequest_PR_Type));
            purchaseNumber = map.get("PurchaseNum");*/
                map.put("CustomerId", Settings.getString(App.DRIVER));
                map.put("SalesOrg", Settings.getString(App.SALES_ORG));
                map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
                map.put("Division", Settings.getString(App.DIVISION));
                // map.put("OrderValue", String.valueOf(orderTotalValue));
                map.put("OrderValue", "0");
                map.put("Currency", App.CURRENCY);
                map.put("PurchaseNum", cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                purchaseNumber = map.get("PurchaseNum");
                //  map.put("DocumentDate", Helpers.parseDateforPost(cursor.getString(cursor.getColumnIndex(db.KEY_DATE))));
                JSONArray deepEntity = new JSONArray();
                int itemno = 10;
                do {
                    if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("Storagelocation", "");
                        //jo.put("RejReason",cursor.getString(cursor.getColumnIndex(db.KEY_REASON_CODE)));
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    } else {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("Storagelocation", "");
                        //jo.put("RejReason",cursor.getString(cursor.getColumnIndex(db.KEY_REASON_CODE)));
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    }
                }
                while (cursor.moveToNext());
                Log.e("Variance", "" + map + deepEntity);
                orderID = IntegrationService.postData(LoadVerifyActivity.this, App.POST_COLLECTION, map, deepEntity);
                // orderID = IntegrationService.postDataBackup(LoadVerifyActivity.this, App.POST_COLLECTION, map, deepEntity);
            } catch (Exception e) {
                Log.e("Variance Error", "Variance Error");
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
        Log.e("ORDERID", "" + orderID);
        return orderID + "," + purchaseNumber;
    }
    public class postData extends AsyncTask<Void, Void, Void> {
        String orderID = "";
        String tempOrderID = "";
        private String[] tokens = new String[2];
        private postData(String tempOrderID) {
            this.tempOrderID = tempOrderID;
            execute();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            this.orderID = postData(tempOrderID);
            Helpers.logData(LoadVerifyActivity.this,"Posting Load Confirmation" + tempOrderID);
            Helpers.logData(LoadVerifyActivity.this,"Response for Load Confirmation" + this.orderID);
            this.tokens = orderID.split(",");
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                if (this.tokens[0].toString().equals(this.tokens[1].toString())) {
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    map.put(db.KEY_ORDER_ID, tokens[0].toString());
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    db.updateData(db.LOAD_CONFIRMATION_HEADER, map, filter);

                    boolean test = addVarianceforPost(dataNew, dataOld);
                    HashMap<String, String> checkMap = new HashMap<>();
                    checkMap.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceDebit);
                    HashMap<String, String> checkMapCredit = new HashMap<>();
                    checkMapCredit.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);
                    if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMapCredit) || db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMap)) {
                        if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMap)) {
                            Helpers.logData(LoadVerifyActivity.this,"Found Variance" + this.orderID);
                            new postDataVariance(ConfigStore.LoadVarianceDebit);
                        } else if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMapCredit)) {
                            new postDataVariance(ConfigStore.LoadVarianceCredit);
                        }
                    } else {
                        new updateStockforCustomer().execute();
                    }
                } else if (this.orderID != null && !(this.orderID.equalsIgnoreCase(""))) {
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    boolean test = addVarianceforPost(dataNew, dataOld);
                    HashMap<String, String> checkMap = new HashMap<>();
                    checkMap.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceDebit);
                    HashMap<String, String> checkMapCredit = new HashMap<>();
                    checkMapCredit.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);
                    if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMapCredit) || db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMap)) {
                        if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMap)) {
                            new postDataVariance(ConfigStore.LoadVarianceDebit);
                        } else if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMapCredit)) {
                            new postDataVariance(ConfigStore.LoadVarianceCredit);
                        }
                    } else {
                        new updateStockforCustomer().execute();
                    }
               /* Intent intent = new Intent(LoadVerifyActivity.this,MyCalendarActivity.class);
                startActivity(intent);*/
                } else if (this.orderID.contains("Error")) {
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                /*if(print){
                    printData(this.tokens[0].toString());
                }*/
                    Toast.makeText(getApplicationContext(), this.orderID.replaceAll("Error", "").trim(), Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e){
                if (loadingSpinner.isShowing()) {
                    loadingSpinner.hide();
                }
                e.printStackTrace();
                Crashlytics.logException(e);
                new updateStockforCustomer().execute();
            }

        }
    }
    public class postDataVariance extends AsyncTask<Void, Void, Void> {
        String varianceType = "";
        String orderID = "";
        private String[] tokens = new String[2];

        private postDataVariance(String varianceType) {
            Helpers.logData(LoadVerifyActivity.this,"Posting Variance of type" + varianceType);
            loadingSpinner.show();
            this.varianceType = varianceType;
            execute();
        }
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            this.orderID = postDataVariance(this.varianceType);
            this.tokens = orderID.split(",");
            Helpers.logData(LoadVerifyActivity.this,"Variance posted with reference" + this.tokens[0].toString());
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            if (this.tokens[0].toString().equals(this.tokens[1].toString())) {
                if(varianceType.equals(ConfigStore.LoadVarianceDebit)){
                    Helpers.logData(LoadVerifyActivity.this,"Variance for Debit completed with reference" + this.tokens[0].toString());
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    map.put(db.KEY_ORDER_ID, tokens[0].toString());
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    filter.put(db.KEY_DOCUMENT_TYPE,ConfigStore.LoadVarianceDebit);

                    db.updateData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    HashMap<String, String> checkMapCredit = new HashMap<>();
                    checkMapCredit.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);
                    if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMapCredit)) {
                        Helpers.logData(LoadVerifyActivity.this,"Variance for Credit found");
                        new postDataVariance(ConfigStore.LoadVarianceCredit);
                    } else {
                        new updateStockforCustomer().execute();
                    }
                }
                else{

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    map.put(db.KEY_ORDER_ID, tokens[0].toString());
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    filter.put(db.KEY_DOCUMENT_TYPE,ConfigStore.LoadVarianceCredit);

                    db.updateData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                    Helpers.logData(LoadVerifyActivity.this, "Variance for Credit completed");
                    new updateStockforCustomer().execute();
                }
            }
            /*if (varianceType.equals(ConfigStore.LoadVarianceDebit)) {
                HashMap<String, String> checkMapCredit = new HashMap<>();
                checkMapCredit.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);
                if (db.checkData(db.LOAD_VARIANCE_ITEMS_POST, checkMapCredit)) {
                    new postDataVariance(ConfigStore.LoadVarianceCredit);
                } else {
                    new updateStockforCustomer().execute();
                }
            } else {
                new updateStockforCustomer().execute();
            }*/
        }
    }
    /*****************************************************************************************
     @ If there are delivery for the customer, stocked needs to be blocked from the van for the customer
     @ against the materials in the van.
     ****************************************************************************************/
    public class updateStockforCustomer extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            calculateStock();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(Helpers.isNetworkAvailable(getApplicationContext())){
                Helpers.createBackgroundJob(getApplicationContext());
            }
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            Helpers.logData(LoadVerifyActivity.this,"Updating stock for customer complete");

            if(print){
                Helpers.logData(LoadVerifyActivity.this,"User had selected to print load verification");
                try{
                    JSONArray jsonArray = createPrintData(object.getLoadingDate(),object.getDeliveryNo());
                    JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);
                    HashMap<String,String>map = new HashMap<>();
                    map.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                    map.put(db.KEY_ORDER_ID,object.getDeliveryNo());
                    map.put(db.KEY_DOC_TYPE,ConfigStore.LoadConfirmation_TR);
                    map.put(db.KEY_DATA,data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT,map);


                    PrinterHelper object = new PrinterHelper(LoadVerifyActivity.this,LoadVerifyActivity.this);
                    object.execute("", jsonArray);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
            else{
                Helpers.logData(LoadVerifyActivity.this,"User had selected for delay print load verification");
                try{
                    JSONArray jsonArray = createPrintData(object.getLoadingDate(),object.getDeliveryNo());
                    JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);

                    HashMap<String,String>map = new HashMap<>();
                    map.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                    map.put(db.KEY_ORDER_ID,object.getDeliveryNo());
                    map.put(db.KEY_DOC_TYPE,ConfigStore.LoadConfirmation_TR);
                    map.put(db.KEY_DATA,data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT,map);

                    Intent intent = new Intent(LoadVerifyActivity.this, MyCalendarActivity.class);
                    startActivity(intent);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }

        }
    }

    void calculateStock() {
        Helpers.logData(LoadVerifyActivity.this,"Updating stock for customer start");
        try{
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_DELIVERY_NO, "");
            map.put(db.KEY_ITEM_NO, "");
            map.put(db.KEY_ITEM_CATEGORY, "");
            map.put(db.KEY_CREATED_BY, "");
            map.put(db.KEY_ENTRY_TIME, "");
            map.put(db.KEY_DATE, "");
            map.put(db.KEY_MATERIAL_NO, "");
            map.put(db.KEY_MATERIAL_ENTERED, "");
            map.put(db.KEY_MATERIAL_GROUP, "");
            map.put(db.KEY_PLANT, "");
            map.put(db.KEY_STORAGE_LOCATION, "");
            map.put(db.KEY_BATCH, "");
            map.put(db.KEY_ACTUAL_QTY, "");
            map.put(db.KEY_REMAINING_QTY, "");
            map.put(db.KEY_UOM, "");
            map.put(db.KEY_DIST_CHANNEL, "");
            map.put(db.KEY_DIVISION, "");
            map.put(db.KEY_IS_DELIVERED, "");
            HashMap<String, String> filter = new HashMap<>();
            Cursor deliveryCursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS, map, filter);
            Helpers.logData(LoadVerifyActivity.this,"Fetching delivery items");
            if (deliveryCursor.getCount() > 0) {
                Helpers.logData(LoadVerifyActivity.this,"found Delivery for Driver :" + deliveryCursor.getCount());
                deliveryCursor.moveToFirst();
                do {
                    HashMap<String, String> mapVanStock = new HashMap<>();
                    mapVanStock.put(db.KEY_DELIVERY_NO, "");
                    mapVanStock.put(db.KEY_ITEM_NO, "");
                    mapVanStock.put(db.KEY_ITEM_CATEGORY, "");
                    mapVanStock.put(db.KEY_CREATED_BY, "");
                    mapVanStock.put(db.KEY_ENTRY_TIME, "");
                    mapVanStock.put(db.KEY_DATE, "");
                    mapVanStock.put(db.KEY_MATERIAL_NO, "");
                    mapVanStock.put(db.KEY_MATERIAL_DESC1, "");
                    mapVanStock.put(db.KEY_MATERIAL_ENTERED, "");
                    mapVanStock.put(db.KEY_MATERIAL_GROUP, "");
                    mapVanStock.put(db.KEY_PLANT, "");
                    mapVanStock.put(db.KEY_STORAGE_LOCATION, "");
                    mapVanStock.put(db.KEY_BATCH, "");
                    mapVanStock.put(db.KEY_ACTUAL_QTY_CASE, "");
                    mapVanStock.put(db.KEY_ACTUAL_QTY_UNIT, "");
                    mapVanStock.put(db.KEY_RESERVED_QTY_CASE, "");
                    mapVanStock.put(db.KEY_RESERVED_QTY_UNIT, "");
                    mapVanStock.put(db.KEY_REMAINING_QTY_CASE, "");
                    mapVanStock.put(db.KEY_REMAINING_QTY_UNIT, "");
                    mapVanStock.put(db.KEY_UOM_CASE, "");
                    mapVanStock.put(db.KEY_UOM_UNIT, "");
                    mapVanStock.put(db.KEY_DIST_CHANNEL, "");
                    mapVanStock.put(db.KEY_DIVISION, "");
                    mapVanStock.put(db.KEY_IS_VERIFIED, "");
                    HashMap<String, String> filterVanStock = new HashMap<>();
                    Cursor vanStockCursor = db.getData(db.VAN_STOCK_ITEMS, mapVanStock, filterVanStock);
                    if (vanStockCursor.getCount() > 0) {
                        vanStockCursor.moveToFirst();
                        do {
                            if (vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_MATERIAL_NO)).equals(deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_MATERIAL_NO)))) {
                                if (deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM_NEW)||deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    String deliveryCase = deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_ACTUAL_QTY));
                                    String actualCase = vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_ACTUAL_QTY_CASE));
                                    String reservedCase = vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_RESERVED_QTY_CASE));
                                    String remainingCase = vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE));
                                    Helpers.logData(LoadVerifyActivity.this,"Actual stock for material" + vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_MATERIAL_NO))
                                            + "on Van" + actualCase + "-" + reservedCase + "-" + remainingCase);
                                    HashMap<String, String> updateMap = new HashMap<>();
                                    updateMap.put(db.KEY_ACTUAL_QTY_CASE, actualCase);
                                    updateMap.put(db.KEY_RESERVED_QTY_CASE, String.valueOf(Float.parseFloat(reservedCase == null ? "0" : reservedCase) + Float.parseFloat(deliveryCase == null ? "0" : deliveryCase)));
                                    updateMap.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(Float.parseFloat(remainingCase == null ? "0" : remainingCase) - Float.parseFloat(deliveryCase == null ? "0" : deliveryCase)));
                                    HashMap<String, String> updateFilter = new HashMap<>();
                                    updateFilter.put(db.KEY_MATERIAL_NO, vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    //Log.e("Update Map C/S","" + updateMap);
                                    db.updateData(db.VAN_STOCK_ITEMS, updateMap, updateFilter);
                                } else {
                                    String deliveryUnits = deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_ACTUAL_QTY));
                                    String actualUnits = vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_ACTUAL_QTY_UNIT));
                                    String reservedUnits = vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_RESERVED_QTY_UNIT));
                                    String remainingUnits = vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT));
                                    HashMap<String, String> updateMap = new HashMap<>();
                                    updateMap.put(db.KEY_ACTUAL_QTY_UNIT, actualUnits);
                                    updateMap.put(db.KEY_RESERVED_QTY_UNIT, String.valueOf(Float.parseFloat(reservedUnits == null ? "0" : reservedUnits) + Float.parseFloat(deliveryUnits == null ? "0" : deliveryUnits)));
                                    updateMap.put(db.KEY_REMAINING_QTY_UNIT, String.valueOf(Float.parseFloat(remainingUnits == null ? "0" : remainingUnits) - Float.parseFloat(deliveryUnits == null ? "0" : deliveryUnits)));
                                    //Log.e("Update Map B/T", "" + updateMap);
                                    HashMap<String, String> updateFilter = new HashMap<>();
                                    updateFilter.put(db.KEY_MATERIAL_NO, vanStockCursor.getString(vanStockCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    db.updateData(db.VAN_STOCK_ITEMS, updateMap, updateFilter);
                                }
                                break;
                            }
                        }
                        while (vanStockCursor.moveToNext());
                    }
                }
                while (deliveryCursor.moveToNext());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    /*****************************************************************************************
     @ Creating data for print and saving it in DB for future printing
     ****************************************************************************************/
    public JSONArray createPrintData(String orderDate,String orderNo){
        JSONArray jArr = new JSONArray();
        try{
            double totalAmount = 0;
            double totalPcs = 0;
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST,App.LOAD_SUMMARY_REQUEST);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE",Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("TIME",Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO","-");
            mainArr.put("DOCUMENT NO",orderNo);  //Load Summary No
            mainArr.put("TRIP START DATE",Helpers.formatDate(new Date(),"dd-MM-yyyy"));
            mainArr.put("supervisorname","-");
            mainArr.put("TripID",Settings.getString(App.TRIP_ID));
            mainArr.put("Load Number", String.valueOf(++Const.loadNumber));

            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();
            HEADERS.put("S.NO");
            HEADERS.put("ITEM#");
            HEADERS.put("DESCRIPTION");
            HEADERS.put("UPC");
            HEADERS.put("LOAD_REQUESTED CTN      PCS");
            HEADERS.put("LOAD_RECEIVED CTN      PCS");
            HEADERS.put("DIFFERENCE CTN      PCS");
            HEADERS.put("VALUE");


//            1.	S.NO				- Serial Number
//            2.	ITEM#				- Item Code
//            3.	ITEM DESCRIPTION		- Description of the Item
//            4.	LOAD REQUESTED (CTN)	- Load requested yesterday
//            5.	LOAD REQUESTED (PCS)		- Load requested yesterday
//            6.	ACTUAL LOAD RECEIVED (CTN)	- Load received today
//            7.	ACTUAL LOAD RECEIVED (PCS)	- Load received today
//            8.	DIFFERENCE (CTN)		- Difference of Load requested yesterday and Load received today
//            9.	DIFFERENCE (PCS)		- Difference of Load requested yesterday and Load received today
//            10.	VALUE				- Amount of each line item (quantity x minimum price)



            //HEADERS.put(obj1);
            // HEADERS.put(obj2);
            mainArr.put("HEADERS",HEADERS);

            JSONArray jData = new JSONArray();

            for(int i=0;i<dataNew.size();i++){
                LoadSummary obj = dataNew.get(i);
                LoadSummary obj1 = dataOld.get(i);

                String deno = "0";
                HashMap<String, String> altMap = new HashMap<>();
                altMap.put(db.KEY_UOM, "");
                altMap.put(db.KEY_DENOMINATOR, "");
                HashMap<String, String> filtera = new HashMap<>();
                filtera.put(db.KEY_MATERIAL_NO, obj.getMaterialNo());
                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                if (altUOMCursor.getCount() > 0) {
                    altUOMCursor.moveToFirst();
                    deno = ""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                }

                double AllitemLR = Integer.parseInt(obj1.getQuantityUnits())/Double.parseDouble(deno);
                int intCasesLR = (int)AllitemLR;
                int intpcsLR = Integer.parseInt(obj1.getQuantityUnits()) - (intCasesLR*Integer.parseInt(deno));

                double AllitemALR = Integer.parseInt(obj.getQuantityUnits())/Double.parseDouble(deno);
                int intCasesALR = (int)AllitemALR;
                int intpcsALR = Integer.parseInt(obj.getQuantityUnits()) - (intCasesLR*Integer.parseInt(deno));

                double variance = Double.parseDouble(obj1.getQuantityUnits())-Double.parseDouble(obj.getQuantityUnits());
                double AllitemDLR = (int)variance/Double.parseDouble(deno);
                int intCasesDLR = (int)AllitemDLR;
                int intpcsDLR = (int)variance - (intCasesDLR*Integer.parseInt(deno));


                JSONArray data = new JSONArray();
                data.put(""+(jData.length()+1));
                data.put(obj.getMaterialNo());
                data.put(UrlBuilder.decodeString(obj.getItemDescription()));
//                data.put(obj.getItem_description_ar()==null?App.ARABIC_TEXT_MISSING:obj.getItem_description_ar());
//                data.put("1");
                data.put(deno);
                data.put(0+ "      " + 0);
                data.put(intCasesALR+"      "+intpcsALR);
                data.put(intCasesDLR+"      "+intpcsDLR);

                totalPcs+= Double.parseDouble(obj.getQuantityUnits());
                data.put(String.format("%.2f",Double.parseDouble(obj.getQuantityUnits())*Double.parseDouble(obj.getPrice())));
                totalAmount+=Double.parseDouble(obj.getQuantityUnits())*Double.parseDouble(obj.getPrice());
                jData.put(data);
            }
            JSONObject totalObj = new JSONObject();
//            totalObj.put("TOTAL UNITS","+" + String.format("%.2f",totalPcs));
//            totalObj.put("UNIT PRICE","");
            totalObj.put("Total Amount(AED)",String.format("%.2f",totalAmount));
            TOTAL.put(totalObj);
            mainArr.put("TOTAL",TOTAL);
            mainArr.put("data",jData);
            jDict.put("mainArr",mainArr);
            jInter.put(jDict);
            jArr.put(jInter);

            // jArr.put(HEADERS);
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return jArr;
    }
    /*****************************************************************************************
     @ Callback function once the printing is completed
     ****************************************************************************************/
    public void callbackFunction(){
        Helpers.logData(LoadVerifyActivity.this,"Callback function for Load Verification Print");
        if (!checkIfLoadExists()){
            Helpers.logData(LoadVerifyActivity.this,"Callback function - This was last load");
            Intent intent = new Intent(LoadVerifyActivity.this, MyCalendarActivity.class);
            startActivity(intent);
        }
        else{
            Helpers.logData(LoadVerifyActivity.this,"Callback function - Going back for another load");
            Intent intent = new Intent(LoadVerifyActivity.this, LoadActivity.class);
            startActivity(intent);
        }

    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here
                Log.e("BE","User clicked on back");
                Helpers.logData(LoadVerifyActivity.this, "Clicked on back on Load Verify Screen");
                Helpers.logData(LoadVerifyActivity.this,"Going back to Load Summary Screen");
                Intent intent = new Intent(LoadVerifyActivity.this, LoadSummaryActivity.class);
                intent.putExtra("headerObj", object);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
