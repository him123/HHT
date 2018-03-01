package com.ae.benchmark.data;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ae.benchmark.activities.AppController;
import com.ae.benchmark.activities.LoadRequestConstants;
import com.ae.benchmark.models.ColletionData;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.models.PreSaleProceed;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.utils.DatabaseHandler;

/**
 * Created by eheuristic on 12/2/2016.
 */

public class Const {

    public static ArrayList<Customer> dataArrayList=new ArrayList<>();
    public static ArrayList<Customer> allCustomerdataArrayList=new ArrayList<>();
    public static  ArrayList<String> addlist=new ArrayList<>();
    public static ArrayList<Sales> salesarrayList;
    public static int loadNumber=0;
    public static String creditLimitDays="0";

    public static HashMap<String, String> AllLoad = new HashMap<>();

    public static ArrayList<LoadSummary> dataNew = new ArrayList<>();
    public static ArrayList<LoadSummary> dataOld = new ArrayList<>();

    public static List<LoadRequestConstants> loadRequestConstantsList=new ArrayList<>();
    public static HashMap<Integer,List<LoadRequestConstants>> constantsHashMap=new HashMap<>();
    public static ArrayList<Sales> focList = new ArrayList<>();


    public static ArrayList<ColletionData> colletionDatas = new ArrayList<>();


    public static ArrayList<PreSaleProceed> proceedArrayList=new ArrayList<>();
    public static String availableLimit = "";
    public static String NumbertoWord = "";

    public static int TotalAPICOUNT = 12;
    public static String APICOUNT = TotalAPICOUNT+"/"+TotalAPICOUNT;


    public static int customerPosition;
    public static Customer customerDetail;
    public static String paySource;
    public static String payReference;
    public static boolean isPostAudit = false;
    public static boolean isDisplayVatTax = true;


    public static int id=-1;

    public static Bundle grBundle = null;
    public static Bundle brBundle = null;
    public static Bundle siBundle = null;
    public static Bundle focBundle = null;
    public static Customer customer;


    public static boolean isInternet() {
        ConnectivityManager cm = (ConnectivityManager) AppController.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static boolean isGPSEnable(Context context) {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

 }
