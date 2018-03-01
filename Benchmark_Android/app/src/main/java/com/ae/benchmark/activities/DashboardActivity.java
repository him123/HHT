package com.ae.benchmark.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ae.benchmark.utils.BackgroundSync;
import com.crashlytics.android.Crashlytics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.Print;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
/**
 * Created by Rakshit on 15-Nov-16.
 */

/************************************************************
 @ This is the dashboard screen
 ************************************************************/
public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Button btnBDay;
    TextView tv_dashboard;
    ImageView iv_drawer;
    DrawerLayout drawer;
    Button btn_message;
    Button btn_settings;
    Button btn_logout;
    private DrawerLayout mDrawerLayout;
    ExpanableListAdapterActivity mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    DatabaseHandler db;
    LoadingSpinner loadingSpinner;
    float salesCount = 0;
    float goodReturnsCount = 0;
    float badReturnsCount = 0;
    int cashCustomerCount = 0;
    int creditCustomerCount = 0;
    int tcCustomerCount = 0;
    int postCount = 0;
    TextView lbl_totalsales;
    TextView tv_route;
    TextView tv_driver_no;
    TextView lbl_totalreceipt;
    TextView tv_tripid;
    TextView tv_driver_name;
    ArrayList<CustomerHeader> customers;
    ArrayList<Print> arrayList = new ArrayList<>();
    int failCount = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        db = DatabaseHandler.getInstance(getApplicationContext());
//        Settings.setString(App.VATValue,"0.05");
//        Settings.setString(App.VATPR,"5");


        Log.v("", "========= Chk: " + Integer.valueOf(8));
        try {
            CustomerHeaders.loadData(getApplicationContext());
            customers = CustomerHeaders.get();

            Helpers.logData(DashboardActivity.this, "On Dashboard Screen");
            new loadTransactions().execute();
        /*ArticleHeaders.loadData(getApplicationContext());
        CustomerHeaders.loadData(getApplicationContext());*/
            //loadingSpinner = new LoadingSpinner(this, getString(R.string.changinglanguage));
            loadingSpinner = new LoadingSpinner(this, "");
            Helpers.loadData(getApplicationContext());
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            btnBDay = (Button) findViewById(R.id.btnBeginDay);
            tv_route = (TextView) findViewById(R.id.tv_route);
            tv_tripid = (TextView) findViewById(R.id.tv_trip_id);
            tv_driver_no = (TextView) findViewById(R.id.tv_driver_no);
            tv_dashboard = (TextView) findViewById(R.id.tv_dashboard);
            tv_dashboard.setVisibility(View.VISIBLE);
            lbl_totalsales = (TextView) findViewById(R.id.lbl_totalsales);
            lbl_totalreceipt = (TextView) findViewById(R.id.lbl_totalreceipt);
            iv_drawer = (ImageView) findViewById(R.id.iv_drawer);
            btn_message = (Button) findViewById(R.id.btn_messages);
            tv_route.setText(getString(R.string.route_code_101) + " " + Settings.getString(App.ROUTE));
            try {
                String driverName = Settings.getString(App.LANGUAGE).equals("en") ? Settings.getString(App.DRIVER_NAME_EN) : Settings.getString(App.DRIVER_NAME_AR);
                tv_driver_no.setText(getString(R.string.welcome_john_doe_1000002445) + " " + Settings.getString(App.DRIVER) + " , " + driverName);
                tv_tripid.setText(getString(R.string.trip) + " " + Settings.getString(App.TRIP_ID));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Settings.getString(App.IS_MESSAGE_DISPLAY).equals("0")) {
                btn_message.setVisibility(View.VISIBLE);
            } else {
                btn_message.setVisibility(View.GONE);
            }

            btn_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.logData(DashboardActivity.this, "Clicked on message button on side menu");
                    Intent intent = new Intent(DashboardActivity.this, CustomerMessageListActivity.class);
                    intent.putExtra("from", "dash");
                    startActivity(intent);
                }
            });
            btn_settings = (Button) findViewById(R.id.btn_settings);
            btn_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.logData(DashboardActivity.this, "Clicked on settings button on side menu");
                    mDrawerLayout.closeDrawers();
                    drawer.closeDrawers();
                    Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
            });
            btn_logout = (Button) findViewById(R.id.btn_logout);
            btn_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.logData(DashboardActivity.this, "Clicked on log out button on side menu");
                    mDrawerLayout.closeDrawers();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DashboardActivity.this);
                    alertDialogBuilder.setTitle(getString(R.string.log_out))
                            .setMessage(getString(R.string.log_out_msg))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.proceed), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Settings.setString("ITripId", null);
                                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            });
            //Load all Articles
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            expandableList = (ExpandableListView) findViewById(R.id.navigationmenu);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            if (navigationView != null) {
                setupDrawerContent(navigationView);
            }
            /************************************************************
             @ This function is for the side list menu activation or
             @ deactivation based on the application flow
             ************************************************************/
            /*prepareListData();
            mMenuAdapter = new ExpanableListAdapterActivity(this, listDataHeader, listDataChild, expandableList);
            // setting list adapter
            expandableList.setAdapter(mMenuAdapter);*/
            expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    String selectedItem = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    if (selectedItem == "Load") {
                        Intent i = new Intent(DashboardActivity.this, LoadActivity.class);
                        startActivity(i);
                    } else if (selectedItem == "Load Request") {
                        Intent i = new Intent(DashboardActivity.this, LoadRequestActivity.class);
                        startActivity(i);
                    } else if (selectedItem == "VanStock") {
                        Intent i = new Intent(DashboardActivity.this, VanStockActivity.class);
                        startActivity(i);
                    } else if (selectedItem == "Unload") {
                        Intent i = new Intent(DashboardActivity.this, UnloadActivity.class);
                        startActivity(i);
                    }
                    return false;
                }
            });
            expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v,
                                            int groupPosition, long id) {
                    Log.v("Group", "click");
                    return false;
                }
            });
            iv_drawer.setVisibility(View.VISIBLE);
            iv_drawer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawer.openDrawer(GravityCompat.START);
                }
            });
            setBeginDayVisibility();
            btnBDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.logData(DashboardActivity.this, "Clicked on Begin Day");
                    Helpers.loadData(getApplicationContext());
                    Intent i = new Intent(DashboardActivity.this, BeginTripActivity.class);
                    startActivity(i);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            });
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            NavigationView navigationView2 = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setItemIconTintList(null);
            navigationView.setNavigationItemSelectedListener(this);
            //createPieChart();
            // createLineChart();
            //createBarChart();
            new loadBarChartData(App.SALES);
            TextView lbl_targetachieved = (TextView) findViewById(R.id.lbl_targetachieved);
            lbl_targetachieved.setText("0.00/0.00");
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    /************************************************************
     @ This function is used to load total sales in terms of
     @ money that the driver created for the day. This includes
     @ summation of all the sales made in terms of order and sales
     @ invoice.
     ************************************************************/
    private void loadTotalSales() {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_ACTIVITY_TYPE, "");
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_ORDER_ID, "");
            map.put(db.KEY_PRICE, "");
            HashMap<String, String> filter = new HashMap<>();
            Cursor c = db.getData(db.DAYACTIVITY, map, filter);
            double totalSales = 0;
            if (c.getCount() > 0) {
                c.moveToFirst();
                do {
                    totalSales += Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_PRICE)));
                }
                while (c.moveToNext());
            }
            lbl_totalsales.setText(String.format("%.2f", totalSales) + " " + getString(R.string.currency));
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    /************************************************************
     @ This function is used to load amount received in terms of
     @ money that the driver collected for the day. This includes
     @ summation of all the collection from sales invoice and open
     @ items collection.
     ************************************************************/
    private void loadTotalReceipt() {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_INVOICE_NO, "");
            map.put(db.KEY_INVOICE_AMOUNT, "");
            map.put(db.KEY_DUE_DATE, "");
            map.put(db.KEY_INVOICE_DATE, "");
            map.put(db.KEY_AMOUNT_CLEARED, "");
            map.put(db.KEY_IS_INVOICE_COMPLETE, "");
            HashMap<String, String> filter = new HashMap<>();
            double totalReceipt = 0;
            Cursor c = db.getData(db.COLLECTION, map, filter);
            if (c.getCount() > 0) {
                do {
                    totalReceipt += Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                }
                while (c.moveToNext());
            }

            //Changes to add partially collected collection as well
            //Changes by Rakshit 09/04/2017
            /*HashMap<String, String> partMap = new HashMap<>();
            partMap.put(db.KEY_CUSTOMER_NO, "");
            partMap.put(db.KEY_INVOICE_NO, "");
            partMap.put(db.KEY_INVOICE_AMOUNT, "");
            partMap.put(db.KEY_DUE_DATE, "");
            partMap.put(db.KEY_INVOICE_DATE, "");
            partMap.put(db.KEY_AMOUNT_CLEARED, "");
            partMap.put(db.KEY_IS_INVOICE_COMPLETE, "");
            HashMap<String, String> partFilter = new HashMap<>();
            Cursor c1 = db.getData(db.PARTIAL_COLLECTION_TEMP, partMap, partFilter);
            Log.e("C1 Count","" + c1.getCount());
            if (c1.getCount() > 0) {
                do {
                    totalReceipt += Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                }
                while (c1.moveToNext());
            }*/
            //Changes by Rakshit 09/04/2017 -- End
            lbl_totalreceipt.setText(String.format("%.2f", totalReceipt) + " " + getString(R.string.currency));
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void setBeginDayVisibility() {
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_IS_BEGIN_DAY, "true");
        if (db.checkData(db.LOCK_FLAGS, map)) {
            /*btnBDay.setEnabled(false);
            btnBDay.setAlpha(.5f);*/
            // btnBDay.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            tv_route.setText(getString(R.string.route_code_101) + " " + Settings.getString(App.ROUTE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String driverName = Settings.getString(App.LANGUAGE).equals("en") ? Settings.getString(App.DRIVER_NAME_EN) : Settings.getString(App.DRIVER_NAME_AR);
            tv_driver_no.setText(getString(R.string.welcome_john_doe_1000002445) + " " + Settings.getString(App.DRIVER) + " , " + driverName);
            tv_tripid.setText(getString(R.string.trip) + " " + Settings.getString(App.TRIP_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        setBeginDayVisibility();
    }

    void createPieChart() {
        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4, 0));
        entries.add(new Entry(8, 1));
        entries.add(new Entry(6, 2));
        PieDataSet dataset = new PieDataSet(entries, "");
        dataset.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) value);
            }
        });
        ArrayList<String> labels = new ArrayList<String>();
        labels.add(getString(R.string.cash));
        labels.add(getString(R.string.credit));
        labels.add("TC");
        PieData data = new PieData(labels, dataset);
        List<Integer> colorCodes = new ArrayList<Integer>();
        colorCodes.add(Color.parseColor("#c15525"));
        colorCodes.add(Color.parseColor("#ffc502"));
        colorCodes.add(Color.parseColor("#ff9201"));
        dataset.setColors(colorCodes); //
        //  pieChart.setDescription("Description");
        pieChart.setDrawSliceText(false);
        pieChart.setData(data);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setDescription("");
        pieChart.getLegend().setPosition(Legend.LegendPosition.PIECHART_CENTER);
        // pieChart.setUsePercentValues(true);
        pieChart.animateY(3000);
    }

    /************************************************************
     @ This function is for the side list menu activation or
     @ deactivation based on the application flow
     ************************************************************/
    private void prepareListData() {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_IS_BEGIN_DAY, "");
            map.put(db.KEY_IS_LOAD_VERIFIED, "");
            map.put(db.KEY_IS_END_DAY, "");
            map.put(db.KEY_IS_UNLOAD, "");
            HashMap<String, String> filter = new HashMap<>();
            Cursor cursor = db.getData(db.LOCK_FLAGS, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
            }
            boolean isBeginTripEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(db.KEY_IS_BEGIN_DAY)));
            boolean isloadVerifiedEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(db.KEY_IS_LOAD_VERIFIED)));
            boolean isUnloadEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(db.KEY_IS_UNLOAD)));
            boolean isEndDayEnabled = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(db.KEY_IS_END_DAY)));

            ExpandedMenuModel beginTrip = new ExpandedMenuModel();
            beginTrip.setIconName("Start Trip");
            beginTrip.setNotificationCount("0");
            beginTrip.setIconImg(R.drawable.ic_begintrip);
            beginTrip.setIsEnabled(isEndDayEnabled ? false : true);
            listDataHeader.add(beginTrip);
            ExpandedMenuModel manageInventory = new ExpandedMenuModel();
            manageInventory.setIconName("Manage Load");
            manageInventory.setIconImg(R.drawable.ic_manageinventory);
            manageInventory.setNotificationCount("0");
            manageInventory.setIsEnabled(isBeginTripEnabled && !isEndDayEnabled ? true : false);
            //manageInventory.setIsEnabled(true);
            listDataHeader.add(manageInventory);
            ExpandedMenuModel customerOperations = new ExpandedMenuModel();
            customerOperations.setIconName("Journey Plan");
            customerOperations.setIconImg(R.drawable.ic_customeropt);
            customerOperations.setNotificationCount("0");
            customerOperations.setIsEnabled(isloadVerifiedEnabled && !isEndDayEnabled ? true : false);
            // customerOperations.setIsEnabled(true);
            listDataHeader.add(customerOperations);
            ExpandedMenuModel endTrip = new ExpandedMenuModel();
            endTrip.setIconName("Payments");
            endTrip.setIconImg(R.drawable.ic_info);
            endTrip.setNotificationCount("0");
            endTrip.setIsEnabled(isUnloadEnabled && !isEndDayEnabled ? true : false);
            //endTrip.setIsEnabled(true);
            listDataHeader.add(endTrip);
            ExpandedMenuModel driverbalance = new ExpandedMenuModel();
            driverbalance.setIconName("Route Recon");
            driverbalance.setNotificationCount("0");
            driverbalance.setIconImg(R.drawable.ic_driver_collection);
            //driverbalance.setIsEnabled(true);
            driverbalance.setIsEnabled(isEndDayEnabled ? false : true);
            listDataHeader.add(driverbalance);
            ExpandedMenuModel information = new ExpandedMenuModel();
            information.setIconName("Sales Snap");
            information.setIconImg(R.drawable.ic_info);
            information.setNotificationCount("0");
            information.setIsEnabled(true);
            listDataHeader.add(information);
            ExpandedMenuModel datapostingaudit = new ExpandedMenuModel();
            String name = getString(R.string.data_posting_audit_slide) + (failCount == 0 ? "" : "   " + String.valueOf(failCount));
            datapostingaudit.setIconName(getString(R.string.data_posting_audit_slide));
            datapostingaudit.setNotificationCount(String.valueOf(failCount));
            datapostingaudit.setIconImg(R.drawable.info_data_posting_audit);
            datapostingaudit.setIsEnabled(true);
            listDataHeader.add(datapostingaudit);
            ExpandedMenuModel catelog_data = new ExpandedMenuModel();
            catelog_data.setIconName("Catalogue");
            catelog_data.setNotificationCount("0");
            catelog_data.setIconImg(R.drawable.ic_driver_collection);
            //driverbalance.setIsEnabled(true);
            catelog_data.setIsEnabled(isEndDayEnabled ? false : true);
            listDataHeader.add(catelog_data);

            // Adding child data
            List<String> manageInventoryItems = new ArrayList<String>();
            manageInventoryItems.add(getString(R.string.load));
            manageInventoryItems.add(getString(R.string.loadrequest));
            manageInventoryItems.add(getString(R.string.vanstock));
            manageInventoryItems.add(getString(R.string.unload));
            listDataChild.put(listDataHeader.get(1), manageInventoryItems);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    void createBarChart() {
        BarChart barChart = (BarChart) findViewById(R.id.barChart);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(4f, 0));
        entries.add(new BarEntry(8f, 1));
        entries.add(new BarEntry(6f, 2));
        BarDataSet dataset = new BarDataSet(entries, "");
        // dataset.setColors(ColorTemplate.PASTEL_COLORS);
        List<Integer> colorCodes = new ArrayList<Integer>();
        colorCodes.add(Color.parseColor("#82d173"));
        colorCodes.add(Color.parseColor("#3e7aae"));
        colorCodes.add(Color.parseColor("#ff715b"));
        dataset.setColors(colorCodes);
        ArrayList<String> labels = new ArrayList<String>();
        labels.add(getString(R.string.sales));
        labels.add(getString(R.string.good_return));
        labels.add(getString(R.string.bad_return));
        BarData data = new BarData(labels, dataset);
        barChart.setData(data);
        barChart.animateY(2000);
        barChart.setDescription("");
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
    }

    /************************************************************
     @ This function is called to create create bar chart from live
     @ data. Barchart is composed of total sales, total good returns
     @ and total bad returns. These are calculated in terms of units
     @ sold for the material
     ************************************************************/
    void createBarChartFromLiveData(float salesCount, float goodreturnsCount, float badreturnsCount) {
        try {
            Log.e("Count in Live Data", "" + salesCount + "/" + goodreturnsCount + "/" + badreturnsCount);
            BarChart barChart = (BarChart) findViewById(R.id.barChart);
            barChart.setDrawBarShadow(false);
            barChart.setDrawValueAboveBar(true);
            barChart.setPinchZoom(false);
            barChart.setDrawGridBackground(false);
            ArrayList<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(salesCount, 0));
            entries.add(new BarEntry(goodreturnsCount, 1));
            entries.add(new BarEntry(badreturnsCount, 2));
        /*entries.add(new BarEntry(4f, 0));
        entries.add(new BarEntry(8f, 1));
        entries.add(new BarEntry(6f, 2));*/
            BarDataSet dataset = new BarDataSet(entries, "");
            // dataset.setColors(ColorTemplate.PASTEL_COLORS);
            List<Integer> colorCodes = new ArrayList<Integer>();
            colorCodes.add(Color.parseColor("#82d173"));
            colorCodes.add(Color.parseColor("#3e7aae"));
            colorCodes.add(Color.parseColor("#ff715b"));
            dataset.setColors(colorCodes);
            ArrayList<String> labels = new ArrayList<String>();
            labels.add(getString(R.string.sales));
            labels.add(getString(R.string.good_return));
            labels.add(getString(R.string.bad_return));
            BarData data = new BarData(labels, dataset);
            barChart.setData(data);
            barChart.animateY(2000);
            barChart.setDescription("");
            barChart.getAxisRight().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    /************************************************************
     @ This function is used to create a pie chart from data for
     @ the number of customers visited by the drivers and based on
     @ the payment type of the customer. Customer payment types are
     @ Cash
     @ Credit
     @ Temporary Credit
     ************************************************************/
    void createPieChartFromLiveData(int cashCustomerCount, int creditCustomerCount, int tcCustomerCount) {
        try {
            if (cashCustomerCount == 0 && creditCustomerCount == 0 && tcCustomerCount == 0) {
                PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
                ArrayList<Entry> entries = new ArrayList<>();
                entries.add(new Entry(1, 0));
                PieDataSet dataset = new PieDataSet(entries, "");
                dataset.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return String.valueOf((int) value);
                    }
                });
                ArrayList<String> labels = new ArrayList<String>();
                labels.add(getString(R.string.no_visit));
                // labels.add(getString(R.string.credit));
                //  labels.add(getString(R.string.tc_lbl));
                PieData data = new PieData(labels, dataset);
                List<Integer> colorCodes = new ArrayList<Integer>();
                colorCodes.add(Color.parseColor("#6e7c7c"));
                // colorCodes.add(Color.parseColor("#ffc502"));
                //  colorCodes.add(Color.parseColor("#ff9201"));
                dataset.setColors(colorCodes); //
                //  pieChart.setDescription("Description");
                pieChart.setDrawSliceText(false);
                pieChart.setData(data);
                pieChart.setHoleRadius(50f);
                pieChart.setTransparentCircleRadius(50f);
                pieChart.setDescription("");
                pieChart.getLegend().setPosition(Legend.LegendPosition.PIECHART_CENTER);
                pieChart.setDrawSliceText(false);
                pieChart.setDrawCenterText(false);
                pieChart.setCenterTextColor(Color.TRANSPARENT);
                // pieChart.setUsePercentValues(true);
                pieChart.animateY(1000);
            } else {
                PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
                ArrayList<Entry> entries = new ArrayList<>();
                entries.add(new Entry(cashCustomerCount, 0));
                entries.add(new Entry(creditCustomerCount, 1));
                entries.add(new Entry(tcCustomerCount, 2));
                PieDataSet dataset = new PieDataSet(entries, "");
                dataset.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return String.valueOf((int) value);
                    }
                });
                ArrayList<String> labels = new ArrayList<String>();
                labels.add(getString(R.string.cash));
                labels.add(getString(R.string.credit));
                labels.add(getString(R.string.tc_lbl));
                PieData data = new PieData(labels, dataset);
                List<Integer> colorCodes = new ArrayList<Integer>();
                colorCodes.add(Color.parseColor("#c15525"));
                //colorCodes.add(Color.parseColor("#0000FF"));
                colorCodes.add(Color.parseColor("#ffc502"));
                //colorCodes.add(Color.parseColor("#FF0000"));
                colorCodes.add(Color.parseColor("#ff9201"));
                //colorCodes.add(Color.parseColor("#FFC200"));
                dataset.setColors(colorCodes); //
                //  pieChart.setDescription("Description");
                pieChart.setDrawSliceText(false);
                pieChart.setData(data);
                pieChart.setHoleRadius(50f);
                pieChart.setTransparentCircleRadius(50f);
                pieChart.setDescription("");
                pieChart.getLegend().setPosition(Legend.LegendPosition.PIECHART_CENTER);
                // pieChart.setUsePercentValues(true);
                pieChart.animateY(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        }
        //super.onBackPressed();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        //revision: this don't works, use setOnChildClickListener() and setOnGroupClickListener() above instead
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        return false;
    }

    private void createBarChartData(final String var) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (var.equals(App.SALES)) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_TIME_STAMP, "");
                        map.put(db.KEY_ORG_CASE, "");
                        map.put(db.KEY_ORG_UNITS, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        HashMap<String, String> filterPostedSales = new HashMap<>();
                        filterPostedSales.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED); //Count of all invoices posted in SAP
                        HashMap<String, String> filterMarkedforPostSales = new HashMap<>();
                        filterMarkedforPostSales.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        Cursor salesMarkforPostCursor = null;
                        Cursor salesPostedCursor = null;
                        if (db.checkData(db.CAPTURE_SALES_INVOICE, filterMarkedforPostSales)) {
                            salesMarkforPostCursor = db.getData(db.CAPTURE_SALES_INVOICE, map, filterMarkedforPostSales);
                            if (salesMarkforPostCursor.getCount() > 0) {
                                salesMarkforPostCursor.moveToFirst();
                            }
                        }
                        if (db.checkData(db.CAPTURE_SALES_INVOICE, filterPostedSales)) {
                            salesPostedCursor = db.getData(db.CAPTURE_SALES_INVOICE, map, filterPostedSales);
                            if (salesPostedCursor.getCount() > 0) {
                                salesPostedCursor.moveToFirst();
                            }
                        }
                        salesCount = calculateData(var, salesPostedCursor, salesMarkforPostCursor);
                    } else if (var.equals(App.DELIVERY)) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_TIME_STAMP, "");
                        map.put(db.KEY_CASE, "");
                        HashMap<String, String> filterPostedSales = new HashMap<>();
                        filterPostedSales.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED); //Count of all invoices posted in SAP
                        HashMap<String, String> filterMarkedforPostSales = new HashMap<>();
                        filterMarkedforPostSales.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        Cursor salesMarkforPostCursor = null;
                        Cursor salesPostedCursor = null;
                        if (db.checkData(db.CUSTOMER_DELIVERY_ITEMS_POST, filterMarkedforPostSales)) {
                            salesMarkforPostCursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filterMarkedforPostSales);
                            if (salesMarkforPostCursor.getCount() > 0) {
                                salesMarkforPostCursor.moveToFirst();
                            }
                        }
                        if (db.checkData(db.CUSTOMER_DELIVERY_ITEMS_POST, filterPostedSales)) {
                            salesPostedCursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filterPostedSales);
                            if (salesPostedCursor.getCount() > 0) {
                                salesPostedCursor.moveToFirst();
                            }
                        }
                        salesCount = salesCount + calculateData(var, salesPostedCursor, salesMarkforPostCursor);
                    } else if (var.equals(App.ORDER_REQUEST)) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_TIME_STAMP, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        HashMap<String, String> filterPostedSales = new HashMap<>();
                        filterPostedSales.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED); //Count of all invoices posted in SAP
                        HashMap<String, String> filterMarkedforPostSales = new HashMap<>();
                        filterMarkedforPostSales.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        Cursor salesMarkforPostCursor = null;
                        Cursor salesPostedCursor = null;
                        if (db.checkData(db.ORDER_REQUEST, filterMarkedforPostSales)) {
                            salesMarkforPostCursor = db.getData(db.ORDER_REQUEST, map, filterMarkedforPostSales);
                            if (salesMarkforPostCursor.getCount() > 0) {
                                salesMarkforPostCursor.moveToFirst();
                            }
                        }
                        if (db.checkData(db.ORDER_REQUEST, filterPostedSales)) {
                            salesPostedCursor = db.getData(db.ORDER_REQUEST, map, filterPostedSales);
                            if (salesPostedCursor.getCount() > 0) {
                                salesPostedCursor.moveToFirst();
                            }
                        }
                        salesCount = salesCount + calculateData(var, salesPostedCursor, salesMarkforPostCursor);
                    } else if (var.equals(App.GOOD_RETURN)) {
                        HashMap<String, String> returnMap = new HashMap<>();
                        returnMap.put(db.KEY_TIME_STAMP, "");
                        returnMap.put(db.KEY_CASE, "");
                        returnMap.put(db.KEY_UNIT, "");
                        returnMap.put(db.KEY_MATERIAL_NO, "");
                        HashMap<String, String> filterPostedGoodReturn = new HashMap<>();
                        filterPostedGoodReturn.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);//Count of all good REturns posted in SAP
                        filterPostedGoodReturn.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                        HashMap<String, String> filterMarkedforPostGoodReturn = new HashMap<>();
                        filterMarkedforPostGoodReturn.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filterMarkedforPostGoodReturn.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                        Cursor goodReturnPostedCursor = null;
                        if (db.checkData(db.RETURNS, filterPostedGoodReturn)) {
                            goodReturnPostedCursor = db.getData(db.RETURNS, returnMap, filterPostedGoodReturn);
                        }
                        Cursor goodReturnMarkforPostCursor = null;
                        if (db.checkData(db.RETURNS, filterMarkedforPostGoodReturn)) {
                            goodReturnMarkforPostCursor = db.getData(db.RETURNS, returnMap, filterMarkedforPostGoodReturn);
                        }
                        goodReturnsCount = calculateData(var, goodReturnPostedCursor, goodReturnMarkforPostCursor);
                    } else if (var.equals(App.BAD_RETURN)) {
                        HashMap<String, String> returnMap = new HashMap<>();
                        returnMap.put(db.KEY_TIME_STAMP, "");
                        returnMap.put(db.KEY_CASE, "");
                        returnMap.put(db.KEY_UNIT, "");
                        returnMap.put(db.KEY_MATERIAL_NO, "");
                        HashMap<String, String> filterPostedBadReturn = new HashMap<>();
                        filterPostedBadReturn.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);//Count of all good REturns posted in SAP
                        filterPostedBadReturn.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                        HashMap<String, String> filterMarkedforPostBadReturn = new HashMap<>();
                        filterMarkedforPostBadReturn.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filterMarkedforPostBadReturn.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                        Cursor badReturnPostedCursor = null;
                        Cursor badReturnMarkforPostCursor = null;
                        if (db.checkData(db.RETURNS, filterPostedBadReturn)) {
                            badReturnPostedCursor = db.getData(db.RETURNS, returnMap, filterPostedBadReturn);
                        }
                        if (db.checkData(db.RETURNS, filterMarkedforPostBadReturn)) {
                            badReturnMarkforPostCursor = db.getData(db.RETURNS, returnMap, filterMarkedforPostBadReturn);
                        }
                        badReturnsCount = calculateData(var, badReturnPostedCursor, badReturnMarkforPostCursor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class loadBarChartData extends AsyncTask<Void, Void, Void> {
        private String var;

        private loadBarChartData(String var) {
            this.var = var;
            execute();
        }

        @Override
        protected void onPreExecute() {
            if (!loadingSpinner.isShowing()) {
                loadingSpinner.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            createBarChartData(var);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                postCount++;
                if (var.equals(App.SALES)) {
                    new loadBarChartData(App.GOOD_RETURN);
                } else if (var.equals(App.GOOD_RETURN)) {
                    new loadBarChartData(App.BAD_RETURN);
                } else if (var.equals(App.BAD_RETURN)) {
                    new loadBarChartData(App.DELIVERY);
                } else if (var.equals(App.DELIVERY)) {
                    new loadBarChartData(App.ORDER_REQUEST);
                } else {
                    if (postCount == 5) {
                        try {
                            if (loadingSpinner.isShowing()) {
                                loadingSpinner.hide();
                            }
                            createBarChartFromLiveData(salesCount, goodReturnsCount, badReturnsCount);
                            new loadPieChartData().execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }

    public class loadPieChartData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            if (!loadingSpinner.isShowing()) {
                loadingSpinner.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(db.KEY_END_TIMESTAMP, "");
            map.put(db.KEY_CUSTOMER_NO, "");
            HashMap<String, String> filter = new HashMap<>();
            // filter.put(db.KEY_IS_VISITED, App.IS_COMPLETE);
            Cursor c = db.getData(db.VISIT_LIST_POST, map, filter);
            if (c.getCount() > 0) {
                c.moveToFirst();
                createPieChart(c);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (loadingSpinner.isShowing()) {
                    loadingSpinner.hide();
                }
                loadTotalSales();
                createPieChartFromLiveData(cashCustomerCount, creditCustomerCount, tcCustomerCount);
                loadTotalReceipt();
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    private void createPieChart(final Cursor c) {
        c.moveToFirst();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> tempCust = new ArrayList<String>();
                    do {
                        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        HashMap<String, String> filter = new HashMap<String, String>();
                        filter.put(db.KEY_CUSTOMER_NO, c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                        if (!tempCust.contains(c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)))) {
                            tempCust.add(c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)));
                            if (customerHeader != null) {
                                if (customerHeader.getTerms().equals(App.CASH_CUSTOMER_CODE)) {
                                    cashCustomerCount++;
                                } else if (customerHeader.getTerms().equals(App.TC_CUSTOMER_CODE)) {
                                    tcCustomerCount++;
                                } else if (!customerHeader.getTerms().equals("")/*&&db.checkData(db.CUSTOMER_CREDIT,filter)*/) {
                                    creditCustomerCount++;
                                } else {
                                    creditCustomerCount++;
                                }
                            }
                        }
                    }
                    while (c.moveToNext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private float calculateData(String var, Cursor c1, Cursor c2) {
        try {
            float count = 0;
            if (var.equals(App.SALES)) {
                if (c1 != null) {
                    if (c1.getCount() > 0) {
                        do {
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_ORG_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_ORG_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_ORG_UNITS))) >= 1) {
                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_ORG_UNITS)));
                            }
                            Log.e("MatNo Sales", " c1 no : " + c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));
                        }
                        while (c1.moveToNext());
                    }
                }
                if (c2 != null) {
                    if (c2.getCount() > 0) {
                        do {

                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_ORG_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c2.getString(c2.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_ORG_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_ORG_UNITS))) >= 1) {
                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_ORG_UNITS)));
                            }
                        }
                        while (c2.moveToNext());
                    }
                }
            } else if (var.equals(App.GOOD_RETURN)) {
                if (c1 != null) {
                    if (c1.getCount() > 0) {
                        do {
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_UNIT)));
                            }
                        }
                        while (c1.moveToNext());
                    }
                }
                if (c2 != null) {
                    if (c2.getCount() > 0) {
                        do {
                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c2.getString(c2.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_UNIT)));
                            }
                        }
                        while (c2.moveToNext());
                    }
                }
            } else if (var.equals(App.BAD_RETURN)) {
                if (c1 != null) {
                    if (c1.getCount() > 0) {
                        do {
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_UNIT)));
                            }
                        }
                        while (c1.moveToNext());
                    }
                }
                if (c2 != null) {
                    if (c2.getCount() > 0) {
                        do {
                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c2.getString(c2.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_UNIT)));
                            }
                        }
                        while (c2.moveToNext());
                    }
                }
            } else if (var.equals(App.DELIVERY)) {
                if (c1 != null) {
                    if (c1.getCount() > 0) {
                        do {
                            count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_CASE)));
                        }
                        while (c1.moveToNext());
                    }
                }
                if (c2 != null) {
                    if (c2.getCount() > 0) {
                        do {
                            count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_CASE)));
                        }
                        while (c2.moveToNext());
                    }
                }
            } else if (var.equals(App.ORDER_REQUEST)) {
                if (c1 != null) {
                    if (c1.getCount() > 0) {
                        do {
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c1.getString(c1.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c1.getString(c1.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                count += Float.parseFloat(c1.getString(c1.getColumnIndex(db.KEY_UNIT)));
                            }
                        }
                        while (c1.moveToNext());
                    }
                }
                if (c2 != null) {
                    if (c2.getCount() > 0) {
                        do {
                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, c2.getString(c2.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_CASE))) * Float.parseFloat(deno);
                            }
                            if (Double.parseDouble(c2.getString(c2.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                count += Float.parseFloat(c2.getString(c2.getColumnIndex(db.KEY_UNIT)));
                            }
                        }
                        while (c2.moveToNext());
                    }
                }
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return 0;
        }
    }

    public class loadTransactions extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            //loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                HashMap<String, String> beginDayMap = new HashMap<>();
                beginDayMap.put(db.KEY_PURCHASE_NUMBER, "");
                beginDayMap.put(db.KEY_IS_POSTED, "");

                HashMap<String, String> odoMeterMap = new HashMap<>();
                odoMeterMap.put(db.KEY_PURCHASE_NUMBER, "");
                odoMeterMap.put(db.KEY_IS_POSTED, "");

                HashMap<String, String> lconMap = new HashMap<>();
                lconMap.put(db.KEY_ORDER_ID, "");
                lconMap.put(db.KEY_IS_POSTED, "");

                HashMap<String, String> filter = new HashMap<>();

                HashMap<String, String> bdFilter = new HashMap<>();
                bdFilter.put(db.KEY_FUNCTION, ConfigStore.BeginDayFunction);

                HashMap<String, String> edFilter = new HashMap<>();
                edFilter.put(db.KEY_FUNCTION, ConfigStore.EndDayFunction);

                HashMap<String, String> odometerFilter = new HashMap<>();
                odometerFilter.put(db.KEY_ODOMETER_TYPE, App.ODOMETER_BEGIN_DAY);

                HashMap<String, String> odometerEndFilter = new HashMap<>();
                odometerEndFilter.put(db.KEY_ODOMETER_TYPE, App.ODOMETER_END_DAY);

                Cursor beginDay = db.getData(db.BEGIN_DAY, beginDayMap, bdFilter);
                Cursor endDay = db.getData(db.BEGIN_DAY, beginDayMap, edFilter);
                Cursor odoMeter = db.getData(db.ODOMETER, odoMeterMap, odometerFilter);
                Cursor odoMeterEnd = db.getData(db.ODOMETER, odoMeterMap, odometerEndFilter);
                Cursor loadConfirmation = db.getData(db.LOAD_CONFIRMATION_HEADER, lconMap, filter);
                Cursor unloadRequest = db.getData(db.UNLOAD_TRANSACTION, beginDayMap, filter);
                if (beginDay.getCount() > 0) {
                    beginDay.moveToFirst();
                }
                if (odoMeter.getCount() > 0) {
                    odoMeter.moveToFirst();
                }
                if (loadConfirmation.getCount() > 0) {
                    loadConfirmation.moveToFirst();
                }
                if (endDay.getCount() > 0) {
                    endDay.moveToFirst();
                }
                if (odoMeterEnd.getCount() > 0) {
                    odoMeterEnd.moveToFirst();
                }
                if (unloadRequest.getCount() > 0) {
                    unloadRequest.moveToFirst();
                }
                setDriverAuditItems(beginDay, odoMeter, loadConfirmation, endDay, odoMeterEnd, unloadRequest);

                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_PURCHASE_NUMBER, "");
                map.put(db.KEY_IS_POSTED, "");
                map.put(db.KEY_CUSTOMER_NO, "");

                HashMap<String, String> gRfilter = new HashMap<>();
                gRfilter.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);

                HashMap<String, String> bRfilter = new HashMap<>();
                bRfilter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);

                HashMap<String, String> collection = new HashMap<>();
                collection.put(db.KEY_TIME_STAMP, "");
                collection.put(db.KEY_INVOICE_NO, "");
                collection.put(db.KEY_IS_POSTED, "");
                collection.put(db.KEY_CUSTOMER_NO, "");
                HashMap<String, String> collectionFilter = new HashMap<>();
                HashMap<String, String> collectionFilter1 = new HashMap<>();
                collectionFilter.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                collectionFilter1.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                Cursor invoicePosted = db.getData(db.COLLECTION, collection, collectionFilter);

                Cursor invoiceMarkPosted = db.getData(db.COLLECTION, collection, collectionFilter1);

                //HashMap<String,String>filter = new HashMap<>();
                Cursor orderRequest = db.getData(db.ORDER_REQUEST, map, filter);
                Cursor salesRequest = db.getData(db.CAPTURE_SALES_INVOICE, map, collectionFilter);
                Cursor salesRequestMarkPost = db.getData(db.CAPTURE_SALES_INVOICE, map, collectionFilter1);
                Cursor deliveryRequest = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
                Cursor goodReturn = db.getData(db.RETURNS, map, gRfilter);
                Cursor badReturn = db.getData(db.RETURNS, map, bRfilter);
                Cursor loadRequest = db.getData(db.LOAD_REQUEST, map, filter);

                if (orderRequest.getCount() > 0) {
                    orderRequest.moveToFirst();
                }
                if (salesRequest.getCount() > 0) {
                    salesRequest.moveToFirst();
                }
                if (salesRequestMarkPost.getCount() > 0) {
                    salesRequestMarkPost.moveToFirst();
                }
                if (deliveryRequest.getCount() > 0) {
                    deliveryRequest.moveToFirst();
                }
                if (goodReturn.getCount() > 0) {
                    goodReturn.moveToFirst();
                }
                if (badReturn.getCount() > 0) {
                    badReturn.moveToFirst();
                }
                if (invoicePosted.getCount() > 0) {
                    invoicePosted.moveToFirst();
                }
                if (invoiceMarkPosted.getCount() > 0) {
                    invoiceMarkPosted.moveToFirst();
                }
                setAuditItems(orderRequest, salesRequest, salesRequestMarkPost, deliveryRequest, goodReturn, badReturn, invoicePosted, invoiceMarkPosted, loadRequest);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (Print print : arrayList) {
                if (!print.isPosted()) {
                    failCount++;
                }
            }
            listDataHeader = new ArrayList<ExpandedMenuModel>();
            listDataChild = new HashMap<ExpandedMenuModel, List<String>>();
            prepareListData();
            mMenuAdapter = new ExpanableListAdapterActivity(DashboardActivity.this, listDataHeader, listDataChild, expandableList);
            // setting list adapter
            expandableList.setAdapter(mMenuAdapter);

            startService(new Intent(DashboardActivity.this, BackgroundSync.class));
        }
    }

    private void setAuditItems(Cursor cursor1, Cursor cursor2, Cursor salesMarkPost, Cursor cursor3, Cursor cursor4, Cursor cursor5, Cursor cursor6, Cursor cursor7, Cursor cursor8) {
        try {
            Cursor orderRequest = cursor1;
            Cursor salesRequest = cursor2;
            Cursor salesRequestMarkPost = salesMarkPost;
            Cursor deliveryRequest = cursor3;
            Cursor goodReturnsRequest = cursor4;
            Cursor badReturnsRequest = cursor5;
            Cursor invoicePosted = cursor6;
            Cursor invoiceMarkPosted = cursor7;
            Cursor loadRequest = cursor8;
            ArrayList<String> temp = new ArrayList<String>();
            temp.clear();
            // arrayList.clear();
            int i = 1;
            if (orderRequest.getCount() > 0) {
                orderRequest.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(orderRequest.getString(orderRequest.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(orderRequest.getString(orderRequest.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.OrderRequest_TR);
                    print.setIsPosted(orderRequest.getString(orderRequest.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }
                    //  arrayList.add(print);

                }
                while (orderRequest.moveToNext());
            }

            if (salesRequest.getCount() > 0) {
                salesRequest.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(salesRequest.getString(salesRequest.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(salesRequest.getString(salesRequest.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.SalesInvoice_TR);
                    print.setIsPosted(salesRequest.getString(salesRequest.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (salesRequest.moveToNext());
            }
            if (salesRequestMarkPost.getCount() > 0) {
                salesRequestMarkPost.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(salesRequestMarkPost.getString(salesRequestMarkPost.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(salesRequestMarkPost.getString(salesRequestMarkPost.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.SalesInvoice_TR);
                    print.setIsPosted(salesRequestMarkPost.getString(salesRequestMarkPost.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (salesRequestMarkPost.moveToNext());
            }

            if (deliveryRequest.getCount() > 0) {
                deliveryRequest.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(deliveryRequest.getString(deliveryRequest.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(deliveryRequest.getString(deliveryRequest.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.DeliveryRequest_TR);
                    print.setIsPosted(deliveryRequest.getString(deliveryRequest.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (deliveryRequest.moveToNext());
            }

            if (goodReturnsRequest.getCount() > 0) {
                goodReturnsRequest.moveToFirst();
                do {
                    Print print = new Print();
                    //print.setCustomer_id(object.getCustomerID());
                    print.setCustomer_id(i == 1 ? String.valueOf(i) : String.valueOf(i));
                    print.setCustomer_id(goodReturnsRequest.getString(goodReturnsRequest.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(goodReturnsRequest.getString(goodReturnsRequest.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.GoodReturns_TR);
                    print.setIsPosted(goodReturnsRequest.getString(goodReturnsRequest.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (goodReturnsRequest.moveToNext());
            }

            if (badReturnsRequest.getCount() > 0) {
                badReturnsRequest.moveToFirst();
                do {
                    Print print = new Print();
                    //print.setCustomer_id(object.getCustomerID());
                    print.setCustomer_id(i == 1 ? String.valueOf(i) : String.valueOf(i));
                    print.setCustomer_id(badReturnsRequest.getString(badReturnsRequest.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(badReturnsRequest.getString(badReturnsRequest.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.BadReturns_TR);
                    print.setIsPosted(badReturnsRequest.getString(badReturnsRequest.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (badReturnsRequest.moveToNext());
            }

            if (invoicePosted.getCount() > 0) {
                invoicePosted.moveToFirst();
                do {
                    Print print = new Print();
                    //print.setCustomer_id(object.getCustomerID());

                    print.setCustomer_id(i == 1 ? String.valueOf(i) : String.valueOf(i));
                    print.setCustomer_id(invoicePosted.getString(invoicePosted.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(invoicePosted.getString(invoicePosted.getColumnIndex(db.KEY_INVOICE_NO)));
                    print.setTransactionType(ConfigStore.CollectionRequest_TR);
                    print.setIsPosted(invoicePosted.getString(invoicePosted.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);
                    arrayList.add(print);
                    i++;
                /*if(!temp.contains(print.getReferenceNumber())){
                    temp.add(print.getReferenceNumber());
                    arrayList.add(print);
                    i++;
                }*/

                }
                while (invoicePosted.moveToNext());
            }

            if (invoiceMarkPosted.getCount() > 0) {
                invoiceMarkPosted.moveToFirst();
                do {
                    Print print = new Print();
                    //print.setCustomer_id(object.getCustomerID());

                    print.setCustomer_id(i == 1 ? String.valueOf(i) : String.valueOf(i));
                    print.setCustomer_id(invoiceMarkPosted.getString(invoiceMarkPosted.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    print.setReferenceNumber(invoiceMarkPosted.getString(invoiceMarkPosted.getColumnIndex(db.KEY_INVOICE_NO)));
                    print.setTransactionType(ConfigStore.CollectionRequest_TR);
                    print.setIsPosted(invoiceMarkPosted.getString(invoiceMarkPosted.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);
                    arrayList.add(print);
                    i++;
                /*if(!temp.contains(print.getReferenceNumber())){
                    temp.add(print.getReferenceNumber());
                    arrayList.add(print);
                    i++;
                }*/

                }
                while (invoiceMarkPosted.moveToNext());
            }

            if (loadRequest.getCount() > 0) {
                loadRequest.moveToFirst();
                do {
                    Print print = new Print();
                    //  print.setCustomer_id(loadRequest.getString(loadRequest.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    //print.setCustomer_id("-");
                    print.setCustomer_id(Settings.getString(App.DRIVER));
                    print.setReferenceNumber(loadRequest.getString(loadRequest.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.LoadRequest_TR);
                    print.setIsPosted(loadRequest.getString(loadRequest.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (loadRequest.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void setDriverAuditItems(Cursor cursor1, Cursor cursor2, Cursor cursor3, Cursor cursor4, Cursor cursor5, Cursor cursor6) {
        try {
            Cursor beginDay = cursor1;
            Cursor odometer = cursor2;
            Cursor loadConfirmation = cursor3;
            Cursor endDay = cursor4;
            Cursor odometerEnd = cursor5;
            Cursor unload = cursor6;

            ArrayList<String> temp = new ArrayList<String>();
            temp.clear();
            arrayList.clear();
            int i = 1;
            if (beginDay.getCount() > 0) {
                beginDay.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(Settings.getString(App.DRIVER));
                    print.setReferenceNumber(beginDay.getString(beginDay.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.BeginDayRequest_TR);
                    print.setIsPosted(beginDay.getString(beginDay.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }
                    //  arrayList.add(print);

                }
                while (beginDay.moveToNext());
            }

            if (odometer.getCount() > 0) {
                odometer.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(Settings.getString(App.DRIVER));
                    print.setReferenceNumber(odometer.getString(odometer.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.BeginDayOdometerRequest_TR);
                    print.setIsPosted(odometer.getString(odometer.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (odometer.moveToNext());
            }

            if (loadConfirmation.getCount() > 0) {
                loadConfirmation.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(Settings.getString(App.DRIVER));
                    print.setReferenceNumber(loadConfirmation.getString(loadConfirmation.getColumnIndex(db.KEY_ORDER_ID)));
                    print.setTransactionType(ConfigStore.LoadConfirmation_TR);
                    print.setIsPosted(loadConfirmation.getString(loadConfirmation.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }

                }
                while (loadConfirmation.moveToNext());
            }

            if (unload.getCount() > 0) {
                unload.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(Settings.getString(App.DRIVER));
                    print.setReferenceNumber(unload.getString(unload.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.UnloadRequest_TR);
                    print.setIsPosted(unload.getString(unload.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }
                    //  arrayList.add(print);

                }
                while (beginDay.moveToNext());
            }

            if (endDay.getCount() > 0) {
                endDay.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(Settings.getString(App.DRIVER));
                    print.setReferenceNumber(endDay.getString(endDay.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.EndDayRequest_TR);
                    print.setIsPosted(endDay.getString(endDay.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }
                    //  arrayList.add(print);

                }
                while (endDay.moveToNext());
            }

            if (odometerEnd.getCount() > 0) {
                odometerEnd.moveToFirst();
                do {
                    Print print = new Print();
                    print.setCustomer_id(Settings.getString(App.DRIVER));
                    print.setReferenceNumber(odometerEnd.getString(odometerEnd.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                    print.setTransactionType(ConfigStore.EndDayOdometerRequest_TR);
                    print.setIsPosted(odometerEnd.getString(odometerEnd.getColumnIndex(db.KEY_IS_POSTED)).equals(App.DATA_IS_POSTED) ? true : false);

                    if (!temp.contains(print.getReferenceNumber())) {
                        temp.add(print.getReferenceNumber());
                        arrayList.add(print);
                        i++;
                    }
                    //  arrayList.add(print);

                }
                while (odometerEnd.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

}
