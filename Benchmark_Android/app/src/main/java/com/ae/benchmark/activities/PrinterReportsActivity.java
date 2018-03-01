package com.ae.benchmark.activities;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.models.UnloadSummaryPrint;
import com.ae.benchmark.utils.ConfigStore;
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
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.data.DriverRouteFlags;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.DamageReport;
import com.ae.benchmark.models.DepositReport;
import com.ae.benchmark.models.SalesSummary;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.PrinterHelper;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

public class PrinterReportsActivity extends AppCompatActivity {
    ImageView iv_back;
    TextView tv_top_header;
    Button btn_print;
    App.DriverRouteControl flag = new App.DriverRouteControl();
    CheckBox cb_deposit_report;
    CheckBox cb_sales_summary;
    CheckBox cb_damaged_reports;
    CheckBox cb_end_reports;
    ArrayList<String> printReports = new ArrayList<>();
    ArrayList<CustomerHeader> customers = new ArrayList<>();
    ArrayList<ArticleHeader> articles = new ArrayList<>();
    ArrayList<DepositReport> depositReports = new ArrayList<>();
    ArrayList<SalesSummary> cashSales = new ArrayList<>();
    ArrayList<SalesSummary> tcSales = new ArrayList<>();
    ArrayList<SalesSummary> creditSales = new ArrayList<>();
    ArrayList<DamageReport> damageReports = new ArrayList<>();
    private ArrayList<UnloadSummaryPrint> printUnloadList = new ArrayList<>();
    DatabaseHandler db = new DatabaseHandler(this);
    LoadingSpinner loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_reports);
        flag = DriverRouteFlags.get();
        customers = CustomerHeaders.get();
        articles = ArticleHeaders.get();
        loadingSpinner = new LoadingSpinner(this);
        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        btn_print = (Button) findViewById(R.id.btn_print_printer_report);
        cb_deposit_report = (CheckBox) findViewById(R.id.cb_deposit_report);
        cb_sales_summary = (CheckBox) findViewById(R.id.cb_sales_summary);
        cb_damaged_reports = (CheckBox) findViewById(R.id.cb_damaged_reports);
        cb_end_reports = (CheckBox) findViewById(R.id.cb_end_reports);
        cb_deposit_report.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    printReports.add(App.DEPOSIT_REPORT);
                } else {
                    printReports.remove(App.DEPOSIT_REPORT);
                }
            }
        });
        cb_sales_summary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    printReports.add(App.SALES_SUMMARY);
                } else {
                    printReports.remove(App.SALES_SUMMARY);
                }
            }
        });
        cb_damaged_reports.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    printReports.add(App.BAD_RETURN_REPORT);
                } else {
                    printReports.remove(App.BAD_RETURN_REPORT);
                }
            }
        });
        cb_end_reports.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    printReports.add(App.ENDING_SUMMERY);
                } else {
                    printReports.remove(App.ENDING_SUMMERY);
                }
            }
        });
        if (!(flag == null)) {
            if (!flag.isEodSalesReports()) {
                btn_print.setAlpha(0.5f);
                btn_print.setEnabled(false);
            }
        }
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (printReports.size() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_select_report), Toast.LENGTH_SHORT).show();
                } else if (printReports.size() > 1) {
                    Toast.makeText(getApplicationContext(), getString(R.string.oneatattime), Toast.LENGTH_SHORT).show();
                } else {
                    final Dialog dialog = new Dialog(PrinterReportsActivity.this);
                    dialog.setContentView(R.layout.dialog_doprint);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
                    LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);
                    btn_print.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new loadReports(printReports.get(0));
                            dialog.dismiss();
                            /*Intent intent = new Intent(PrinterReportsActivity.this, DashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();*/
                        }
                    });
                    btn_notprint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            /*Intent intent = new Intent(PrinterReportsActivity.this, DashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();*/
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                }


            }
        });
        if (getIntent().getExtras() != null) {
            String from = getIntent().getStringExtra("from");
            if (from.equals("customer")) {
                tv_top_header.setText("Print");
            } else {
                tv_top_header.setText("Print Reports");
            }
        } else {
            tv_top_header.setText("End Trip");
        }
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrinterReportsActivity.this, InformationsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public class loadReports extends AsyncTask<String, String, String> {

        String invoker = null;

        private loadReports(String invoker) {
            this.invoker = invoker;
            loadingSpinner.show();
            execute();
        }

        @Override
        protected String doInBackground(String... params) {
            switch (invoker) {
                case App.DEPOSIT_REPORT: {
                    loadData(App.DEPOSIT_REPORT);
                    //printReport(App.DEPOSIT_REPORT);
                    break;
                }
                case App.SALES_SUMMARY: {
                    loadData(App.SALES_SUMMARY);
                    break;
                }
                case App.BAD_RETURN_REPORT: {
                    loadData(App.BAD_RETURN_REPORT);
                    break;
                }
                case App.ENDING_SUMMERY: {
                    loadData(App.ENDING_SUMMERY);
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }

            if (invoker.equals(App.DEPOSIT_REPORT)) {
                printReport(App.DEPOSIT_REPORT);
            }
            if (invoker.equals(App.SALES_SUMMARY)) {
                printReport(App.SALES_SUMMARY);
            }
            if (invoker.equals(App.BAD_RETURN_REPORT)) {
                printReport(App.BAD_RETURN_REPORT);
            }
            if (invoker.equals(App.ENDING_SUMMERY)) {
                printReport(App.ENDING_SUMMERY);
            }
        }
    }

    public class loadDataforPrint extends AsyncTask<String, String, String> {
        JSONArray jsonArray = new JSONArray();

        @Override
        protected void onPreExecute() {
            Log.e("Load Data Print", "Print");
            try {
                loadingSpinner.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }



            /*if(isPrint){
                try{


                    final JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String,String>map = new HashMap<>();
                            map.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                            map.put(db.KEY_ORDER_ID,purchaseNumber);
                            map.put(db.KEY_DOC_TYPE, ConfigStore.UnloadRequest_TR);
                            map.put(db.KEY_DATA,data.toString());
                            db.addDataPrint(db.DELAY_PRINT,map);
                        }
                    });


                    PrinterHelper object = new PrinterHelper(UnloadActivity.this,UnloadActivity.this);
                    object.execute("", jsonArray);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                *//*Intent intent = new Intent(LoadVerifyActivity.this, MyCalendarActivity.class);
                startActivity(intent);*//*
            }
            else{
                try{

                    final JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            HashMap<String,String>map = new HashMap<>();
                            map.put(db.KEY_CUSTOMER_NO,Settings.getString(App.DRIVER));
                            map.put(db.KEY_ORDER_ID,purchaseNumber);
                            map.put(db.KEY_DOC_TYPE,ConfigStore.UnloadRequest_TR);
                            map.put(db.KEY_DATA,data.toString());
                            db.addDataPrint(db.DELAY_PRINT, map);
                        }
                    });

                    Intent intent = new Intent(UnloadActivity.this,DashboardActivity.class);
                    startActivity(intent);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }*/
        }
    }

    public void loadData(String type) {
        try {
            switch (type) {
                case App.DEPOSIT_REPORT: {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_COLLECTION_TYPE, "");
                        map.put(db.KEY_CUSTOMER_TYPE, "");
                        map.put(db.KEY_CUSTOMER_NO, "");
                        map.put(db.KEY_INVOICE_NO, "");
                        map.put(db.KEY_INVOICE_AMOUNT, "");
                        map.put(db.KEY_DUE_DATE, "");
                        map.put(db.KEY_INVOICE_DATE, "");
                        map.put(db.KEY_AMOUNT_CLEARED, "");
                        map.put(db.KEY_CASH_AMOUNT, "");
                        map.put(db.KEY_CHEQUE_AMOUNT, "");
                        map.put(db.KEY_CHEQUE_NUMBER, "");
                        map.put(db.KEY_CHEQUE_DATE, "");
                        map.put(db.KEY_CHEQUE_BANK_CODE, "");
                        map.put(db.KEY_CHEQUE_BANK_NAME, "");
                        map.put(db.KEY_SAP_INVOICE_NO, "");
                        map.put(db.KEY_INVOICE_DAYS, "");
                        map.put(db.KEY_INDICATOR, "");
                        map.put(db.KEY_IS_POSTED, "");
                        map.put(db.KEY_IS_PRINTED, "");
                        map.put(db.KEY_IS_INVOICE_COMPLETE, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        Cursor c = db.getData(db.COLLECTION, map, filter);
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            do {
                                DepositReport depositReport = new DepositReport();
                                depositReport.setInvoiceNo(c.getString(c.getColumnIndex(db.KEY_INVOICE_NO)));
                                depositReport.setCustomerNo(c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                                CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                                if (customerHeader != null) {
                                    depositReport.setCustomerName(customerHeader.getName1());
                                } else {
                                    depositReport.setCustomerName(c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                                }
                                String[] cheques = new String[10];
                                String[] chequeDate = new String[10];
                                String[] chequeAmount = new String[10];
                                String[] bankCode = new String[10];
                                String[] bankNames = new String[10];
                                Log.e("Cheq", "" + UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_CHEQUE_NUMBER))));
                                cheques = UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_CHEQUE_NUMBER))).split(",");
                                Log.e("cheqest", "" + UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_CHEQUE_DATE))));
                                chequeDate = UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_CHEQUE_DATE))).split(",");
                                chequeAmount = UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_CHEQUE_AMOUNT))).split(",");
                                bankCode = UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_BANK_CODE))).split(",");
                                bankNames = UrlBuilder.decodeString(c.getString(c.getColumnIndex(db.KEY_BANK_NAME))).split(",");
                                if (cheques.length > 2) {
                                    for (int j = 1; j < cheques.length; j++) {
                                        depositReport.setChequeNo(cheques[j]);
                                        depositReport.setBankName(bankNames[j]);
                                        depositReport.setBankCode(bankCode[j]);
                                        depositReport.setChequeDate(chequeDate[j]);
                                        depositReport.setChequeAmount(chequeAmount[j]);
                                        depositReport.setCashAmount(c.getString(c.getColumnIndex(db.KEY_CASH_AMOUNT)));
                                        depositReports.add(depositReport);
                                    }
                                } else if (cheques.length == 1) {
                                    depositReport.setChequeNo(cheques[0].equals("0000") ? "-" : cheques[0]);
                                    depositReport.setBankName(bankNames[0].equals("0000") ? "-" : bankNames[0]);
                                    depositReport.setBankCode(bankCode[0].equals("0000") ? "-" : bankCode[0]);
                                    depositReport.setChequeDate(chequeDate[0].equals("0000") ? "-" : chequeDate[0]);
                                    depositReport.setChequeAmount(chequeAmount[0].equals("0000") ? "-" : chequeAmount[0]);
                                    depositReport.setCashAmount(c.getString(c.getColumnIndex(db.KEY_CASH_AMOUNT)));

                                } else if (cheques.length == 2) {
                                    depositReport.setChequeNo(cheques[1].equals("0000") ? "-" : cheques[1]);
                                    depositReport.setBankName(bankNames[1].equals("0000") ? "-" : bankNames[1]);
                                    depositReport.setBankCode(bankCode[1].equals("0000") ? "-" : bankCode[1]);
                                    depositReport.setChequeDate(chequeDate[0].equals("0000") ? chequeDate.length > 1 ? chequeDate[1] : "-" : Helpers.formatDate(new Date(), App.DATE_PICKER_FORMAT));
                                    depositReport.setChequeAmount(chequeAmount[0].equals("0000") ? "-" : chequeAmount[0]);
                                    depositReport.setCashAmount(c.getString(c.getColumnIndex(db.KEY_CASH_AMOUNT)));
                                }
                                depositReports.add(depositReport);
                            }
                            while (c.moveToNext());
                        }

                        HashMap<String, String> map1 = new HashMap<>();
                        map1.put(db.KEY_COLLECTION_TYPE, "");
                        map1.put(db.KEY_CUSTOMER_TYPE, "");
                        map1.put(db.KEY_CUSTOMER_NO, "");
                        map1.put(db.KEY_INVOICE_NO, "");
                        map1.put(db.KEY_INVOICE_AMOUNT, "");
                        map1.put(db.KEY_DUE_DATE, "");
                        map1.put(db.KEY_INVOICE_DATE, "");
                        map1.put(db.KEY_AMOUNT_CLEARED, "");
                        map1.put(db.KEY_CASH_AMOUNT, "");
                        map1.put(db.KEY_CHEQUE_AMOUNT, "");
                        map1.put(db.KEY_CHEQUE_NUMBER, "");
                        map1.put(db.KEY_CHEQUE_DATE, "");
                        map1.put(db.KEY_CHEQUE_BANK_CODE, "");
                        map1.put(db.KEY_CHEQUE_BANK_NAME, "");
                        map1.put(db.KEY_SAP_INVOICE_NO, "");
                        map1.put(db.KEY_INVOICE_DAYS, "");
                        map1.put(db.KEY_INDICATOR, "");
                        map1.put(db.KEY_IS_POSTED, "");
                        map1.put(db.KEY_IS_PRINTED, "");
                        map1.put(db.KEY_IS_INVOICE_COMPLETE, "");
                        HashMap<String, String> filter1 = new HashMap<>();
                        filter1.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                        Cursor c1 = db.getData(db.COLLECTION, map1, filter1);
                        if (c1.getCount() > 0) {
                            c1.moveToFirst();
                            do {
                                DepositReport depositReport = new DepositReport();
                                depositReport.setInvoiceNo(c1.getString(c1.getColumnIndex(db.KEY_INVOICE_NO)));
                                depositReport.setCustomerNo(c1.getString(c1.getColumnIndex(db.KEY_CUSTOMER_NO)));
                                CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, c1.getString(c1.getColumnIndex(db.KEY_CUSTOMER_NO)));
                                if (customerHeader != null) {
                                    depositReport.setCustomerName(customerHeader.getName1());
                                } else {
                                    depositReport.setCustomerName(c1.getString(c1.getColumnIndex(db.KEY_CUSTOMER_NO)));
                                }
                                String[] cheques = new String[10];
                                String[] chequeDate = new String[10];
                                String[] chequeAmount = new String[10];
                                String[] bankCode = new String[10];
                                String[] bankNames = new String[10];
                                Log.e("Cheq", "" + UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_CHEQUE_NUMBER))));
                                cheques = UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_CHEQUE_NUMBER))).split(",");
                                Log.e("cheqest", "" + UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_CHEQUE_DATE))));
                                chequeDate = UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_CHEQUE_DATE))).split(",");
                                chequeAmount = UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_CHEQUE_AMOUNT))).split(",");
                                bankCode = UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_BANK_CODE))).split(",");
                                bankNames = UrlBuilder.decodeString(c1.getString(c1.getColumnIndex(db.KEY_BANK_NAME))).split(",");
                                if (cheques.length > 2) {
                                    for (int j = 1; j < cheques.length; j++) {
                                        depositReport.setChequeNo(cheques[j]);
                                        depositReport.setBankName(bankNames[j]);
                                        depositReport.setBankCode(bankCode[j]);
                                        depositReport.setChequeDate(chequeDate[j]);
                                        depositReport.setChequeAmount(chequeAmount[j]);
                                        depositReport.setCashAmount(c1.getString(c1.getColumnIndex(db.KEY_CASH_AMOUNT)));
                                        depositReports.add(depositReport);
                                    }
                                } else if (cheques.length == 1) {
                                    depositReport.setChequeNo(cheques[0].equals("0000") ? "-" : cheques[0]);
                                    depositReport.setBankName(bankNames[0].equals("0000") ? "-" : bankNames[0]);
                                    depositReport.setBankCode(bankCode[0].equals("0000") ? "-" : bankCode[0]);
                                    depositReport.setChequeDate(chequeDate[0].equals("0000") ? "-" : chequeDate[0]);
                                    depositReport.setChequeAmount(chequeAmount[0].equals("0000") ? "-" : chequeAmount[0]);
                                    depositReport.setCashAmount(c1.getString(c1.getColumnIndex(db.KEY_CASH_AMOUNT)));

                                } else if (cheques.length == 2) {
                                    depositReport.setChequeNo(cheques[1].equals("0000") ? "-" : cheques[1]);
                                    depositReport.setBankName(bankNames[1].equals("0000") ? "-" : bankNames[1]);
                                    depositReport.setBankCode(bankCode[1].equals("0000") ? "-" : bankCode[1]);
                                    depositReport.setChequeDate(chequeDate[0].equals("0000") ? chequeDate.length > 1 ? chequeDate[1] : "-" : Helpers.formatDate(new Date(), App.DATE_PICKER_FORMAT));
                                    depositReport.setChequeAmount(chequeAmount[0].equals("0000") ? "-" : chequeAmount[0]);
                                    depositReport.setCashAmount(c1.getString(c1.getColumnIndex(db.KEY_CASH_AMOUNT)));
                                }
                                depositReports.add(depositReport);
                            }
                            while (c1.moveToNext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }

                    break;
                }
                case App.SALES_SUMMARY: {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TIME_STAMP, "");
                    map.put(db.KEY_CUSTOMER_NO, "");
                    map.put(db.KEY_CUSTOMER_TYPE, "");
                    map.put(db.KEY_ACTIVITY_TYPE, "");
                    map.put(db.KEY_ORDER_TOTAL, "");
                    map.put(db.KEY_ORDER_NET_TOTAL, "");
                    map.put(db.KEY_RETURN_TOTAL, "");
                    map.put(db.KEY_GOOD_RETURN_TOTAL, "");
                    map.put(db.KEY_ORDER_DISCOUNT, "");
                    map.put(db.KEY_ORDER_ID, "");
                    HashMap<String, String> filter = new HashMap<>();
                    Cursor c = db.getData(db.TODAYS_SUMMARY_SALES, map, filter);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            SalesSummary salesSummary = new SalesSummary();
                            salesSummary.setTransactionNo(c.getString(c.getColumnIndex(db.KEY_ORDER_ID)));
                            salesSummary.setCustomerNo(c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            if (customerHeader != null) {
                                salesSummary.setCustomerName(customerHeader.getName1());
                            } else {
                                salesSummary.setCustomerName(c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            }
                            HashMap<String, String> invoiceClearMap = new HashMap<>();
                            invoiceClearMap.put(db.KEY_CUSTOMER_NO, "");
                            invoiceClearMap.put(db.KEY_INVOICE_NO, "");
                            invoiceClearMap.put(db.KEY_INVOICE_AMOUNT, "");
                            invoiceClearMap.put(db.KEY_DUE_DATE, "");
                            invoiceClearMap.put(db.KEY_INVOICE_DATE, "");
                            invoiceClearMap.put(db.KEY_AMOUNT_CLEARED, "");
                            invoiceClearMap.put(db.KEY_INDICATOR, "");
                            invoiceClearMap.put(db.KEY_IS_INVOICE_COMPLETE, "");
                            HashMap<String, String> invoiceClearMapFilter = new HashMap<>();
                            invoiceClearMapFilter.put(db.KEY_CUSTOMER_NO, c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            invoiceClearMapFilter.put(db.KEY_AMOUNT_CLEARED, c.getString(c.getColumnIndex(db.KEY_ORDER_TOTAL)));
                            Cursor ivCursor = db.getData(db.COLLECTION, invoiceClearMap, invoiceClearMapFilter);
                            if (ivCursor.getCount() > 0) {
                                ivCursor.moveToFirst();
                                try {
                                    salesSummary.setAmountPaid(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                } catch (Exception e) {
                                    salesSummary.setAmountPaid("0.0");
                                    e.printStackTrace();
                                }
                                try {
                                    salesSummary.setAmountDue(String.valueOf(Double.parseDouble(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_INVOICE_AMOUNT)))));
                                } catch (Exception e) {
                                    salesSummary.setAmountDue("0.0");
                                    e.printStackTrace();
                                }
                            } else {
                                salesSummary.setAmountDue("0.0");
                                salesSummary.setAmountPaid("0.0");
                            }
                            salesSummary.setTransactionType(c.getString(c.getColumnIndex(db.KEY_ACTIVITY_TYPE)).equals(App.ACTIVITY_INVOICE) ? "INV" : "DLV");
                            salesSummary.setTotalSales(c.getString(c.getColumnIndex(db.KEY_ORDER_NET_TOTAL)) == null ? "0.0" : c.getString(c.getColumnIndex(db.KEY_ORDER_NET_TOTAL)));
                            salesSummary.setTotalReturns(c.getString(c.getColumnIndex(db.KEY_RETURN_TOTAL)) == null ? "0.0" : c.getString(c.getColumnIndex(db.KEY_RETURN_TOTAL)));
                            salesSummary.setTotalgoodReturns(c.getString(c.getColumnIndex(db.KEY_GOOD_RETURN_TOTAL)) == null ? "0.0" : c.getString(c.getColumnIndex(db.KEY_GOOD_RETURN_TOTAL)));
                            salesSummary.setDiscounts(c.getString(c.getColumnIndex(db.KEY_ORDER_DISCOUNT)) == null ? "0.0" : c.getString(c.getColumnIndex(db.KEY_ORDER_DISCOUNT)));
                            salesSummary.setNetSales(c.getString(c.getColumnIndex(db.KEY_ORDER_TOTAL)) == null ? "0.0" : c.getString(c.getColumnIndex(db.KEY_ORDER_TOTAL)));
                            if (c.getString(c.getColumnIndex(db.KEY_CUSTOMER_TYPE)).equals(App.CASH_CUSTOMER)) {
                                cashSales.add(salesSummary);
                            } else if (c.getString(c.getColumnIndex(db.KEY_CUSTOMER_TYPE)).equals(App.TC_CUSTOMER)) {
                                tcSales.add(salesSummary);
                            } else if (c.getString(c.getColumnIndex(db.KEY_CUSTOMER_TYPE)).equals(App.CREDIT_CUSTOMER)) {
                                creditSales.add(salesSummary);
                            }
                        }
                        while (c.moveToNext());
                    }
                    break;
                }
                case App.BAD_RETURN_REPORT: {
                    for (int i = 0; i < articles.size(); i++) {
                        DamageReport damageReport = new DamageReport();
                        float badReturnQuantity = 0;
                        float badReturnVariance = 0;
                        damageReport.setItemNo(articles.get(i).getMaterialNo());
                        ArticleHeader articleHeader = ArticleHeader.getArticle(articles, articles.get(i).getMaterialNo());
                        if (articleHeader != null) {
                            damageReport.setItemDescription(articleHeader.getMaterialDesc1());
                        } else {
                            damageReport.setItemDescription(articles.get(i).getMaterialNo());
                        }
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                        filter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        Cursor cursor = db.getData(db.RETURNS, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                badReturnQuantity += Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                            }
                            while (cursor.moveToNext());
                            HashMap<String, String> varMap = new HashMap<>();
                            varMap.put(db.KEY_MATERIAL_NO, "");
                            varMap.put(db.KEY_CASE, "");
                            varMap.put(db.KEY_UNIT, "");
                            HashMap<String, String> varFilter = new HashMap<>();
                            varFilter.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                            varFilter.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                            Cursor varCursor = db.getData(db.UNLOAD_VARIANCE, varMap, varFilter);
                            if (varCursor.getCount() > 0) {
                                varCursor.moveToFirst();
                                do {
                                    badReturnVariance += Float.parseFloat(varCursor.getString(varCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (varCursor.moveToNext());
                            }
                            damageReport.setItemQuantity(String.valueOf(badReturnQuantity));
                            damageReport.setItemVariance(String.valueOf(badReturnVariance));
                        }

                        HashMap<String, String> priceMap = new HashMap<>();
                        priceMap.put(db.KEY_AMOUNT, "");
                        HashMap<String, String> filterPrice = new HashMap<>();
                        filterPrice.put(db.KEY_MATERIAL_NO, articles.get(i).getMaterialNo());
                        filterPrice.put(db.KEY_PRIORITY, "2");
                        Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            damageReport.setItemPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        } else {
                            damageReport.setItemPrice("0");
                        }


                        if (badReturnQuantity > 0) {
                            damageReports.add(damageReport);
                        }
                    }
                    break;
                }
                case App.ENDING_SUMMERY: {
                    for (ArticleHeader articleHeader : articles) {
                        //double vanCase = 0;
                        double vanQty = 0;
                        double totalVarianceQty = 0;
                        double totalVarianceAmount = 0;
//                    double brCase = 0;
                        double openUnits = 0;
                        double todayUnits = 0;
                        double brUnits = 0;
                        double grUnits = 0;
                        double siUnits = 0;
//                    double eiCase = 0;
                        double eiUnits = 0;
//                    double freshUnloadCase = 0;
                        double freshUnloadUnits = 0;
//                    double truckDamageCase = 0;
                        double truckDamageUnits = 0;
                        double itemPrice = 0;


                        UnloadSummaryPrint unloadSummaryPrint = new UnloadSummaryPrint();
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NO, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_RESERVED_QTY_CASE, "");
                        map.put(db.KEY_RESERVED_QTY_UNIT, "");
                        map.put(db.KEY_REMAINING_QTY_CASE, "");
                        map.put(db.KEY_REMAINING_QTY_UNIT, "");
                        map.put(db.KEY_UOM_CASE, "");
                        map.put(db.KEY_UOM_UNIT, "");
                        map.put(db.KEY_DIST_CHANNEL, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                        Cursor c = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            unloadSummaryPrint.setItemNo(articleHeader.getMaterialNo());
                            unloadSummaryPrint.setItemDescription(UrlBuilder.decodeString(articleHeader.getMaterialDesc1()));
                            String cases = String.valueOf(Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_RESERVED_QTY_CASE)))
                                    + Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_CASE))));
                            String units = String.valueOf(Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_RESERVED_QTY_CASE)))
                                    + Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_CASE))));
                            vanQty = Double.parseDouble(units);
                            unloadSummaryPrint.setVanStock(units);

                            //Opening stock
                            HashMap<String, String> mapOpen = new HashMap<>();
                            mapOpen.put(db.KEY_DELIVERY_NO, "");
                            mapOpen.put(db.KEY_TABLE_FLAGE, "");
                            mapOpen.put(db.KEY_MATERIAL_NO, "");
                            mapOpen.put(db.KEY_ACTUAL_QTY, "");
                            HashMap<String, String> filterOpen = new HashMap<>();
                            filterOpen.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());

                            Cursor cursor = db.getData(db.LOAD_DELIVERY_ITEMS, mapOpen, filterOpen);
                            if (cursor.getCount() > 0) {
                                cursor.moveToFirst();
                                do {
                                    try {
                                        Double qtyPcs = 0.0;
                                        qtyPcs = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)));
                                        if (cursor.getString(cursor.getColumnIndex(db.KEY_TABLE_FLAGE)).equals("2")) {
                                            openUnits += qtyPcs;
                                        } else {
                                            todayUnits += qtyPcs;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Crashlytics.logException(e);
                                    }
                                }
                                while (cursor.moveToNext());
                            }
                            unloadSummaryPrint.setBeginLoad(String.valueOf(openUnits));
                            unloadSummaryPrint.setTodayLoad(String.valueOf(todayUnits));


                            String deno = "0";
                            HashMap<String, String> altMap = new HashMap<>();
                            altMap.put(db.KEY_UOM, "");
                            altMap.put(db.KEY_DENOMINATOR, "");
                            HashMap<String, String> filtera = new HashMap<>();
                            filtera.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                            if (altUOMCursor.getCount() > 0) {
                                altUOMCursor.moveToFirst();
                                deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                            }

                            //Sales
                            HashMap<String, String> itemMap = new HashMap<>();
                            itemMap.put(db.KEY_MATERIAL_NO, "");
                            itemMap.put(db.KEY_ORG_CASE, "");
                            itemMap.put(db.KEY_ORG_UNITS, "");
                            HashMap<String, String> filtersi = new HashMap<>();
                            filtersi.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());

                            Cursor pendingInvoiceCursor = db.getData(db.CAPTURE_SALES_INVOICE, itemMap, filtersi);
                            if (pendingInvoiceCursor.getCount() > 0) {
                                pendingInvoiceCursor.moveToFirst();
                                do {
                                    Double qtyPcs = 0.0, qtyCases = 0.0;
                                    if (Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_CASE))) >= 1) {


                                        qtyCases = Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_CASE))) * Double.parseDouble(deno);
                                    }
                                    if (Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_UNITS))) >= 1) {
                                        qtyPcs += Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                                    }
                                    siUnits += qtyCases + qtyPcs;

                                }
                                while (pendingInvoiceCursor.moveToNext());
                            }
                            unloadSummaryPrint.setSalesUnit(String.valueOf(siUnits));

                            //Bad return

                            HashMap<String, String> brMap = new HashMap<>();
                            brMap.put(db.KEY_MATERIAL_NO, "");
                            brMap.put(db.KEY_CASE, "");
                            brMap.put(db.KEY_UNIT, "");
                            HashMap<String, String> brMapFilter = new HashMap<>();
                            brMapFilter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                            brMapFilter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            Cursor brCursor = db.getData(db.RETURNS, brMap, brMapFilter);
                            if (brCursor.getCount() > 0) {
                                brCursor.moveToFirst();
                                do {
                                    //brCase += Double.parseDouble(brCursor.getString(brCursor.getColumnIndex(db.KEY_CASE)));

                                    Double qtyPcs = 0.0, qtyCases = 0.0;

                                    if (Double.parseDouble(brCursor.getString(brCursor.getColumnIndex(db.KEY_CASE))) >= 1) {


                                        qtyCases = Float.parseFloat(brCursor.getString(brCursor.getColumnIndex(db.KEY_CASE))) * Double.parseDouble(deno);
                                    }
                                    if (Double.parseDouble(brCursor.getString(brCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                        qtyPcs += Double.parseDouble(brCursor.getString(brCursor.getColumnIndex(db.KEY_UNIT)));
                                    }

                                    brUnits += qtyCases + qtyPcs;
                                }
                                while (brCursor.moveToNext());
                            }
                            unloadSummaryPrint.setBadReturns(String.valueOf(brUnits));

                            //Good
                            HashMap<String, String> grMap = new HashMap<>();
                            grMap.put(db.KEY_MATERIAL_NO, "");
                            grMap.put(db.KEY_CASE, "");
                            grMap.put(db.KEY_UNIT, "");
                            HashMap<String, String> grMapFilter = new HashMap<>();
                            grMapFilter.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                            grMapFilter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            Cursor grCursor = db.getData(db.RETURNS, grMap, grMapFilter);
                            if (grCursor.getCount() > 0) {
                                grCursor.moveToFirst();
                                do {
                                    //brCase += Double.parseDouble(brCursor.getString(brCursor.getColumnIndex(db.KEY_CASE)));

                                    Double qtyPcs = 0.0, qtyCases = 0.0;

                                    if (Double.parseDouble(grCursor.getString(grCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                        qtyCases = Float.parseFloat(grCursor.getString(grCursor.getColumnIndex(db.KEY_CASE))) * Double.parseDouble(deno);
                                    }
                                    if (Double.parseDouble(grCursor.getString(grCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                        qtyPcs += Double.parseDouble(grCursor.getString(grCursor.getColumnIndex(db.KEY_UNIT)));
                                    }

                                    grUnits += qtyCases + qtyPcs;
                                }
                                while (brCursor.moveToNext());
                            }
                            unloadSummaryPrint.setGoodReturns(String.valueOf(grUnits));

                            HashMap<String, String> filterPart = new HashMap<>();
                            filterPart.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            HashMap<String, String> priceMap = new HashMap<>();
                            priceMap.put(db.KEY_MATERIAL_NO, "");
                            priceMap.put(db.KEY_AMOUNT, "");
                            Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPart);
                            if (priceCursor.getCount() > 0) {
                                priceCursor.moveToFirst();
                                do {
                                    itemPrice = Double.parseDouble(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                                }
                                while (priceCursor.moveToNext());
                            }
                            unloadSummaryPrint.setItemPrice(String.valueOf(itemPrice));
                            HashMap<String, String> brVariance = new HashMap<>();
                            brVariance.put(db.KEY_ITEM_NO, "");
                            brVariance.put(db.KEY_MATERIAL_DESC1, "");
                            brVariance.put(db.KEY_MATERIAL_NO, "");
                            brVariance.put(db.KEY_MATERIAL_GROUP, "");
                            brVariance.put(db.KEY_CASE, "");
                            brVariance.put(db.KEY_UNIT, "");
                            brVariance.put(db.KEY_UOM, "");
                            HashMap<String, String> brVarianceFilter = new HashMap<>();
                            brVarianceFilter.put(db.KEY_VARIANCE_TYPE, App.BAD_RETURN_VARIANCE);
                            brVarianceFilter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            Cursor brVarianceCursor = db.getData(db.UNLOAD_VARIANCE, brVariance, brVarianceFilter);

                            if (brVarianceCursor.getCount() > 0) {
                                brVarianceCursor.moveToFirst();
                                do {
                                    totalVarianceQty += Double.parseDouble(brVarianceCursor.getString(brVarianceCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (brVarianceCursor.moveToNext());
                            }

                            HashMap<String, String> theftMap = new HashMap<>();
                            theftMap.put(db.KEY_ITEM_NO, "");
                            theftMap.put(db.KEY_MATERIAL_NO, "");
                            theftMap.put(db.KEY_CASE, "");
                            theftMap.put(db.KEY_UNIT, "");
                            HashMap<String, String> theftMapFilter = new HashMap<>();
                            theftMapFilter.put(db.KEY_VARIANCE_TYPE, App.THEFT);
                            theftMapFilter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            Cursor theftCursor = db.getData(db.UNLOAD_VARIANCE, theftMap, theftMapFilter);
                            if (theftCursor.getCount() > 0) {
                                theftCursor.moveToFirst();
                                do {
                                    totalVarianceQty += Double.parseDouble(theftCursor.getString(theftCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (theftCursor.moveToNext());
                            }

                            unloadSummaryPrint.setVarianceQty(String.valueOf(totalVarianceQty));

                            HashMap<String, String> eiMap = new HashMap<>();
                            eiMap.put(db.KEY_CASE, "");
                            eiMap.put(db.KEY_UNIT, "");
                            HashMap<String, String> eiMapFilter = new HashMap<>();
                            eiMapFilter.put(db.KEY_VARIANCE_TYPE, App.ENDING_INVENTORY);
                            eiMapFilter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            Cursor eiCursor = db.getData(db.UNLOAD_VARIANCE, eiMap, eiMapFilter);
                            if (eiCursor.getCount() > 0) {
                                eiCursor.moveToFirst();
                                do {
                                    eiUnits += Double.parseDouble(eiCursor.getString(eiCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (eiCursor.moveToNext());
                            }
                            unloadSummaryPrint.setEndingInventory(String.valueOf(eiUnits));

                            HashMap<String, String> truckDamageMap = new HashMap<>();
                            truckDamageMap.put(db.KEY_MATERIAL_NO, "");
                            truckDamageMap.put(db.KEY_CASE, "");
                            truckDamageMap.put(db.KEY_UNIT, "");
                            HashMap<String, String> truckDamageFilter = new HashMap<>();
                            truckDamageFilter.put(db.KEY_VARIANCE_TYPE, App.TRUCK_DAMAGE);
                            truckDamageFilter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            Cursor truckDamageCursor = db.getData(db.UNLOAD_VARIANCE, truckDamageMap, truckDamageFilter);
                            if (truckDamageCursor.getCount() > 0) {
                                truckDamageCursor.moveToFirst();
                                do {
                                    truckDamageUnits += Double.parseDouble(truckDamageCursor.getString(truckDamageCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (truckDamageCursor.moveToNext());
                            }

                            unloadSummaryPrint.setTruckDamage(String.valueOf(truckDamageUnits));
                            freshUnloadUnits = vanQty - eiUnits - truckDamageUnits - totalVarianceQty - brUnits;
                            unloadSummaryPrint.setFreshUnload(String.valueOf(freshUnloadUnits));
                            printUnloadList.add(unloadSummaryPrint);
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public void printReport(String type) {
        try {


            PrinterHelper object = new PrinterHelper(PrinterReportsActivity.this, PrinterReportsActivity.this);
            switch (type) {
                case App.DEPOSIT_REPORT: {
                    JSONArray jArr = createDataforDeposit();
                    object.execute("", createDataforDeposit());
                    break;
                }
                case App.SALES_SUMMARY: {
                    object.execute("", createDataforSalesSummary());
                    break;
                }
                case App.BAD_RETURN_REPORT: {
                    object.execute("", createDataforBadReturns());
                    break;
                }
                case App.ENDING_SUMMERY: {
                    object.execute("", createPrintData());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public JSONArray createDataforDeposit() {
        JSONArray jArr = new JSONArray();
        try {
            float totalCash = 0;
            float totalCheque = 0;

            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST, App.DEPOSIT_REPORT);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("supervisorname", "-");
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            //mainArr.put("invheadermsg","HAPPY NEW YEAR");
            mainArr.put("LANG", "en");


            //mainArr.put("Load Number","1");


            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();

            HEADERS.put("SI No.");
            HEADERS.put("Transaction Number");
            HEADERS.put("Customer Code");
            HEADERS.put("Customer Name");  //Fresh unload
            HEADERS.put("Cheque No");  //Summation of all
            HEADERS.put("Cheque Date");  //Truck Damage
            HEADERS.put("Bank Name");  //Truck Damage
            HEADERS.put("Cheque Amount");  //Bad Returns
            HEADERS.put("Cash Amount");


            //HEADERS.put("Description");

            //HEADERS.put(obj1);
            // HEADERS.put(obj2);
            mainArr.put("HEADERS", HEADERS);

            JSONArray jData = new JSONArray();
            for (DepositReport obj : depositReports) {
                JSONArray jData1 = new JSONArray();
                jData1.put("" + (jData.length() + 1));
                jData1.put(obj.getInvoiceNo());
                jData1.put(obj.getCustomerNo());
                jData1.put(obj.getCustomerName());
                jData1.put(obj.getChequeNo());
                jData1.put(obj.getChequeDate());
                jData1.put(obj.getBankName());
                jData1.put(obj.getChequeAmount());
                totalCheque += Double.parseDouble(obj.getChequeAmount());
                jData1.put(obj.getCashAmount());
                totalCash += Double.parseDouble(obj.getCashAmount());
                jData.put(jData1);
            }

            JSONObject totalObj = new JSONObject();
            totalObj.put("Cheque Amount", "+" + String.valueOf(totalCheque));
            totalObj.put("Cash Amount", "+" + String.valueOf(totalCash));  //Summation of all
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);
            mainArr.put("TOTAL DEPOSIT AMOUNT", "+" + String.valueOf(totalCash + totalCheque));
            mainArr.put("TOTAL CASH AMOUNT", "+" + String.valueOf(totalCash));
            mainArr.put("TOTAL CHEQUE AMOUNT", "+" + String.valueOf(totalCheque));
            /*JSONArray jData1 = new JSONArray();
            jData1.put("14020106");
            jData1.put("200001");
            //jData1.put("شد 48*200مل بيرين PH8");
            //jData1.put("+1");
            jData1.put("Test Customer");
            jData1.put("1234");
            jData1.put("20/02/2016");
            jData1.put("Emirates NBD");
            jData1.put("100");
            jData1.put("0");

            JSONArray jData2 = new JSONArray();
            jData2.put("14020107");
            jData2.put("200001");
            //jData1.put("شد 48*200مل بيرين PH8");
            //jData1.put("+1");
            jData2.put("Test Customer");
            jData2.put("1234");
            jData2.put("20/02/2016");
            jData2.put("Emirates NBD");
            jData2.put("100");
            jData2.put("0");

            JSONArray jData3 = new JSONArray();
            jData3.put("14020106");
            jData3.put("200001");
            //jData1.put("شد 48*200مل بيرين PH8");
            //jData1.put("+1");
            jData3.put("Test Customer");
            jData3.put("-");
            jData3.put("-");
            jData3.put("-");
            jData3.put("0");
            jData3.put("100");*/




            /*jData.put(jData1);
            jData.put(jData2);
            jData.put(jData3);*/
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("DATA", jData);
            jsonObject.put("HEADERS", HEADERS);
            jsonObject.put("TOTAL", totalObj);
            JSONArray jDataNew = new JSONArray();
            jDataNew.put(jsonObject);
            mainArr.put("data", jDataNew);
            //mainArr.put("data",jData);

            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);

            jArr.put(HEADERS);


        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return jArr;
    }

    public JSONArray createDataforSalesSummary() {
        JSONArray jArr = new JSONArray();
        try {
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST, App.SALES_SUMMARY);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            //mainArr.put("invheadermsg","HAPPY NEW YEAR");
            mainArr.put("LANG", "en");
            mainArr.put("invoicepaymentterms", "2");
            mainArr.put("invoicenumber", "1300000001");
            mainArr.put("INVOICETYPE", "SALES INVOICE");
            String arabicCustomer = "اللولو هايبر ماركت";
            mainArr.put("CUSTOMER", "LULU HYPER MARKET" + "-" + arabicCustomer);
            mainArr.put("ADDRESS", "3101, 21st Street, Riyadh");
            mainArr.put("ARBADDRESS", "");
            mainArr.put("displayupc", "0");
            mainArr.put("invoicepriceprint", "1");
            mainArr.put("SUB TOTAL", "1000");
            mainArr.put("INVOICE DISCOUNT", "20");
            mainArr.put("NET SALES", "980");
            mainArr.put("closevalue", "+5000");
            mainArr.put("availvalue", "+1000");
            mainArr.put("TOTAL DEPOSIT AMOUNT", "+2000");

            //mainArr.put("Load Number","1");


            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();

            HEADERS.put("SI No.");
            HEADERS.put("Transaction No.");
            HEADERS.put("Cust. No.");
            HEADERS.put("Customer");
            HEADERS.put("Type");
            HEADERS.put("Sales");
            HEADERS.put("Returns");//Summation of all
            HEADERS.put("Good Rtns");  //Truck Damage
            HEADERS.put("Net Sales");  //Truck Damage
            HEADERS.put("Discounts");  //Bad Returns
            HEADERS.put("Amount Paid");
            HEADERS.put("T.C");

            mainArr.put("HEADERS", HEADERS);
            JSONObject totalObj = new JSONObject();
            totalObj.put("Cheque Amount", "+200");
            totalObj.put("Cash Amount", "+100");  //Summation of all
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);
            JSONArray cashData = new JSONArray();
            JSONArray tcData = new JSONArray();
            JSONArray chequeData = new JSONArray();
            for (SalesSummary salesSummary : cashSales) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put("" + (cashData.length() + 1));
                jsonArray.put(salesSummary.getTransactionNo());
                jsonArray.put(salesSummary.getCustomerNo());
                jsonArray.put(salesSummary.getCustomerName());
                jsonArray.put(salesSummary.getTransactionType());
                jsonArray.put(salesSummary.getTotalSales());
                jsonArray.put(salesSummary.getTotalReturns());
                jsonArray.put(salesSummary.getTotalgoodReturns());
                jsonArray.put(salesSummary.getNetSales());
                jsonArray.put(salesSummary.getDiscounts());
                jsonArray.put(salesSummary.getAmountPaid());
                jsonArray.put(" " + salesSummary.getAmountDue());
                cashData.put(jsonArray);
            }
            for (SalesSummary salesSummary : tcSales) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put("" + (tcData.length() + 1));
                jsonArray.put(salesSummary.getTransactionNo());
                jsonArray.put(salesSummary.getCustomerNo());
                jsonArray.put(salesSummary.getCustomerName());
                jsonArray.put(salesSummary.getTransactionType());
                jsonArray.put(salesSummary.getTotalSales());
                jsonArray.put(salesSummary.getTotalReturns());
                jsonArray.put(salesSummary.getTotalgoodReturns());
                jsonArray.put(salesSummary.getNetSales());
                jsonArray.put(salesSummary.getDiscounts());
                jsonArray.put(salesSummary.getAmountPaid());
                jsonArray.put(" " + salesSummary.getAmountDue());
                tcData.put(jsonArray);
            }
            for (SalesSummary salesSummary : creditSales) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put("" + (chequeData.length() + 1));
                jsonArray.put(salesSummary.getTransactionNo());
                jsonArray.put(salesSummary.getCustomerNo());
                jsonArray.put(salesSummary.getCustomerName());
                jsonArray.put(salesSummary.getTransactionType());
                jsonArray.put(salesSummary.getTotalSales());
                jsonArray.put(salesSummary.getTotalReturns());
                jsonArray.put(salesSummary.getTotalgoodReturns());
                jsonArray.put(salesSummary.getNetSales());
                jsonArray.put(salesSummary.getDiscounts());
                jsonArray.put(salesSummary.getAmountPaid());
                jsonArray.put(" " + salesSummary.getAmountDue());
                chequeData.put(jsonArray);
            }

            JSONArray jData = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("DATA", jData);
            jsonObject.put("HEADERS", HEADERS);
            jsonObject.put("TOTAL", totalObj);
            JSONArray jDataNew = new JSONArray();
            jDataNew.put(jsonObject);
            mainArr.put("data", cashData);
            mainArr.put("tcData", tcData);
            mainArr.put("creditData", chequeData);

            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);
            jArr.put(HEADERS);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return jArr;
    }

    public JSONArray createDataforBadReturns() {
        JSONArray jArr = new JSONArray();
        try {
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            float totalInventoryCredit = 0;
            float totalLoadedIn = 0;
            float totalVarianceQty = 0;
            float totalVarianceAmount = 0;
            float totalInventoryCreditValue = 0;
            float totalLoadedInValue = 0;
            jDict.put(App.REQUEST, App.BAD_RETURN_REPORT);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO", "1234");
            mainArr.put("DOCUMENT NO", "80001234");  //Load Summary No
            mainArr.put("ORDERNO", "80001234");  //Load Summary No
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("supervisorname", "-");
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            //mainArr.put("invheadermsg","HAPPY NEW YEAR");
            mainArr.put("LANG", "en");
            mainArr.put("invoicepaymentterms", "2");
            mainArr.put("invoicenumber", "1300000001");
            mainArr.put("INVOICETYPE", "SALES INVOICE");
            String arabicCustomer = "اللولو هايبر ماركت";
            mainArr.put("CUSTOMER", "LULU HYPER MARKET" + "-" + arabicCustomer);
            mainArr.put("ADDRESS", "3101, 21st Street, Riyadh");
            mainArr.put("ARBADDRESS", "");
            mainArr.put("displayupc", "0");
            mainArr.put("invoicepriceprint", "1");
            mainArr.put("SUB TOTAL", "1000");
            mainArr.put("INVOICE DISCOUNT", "20");
            mainArr.put("NET SALES", "980");
            mainArr.put("closevalue", "+5000");


            //mainArr.put("Load Number","1");


            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();

            HEADERS.put("SI No.");
            HEADERS.put("ITEM#");
            HEADERS.put("DESCRIPTION");
            HEADERS.put("INVOICE CREDIT");
            HEADERS.put("LOADED IN");
            HEADERS.put("PRICE");//Summation of all
            HEADERS.put("-----VARIANCE----- QTY         AMOUNT");  //Truck Damage

            //HEADERS.put("Description");

            //HEADERS.put(obj1);
            // HEADERS.put(obj2);
            mainArr.put("HEADERS", HEADERS);
            JSONArray jData = new JSONArray();
            for (DamageReport obj : damageReports) {
                JSONArray jsonArray = new JSONArray();

                jsonArray.put("" + (jData.length() + 1));
                jsonArray.put(StringUtils.stripStart(obj.getItemNo(), "0"));
                jsonArray.put(UrlBuilder.decodeString(obj.getItemDescription()));
                jsonArray.put(obj.getItemQuantity());
                double inventoryCredit = Double.parseDouble(obj.getItemQuantity());
                totalInventoryCredit += Double.parseDouble(obj.getItemQuantity());
                jsonArray.put(String.valueOf(Double.parseDouble(obj.getItemQuantity()) - Double.parseDouble(obj.getItemVariance())));
                double loadedIn = Double.parseDouble(obj.getItemQuantity()) - Double.parseDouble(obj.getItemVariance());
                totalLoadedIn += loadedIn;
                jsonArray.put(obj.getItemPrice());
                double itemPrice = Double.parseDouble(obj.getItemPrice());
                totalLoadedInValue += loadedIn * itemPrice;
                totalInventoryCreditValue += inventoryCredit * itemPrice;
                double varianceValue = Double.parseDouble(obj.getItemVariance()) * itemPrice;
                jsonArray.put(obj.getItemVariance() + "         " + String.valueOf(varianceValue));
                totalVarianceQty += Double.parseDouble(obj.getItemVariance());
                totalVarianceAmount += varianceValue;
                jData.put(jsonArray);
            }
            JSONObject totalObj = new JSONObject();
            totalObj.put("INVOICE CREDIT", "+" + String.valueOf(totalInventoryCredit));
            totalObj.put("LOADED IN", "+" + String.valueOf(totalLoadedIn));  //Summation of all
            totalObj.put("-----VARIANCE----- QTY         AMOUNT", String.valueOf(totalVarianceQty) + "         " + String.valueOf(totalVarianceAmount));  //Summation of all
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);
            mainArr.put("damagevariance", "+" + String.valueOf(totalLoadedInValue));
            mainArr.put("TOTAL_DAMAGE_VALUE", "+" + String.valueOf(totalInventoryCreditValue));
            /*JSONArray jData1 = new JSONArray();
            jData1.put("14020106");
            jData1.put("Test Material");
            jData1.put("+10");
            jData1.put("+9");
            jData1.put("+12");
            jData1.put("-1         +12");

            JSONArray jData2 = new JSONArray();
            jData2.put("14020106");
            jData2.put("Test Material");
            jData2.put("+10");
            jData2.put("+9");
            jData2.put("+12");
            jData2.put("-1         +12");

            JSONArray jData3 = new JSONArray();
            jData3.put("14020106");
            jData3.put("Test Material");
            jData3.put("+10");
            jData3.put("+9");
            jData3.put("+12");
            jData3.put("-1         +12");


            jData.put(jData1);
            jData.put(jData2);
            jData.put(jData3);*/
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("DATA", jData);
            jsonObject.put("HEADERS", HEADERS);
            jsonObject.put("TOTAL", totalObj);
            JSONArray jDataNew = new JSONArray();
            jDataNew.put(jsonObject);
            mainArr.put("data", jData);
            // mainArr.put("tcData",jData);
            //  mainArr.put("creditData",jData);

            /*mainArr.put("data",jData);
            mainArr.put("data",jData);
            mainArr.put("data",jData);
*/
            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);
            jArr.put(HEADERS);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return jArr;
    }

    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }


    public JSONArray createPrintData() {
        JSONArray jArr = new JSONArray();
        try {
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST, App.UNLOAD);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO", "1234");
            mainArr.put("DOCUMENT NO", "80001234");  //Load Summary No
            mainArr.put("ORDERNO", "80001234");  //Load Summary No
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("supervisorname", "-");
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            mainArr.put("invheadermsg", "HAPPY NEW YEAR");
            mainArr.put("LANG", "en");
            mainArr.put("invoicepaymentterms", "2");
            mainArr.put("invoicenumber", "1300000001");
            mainArr.put("INVOICETYPE", "SALES INVOICE");
            String arabicCustomer = "اللولو هايبر ماركت";
            mainArr.put("CUSTOMER", "LULU HYPER MARKET" + "-" + arabicCustomer);
            mainArr.put("ADDRESS", "3101, 21st Street, Riyadh");
            mainArr.put("ARBADDRESS", "");
            mainArr.put("displayupc", "0");
            mainArr.put("invoicepriceprint", "1");
            mainArr.put("SUB TOTAL", "1000");
            mainArr.put("INVOICE DISCOUNT", "20");
            mainArr.put("NET SALES", "980");

            mainArr.put("availvalue", "+1000");
            mainArr.put("unloadvalue", "+2000");

            //mainArr.put("Load Number","1");


            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();

//            1.	S.NO				- Serial Number
//            2.	ITEM#				- Item Code
//            3.	DESCRIPTION			- Item Description
//            4.	OPENING STOCK (CTN)		- Opening Stock from yesterday’s closing Stock
//            5.	LOAD RECEIVED	(CTN)		- Load received from Today’s Loadsheet
//            6.	TOTAL LOAD (CTN)		- OPENING STOCK + LOAD RECEIVED + GR Stock
//            7.	ENDING LOAD (CTN)		- TOTAL LOAD – Sales
//            8.	BAD RETURN (CTN)		- Quantity of Bad Return
//            9.	TRUCK SPOILS (CTN)		- Quantity of Truck Spoils
//            10.	ACTUAL ENDING LOAD (CTN)	- ENDING LOAD – BAD RETURN – TRUCK SPOILS
//            11.	ENDING VALUE			- Value of each line item for ACTUAL ENDING LOAD


            HEADERS.put("S.NO");
            HEADERS.put("ITEM#");
            HEADERS.put("DESCRIPTION");
            HEADERS.put("OPENING_STOCK CTN    PCS");
            HEADERS.put("LOAD_RECEIVED CTN    PCS");
            HEADERS.put("TOTAL_LOAD CTN    PCS");
            HEADERS.put("ENDING_LOAD CTN    PCS");
            HEADERS.put("BAD_RETURN CTN    PCS");
            HEADERS.put("TRUCK_SPOILS CTN    PCS");
            HEADERS.put("ACTUAL_ENDING_LOAD CTN    PCS");
            HEADERS.put("ENDING VALUE");

            mainArr.put("HEADERS", HEADERS);


            JSONArray jData = new JSONArray();
//            double totalEndingInventory = 0;
//            double totalfreshUnload = 0;
//            double totalTruckDamange = 0;
//            double totalVanStock = 0;
//            double totalBadReturns = 0;
//            double totalVariance = 0;
//            double totalVarianceAmount = 0;
            double totalEndingInventoryValue = 0;

            for (UnloadSummaryPrint obj : printUnloadList) {
                JSONArray data = new JSONArray();
                data.put("" + (jData.length() + 1));//S.NO
                data.put(obj.getItemNo());//ITEM#
                data.put(UrlBuilder.decodeString(obj.getItemDescription()));//DESCRIPTION

                String deno = "0";
                HashMap<String, String> altMap = new HashMap<>();
                altMap.put(db.KEY_UOM, "");
                altMap.put(db.KEY_DENOMINATOR, "");
                HashMap<String, String> filtera = new HashMap<>();
                filtera.put(db.KEY_MATERIAL_NO, obj.getItemNo());
                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                if (altUOMCursor.getCount() > 0) {
                    altUOMCursor.moveToFirst();
                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                }

                int intCasesopen = 0;
                int intpcsopen = 0;
                int intCasesEndInv = 0;
                int intpcsEndInv = 0;
                int intCasesgood = 0;
                int intpcsgood = 0;
                int intCasesEndLoad = 0;
                int intpcsEndLoad = 0;
                int intCasesSale = 0;
                int intpcsSale = 0;
                int intCasesBR = 0;
                int intpcsBR = 0;
                int intCasesSpoils = 0;
                int intpcsSpoils = 0;
                int intCasesStock = 0;
                int intpcsStock = 0;
                try {

                    if (Double.parseDouble(deno) > 0) {
                        if (Double.parseDouble(obj.getBeginLoad()) >= Double.parseDouble(deno)) {
                            double Allitem = Double.parseDouble(obj.getBeginLoad()) / Double.parseDouble(deno);
                            intCasesopen = (int) Allitem;
                            intpcsopen = (int) Double.parseDouble(obj.getBeginLoad()) - (intCasesopen * Integer.parseInt(deno));
                        } else {
                            intpcsopen = (int) Double.parseDouble(obj.getBeginLoad());
                        }
                    } else {
                        intpcsopen = (int) Double.parseDouble(obj.getBeginLoad());
                    }
                    data.put(String.valueOf(intCasesopen) + "    " + String.valueOf(intpcsopen));//ENDING LOAD (CTN)
                } catch (Exception e) {
                    e.printStackTrace();
                    data.put(String.valueOf(0) + "    " + String.valueOf(0));//ENDING LOAD (CTN)
                }

                try {

                    if (Double.parseDouble(deno) > 0) {
                        if (Double.parseDouble(obj.getGoodReturns()) >= Double.parseDouble(deno)) {
                            double Allitem = Double.parseDouble(obj.getGoodReturns()) / Double.parseDouble(deno);
                            intCasesgood = (int) Allitem;
                            intpcsgood = (int) Double.parseDouble(obj.getGoodReturns()) - (intCasesgood * Integer.parseInt(deno));
                        } else {
                            intpcsgood = (int) Double.parseDouble(obj.getGoodReturns());
                        }
                    } else {
                        intpcsgood = (int) Double.parseDouble(obj.getGoodReturns());
                    }

                    if (Double.parseDouble(deno) > 0) {
                        if (Double.parseDouble(obj.getTodayLoad()) >= Double.parseDouble(deno)) {
                            double Allitem = Double.parseDouble(obj.getTodayLoad()) / Double.parseDouble(deno);
                            intCasesEndInv = (int) Allitem;
                            intpcsEndInv = (int) Double.parseDouble(obj.getTodayLoad()) - (intCasesEndInv * Integer.parseInt(deno));
                        } else {
                            intpcsEndInv = (int) Double.parseDouble(obj.getTodayLoad());
                        }
                    } else {
                        intpcsEndInv = (int) Double.parseDouble(obj.getTodayLoad());
                    }

                    data.put(String.valueOf(intCasesEndInv) + "    " + String.valueOf(intpcsEndInv));//LOAD RECEIVED (CTN)
                    data.put(String.valueOf(intCasesEndInv + intCasesopen + intCasesgood) + "    " + String.valueOf(intpcsEndInv + intpcsopen + intpcsgood));//TOTAL LOAD (CTN)
                } catch (Exception e) {
                    e.printStackTrace();
                    data.put(String.valueOf(0) + "    " + String.valueOf(0));//LOAD RECEIVED (CTN)
                    data.put(String.valueOf(0) + "    " + String.valueOf(0));//TOTAL LOAD (CTN)
                }
                //totalEndingInventory += Double.parseDouble(obj.getEndingInventory());

                try {
                    if (Double.parseDouble(deno) > 0) {
                        if (Double.parseDouble(obj.getSalesUnit()) >= Double.parseDouble(deno)) {
                            double Allitem = Double.parseDouble(obj.getSalesUnit()) / Double.parseDouble(deno);
                            intCasesSale = (int) Allitem;
                            intpcsSale = (int) Double.parseDouble(obj.getSalesUnit()) - (intCasesSale * Integer.parseInt(deno));
                        } else {
                            intpcsSale = (int) Double.parseDouble(obj.getSalesUnit());
                        }
                    } else {
                        intpcsSale = (int) Double.parseDouble(obj.getSalesUnit());
                    }
                    intCasesEndLoad = (intCasesEndInv + intCasesopen + intCasesgood) - intCasesSale;
                    intpcsEndLoad = (intpcsEndInv + intpcsopen + intpcsgood) - intpcsSale;

                    data.put(String.valueOf(intCasesEndLoad) + "    " + String.valueOf(intpcsEndLoad));//ENDING LOAD (CTN)
                } catch (Exception e) {
                    e.printStackTrace();
                    data.put(String.valueOf(0) + "    " + String.valueOf(0));//ENDING LOAD (CTN)
                }
                //totalfreshUnload += Double.parseDouble(obj.getFreshUnload());


                try {
                    if (Double.parseDouble(deno) > 0) {
                        if (Double.parseDouble(obj.getBadReturns()) >= Double.parseDouble(deno)) {
                            double Allitem = Double.parseDouble(obj.getBadReturns()) / Double.parseDouble(deno);
                            intCasesBR = (int) Allitem;
                            intpcsBR = (int) Double.parseDouble(obj.getBadReturns()) - (intCasesBR * Integer.parseInt(deno));
                        } else {
                            intpcsBR = (int) Double.parseDouble(obj.getBadReturns());
                        }
                    } else {
                        intpcsBR = (int) Double.parseDouble(obj.getBadReturns());
                    }
                    data.put(String.valueOf(intCasesBR) + "    " + String.valueOf(intpcsBR));//BAD RETURN (CTN)
                } catch (Exception e) {
                    e.printStackTrace();
                    data.put(String.valueOf(0) + "    " + String.valueOf(0));//BAD RETURN (CTN)
                }
                //totalBadReturns += Double.parseDouble(obj.getBadReturns());

                try {
                    if (Double.parseDouble(deno) > 0) {
                        if (Double.parseDouble(obj.getTruckDamage()) >= Double.parseDouble(deno)) {
                            double Allitem = Double.parseDouble(obj.getTruckDamage()) / Double.parseDouble(deno);
                            intCasesSpoils = (int) Allitem;
                            intpcsSpoils = (int) Double.parseDouble(obj.getTruckDamage()) - (intCasesSpoils * Integer.parseInt(deno));
                        } else {
                            intpcsSpoils = (int) Double.parseDouble(obj.getTruckDamage());
                        }
                    } else {
                        intpcsSpoils = (int) Double.parseDouble(obj.getTruckDamage());
                    }
                    data.put(String.valueOf(intCasesSpoils) + "    " + String.valueOf(intpcsSpoils));//TRUCK SPOILS (CTN)
                } catch (Exception e) {
                    e.printStackTrace();
                    data.put(String.valueOf(0) + "    " + String.valueOf(0));//TRUCK SPOILS (CTN)
                }
                //totalTruckDamange += Double.parseDouble(obj.getTruckDamage());

                try {
                    intCasesStock = intCasesEndLoad - (intCasesBR + intCasesSpoils);
                    intpcsStock = intpcsEndLoad - (intpcsBR + intpcsSpoils);
                    data.put(String.valueOf(intCasesStock) + "    " + String.valueOf(intpcsStock));//ACTUAL ENDING LOAD (CTN)
                } catch (Exception e) {
                    e.printStackTrace();
                    data.put(String.valueOf(0) + "    " + String.valueOf(0));//ACTUAL ENDING LOAD (CTN)
                }


                //totalVanStock += Double.parseDouble(obj.getVanStock());
                //totalVariance += Double.parseDouble(obj.getVarianceQty());
                //totalVarianceAmount += Double.parseDouble(obj.getVarianceQty())*Double.parseDouble(obj.getItemPrice());

                double caseprice = (intCasesStock * Double.parseDouble(deno)) * Double.parseDouble(obj.getItemPrice());
                double unitprice = intpcsStock * Double.parseDouble(obj.getItemPrice());

                data.put(String.format("%.2f", caseprice + unitprice));//ENDING VALUE
                totalEndingInventoryValue += Double.parseDouble(obj.getEndingInventory()) * Double.parseDouble(obj.getItemPrice());
                jData.put(data);
            }

            JSONObject totalObj = new JSONObject();
//            totalObj.put("INVENTORY CALCULATED",String.valueOf(totalEndingInventory));
//            totalObj.put("RETURN TO STOCK",String.valueOf(totalfreshUnload));  //Summation of all
//            totalObj.put("TRUCK SPOILS",String.valueOf(totalTruckDamange));  //Truck Damage
//            totalObj.put("ACTUAL ON TRUCK",String.valueOf(totalVanStock));  //Truck Damage
//            totalObj.put("BAD RTRNS",String.valueOf(totalBadReturns));  //Bad Returns
//            totalObj.put("----VARIANCE---- QTY        AMNT"," " + String.valueOf(totalVariance) + "       " + String.valueOf(totalVarianceAmount));
//            totalObj.put("ENDING INV.VALUE",String.valueOf(totalEndingInventoryValue));

            mainArr.put("closevalue", "+" + String.format("%.2f", totalEndingInventoryValue));
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);


            mainArr.put("data", jData);

            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);

            jArr.put(HEADERS);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return jArr;
    }
}
