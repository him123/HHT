package com.ae.benchmark.activities;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.SalesInvoiceAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.sap.IntegrationService;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
/************************************************************
 @ This activity is used to view details of the Sales invoice
 @ in terms of sales, good returns and bad returns. Free of Cost
 @ (FOC) is currently not implemented in the application
 ************************************************************/
public class InvoiceSummeryActivity extends AppCompatActivity {
    ImageView iv_back;
    TextView tv_top_header;
    Customer object;
    LoadingSpinner loadingSpinner;
    DatabaseHandler db;
    int orderTotalValue = 0;
    ArrayList<ArticleHeader> articles;
    ArrayList<Sales> arraylist = new ArrayList<>();
    ArrayList<Sales> salesList = new ArrayList<>();
    ArrayList<Sales> focList = new ArrayList<>();
    ArrayList<Sales> grList = new ArrayList<>();
    ArrayList<Sales> brList = new ArrayList<>();
    EditText et_sales_cases;
    EditText et_sales_units;
    EditText et_sales_amount;
    EditText et_good_cases;
    EditText et_good_units;
    EditText et_good_amount;
    EditText et_bad_cases;
    EditText et_bad_units;
    EditText et_bad_amount;
    EditText et_foc_cases;
    EditText et_foc_units;
    EditText et_foc_amount;
    TextView tv_total_amount;
    EditText tv_sales_expand;
    EditText tv_gr_expand;
    EditText tv_br_expand;
    EditText tv_foc_expand;
    Double totalamnt = 0.0;
    ArrayAdapter<Sales> adapter;
    boolean salesInvoiceExist = false;
    boolean focExist = false;
    boolean grExist = false;
    boolean brExist = false;
    Double salesAmount = 0.0;
    float grAmount = 0;
    float brAmount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_summery);
        db = DatabaseHandler.getInstance(getApplicationContext());
        loadingSpinner = new LoadingSpinner(this);
        Intent i = this.getIntent();
        object = (Customer) i.getParcelableExtra("headerObj");
        articles = ArticleHeaders.get();
        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText(getString(R.string.invoice_summary));
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tv_customer_id = (TextView) findViewById(R.id.tv_customer_id);
        TextView tv_customer_name = (TextView) findViewById(R.id.tv_customer_name);
        tv_customer_id.setText(StringUtils.stripStart(object.getCustomerID(), "0"));
        tv_customer_name.setText(UrlBuilder.decodeString(object.getCustomerName()));
        tv_sales_expand = (EditText) findViewById(R.id.tv_sales_expand);
        tv_gr_expand = (EditText) findViewById(R.id.tv_gr_expand);
        tv_br_expand = (EditText) findViewById(R.id.tv_br_expand);
        tv_foc_expand = (EditText)findViewById(R.id.tv_foc_expand);
        et_sales_cases = (EditText) findViewById(R.id.et_sales_cases);
        et_sales_units = (EditText) findViewById(R.id.et_sales_units);
        et_sales_amount = (EditText) findViewById(R.id.et_sales_amount);
        et_good_cases = (EditText) findViewById(R.id.et_good_cases);
        et_good_units = (EditText) findViewById(R.id.et_good_units);
        et_good_amount = (EditText) findViewById(R.id.et_good_amount);
        et_bad_cases = (EditText) findViewById(R.id.et_bad_cases);
        et_bad_units = (EditText) findViewById(R.id.et_bad_units);
        et_bad_amount = (EditText) findViewById(R.id.et_bad_amount);
        et_foc_cases = (EditText) findViewById(R.id.et_foc_cases);
        et_foc_units = (EditText) findViewById(R.id.et_foc_units);
        et_foc_amount = (EditText) findViewById(R.id.et_foc_amount);
        tv_total_amount = (TextView) findViewById(R.id.tv_total_amount);
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
        map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
        tv_sales_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(App.SALES_INVOICE);
            }
        });
        tv_gr_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(App.GOOD_RETURN);
            }
        });
        tv_br_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(App.BAD_RETURN);
            }
        });
        tv_foc_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(App.FOC);
            }
        });

        try{
            if (db.checkData(db.CAPTURE_SALES_INVOICE, map)) {
                salesInvoiceExist = true;
                new loadData().execute();
            } else {
                HashMap<String, String> gRMap = new HashMap<>();
                gRMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                gRMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                gRMap.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                HashMap<String, String> bRMap = new HashMap<>();
                bRMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                bRMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                bRMap.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                boolean gRexists = false;
                boolean bRexists = false;
                if (db.checkData(db.RETURNS, gRMap)) {
                    gRexists = true;
                    grExist = true;
                }
                if (db.checkData(db.RETURNS, bRMap)) {
                    bRexists = true;
                    brExist = true;
                }
                if (gRexists && bRexists) {
                    new loadReturns(App.GOOD_RETURN);
                    new loadReturns(App.BAD_RETURN);
                } else if (gRexists) {
                    new loadReturns(App.GOOD_RETURN);
                } else if (bRexists) {
                    new loadReturns(App.BAD_RETURN);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        Button btn_complete_invoice = (Button) findViewById(R.id.btn_complete_invoice);
        btn_complete_invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(InvoiceSummeryActivity.this);
                dialog.setContentView(R.layout.activity_print);
                Button print = (Button) dialog.findViewById(R.id.btnPrint);
                print.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new postData().execute();
                        dialog.dismiss();
                    }
                });
                Button donotPrint = (Button) dialog.findViewById(R.id.btnCancel2);
                donotPrint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
    }
    /************************************************************
     @ Loading all the sales data for the customer
     ************************************************************/
    public class loadData extends AsyncTask<Void, Void, Void> {
        float case_sale = 0;
        float unit_sale = 0;
        Double amount = 0.0;
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try{
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                map.put(db.KEY_ITEM_NO, object.getCustomerID());
                map.put(db.KEY_ITEM_CATEGORY, object.getCustomerID());
                map.put(db.KEY_MATERIAL_NO, object.getCustomerID());
                map.put(db.KEY_MATERIAL_DESC1, object.getCustomerID());
                map.put(db.KEY_MATERIAL_GROUP, object.getCustomerID());
                map.put(db.KEY_ORG_CASE, "");
                map.put(db.KEY_ORG_UNITS, "");
                map.put(db.KEY_AMOUNT, "");
                map.put(db.KEY_AMOUNTPCS, "");
                map.put(db.KEY_AMOUNTCASE, "");
                map.put(db.KEY_UOM, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                Cursor cursor = db.getData(db.CAPTURE_SALES_INVOICE, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        case_sale += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        unit_sale += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                        Double tempamount = 0.0;
                        if (case_sale >= 1) {
                            String deno = "0";
                            HashMap<String, String> altMap = new HashMap<>();
                            altMap.put(db.KEY_UOM, "");
                            altMap.put(db.KEY_DENOMINATOR, "");
                            HashMap<String, String> filtera = new HashMap<>();
                            filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                            if (altUOMCursor.getCount() > 0) {
                                altUOMCursor.moveToFirst();
                                deno = ""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                            }

                            Double casePrice = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTCASE)));
                            tempamount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        }
                        if(unit_sale >= 1){
                            tempamount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTPCS))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                        }
                        tempamount = tempamount + tempamount*Double.parseDouble(Settings.getString(App.VATValue));
                        amount= amount+tempamount;
                        Sales sales = new Sales();
                        sales.setMaterial_no(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        sales.setItem_code(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                        ArticleHeader articleHeader = ArticleHeader.getArticle(articles, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        if (articleHeader != null) {
                            sales.setMaterial_description(articleHeader.getMaterialDesc1());
                            sales.setName(UrlBuilder.decodeString(articleHeader.getMaterialDesc1()));
                        } else {
                            sales.setMaterial_description(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                            sales.setName(UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        }
                        sales.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        sales.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        sales.setPic(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                        sales.setPrice(String.format("%.2f",amount));
                        sales.setPricecase(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTCASE)));
                        sales.setPricepcs(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTPCS)));
                        salesList.add(sales);
                    }
                    while (cursor.moveToNext());
                    salesAmount = amount;
                    totalamnt = amount;
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
                et_sales_cases.setText(String.valueOf(case_sale));
                et_sales_units.setText(String.valueOf(unit_sale));
                et_sales_amount.setText(String.format("%.2f",amount));
                tv_total_amount.setText(String.format("%.2f",totalamnt));
                HashMap<String, String> focMap = new HashMap<>();
                focMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                focMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                HashMap<String, String> gRMap = new HashMap<>();
                gRMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                gRMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                gRMap.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                HashMap<String, String> bRMap = new HashMap<>();
                bRMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                bRMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                bRMap.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                boolean gRexists = false;
                boolean bRexists = false;
                if (db.checkData(db.RETURNS, gRMap)) {
                    gRexists = true;
                    grExist = true;
                }
                if (db.checkData(db.RETURNS, bRMap)) {
                    bRexists = true;
                    brExist = true;
                }
                if (db.checkData(db.FOC_INVOICE, focMap)) {
                    focExist = true;
                    new loadFOC().execute();
                }
                if (gRexists && bRexists) {
                    new loadReturns(App.GOOD_RETURN);
                    new loadReturns(App.BAD_RETURN);
                } else if (gRexists) {
                    new loadReturns(App.GOOD_RETURN);
                } else if (bRexists) {
                    new loadReturns(App.BAD_RETURN);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }
    /************************************************************
     @ Loading free of Cost data for the customer
     ************************************************************/
    public class loadFOC extends AsyncTask<Void, Void, Void> {
        float case_foc = 0;
        float unit_foc = 0;
        float amount = 0;
        @Override
        protected Void doInBackground(Void... params) {
            try{
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                map.put(db.KEY_ITEM_NO, object.getCustomerID());
                map.put(db.KEY_ITEM_CATEGORY, object.getCustomerID());
                map.put(db.KEY_MATERIAL_NO, object.getCustomerID());
                map.put(db.KEY_MATERIAL_DESC1, object.getCustomerID());
                map.put(db.KEY_ORG_CASE, "");
                map.put(db.KEY_ORG_UNITS, "");
                map.put(db.KEY_AMOUNT, "");
                map.put(db.KEY_UOM, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                Cursor cursor = db.getData(db.FOC_INVOICE, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        case_foc += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        unit_foc += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));

                        Sales sales = new Sales();
                        sales.setMaterial_no(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        sales.setItem_code(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                        ArticleHeader articleHeader = ArticleHeader.getArticle(articles, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        if (articleHeader != null) {
                            sales.setMaterial_description(articleHeader.getMaterialDesc1());
                            sales.setName(UrlBuilder.decodeString(articleHeader.getMaterialDesc1()));
                        } else {
                            sales.setMaterial_description(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                            sales.setName(UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        }
                        sales.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        sales.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        sales.setPic(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                        sales.setPrice(String.valueOf(Float.parseFloat("0")));
                        focList.add(sales);
                    }
                    while (cursor.moveToNext());
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
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            Log.e("FOC C/S","" + case_foc);
            Log.e("FOC B/T","" + unit_foc);
            et_foc_cases.setText(String.valueOf(case_foc));
            et_foc_units.setText(String.valueOf(unit_foc));
            et_foc_amount.setText("N/A");
        }
    }
    /************************************************************
     @ Loading good returns and bad returns data for the customer
     ************************************************************/
    public class loadReturns extends AsyncTask<Void, Void, Void> {
        float case_sale = 0;
        float unit_sale = 0;
        Double amount = 0.0;
        String returnType;
        private loadReturns(String returnType) {
            this.returnType = returnType;
            execute();
        }
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try{
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_ITEM_NO, "");
                map.put(db.KEY_MATERIAL_DESC1, "");
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_MATERIAL_GROUP, "");
                map.put(db.KEY_CASE, "");
                map.put(db.KEY_UNIT, "");
                map.put(db.KEY_PRICE, "");
                map.put(db.KEY_PRICEPCS, "");
                map.put(db.KEY_PRICECASE, "");
                map.put(db.KEY_UOM, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                filter.put(db.KEY_REASON_TYPE, returnType);
                Cursor cursor = db.getData(db.RETURNS, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        case_sale += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        unit_sale += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        /*if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                            amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICE))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                            //amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        } else {
                            amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICE))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        }*/
                        Double tempamount = 0.0;
                        if (case_sale >= 1) {
                            String deno = "0";
                            HashMap<String, String> altMap = new HashMap<>();
                            altMap.put(db.KEY_UOM, "");
                            altMap.put(db.KEY_DENOMINATOR, "");
                            HashMap<String, String> filtera = new HashMap<>();
                            filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                            if (altUOMCursor.getCount() > 0) {
                                altUOMCursor.moveToFirst();
                                deno = ""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                            }

                            Double casePrice = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE)));
                            tempamount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        }
                        if(unit_sale >= 1){
                            tempamount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        }
                        tempamount = tempamount +tempamount*Double.parseDouble(Settings.getString(App.VATValue));
                        amount= amount+tempamount;
                        Sales sales = new Sales();
                        sales.setItem_code(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                        sales.setMaterial_no(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        sales.setMaterial_description(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        sales.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        sales.setName(UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        sales.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        sales.setPic(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        sales.setPrice(String.format("%.2f",amount));
                        sales.setPricecase(cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE)));
                        sales.setPricepcs(cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS)));
                        if (returnType.equals(App.GOOD_RETURN)) {
                            grList.add(sales);
                            Log.e("Price", "" + grList.get(0).getPrice());
                        }
                        if (returnType.equals(App.BAD_RETURN)) {
                            brList.add(sales);
                        }
                    }
                    while (cursor.moveToNext());

                    totalamnt = totalamnt - amount;
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
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            if (returnType.equals(App.GOOD_RETURN)) {
                et_good_cases.setText(String.valueOf(case_sale));
                et_good_units.setText(String.valueOf(unit_sale));
                et_good_amount.setText(String.format( "%.2f",amount));
            } else if (returnType.equals(App.BAD_RETURN)) {
                et_bad_cases.setText(String.valueOf(case_sale));
                et_bad_units.setText(String.valueOf(unit_sale));
                et_bad_amount.setText(String.format("%.2f",amount));
            }
            tv_total_amount.setText(String.format( "%.2f",totalamnt));
        }
    }
    /************************************************************
     @ Not used
     ************************************************************/
    public class postData extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> returnList;
        private String orderID = "";
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            //this.returnList = IntegrationService.RequestToken(LoadRequestActivity.this);
            this.orderID = postData();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            //Log.e("Order ID", "" + this.orderID);
            try{
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_CUSTOMER_NO, "");
                map.put(db.KEY_ITEM_NO, "");
                map.put(db.KEY_ITEM_CATEGORY, "");
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_MATERIAL_GROUP, "");
                map.put(db.KEY_MATERIAL_DESC1, "");
                map.put(db.KEY_ORG_CASE, "");
                map.put(db.KEY_UOM, "");
                map.put(db.KEY_ORG_UNITS, "");
                map.put(db.KEY_AMOUNT, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_IS_POSTED, "N");
                Cursor cursor = db.getData(db.CAPTURE_SALES_INVOICE, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        Sales sale = new Sales();
                        sale.setMaterial_no(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        sale.setPic(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                        sale.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        sale.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        arraylist.add(sale);
                    }
                    while (cursor.moveToNext());
                }
                for (Sales sale : arraylist) {
                    HashMap<String, String> postmap = new HashMap<String, String>();
                    postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    postmap.put(db.KEY_IS_POSTED, "Y");
                    HashMap<String, String> filtermap = new HashMap<>();
                    filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    filtermap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                    db.updateData(db.CAPTURE_SALES_INVOICE, map, filter);
                }
                if (loadingSpinner.isShowing()) {
                    loadingSpinner.hide();
                }
                if (this.orderID.isEmpty() || this.orderID.equals("") || this.orderID == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(InvoiceSummeryActivity.this);
                    alertDialogBuilder.setTitle("Message")
                            .setMessage("Request " + this.orderID + " has been created")
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateStockinVan();
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }
    /************************************************************
     @ Not used
     ************************************************************/
    public String postData() {
        String orderID = "";
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("Function", ConfigStore.InvoiceRequestFunction);
            map.put("OrderId", "");
            map.put("DocumentType", ConfigStore.DocumentType);
            // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
            // map.put("DocumentDate", null);
            map.put("CustomerId", object.getCustomerID());
            map.put("SalesOrg", Settings.getString(App.SALES_ORG));
            map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
            map.put("Division", Settings.getString(App.DIVISION));
            map.put("OrderValue", String.format( "%.2f",orderTotalValue));
            map.put("Currency", App.CURRENCY);
            map.put("PurchaseNum", Helpers.generateNumber(db, ConfigStore.InvoiceRequest_PR_Type));
            JSONArray deepEntity = new JSONArray();
            HashMap<String, String> itemMap = new HashMap<>();
            itemMap.put(db.KEY_ITEM_NO, "");
            itemMap.put(db.KEY_MATERIAL_NO, "");
            itemMap.put(db.KEY_MATERIAL_DESC1, "");
            itemMap.put(db.KEY_ORG_CASE, "");
            itemMap.put(db.KEY_ORG_UNITS, "");
            itemMap.put(db.KEY_UOM, "");
            itemMap.put(db.KEY_AMOUNT, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, "N");
            Cursor cursor = db.getData(db.CAPTURE_SALES_INVOICE, itemMap, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int itemno = 10;
                do {
                    if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM)) {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("UoM", App.CASE_UOM);
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("Storagelocation", "");
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    }
                    if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("UoM", App.BOTTLES_UOM);
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("Storagelocation", "");
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    }
                }
                while (cursor.moveToNext());
            }
            orderID = IntegrationService.postData(InvoiceSummeryActivity.this, App.POST_COLLECTION, map, deepEntity);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return orderID;
    }
    /************************************************************
     @ Not used
     ************************************************************/
    public void updateStockinVan() {
        try{
            loadingSpinner.show();
            //Log.e("ArrayList Size", "" + arraylist.size());
            for (Sales sale : arraylist) {
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_REMAINING_QTY_CASE, "");
                map.put(db.KEY_REMAINING_QTY_UNIT, "");
                HashMap<String, String> filter = new HashMap<>();
                //Log.e("Filter MN", "" + sale.getMaterial_no());
                filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                //Log.e("Cursor count", "" + cursor.getCount());
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                }
                do {
                    HashMap<String, String> updateDataMap = new HashMap<>();
                    float remainingCase = 0;
                    float remainingUnit = 0;
                    remainingCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                    remainingUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                    //Log.e("RemainingCs", "" + remainingCase + sale.getCases());
                    //Log.e("RemainingPc", "" + remainingUnit + sale.getPic());
                    if (!(sale.getCases().isEmpty() || sale.getCases().equals("") || sale.getCases() == null || sale.getCases().equals("0"))) {
                        remainingCase = remainingCase - Float.parseFloat(sale.getCases());
                    }
                    if (!(sale.getPic().isEmpty() || sale.getPic().equals("") || sale.getPic() == null || sale.getPic().equals("0"))) {
                        remainingUnit = remainingUnit - Float.parseFloat(sale.getPic());
                    }
                    updateDataMap.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(remainingCase));
                    updateDataMap.put(db.KEY_REMAINING_QTY_UNIT, String.valueOf(remainingUnit));
                    HashMap<String, String> filterInter = new HashMap<>();
                    filterInter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    db.updateData(db.VAN_STOCK_ITEMS, updateDataMap, filterInter);
                }
                while (cursor.moveToNext());
            }
            loadingSpinner.hide();
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    /************************************************************
     @ Not used
     ************************************************************/
    public void showDialog(String type) {
        try{
            final Dialog dialog = new Dialog(InvoiceSummeryActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //dialog.setTitle(getString(R.string.shop_status));
            View view = getLayoutInflater().inflate(R.layout.activity_select_customer_status, null);
            dialog.setContentView(view);
            dialog.setCancelable(false);
            TextView tv = (TextView) view.findViewById(R.id.tv_top_header);
            ListView lv = (ListView) view.findViewById(R.id.statusList);
            Button cancel = (Button) view.findViewById(R.id.btnCancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            if (type.equals(App.SALES_INVOICE)) {
                if (salesInvoiceExist) {
                    tv.setText("Total Amount - " + String.format("%.2f",salesAmount));
                    adapter = new SalesInvoiceAdapter(InvoiceSummeryActivity.this, salesList,"Invoice");
                    lv.setAdapter(adapter);
                    dialog.show();
                }
            }
            else if (type.equals(App.FOC)) {
                if (focExist) {
                    tv.setText("Total Amount - n/A");
                    adapter = new SalesInvoiceAdapter(InvoiceSummeryActivity.this, focList,"Invoice");
                    lv.setAdapter(adapter);
                    dialog.show();
                }
            }
            else if (type.equals(App.GOOD_RETURN)) {
                if (grExist) {
                    tv.setText("Total Amount - " + et_good_amount.getText().toString());
                    adapter = new SalesInvoiceAdapter(InvoiceSummeryActivity.this, grList,"Invoice");
                    adapter.notifyDataSetChanged();
                    lv.setAdapter(adapter);
                    dialog.show();
                }
            } else if (type.equals(App.BAD_RETURN)) {
                if (brExist) {
                    tv.setText("Total Amount - " + et_bad_amount.getText().toString());
                    adapter = new SalesInvoiceAdapter(InvoiceSummeryActivity.this, brList,"Invoice");
                    adapter.notifyDataSetChanged();
                    lv.setAdapter(adapter);
                    dialog.show();
                }
            }
            //lv.setAdapter(statusAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //dialog.dismiss();
                }
            });
        /*dialog.show();*/
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
}
