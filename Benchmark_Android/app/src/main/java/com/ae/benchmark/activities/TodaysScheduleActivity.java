package com.ae.benchmark.activities;

import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.CustomerInformationAdapter;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

public class TodaysScheduleActivity extends AppCompatActivity {
    ImageView iv_back;
    TextView tv_top_header;
    View view1;
    ArrayList<CustomerHeader> customers;
    DatabaseHandler db = new DatabaseHandler(this);
    LoadingSpinner loadingSpinner;
    String schedule;
    ArrayList<Customer> arrayList = new ArrayList<>();
    private ArrayAdapter<Customer> adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todays_unserviced);
        schedule = getIntent().getExtras().getString("schedule");
        customers = CustomerHeaders.get();
        //Log.e("customers","" + customers.size());
        loadingSpinner = new LoadingSpinner(this);
        //  CustomerHeader customerHeader = CustomerHeader.getCustomer(customers,object.getCustomerID());
        listView = (ListView) findViewById(R.id.listView);

        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText(schedule.equals("schedule") ? getString(R.string.today_s_schedule) :
                schedule.equals("unserviced") ? getString(R.string.today_s_unserviced) : getString(R.string.all_route_customer));
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        new loadSchedule(schedule);
    }

    public void setVisitList(Cursor cursor, String schedule) {
        try {
            Cursor customerCursor = cursor;
            switch (schedule) {
                case "schedule": {
                    do {
                        Customer customer = new Customer();
                        customer.setCustomerID(customerCursor.getString(customerCursor.getColumnIndex(db.KEY_CUSTOMER_NO)).replaceAll("%20", " "));
                        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, customer.getCustomerID());
                        if (!(customerHeader == null)) {
                            customer.setCustomerName(UrlBuilder.decodeString(customerHeader.getName1()));
                            customer.setCustomerAddress(UrlBuilder.decodeString(customerHeader.getStreet()));
                        } else {
                            customer.setCustomerName(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            customer.setCustomerAddress("");
                        }
                        arrayList.add(customer);
                    }
                    while (customerCursor.moveToNext());
                    break;
                }
                case "unserviced": {
                    do {
                        Customer customer = new Customer();
                        customer.setCustomerID(customerCursor.getString(customerCursor.getColumnIndex(db.KEY_CUSTOMER_NO)).replaceAll("%20", " "));
                        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, customer.getCustomerID());
                        if (!(customerHeader == null)) {
                            customer.setCustomerName(UrlBuilder.decodeString(customerHeader.getName1()));
                            customer.setCustomerAddress(UrlBuilder.decodeString(customerHeader.getStreet()));
                        } else {
                            customer.setCustomerName(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            customer.setCustomerAddress("");
                        }
                        Log.e("Unserviced", "" + customerCursor.getString(customerCursor.getColumnIndex(db.KEY_VISIT_UNSERVICED_REASON)));
                        if (customerCursor.getString(customerCursor.getColumnIndex(db.KEY_VISIT_UNSERVICED_REASON)) == null ||
                                customerCursor.getString(customerCursor.getColumnIndex(db.KEY_VISIT_UNSERVICED_REASON)).equals("")) {

                        } else {
                            arrayList.add(customer);
                        }

                    }
                    while (customerCursor.moveToNext());
                    break;
                }
                case "all": {
                    do {
                        Customer customer = new Customer();
                        customer.setCustomerID(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)).replaceAll("%20", " "));
                        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        if (!(customerHeader == null)) {
                            customer.setCustomerName(UrlBuilder.decodeString(customerHeader.getName1()));
                            customer.setCustomerAddress(UrlBuilder.decodeString(customerHeader.getStreet()));
                        } else {
                            customer.setCustomerName(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            customer.setCustomerAddress("");
                        }
                        arrayList.add(customer);
                    }
                    while (customerCursor.moveToNext());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public class loadSchedule extends AsyncTask<Void, Void, Void> {
        private String schedule;

        private loadSchedule(String schedule) {
            this.schedule = schedule;
            execute();
        }

        @Override
        protected void onPreExecute() {
            //loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            switch (this.schedule) {
                case "schedule": {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TRIP_ID, "");
                    map.put(db.KEY_VISITLISTID, "");
                    map.put(db.KEY_ITEMNO, "");
                    map.put(db.KEY_CUSTOMER_NO, "");
                    map.put(db.KEY_EXEC_DATE, "");
                    map.put(db.KEY_DRIVER, "");
                    HashMap<String, String> filters = new HashMap<>();
                    filters.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    // filters.put(db.KEY_IS_VERIFIED,"false");
                    Cursor cursor = db.getData(db.VISIT_LIST, map, filters);
                    if (cursor.getCount() > 0) {
                        setVisitList(cursor, schedule);
                    }
                    break;
                }
                case "unserviced": {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TRIP_ID, "");
                    map.put(db.KEY_VISITLISTID, "");
                    map.put(db.KEY_ITEMNO, "");
                    map.put(db.KEY_CUSTOMER_NO, "");
                    map.put(db.KEY_EXEC_DATE, "");
                    map.put(db.KEY_DRIVER, "");
                    map.put(db.KEY_VISIT_UNSERVICED_REASON, "");
                    HashMap<String, String> filters = new HashMap<>();
                    filters.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    // filters.put(db.KEY_IS_VERIFIED,"false");
                    Cursor cursor = db.getData(db.VISIT_LIST, map, filters);
                    if (cursor.getCount() > 0) {
                        setVisitList(cursor, schedule);
                    }
                    break;
                }
                case "all": {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TRIP_ID, "");
                    map.put(db.KEY_CUSTOMER_NO, "");
                    HashMap<String, String> filters = new HashMap<>();
                    filters.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    // filters.put(db.KEY_IS_VERIFIED,"false");
                    Cursor cursor = db.getData(db.CUSTOMER_HEADER, map, filters);
                    if (cursor.getCount() > 0) {
                        setVisitList(cursor, schedule);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /*if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }*/
            try {
                adapter = new CustomerInformationAdapter(TodaysScheduleActivity.this, arrayList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
}
