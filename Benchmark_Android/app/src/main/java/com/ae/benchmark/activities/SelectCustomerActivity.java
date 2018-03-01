package com.ae.benchmark.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.ae.benchmark.App;
import com.ae.benchmark.Fragment.AllCustomerFragment;
import com.ae.benchmark.Fragment.VisitAllFragment;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.DataAdapter;
import com.ae.benchmark.adapters.DataExpandAdapter;
import com.ae.benchmark.adapters.PagerAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.data.CustomerDelivery;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.data.DriverRouteFlags;
import com.ae.benchmark.data.OrderReasons;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.OfflineResponse;
import com.ae.benchmark.sap.SyncData;
import com.ae.benchmark.utils.Chain;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

import static com.ae.benchmark.App.TRIP_ID;

/**
 * Created by eheuristic on 12/2/2016.
 */
public class SelectCustomerActivity extends AppCompatActivity {

    public static String distributionCustomerNo="";
    ViewPager viewPager;
    TabLayout tabLayout;
    ImageView iv_back;

    DataAdapter dataAdapter;
    ArrayList<Customer> dataArrayList;
    ImageView toolbar_iv_back;
    ImageView iv_search;
    EditText et_search;
    TextView tv_top_header;
    int tab_position;
    boolean isRefresh=false;

    FloatingActionButton fab;
    FloatingActionButton addCustomer;
    public static final int MY_PERMISSIONS_LOCATION = 1;
    DatabaseHandler db ;
    public ArrayList<CustomerHeader> customers = new ArrayList<>();
    LoadingSpinner loadingSpinner;
    App.DriverRouteControl flag = new App.DriverRouteControl();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_trip);
        db = DatabaseHandler.getInstance(getApplicationContext());
        try {
            Const.creditLimitDays = "0";
            flag = DriverRouteFlags.get();
            Helpers.logData(SelectCustomerActivity.this, "At Customer Activity Screen");
            loadingSpinner = new LoadingSpinner(this);
            customers = CustomerHeaders.get();
            OrderReasons.loadData(getApplicationContext());
            dataArrayList = new ArrayList<>();
            new loadVisitList(Settings.getString(TRIP_ID));
            new loadAllCustomers(Settings.getString(TRIP_ID));
        /*viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Sequence"));
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);*/
            iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
            tv_top_header = (TextView) findViewById(R.id.tv_top_header);
            addCustomer = (FloatingActionButton)findViewById(R.id.addCustomer);
            fab = (FloatingActionButton)findViewById(R.id.float_map);
            fab.setVisibility(View.INVISIBLE);
            if(!(flag == null)){
                if(!flag.isAddCustomer()){
                    addCustomer.setEnabled(false);
                    addCustomer.setAlpha(0.5f);
                }
            }
            addCustomer.setVisibility(View.VISIBLE);
            iv_back.setVisibility(View.VISIBLE);
            tv_top_header.setVisibility(View.VISIBLE);
            tv_top_header.setText(getString(R.string.select_customer));
            iv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SelectCustomerActivity.this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });

            //loadData();
            tv_top_header = (TextView) findViewById(R.id.tv_top_header);
            if (tv_top_header != null) {
                tv_top_header.setVisibility(View.VISIBLE);
                tv_top_header.setText(getString(R.string.select_customer));
            }
            toolbar_iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
            if (toolbar_iv_back != null) {
                toolbar_iv_back.setVisibility(View.VISIBLE);
            }
            iv_search = (ImageView) findViewById(R.id.iv_search);
            if (iv_search != null) {
                iv_search.setVisibility(View.VISIBLE);
            }
            iv_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv_search.setVisibility(View.GONE);
                    et_search.setVisibility(View.VISIBLE);
                    toolbar_iv_back.setVisibility(View.GONE);
                    tv_top_header.setVisibility(View.GONE);
                }
            });

            addCustomer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SelectCustomerActivity.this, AddCustomerActivity.class);
                    startActivity(intent);
                }
            });

            et_search = (EditText) findViewById(R.id.et_search_customer);
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
                            toolbar_iv_back.setVisibility(View.VISIBLE);
                            tv_top_header.setVisibility(View.VISIBLE);
                            return true;
                        }
                    }
                    return false;
                }
            });

            toolbar_iv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SelectCustomerActivity.this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                /*finish();*/
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
                    new search().execute(s.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private class search extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {

            if (tab_position == 0) {
                    if (strings[0].length() > 0) {
                        VisitAllFragment.dataAdapter.filterData(strings[0].toString(),SelectCustomerActivity.this);
                    } else {
                        VisitAllFragment.dataAdapter.filterData("",SelectCustomerActivity.this);
                    }

            } else {
                    if (strings[0].toString().length() > 0) {
                        AllCustomerFragment.dataAdapter1.filterData(strings[0].toString(),SelectCustomerActivity.this);
                    } else {
                        AllCustomerFragment.dataAdapter1.filterData("",SelectCustomerActivity.this);
                    }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (tab_position == 0) {
                    VisitAllFragment.dataAdapter.notifyDataSetChanged();
            } else {
                    AllCustomerFragment.dataAdapter1.notifyDataSetChanged();
            }

            super.onPostExecute(s);
        }
    }

    public void refreshData(SwipeRefreshLayout swipeRefreshLayout)
    {
        if(Const.isInternet()) {
            isRefresh=true;

            Const.allCustomerdataArrayList.clear();
            Const.dataArrayList.clear();
            AllCustomerFragment.listView.setAdapter((DataExpandAdapter)null);
            VisitAllFragment.listView.setAdapter((DataExpandAdapter)null);

//            db.deleteTable(DatabaseHandler.DELIVERY_CHECK);
//            db.deleteTable(DatabaseHandler.CUSTOMER_DELIVERY_HEADER);
//            db.deleteTable(DatabaseHandler.CUSTOMER_DELIVERY_ITEMS);
            db.deleteTable(DatabaseHandler.CUSTOMER_CHECK);
            db.deleteTable(DatabaseHandler.CUSTOMER_HEADER);
            db.deleteTable(DatabaseHandler.CUSTOMER_CREDIT);
            db.deleteTable(DatabaseHandler.CUSTOMER_SALES_AREAS);
//            db.deleteTable(DatabaseHandler.COLLECTION);
            db.deleteTable(DatabaseHandler.CUSTOMER_FLAGS);
            db.deleteTable(DatabaseHandler.VISIT_LIST);

            //CustomerDelivery.load(SelectCustomerActivity.this,Settings.getString(App.TRIP_ID),db);
            CustomerHeaders.load(SelectCustomerActivity.this,Settings.getString(App.TRIP_ID), db);
            com.ae.benchmark.data.VisitList.load(SelectCustomerActivity.this,Settings.getString(App.TRIP_ID), db);

            new syncData().execute();
            swipeRefreshLayout.setRefreshing(false);
        }
        else {
            Toast.makeText(getApplicationContext(),"No Internet Available",Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class syncData extends AsyncTask<Void, Void, Void> {
        ArrayList<OfflineResponse> data = new ArrayList<>();
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            SelectCustomerActivity.this.startService(new Intent(SelectCustomerActivity.this, SyncData.class));
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                //  setSyncCount();
                loadingSpinner.hide();
            }
            CustomerHeaders.loadData(getApplicationContext());
            OrderReasons.loadData(getApplicationContext());
            customers = CustomerHeaders.get();

            new loadVisitList(Settings.getString(App.TRIP_ID));
            new loadAllCustomers(Settings.getString(App.TRIP_ID));
        }
    }
    public void setTabs(){
        try{
            viewPager = (ViewPager) findViewById(R.id.pager);
            tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.sequence).toUpperCase()));
            tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.all).toUpperCase()));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            final PagerAdapter adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount(), "s");
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                    tab_position = tab.getPosition();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(SelectCustomerActivity.this, CustomerOperationsMapActivity.class);
                    startActivity(intent);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public class setVisitList extends AsyncTask<Void, Void, Void> {
        Cursor visitListCursor;
        boolean isVisitList;

        private setVisitList(Cursor visitListCursor, boolean isVisitList) {
            this.visitListCursor = visitListCursor;
            this.isVisitList = isVisitList;
            execute();
        }

        @Override
        protected void onPreExecute() {
//            loadingSpinner.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                Cursor cursor = visitListCursor;
                cursor.moveToFirst();
                Log.e("Cursor count", "" + cursor.getCount());
                if (isVisitList) {
                    dataArrayList.clear();
                    ArrayList<Customer> data = new ArrayList<>();
                    do {
                        Customer customer = new Customer();
                        customer.setCustomerID(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)).replaceAll("%20"," "));
                        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));

                        /*String strZPreferred=db.getzPreferredData(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        customer.setZpreferred(strZPreferred);
                        String strArea=db.getArea(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        customer.setArea(strArea);
                        String strPaysource=db.getPaysource(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        customer.setPaysource(strPaysource);*/

                        HashMap<String,String> MapArea = new HashMap<>();
                        MapArea.put(db.KEY_PREFERRED,"");
                        MapArea.put(db.KEY_AREA,"");
                        MapArea.put(db.KEY_PAYSOURCE,"");
                        HashMap<String,String>newFilterArea = new HashMap<>();
                        newFilterArea.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        Cursor cArea = db.getData(db.CUSTOMER_DELIVERY_HEADER, MapArea, newFilterArea);

                        if(cArea.getCount()>0){
                            cArea.moveToFirst();
                            customer.setZpreferred(""+cArea.getString(cArea.getColumnIndex(db.KEY_PREFERRED)));
                            customer.setArea(""+cArea.getString(cArea.getColumnIndex(db.KEY_AREA)));
                            customer.setPaysource(""+cArea.getString(cArea.getColumnIndex(db.KEY_PAYSOURCE)));
                        }else{
                            customer.setZpreferred("");
                            customer.setArea("");
                            customer.setPaysource("");
                        }

                        if(!(customerHeader==null)){
                            customer.setCustomerName(customerHeader.getName1());
                            customer.setCustomer_name_ar(customerHeader.getName3());
                            customer.setCustomerAddress(UrlBuilder.decodeString(customerHeader.getStreet()));
                            customer.setLatitude(customerHeader.getLatitude());
                            customer.setLongitude(customerHeader.getLongitude());

                        } else {
                            customer.setCustomerName(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            customer.setCustomer_name_ar(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            customer.setCustomerAddress("");
                            customer.setLatitude("0.000000");
                            customer.setLongitude("0.000000");
                        }

                        HashMap<String,String>newMap = new HashMap<>();
                        newMap.put(db.KEY_TERMS,"");
                        newMap.put(db.KEY_PHONE_NO,"");
                        HashMap<String,String>newFilter = new HashMap<>();
                        newFilter.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        Cursor c = db.getData(db.CUSTOMER_HEADER, newMap, newFilter);
                        String paymentTerm = "";
                        String phonenumber="";
                        if(c.getCount()>0){
                            c.moveToFirst();
                            paymentTerm = c.getString(c.getColumnIndex(db.KEY_TERMS));
                            phonenumber = c.getString(c.getColumnIndex(db.KEY_PHONE_NO));
                        }
                        customer.setPhoneNumber(phonenumber);
                        Log.e("Payment Term1", "" + paymentTerm + customer.getCustomerID()+" -"+phonenumber);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));

                        HashMap<String, String> collectionMap = new HashMap<>();
                        collectionMap.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        collectionMap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                        HashMap<String, String> collectionMap1 = new HashMap<>();
                        collectionMap1.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        collectionMap1.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);


                        if (paymentTerm.equals(App.CASH_CUSTOMER_CODE)) {
                            customer.setPaymentMethod(App.CASH_CUSTOMER);
                        } else if (paymentTerm.equals(App.TC_CUSTOMER_CODE)) {
                            customer.setPaymentMethod(App.TC_CUSTOMER);
                        } else if (!paymentTerm.equals("")) {
                            Log.e("Here", "Here");
                            if (db.checkData(db.CUSTOMER_CREDIT, map)) {
                                Log.e("Credit Exist", "Credit Exist");
                                customer.setPaymentMethod(App.CREDIT_CUSTOMER);
                            } else {
                                //customer.setPaymentMethod(App.CASH_CUSTOMER);
                                customer.setPaymentMethod(App.CREDIT_CUSTOMER);
                            }
                        } else {
                            customer.setPaymentMethod(App.CASH_CUSTOMER);
                        }

                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_DELIVERED, App.FALSE);
                        filter.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));

                        customer.setOpenDelivery(db.checkData(db.CUSTOMER_DELIVERY_HEADER, filter));
                        customer.setOrder(db.checkData(db.ORDER_REQUEST, map));
                        customer.setSale(db.checkData(db.CAPTURE_SALES_INVOICE, map));
                        customer.setCollection(db.checkData(db.COLLECTION, collectionMap) || db.checkData(db.COLLECTION, collectionMap1));
                        customer.setMerchandize(false);
                        customer.setDelivery(db.checkData(db.CUSTOMER_DELIVERY_ITEMS_POST, map) || db.checkData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, map));
                        customer.setNewCustomer(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(db.KEY_IS_NEW_CUSTOMER))));
                        customer.setCustomerItemNo(cursor.getString(cursor.getColumnIndex(db.KEY_ITEMNO)));
                        customer.setVisitListID(cursor.getString(cursor.getColumnIndex(db.KEY_VISITLISTID)));
                        data.add(customer);
                    }
                    while (cursor.moveToNext());
                    cursor.close();
                    Const.dataArrayList = data;
                    Log.e("Data Array", "" + Const.dataArrayList.size());
                } else {
                    dataArrayList.clear();
                    ArrayList<Customer> data = new ArrayList<>();
                    do {
                        Customer customer = new Customer();
                        //Log.e("Cursor count", "" + cursor.getCount());
                        customer.setCustomerID(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)).replaceAll("%20"," "));
                        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));

                        /*String strZPreferred=db.getzPreferredData(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        customer.setZpreferred(strZPreferred);
                        String strArea=db.getArea(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        customer.setArea(strArea);
                        String strPaysource=db.getPaysource(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        customer.setPaysource(strPaysource);*/

                        HashMap<String,String> MapArea = new HashMap<>();
                        MapArea.put(db.KEY_PREFERRED,"");
                        MapArea.put(db.KEY_AREA,"");
                        MapArea.put(db.KEY_PAYSOURCE,"");
                        HashMap<String,String>newFilterArea = new HashMap<>();
                        newFilterArea.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        Cursor cArea = db.getData(db.CUSTOMER_DELIVERY_HEADER, MapArea, newFilterArea);

                        if(cArea.getCount()>0){
                            cArea.moveToFirst();
                            customer.setZpreferred(""+cArea.getString(cArea.getColumnIndex(db.KEY_PREFERRED)));
                            customer.setArea(""+cArea.getString(cArea.getColumnIndex(db.KEY_AREA)));
                            customer.setPaysource(""+cArea.getString(cArea.getColumnIndex(db.KEY_PAYSOURCE)));
                        }else{
                            customer.setZpreferred("");
                            customer.setArea("");
                            customer.setPaysource("");
                        }

                        if(!(customerHeader==null)){
                            customer.setCustomerName(customerHeader.getName1());
                            customer.setCustomerAddress(UrlBuilder.decodeString(customerHeader.getStreet()));
                            customer.setLatitude(customerHeader.getLatitude());
                            customer.setLongitude(customerHeader.getLongitude());
                        } else {
                            customer.setCustomerName(cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            customer.setCustomerAddress("");
                            customer.setLatitude("0.000000");
                            customer.setLongitude("0.000000");
                        }
                        HashMap<String,String>newMap = new HashMap<>();
                        newMap.put(db.KEY_TERMS,"");
                        newMap.put(db.KEY_PHONE_NO,"");
                        HashMap<String,String>newFilter = new HashMap<>();
                        newFilter.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        Cursor c = db.getData(db.CUSTOMER_HEADER, newMap, newFilter);
                        String paymentTerm = "";
                        String phonenumber="";
                        if(c.getCount()>0){
                            c.moveToFirst();
                            paymentTerm = c.getString(c.getColumnIndex(db.KEY_TERMS));
                            phonenumber = c.getString(c.getColumnIndex(db.KEY_PHONE_NO));
                        }
                        customer.setPhoneNumber(phonenumber);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        if (paymentTerm.equals(App.CASH_CUSTOMER_CODE)) {
                            customer.setPaymentMethod(App.CASH_CUSTOMER);
                        } else if (paymentTerm.equals(App.TC_CUSTOMER_CODE)) {
                            customer.setPaymentMethod(App.TC_CUSTOMER);
                        } else if (!paymentTerm.equals("")) {
                            if (db.checkData(db.CUSTOMER_CREDIT, map)) {
                                Log.e("Credit Exist", "Credit Exist");
                                customer.setPaymentMethod(App.CREDIT_CUSTOMER);
                            } else {
                                // customer.setPaymentMethod(App.CASH_CUSTOMER);
                                customer.setPaymentMethod(App.CREDIT_CUSTOMER);
                            }
                        } else {
                            customer.setPaymentMethod(App.CASH_CUSTOMER);
                        }
                        //Log.e("Where the F",""+db.checkData(db.CUSTOMER_DELIVERY_ITEMS_POST, map));

                        HashMap<String, String> collectionMap = new HashMap<>();
                        collectionMap.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        collectionMap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                        HashMap<String, String> collectionMap1 = new HashMap<>();
                        collectionMap1.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        collectionMap1.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);

                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_DELIVERED, App.FALSE);
                        filter.put(db.KEY_CUSTOMER_NO, cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));

                        customer.setOpenDelivery(db.checkData(db.CUSTOMER_DELIVERY_HEADER, filter));

                        customer.setOrder(db.checkData(db.ORDER_REQUEST, map));
                        customer.setSale(db.checkData(db.CAPTURE_SALES_INVOICE, map));
                        customer.setCollection(db.checkData(db.COLLECTION, collectionMap) || db.checkData(db.COLLECTION, collectionMap1));
                        customer.setMerchandize(false);
                        customer.setDelivery(db.checkData(db.CUSTOMER_DELIVERY_ITEMS_POST, map) || db.checkData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, map));
                        // customer.setCustomerItemNo(cursor.getString(cursor.getColumnIndex(db.KEY_ITEMNO)));
                        data.add(customer);
                    }
                    while (cursor.moveToNext());
                    cursor.close();
                    Const.allCustomerdataArrayList = data;
                    Log.e("All Array", "" + Const.allCustomerdataArrayList.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /*if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }*/
            if(isVisitList) {
                if(VisitAllFragment.dataAdapter!=null) {

                    new shortList(true);

                }
            }else {
                if (!isRefresh) {
                    setTabs();
                }

                if (AllCustomerFragment.dataAdapter1 != null) {

                    new shortList(false);
                }
            }
        }
    }

    private class shortList extends AsyncTask<Void, Void, Void> {
        boolean isVisit;

        final TreeMap<String, List<Customer>> map;
        final List<String> header;

        private shortList(boolean isVisit) {
            //Log.e("CALLED","CALLED");
            this.isVisit = isVisit;
            map = new TreeMap<String, List<Customer>>();
            header = new ArrayList<>();
            execute();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            if(isVisit) {
                Collections.sort(Const.dataArrayList, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer lhs, Customer rhs) {
                        return (lhs.isOpenDelivery() != rhs.isOpenDelivery()) ? (lhs.isOpenDelivery()) ? -1 : 1 : 0;
                    }
                });

                Collections.sort(Const.dataArrayList, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer lhs, Customer rhs) {
                        return rhs.getZpreferred().compareTo(lhs.getZpreferred());
                    }
                });

                Log.v("VisitAllFragmentSize", Const.dataArrayList.size() + "-");


                for (final Customer data : Const.dataArrayList) {
                    Log.e("Customer allCustomer:", "id : " + data.getCustomerID() + " Area " + data.getArea());
                    if (data.getArea() != null) {
                        if (!data.getArea().equals("")) {
                            addToMap(map, data.getArea().replace("%20", " "), data);
                            if (!header.contains(data.getArea().replace("%20", " "))) {
                                header.add(data.getArea().replace("%20", " "));
                            }
                        } else {
                            addToMap(map, "ZOthers", data);
                            if (!header.contains("ZOthers")) {
                                header.add("ZOthers");
                            }
                        }

                    } else {
                        addToMap(map, "ZOthers", data);
                        if (!header.contains("ZOthers")) {
                            header.add("ZOthers");
                        }
                    }
                }
                Collections.sort(header, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
            }else{
                Collections.sort(Const.allCustomerdataArrayList, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer lhs, Customer rhs) {
                        return (lhs.isOpenDelivery() != rhs.isOpenDelivery()) ? (lhs.isOpenDelivery()) ? -1 : 1 : 0;
                    }
                });

                Collections.sort(Const.allCustomerdataArrayList, new Comparator<Customer>() {
                    @Override
                    public int compare(Customer lhs, Customer rhs) {
                        return rhs.getZpreferred().compareTo(lhs.getZpreferred());
                    }
                });


                Log.v("AllCustomerFragmentSize", Const.allCustomerdataArrayList.size() + "-");

                for (final Customer data : Const.allCustomerdataArrayList) {
                    Log.e("Customer allCustomer:", "id : " + data.getCustomerID() + " Area " + data.getArea());
                    if (data.getArea() != null) {
                        if (!data.getArea().equals("")) {
                            addToMap(map, data.getArea().replace("%20", " "), data);
                            if (!header.contains(data.getArea().replace("%20", " "))) {
                                header.add(data.getArea().replace("%20", " "));
                            }
                        } else {
                            addToMap(map, "ZOthers", data);
                            if (!header.contains("ZOthers")) {
                                header.add("ZOthers");
                            }
                        }

                    } else {
                        addToMap(map, "ZOthers", data);
                        if (!header.contains("ZOthers")) {
                            header.add("ZOthers");
                        }
                    }
                }
                Collections.sort(header, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            if(isVisit) {
                VisitAllFragment.dataAdapter = new DataExpandAdapter(SelectCustomerActivity.this, header, map, VisitAllFragment.listView, "VisitAll");
                VisitAllFragment.listView.setAdapter(VisitAllFragment.dataAdapter);
            }else{
                AllCustomerFragment.dataAdapter1 = new DataExpandAdapter(SelectCustomerActivity.this, header, map, AllCustomerFragment.listView, "AllCustomer");
                AllCustomerFragment.listView.setAdapter(AllCustomerFragment.dataAdapter1);
            }
        }
    }

    private class loadVisitList extends AsyncTask<Void, Void, Void> {
        String tripId;
        Cursor cursor;
        private loadVisitList(String tripId) {
            //Log.e("CALLED","CALLED");
            this.tripId = tripId;
            execute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_TRIP_ID, "");
                map.put(db.KEY_VISITLISTID, "");
                map.put(db.KEY_ITEMNO, "");
                map.put(db.KEY_CUSTOMER_NO, "");
                map.put(db.KEY_EXEC_DATE, "");
                map.put(db.KEY_DRIVER, "");
                map.put(db.KEY_VP_TYPE, "");
                map.put(db.KEY_IS_ORDER_CAPTURED, "");
                map.put(db.KEY_IS_DELIVERY_CAPTURED, "");
                map.put(db.KEY_IS_SALES_CAPTURED, "");
                map.put(db.KEY_IS_COLLECTION_CAPTURED, "");
                map.put(db.KEY_IS_MERCHANDIZE_CAPTURED, "");
                map.put(db.KEY_IS_ORDER_POSTED, "");
                map.put(db.KEY_IS_DELIVERY_POSTED, "");
                map.put(db.KEY_IS_SALES_POSTED, "");
                map.put(db.KEY_IS_COLLECTION_POSTED, "");
                map.put(db.KEY_IS_MERCHANDIZE_POSTED, "");
                map.put(db.KEY_IS_VISITED, "");
                map.put(db.KEY_IS_NEW_CUSTOMER, "");
                HashMap<String, String> filters = new HashMap<>();
                filters.put(db.KEY_TRIP_ID, Settings.getString(TRIP_ID));
                // filters.put(db.KEY_IS_VERIFIED,"false");
                cursor = db.getData(db.VISIT_LIST, map, filters);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } finally {
                //db.close();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /*if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }*/
            if (cursor.getCount() > 0) {
                new setVisitList(cursor, true);
            }
        }
    }
    private class loadAllCustomers extends AsyncTask<Void, Void, Void> {
        String tripId;
        Cursor cursor;
        private loadAllCustomers(String tripId) {
            this.tripId = tripId;
            execute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_TRIP_ID, "");
                map.put(db.KEY_CUSTOMER_NO, "");
                HashMap<String, String> filters = new HashMap<>();
                filters.put(db.KEY_TRIP_ID, Settings.getString(TRIP_ID));
                // filters.put(db.KEY_IS_VERIFIED,"false");
                cursor = db.getData(db.CUSTOMER_HEADER, map, filters);
                Log.v("size",cursor.getCount()+"-");

            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            } finally {
//                db.close();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingSpinner.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /*if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }*/

            if (cursor.getCount() > 0) {
                new setVisitList(cursor, false);
            }
        }
    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }

    public void addToMap(TreeMap<String, List<Customer>> map, String key, Customer value){
        if(!map.containsKey(key)){
            map.put(key, new ArrayList<Customer>());
        }
        map.get(key).add(value);
    }

    public void downloadData(final String tripId){
        Helpers.logData(SelectCustomerActivity.this,"Downloading user data");
        db = new DatabaseHandler(SelectCustomerActivity.this);
        db.getWritableDatabase();

//        HashMap<String, String> map = new HashMap<>();
//        map.put(db.KEY_IS_BEGIN_DAY, App.FALSE);
//        map.put(db.KEY_IS_LOAD_VERIFIED, App.FALSE);
//        map.put(db.KEY_IS_END_DAY,App.FALSE);
//        map.put(db.KEY_IS_UNLOAD,App.FALSE);
//        db.addData(db.LOCK_FLAGS, map);

        Chain chain = new Chain(new Chain.Link(){
            @Override
            public void run() {
                go();
            }
        });

        chain.setFail(new Chain.Link() {
            @Override
            public void run() throws Exception {
                fail();
            }
        });

        chain.add(new Chain.Link() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CustomerHeaders.load(SelectCustomerActivity.this, tripId, db);
                        CustomerDelivery.load(SelectCustomerActivity.this, tripId, db);
                    }
                });
            }
        });

        chain.add(new Chain.Link(){
            @Override
            public void run() {

            }
        });
        chain.start();

    }

    private void go() {
        loadingSpinner.hide();
        Intent intent = new Intent(this, SelectCustomerActivity.class);
        startActivity(intent);
        finish();
    }

    private void fail() {
        if(loadingSpinner.isShowing()){
            loadingSpinner.hide();
            finish();
        }
    }
}