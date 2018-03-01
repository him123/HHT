package com.ae.benchmark.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.models.Sales;
import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.activities.CustomerDetailActivity;
import com.ae.benchmark.activities.MapActivity;
import com.ae.benchmark.activities.SelectCustomerActivity;
import com.ae.benchmark.adapters.CustomerStatusAdapter;
import com.ae.benchmark.adapters.DataExpandAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.data.DriverRouteFlags;
import com.ae.benchmark.data.OrderReasons;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.CustomerStatus;
import com.ae.benchmark.models.Reasons;
import com.ae.benchmark.utils.Callback;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.OTPGenerator;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

/**
 * Created by eheuristic on 12/2/2016.
 */
public class VisitAllFragment extends Fragment implements View.OnFocusChangeListener, View.OnKeyListener, TextWatcher {


    public static DataExpandAdapter dataAdapter;
    private ArrayList<CustomerStatus> arrayList = new ArrayList<>();
    private ArrayAdapter<CustomerStatus> adapter;
    private ArrayList<Reasons> reasonsList = new ArrayList<>();
    DatabaseHandler db;
//    ArrayList<CustomerData> dataArrayList;

    public static ExpandableListView listView;
    public static LinearLayout llArea;
    FloatingActionButton fab;
    View view;
    ArrayList<CustomerHeader> customers = new ArrayList<>();
    android.location.Location myLocation = null;
    App.DriverRouteControl flag = new App.DriverRouteControl();

    private EditText mPinFirstDigitEditText;
    private EditText mPinSecondDigitEditText;
    private EditText mPinThirdDigitEditText;
    private EditText mPinForthDigitEditText;
    private EditText mPinFifthDigitEditText;
    private EditText mPinSixthDigitEditText;
    private EditText mPinHiddenEditText;

    private String accessCodeEntered = "";
//    SwipeRefreshLayout swipe_refresh_layout;

    private static VisitAllFragment instance = null;

    public VisitAllFragment() {
        instance = this;
    }

    public static synchronized VisitAllFragment getInstance() {

        if (instance == null) {
            instance = new VisitAllFragment();
        }

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            view = inflater.inflate(R.layout.visitall_fragment, container, false);
            flag = DriverRouteFlags.get();
            /*swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

            swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipe_refresh_layout.setRefreshing(true);
                    ((SelectCustomerActivity) getActivity()).refreshData(swipe_refresh_layout);

                }
            });*/

            new com.ae.benchmark.google.Location(getActivity(), new Callback() {
                @Override
                public void callbackSuccess(android.location.Location location) {
                    myLocation = location;
                }

                @Override
                public void callbackFailure() {
                }
            });
            reasonsList = OrderReasons.get();
            db = DatabaseHandler.getInstance(getActivity());
            listView = (ExpandableListView) view.findViewById(R.id.journeyPlanList);
//            llArea = (LinearLayout) view.findViewById(R.id.llArea);
            fab = (FloatingActionButton) view.findViewById(R.id.fab);
            if (Const.dataArrayList.size() > 0) {

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


//                adapter = new CustomerStatusAdapter(getActivity(),arrayList);
//                loadCustomerStatus();
            }

            adapter = new CustomerStatusAdapter(getActivity(), arrayList);
            loadCustomerStatus();
            final TreeMap<String, List<Customer>> map = new TreeMap<String, List<Customer>>();
            final List<String> header = new ArrayList<>();

            for (final Customer data : Const.dataArrayList) {
                Log.e("Customer allCustomer:", "id : " + data.getCustomerID() + " Area " + data.getArea());
                if (data.getArea() != null) {
                    if (!data.getArea().equals("")) {
                        addToMap(map,data.getArea().replace("%20"," "), data);
                        if(!header.contains(data.getArea().replace("%20"," "))){
                            header.add(data.getArea().replace("%20"," "));
                        }
                    } else {
                        addToMap(map,"ZOthers", data);
                        if(!header.contains("ZOthers")){
                            header.add("ZOthers");
                        }
                    }

                } else {
                    addToMap(map,"ZOthers", data);
                    if(!header.contains("ZOthers")){
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
            dataAdapter = new DataExpandAdapter(getActivity().getBaseContext(), header,map,listView,"VisitAll");
            listView.setAdapter(dataAdapter);

            /*try {
                int pos = header.size() - 1;
                listView.expandGroup(pos,true);
            }catch (Exception e){
                e.printStackTrace();
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
    public void viewCustomer(final Customer customer,boolean isClick){
        if(isClick){

            Const.customerDetail = customer;
            Const.paySource = customer.getPaysource();
            SelectCustomerActivity.distributionCustomerNo = customer.getCustomerID();

            if (customerFlagExist(customer)) {
                loadCustomerFlag(customer);
                if (App.CustomerRouteControl.isVerifyGPS()) {
                    if (verifyGPS(customer, App.CustomerRouteControl.getThresholdLimit())) {
                        showStatusDialog(customer);
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Message")
                                .setMessage(getString(R.string.coordinate_mismatch_msg))
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.continue_lbl), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        showAccessCode(customer);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                } else {
                    showStatusDialog(customer);
                }
            } else {
                Log.e("I dont have Flag", "Flag");
                App.CustomerRouteControl obj = new App.CustomerRouteControl();
                obj.setThresholdLimit("99");
                obj.setIsVerifyGPS(false);
                obj.setIsEnableIVCopy(true);
                obj.setIsDelayPrint(true);
                obj.setIsEditOrders(true);
                obj.setIsEditInvoice(true);
                obj.setIsReturns(true);
                obj.setIsDamaged(true);
                obj.setIsSignCapture(true);
                obj.setIsReturnCustomer(true);
                obj.setIsCollection(true);
                showStatusDialog(customer);
            }
        }else{
            List<CustomerHeader> customerdata = db.getCustomerDetails(DatabaseHandler.CUSTOMER_HEADER, customer.getCustomerID());
            for (CustomerHeader c : customerdata) {
                String log = "Trip id: " + c.getTripId() + " ,Lat: " + c.getLatitude() + " ,Long: " + c.getLongitude();
                Log.d("Name: ", log);
                System.out.println(log);

                App.Latitude = c.getLatitude().toString().replaceAll("%20", "");
                App.Longitude = c.getLongitude().toString().replaceAll("%20", "");

            }
            Intent i = new Intent(getActivity(), MapActivity.class);
            startActivity(i);
        }

    }

    public void addToMap(TreeMap<String, List<Customer>> map, String key, Customer value){
        if(!map.containsKey(key)){
            map.put(key, new ArrayList<Customer>());
        }
        map.get(key).add(value);
    }
    private void showStatusDialog(final Customer customer) {
        final Dialog dialog = new Dialog(getActivity());
        //dialog.setTitle(getString(R.string.shop_status));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getActivity().getLayoutInflater().inflate(R.layout.activity_select_customer_status, null);
        Button cancel = (Button) view.findViewById(R.id.btnCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ListView lv = (ListView) view.findViewById(R.id.statusList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (customer.isNewCustomer()) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    //filter.put(db.KEY_CUSTOMER_IN_TIMESTAMP, Helpers.getCurrentTimeStamp());
                    filter.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    filter.put(db.KEY_IS_VISITED, App.IS_NOT_COMPLETE);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(db.KEY_VISIT_SERVICED_REASON, arrayList.get(position).getReasonCode());
                    map.put(db.KEY_CUSTOMER_IN_TIMESTAMP, Helpers.getCurrentTimeStamp());
                    map.put(db.KEY_IS_VISITED, App.IS_COMPLETE);
                    map.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    db.updateData(db.VISIT_LIST, map, filter);

                    //Visit List Posting
                    HashMap<String, String> newMap = new HashMap<String, String>();
                    newMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    newMap.put(db.KEY_START_TIMESTAMP, Helpers.getCurrentTimeStamp());
                    newMap.put(db.KEY_VISITLISTID, Helpers.generateVisitList(db));
                    newMap.put(db.KEY_VISIT_SERVICED_REASON, arrayList.get(position).getReasonCode());
                    int activityID = 0;

                    //Check if any Activity for Customer
                    String activityId = "";
                    HashMap<String, String> activityMap = new HashMap<String, String>();
                    activityMap.put(db.KEY_ACTIVITY_ID, "");
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    if (db.checkData(db.VISIT_LIST_POST, filterMap)) {
                        Cursor c = db.getData(db.VISIT_LIST_POST, activityMap, filterMap);
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            if (c.getCount() == 1) {
                                activityId = c.getString(c.getColumnIndex(db.KEY_ACTIVITY_ID));
                            } else {
                                do {
                                    activityId = c.getString(c.getColumnIndex(db.KEY_ACTIVITY_ID));
                                }
                                while (c.moveToNext());
                            }
                        }
                    }
                    if (!activityId.equals("")) {
                        activityID = Integer.parseInt(activityId);
                    }
                    newMap.put(db.KEY_ACTIVITY_ID, String.valueOf(++activityID));
                    newMap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    newMap.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    newMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    newMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);

                    db.addData(db.VISIT_LIST_POST, newMap);
                } else {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    //filter.put(db.KEY_CUSTOMER_IN_TIMESTAMP, Helpers.getCurrentTimeStamp());
                    filter.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    filter.put(db.KEY_IS_VISITED, App.IS_NOT_COMPLETE);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(db.KEY_VISIT_SERVICED_REASON, arrayList.get(position).getReasonCode());
                    map.put(db.KEY_CUSTOMER_IN_TIMESTAMP, Helpers.getCurrentTimeStamp());
                    map.put(db.KEY_IS_VISITED, App.IS_COMPLETE);
                    map.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    db.updateData(db.VISIT_LIST, map, filter);

                    //Visit List Posting
                    HashMap<String, String> newMap = new HashMap<String, String>();
                    newMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                    newMap.put(db.KEY_START_TIMESTAMP, Helpers.getCurrentTimeStamp());
                    newMap.put(db.KEY_VISITLISTID, customer.getVisitListID());
                    newMap.put(db.KEY_VISIT_SERVICED_REASON, arrayList.get(position).getReasonCode());
                    int activityID = 0;

                    //Check if any Activity for Customer
                    String activityId = "";
                    HashMap<String, String> activityMap = new HashMap<String, String>();
                    activityMap.put(db.KEY_ACTIVITY_ID, "");
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    if (db.checkData(db.VISIT_LIST_POST, filterMap)) {
                        Cursor c = db.getData(db.VISIT_LIST_POST, activityMap, filterMap);
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            if (c.getCount() == 1) {
                                activityId = c.getString(c.getColumnIndex(db.KEY_ACTIVITY_ID));
                            } else {
                                do {
                                    activityId = c.getString(c.getColumnIndex(db.KEY_ACTIVITY_ID));
                                }
                                while (c.moveToNext());
                            }
                        }
                    }
                    if (!activityId.equals("")) {
                        activityID = Integer.parseInt(activityId);
                    }
                    newMap.put(db.KEY_ACTIVITY_ID, String.valueOf(++activityID));
                    newMap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                    newMap.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
                    newMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    newMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                    //newMap.put(db.KEY_CUSTOMER_TYPE,customer.getPaymentMethod());
                    db.addData(db.VISIT_LIST_POST, newMap);
                }

                /*if (db.checkData(db.VISIT_LIST, filter)) {
                } else {
                    db.addData(db.VISIT_LIST, map);
                }*/
                /*if (customerFlagExist(customer)) {
                    loadCustomerFlag(customer);
                } else {
                    App.CustomerRouteControl obj = new App.CustomerRouteControl();
                    obj.setThresholdLimit("99");
                    obj.setIsVerifyGPS(false);
                    obj.setIsEnableIVCopy(true);
                    obj.setIsDelayPrint(true);
                    obj.setIsEditOrders(true);
                    obj.setIsEditInvoice(true);
                    obj.setIsReturns(true);
                    obj.setIsDamaged(true);
                    obj.setIsSignCapture(true);
                    obj.setIsReturnCustomer(true);
                    obj.setIsCollection(true);
                }*/
                Intent intent = new Intent(getActivity(), CustomerDetailActivity.class);
                intent.putExtra("headerObj", customer);
                intent.putExtra("msg", "visit");
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void loadCustomerStatus() {
        try {
            if (reasonsList.size() > 0) {
                for (Reasons reason : reasonsList) {
                    CustomerStatus status = new CustomerStatus();
                    if (reason.getReasonType().equals(App.VisitReasons)) {
                        status.setReasonCode(reason.getReasonID());
                        //Log.e("STATUS","" + status.getReasonDescription());
                        if (Settings.getString(App.LANGUAGE).equals("en")) {
                            status.setReasonDescription(UrlBuilder.decodeString(reason.getReasonDescription()));
                        } else {
                            status.setReasonDescription(reason.getReasonDescriptionAr());
                        }
                        if (status.getReasonCode().contains("V")) {
                            arrayList.add(status);
                        }

                    }
                }
            }
        /*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&*/
            //Logic added coz status is not downloaded for a lot of drivers
            /*******Changes made on 24/05/2017******************************/
            /*******Changes by Rakshit Doshi *******************************/
            /*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&*/
            else {
                CustomerStatus status = new CustomerStatus();
                status.setReasonCode("V1");
                if (Settings.getString(App.LANGUAGE).equals("en")) {
                    status.setReasonDescription("Shop is Open");
                } else {
                    status.setReasonDescription("المحل مفتوح");
                }
                arrayList.add(status);
            }
            /*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&*/
            /*****************End of Changes *******************************/
            /*&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&*/

            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private boolean checkIfinSequence(Customer customer) {
        String itemNo = customer.getCustomerItemNo();
        int prevItemNo = Integer.parseInt(itemNo) - 1;
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_ITEMNO, StringUtils.leftPad(String.valueOf(prevItemNo), 3, "0"));
        map.put(db.KEY_IS_VISITED, App.IS_COMPLETE);
        if (itemNo.equals("001")) {
            return true;
        } else {
            Log.e("MAP", "" + map);
            if (db.checkData(db.VISIT_LIST, map)) {
                return true;
            } else {
                return false;
            }
        }

        // return false;
    }

    public boolean customerFlagExist(Customer customer) {
        HashMap<String, String> filter = new HashMap<>();
        filter.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
        return db.checkData(db.CUSTOMER_FLAGS, filter);
    }

    public void loadCustomerFlag(Customer customer) {
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_TRIP_ID, "");
        map.put(db.KEY_CUSTOMER_NO, "");
        map.put(db.KEY_THRESHOLD_LIMIT, "");
        map.put(db.KEY_VERIFYGPS, "");
        map.put(db.KEY_GPS_SAVE, "");
        map.put(db.KEY_ENABLE_INVOICE, "");
        map.put(db.KEY_ENABLE_DELAY_PRINT, "");
        map.put(db.KEY_ENABLE_EDIT_ORDERS, "");
        map.put(db.KEY_ENABLE_EDIT_INVOICE, "");
        map.put(db.KEY_ENABLE_RETURNS, "");
        map.put(db.KEY_ENABLE_DAMAGED, "");
        map.put(db.KEY_ENABLE_SIGN_CAPTURE, "");
        map.put(db.KEY_ENABLE_RETURN, "");
        map.put(db.KEY_ENABLE_AR_COLLECTION, "");
        map.put(db.KEY_ENABLE_POS_EQUI, "");
        map.put(db.KEY_ENABLE_SUR_AUDIT, "");
        HashMap<String, String> filter = new HashMap<>();
        filter.put(db.KEY_CUSTOMER_NO, customer.getCustomerID());
        Cursor cursor = db.getData(db.CUSTOMER_FLAGS, map, filter);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            App.CustomerRouteControl obj = new App.CustomerRouteControl();
            obj.setThresholdLimit(cursor.getString(cursor.getColumnIndex(db.KEY_THRESHOLD_LIMIT)));
            obj.setIsVerifyGPS(cursor.getString(cursor.getColumnIndex(db.KEY_VERIFYGPS)).equals("0") ? false : true);
            obj.setIsEnableIVCopy(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_INVOICE)).equals("0") ? false : true);
            obj.setIsDelayPrint(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_DELAY_PRINT)).equals("0") ? false : true);
            obj.setIsEditOrders(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_EDIT_ORDERS)).equals("0") ? false : true);
            obj.setIsEditInvoice(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_EDIT_INVOICE)).equals("0") ? false : true);
            obj.setIsReturns(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_RETURNS)).equals("0") ? false : true);
            obj.setIsDamaged(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_DAMAGED)).equals("0") ? false : true);
            obj.setIsSignCapture(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_SIGN_CAPTURE)).equals("0") ? false : true);
            obj.setIsReturnCustomer(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_RETURN)).equals("0") ? false : true);
            obj.setIsCollection(cursor.getString(cursor.getColumnIndex(db.KEY_ENABLE_AR_COLLECTION)).equals("0") ? false : true);
        }

    }

    public boolean verifyGPS(Customer customer, String thresHoldLimit) {

        String customerLatitude = UrlBuilder.decodeString(customer.getLatitude()).equals("") ? "0.000000" : UrlBuilder.decodeString(customer.getLatitude());
        String customerLongitude = UrlBuilder.decodeString(customer.getLongitude()).equals("") ? "0.000000" : UrlBuilder.decodeString(customer.getLongitude());

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            Toast.makeText(getActivity(), "Location turned off", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String outlet_georadius = thresHoldLimit; //Distance is in metres(5445.938)
            android.location.Location customerLocation = new android.location.Location("");
            customerLocation.setLatitude(Double.parseDouble(customerLatitude));
            customerLocation.setLongitude(Double.parseDouble(customerLongitude));
            double radius = Double.parseDouble(outlet_georadius);
            try {
                float distance = myLocation.distanceTo(customerLocation);
                Log.e("Distance", "" + distance);
                Log.e("My Location", "" + myLocation.getLatitude() + "," + myLocation.getLongitude() + "/" + outlet_georadius);
                if (distance < radius) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

       /* String customerLatitude = "24.942091";
        String customerLongitude = "46.712125";*/

       /* LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled){
            Toast.makeText(getActivity(),"Location turned off",Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            String customerLatitude = "25.042429";
            String customerLongitude = "55.137817";

            String outlet_georadius = "2500"; //Distance is in metres(5445.938)

            android.location.Location customerLocation = new android.location.Location("");
            customerLocation.setLatitude(Double.parseDouble(customerLatitude));
            customerLocation.setLongitude(Double.parseDouble(customerLongitude));
            double radius = Double.parseDouble(outlet_georadius);
            float distance = myLocation.distanceTo(customerLocation);
            Log.e("Distance", "" + distance);
            if(distance<radius){
                return true;
            }
            else{
                return false;
            }
        }*/
        //return true;


    }

    public void showAccessCode(final Customer customer) {
        try {
            String accessCode = Helpers.getCurrentTimeStampAccessCode() + StringUtils.stripStart(customer.getCustomerID(), "0") + App.CUSTOMER_OUT_OF_RANGE;
            byte[] code = accessCode.getBytes();
            final String generatedCode = OTPGenerator.generateOTP(code, 1, 6, false, 1);
            Log.e("Generated code", "" + generatedCode);

            final Dialog dialog = new Dialog(getActivity());
            //dialog.setTitle(getString(R.string.shop_status));
            View view = getActivity().getLayoutInflater().inflate(R.layout.activity_access_code, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view);

            TextView tv_access_code = (TextView) view.findViewById(R.id.tv_access_code);
            tv_access_code.setText(getString(R.string.accesscode) + "\n" + accessCode);
            mPinFirstDigitEditText = (EditText) view.findViewById(R.id.pin_first_edittext);
            mPinSecondDigitEditText = (EditText) view.findViewById(R.id.pin_second_edittext);
            mPinThirdDigitEditText = (EditText) view.findViewById(R.id.pin_third_edittext);
            mPinForthDigitEditText = (EditText) view.findViewById(R.id.pin_forth_edittext);
            mPinFifthDigitEditText = (EditText) view.findViewById(R.id.pin_fifth_edittext);
            mPinSixthDigitEditText = (EditText) view.findViewById(R.id.pin_sixth_edittext);
            mPinHiddenEditText = (EditText) view.findViewById(R.id.pin_hidden_edittext);
            setPINListeners();
            final AlertDialog dialog1 = builder.create();
            Button cancel = (Button) view.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog1.dismiss();
                }
            });
            Button btn_continue = (Button) view.findViewById(R.id.btn_ok);
            btn_continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("CODE", "" + accessCodeEntered);
                    if (accessCodeEntered.equals(generatedCode)) {
                        dialog1.dismiss();
                        showStatusDialog(customer);
                    } else {
                        dialog1.dismiss();
                        showStatusDialog(customer);
                       /* Toast.makeText(getActivity(),getString(R.string.code_mismatch),Toast.LENGTH_SHORT).show();
                        showAccessCode(customer);*/
                    }
                    dialog1.dismiss();
                }
            });
            dialog1.setCancelable(false);
            dialog1.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        final int id = v.getId();
        switch (id) {
            case R.id.pin_first_edittext:

                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_second_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_third_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_forth_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;

            case R.id.pin_fifth_edittext:
                if (hasFocus) {
                    setFocus(mPinHiddenEditText);
                    showSoftKeyboard(mPinHiddenEditText);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            final int id = v.getId();
            switch (id) {
                case R.id.pin_hidden_edittext:
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (mPinHiddenEditText.getText().length() == 5)
                            mPinFifthDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 4)
                            mPinForthDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 3)
                            mPinThirdDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 2)
                            mPinSecondDigitEditText.setText("");
                        else if (mPinHiddenEditText.getText().length() == 1)
                            mPinFirstDigitEditText.setText("");

                        if (mPinHiddenEditText.length() > 0)
                            mPinHiddenEditText.setText(mPinHiddenEditText.getText().subSequence(0, mPinHiddenEditText.length() - 1));

                        return true;
                    }

                    break;

                default:
                    return false;
            }
        }

        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setDefaultPinBackground(mPinFirstDigitEditText);
        setDefaultPinBackground(mPinSecondDigitEditText);
        setDefaultPinBackground(mPinThirdDigitEditText);
        setDefaultPinBackground(mPinForthDigitEditText);
        setDefaultPinBackground(mPinFifthDigitEditText);
        setDefaultPinBackground(mPinSixthDigitEditText);
        if (s.length() == 0) {
            setFocusedPinBackground(mPinFirstDigitEditText);
            mPinFirstDigitEditText.setText("");
        } else if (s.length() == 1) {
            setFocusedPinBackground(mPinSecondDigitEditText);
            mPinFirstDigitEditText.setText(s.charAt(0) + "");
            mPinSecondDigitEditText.setText("");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            mPinFifthDigitEditText.setText("");
            mPinSixthDigitEditText.setText("");
        } else if (s.length() == 2) {
            setFocusedPinBackground(mPinThirdDigitEditText);
            mPinSecondDigitEditText.setText(s.charAt(1) + "");
            mPinThirdDigitEditText.setText("");
            mPinForthDigitEditText.setText("");
            mPinFifthDigitEditText.setText("");
            mPinSixthDigitEditText.setText("");
        } else if (s.length() == 3) {
            setFocusedPinBackground(mPinForthDigitEditText);
            mPinThirdDigitEditText.setText(s.charAt(2) + "");
            mPinForthDigitEditText.setText("");
            mPinFifthDigitEditText.setText("");
            mPinSixthDigitEditText.setText("");
        } else if (s.length() == 4) {
            setFocusedPinBackground(mPinFifthDigitEditText);
            mPinForthDigitEditText.setText(s.charAt(3) + "");
            mPinFifthDigitEditText.setText("");
            mPinSixthDigitEditText.setText("");
        } else if (s.length() == 5) {
            setDefaultPinBackground(mPinFifthDigitEditText);
            mPinFifthDigitEditText.setText(s.charAt(4) + "");
            mPinSixthDigitEditText.setText("");
        } else if (s.length() == 6) {
            setDefaultPinBackground(mPinFifthDigitEditText);
            mPinSixthDigitEditText.setText(s.charAt(5) + "");
            accessCodeEntered = s.toString();
            hideSoftKeyboard(mPinFifthDigitEditText);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private void setDefaultPinBackground(EditText editText) {
        //setViewBackground(editText, getResources().getDrawable(R.drawable.textfield_default_holo_light));
    }

    private void setFocusedPinBackground(EditText editText) {
        //setViewBackground(editText, getResources().getDrawable(R.drawable.textfield_focused_holo_light));
    }

    private void setPINListeners() {
        mPinHiddenEditText.addTextChangedListener(this);
        mPinFirstDigitEditText.setOnFocusChangeListener(this);
        mPinSecondDigitEditText.setOnFocusChangeListener(this);
        mPinThirdDigitEditText.setOnFocusChangeListener(this);
        mPinForthDigitEditText.setOnFocusChangeListener(this);
        mPinFifthDigitEditText.setOnFocusChangeListener(this);
        mPinSixthDigitEditText.setOnFocusChangeListener(this);

        mPinFirstDigitEditText.setOnKeyListener(this);
        mPinSecondDigitEditText.setOnKeyListener(this);
        mPinThirdDigitEditText.setOnKeyListener(this);
        mPinForthDigitEditText.setOnKeyListener(this);
        mPinFifthDigitEditText.setOnKeyListener(this);
        mPinHiddenEditText.setOnKeyListener(this);
        mPinSixthDigitEditText.setOnKeyListener(this);
    }

    @SuppressWarnings("deprecation")
    public void setViewBackground(View view, Drawable background) {
        if (view == null || background == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    public void hideSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void showSoftKeyboard(EditText editText) {
        if (editText == null)
            return;

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);
    }

    public static void setFocus(EditText editText) {
        if (editText == null)
            return;

        editText.setFocusable(true);
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
    }

}
