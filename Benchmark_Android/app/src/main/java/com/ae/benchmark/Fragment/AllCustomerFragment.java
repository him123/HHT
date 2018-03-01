package com.ae.benchmark.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
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
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

/**
 * Created by eheuristic on 12/2/2016.
 */

public class AllCustomerFragment extends Fragment {

    public static DataExpandAdapter dataAdapter1;
    //    ArrayList<CustomerData> dataArrayList;
    private ArrayList<CustomerStatus> arrayList = new ArrayList<>();

    private ArrayAdapter<CustomerStatus> adapter;
    private ArrayList<Reasons> reasonsList = new ArrayList<>();
    App.DriverRouteControl flag = new App.DriverRouteControl();
    public static   ExpandableListView listView;
    //    public static LinearLayout llArea;
    FloatingActionButton fab;

    DatabaseHandler db;
    View view;
    android.location.Location myLocation = null;
    private Context context;

    private static AllCustomerFragment instance = null;

    public AllCustomerFragment() {
        instance = this;
    }

    public static synchronized AllCustomerFragment getInstance() {

        if (instance == null) {
            instance = new AllCustomerFragment();
        }

        return instance;
    }

//    SwipeRefreshLayout  swipe_refresh_layout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            view = inflater.inflate(R.layout.visitall_fragment, container, false);
            /*swipe_refresh_layout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);

            swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipe_refresh_layout.setRefreshing(true);
                    ((SelectCustomerActivity)getActivity()).refreshData(swipe_refresh_layout);

                }
            });*/


            flag = DriverRouteFlags.get();
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
//        dataArrayList =new ArrayList<>();
//        loadData();

            listView = (ExpandableListView) view.findViewById(R.id.journeyPlanList);
//            llArea = (LinearLayout) view.findViewById(R.id.llArea);
            fab = (FloatingActionButton) view.findViewById(R.id.fab);
            if(Const.allCustomerdataArrayList.size()>0){

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



            }
            adapter = new CustomerStatusAdapter(getActivity(),arrayList);
            loadCustomerStatus();

            final TreeMap<String, List<Customer>> map = new TreeMap<String, List<Customer>>();
            final List<String> header = new ArrayList<>();

            for (final Customer data : Const.allCustomerdataArrayList) {
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
            dataAdapter1 = new DataExpandAdapter(getActivity(), header,map,listView,"AllCustomer");
            listView.setAdapter(dataAdapter1);
            /*try {
                int pos = header.size() - 1;
                listView.expandGroup(pos,true);
            }catch (Exception e){
                e.printStackTrace();
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return view;
    }

    public void viewCustomer(Customer customer,boolean isClick){
        if(isClick){

            Const.customerDetail = customer;
            Const.paySource = customer.getPaysource();
            showStatusDialog(customer);
        }else{
            List<CustomerHeader> customerdata = db.getCustomerDetails(DatabaseHandler.CUSTOMER_HEADER, customer.getCustomerID());
            for (CustomerHeader c : customerdata) {
                String log = "Trip id: " + c.getTripId() + " ,Lat: " + c.getLatitude() + " ,Long: " + c.getLongitude();
                Log.d("Name: ", log);
                System.out.println(log);

                App.Latitude = c.getLatitude().toString().replaceAll("%20", "");
                App.Longitude = c.getLongitude().toString().replaceAll("%20", "");

                Toast.makeText(getActivity(), log, Toast.LENGTH_LONG).show();

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
        try {
            final Dialog dialog = new Dialog(getActivity());
            // dialog.setTitle(getString(R.string.shop_status));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View view = getActivity().getLayoutInflater().inflate(R.layout.activity_select_customer_status, null);
            ListView lv = (ListView) view.findViewById(R.id.statusList);
            Button cancel = (Button) view.findViewById(R.id.btnCancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               /* HashMap<String,String>filter = new HashMap<String, String>();
                filter.put(db.KEY_CUSTOMER_IN_TIMESTAMP, Helpers.getCurrentTimeStamp());
                filter.put(db.KEY_IS_VISITED,App.IS_COMPLETE);
                HashMap<String,String>map = new HashMap<String, String>();
                map.put(db.KEY_VISIT_SERVICED_REASON,arrayList.get(position).getReasonCode());
                map.put(db.KEY_CUSTOMER_IN_TIMESTAMP, Helpers.getCurrentTimeStamp());
                map.put(db.KEY_IS_VISITED,App.IS_COMPLETE);
                if(db.checkData(db.VISIT_LIST,filter)){
                    db.updateData(db.VISIT_LIST,map,filter);
                }
                else{
                    db.addData(db.VISIT_LIST,map);
                }*/

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
                    //newMap.put(db.KEY_CUSTOMER_TYPE,customer.getPaymentMethod());
                    db.addData(db.VISIT_LIST_POST, newMap);


                    if (customerFlagExist(customer)) {
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
                    }
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
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void loadCustomerStatus() {
        try {
            if (reasonsList.size() > 0) {
                for (Reasons reason : reasonsList) {
                    CustomerStatus status = new CustomerStatus();
                    if (reason.getReasonType().equals(App.VisitReasons)) {
                        status.setReasonCode(reason.getReasonID());
                        if (Settings.getString(App.LANGUAGE).equals("en")) {
                            status.setReasonDescription(UrlBuilder.decodeString(reason.getReasonDescription()));
                        } else {
                            status.setReasonDescription(UrlBuilder.decodeString(reason.getReasonDescriptionAr()));
                        }
                        if (status.getReasonCode().contains("V")) {
                            arrayList.add(status);
                        }
                        //status.setReasonDescription(UrlBuilder.decodeString(reason.getReasonDescription()));
                        //arrayList.add(status);
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
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public boolean verifyGPS() {

       /* String customerLatitude = "24.942091";
        String customerLongitude = "46.712125";*/

        String customerLatitude = "25.042429";
        String customerLongitude = "55.137817";

        String outlet_georadius = "5500"; //Distance is in metres(5445.938)

        android.location.Location customerLocation = new android.location.Location("");
        customerLocation.setLatitude(Double.parseDouble(customerLatitude));
        customerLocation.setLongitude(Double.parseDouble(customerLongitude));
        double radius = Double.parseDouble(outlet_georadius);
        float distance = myLocation.distanceTo(customerLocation);
        Log.e("Distance", "" + distance);
        if (distance < radius) {
            return true;
        } else {
            return false;
        }
    }

}