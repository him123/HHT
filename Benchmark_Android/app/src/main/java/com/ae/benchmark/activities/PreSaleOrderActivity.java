package com.ae.benchmark.activities;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.OrderListBadgeAdapter;
import com.ae.benchmark.adapters.PresaleAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.OrderList;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

/*****************************************************************************************
 @ This activity is called when driver clicks on order request in the
 @ Customer detail screen
 ****************************************************************************************/
public class PreSaleOrderActivity extends AppCompatActivity {
    ImageView iv_back, iv_refresh;
    TextView tv_top_header;
    ListView list_delivery;
    PresaleAdapter presaleAdapterdapter;
    OrderListBadgeAdapter adapter;
    FloatingActionButton flt_presale;
    ArrayList<Integer> proceedArrayList;
    Customer object;
    ArrayList<CustomerHeader> customers;
    ArrayList<OrderList> arrayList;
    DatabaseHandler db = new DatabaseHandler(this);
    LoadingSpinner loadingSpinner;
    SwipeRefreshLayout refreshLayout;
    String customerPO="000000";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_list);
        try{
            Helpers.logData(PreSaleOrderActivity.this, "On Order Request Screen");
            Intent i = this.getIntent();
            object = (Customer) i.getParcelableExtra("headerObj");
            loadingSpinner = new LoadingSpinner(this);
            refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

            arrayList = new ArrayList<>();

            new loadOrders().execute();
            new loadOrdersLocal().execute();

            adapter = new OrderListBadgeAdapter(this, arrayList);

            Const.constantsHashMap.clear();
            customers = CustomerHeaders.get();
            CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, object.getCustomerID());
            TextView tv_customer_name = (TextView) findViewById(R.id.tv_customer_id);
            TextView tv_customer_address = (TextView) findViewById(R.id.tv_customer_address);
            TextView tv_customer_pobox = (TextView) findViewById(R.id.tv_customer_pobox);
            TextView tv_customer_contact = (TextView) findViewById(R.id.tv_customer_contact);
            if (!(customerHeader == null)) {
                tv_customer_name.setText(StringUtils.stripStart(customerHeader.getCustomerNo(), "0") + " " + UrlBuilder.decodeString(customerHeader.getName1()));
                tv_customer_address.setText(UrlBuilder.decodeString(customerHeader.getStreet()));
                tv_customer_pobox.setText(getString(R.string.pobox) + " " + customerHeader.getPostCode());
                tv_customer_contact.setText(customerHeader.getPhone());
            } else {
                tv_customer_name.setText(StringUtils.stripStart(object.getCustomerID(),"0") + " " + UrlBuilder.decodeString(object.getCustomerName().toString()));
                tv_customer_address.setText(object.getCustomerAddress().toString());
                tv_customer_pobox.setText("");
                tv_customer_contact.setText("");
            }
            iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
            tv_top_header = (TextView) findViewById(R.id.tv_top_header);
            flt_presale = (FloatingActionButton) findViewById(R.id.flt_presale);
            iv_back.setVisibility(View.VISIBLE);
            tv_top_header.setVisibility(View.VISIBLE);
            tv_top_header.setText(getString(R.string.order_request));
            iv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            list_delivery = (ListView) findViewById(R.id.list_delivery);
            iv_refresh = (ImageView) findViewById(R.id.img_refresh);
            iv_refresh.setVisibility(View.INVISIBLE);
            proceedArrayList = new ArrayList<>();
            presaleAdapterdapter = new PresaleAdapter(PreSaleOrderActivity.this, R.layout.custom_delivery, proceedArrayList.size());
            //list_delivery.setAdapter(adapter);
            flt_presale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.logData(PreSaleOrderActivity.this, "Creating an order Request button clicked");
                    Log.e("Driver Dist Chan","" + Settings.getString(App.DIST_CHANNEL));
                    if(Settings.getString(App.DIST_CHANNEL).equals("10")){
                        showDialog();
                    }
                    else{
                        Intent intent = new Intent(PreSaleOrderActivity.this, PreSaleOrderProceedActivity.class);
                        intent.putExtra("from", "button");
                        intent.putExtra("headerObj", object);
                        intent.putExtra("customerpo",customerPO);
                        startActivity(intent);
                    }

                }
            });
            /********************************************************
             @ Reading details of order created locally on the device
             ********************************************************/
            list_delivery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    OrderList orderList = arrayList.get(position);
                    Helpers.logData(PreSaleOrderActivity.this, "Viewing details of order from list" + orderList.getOrderId());
                    Intent intent = new Intent(PreSaleOrderActivity.this, PreSaleOrderProceedActivity.class);
                    intent.putExtra("from", "list");
                    intent.putExtra("pos", position);
                    intent.putExtra("orderList", orderList);
                    intent.putExtra("headerObj", object);
                    startActivity(intent);
                }
            });
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (refreshLayout.isRefreshing()) {
                                refreshLayout.setRefreshing(false);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }, 2000);
                }
            });
            iv_refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchRefresh();
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public void showDialog() {
        LayoutInflater li = LayoutInflater.from(PreSaleOrderActivity.this);
        View promptsView = li.inflate(R.layout.activity_odometer_popup, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                PreSaleOrderActivity.this);
        alertDialogBuilder.setView(promptsView);
        final TextView tv_title = (TextView) promptsView
                .findViewById(R.id.textView1);
        tv_title.setText("Please enter Customer Purchase Number");
        //Reading last save odometer
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);
        // set dialog message
        Helpers.logData(PreSaleOrderActivity.this, "Capturing customer purchase number value");
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String input = userInput.getText().toString();
                                if (input.equals("")) {
                                    Helpers.logData(PreSaleOrderActivity.this, "User entered blank  value");
                                    dialog.cancel();
                                    Toast.makeText(PreSaleOrderActivity.this, getString(R.string.valid_value), Toast.LENGTH_SHORT).show();
                                } else {
                                    Helpers.logData(PreSaleOrderActivity.this, "User value" + input + "for posting");
                                    customerPO = input.trim().toString();
                                    Intent intent = new Intent(PreSaleOrderActivity.this, PreSaleOrderProceedActivity.class);
                                    intent.putExtra("from", "button");
                                    intent.putExtra("headerObj", object);
                                    intent.putExtra("customerpo", customerPO);
                                    startActivity(intent);

                                }
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
//                                Intent i=new Intent(getActivity(),DashboardActivity.class);
//                                startActivity(i);
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    public class loadOrders extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            Helpers.logData(PreSaleOrderActivity.this, "Loading already posted orders");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
            map.put(db.KEY_ORDER_ID, "");
            map.put(db.KEY_PURCHASE_NUMBER, "");
            map.put(db.KEY_DATE, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            Cursor cursor = db.getData(db.ORDER_REQUEST, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                setOrders(cursor);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            adapter = new OrderListBadgeAdapter(PreSaleOrderActivity.this, arrayList);
            list_delivery.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            //adapter.notifyDataSetChanged();
        }
    }
    public class loadOrdersLocal extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            Helpers.logData(PreSaleOrderActivity.this, "Loading marked for post orders");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
            map.put(db.KEY_ORDER_ID, "");
            map.put(db.KEY_PURCHASE_NUMBER, "");
            map.put(db.KEY_DATE, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            Cursor cursor = db.getData(db.ORDER_REQUEST, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                setOrdersLocal(cursor);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            //Log.e("ArrayList","" + arrayList.size());
            adapter = new OrderListBadgeAdapter(PreSaleOrderActivity.this, arrayList);
            list_delivery.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
    private void setOrders(Cursor cursor) {
        Helpers.logData(PreSaleOrderActivity.this, cursor.getCount() + "Orders are posted");
        try{
            ArrayList<String> temp = new ArrayList<String>();
            temp.clear();
            arrayList.clear();
            do {
                OrderList orderList = new OrderList();
                //orderList.setOrderId(cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                orderList.setOrderId(cursor.getString(cursor.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                orderList.setOrderDate(cursor.getString(cursor.getColumnIndex(db.KEY_DATE)));
                //Log.e("ORDER1", "" + orderList.getOrderId());
                if (!temp.contains(orderList.getOrderId())) {
                    temp.add(orderList.getOrderId());
                    Helpers.logData(PreSaleOrderActivity.this, "Already Posted orders are" + orderList.getOrderId());
                    arrayList.add(orderList);
                }
            }
            while (cursor.moveToNext());
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    private void setOrdersLocal(Cursor cursor) {
        Helpers.logData(PreSaleOrderActivity.this, cursor.getCount() + "Orders are marked for post");
        try{
            ArrayList<String> temp = new ArrayList<String>();
            temp.clear();
            //arrayList.clear();
            //Log.e("Cursor", "" + cursor.getCount());
            cursor.moveToFirst();
            do {
                OrderList orderList = new OrderList();
                //orderList.setOrderId(cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                orderList.setOrderId(cursor.getString(cursor.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                orderList.setOrderDate(cursor.getString(cursor.getColumnIndex(db.KEY_DATE)));

                if (!temp.contains(orderList.getOrderId())) {
                    //Log.e("ORDER", "" + orderList.getOrderId());
                    temp.add(orderList.getOrderId());
                    Helpers.logData(PreSaleOrderActivity.this, "Marked for Post orders are" + orderList.getOrderId());
                    arrayList.add(orderList);
                }
            }
            while (cursor.moveToNext());
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    public void dispatchRefresh() {
        refreshLayout.setRefreshing(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                }
            }
        }, 2000);
    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
}
