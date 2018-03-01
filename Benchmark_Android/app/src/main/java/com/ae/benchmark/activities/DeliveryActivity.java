package com.ae.benchmark.activities;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.ae.benchmark.adapters.CustomerStatusAdapter;
import com.ae.benchmark.adapters.DeliveryListBadgeAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.data.OrderReasons;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.CustomerStatus;
import com.ae.benchmark.models.DeliveryOrderList;
import com.ae.benchmark.models.Reasons;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by eheuristic on 12/10/2016.
 */
/************************************************************
 @ This activity is launched when u click on the delivery button
 @ on the customer detail screen. The screen will show all the deliveries
 @ for the said day for the customer.
 ************************************************************/
public class DeliveryActivity extends AppCompatActivity {
    ImageView iv_back, iv_refresh;
    TextView tv_top_header;
    ListView list_delivery;
    DeliveryListBadgeAdapter adapter;
    FloatingActionButton flt_button;
    Customer object;
    ArrayList<CustomerHeader> customers;
    DatabaseHandler db = new DatabaseHandler(this);

    ArrayList<DeliveryOrderList> arrayList;

    LoadingSpinner loadingSpinner;
    SwipeRefreshLayout refreshLayout;
    private ArrayAdapter<CustomerStatus> statusAdapter;
    private ArrayList<CustomerStatus> statusList = new ArrayList<>();
    private ArrayList<Reasons> reasonsList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_list);
        loadingSpinner = new LoadingSpinner(this);
        try {
            Intent i = this.getIntent();
            object = (Customer) i.getParcelableExtra("headerObj");
            Helpers.logData(DeliveryActivity.this, "On Delivery Screen for Customer" + object.getCustomerID());
            customers = CustomerHeaders.get();
            arrayList = new ArrayList<>();
            refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
            new loadDeliveries().execute();
            adapter = new DeliveryListBadgeAdapter(this, arrayList);
            reasonsList = OrderReasons.get();
            statusAdapter = new CustomerStatusAdapter(this, statusList);
            loadStatus();
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
                tv_customer_name.setText(StringUtils.stripStart(object.getCustomerID(), "0") + " " + UrlBuilder.decodeString(object.getCustomerName().toString()));
                tv_customer_address.setText(object.getCustomerAddress().toString());
                tv_customer_pobox.setText("");
                tv_customer_contact.setText("");
            }
            iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
            tv_top_header = (TextView) findViewById(R.id.tv_top_header);
            iv_back.setVisibility(View.VISIBLE);
            tv_top_header.setVisibility(View.VISIBLE);
            tv_top_header.setText(getString(R.string.delivery_list));
            iv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.logData(DeliveryActivity.this, "Back Clicked on Delivery for Customer" + object.getCustomerID());
                    Intent intent = new Intent(DeliveryActivity.this, CustomerDetailActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("headerObj", object);
                    intent.putExtra("msg", "all");
                    startActivity(intent);
                    finish();
                }
            });
            flt_button = (FloatingActionButton) findViewById(R.id.flt_presale);
            flt_button.setVisibility(View.GONE);
            list_delivery = (ListView) findViewById(R.id.list_delivery);
            iv_refresh = (ImageView) findViewById(R.id.img_refresh);
            iv_refresh.setVisibility(View.GONE);

            /************************************************************
             @ Registering the listview containing delivery for context menu
             @ which will allow the driver to delete the delivery if the customer
             @ doesnt wish to take the delivery.
             ************************************************************/
            registerForContextMenu(list_delivery);
            list_delivery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    DeliveryOrderList delivery = arrayList.get(position);

                    if (delivery.getOrderStatus().equals("false")) {
                        Const.payReference = ""+ delivery.getOrderReferenceNo();
                        Helpers.logData(DeliveryActivity.this, "Const.payReference " + delivery.getOrderId()+" "+delivery.getOrderReferenceNo());
                        Intent intent = new Intent(DeliveryActivity.this, DeliveryOrderActivity.class);
                        intent.putExtra("headerObj", object);
                        intent.putExtra("delivery", delivery);
                        startActivity(intent);
                    } else if (delivery.getOrderStatus().equals("true")) {
                        Helpers.logData(DeliveryActivity.this, "Delivery Click on already completed delivery" + delivery.getOrderId());
                        Toast.makeText(getApplicationContext(), getString(R.string.delivery_complete), Toast.LENGTH_SHORT).show();
                    } else if (delivery.getOrderStatus().contains("delete")) {
                        Helpers.logData(DeliveryActivity.this, "Delivery Click on already deleted delivery" + delivery.getOrderId());
                        Toast.makeText(getApplicationContext(), getString(R.string.delivery_delete), Toast.LENGTH_SHORT).show();
                    }
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
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.list_delivery) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_delivery, menu);
        }
    }
    /************************************************************
     @ These are the context menu option which pop up if the item is long
     @ clicked. Options are
     @ 1. Remove - This will mark the delivery for deletion but will stay
                   on the list with a cross mark.
     @ 2. Cancel - This will just close the context menu
     ************************************************************/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.remove:
                /************************************************************
                 @ Once remove is clicked it will prompt if user wants to
                 @ delete the delivery or not. If the driver clicks on Yes
                 @ it will prompt to select the reason for deletion.
                 ************************************************************/
                Helpers.logData(DeliveryActivity.this, "Driver deleting delivery");
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DeliveryActivity.this);
                alertDialogBuilder.setTitle(getString(R.string.message))
                        .setMessage(getString(R.string.delete_msg))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Helpers.logData(DeliveryActivity.this, "Driver confirmed deletion of delivery" + arrayList.get(info.position).getOrderId());
                                showReasonDialog(arrayList, info.position);
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
                // showReasonDialog(arrayList, info.position);
                return true;
            case R.id.cancel:
                // edit stuff here
                Helpers.logData(DeliveryActivity.this, "Clicked Cancel from context menu");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    /************************************************************
     @ This function will load all the deliveries for the customer
     @ for the said day.
     ************************************************************/
    public class loadDeliveries extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(db.KEY_DELIVERY_NO, "");
                map.put(db.KEY_DELIVERY_DATE, "");
                map.put(db.KEY_IS_DELIVERED, "");
                map.put(db.KEY_REFERENCE_NO, "");

                HashMap<String, String> filter = new HashMap<>();
                //filter.put(db.KEY_IS_DELIVERED, "false");
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                Cursor cursor = db.getData(db.CUSTOMER_DELIVERY_HEADER, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    setDeliveryList(cursor);
                }
            } catch (Exception e) {
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
            adapter.notifyDataSetChanged();
            list_delivery.setAdapter(adapter);
        }
    }
    private void setDeliveryList(Cursor cursor) {
        try {
            ArrayList<String> temp = new ArrayList<String>();
            temp.clear();
            arrayList.clear();
            do {
                DeliveryOrderList orderList = new DeliveryOrderList();

                orderList.setOrderId(cursor.getString(cursor.getColumnIndex(db.KEY_DELIVERY_NO)));
                String date = cursor.getString(cursor.getColumnIndex(db.KEY_DELIVERY_DATE));
                String[] token = date.split("\\.");
                orderList.setOrderStatus(cursor.getString(cursor.getColumnIndex(db.KEY_IS_DELIVERED)));
                orderList.setOrderDate(Helpers.getMaskedValue(token[2], 2) + "-" + Helpers.getMaskedValue(token[1], 2) + "-" + token[0]);
                orderList.setOrderReferenceNo(cursor.getString(cursor.getColumnIndex(db.KEY_REFERENCE_NO)));

                if (!temp.contains(orderList.getOrderId())) {
                    temp.add(orderList.getOrderId());
                    arrayList.add(orderList);
                }
            }
            while (cursor.moveToNext());
        } catch (Exception e) {
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
    /************************************************************
     @ Once remove is clicked it will prompt if user wants to
     @ delete the delivery or not. If the driver clicks on Yes
     @ it will prompt to select the reason for deletion. This function
     @ will load and show the reasons in a dialog form.
     ************************************************************/
    private void showReasonDialog(ArrayList<DeliveryOrderList> list, final int position) {
        try {
            final int pos = position;
            final Dialog dialog = new Dialog(DeliveryActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //dialog.setTitle(getString(R.string.shop_status));
            View view = getLayoutInflater().inflate(R.layout.activity_select_customer_status, null);
            TextView tv = (TextView) view.findViewById(R.id.tv_top_header);
            tv.setText(getString(R.string.select_reason));
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
                    Helpers.logData(DeliveryActivity.this, "Reason Selected for deletion of delivery" + arrayList.get(pos).getOrderId() + "-"
                            + statusList.get(position).getReasonCode() + "-" + statusList.get(position).getReasonDescription());
                    deleteDeliveryItems(arrayList.get(pos).getOrderId(), statusList.get(position).getReasonCode(), statusList.get(position).getReasonDescription());
                    deleteDelivery(pos);
                    if (Helpers.isNetworkAvailable(DeliveryActivity.this)) {
                        Helpers.createBackgroundJob(DeliveryActivity.this);
                    }
                    dialog.dismiss();
                }
            });
            dialog.setContentView(view);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
    /************************************************************
     @ This function will delete the items within the delivery no and
     @ add it to the the table for posting the entries.
     ************************************************************/
    private void deleteDeliveryItems(String deliveryNo, String reasonCode, String reasonDescription) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_ID, "");
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
            filter.put(db.KEY_DELIVERY_NO, deliveryNo);
            filter.put(db.KEY_IS_DELIVERED, App.FALSE);
            Cursor cursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS, map, filter);
            if (cursor.getCount() > 0) {
                String purchaseNumber = "";
                HashMap<String, String> filter1 = new HashMap<String, String>();
                filter1.put(db.KEY_DELIVERY_NO, deliveryNo);
                filter1.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                if (db.checkData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, filter1)) {
                    HashMap<String, String> map1 = new HashMap<String, String>();
                    map1.put(db.KEY_ORDER_ID, "");
                    Cursor cursor1 = db.getData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, map1, filter1);
                    cursor1.moveToFirst();
                    purchaseNumber = cursor1.getString(cursor1.getColumnIndex(db.KEY_ORDER_ID));
                }
                do {
                    HashMap<String, String> deleteMap = new HashMap<String, String>();
                    deleteMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    deleteMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    deleteMap.put(db.KEY_DELIVERY_NO, deliveryNo);
                    deleteMap.put(db.KEY_ITEM_NO, cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                    deleteMap.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    //deleteMap.put(db.KEY_MATERIAL_DESC1,deliveryItem.getItemDescription());
                    deleteMap.put(db.KEY_CASE, cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)));
                    deleteMap.put(db.KEY_UNIT, "0");
                    deleteMap.put(db.KEY_UOM, cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                    deleteMap.put(db.KEY_REASON_CODE, reasonCode);
                    deleteMap.put(db.KEY_REASON_DESCRIPTION, reasonDescription);
                    deleteMap.put(db.KEY_ORDER_ID, purchaseNumber.equals("") ? Helpers.generateNumber(db, ConfigStore.CustomerDeliveryDelete_PR_Type) : purchaseNumber);
                    deleteMap.put(db.KEY_PURCHASE_NUMBER, purchaseNumber.equals("") ? Helpers.generateNumber(db, ConfigStore.CustomerDeliveryDelete_PR_Type) : purchaseNumber);
                    deleteMap.put(db.KEY_AMOUNT, "");
                    deleteMap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    deleteMap.put(db.KEY_IS_PRINTED, App.DATA_MARKED_FOR_POST);
                    Helpers.logData(DeliveryActivity.this, "Deleting Items for delivery" + deliveryNo + ":" + deleteMap);
                    //Adding item for delete in post
                    db.addData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, deleteMap);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
    public void deleteDelivery(int pos) {
        new returnDeliveryItems(arrayList.get(pos).getOrderId());
        //Log.e("Delete Deliver","" + arrayList.get(pos).getOrderId());
    }
    public void loadStatus() {
        for (Reasons reason : reasonsList) {
            if (reason.getReasonType().equals(App.REASON_REJECT)) {
                CustomerStatus status = new CustomerStatus();
                status.setReasonCode(reason.getReasonID());
                if (Settings.getString(App.LANGUAGE).equals("en")) {
                    status.setReasonDescription(UrlBuilder.decodeString(reason.getReasonDescription()));
                } else {
                    status.setReasonDescription(UrlBuilder.decodeString(reason.getReasonDescriptionAr()));
                }
                statusList.add(status);
            }
        }
        statusAdapter.notifyDataSetChanged();
    }

    public class returnDeliveryItems extends AsyncTask<Void, Void, Void> {
        private String deliveryID = "";
        private returnDeliveryItems(String deliveryID) {
            this.deliveryID = deliveryID;
            execute();
        }
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_ID, "");
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
                filter.put(db.KEY_DELIVERY_NO, this.deliveryID);
                Cursor cursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    returnData(cursor);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (loadingSpinner.isShowing()) {
                    loadingSpinner.hide();
                }
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(db.KEY_IS_DELIVERED, "deleted");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                filter.put(db.KEY_DELIVERY_NO, this.deliveryID);
                db.updateData(db.CUSTOMER_DELIVERY_HEADER, map, filter);
                if (Helpers.isNetworkAvailable(getApplicationContext())) {
                    Helpers.createBackgroundJob(getApplicationContext());
                }
                new loadDeliveries().execute();
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }
    /************************************************************
     @ When delivery are loaded at the start of the day, stock is
     @ blocked in the van for the delivery. Since delivery is getting
     @ deleted, the blocked stock needs to be returned back to the
     @ truck to be available for sale.
     ************************************************************/
    private void returnData(Cursor cursor) {
        try {
            Helpers.logData(DeliveryActivity.this, "Returning Data to Van Stock");
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_REMAINING_QTY_CASE, "");
                map.put(db.KEY_REMAINING_QTY_UNIT, "");
                map.put(db.KEY_RESERVED_QTY_CASE, "");
                map.put(db.KEY_RESERVED_QTY_UNIT, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                Cursor c = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    do {
                        HashMap<String, String> updateDataMap = new HashMap<>();
                        float remainingCase = 0;
                        float remainingUnit = 0;
                        float reservedCase = 0;
                        float reservedUnit = 0;
                        remainingCase = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                        remainingUnit = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                        reservedCase = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_RESERVED_QTY_CASE)));
                        reservedUnit = Float.parseFloat(c.getString(c.getColumnIndex(db.KEY_RESERVED_QTY_UNIT)));
                        if (!(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)).isEmpty() || cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)).equals("") || cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)) == null || cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)).equals("0"))) {
                            remainingCase = remainingCase + Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)));
                            reservedCase = reservedCase - Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ACTUAL_QTY)));
                        }
                        Helpers.logData(DeliveryActivity.this, "Remaining Case" + String.valueOf(remainingCase));
                        Helpers.logData(DeliveryActivity.this, "Reserved Case" + String.valueOf(reservedCase));
                        updateDataMap.put(db.KEY_REMAINING_QTY_CASE, String.valueOf(remainingCase));
                        updateDataMap.put(db.KEY_RESERVED_QTY_CASE, String.valueOf(reservedCase));
                        HashMap<String, String> filterInter = new HashMap<>();
                        filterInter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        db.updateData(db.VAN_STOCK_ITEMS, updateDataMap, filterInter);
                    }
                    while (c.moveToNext());
                }
            }
            while (cursor.moveToNext());
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
