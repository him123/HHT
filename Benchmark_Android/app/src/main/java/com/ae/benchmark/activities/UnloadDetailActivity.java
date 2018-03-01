package com.ae.benchmark.activities;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ae.benchmark.models.CustomerStatus;
import com.ae.benchmark.utils.ConfigStore;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.*;
import com.ae.benchmark.adapters.UnloadAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Unload;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by Rakshit on 18-Jan-17.
 */
public class UnloadDetailActivity extends AppCompatActivity {
    private DatabaseHandler db = new DatabaseHandler(this);
    private LoadingSpinner loadingSpinner;
    ListView listView;
    CheckBox cbSelectAll;
    ArrayAdapter<Unload> adapter;
    public ArrayList<Unload> arrayList = new ArrayList<>();
    private ArrayList<Unload> dataStoreList = new ArrayList<>();
    private ArrayList<Unload> varianceList = new ArrayList<>();
    public ArrayList<ArticleHeader> articles;
    private ArrayList<CustomerStatus> ResonList = new ArrayList<>();
    private ArrayAdapter<CustomerStatus> statusAdapter;
    private ArrayList<String> reasonsArray = new ArrayList<>();
    public String context;
    private String reasonCode;
    FloatingActionButton fab;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unload_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = this.getIntent();
        context = i.getExtras().getString("context");
        if(context.equals("badreturn")){
            reasonCode = i.getExtras().getString("code");
        }
        loadingSpinner = new LoadingSpinner(this);
        articles = ArticleHeaders.get();
        setTitle(getTitle(context));
        cbSelectAll = (CheckBox) findViewById(R.id.cbSelectAll);
        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter = new UnloadAdapter(UnloadDetailActivity.this, arrayList);
        listView.setAdapter(adapter);
        loadingSpinner.show();
        try {
            new loadData(context);
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        reasonsArray.add(getString(R.string.select_reason));
        reasonsArray.add(getString(R.string.ending_inventory));
        reasonsArray.add(getString(R.string.truck_damage));
        reasonsArray.add(getString(R.string.theft));
        reasonsArray.add(getString(R.string.excess));

        CustomerStatus status = new CustomerStatus();
//        status.setReasonCode("0");
//        status.setReasonDescription(getString(R.string.select_reason));
//        ResonList.add(status);
//        status = new CustomerStatus();
        status.setReasonCode("1");
        status.setReasonDescription(getString(R.string.ending_inventory));
        ResonList.add(status);
        status = new CustomerStatus();
        status.setReasonCode("2");
        status.setReasonDescription(getString(R.string.truck_damage));
        ResonList.add(status);
        status = new CustomerStatus();
        status.setReasonCode("3");
        status.setReasonDescription(getString(R.string.theft));
        ResonList.add(status);
        status = new CustomerStatus();
        status.setReasonCode("4");
        status.setReasonDescription(getString(R.string.excess));
        ResonList.add(status);

        statusAdapter = new CustomerStatusAdapter(this, ResonList);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.equals("freshunload")) {
                    showStatusDialog();
                }else {
                    new saveItems().execute();
                }
            }
        });
        if (context.equals("freshunload")) {
            cbSelectAll.setVisibility(View.VISIBLE);
            cbSelectAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(arrayList.size() > 0){
                        if(cbSelectAll.isChecked()) {
                            for (Unload unload : arrayList) {
                                    arrayList.get(arrayList.indexOf(unload)).setIsCheck("true");
                            }
                        }else{
                            for (Unload unload : arrayList) {
                                arrayList.get(arrayList.indexOf(unload)).setIsCheck("false");
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }else{
            cbSelectAll.setVisibility(View.GONE);
        }

        if(context.equals("endinginventory")||context.equals("inventoryvariance")||context.equals("truckdamage")||context.equals("badreturnvariance")){
            registerForContextMenu(listView);
        }



        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

            }
        });*/
    }

    public void setclick(final View view, final int position){
        try{
            final Unload sales = arrayList.get(position);
            final Dialog dialog = new Dialog(UnloadDetailActivity.this);
            final String[] reasonCode = {""};
            dialog.setContentView(R.layout.dialog_with_crossbutton);
            dialog.setCancelable(false);
            TextView tv = (TextView) dialog.findViewById(R.id.dv_title);
            tv.setText(sales.getName());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            ImageView iv_cancle = (ImageView) dialog.findViewById(R.id.imageView_close);
            Button btn_save = (Button) dialog.findViewById(R.id.btn_save);
            final EditText ed_cases = (EditText) dialog.findViewById(R.id.ed_cases);
            final EditText ed_pcs = (EditText) dialog.findViewById(R.id.ed_pcs);
            final EditText ed_cases_inv = (EditText) dialog.findViewById(R.id.ed_cases_inv);
            final EditText ed_pcs_inv = (EditText) dialog.findViewById(R.id.ed_pcs_inv);
            LinearLayout ll1 = (LinearLayout) dialog.findViewById(R.id.ll_1);
            ll1.setVisibility(View.GONE);
            RelativeLayout rl_specify = (RelativeLayout) dialog.findViewById(R.id.rl_specify_reason);
            final Spinner spin = (Spinner) dialog.findViewById(R.id.spin);

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(UnloadDetailActivity.this, android.R.layout.simple_spinner_item, reasonsArray); //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spin.setAdapter(spinnerArrayAdapter);
            if (sales.getReasonCode() != null) {
                spin.setSelection(Integer.parseInt(sales.getReasonCode()));
            }
//                    if (context.equals("freshunload")||context.equals("badreturn")) {
//                        rl_specify.setVisibility(View.VISIBLE);
//                    } else {
            rl_specify.setVisibility(View.GONE);
//                    }
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String reason = spin.getSelectedItem().toString();
                    if (reason.equals(getString(R.string.ending_inventory))) {
                        reasonCode[0] = "1";
                    } else if (reason.equals(getString(R.string.truck_damage))) {
                        reasonCode[0] = "2";
                    } else if (reason.equals(getString(R.string.theft))) {
                        reasonCode[0] = "3";
                    } else if (reason.equals(getString(R.string.excess))) {
                        reasonCode[0] = "4";
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            ed_cases_inv.setText(sales.getInv_cases());
            ed_pcs_inv.setText(sales.getInv_piece());
//                    ed_cases_inv.setEnabled(false);
//                    ed_pcs_inv.setEnabled(false);
            ed_cases_inv.setVisibility(View.INVISIBLE);
            ed_pcs_inv.setVisibility(View.INVISIBLE);
//                    if (sales.isAltUOM()) {
//                        ed_pcs.setEnabled(true);
//                    } else {
//                        ed_pcs.setEnabled(false);
//                    }

            //ed_cases.setText(sales.getCases());
            //ed_pcs.setText(sales.getPic());

            ed_cases.setText(sales.getCases().equals("0")?"":sales.getCases());
            ed_pcs.setText(sales.getPic().equals("0")?"":sales.getPic());

            LinearLayout ll_1 = (LinearLayout) dialog.findViewById(R.id.ll_1);
            iv_cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            if(context.equals("endinginventory")||context.equals("inventoryvariance")||context.equals("truckdamage")||context.equals("badreturnvariance")){
            }
            else{
                dialog.show();
            }

            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                            /*if (spin.getSelectedItem().toString().equals(getString(R.string.select_reason))) {
                                ((TextView) spin.getSelectedView()).setError("select reason");
                            } else {*/

                    String strCase = ed_cases.getText().toString();
                    String strpcs = ed_pcs.getText().toString();
                    String strcaseinv = ed_cases_inv.getText().toString();
                    String strpcsinv = ed_pcs_inv.getText().toString();
                    TextView tv_cases = (TextView) view.findViewById(R.id.tv_cases_value);
                    TextView tv_pcs = (TextView) view.findViewById(R.id.tv_pcs_value);

                    tv_cases.setText(strCase.equals("")?"0":strCase);
                    tv_pcs.setText(strpcs.equals("")?"0":strpcs);

                    if (strCase.isEmpty() || strCase == null || strCase.trim().equals("")) {
                        strCase = String.valueOf(0);
                    }
                    if (strpcs.isEmpty() || strpcs == null || strpcs.trim().equals("")) {
                        strpcs = String.valueOf(0);
                    }
                    if (strcaseinv.isEmpty() || strcaseinv == null || strcaseinv.trim().equals("")) {
                        strcaseinv = String.valueOf(0);
                    }
                    if (strpcsinv.isEmpty() || strpcsinv == null || strpcsinv.trim().equals("")) {
                        strpcsinv = String.valueOf(0);
                    }

                    sales.setCases(strCase);
                    sales.setPic(strpcs);
//                                sales.setReasonCode(reasonCode[0]);
                    arrayList.remove(position);
                    arrayList.add(position, sales);
                    hideSoftKeyboard();
                    dialog.dismiss();

//                            }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void showStatusDialog() {
        try{
            final Dialog dialog = new Dialog(UnloadDetailActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View view = getLayoutInflater().inflate(R.layout.activity_select_customer_status, null);
            TextView tv_header = (TextView) view.findViewById(R.id.tv_top_header);
            tv_header.setText("Unload Reason");
            ListView lv = (ListView) view.findViewById(R.id.statusList);
            Button cancel = (Button) view.findViewById(R.id.btnCancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            lv.setAdapter(statusAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String data = ResonList.get(position).getReasonCode().toString();
                    Log.e("Data","data "+data);
                    if(!data.equals(getString(R.string.select_reason))){
                        new loadItems(data);
                    }
                    dialog.dismiss();
                }
            });
            Log.e("arr","data"+arrayList.size());
            dialog.setContentView(view);
            dialog.setCancelable(false);
            dialog.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private class loadItems extends AsyncTask<Void, Void, Void> {
        String code="";
        public loadItems(String data) {
            code = data;
            execute();
        }

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            new saveItems().execute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            for(Unload unload: arrayList){
                if(unload.getIsCheck().equals("true")){
                    arrayList.get(arrayList.indexOf(unload)).setReasonCode(code);
                }
            }


            return null;
        }
    }
    private class saveItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

                try{
                    dataStoreList.clear();
                    if (context.equals("badreturn")) {
                        for (int i = 0; i < articles.size(); i++) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_ITEM_NO, "");
                            map.put(db.KEY_MATERIAL_DESC1, "");
                            map.put(db.KEY_MATERIAL_NO, "");
                            map.put(db.KEY_MATERIAL_GROUP, "");
                            map.put(db.KEY_CASE, "");
                            map.put(db.KEY_UNIT, "");
                            map.put(db.KEY_UOM, "");
                            HashMap<String, String> filter = new HashMap<>();
                            filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                            filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                            filter.put(db.KEY_REASON_CODE,reasonCode);
                            Cursor c = db.getData(db.RETURNS, map, filter);
                            if (c.getCount() > 0) {
                                c.moveToFirst();
                                Unload unload = new Unload();
                                unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                                unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                                unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));
                                float casesTotal = 0;
                                float unitsTotal = 0;
                                do {
                                    casesTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                    unitsTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (c.moveToNext());
                                unload.setCases(String.valueOf(casesTotal));
                                unload.setPic(String.valueOf(unitsTotal));
                                Helpers.logData(UnloadDetailActivity.this,"Unmodified data" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                                dataStoreList.add(unload);
                            }
                        }
                    }
                    if (context.equals("badreturnvariance")) {
                        for (int i = 0; i < articles.size(); i++) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_ITEM_NO, "");
                            map.put(db.KEY_MATERIAL_DESC1, "");
                            map.put(db.KEY_MATERIAL_NO, "");
                            map.put(db.KEY_MATERIAL_GROUP, "");
                            map.put(db.KEY_CASE, "");
                            map.put(db.KEY_UNIT, "");
                            map.put(db.KEY_UOM, "");
                            HashMap<String, String> filter = new HashMap<>();
                            filter.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                            filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                            Cursor c = db.getData(db.UNLOAD_VARIANCE, map, filter);
                            if (c.getCount() > 0) {
                                c.moveToFirst();
                                Unload unload = new Unload();
                                unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                                unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                                unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));
                                float casesTotal = 0;
                                float unitsTotal = 0;
                                do {
                                    casesTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                    unitsTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (c.moveToNext());
                                unload.setCases(String.valueOf(casesTotal));
                                unload.setPic(String.valueOf(unitsTotal));
                                Helpers.logData(UnloadDetailActivity.this, "Bad Return Variance" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                                dataStoreList.add(unload);
                            }
                        }
                    }
                    if (context.equals("freshunload")) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_ITEM_CATEGORY, "");
                        map.put(db.KEY_CREATED_BY, "");
                        map.put(db.KEY_ENTRY_TIME, "");
                        map.put(db.KEY_DATE, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_MATERIAL_DESC1, "");
                        map.put(db.KEY_MATERIAL_ENTERED, "");
                        map.put(db.KEY_MATERIAL_GROUP, "");
                        map.put(db.KEY_PLANT, "");
                        map.put(db.KEY_STORAGE_LOCATION, "");
                        map.put(db.KEY_BATCH, "");
                        map.put(db.KEY_ACTUAL_QTY_CASE, "");
                        map.put(db.KEY_ACTUAL_QTY_UNIT, "");
                        map.put(db.KEY_RESERVED_QTY_CASE, "");
                        map.put(db.KEY_RESERVED_QTY_UNIT, "");
                        map.put(db.KEY_REMAINING_QTY_CASE, "");
                        map.put(db.KEY_REMAINING_QTY_UNIT, "");
                        map.put(db.KEY_UOM_CASE, "");
                        map.put(db.KEY_UOM_UNIT, "");
                        map.put(db.KEY_DIST_CHANNEL, "");
                        HashMap<String, String> filter = new HashMap<>();
                        Cursor c1 = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                        if (c1.getCount() > 0) {
                            c1.moveToFirst();
                            do {
                                Unload unload = new Unload();
                                unload.setName(UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                unload.setItem_code(c1.getString(c1.getColumnIndex(db.KEY_ITEM_NO)));
                                unload.setMaterial_no(c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));
                                ArticleHeader articleHeader = ArticleHeader.getArticle(articles,c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));
                                String uomCase = c1.getString(c1.getColumnIndex(db.KEY_UOM_CASE));
                                String uomUnit = c1.getString(c1.getColumnIndex(db.KEY_UOM_UNIT));
                                unload.setUom((uomCase == null || uomCase.equals("")) ? uomUnit : uomCase);
                                unload.setCases(String.valueOf(Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_REMAINING_QTY_CASE))) + Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_RESERVED_QTY_CASE)))));
                                unload.setPic(String.valueOf(Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_REMAINING_QTY_UNIT))) + Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_RESERVED_QTY_UNIT)))));
                                Helpers.logData(UnloadDetailActivity.this, "Fresh Unload data" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                                dataStoreList.add(unload);
                            }
                            while (c1.moveToNext());
                        }
                        try{
                            ArrayList<Unload> vanData = dataStoreList;
                            for (int i = 0; i < vanData.size(); i++) {
                                Unload unload = vanData.get(i);
                                HashMap<String, String> map1 = new HashMap<>();
                                map1.put(db.KEY_CASE, "");
                                map1.put(db.KEY_UNIT, "");
                                HashMap<String, String> checkInventoryFilter = new HashMap<>();
                                checkInventoryFilter.put(db.KEY_VARIANCE_TYPE, App.ENDING_INVENTORY);
                                checkInventoryFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                                HashMap<String, String> truckDamageFilter = new HashMap<>();
                                truckDamageFilter.put(db.KEY_VARIANCE_TYPE, App.TRUCK_DAMAGE);
                                truckDamageFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                                HashMap<String, String> theftFilter = new HashMap<>();
                                theftFilter.put(db.KEY_VARIANCE_TYPE, App.THEFT);
                                theftFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                                HashMap<String, String> excessFilter = new HashMap<>();
                                excessFilter.put(db.KEY_VARIANCE_TYPE, App.EXCESS);
                                excessFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                                //Inventory Exists
                                float cases = 0;
                                float units = 0;
                                float excessCases = 0;
                                float excessUnits = 0;
                                if (db.checkData(db.UNLOAD_VARIANCE, checkInventoryFilter)) {
                                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map1, checkInventoryFilter);
                                    if (c.getCount() > 0) {
                                        c.moveToFirst();
                                        do {
                                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                        }
                                        while (c.moveToNext());
                                    }
                                }
                                if (db.checkData(db.UNLOAD_VARIANCE, truckDamageFilter)) {
                                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map1, truckDamageFilter);
                                    if (c.getCount() > 0) {
                                        c.moveToFirst();
                                        do {
                                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                        }
                                        while (c.moveToNext());
                                    }
                                }
                                if (db.checkData(db.UNLOAD_VARIANCE, theftFilter)) {
                                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map1, theftFilter);
                                    if (c.getCount() > 0) {
                                        c.moveToFirst();
                                        do {
                                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                        }
                                        while (c.moveToNext());
                                    }
                                }
                                if (db.checkData(db.UNLOAD_VARIANCE, excessFilter)) {
                                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map1, excessFilter);
                                    if (c.getCount() > 0) {
                                        c.moveToFirst();
                                        do {
                                            excessCases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                            excessUnits += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                        }
                                        while (c.moveToNext());
                                    }
                                }
                                float finalCases = Float.parseFloat(unload.getCases()) - cases + (excessCases * -1);
                                float finalUnits = Float.parseFloat(unload.getPic()) - units + (excessUnits * -1);
                                unload.setCases(String.valueOf(finalCases));
                                unload.setPic(String.valueOf(finalUnits));
                                vanData.remove(i);
                                vanData.add(i, unload);
                            }
                            // arrayList = vanData;
                            dataStoreList = vanData;
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            try{
                for (int i = 0; i < dataStoreList.size(); i++) {
                    Unload unload = new Unload();
                    if (dataStoreList.get(i).getMaterial_no().equals(arrayList.get(i).getMaterial_no())) {
                        unload.setName(dataStoreList.get(i).getName());
                        unload.setItem_code(dataStoreList.get(i).getItem_code());
                        unload.setMaterial_no(dataStoreList.get(i).getMaterial_no());
                        unload.setUom(dataStoreList.get(i).getUom());
                        unload.setPrice(dataStoreList.get(i).getPrice());
                        float casesTotal = 0;
                        float unitsTotal = 0;
                        //casesTotal = Float.parseFloat(dataStoreList.get(i).getCases()) - Float.parseFloat(arrayList.get(i).getCases());
                        casesTotal = Float.parseFloat(arrayList.get(i).getCases());
                        // unitsTotal = Float.parseFloat(dataStoreList.get(i).getPic()) - Float.parseFloat(arrayList.get(i).getPic());
                        unitsTotal = Float.parseFloat(arrayList.get(i).getPic());
                        unload.setCases(String.valueOf(casesTotal));
                        unload.setPic(String.valueOf(unitsTotal));
                        unload.setReasonCode(arrayList.get(i).getReasonCode());
                        varianceList.add(unload);
                    }
                }
                for (Unload unload : varianceList) {
                    HashMap<String, String> map = new HashMap<>();
                    HashMap<String, String> filter = new HashMap<>();
                    map.put(db.KEY_TIME_STAMP, "");
                    if (context.equals("badreturn")) {
                        map.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                    } else if (context.equals("freshunload")) {
                        if (unload.getReasonCode().equals("") || unload.getReasonCode() == null) {
                        } else {
                            if (unload.getReasonCode().equals("1")) {
                                map.put(db.KEY_VARIANCE_TYPE, App.ENDING_INVENTORY);
                                filter.put(db.KEY_VARIANCE_TYPE, App.ENDING_INVENTORY);
                            } else if (unload.getReasonCode().equals("2")) {
                                map.put(db.KEY_VARIANCE_TYPE, App.TRUCK_DAMAGE);
                                filter.put(db.KEY_VARIANCE_TYPE, App.TRUCK_DAMAGE);
                            } else if (unload.getReasonCode().equals("3")) {
                                map.put(db.KEY_VARIANCE_TYPE, App.THEFT);
                                filter.put(db.KEY_VARIANCE_TYPE, App.THEFT);
                            } else if (unload.getReasonCode().equals("4")) {
                                map.put(db.KEY_VARIANCE_TYPE, App.EXCESS);
                                filter.put(db.KEY_VARIANCE_TYPE, App.EXCESS);
                            }
                        }
                    }
                    map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    map.put(db.KEY_ITEM_NO, unload.getItem_code());
                    map.put(db.KEY_MATERIAL_DESC1, unload.getName());
                    map.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                    map.put(db.KEY_MATERIAL_GROUP, "");
                    map.put(db.KEY_CASE, unload.getCases());
                    map.put(db.KEY_UNIT, unload.getPic());
                    map.put(db.KEY_UOM, unload.getUom());
                    map.put(db.KEY_PRICE, unload.getPrice());
                    String OrderId = Helpers.generateNumber(db, ConfigStore.Unload_PR_Type);
                    map.put(db.KEY_ORDER_ID, OrderId);
                    map.put(db.KEY_PURCHASE_NUMBER, OrderId);
                    map.put(db.KEY_REASON_CODE, context.equals("badreturn") ? reasonCode + "," + unload.getReasonCode() : unload.getReasonCode());
                    map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    map.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                    filter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                    if (context.equals("freshunload")) {
                    } else if (context.equals("badreturn")) {
                        filter.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                    }
                    if (context.equals("freshunload")) {
                        if (unload.getReasonCode().equals("") || unload.getReasonCode() == null || unload.getReasonCode().equals("0")) {
                        } else {
                            if (db.checkData(db.UNLOAD_VARIANCE, filter)) {
                                Cursor c = db.getData(db.UNLOAD_VARIANCE, map, filter);
                                float cases = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                float units = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                map.put(db.KEY_CASE, String.valueOf(Float.parseFloat(unload.getCases())));
                                map.put(db.KEY_UNIT, String.valueOf(Float.parseFloat(unload.getPic())));
                                db.updateData(db.UNLOAD_VARIANCE, map, filter);
                            } else {
                                db.addData(db.UNLOAD_VARIANCE, map);
                            }
                        }
                    }
                    else if(context.equals("badreturn")){
                        if (unload.getReasonCode().equals("") || unload.getReasonCode() == null || unload.getReasonCode().equals("0")) {
                        } else {
                            if (db.checkData(db.UNLOAD_VARIANCE, filter)) {
                                Cursor c = db.getData(db.UNLOAD_VARIANCE, map, filter);
                                float cases = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                float units = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                map.put(db.KEY_CASE, String.valueOf(Float.parseFloat(unload.getCases()) + cases));
                                map.put(db.KEY_UNIT, String.valueOf(Float.parseFloat(unload.getPic()) + units));
                                db.updateData(db.UNLOAD_VARIANCE, map, filter);
                            } else {
                                db.addData(db.UNLOAD_VARIANCE, map);
                            }
                        }
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            Intent i = new Intent(UnloadDetailActivity.this, UnloadActivity.class);
            startActivity(i);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private String getTitle(String context) {
        String title = "";
        switch (context) {
            case "badreturn": {
                title = getString(R.string.bad_return);
                break;
            }
            case "badreturnvariance": {
                title = getString(R.string.bad_return_variance);
                break;
            }
            case "freshunload": {
                title = getString(R.string.fresh_unload);
                break;
            }
            case "endinginventory": {
                title = getString(R.string.ending_inventory);
                break;
            }
            case "inventoryvariance": {
                title = getString(R.string.inventory_variance);
                break;
            }
            case "truckdamage": {
                title = getString(R.string.truck_damage);
                break;
            }
        }
        return title;
    }
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) UnloadDetailActivity.this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                UnloadDetailActivity.this.getCurrentFocus().getWindowToken(), 0);
    }
    public class loadData extends AsyncTask<Void, Void, Void> {
        private String context;
        private loadData(String context) {
            this.context = context;
            execute();
        }
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try{
                if (context.equals("badreturn")) {
                    for (int i = 0; i < articles.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_MATERIAL_DESC1, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_MATERIAL_GROUP, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_UOM, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                        filter.put(db.KEY_REASON_CODE,reasonCode);
                        filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        Cursor cursor = db.getData(db.RETURNS, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            setData(cursor, context);
                        }
                    }
                }
                if (context.equals("badreturnvariance")) {
                    for (int i = 0; i < articles.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_MATERIAL_DESC1, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_MATERIAL_GROUP, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_UOM, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                        filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        Cursor cursor = db.getData(db.UNLOAD_VARIANCE, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            setData(cursor, context);
                        }
                    }
                }
                if (context.equals("freshunload")) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_ITEM_NO, "");
                    map.put(db.KEY_ITEM_CATEGORY, "");
                    map.put(db.KEY_CREATED_BY, "");
                    map.put(db.KEY_ENTRY_TIME, "");
                    map.put(db.KEY_DATE, "");
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_MATERIAL_DESC1, "");
                    map.put(db.KEY_MATERIAL_ENTERED, "");
                    map.put(db.KEY_MATERIAL_GROUP, "");
                    map.put(db.KEY_PLANT, "");
                    map.put(db.KEY_STORAGE_LOCATION, "");
                    map.put(db.KEY_BATCH, "");
                    map.put(db.KEY_ACTUAL_QTY_CASE, "");
                    map.put(db.KEY_ACTUAL_QTY_UNIT, "");
                    map.put(db.KEY_RESERVED_QTY_CASE, "");
                    map.put(db.KEY_RESERVED_QTY_UNIT, "");
                    map.put(db.KEY_REMAINING_QTY_CASE, "");
                    map.put(db.KEY_REMAINING_QTY_UNIT, "");
                    map.put(db.KEY_UOM_CASE, "");
                    map.put(db.KEY_UOM_UNIT, "");
                    map.put(db.KEY_DIST_CHANNEL, "");
                    HashMap<String, String> filter = new HashMap<>();
                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        setData(cursor, context);
                    }
                }
                if (context.equals("endinginventory")) {
                    for (int i = 0; i < articles.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_MATERIAL_DESC1, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_MATERIAL_GROUP, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_UOM, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_VARIANCE_TYPE, App.ENDING_INVENTORY);
                        filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        Cursor cursor = db.getData(db.UNLOAD_VARIANCE, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            setData(cursor, context);
                        }
                    }
                }
                if (context.equals("inventoryvariance")) {
                    for (int i = 0; i < articles.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_REASON_CODE,"");
                        map.put(db.KEY_MATERIAL_DESC1, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_MATERIAL_GROUP, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_UOM, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_VARIANCE_TYPE, App.THEFT);
                        filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        Cursor cursor = db.getData(db.UNLOAD_VARIANCE, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            setData(cursor, context);
                        }
                    }
                    for (int i = 0; i < articles.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_REASON_CODE,"");
                        map.put(db.KEY_MATERIAL_DESC1, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_MATERIAL_GROUP, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_UOM, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_VARIANCE_TYPE, App.EXCESS);
                        filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        Cursor cursor = db.getData(db.UNLOAD_VARIANCE, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            setData(cursor, context);
                        }
                    }
                }
                if (context.equals("truckdamage")) {
                    for (int i = 0; i < articles.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_MATERIAL_DESC1, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_MATERIAL_GROUP, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_UOM, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_VARIANCE_TYPE, App.TRUCK_DAMAGE);
                        filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        Cursor cursor = db.getData(db.UNLOAD_VARIANCE, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            setData(cursor, context);
                        }
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                if (loadingSpinner.isShowing()) {
                    loadingSpinner.hide();
                }
                if (context.equals("freshunload")) {
//                    recalculateFreshUnload(arrayList);
                }
                if(context.equals("badreturn")){
                    recalculateBadReturn(arrayList);
                }
                // dataStoreList = arrayList;
                adapter.notifyDataSetChanged();
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }

    private void unModifiedData(String context) {


    }
    private void setData(final Cursor cursor, final String context) {
        final Cursor c = cursor;
                try{
                    if (context.equals("badreturn")) {
                        c.moveToFirst();
                        Unload unload = new Unload();
                        unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                        unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                        unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        HashMap<String, String> filter1 = new HashMap<>();
                        filter1.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter1);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                                unload.setIsAltUOM(false);
                            } else {
                                unload.setIsAltUOM(true);
                            }
                        } else {
                            unload.setIsAltUOM(false);
                        }
                        HashMap<String, String> priceMap = new HashMap<>();
                        priceMap.put(db.KEY_AMOUNT, "");
                        HashMap<String, String> filterPrice = new HashMap<>();
                        filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filterPrice.put(db.KEY_PRIORITY, "2");
                        Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            unload.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        } else {
                            unload.setPrice("0");
                        }
                        float casesTotal = 0;
                        float unitsTotal = 0;
                        do {
                            casesTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            unitsTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                        unload.setCases(String.valueOf(casesTotal));
                        unload.setPic(String.valueOf(unitsTotal));
                        Helpers.logData(UnloadDetailActivity.this, "Setting Bad Return" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                        arrayList.add(unload);
                    } else if (context.equals("badreturnvariance")) {
                        c.moveToFirst();
                        Unload unload = new Unload();
                        unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                        unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                        unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        HashMap<String, String> filter1 = new HashMap<>();
                        filter1.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter1);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                                unload.setIsAltUOM(false);
                            } else {
                                unload.setIsAltUOM(true);
                            }
                        } else {
                            unload.setIsAltUOM(false);
                        }
                        HashMap<String, String> priceMap = new HashMap<>();
                        priceMap.put(db.KEY_AMOUNT, "");
                        HashMap<String, String> filterPrice = new HashMap<>();
                        filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filterPrice.put(db.KEY_PRIORITY, "2");
                        Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            unload.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        } else {
                            unload.setPrice("0");
                        }
                        float casesTotal = 0;
                        float unitsTotal = 0;
                        do {
                            casesTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            unitsTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                        unload.setCases(String.valueOf(casesTotal));
                        unload.setPic(String.valueOf(unitsTotal));
                        Helpers.logData(UnloadDetailActivity.this, "Setting Bad Return Variance" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                        arrayList.add(unload);
                    } else if (context.equals("freshunload")) {
                        do {
                            Unload unload = new Unload();
                            unload.setIsCheck("false");
                            unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                            unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                            unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                            String uomCase = c.getString(c.getColumnIndex(db.KEY_UOM_CASE));
                            String uomUnit = c.getString(c.getColumnIndex(db.KEY_UOM_UNIT));
                            ArticleHeader articleHeader = ArticleHeader.getArticle(articles, unload.getMaterial_no());
                            //unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM_CASE)).equals(App.CASE_UOM)?App.CASE_UOM:App.BOTTLES_UOM);
                            try {
                                unload.setUom(articleHeader.getBaseUOM());
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            HashMap<String, String> altMap = new HashMap<>();
                            altMap.put(db.KEY_UOM, "");
                            HashMap<String, String> filter1 = new HashMap<>();
                            filter1.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter1);
                            if (altUOMCursor.getCount() > 0) {
                                altUOMCursor.moveToFirst();
                                if (articleHeader.getBaseUOM().equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                                    unload.setIsAltUOM(false);
                                } else {
                                    unload.setIsAltUOM(true);
                                }
                            } else {
                                unload.setIsAltUOM(false);
                            }
                            HashMap<String, String> priceMap = new HashMap<>();
                            priceMap.put(db.KEY_AMOUNT, "");
                            HashMap<String, String> filterPrice = new HashMap<>();
                            filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            filterPrice.put(db.KEY_PRIORITY, "2");
                            Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                            if (priceCursor.getCount() > 0) {
                                priceCursor.moveToFirst();
                                unload.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                            } else {
                                unload.setPrice("0");
                            }
                            double cases = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_RESERVED_QTY_CASE))) + Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                            double units = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_RESERVED_QTY_UNIT))) + Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                            //unload.setCases(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                            //unload.setPic(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                            unload.setCases("0");
//                            unload.setCases(String.valueOf(cases));
                            unload.setPic(String.valueOf(units));
                            unload.setReasonCode("0");
                            Helpers.logData(UnloadDetailActivity.this, "Setting Fresh Unload" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                            arrayList.add(unload);
                        }
                        while (c.moveToNext());
                    } else if (context.equals("endinginventory")) {
                        c.moveToFirst();
                        Unload unload = new Unload();
                        unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                        unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                        unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        HashMap<String, String> filter1 = new HashMap<>();
                        Log.v("materialno",cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filter1.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter1);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                                unload.setIsAltUOM(false);
                            } else {
                                unload.setIsAltUOM(true);
                            }
                        } else {
                            unload.setIsAltUOM(false);
                        }
                        HashMap<String, String> priceMap = new HashMap<>();
                        priceMap.put(db.KEY_AMOUNT, "");
                        HashMap<String, String> filterPrice = new HashMap<>();
                        filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filterPrice.put(db.KEY_PRIORITY, "2");
                        Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            unload.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        } else {
                            unload.setPrice("0");
                        }
                        float casesTotal = 0;
                        float unitsTotal = 0;
                        do {
                            casesTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            unitsTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                        unload.setCases("0");
//                        unload.setCases(String.valueOf(casesTotal));
                        unload.setReasonCode("1");
                        unload.setPic(String.valueOf(unitsTotal));
                        Helpers.logData(UnloadDetailActivity.this, "Setting Ending Inventory Data" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                        arrayList.add(unload);
                    } else if (context.equals("inventoryvariance")) {
                        c.moveToFirst();
                        Unload unload = new Unload();
                        unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                        unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                        unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));
                        unload.setReasonCode(c.getString(c.getColumnIndex(db.KEY_REASON_CODE)));
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        HashMap<String, String> filter1 = new HashMap<>();
                        filter1.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter1);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                                unload.setIsAltUOM(false);
                            } else {
                                unload.setIsAltUOM(true);
                            }
                        } else {
                            unload.setIsAltUOM(false);
                        }
                        HashMap<String, String> priceMap = new HashMap<>();
                        priceMap.put(db.KEY_AMOUNT, "");
                        HashMap<String, String> filterPrice = new HashMap<>();
                        filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filterPrice.put(db.KEY_PRIORITY, "2");
                        Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            unload.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        } else {
                            unload.setPrice("0");
                        }
                        float casesTotal = 0;
                        float unitsTotal = 0;
                        do {
                            casesTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            unitsTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                        unload.setCases(String.valueOf(casesTotal));
                        unload.setPic(String.valueOf(unitsTotal));
                        Helpers.logData(UnloadDetailActivity.this, "Setting Inventory Variance" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                        arrayList.add(unload);
                    } else if (context.equals("truckdamage")) {
                        c.moveToFirst();
                        Unload unload = new Unload();
                        unload.setName(UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                        unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                        unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        HashMap<String, String> filter1 = new HashMap<>();
                        filter1.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter1);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                                unload.setIsAltUOM(false);
                            } else {
                                unload.setIsAltUOM(true);
                            }
                        } else {
                            unload.setIsAltUOM(false);
                        }
                        HashMap<String, String> priceMap = new HashMap<>();
                        priceMap.put(db.KEY_AMOUNT, "");
                        HashMap<String, String> filterPrice = new HashMap<>();
                        filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filterPrice.put(db.KEY_PRIORITY, "2");
                        Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            unload.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        } else {
                            unload.setPrice("0");
                        }
                        float casesTotal = 0;
                        float unitsTotal = 0;
                        do {
                            casesTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            unitsTotal += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                        unload.setCases(String.valueOf(casesTotal));
                        unload.setPic(String.valueOf(unitsTotal));
                        Helpers.logData(UnloadDetailActivity.this, "Setting Truck Damage" + unload.getName() + "-" + unload.getCases() + "-" + unload.getPic());
                        arrayList.add(unload);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

    }

    private void recalculateData(ArrayList<Unload> data) {


    }
    private void recalculateFreshUnload(ArrayList<Unload> data) {
        try{
            ArrayList<Unload> vanData = data;
            for (int i = 0; i < vanData.size(); i++) {
                Unload unload = vanData.get(i);
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_CASE, "");
                map.put(db.KEY_UNIT, "");
                HashMap<String, String> checkInventoryFilter = new HashMap<>();
                checkInventoryFilter.put(db.KEY_VARIANCE_TYPE, App.ENDING_INVENTORY);
                checkInventoryFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                HashMap<String, String> truckDamageFilter = new HashMap<>();
                truckDamageFilter.put(db.KEY_VARIANCE_TYPE, App.TRUCK_DAMAGE);
                truckDamageFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                HashMap<String, String> theftFilter = new HashMap<>();
                theftFilter.put(db.KEY_VARIANCE_TYPE, App.THEFT);
                theftFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                HashMap<String, String> excessFilter = new HashMap<>();
                excessFilter.put(db.KEY_VARIANCE_TYPE, App.EXCESS);
                excessFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                //Inventory Exists
                float cases = 0;
                float units = 0;
                float excessCases = 0;
                float excessUnits = 0;
                if (db.checkData(db.UNLOAD_VARIANCE, checkInventoryFilter)) {
                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map, checkInventoryFilter);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                if (db.checkData(db.UNLOAD_VARIANCE, truckDamageFilter)) {
                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map, truckDamageFilter);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                if (db.checkData(db.UNLOAD_VARIANCE, theftFilter)) {
                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map, theftFilter);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                if (db.checkData(db.UNLOAD_VARIANCE, excessFilter)) {
                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map, excessFilter);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            excessCases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                            excessUnits += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        while (c.moveToNext());
                    }
                }
                float finalCases = Float.parseFloat(unload.getCases()) - cases + (excessCases * -1);
                float finalUnits = Float.parseFloat(unload.getPic()) - units + (excessUnits * -1);
                unload.setCases(String.valueOf(finalCases));
                unload.setPic(String.valueOf(finalUnits));
                vanData.remove(i);
                vanData.add(i, unload);
            }
            arrayList = vanData;
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void recalculateBadReturn(ArrayList<Unload> data) {
        try{
            ArrayList<Unload> vanData = data;
            for (int i = 0; i < vanData.size(); i++) {
                Unload unload = vanData.get(i);
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_CASE, "");
                map.put(db.KEY_UNIT, "");
                map.put(db.KEY_REASON_CODE,"");
                HashMap<String, String> checkInventoryFilter = new HashMap<>();
                checkInventoryFilter.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                checkInventoryFilter.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                //Inventory Exists
                float cases = 0;
                float units = 0;
                String reasonCd = "";
                if (db.checkData(db.UNLOAD_VARIANCE, checkInventoryFilter)) {
                    Cursor c = db.getData(db.UNLOAD_VARIANCE, map, checkInventoryFilter);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        reasonCd = UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_REASON_CODE)));
                        String[]tokens = reasonCd.split("\\,");
                        if(reasonCode.equals(tokens[0])){
                            do {
                                cases += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                units += Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                            }
                            while (c.moveToNext());
                        }

                    }
                }
                float finalCases = Float.parseFloat(unload.getCases()) - cases;
                float finalUnits = Float.parseFloat(unload.getPic()) - units;
                unload.setCases(String.valueOf(finalCases));
                unload.setPic(String.valueOf(finalUnits));
                vanData.remove(i);
                vanData.add(i, unload);
            }
            arrayList = vanData;
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        // adapter.notifyDataSetChanged();
        //dataStoreList = vanData;
    }

    private void captureVariance() {



    }
    private String getVarianceType(String context) {
        String varianceType = "";
        switch (context) {
            case "badreturn": {
                varianceType = App.BAD_RETURN_VARIANCE;
            }
            case "badreturnvariance": {
                varianceType = App.BAD_RETURN_VARIANCE;
            }
            case "endinginventory": {
                varianceType = App.ENDING_INVENTORY;
            }
            case "inventoryvariance": {
                varianceType = App.INVENTORY_VARIANCE;
            }
            case "truckdamage": {
                varianceType = App.TRUCK_DAMAGE;
            }
        }
        return varianceType;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.remove:
                // add stuff here
                showReasonDialog(arrayList, info.position);
                return true;
            case R.id.cancel:
                // edit stuff here
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }
    private void showReasonDialog(ArrayList<Unload> list, final int position){
        final int pos = position;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(UnloadDetailActivity.this);
        builderSingle.setTitle(getString(R.string.select_reason));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(UnloadDetailActivity.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Lost Product");
        arrayAdapter.add("Other Reasons");
        builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    HashMap<String, String> map = new HashMap<String, String>();
                    if (context.equals("inventoryvariance")) {
                        if (arrayList.get(pos).getReasonCode().equals("3")) {
                            map.put(db.KEY_VARIANCE_TYPE, App.THEFT);
                        } else {
                            map.put(db.KEY_VARIANCE_TYPE, App.EXCESS);
                        }
                    } else if (context.equals("badreturnvariance")) {
                        map.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                    } else {
                        if (context.equals("endinginventory")) {
                            map.put(db.KEY_VARIANCE_TYPE, App.ENDING_INVENTORY);
                        }
                        if (context.equals("truckdamage")) {
                            map.put(db.KEY_VARIANCE_TYPE, App.TRUCK_DAMAGE);
                        }
                    }
                    map.put(db.KEY_MATERIAL_NO, arrayList.get(pos).getMaterial_no());
                    db.deleteData(db.UNLOAD_VARIANCE, map);
                    arrayList.remove(pos);
                    adapter.notifyDataSetChanged();
                    if (arrayList.size() == 0) {
                        //finish();
                        Intent i = new Intent(UnloadDetailActivity.this, UnloadActivity.class);
                        startActivity(i);
                    }
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

               /* String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(DeliveryOrderActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();*/
            }
        });
        builderSingle.show();


    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
        Intent i = new Intent(UnloadDetailActivity.this, UnloadActivity.class);
        startActivity(i);
        //finish();
    }
}
