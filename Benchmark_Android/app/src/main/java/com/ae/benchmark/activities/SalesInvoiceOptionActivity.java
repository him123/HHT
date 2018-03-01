package com.ae.benchmark.activities;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.CustomerOperationAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.data.DriverRouteFlags;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by eheuristic on 12/5/2016.
 */
public class SalesInvoiceOptionActivity extends AppCompatActivity {
    GridView gridView;
    CustomerOperationAdapter adapter;
    String strText[] = {};/*{"Sales Invoice", "Invoice", "End Invoice"};*/
    int resarr[] = {R.drawable.ic_sales_invoice, R.drawable.ic_invoice, R.drawable.ic_endinvoice};
    ImageView iv_back;
    TextView tv_top_header;
    ImageView iv_updown;
    Customer object;
    String from = "";
    ArrayList<CustomerHeader> customers;
    DatabaseHandler db ;
    boolean isLimitAvailable = true;
    double totalInvoiceAmount = 0;
    double limit = 0;
    TextView tv_credit_days;
    TextView tv_credit_limit;
    TextView tv_available_limit;
    App.DriverRouteControl flag = new App.DriverRouteControl();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);
        db = DatabaseHandler.getInstance(getApplicationContext());
        flag = DriverRouteFlags.get();
        Helpers.logData(SalesInvoiceOptionActivity.this, "Came to Sales Invoice Option Screen");
        strText = new String[]{getString(R.string.sales_invoice), getString(R.string.invoice_label), getString(R.string.end_invoice)};
        TextView tv_customer_name = (TextView) findViewById(R.id.tv_customer_id);
        TextView tv_customer_address = (TextView) findViewById(R.id.tv_customer_address);
        TextView tv_customer_pobox = (TextView) findViewById(R.id.tv_customer_pobox);
        TextView tv_customer_contact = (TextView) findViewById(R.id.tv_customer_contact);
        tv_credit_days = (TextView) findViewById(R.id.tv_digits);
        tv_credit_limit = (TextView) findViewById(R.id.tv_digits1);
        tv_available_limit = (TextView) findViewById(R.id.tv_digits2);
        if (getIntent().getExtras() != null) {
            from = getIntent().getStringExtra("from");
            if (from.equals("customerdetail")) {
                Intent i = this.getIntent();
                object = (Customer) i.getParcelableExtra("headerObj");
                if (object == null) {
                    object = Const.customerDetail;
                }
                customers = CustomerHeaders.get();
                CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, object.getCustomerID());
                if (!(customerHeader == null)) {
                    tv_customer_name.setText(StringUtils.stripStart(customerHeader.getCustomerNo(), "0") + " " + UrlBuilder.decodeString(customerHeader.getName1()));
                    tv_customer_address.setText(UrlBuilder.decodeString(customerHeader.getStreet()));
                    tv_customer_pobox.setText(getString(R.string.pobox) + customerHeader.getPostCode());
                    tv_customer_contact.setText(customerHeader.getPhone());
                } else {
                    tv_customer_name.setText(StringUtils.stripStart(object.getCustomerID(),"0") + " " + UrlBuilder.decodeString(object.getCustomerName().toString()));
                    tv_customer_address.setText(object.getCustomerAddress().toString());
                    tv_customer_pobox.setText("");
                    tv_customer_contact.setText("");
                }
                if (object.getPaymentMethod().equalsIgnoreCase("cash")) {
                    tv_credit_days.setText("0");
                    tv_credit_limit.setText("0");
                    tv_available_limit.setText("0");
                } else {
                    calculateAvailableLimit();
                    /*try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, "");
                        map.put(db.KEY_CREDIT_LIMIT, "");
                        map.put(db.KEY_AVAILABLE_LIMIT, "");
                        HashMap<String, String> filters = new HashMap<>();
                        filters.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        Cursor cursor = db.getData(db.CUSTOMER_CREDIT, map, filters);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            tv_credit_days.setText("0");
                            tv_credit_limit.setText(cursor.getString(cursor.getColumnIndex(db.KEY_CREDIT_LIMIT)));
                            tv_available_limit.setText(cursor.getString(cursor.getColumnIndex(db.KEY_AVAILABLE_LIMIT)));
                            Const.availableLimit = cursor.getString(cursor.getColumnIndex(db.KEY_AVAILABLE_LIMIT));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        db.close();
                    }*/
                }
            }
        }
        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        gridView = (GridView) findViewById(R.id.grid);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        iv_updown = (ImageView) findViewById(R.id.iv_updown);
        adapter = new CustomerOperationAdapter(SalesInvoiceOptionActivity.this, strText, resarr, "SalesInvoiceOptionActivity");
        gridView.setAdapter(adapter);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        iv_updown.setVisibility(View.INVISIBLE);
        tv_top_header.setText(getString(R.string.sales_invoice_option));
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Helpers.logData(SalesInvoiceOptionActivity.this, "Came to Sales Invoice Option Screen");
                        Intent intent1 = new Intent(SalesInvoiceOptionActivity.this, SalesInvoiceActivity.class);
                        intent1.putExtra("headerObj", object);
                        startActivity(intent1);
                        break;
                    case 1:
                        if(!flag.isDisplayInvoiceSummary()){
                            Helpers.logData(SalesInvoiceOptionActivity.this, "Invoice Summary is blocked");
                            break;
                        }
                        else{
                            if(invoiceExist()){
                                Helpers.logData(SalesInvoiceOptionActivity.this, "Going for invoice summary");
                                Intent intent2 = new Intent(SalesInvoiceOptionActivity.this, InvoiceSummeryActivity.class);
                                intent2.putExtra("headerObj", object);
                                startActivity(intent2);
                                break;
                            }
                            else{
                                Helpers.logData(SalesInvoiceOptionActivity.this, "No invoice or returns exist");
                                Toast.makeText(SalesInvoiceOptionActivity.this,getString(R.string.invoice_not_exist),Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    case 2:
                        if(invoiceExist()){
                            Helpers.logData(SalesInvoiceOptionActivity.this, "End Invoice clicked");
                            Intent intent3 = new Intent(SalesInvoiceOptionActivity.this, PromotionListActivity.class);
                            intent3.putExtra("headerObj", object);
                            intent3.putExtra("from","Final Invoice");
                            startActivity(intent3);
                            break;
                        }
                        else{
                            Helpers.logData(SalesInvoiceOptionActivity.this, "No invoice or returns exist");
                            Toast.makeText(SalesInvoiceOptionActivity.this,getString(R.string.invoice_not_exist),Toast.LENGTH_SHORT).show();
                            break;
                        }

                    default:
                        break;
                }
            }
        });
    }

    private boolean invoiceExist(){
        HashMap<String,String>map = new HashMap<>();
        map.put(db.KEY_TIME_STAMP,"");
        HashMap<String,String>filter = new HashMap<>();
        filter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
        filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
        if(db.checkData(db.CAPTURE_SALES_INVOICE,filter)||db.checkData(db.RETURNS,filter)){
            return true;
        }
        else{
            return false;
        }
    }
    /**********************************************************
     @ Calculating available limit for the customer.
     @ To calculate available limit its calculated against
     @ credit limit of customer - available limit - sum of all the
     @ open items
     *********************************************************/
    private void calculateAvailableLimit() {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_CREDIT_LIMIT, "");
            map.put(db.KEY_CREDIT_DAYS, "");
            map.put(db.KEY_RECEIVABLES, "");
            HashMap<String, String> filters = new HashMap<>();
            filters.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            Cursor cursor = db.getData(db.CUSTOMER_CREDIT, map, filters);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                tv_credit_days.setText("0");
                tv_credit_limit.setText(cursor.getString(cursor.getColumnIndex(db.KEY_CREDIT_LIMIT)));
                limit = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_CREDIT_LIMIT)));
                HashMap<String, String> map1 = new HashMap<>();
                map1.put(db.KEY_CUSTOMER_NO, "");
                map1.put(db.KEY_INVOICE_NO, "");
                map1.put(db.KEY_INVOICE_AMOUNT, "");
                map1.put(db.KEY_DUE_DATE, "");
                map1.put(db.KEY_INVOICE_DATE, "");
                map1.put(db.KEY_AMOUNT_CLEARED, "");
                map1.put(db.KEY_IS_INVOICE_COMPLETE, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                Cursor c = db.getData(db.COLLECTION, map1, filter);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    do {
                        totalInvoiceAmount += Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_INVOICE_AMOUNT)));
                    }
                    while (c.moveToNext());
                }
                Log.e("Total Invoice", "" + totalInvoiceAmount);
                double availableLimit = 0.0;
                try{
                    availableLimit = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_RECEIVABLES)));
                }catch (Exception e){
                    e.printStackTrace();
                }
                /*if (limit - totalInvoiceAmount == 0) {
                    isLimitAvailable = false;
                }*/
                if (limit - availableLimit - totalInvoiceAmount == 0) {
                    isLimitAvailable = false;
                }
                // tv_available_limit.setText(String.valueOf(limit - totalInvoiceAmount));
                tv_available_limit.setText(String.format( "%.2f",(limit - availableLimit - totalInvoiceAmount)));
                tv_credit_days.setText(cursor.getString(cursor.getColumnIndex(db.KEY_CREDIT_DAYS)));
            } else {
                tv_credit_days.setText("0");
                tv_credit_limit.setText("0");
                tv_available_limit.setText("0");
                isLimitAvailable = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
}
