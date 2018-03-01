package com.ae.benchmark.sap;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.ae.benchmark.App;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.OfflinePost;
import com.ae.benchmark.models.OfflineResponse;
import com.ae.benchmark.models.Unload;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rakshit on 11-Jan-17.
 */
public class SyncData extends IntentService {
    ArrayList<OfflinePost> arrayList = new ArrayList<>();
    ArrayList<OfflinePost> beginDayList = new ArrayList<>();
    ArrayList<OfflinePost> odometerList = new ArrayList<>();
    ArrayList<OfflinePost> customerList = new ArrayList<>();

    ArrayList<OfflinePost> loadList = new ArrayList<>();
    ArrayList<OfflinePost> unloadList = new ArrayList<>();

    ArrayList<OfflinePost> orderreqList = new ArrayList<>();
    ArrayList<OfflinePost> LoadreqList = new ArrayList<>();

    ArrayList<OfflinePost> salesList = new ArrayList<>();
    ArrayList<OfflinePost> returngList = new ArrayList<>();
    ArrayList<OfflinePost> returnbList = new ArrayList<>();
    ArrayList<OfflinePost> custdelreqList = new ArrayList<>();
    ArrayList<OfflinePost> collectionList = new ArrayList<>();
    ArrayList<OfflinePost> pcollectionList = new ArrayList<>();
    public static String TAG = "SyncData";
    DatabaseHandler db;
    public ArrayList<ArticleHeader> articles;
    Activity activity;

    private static SyncData instance = null;

    public static synchronized SyncData getInstance() {

        if (instance == null) {
            instance = new SyncData();
        }

        return instance;
    }

    public SyncData() {
        super(TAG);
        instance = this;
        db = new DatabaseHandler(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("I am here", "IntentService" + Settings.getString(App.IS_DATA_SYNCING));

        if (!Boolean.parseBoolean(Settings.getString(App.IS_DATA_SYNCING))) {
            Log.e("Inside", "Inside" + getSyncCount());
            if (getSyncCount() > 0) {
                articles = new ArrayList<>();
                articles = ArticleHeaders.get();
                syncData();
            }
        }
    }

    public void syncData() {
        if (false) {

        }
//        if(getSyncCount(ConfigStore.BeginDayFunction)>0){
//            generateBatch(ConfigStore.BeginDayFunction);
//            new syncData(ConfigStore.BeginDayFunction);
//        }
//         if(getSyncCount(ConfigStore.OdometerFunction)>0){
//            generateBatch(ConfigStore.OdometerFunction);
//            new syncData(ConfigStore.OdometerFunction);
//        }
         if(getSyncCount(ConfigStore.AddCustomerFunction)>0){
            generateBatch(ConfigStore.AddCustomerFunction);
            new syncData(ConfigStore.AddCustomerFunction);
        }
        if (getSyncCount(ConfigStore.LoadConfirmationFunction) > 0) {
            generateBatch(ConfigStore.LoadConfirmationFunction);
            new syncData(ConfigStore.LoadConfirmationFunction);
        }
        if (getSyncCount(ConfigStore.LoadVarianceFunction + "D") > 0) {
            generateBatch(ConfigStore.LoadVarianceFunction + "D");
            new syncData(ConfigStore.LoadVarianceFunction + "D");
        }
        if (getSyncCount(ConfigStore.LoadVarianceFunction + "C") > 0) {
            generateBatch(ConfigStore.LoadVarianceFunction + "C");
            new syncData(ConfigStore.LoadVarianceFunction + "C");
        }
        if (getSyncCount(ConfigStore.LoadRequestFunction) > 0) {
            generateBatch(ConfigStore.LoadRequestFunction);
            new syncData(ConfigStore.LoadRequestFunction);
        }
        if (getSyncCount(ConfigStore.CustomerOrderRequestFunction + "O") > 0) {
            generateBatch(ConfigStore.CustomerOrderRequestFunction + "O");
            new syncData(ConfigStore.CustomerOrderRequestFunction + "O");
        }
        if (getSyncCount(ConfigStore.InvoiceRequestFunction) > 0) {
            generateBatch(ConfigStore.InvoiceRequestFunction);
            new syncData(ConfigStore.InvoiceRequestFunction);
        }
        if (getSyncCount(ConfigStore.ReturnsFunction + "G") > 0) {
            generateBatch(ConfigStore.ReturnsFunction + "G");
            new syncData(ConfigStore.ReturnsFunction + "G");
        }
        if (getSyncCount(ConfigStore.ReturnsFunction + "B") > 0) {
            generateBatch(ConfigStore.ReturnsFunction + "B");
            new syncData(ConfigStore.ReturnsFunction + "B");
        }
        if (getSyncCount(ConfigStore.CustomerDeliveryRequestFunction) > 0) {
            generateBatch(ConfigStore.CustomerDeliveryRequestFunction);
            new syncData(ConfigStore.CustomerDeliveryRequestFunction);
        }
        if (getSyncCount(ConfigStore.CustomerDeliveryDeleteRequestFunction) > 0) {
            generateBatch(ConfigStore.CustomerDeliveryDeleteRequestFunction);
            new syncData(ConfigStore.CustomerDeliveryDeleteRequestFunction);
        }
//         if(getSyncCount(ConfigStore.VisitListFunction)>0){
//            generateBatch(ConfigStore.VisitListFunction);
//            new syncData(ConfigStore.VisitListFunction);
//        }
        if (getSyncCount(ConfigStore.CollectionFunction) > 0) {
            generateBatch(ConfigStore.CollectionFunction);
            new syncData(ConfigStore.CollectionFunction);
        }
        if (getSyncCount(ConfigStore.PartialCollectionFunction) > 0) {
            generateBatch(ConfigStore.PartialCollectionFunction);
            new syncData(ConfigStore.PartialCollectionFunction);
        }
        if (getSyncCount(ConfigStore.UnloadFunction + "U") > 0) {
            generateBatch(ConfigStore.UnloadFunction + "U");
            new syncData(ConfigStore.UnloadFunction + "U");
        }

    }

    public void generateBatch(String request) {
        //Log.e("Request","Request" + request);
        try {

            String purchaseNumber = "";
            String tempPurchaseNumber = "";
            String customerNumber = "";
            String tempCustomerNumber = "";
            String deliveryNumber = "";
            String tempDeliveryNumber = "";
            String customerPO = "";

            switch (request) {

                case ConfigStore.BeginDayFunction: {
                    JSONArray deepEntity = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    deepEntity.put(jsonObject);
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TIME_STAMP, "");
                    map.put(db.KEY_TRIP_ID, "");
                    map.put(db.KEY_PURCHASE_NUMBER, "");
                    map.put(db.KEY_DATE, "");
                    map.put(db.KEY_IS_SELECTED, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_FUNCTION, ConfigStore.BeginDayFunction);
                    filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    Cursor beginDayCursor = db.getData(db.BEGIN_DAY, map, filter);
                    if (beginDayCursor.getCount() > 0) {
                        beginDayCursor.moveToFirst();
                        OfflinePost object = new OfflinePost();
                        object.setCollectionName(App.POST_COLLECTION);
                        object.setMap(Helpers.buildBeginDayHeader(ConfigStore.BeginDayFunction, Settings.getString(App.TRIP_ID),
                                Settings.getString(App.DRIVER), beginDayCursor.getString(beginDayCursor.getColumnIndex(db.KEY_TIME_STAMP)),
                                beginDayCursor.getString(beginDayCursor.getColumnIndex(db.KEY_PURCHASE_NUMBER))));
                        object.setDeepEntity(deepEntity);
                        Helpers.logData(getApplication(), "Begin Day Batch Header" + object.getMap().toString());
                        Helpers.logData(getApplication(), "Begin Day Batch Body" + deepEntity.toString());
                        beginDayList.add(object);
                    }
                    break;
                }
                case ConfigStore.OdometerFunction: {
                    JSONArray deepEntity = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    deepEntity.put(jsonObject);
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_ODOMETER_VALUE, "");
                    map.put(db.KEY_TRIP_ID, "");
                    map.put(db.KEY_PURCHASE_NUMBER, "");
                    map.put(db.KEY_TIME_STAMP, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    Cursor odometerCursor = db.getData(db.ODOMETER, map, filter);
                    if (odometerCursor.getCount() > 0) {
                        odometerCursor.moveToFirst();
                        OfflinePost object = new OfflinePost();
                        object.setCollectionName(App.POST_ODOMETER_SET);
                        object.setMap(Helpers.buildOdometerHeader(odometerCursor.getString(odometerCursor.getColumnIndex(db.KEY_TRIP_ID))
                                , odometerCursor.getString(odometerCursor.getColumnIndex(db.KEY_ODOMETER_VALUE))));
                        object.setDeepEntity(deepEntity);
                        Helpers.logData(getApplication(), "Odometer Batch Header" + object.getMap().toString());
                        Helpers.logData(getApplication(), "Odometer Batch Body" + deepEntity.toString());
                        odometerList.add(object);
                    }
                    break;
                }
                case ConfigStore.AddCustomerFunction: {
                    JSONArray deepEntity = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    deepEntity.put(jsonObject);
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TIME_STAMP, "");
                    map.put(db.KEY_CUSTOMER_NO, "");
                    map.put(db.KEY_OWNER_NAME, "");
                    map.put(db.KEY_OWNER_NAME_AR, "");
                    map.put(db.KEY_TRADE_NAME, "");
                    map.put(db.KEY_TRADE_NAME_AR, "");
                    map.put(db.KEY_AREA, "");
                    map.put(db.KEY_STREET, "");
                    map.put(db.KEY_CR_NO, "");
                    map.put(db.KEY_PO_BOX, "");
                    map.put(db.KEY_EMAIL, "");
                    map.put(db.KEY_TELEPHONE, "");
                    map.put(db.KEY_FAX, "");
                    map.put(db.KEY_SALES_AREA, "");
                    map.put(db.KEY_DISTRIBUTION, "");
                    map.put(db.KEY_DIVISION, "");
                    map.put(db.KEY_IS_POSTED, "");
                    map.put(db.KEY_IS_PRINTED, "");
                    map.put(db.KEY_LATITUDE, "");
                    map.put(db.KEY_LONGITUDE, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    Cursor c = db.getData(db.NEW_CUSTOMER_POST, map, filter);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        do {
                            OfflinePost object = new OfflinePost();
                            object.setCollectionName(App.POST_CUSTOMER_SET);
                            object.setMap(Helpers.buildnewCustomerHeader(c.getString(c.getColumnIndex(db.KEY_CUSTOMER_NO)), c.getString(c.getColumnIndex(db.KEY_OWNER_NAME)),
                                    c.getString(c.getColumnIndex(db.KEY_OWNER_NAME_AR)), c.getString(c.getColumnIndex(db.KEY_TRADE_NAME)), c.getString(c.getColumnIndex(db.KEY_TRADE_NAME_AR)),
                                    c.getString(c.getColumnIndex(db.KEY_AREA)), c.getString(c.getColumnIndex(db.KEY_STREET)), c.getString(c.getColumnIndex(db.KEY_CR_NO)),
                                    c.getString(c.getColumnIndex(db.KEY_PO_BOX)), c.getString(c.getColumnIndex(db.KEY_EMAIL)),
                                    c.getString(c.getColumnIndex(db.KEY_TELEPHONE)), c.getString(c.getColumnIndex(db.KEY_FAX)),
                                    c.getString(c.getColumnIndex(db.KEY_SALES_AREA)), c.getString(c.getColumnIndex(db.KEY_DISTRIBUTION)),
                                    c.getString(c.getColumnIndex(db.KEY_DIVISION)), c.getString(c.getColumnIndex(db.KEY_LATITUDE)),
                                    c.getString(c.getColumnIndex(db.KEY_LONGITUDE))));
                            object.setDeepEntity(deepEntity);
                            Helpers.logData(getApplication(), "Add Customer Batch Header" + object.getMap().toString());
                            Helpers.logData(getApplication(), "Add Customer Batch Body" + deepEntity.toString());
                            customerList.add(object);
                        }
                        while (c.moveToNext());

                    }
                    break;
                }
                case ConfigStore.VisitListFunction: {
                    JSONArray deepEntity = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    deepEntity.put(jsonObject);
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_TIME_STAMP, "");
                    map.put(db.KEY_START_TIMESTAMP, "");
                    map.put(db.KEY_END_TIMESTAMP, "");
                    map.put(db.KEY_VISITLISTID, "");
                    map.put(db.KEY_TRIP_ID, "");
                    map.put(db.KEY_ACTIVITY_ID, "");
                    map.put(db.KEY_VISIT_SERVICED_REASON, "");
                    map.put(db.KEY_CUSTOMER_NO, "");
                    map.put(db.KEY_IS_POSTED, "");
                    map.put(db.KEY_IS_PRINTED, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                    Cursor visitListCursor = db.getData(db.VISIT_LIST_POST, map, filter);
                    if (visitListCursor.getCount() > 0) {
                        visitListCursor.moveToFirst();
                        do {
                            OfflinePost object = new OfflinePost();
                            object.setCollectionName(App.POST_COLLECTION);
                            object.setMap(Helpers.buildHeaderMapVisitList(ConfigStore.VisitListFunction,
                                    visitListCursor.getString(visitListCursor.getColumnIndex(db.KEY_START_TIMESTAMP)),
                                    visitListCursor.getString(visitListCursor.getColumnIndex(db.KEY_END_TIMESTAMP)),
                                    visitListCursor.getString(visitListCursor.getColumnIndex(db.KEY_VISITLISTID)),
                                    visitListCursor.getString(visitListCursor.getColumnIndex(db.KEY_ACTIVITY_ID)),
                                    visitListCursor.getString(visitListCursor.getColumnIndex(db.KEY_VISIT_SERVICED_REASON)),
                                    visitListCursor.getString(visitListCursor.getColumnIndex(db.KEY_CUSTOMER_NO))));
                            object.setDeepEntity(deepEntity);
                            Helpers.logData(getApplication(), "Visit List Batch Header" + object.getMap().toString());
                            Helpers.logData(getApplication(), "Visit List Batch Body" + deepEntity.toString());
                            arrayList.add(object);
                        }
                        while (visitListCursor.moveToNext());

                    }
                    break;
                }
                case ConfigStore.LoadConfirmationFunction: {
                    try {
                        JSONArray deepEntity = new JSONArray();
                        JSONObject obj = new JSONObject();
                        deepEntity.put(obj);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_TIME_STAMP, "");
                        map.put(db.KEY_TRIP_ID, "");
                        map.put(db.KEY_FUNCTION, "");
                        map.put(db.KEY_ORDER_ID, "");
                        map.put(db.KEY_CUSTOMER_NO, "");
                        map.put(db.KEY_IS_POSTED, "");
                        map.put(db.KEY_IS_PRINTED, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        Cursor cursor = db.getData(db.LOAD_CONFIRMATION_HEADER, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                OfflinePost object = new OfflinePost();
                                object.setCollectionName(App.POST_COLLECTION);
                                object.setMap(Helpers.buildLoadConfirmationHeader(cursor.getString(cursor.getColumnIndex(db.KEY_FUNCTION)),
                                        cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)), Settings.getString(App.DRIVER)));
                                object.setDeepEntity(deepEntity);
                                Helpers.logData(getApplication(), "Load Confirmation Batch Header" + object.getMap().toString());
                                Helpers.logData(getApplication(), "Load Confirmation Batch Body" + deepEntity.toString());
                                loadList.add(object);
                            }
                            while (cursor.moveToNext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.LoadVarianceFunction + "D": {
                    try {

                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_DATE, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_PRICE, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceDebit);

                        Cursor pendingLoadRequestCursor = db.getData(db.LOAD_VARIANCE_ITEMS_POST, itemMap, filter);
                        if (pendingLoadRequestCursor.getCount() > 0) {
                            pendingLoadRequestCursor.moveToFirst();
                            int itemno = 10;
                            String documentDate = "";
                            do {
                                tempPurchaseNumber = pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_ORDER_ID));
                                documentDate = pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_DATE));
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {
                                /*OfflinePost object = new OfflinePost();
                                object.setCollectionName(App.POST_COLLECTION);
                                object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadVarianceFunction,"",ConfigStore.LoadVarianceDebit,Settings.getString(App.DRIVER),"",purchaseNumber,documentDate));
                                object.setDeepEntity(deepEntity);
                                arrayList.add(object);
                                purchaseNumber = tempPurchaseNumber;
                                deepEntity = new JSONArray();*/
                                }

                                if (pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("Material", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_CASE)));
                                    jo.put("ItemValue", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("UoM", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    jo.put("Value", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("Storagelocation", "");
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                } else {
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("Material", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                    jo.put("ItemValue", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("UoM", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    jo.put("Value", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("Storagelocation", "");
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                }
                                //Check if cursor is at last position
                                if (pendingLoadRequestCursor.getPosition() == pendingLoadRequestCursor.getCount() - 1) {
                                    OfflinePost object = new OfflinePost();
                                    object.setCollectionName(App.POST_COLLECTION);
                                    object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadVarianceFunction, "", ConfigStore.LoadVarianceDebit, Settings.getString(App.DRIVER), "", purchaseNumber, documentDate));
                                    object.setDeepEntity(deepEntity);
                                    arrayList.add(object);
                                    Helpers.logData(getApplication(), "Load Variance Batch Header" + object.getMap().toString());
                                    Helpers.logData(getApplication(), "Load Variance Batch Body" + deepEntity.toString());
                                    deepEntity = new JSONArray();
                                }

                            }
                            while (pendingLoadRequestCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.LoadVarianceFunction + "C": {
                    try {

                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_DATE, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_PRICE, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);

                        Cursor pendingLoadRequestCursor = db.getData(db.LOAD_VARIANCE_ITEMS_POST, itemMap, filter);
                        if (pendingLoadRequestCursor.getCount() > 0) {
                            pendingLoadRequestCursor.moveToFirst();
                            int itemno = 10;
                            String documentDate = "";
                            do {
                                tempPurchaseNumber = pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_ORDER_ID));
                                documentDate = pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_DATE));
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {
                                /*OfflinePost object = new OfflinePost();
                                object.setCollectionName(App.POST_COLLECTION);
                                object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadVarianceFunction,"",ConfigStore.LoadVarianceCredit,Settings.getString(App.DRIVER),"",purchaseNumber,documentDate));
                                object.setDeepEntity(deepEntity);
                                arrayList.add(object);
                                purchaseNumber = tempPurchaseNumber;
                                deepEntity = new JSONArray();*/
                                }

                                if (pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("Material", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_CASE)));
                                    jo.put("ItemValue", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("UoM", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    jo.put("Value", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("Storagelocation", "");
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                } else {
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("Material", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                    jo.put("ItemValue", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("UoM", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    jo.put("Value", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                    jo.put("Storagelocation", "");
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                }
                                //Check if cursor is at last position
                                if (pendingLoadRequestCursor.getPosition() == pendingLoadRequestCursor.getCount() - 1) {
                                    OfflinePost object = new OfflinePost();
                                    object.setCollectionName(App.POST_COLLECTION);
                                    object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadVarianceFunction, "", ConfigStore.LoadVarianceCredit, Settings.getString(App.DRIVER), "", purchaseNumber, documentDate));
                                    object.setDeepEntity(deepEntity);
                                    arrayList.add(object);
                                    Helpers.logData(getApplication(), "Load Variance Batch Header" + object.getMap().toString());
                                    Helpers.logData(getApplication(), "Load Variance Batch Body" + deepEntity.toString());
                                    deepEntity = new JSONArray();
                                }

                            }
                            while (pendingLoadRequestCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.LoadRequestFunction: {
                    try {

                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_DATE, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_PRICE, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                        Cursor pendingLoadRequestCursor = db.getData(db.LOAD_REQUEST, itemMap, filter);
                        if (pendingLoadRequestCursor.getCount() > 0) {
                            pendingLoadRequestCursor.moveToFirst();
                            int itemno = 10;
                            String documentDate = "";
                            Double amount = 0.0;
                            do {
                                tempPurchaseNumber = pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_ORDER_ID));
                                documentDate = pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_DATE));
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {
                                    OfflinePost object = new OfflinePost();
                                    object.setCollectionName(App.POST_COLLECTION);
                                    object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadRequestFunction, "", ConfigStore.DocumentType, Settings.getString(App.DRIVER), String.format("%.2f", amount), purchaseNumber, documentDate));
                                    object.setDeepEntity(deepEntity);
                                    LoadreqList.add(object);
                                    purchaseNumber = tempPurchaseNumber;
                                    deepEntity = new JSONArray();
                                }
                                Double itemamount = 0.0, qty = 0.0;
                                if (Double.parseDouble(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = Double.parseDouble(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE))) * Double.parseDouble(deno);
                                    itemamount += casePrice * Float.parseFloat(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_CASE)));
                                    qty = Float.parseFloat(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_CASE))) * Double.parseDouble(deno);
                                }
                                if (Double.parseDouble(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                    itemamount += Float.parseFloat(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE))) * Float.parseFloat(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                    qty += Double.parseDouble(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                amount = amount + Double.parseDouble(String.format("%.2f", itemamount));


                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                jo.put("Description", UrlBuilder.decodeString(pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                jo.put("Plant", "");
                                jo.put("Quantity", String.format("%.2f", qty));
                                jo.put("ItemValue", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                jo.put("UoM", pendingLoadRequestCursor.getString(pendingLoadRequestCursor.getColumnIndex(db.KEY_UOM)));
                                jo.put("Value", String.format("%.2f", itemamount));
                                //jo.put("Vat", String.format("%.2f",itemamount*Double.parseDouble(Settings.getString(App.VATValue))));
                                jo.put("Storagelocation", "");
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);
                                //Check if cursor is at last position
                                if (pendingLoadRequestCursor.getPosition() == pendingLoadRequestCursor.getCount() - 1) {
                                    OfflinePost object = new OfflinePost();
                                    object.setCollectionName(App.POST_COLLECTION);
                                    object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadRequestFunction, "", ConfigStore.DocumentType, Settings.getString(App.DRIVER), String.format("%.2f", amount), purchaseNumber, documentDate));
                                    object.setDeepEntity(deepEntity);
                                    LoadreqList.add(object);
                                    Helpers.logData(getApplication(), "Load Request Batch Header" + object.getMap().toString());
                                    Helpers.logData(getApplication(), "Load Request Batch Body" + deepEntity.toString());
                                    deepEntity = new JSONArray();
                                }

                            }
                            while (pendingLoadRequestCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.CustomerOrderRequestFunction + "O": {
                    try {

                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_DATE, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_PRICE, "");
                        itemMap.put(db.KEY_PRICECASE, "");
                        itemMap.put(db.KEY_PRICEPCS, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        itemMap.put(db.KEY_CUSTOMER_PO, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                        Cursor pendingOrderRequestCursor = db.getData(db.ORDER_REQUEST, itemMap, filter);
                        if (pendingOrderRequestCursor.getCount() > 0) {
                            pendingOrderRequestCursor.moveToFirst();
                            int itemno = 10;
                            String documentDate = "";
                            Double amount = 0.0;
                            do {
                                tempPurchaseNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_ORDER_ID));
                                tempCustomerNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CUSTOMER_NO));
                                documentDate = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_DATE));
                                customerPO = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CUSTOMER_PO));
                                if (customerNumber.equals("")) {
                                    customerNumber = tempCustomerNumber;
                                }
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {
                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapOrder(ConfigStore.LoadRequestFunction, "", ConfigStore.CustomerOrderRequestDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, customerPO));
                                        object.setDeepEntity(deepEntity);
                                        Helpers.logData(getApplication(), "Order Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Order Batch Body" + deepEntity.toString());
                                        orderreqList.add(object);
                                        purchaseNumber = tempPurchaseNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        //object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadRequestFunction, "", ConfigStore.CustomerOrderRequestDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setMap(Helpers.buildHeaderMapOrder(ConfigStore.LoadRequestFunction, "", ConfigStore.CustomerOrderRequestDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, customerPO));
                                        object.setDeepEntity(deepEntity);
                                        Helpers.logData(getApplication(), "Customer Order Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Customer Order Batch Body" + deepEntity.toString());
                                        orderreqList.add(object);
                                        purchaseNumber = tempPurchaseNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }
                                }

                                Double itemamount = 0.0,qty = 0.0;
                                if (Double.parseDouble(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = Double.parseDouble(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_PRICECASE)));
                                    itemamount += casePrice * Float.parseFloat(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CASE)));
                                    qty = Float.parseFloat(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CASE))) * Double.parseDouble(deno);
                                }
                                if (Double.parseDouble(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                    itemamount += Float.parseFloat(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_PRICEPCS))) * Float.parseFloat(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                    qty += Double.parseDouble(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                amount = amount + Double.parseDouble(String.format("%.2f", itemamount));
                                Double itemValue = Double.parseDouble(String.format("%.2f", itemamount)) / qty;

                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                jo.put("Description", UrlBuilder.decodeString(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                jo.put("Plant", "");
                                jo.put("Quantity", String.format("%.2f", qty));
                                jo.put("ItemValue", String.format("%.2f", itemValue));
                                jo.put("ActPricePcs", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_PRICE)));
                                jo.put("UoM", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)));
                                jo.put("Value", String.format("%.2f", itemamount));
                                jo.put("Vat", String.format("%.2f", itemamount * Double.parseDouble(Settings.getString(App.VATValue))));
                                jo.put("Vatpr", Settings.getString(App.VATPR));
                                jo.put("Storagelocation", "");
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);

                                //Check if cursor is at last position
                                if (pendingOrderRequestCursor.getPosition() == pendingOrderRequestCursor.getCount() - 1) {

                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        //object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadRequestFunction, "", ConfigStore.CustomerOrderRequestDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setMap(Helpers.buildHeaderMapOrder(ConfigStore.LoadRequestFunction, "", ConfigStore.CustomerOrderRequestDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, customerPO));
                                        object.setDeepEntity(deepEntity);
                                        Helpers.logData(getApplication(), "Customer Order Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Customer Order Batch Body" + deepEntity.toString());

                                        orderreqList.add(object);
                                        purchaseNumber = tempPurchaseNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        //object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadRequestFunction, "", ConfigStore.CustomerOrderRequestDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setMap(Helpers.buildHeaderMapOrder(ConfigStore.LoadRequestFunction, "", ConfigStore.CustomerOrderRequestDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, customerPO));
                                        object.setDeepEntity(deepEntity);
                                        Helpers.logData(getApplication(), "Customer Order Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Customer Order Batch Body" + deepEntity.toString());
                                        orderreqList.add(object);
                                        purchaseNumber = tempPurchaseNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }

                                }

                            }
                            while (pendingOrderRequestCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.InvoiceRequestFunction: {
                    try {

                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_TIME_STAMP, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_ORG_CASE, "");
                        itemMap.put(db.KEY_ORG_UNITS, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_AMOUNT, "");
                        itemMap.put(db.KEY_AMOUNTPCS, "");
                        itemMap.put(db.KEY_AMOUNTCASE, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        itemMap.put(db.KEY_PURCHASE_NUMBER, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                        Cursor pendingInvoiceCursor = db.getData(db.CAPTURE_SALES_INVOICE, itemMap, filter);

                        if (pendingInvoiceCursor.getCount() > 0) {
                            pendingInvoiceCursor.moveToFirst();
                            String documentDate = "";
                            int itemno = 10;
                            Double amount = 0.0;
                            do {
                                tempPurchaseNumber = pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORDER_ID));
                                tempCustomerNumber = pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_CUSTOMER_NO));
                                documentDate = pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_TIME_STAMP));

                                if (customerNumber.equals("")) {
                                    customerNumber = tempCustomerNumber;
                                }
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {


                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.InvoiceRequestFunction, "", ConfigStore.InvoiceDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        salesList.add(object);
                                        Helpers.logData(getApplication(), "Invoice Order Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Invoice Order Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.InvoiceRequestFunction, "", ConfigStore.InvoiceDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        salesList.add(object);
                                        Helpers.logData(getApplication(), "Invoice Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Invoice Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }
                                }

                                Double itemamount = 0.0, qtyPcs = 0.0, qtyCases = 0.0;
                                Double ActcasePrice = 0.0;
                                if (Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_AMOUNTCASE)));
                                    ActcasePrice = Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_AMOUNT))) * Double.parseDouble(deno);
                                    itemamount += casePrice * Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_CASE)));
                                    qtyCases = Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_CASE))) * Double.parseDouble(deno);
                                }
                                if (Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_UNITS))) >= 1) {
                                    itemamount += Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_AMOUNTPCS))) * Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                                    qtyPcs += Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                                }

                                amount = amount + Double.parseDouble(String.format("%.2f", itemamount));
                                Double itemValue = Double.parseDouble(String.format("%.2f", itemamount)) / (qtyCases + qtyPcs);

                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                jo.put("Description", UrlBuilder.decodeString(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                jo.put("Plant", "");

                                jo.put("Quantity", String.format("%.2f", qtyCases + qtyPcs));
                                jo.put("ItemValue", String.format("%.2f", itemValue));
                                jo.put("ActPricePcs", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_AMOUNT)));

//                                jo.put("QuantityCase",""+String.format("%.2f",qtyCases));
//                                jo.put("QuantityPcs",""+String.format("%.2f",qtyPcs));
//                                jo.put("ActPriceCase", String.format("%.2f",ActcasePrice));
//                                jo.put("ActPricePcs", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_AMOUNT)));
//                                jo.put("EditPriceCase", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_AMOUNTCASE)));
//                                jo.put("EditPricePcs", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_AMOUNTPCS)));
                                jo.put("Value", String.format("%.2f", itemamount));
                                jo.put("Vat", String.format("%.2f", itemamount * Double.parseDouble(Settings.getString(App.VATValue))));
                                jo.put("Vatpr", Settings.getString(App.VATPR));
                                jo.put("Storagelocation", "");
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);
//
                                //Check if cursor is at last position
                                if (pendingInvoiceCursor.getPosition() == pendingInvoiceCursor.getCount() - 1) {

                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.InvoiceRequestFunction, "", ConfigStore.InvoiceDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        salesList.add(object);
                                        Helpers.logData(getApplication(), "Invoice Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Invoice Batch Body" + deepEntity.toString());
                                        purchaseNumber = tempPurchaseNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.InvoiceRequestFunction, "", ConfigStore.InvoiceDocumentType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        salesList.add(object);
                                        Helpers.logData(getApplication(), "Invoice Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Invoice Batch Body" + deepEntity.toString());
                                        purchaseNumber = tempPurchaseNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }
                                }

                            }
                            while (pendingInvoiceCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.ReturnsFunction + "G": {
                    Log.e("Call here", "Call here");
                    try {

                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        itemMap.put(db.KEY_TIME_STAMP, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_PRICE, "");
                        itemMap.put(db.KEY_PRICEPCS, "");
                        itemMap.put(db.KEY_PRICECASE, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        itemMap.put(db.KEY_REASON_CODE, "");
                        itemMap.put(db.KEY_PURCHASE_NUMBER, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filter.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);

                        Cursor pendingGRCursor = db.getData(db.RETURNS, itemMap, filter);
                        if (pendingGRCursor.getCount() > 0) {
                            pendingGRCursor.moveToFirst();
                            int itemno = 10;
                            String documentDate = "";
                            String reasonCode = "";
                            Double amount = 0.0;
                            do {
                                tempPurchaseNumber = pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_ORDER_ID));
                                tempCustomerNumber = pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_CUSTOMER_NO));
                                reasonCode = pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_REASON_CODE));
                                documentDate = pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_TIME_STAMP));
                                if (customerNumber.equals("")) {
                                    customerNumber = tempCustomerNumber;
                                }
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {
                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.GoodReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                        object.setDeepEntity(deepEntity);
                                        returngList.add(object);
                                        Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());
                                        purchaseNumber = tempPurchaseNumber;

                                        deepEntity = new JSONArray();
                                    } else {
                                        if (deepEntity.length() > 0) {
                                            OfflinePost object = new OfflinePost();
                                            object.setCollectionName(App.POST_COLLECTION);
                                            object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.GoodReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                            object.setDeepEntity(deepEntity);
                                            returngList.add(object);
                                            Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                            Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());
                                            purchaseNumber = tempPurchaseNumber;
                                            customerNumber = tempCustomerNumber;
                                            deepEntity = new JSONArray();
                                        }

                                    }

                                }
                                Double itemamount = 0.0, qtyPcs = 0.0, qtyCases = 0.0;
                                Double ActcasePrice = 0.0;
                                if (Double.parseDouble(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = Double.parseDouble(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_PRICECASE)));
                                    ActcasePrice = Double.parseDouble(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_PRICE)));
                                    itemamount += casePrice * Float.parseFloat(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_CASE)));
                                    qtyCases = Float.parseFloat(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_CASE))) * Double.parseDouble(deno);
                                }

                                if (Double.parseDouble(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                    itemamount += Float.parseFloat(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_PRICEPCS))) * Float.parseFloat(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_UNIT)));
                                    qtyPcs += Double.parseDouble(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_UNIT)));
                                }

                                amount = amount + Double.parseDouble(String.format("%.2f", itemamount));
                                Double itemValue = Double.parseDouble(String.format("%.2f", itemamount)) / (qtyCases + qtyPcs);
                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                jo.put("Description", UrlBuilder.decodeString(pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                jo.put("Plant", "");

                                jo.put("Quantity", String.format("%.2f", qtyCases + qtyPcs));
                                jo.put("ItemValue", String.format("%.2f", itemValue));
                                jo.put("ActPricePcs", pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_PRICE)));

//                                    jo.put("QuantityCase",""+String.format("%.2f",qtyCases));
//                                    jo.put("QuantityPcs",""+String.format("%.2f",qtyPcs));
//                                    jo.put("ActPriceCase", String.format("%.2f",ActcasePrice));
//                                    jo.put("ActPricePcs", pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_PRICE)));
//                                    jo.put("EditPriceCase", pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_PRICECASE)));
//                                    jo.put("EditPricePcs", pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_PRICEPCS)));


                                jo.put("UoM", pendingGRCursor.getString(pendingGRCursor.getColumnIndex(db.KEY_UOM)));
                                jo.put("Value", String.format("%.2f", itemamount));
                                jo.put("Vat", String.format("%.2f", itemamount * Double.parseDouble(Settings.getString(App.VATValue))));
                                jo.put("Vatpr", Settings.getString(App.VATPR));
                                jo.put("Storagelocation", "");
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);
                                //Check if cursor is at last position
                                if (pendingGRCursor.getPosition() == pendingGRCursor.getCount() - 1) {

                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        //Log.e("This is test1","Test1");
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.GoodReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                        object.setDeepEntity(deepEntity);
                                        Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());

                                        returngList.add(object);
                                        purchaseNumber = tempPurchaseNumber;
                                        deepEntity = new JSONArray();

                                    } else {
                                        //Log.e("This is test2","Test2");
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.GoodReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                        object.setDeepEntity(deepEntity);
                                        returngList.add(object);
                                        Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }
                                }

                            }
                            while (pendingGRCursor.moveToNext());
                            Log.e("ArrayList", "" + returngList.size());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.ReturnsFunction + "B": {
                    try {

                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_TIME_STAMP, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_PRICE, "");
                        itemMap.put(db.KEY_PRICECASE, "");
                        itemMap.put(db.KEY_PRICEPCS, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        itemMap.put(db.KEY_REASON_CODE, "");
                        itemMap.put(db.KEY_PURCHASE_NUMBER, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);

                        Cursor pendingInvoiceCursor = db.getData(db.RETURNS, itemMap, filter);
                        if (pendingInvoiceCursor.getCount() > 0) {
                            pendingInvoiceCursor.moveToFirst();
                            int itemno = 10;
                            String documentDate = "";
                            String reasonCode = "";
                            Double amount = 0.0;
                            do {
                                tempPurchaseNumber = pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_ORDER_ID));
                                tempCustomerNumber = pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_CUSTOMER_NO));
                                reasonCode = pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_REASON_CODE));
                                documentDate = pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_TIME_STAMP));
                                if (customerNumber.equals("")) {
                                    customerNumber = tempCustomerNumber;
                                }
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {
                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.BadReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                        object.setDeepEntity(deepEntity);
                                        returnbList.add(object);
                                        Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;

                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.BadReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                        object.setDeepEntity(deepEntity);
                                        returnbList.add(object);
                                        Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }

                                }
                                Double itemamount = 0.0, qtyPcs = 0.0, qtyCases = 0.0;
                                Double ActcasePrice = 0.0;

                                if (Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_PRICECASE)));
                                    ActcasePrice = Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_PRICE))) * Double.parseDouble(deno);
                                    itemamount += casePrice * Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_CASE)));
                                    qtyCases = Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_CASE))) * Double.parseDouble(deno);
                                }
                                if (Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                    itemamount += Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_PRICEPCS))) * Float.parseFloat(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_UNIT)));
                                    qtyPcs += Double.parseDouble(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_UNIT)));
                                }
                                amount = amount + Double.parseDouble(String.format("%.2f", itemamount));
                                Double itemValue = Double.parseDouble(String.format("%.2f", itemamount)) / (qtyCases + qtyPcs);

                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                jo.put("Description", UrlBuilder.decodeString(pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                jo.put("Plant", "");

                                jo.put("Quantity", String.format("%.2f", qtyCases + qtyPcs));
                                jo.put("ItemValue", String.format("%.2f", itemValue));
                                jo.put("ActPricePcs", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_PRICE)));

//                                jo.put("QuantityCase",""+String.format("%.2f",qtyCases));
//                                jo.put("QuantityPcs",""+String.format("%.2f",qtyPcs));
//                                jo.put("ActPriceCase", String.format("%.2f",ActcasePrice));
//                                jo.put("ActPricePcs", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_PRICE)));
//                                jo.put("EditPriceCase", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_PRICECASE)));
//                                jo.put("EditPricePcs", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_PRICEPCS)));

                                jo.put("UoM", pendingInvoiceCursor.getString(pendingInvoiceCursor.getColumnIndex(db.KEY_UOM)));
                                jo.put("Value", String.format("%.2f", itemamount));
                                jo.put("Vat", String.format("%.2f", itemamount * Double.parseDouble(Settings.getString(App.VATValue))));
                                jo.put("Vatpr", Settings.getString(App.VATPR));
                                jo.put("Storagelocation", "");
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);
                                //Check if cursor is at last position
                                if (pendingInvoiceCursor.getPosition() == pendingInvoiceCursor.getCount() - 1) {

                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.BadReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                        object.setDeepEntity(deepEntity);
                                        returnbList.add(object);
                                        Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMapReason(ConfigStore.ReturnsFunction, "", ConfigStore.BadReturnType, customerNumber, String.format("%.2f", amount), purchaseNumber, documentDate, reasonCode));
                                        object.setDeepEntity(deepEntity);
                                        returnbList.add(object);
                                        Helpers.logData(getApplication(), "Returns Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Returns Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }
                                }
//                                }
//
                            }
                            while (pendingInvoiceCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.CustomerDeliveryRequestFunction: {
                    try {
                        JSONArray deepEntity = new JSONArray();
                        HashMap<String, String> itemMap = new HashMap<>();

                        itemMap.put(db.KEY_TIME_STAMP, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_DELIVERY_NO, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_AMOUNT, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        itemMap.put(db.KEY_UOM, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                        Cursor pendingOrderRequestCursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, itemMap, filter);
                        if (pendingOrderRequestCursor.getCount() > 0) {
                            pendingOrderRequestCursor.moveToFirst();
                            //String deliveryNo = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_DELIVERY_NO));
                            int itemno = 10;
                            String documentDate = "";
                            do {
                                tempDeliveryNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_DELIVERY_NO));
                                tempPurchaseNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_ORDER_ID));
                                tempCustomerNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CUSTOMER_NO));
                                documentDate = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_TIME_STAMP));
                                if (customerNumber.equals("")) {
                                    customerNumber = tempCustomerNumber;
                                }
                                if (deliveryNumber.equals("")) {
                                    deliveryNumber = tempDeliveryNumber;
                                }
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {
                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        custdelreqList.add(object);
                                        Helpers.logData(getApplication(), "Delivery Request Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Delivery Request Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deliveryNumber = tempDeliveryNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        custdelreqList.add(object);
                                        Helpers.logData(getApplication(), "Delivery Request Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Delivery Request Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deliveryNumber = tempDeliveryNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }

                                }

                                if (pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    JSONObject jo = new JSONObject();
                                    //jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno),4));
                                    jo.put("Item", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_ITEM_NO)));
                                    jo.put("Material", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CASE)));
                                    jo.put("ItemValue", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("UoM", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    jo.put("Value", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("Storagelocation", "");
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                } else {
                                    JSONObject jo = new JSONObject();
                                    //jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno),4));
                                    jo.put("Item", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_ITEM_NO)));
                                    jo.put("Material", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                    jo.put("ItemValue", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("UoM", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    jo.put("Value", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("Storagelocation", "");
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                }
                                //Check if cursor is at last position
                                if (pendingOrderRequestCursor.getPosition() == pendingOrderRequestCursor.getCount() - 1) {

                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        custdelreqList.add(object);
                                        Helpers.logData(getApplication(), "Delivery Request Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Delivery Request Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deliveryNumber = tempDeliveryNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        custdelreqList.add(object);
                                        Helpers.logData(getApplication(), "Delivery Request Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Delivery Request Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deliveryNumber = tempDeliveryNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }

                                /*OfflinePost object = new OfflinePost();
                                object.setCollectionName(App.POST_COLLECTION);
                                object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadRequestFunction, "", ConfigStore.DocumentType, "0000205005", "", purchaseNumber));
                                object.setDeepEntity(deepEntity);
                                arrayList.add(object);
                                deepEntity = new JSONArray();*/
                                }

                            }
                            while (pendingOrderRequestCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.CustomerDeliveryDeleteRequestFunction: {
                    try {

                        JSONArray deepEntity = new JSONArray();

                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_DELIVERY_NO, "");
                        itemMap.put(db.KEY_DATE, "");
                        itemMap.put(db.KEY_ITEM_NO, "");
                        itemMap.put(db.KEY_MATERIAL_NO, "");
                        itemMap.put(db.KEY_MATERIAL_DESC1, "");
                        itemMap.put(db.KEY_CASE, "");
                        itemMap.put(db.KEY_UNIT, "");
                        itemMap.put(db.KEY_UOM, "");
                        itemMap.put(db.KEY_REASON_CODE, "");
                        itemMap.put(db.KEY_REASON_DESCRIPTION, "");
                        itemMap.put(db.KEY_AMOUNT, "");
                        itemMap.put(db.KEY_ORDER_ID, "");
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                        Cursor pendingOrderRequestCursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, itemMap, filter);
                        if (pendingOrderRequestCursor.getCount() > 0) {
                            pendingOrderRequestCursor.moveToFirst();
                            int itemno = 10;
                            String deliveryNo = "";
                            String documentDate = "";
                            do {
                                tempDeliveryNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_DELIVERY_NO));
                                tempPurchaseNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_ORDER_ID));
                                tempCustomerNumber = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CUSTOMER_NO));
                                deliveryNo = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_DELIVERY_NO));
                                documentDate = pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_DATE));
                                if (customerNumber.equals("")) {
                                    customerNumber = tempCustomerNumber;
                                }
                                if (deliveryNumber.equals("")) {
                                    deliveryNumber = tempDeliveryNumber;
                                }
                                if (purchaseNumber.equals("")) {
                                    purchaseNumber = tempPurchaseNumber;
                                } else if (purchaseNumber.equals(tempPurchaseNumber)) {

                                } else {

                                    if (deliveryNumber.equals(tempDeliveryNumber)) {

                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryDeleteRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        arrayList.add(object);
                                        Helpers.logData(getApplication(), "Delivery Delete Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Delivery Delete Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deliveryNumber = tempDeliveryNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }
                                /*if(customerNumber.equals(tempCustomerNumber)){
                                    OfflinePost object = new OfflinePost();
                                    object.setCollectionName(App.POST_COLLECTION);
                                    object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryDeleteRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                    object.setDeepEntity(deepEntity);
                                    arrayList.add(object);
                                    deliveryNumber = tempDeliveryNumber;
                                    purchaseNumber = tempPurchaseNumber;
                                    deepEntity = new JSONArray();
                                }
                                else{
                                    OfflinePost object = new OfflinePost();
                                    object.setCollectionName(App.POST_COLLECTION);
                                    object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryDeleteRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                    object.setDeepEntity(deepEntity);
                                    arrayList.add(object);
                                    purchaseNumber = tempPurchaseNumber;
                                    deliveryNumber = tempDeliveryNumber;
                                    customerNumber = tempCustomerNumber;
                                    deepEntity = new JSONArray();
                                }*/

                                }

                                if (pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("OrderId", deliveryNo);
                                    jo.put("Material", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_CASE)));
                                    //jo.put("ItemValue", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("UoM", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    //jo.put("Value", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("Storagelocation", "");
                                    jo.put("RejReason", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_REASON_CODE)));
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                } else {
                                    JSONObject jo = new JSONObject();
                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                    jo.put("OrderId", deliveryNo);
                                    jo.put("Material", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    jo.put("Description", UrlBuilder.decodeString(pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                                    jo.put("Plant", "");
                                    jo.put("Quantity", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UNIT)));
                                    // jo.put("ItemValue", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("UoM", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_UOM)));
                                    //jo.put("Value", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_AMOUNT)));
                                    jo.put("Storagelocation", "");
                                    jo.put("RejReason", pendingOrderRequestCursor.getString(pendingOrderRequestCursor.getColumnIndex(db.KEY_REASON_CODE)));
                                    jo.put("Route", Settings.getString(App.ROUTE));
                                    itemno = itemno + 10;
                                    deepEntity.put(jo);
                                }
                                //Check if cursor is at last position
                                if (pendingOrderRequestCursor.getPosition() == pendingOrderRequestCursor.getCount() - 1) {

                                    if (customerNumber.equals(tempCustomerNumber)) {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryDeleteRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        arrayList.add(object);
                                        Helpers.logData(getApplication(), "Delivery Delete Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Delivery Delete Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deliveryNumber = tempDeliveryNumber;
                                        deepEntity = new JSONArray();
                                    } else {
                                        OfflinePost object = new OfflinePost();
                                        object.setCollectionName(App.POST_COLLECTION);
                                        object.setMap(Helpers.buildHeaderMap(ConfigStore.CustomerDeliveryDeleteRequestFunction, deliveryNumber, ConfigStore.DeliveryDocumentType, customerNumber, "", purchaseNumber, documentDate));
                                        object.setDeepEntity(deepEntity);
                                        arrayList.add(object);
                                        Helpers.logData(getApplication(), "Delivery Delete Batch Header" + object.getMap().toString());
                                        Helpers.logData(getApplication(), "Delivery Delete Batch Body" + deepEntity.toString());

                                        purchaseNumber = tempPurchaseNumber;
                                        deliveryNumber = tempDeliveryNumber;
                                        customerNumber = tempCustomerNumber;
                                        deepEntity = new JSONArray();
                                    }

                                /*OfflinePost object = new OfflinePost();
                                object.setCollectionName(App.POST_COLLECTION);
                                object.setMap(Helpers.buildHeaderMap(ConfigStore.LoadRequestFunction, "", ConfigStore.DocumentType, "0000205005", "", purchaseNumber));
                                object.setDeepEntity(deepEntity);
                                arrayList.add(object);
                                deepEntity = new JSONArray();*/
                                }

                            }
                            while (pendingOrderRequestCursor.moveToNext());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.CollectionFunction: {
                    try {
                        JSONArray deepEntity = new JSONArray();
                        ArrayList<String> tempCustomers = new ArrayList<>();
                        ArrayList<String> tempCustomersDate = new ArrayList<>();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        itemMap.put(db.KEY_TIME_STAMP, "");
                        itemMap.put(db.KEY_AMOUNT_CLEARED, "");
                        itemMap.put(db.KEY_AMOUNT_PAY, "");
                        itemMap.put(db.KEY_INVOICE_AMOUNT, "");
                        itemMap.put(db.KEY_DUE_DATE, "");
                        itemMap.put(db.KEY_INVOICE_NO, "");

                        itemMap.put(db.KEY_CHEQUE_NUMBER, "");
                        itemMap.put(db.KEY_CHEQUE_DATE,"");
                        itemMap.put(db.KEY_CASH_AMOUNT, "");
                        itemMap.put(db.KEY_CHEQUE_AMOUNT, "");
                        itemMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL,"");
                        itemMap.put(db.KEY_CHEQUE_BANK_CODE, "");
                        itemMap.put(db.KEY_CHEQUE_BANK_NAME, "");

                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filter.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_COMPLETE);
                        Cursor pCCursor = db.getData(db.COLLECTION, itemMap, filter);
                        if (pCCursor.getCount() > 0) {
                            pCCursor.moveToFirst();
//                            do{
                            if (!tempCustomers.contains(pCCursor.getString(pCCursor.getColumnIndex(db.KEY_CUSTOMER_NO)))) {
                                tempCustomers.add(pCCursor.getString(pCCursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
//                                    tempCustomersDate.add(pCCursor.getString(pCCursor.getColumnIndex(db.KEY_TIME_STAMP)));
                            }
//                            }
//                            while (pCCursor.moveToNext());
                        }
                        for (int i = 0; i < tempCustomers.size(); i++) {
                            double amountcleared = 0;
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_AMOUNT_CLEARED, "");
                            map.put(db.KEY_INVOICE_NO, "");
//                            map.put(db.KEY_DUE_DATE,"");
//                            map.put(db.KEY_INVOICE_AMOUNT,"");
                            HashMap<String, String> filt = new HashMap<>();
                            filt.put(db.KEY_CUSTOMER_NO, tempCustomers.get(i).toString());
                            filt.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                            Cursor cursor = db.getData(db.COLLECTION, itemMap, filt);

                            JSONArray deep = new JSONArray();
                            if (cursor.getCount() > 0) {
                                cursor.moveToFirst();

                                do {
                                    JSONObject object = new JSONObject();
                                    amountcleared += Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT_PAY)));
                                    object.put("OrderId", cursor.getString(cursor.getColumnIndex(db.KEY_INVOICE_NO)));
                                    object.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT_PAY)));
                                    object.put("DueDate", cursor.getString(cursor.getColumnIndex(db.KEY_DUE_DATE)));
                                    object.put("DueAmount", cursor.getString(cursor.getColumnIndex(db.KEY_INVOICE_AMOUNT)));

                                    object.put("ChequeNo", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_NUMBER)));
                                    object.put("ChequeDate", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_DATE)));
                                    object.put("ChequeAmount", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_AMOUNT)));
                                    object.put("CashAmount", cursor.getString(cursor.getColumnIndex(db.KEY_CASH_AMOUNT)));
                                    object.put("ChequeBankCode", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_BANK_CODE)));
                                    object.put("ChequeBankDate", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_BANK_NAME)));


                                    deep.put(object);
                                }
                                while (cursor.moveToNext());
                            }
                            OfflinePost offlinePost = new OfflinePost();
                            offlinePost.setCollectionName(App.POST_COLLECTION);
                            offlinePost.setMap(Helpers.buildCollectionHeader(ConfigStore.CollectionFunction, tempCustomers.get(i).toString(), String.valueOf(amountcleared), Helpers.getCurrentTimeStampformateddmmyy(), Helpers.generateNumber(db, ConfigStore.Collection_PR_Type)));
                            offlinePost.setDeepEntity(deep);
                            Helpers.logData(getApplication(), "Collection Batch Header" + offlinePost.getMap().toString());
                            Helpers.logData(getApplication(), "Collection Batch Body" + deep.toString());

                            collectionList.add(offlinePost);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.PartialCollectionFunction: {
                    try {
                        JSONArray deepEntity = new JSONArray();
                        ArrayList<String> tempCustomers = new ArrayList<>();
                        ArrayList<String> tempCustomersDate = new ArrayList<>();
                        HashMap<String, String> itemMap = new HashMap<>();
                        itemMap.put(db.KEY_CUSTOMER_NO, "");
                        itemMap.put(db.KEY_TIME_STAMP, "");
                        itemMap.put(db.KEY_AMOUNT_CLEARED, "");
                        itemMap.put(db.KEY_INVOICE_NO, "");
                        itemMap.put(db.KEY_AMOUNT_PAY, "");
                        itemMap.put(db.KEY_INVOICE_AMOUNT, "");
                        itemMap.put(db.KEY_DUE_DATE, "");

                        itemMap.put(db.KEY_CHEQUE_NUMBER, "");
                        itemMap.put(db.KEY_CHEQUE_DATE,"");
                        itemMap.put(db.KEY_CASH_AMOUNT, "");
                        itemMap.put(db.KEY_CHEQUE_AMOUNT, "");
                        itemMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL,"");
                        itemMap.put(db.KEY_CHEQUE_BANK_CODE, "");
                        itemMap.put(db.KEY_CHEQUE_BANK_NAME, "");

                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        filter.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_PARTIAL);
                        Cursor pCCursor = db.getData(db.COLLECTION, itemMap, filter);
                        if (pCCursor.getCount() > 0) {
                            pCCursor.moveToFirst();
//                            do{
                            if (!tempCustomers.contains(pCCursor.getString(pCCursor.getColumnIndex(db.KEY_CUSTOMER_NO)))) {
                                tempCustomers.add(pCCursor.getString(pCCursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
//                                    tempCustomersDate.add(pCCursor.getString(pCCursor.getColumnIndex(db.KEY_TIME_STAMP)));
                            }
//                            }
//                            while (pCCursor.moveToNext());
                        }
                        for (int i = 0; i < tempCustomers.size(); i++) {
                            double amountcleared = 0;
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_AMOUNT_CLEARED, "");
                            itemMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL, "");
                            map.put(db.KEY_INVOICE_NO, "");
                            HashMap<String, String> filt = new HashMap<>();
                            filt.put(db.KEY_CUSTOMER_NO, tempCustomers.get(i).toString());
                            filt.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                            filt.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_PARTIAL);
                            Cursor cursor = db.getData(db.COLLECTION, itemMap, filt);

                            JSONArray deep = new JSONArray();
                            if (cursor.getCount() > 0) {
                                cursor.moveToFirst();
                                do {
                                    JSONObject object = new JSONObject();
                                    amountcleared += Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT_PAY)));
                                    object.put("OrderId", cursor.getString(cursor.getColumnIndex(db.KEY_INVOICE_NO)));
                                    object.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT_PAY)));
                                    object.put("DueDate", cursor.getString(cursor.getColumnIndex(db.KEY_DUE_DATE)));
                                    object.put("DueAmount", cursor.getString(cursor.getColumnIndex(db.KEY_INVOICE_AMOUNT)));

                                    object.put("ChequeNo", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_NUMBER)));
                                    object.put("ChequeDate", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_DATE)));
                                    object.put("ChequeAmount", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_AMOUNT)));
                                    object.put("CashAmount", cursor.getString(cursor.getColumnIndex(db.KEY_CASH_AMOUNT)));
                                    object.put("ChequeBankCode", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_BANK_CODE)));
                                    object.put("ChequeBankDate", cursor.getString(cursor.getColumnIndex(db.KEY_CHEQUE_BANK_NAME)));

                                    deep.put(object);
                                }
                                while (cursor.moveToNext());
                            }
                            OfflinePost offlinePost = new OfflinePost();
                            offlinePost.setCollectionName(App.POST_COLLECTION);
                            offlinePost.setMap(Helpers.buildCollectionHeader(ConfigStore.PartialCollectionFunction, tempCustomers.get(i).toString(), String.valueOf(amountcleared), Helpers.getCurrentTimeStampformateddmmyy(), Helpers.generateNumber(db, ConfigStore.Collection_PR_Type)));
                            offlinePost.setDeepEntity(deep);
                            Helpers.logData(getApplication(), "Partial Collection Batch Header" + offlinePost.getMap().toString());
                            Helpers.logData(getApplication(), "Partial Collection Batch Body" + deep.toString());

                            pcollectionList.add(offlinePost);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case ConfigStore.CollectionFunction + "D": {
                /*try{
                    JSONArray deepEntity = new JSONArray();
                    ArrayList<String>tempCustomers = new ArrayList<>();
                    HashMap<String, String> itemMap = new HashMap<>();
                    itemMap.put(db.KEY_CUSTOMER_NO,"");
                    // itemMap.put(db.KEY_DATE,"");
                    itemMap.put(db.KEY_AMOUNT_CLEARED,"");
                    itemMap.put(db.KEY_INVOICE_NO,"");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_IS_POSTED,App.DATA_MARKED_FOR_POST);
                    Cursor pCCursor = db.getData(db.DRIVER_COLLECTION,itemMap,filter);
                    if(pCCursor.getCount()>0){
                        pCCursor.moveToFirst();
                        double amountcleared = 0;
                        do{
                            JSONArray deep = new JSONArray();
                            if(pCCursor.getCount()>0){
                                pCCursor.moveToFirst();

                                do{
                                    JSONObject object = new JSONObject();
                                    amountcleared += Double.parseDouble(pCCursor.getString(pCCursor.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                    object.put("OrderId",pCCursor.getString(pCCursor.getColumnIndex(db.KEY_INVOICE_NO)));
                                    deep.put(object);
                                }
                                while (pCCursor.moveToNext());
                            }
                            OfflinePost offlinePost = new OfflinePost();
                            offlinePost.setCollectionName(App.POST_COLLECTION);
                            offlinePost.setMap(Helpers.buildCollectionHeader(ConfigStore.CollectionFunction, Settings.getString(App.DRIVER), String.valueOf(amountcleared)));
                            offlinePost.setDeepEntity(deep);
                            arrayList.add(offlinePost);
                        }
                        while (pCCursor.moveToNext());
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }*/
                    break;
                }
                case ConfigStore.UnloadFunction + "U": {
                    try {
                        String documentDate = "";
                        ArrayList<Unload> arrayListDebit = new ArrayList<>();
                        ArrayList<Unload> arrayListCredit = new ArrayList<>();
                        ArrayList<Unload> arrayListEndingInventory = new ArrayList<>();
                        for (ArticleHeader articleHeader : articles) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_ID, "");
                            map.put(db.KEY_TIME_STAMP, "");
                            map.put(db.KEY_REASON_CODE, "");
                            map.put(db.KEY_VARIANCE_TYPE, "");
                            map.put(db.KEY_TRIP_ID, "");
                            map.put(db.KEY_ITEM_NO, "");
                            map.put(db.KEY_MATERIAL_DESC1, "");
                            map.put(db.KEY_MATERIAL_NO, "");
                            map.put(db.KEY_MATERIAL_GROUP, "");
                            map.put(db.KEY_CASE, "");
                            map.put(db.KEY_UNIT, "");
                            map.put(db.KEY_UOM, "");
                            map.put(db.KEY_PRICE, "");
                            map.put(db.KEY_ORDER_ID, "");
                            map.put(db.KEY_PURCHASE_NUMBER, "");
                            map.put(db.KEY_IS_POSTED, "");
                            map.put(db.KEY_IS_PRINTED, "");
                            HashMap<String, String> filter = new HashMap<>();
                            filter.put(db.KEY_MATERIAL_NO, articleHeader.getMaterialNo());
                            filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                            Cursor c = db.getData(db.UNLOAD_VARIANCE, map, filter);


                            if (c.getCount() > 0) {
                                c.moveToFirst();
                                documentDate = c.getString(c.getColumnIndex(db.KEY_TIME_STAMP));
                                Unload unload = new Unload();
                                unload.setMaterial_no(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                                unload.setMaterial_description(c.getString(c.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                                unload.setItem_code(c.getString(c.getColumnIndex(db.KEY_ITEM_NO)));
                                String varianceType = c.getString(c.getColumnIndex(db.KEY_VARIANCE_TYPE));
                                unload.setUom(c.getString(c.getColumnIndex(db.KEY_UOM)));

                                HashMap<String, String> priceMap = new HashMap<>();
                                priceMap.put(db.KEY_AMOUNT, "");
                                HashMap<String, String> filterPrice = new HashMap<>();
                                filterPrice.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                                filterPrice.put(db.KEY_PRIORITY, "2");
                                Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                                if (priceCursor.getCount() > 0) {
                                    priceCursor.moveToFirst();
                                    unload.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                                } else {
                                    unload.setPrice("0");
                                }


                                tempPurchaseNumber = c.getString(c.getColumnIndex(db.KEY_ORDER_ID));
                                double cases = 0;
                                double units = 0;
                                do {
                                    cases += Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_CASE)));
                                    units += Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                                }
                                while (c.moveToNext());

                                unload.setCases(String.valueOf(cases));
                                unload.setPic(String.valueOf(units));
                                if (varianceType.equals(App.TRUCK_DAMAGE) || varianceType.equals(App.THEFT)) {
                                    arrayListDebit.add(unload);
                                } else if (varianceType.equals(App.ENDING_INVENTORY)) {
                                    arrayListEndingInventory.add(unload);
                                } else if (varianceType.equals(App.EXCESS)) {
                                    arrayListCredit.add(unload);
                                }

                            }
                        }
                        if (arrayListDebit.size() > 0) {
                            JSONArray deepEntity = new JSONArray();
                            int itemno = 10;
                            for (Unload unload : arrayListDebit) {
//                                if(unload.getUom().equals(App.CASE_UOM)||unload.getUom().equals(App.BOTTLES_UOM)){
                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", unload.getMaterial_no());
                                jo.put("Description", unload.getName());
                                jo.put("Plant", App.PLANT);
                                jo.put("Quantity", unload.getPic());
                                jo.put("ItemValue", unload.getPrice());
                                jo.put("UoM", unload.getUom());
                                jo.put("Value", unload.getPrice());
                                jo.put("Storagelocation", App.STORAGE_LOCATION);
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);
//                                }
//                                else{
//                                    JSONObject jo = new JSONObject();
//                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
//                                    jo.put("Material", unload.getMaterial_no());
//                                    jo.put("Description", unload.getName());
//                                    jo.put("Plant", App.PLANT);
//                                    jo.put("Quantity", unload.getCases());
//                                    jo.put("ItemValue", unload.getPrice());
//                                    jo.put("UoM", unload.getUom());
//                                    jo.put("Value", unload.getPrice());
//                                    jo.put("Storagelocation", App.STORAGE_LOCATION);
//                                    jo.put("Route", Settings.getString(App.ROUTE));
//                                    itemno = itemno + 10;
//                                    deepEntity.put(jo);
//                                }
                            }
                            OfflinePost offlinePost = new OfflinePost();
                            offlinePost.setCollectionName(App.POST_COLLECTION);
                            offlinePost.setMap(Helpers.buildHeaderMap(ConfigStore.UnloadFunction, "", ConfigStore.LoadVarianceDebit, Settings.getString(App.DRIVER), "", tempPurchaseNumber, documentDate));
                            offlinePost.setDeepEntity(deepEntity);
                            Helpers.logData(getApplication(), "Unload Debit Header" + offlinePost.getMap().toString());
                            Helpers.logData(getApplication(), "Unload Debit Batch Body" + deepEntity.toString());
                            unloadList.add(offlinePost);
                        }
                        if (arrayListEndingInventory.size() > 0) {
                            JSONArray deepEntity = new JSONArray();
                            int itemno = 10;
                            Double amount = 0.0;
                            for (Unload unload : arrayListEndingInventory) {
//                                if(unload.getUom().equals(App.CASE_UOM)||unload.getUom().equals(App.BOTTLES_UOM)){

                                Double qty = 0.0;
                                Double itemamount = 0.0;
                                if (Double.parseDouble(unload.getCases()) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, unload.getMaterial_no());
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = Double.parseDouble(unload.getPrice()) * Double.parseDouble(deno);
                                    itemamount += casePrice * Float.parseFloat(unload.getCases());
                                    qty = Float.parseFloat(unload.getCases()) * Double.parseDouble(deno);
                                }
                                if (Double.parseDouble(unload.getPic()) >= 1) {
                                    itemamount += Float.parseFloat(unload.getPrice()) * Float.parseFloat(unload.getPic());
                                    qty += Double.parseDouble(unload.getPic());
                                }
                                amount = amount + Double.parseDouble(String.format("%.2f", itemamount));
                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", unload.getMaterial_no());
                                jo.put("Description", unload.getName());
                                jo.put("Plant", App.PLANT);
                                jo.put("Quantity", String.format("%.2f", qty));
                                jo.put("ItemValue", unload.getPrice());
                                jo.put("UoM", unload.getUom());
                                jo.put("Value", String.format("%.2f", itemamount));
                                //jo.put("Vat", String.format("%.2f", itemamount * Double.parseDouble(Settings.getString(App.VATValue))));
                                jo.put("Storagelocation", App.STORAGE_LOCATION);
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);
//                                }
//                                else{
//                                    JSONObject jo = new JSONObject();
//                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
//                                    jo.put("Material", unload.getMaterial_no());
//                                    jo.put("Description", unload.getName());
//                                    jo.put("Plant", App.PLANT);
//                                    jo.put("Quantity", unload.getCases());
//                                    jo.put("ItemValue", unload.getPrice());
//                                    jo.put("UoM", unload.getUom());
//                                    jo.put("Value", unload.getPrice());
//                                    jo.put("Storagelocation", App.STORAGE_LOCATION);
//                                    jo.put("Route", Settings.getString(App.ROUTE));
//                                    itemno = itemno + 10;
//                                    deepEntity.put(jo);
//                                }
                            }
                            OfflinePost offlinePost = new OfflinePost();
                            offlinePost.setCollectionName(App.POST_COLLECTION);
                            offlinePost.setMap(Helpers.buildHeaderMap(ConfigStore.UnloadFunction, "", ConfigStore.EndingInventory, Settings.getString(App.DRIVER), String.format("%.2f", amount), tempPurchaseNumber, documentDate));
                            offlinePost.setDeepEntity(deepEntity);
                            Helpers.logData(getApplication(), "Ending Inventory Batch Header" + offlinePost.getMap().toString());
                            Helpers.logData(getApplication(), "Ending Inventory Batch Body" + deepEntity.toString());
                            unloadList.add(offlinePost);
                        }
                        if (arrayListCredit.size() > 0) {
                            JSONArray deepEntity = new JSONArray();
                            int itemno = 10;
                            for (Unload unload : arrayListDebit) {
//                                if(unload.getUom().equals(App.CASE_UOM)||unload.getUom().equals(App.BOTTLES_UOM)){
                                JSONObject jo = new JSONObject();
                                jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                                jo.put("Material", unload.getMaterial_no());
                                jo.put("Description", unload.getName());
                                jo.put("Plant", App.PLANT);
                                jo.put("Quantity", unload.getPic());
                                jo.put("ItemValue", unload.getPrice());
                                jo.put("UoM", unload.getUom());
                                jo.put("Value", unload.getPrice());
                                jo.put("Storagelocation", App.STORAGE_LOCATION);
                                jo.put("Route", Settings.getString(App.ROUTE));
                                itemno = itemno + 10;
                                deepEntity.put(jo);
//                                }
//                                else{
//                                    JSONObject jo = new JSONObject();
//                                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
//                                    jo.put("Material", unload.getMaterial_no());
//                                    jo.put("Description", unload.getName());
//                                    jo.put("Plant", App.PLANT);
//                                    jo.put("Quantity", unload.getCases());
//                                    jo.put("ItemValue", unload.getPrice());
//                                    jo.put("UoM", unload.getUom());
//                                    jo.put("Value", unload.getPrice());
//                                    jo.put("Storagelocation", App.STORAGE_LOCATION);
//                                    jo.put("Route", Settings.getString(App.ROUTE));
//                                    itemno = itemno + 10;
//                                    deepEntity.put(jo);
//                                }
                            }
                            OfflinePost offlinePost = new OfflinePost();
                            offlinePost.setCollectionName(App.POST_COLLECTION);
                            offlinePost.setMap(Helpers.buildHeaderMap(ConfigStore.UnloadFunction, "", ConfigStore.LoadVarianceCredit, Settings.getString(App.DRIVER), "", tempPurchaseNumber, documentDate));
                            offlinePost.setDeepEntity(deepEntity);
                            Helpers.logData(getApplication(), "ArrayList Credit Batch Header" + offlinePost.getMap().toString());
                            Helpers.logData(getApplication(), "ArrayList Credit Batch Body" + deepEntity.toString());
                            unloadList.add(offlinePost);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Settings.setString(App.IS_DATA_SYNCING, "false");
            Crashlytics.logException(e);
        }


    }

    public class syncData extends AsyncTask<Void, Void, Void> {
        ArrayList<OfflineResponse> data = new ArrayList<>();
        String value = "";

        @Override
        protected void onPreExecute() {

        }

        private syncData(String value) {
            this.value = value;
            Settings.setString(App.IS_DATA_SYNCING, "true");
            execute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.e("Going for Batch Request", "(Y)" + this.value);
            /*if(this.value.equals("BEGINDAY")){
                Log.e("Going for beginday","");
                this.data = IntegrationService.batchRequestBeginDay(getApplication(), App.POST_COLLECTION, beginDayList);
            }
            else if(this.value.equals("ODOMETER")){
                this.data = IntegrationService.batchRequestOdometer(getApplication(), App.POST_ODOMETER_SET, odometerList);
            }
            else if(this.value.equals("ADDCUSTOMER")){
                this.data = IntegrationService.batchRequestCustomer(getApplication(), App.POST_CUSTOMER_SET, customerList);
            }
            else{
                this.data = IntegrationService.batchRequest(getApplication(), App.POST_COLLECTION, arrayList);
            }*/


            if (this.value.equals(ConfigStore.BeginDayFunction)) {
                // this.data = IntegrationServiceJSON.batchRequestBeginDay(getApplication(), App.Start_DAY_URL, beginDayList);
            } else if (this.value.equals(ConfigStore.OdometerFunction)) {
//                this.data = IntegrationServiceJSON.batchRequestOdometer(getApplication(), App.POST_ODOMETER_SET, odometerList);
            } else if (this.value.equals(ConfigStore.AddCustomerFunction)) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.ADDCUST_POST_URL, customerList);
            } else if (this.value.equals(ConfigStore.LoadConfirmationFunction)) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.LOAD_CONFIRM_URL, loadList);
            } else if (this.value.equals(ConfigStore.LoadVarianceFunction + "D")) {
//                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.POST_COLLECTION, arrayList);
            } else if (this.value.equals(ConfigStore.LoadVarianceFunction + "C")) {
//                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.POST_COLLECTION, arrayList);
            } else if (this.value.equals(ConfigStore.LoadRequestFunction)) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.CUSTDEL_POST_URL, LoadreqList);
            } else if (this.value.equals(ConfigStore.CustomerOrderRequestFunction + "O")) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.CUSTDEL_POST_URL, orderreqList);
            } else if (this.value.equals(ConfigStore.InvoiceRequestFunction)) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.SALES_POST_URL, salesList);//New Post API
            } else if (this.value.equals(ConfigStore.ReturnsFunction + "G")) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.RETURN_POST_URL, returngList);//New Post API
            } else if (this.value.equals(ConfigStore.ReturnsFunction + "B")) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.RETURN_POST_URL, returnbList);//New Post API
            } else if (this.value.equals(ConfigStore.CustomerDeliveryRequestFunction)) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.CUSTDEL_POST_URL, custdelreqList);//New Post API
            } else if (this.value.equals(ConfigStore.CustomerDeliveryDeleteRequestFunction)) {
//                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.POST_COLLECTION, arrayList);
            } else if (this.value.equals(ConfigStore.VisitListFunction)) {
//                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.POST_COLLECTION, arrayList);
            } else if (this.value.equals(ConfigStore.CollectionFunction)) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.COLLECTION_POST_URL, collectionList);//New Post API
            } else if (this.value.equals(ConfigStore.PartialCollectionFunction)) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.COLLECTION_POST_URL, pcollectionList);//New Post API
            } else if (this.value.equals(ConfigStore.UnloadFunction + "U")) {
                this.data = IntegrationServiceJSON.batchRequest(getApplication(), App.UNLOAD_URL, unloadList);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("REturn data", "" + this.data.size());
            try {
                for (OfflineResponse response : this.data) {
                    Log.e("Resp Fun", "" + response.getFunction() + " " + response.getPurchaseNumber() + " " + response.getOrderID());
                    Helpers.logData(getApplication(), "ODATA Response" + response.getResponse_code() + "-" + response.getResponse_message()
                            + "-" + response.getFunction() + "-" + response.getOrderID() + "-" + response.getCustomerID());
                    switch (response.getFunction()) {
                        case ConfigStore.BeginDayFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_FUNCTION, ConfigStore.BeginDayFunction);
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.BeginDayFunction);
                                db.updateData(db.BEGIN_DAY, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_FUNCTION, ConfigStore.BeginDayFunction);
                                db.updateData(db.BEGIN_DAY, map, filter);
                            }

                            break;
                        }
                        case ConfigStore.OdometerFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, response.getCustomerID().equals("Y") ? App.DATA_IS_POSTED : App.DATA_MARKED_FOR_POST);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                Helpers.logData(getApplication(), "Going for Update" + "Odometer");
                                db.updateData(db.ODOMETER, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                db.updateData(db.ODOMETER, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.VisitListFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                Helpers.logData(getApplication(), "Going for Update" + "Visit List");
                                db.updateData(db.VISIT_LIST_POST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                db.updateData(db.VISIT_LIST_POST, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.LoadConfirmationFunction: {
                            if (response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_CUSTOMER_NO, Settings.getString(App.DRIVER));
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                db.updateData(db.LOAD_CONFIRMATION_HEADER, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_CUSTOMER_NO, Settings.getString(App.DRIVER));
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                Helpers.logData(getApplication(), "Going for Update" + "LCON");
                                db.updateData(db.LOAD_CONFIRMATION_HEADER, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.LoadVarianceFunction + "D": {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceDebit);
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.LoadVarianceDebit);
                                db.updateData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceDebit);
                                db.updateData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                            }

                            break;
                        }
                        case ConfigStore.LoadVarianceFunction + "C": {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.LoadVarianceCredit);
                                db.updateData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);
                                db.updateData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                            }

                            break;
                        }
                        case ConfigStore.LoadRequestFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.LoadRequestFunction);
                                db.updateData(db.LOAD_REQUEST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                db.updateData(db.LOAD_REQUEST, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.CustomerOrderRequestFunction + "O": {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                Helpers.logData(getApplication(), "Going for Update" + "Order Request");
                                db.updateData(db.ORDER_REQUEST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                db.updateData(db.ORDER_REQUEST, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.InvoiceRequestFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.InvoiceRequestFunction);
                                db.updateData(db.CAPTURE_SALES_INVOICE, map, filter);

                                HashMap<String, String> map1 = new HashMap<String, String>();
                                map1.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map1.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map1.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter1 = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter1.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                Helpers.logData(getApplication(), "Going for Update2" + ConfigStore.InvoiceRequestFunction);
                                db.updateData(db.CAPTURE_SALES_INVOICE, map1, filter1);

                                HashMap<String, String> returnFilter = new HashMap<>();
                                returnFilter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                returnFilter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                returnFilter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                if (db.checkData(db.RETURNS, returnFilter)) {
                                    Helpers.logData(getApplication(), "Going for Update" + "Returns");
                                    db.updateData(db.RETURNS, map, returnFilter);
                                }
                            } else {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                db.updateData(db.CAPTURE_SALES_INVOICE, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.ReturnsFunction + "G": {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                filter.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                                Helpers.logData(getApplication(), "Going for Update" + "Good Returns");
                                db.updateData(db.RETURNS, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                filter.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                                db.updateData(db.RETURNS, map, filter);
                            }

                            break;
                        }
                        case ConfigStore.ReturnsFunction + "B": {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                                Helpers.logData(getApplication(), "Going for Update" + "Bad Returns");
                                db.updateData(db.RETURNS, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                                db.updateData(db.RETURNS, map, filter);
                            }

                            break;
                        }
                        case ConfigStore.CustomerDeliveryRequestFunction: {

                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.CustomerDeliveryRequestFunction);
                                db.updateData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                db.updateData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.CustomerDeliveryDeleteRequestFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.CustomerDeliveryDeleteRequestFunction);
                                db.updateData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_ERROR);
                                map.put(db.KEY_ORDER_ID, response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                db.updateData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.CollectionFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> ivMap = new HashMap<>();
                                ivMap.put(db.KEY_AMOUNT_CLEARED, "");
                                ivMap.put(db.KEY_INVOICE_AMOUNT, "");

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                // map.put(db.KEY_ORDER_ID,response.getOrderID());
                                if (!response.getPurchaseNumber().equals("")) {
                                    map.put(db.KEY_INVOICE_NO, response.getPurchaseNumber());
                                }

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
//                                    if (!response.getPurchaseNumber().equals("")) {
//                                        filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
//                                    }
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());

                                Cursor prevAmnt = db.getData(db.COLLECTION, ivMap, filter);
                                double newinvAmount = 0;
                                if (prevAmnt.getCount() > 0) {
                                    prevAmnt.moveToFirst();
                                    newinvAmount = Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT))) -
                                            Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                }
                                if (!(newinvAmount == 0)) {
//                                    map.put(db.KEY_INVOICE_AMOUNT,String.valueOf(newinvAmount));
//                                    map.put(db.KEY_AMOUNT_CLEARED,String.valueOf("0"));
                                }
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.CollectionFunction);
                                HashMap<String,String>mapprint = new HashMap<String, String>();
                                mapprint.put(db.KEY_INVOICE_NO,"");
                                Cursor c = db.getData(db.COLLECTION,mapprint,filter);
                                if(c.getCount()>0){
                                    c.moveToFirst();
                                    try{
                                        String jsonString = c.getString(c.getColumnIndex(db.KEY_INVOICE_NO));
                                        HashMap<String,String>map1 = new HashMap<>();
                                        map1.put(db.KEY_ORDER_ID,response.getPurchaseNumber());
                                        HashMap<String, String> filter1 = new HashMap<>();
                                        filter1.put(db.KEY_ORDER_ID, jsonString);
                                        db.updateData(db.DELAY_PRINT,map1,filter1);
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                db.updateData(db.COLLECTION, map, filter);



                            } else {
                                HashMap<String, String> ivMap = new HashMap<>();
                                ivMap.put(db.KEY_AMOUNT_CLEARED, "");
                                ivMap.put(db.KEY_INVOICE_AMOUNT, "");

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                // map.put(db.KEY_ORDER_ID,response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                //filter.put(db.KEY_ORDER_ID,response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());

                                Cursor prevAmnt = db.getData(db.COLLECTION, ivMap, filter);
                                double newinvAmount = 0;
                                if (prevAmnt.getCount() > 0) {
                                    prevAmnt.moveToFirst();
                                    newinvAmount = Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT))) -
                                            Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                }
                                if (!(newinvAmount == 0)) {
//                                    map.put(db.KEY_INVOICE_AMOUNT,String.valueOf(newinvAmount));
//                                    map.put(db.KEY_AMOUNT_CLEARED,String.valueOf("0"));
                                }
                                db.updateData(db.COLLECTION, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.CollectionFunction + "D": {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> ivMap = new HashMap<>();
                                ivMap.put(db.KEY_AMOUNT_CLEARED, "");
                                ivMap.put(db.KEY_INVOICE_AMOUNT, "");

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                // map.put(db.KEY_ORDER_ID,response.getOrderID());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
//                                    if (!response.getPurchaseNumber().equals("")) {
//                                        filter.put(db.KEY_ORDER_ID, response.getPurchaseNumber());
//                                    }
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());

                                Cursor prevAmnt = db.getData(db.DRIVER_COLLECTION, ivMap, filter);
                                double newinvAmount = 0;
                                if (prevAmnt.getCount() > 0) {
                                    prevAmnt.moveToFirst();
                                    newinvAmount = Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT))) -
                                            Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                }
                                if (!(newinvAmount == 0)) {
//                                    map.put(db.KEY_INVOICE_AMOUNT,String.valueOf(newinvAmount));
//                                    map.put(db.KEY_AMOUNT_CLEARED,String.valueOf("0"));
                                }
                                Helpers.logData(getApplication(), "Going for Update" + "Driver Collection");
                                db.updateData(db.DRIVER_COLLECTION, map, filter);
                            } else {
                                HashMap<String, String> ivMap = new HashMap<>();
                                ivMap.put(db.KEY_AMOUNT_CLEARED, "");
                                ivMap.put(db.KEY_INVOICE_AMOUNT, "");

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                // map.put(db.KEY_ORDER_ID,response.getOrderID());
                                if (!response.getPurchaseNumber().equals("")) {
                                    map.put(db.KEY_INVOICE_NO, response.getPurchaseNumber());
                                }

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                //filter.put(db.KEY_ORDER_ID,response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());

                                Cursor prevAmnt = db.getData(db.DRIVER_COLLECTION, ivMap, filter);
                                double newinvAmount = 0;
                                if (prevAmnt.getCount() > 0) {
                                    prevAmnt.moveToFirst();
                                    newinvAmount = Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT))) -
                                            Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                }
                                if (!(newinvAmount == 0)) {
//                                    map.put(db.KEY_INVOICE_AMOUNT,String.valueOf(newinvAmount));
//                                    map.put(db.KEY_AMOUNT_CLEARED,String.valueOf("0"));
                                }
                                db.updateData(db.DRIVER_COLLECTION, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.PartialCollectionFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> ivMap = new HashMap<>();
                                ivMap.put(db.KEY_AMOUNT_CLEARED, "");
                                ivMap.put(db.KEY_INVOICE_AMOUNT, "");

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                                // map.put(db.KEY_ORDER_ID,response.getOrderID());

                                //Added by Rakshit on 09/04/2017 to store the partial collection temporarily
                                //Change no 20170409
                                HashMap<String, String> partMap = new HashMap<>();
                                partMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
//                                    if (!response.getOrderID().equals("")) {
//                                        filter.put(db.KEY_INVOICE_NO, response.getOrderID());
//                                        partMap.put(db.KEY_INVOICE_NO, response.getOrderID()); //Change no 20170409
//                                    }
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                partMap.put(db.KEY_CUSTOMER_NO, response.getCustomerID()); //Change no 20170409

                                Cursor prevAmnt = db.getData(db.COLLECTION, ivMap, filter);
                                double newinvAmount = 0;
                                if (prevAmnt.getCount() > 0) {
                                    prevAmnt.moveToFirst();
                                    newinvAmount = Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT))) -
                                            Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                    partMap.put(db.KEY_INVOICE_AMOUNT, prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT)));
                                    partMap.put(db.KEY_AMOUNT_CLEARED, prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                }
                                if (!(newinvAmount == 0)) {
//                                    map.put(db.KEY_INVOICE_AMOUNT,String.valueOf(newinvAmount));
//                                    map.put(db.KEY_AMOUNT_CLEARED,String.valueOf("0"));
                                }
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.PartialCollectionFunction);

                                db.updateData(db.COLLECTION, map, filter);
                                db.addData(db.PARTIAL_COLLECTION_TEMP, partMap); //Change no 20170409
                            } else {
                                HashMap<String, String> ivMap = new HashMap<>();
                                ivMap.put(db.KEY_AMOUNT_CLEARED, "");
                                ivMap.put(db.KEY_INVOICE_AMOUNT, "");

                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                // map.put(db.KEY_ORDER_ID,response.getOrderID());

                                //Added by Rakshit on 09/04/2017 to store the partial collection temporarily
                                //Change no 20170409
                                HashMap<String, String> partMap = new HashMap<>();
                                partMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                //filter.put(db.KEY_ORDER_ID,response.getPurchaseNumber());
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                partMap.put(db.KEY_CUSTOMER_NO, response.getCustomerID()); //Change no 20170409

                                Cursor prevAmnt = db.getData(db.COLLECTION, ivMap, filter);
                                double newinvAmount = 0;
                                if (prevAmnt.getCount() > 0) {
                                    prevAmnt.moveToFirst();
                                    newinvAmount = Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT))) -
                                            Double.parseDouble(prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));

                                    partMap.put(db.KEY_INVOICE_AMOUNT, prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_INVOICE_AMOUNT)));
                                    partMap.put(db.KEY_AMOUNT_CLEARED, prevAmnt.getString(prevAmnt.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                                }
                                if (!(newinvAmount == 0)) {
//                                    map.put(db.KEY_INVOICE_AMOUNT,String.valueOf(newinvAmount));
//                                    map.put(db.KEY_AMOUNT_CLEARED,String.valueOf("0"));
                                }
                                Log.e("Going Partial", "" + map);
                                Log.e("Going Filter", "" + filter);
                                db.updateData(db.COLLECTION, map, filter);
                                db.addData(db.PARTIAL_COLLECTION_TEMP, partMap); //Change no 20170409
                            }
                            break;
                        }
                        case ConfigStore.AddCustomerFunction: {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                Helpers.logData(getApplication(), "Going for Update" + ConfigStore.AddCustomerFunction);
                                db.updateData(db.NEW_CUSTOMER_POST, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                filter.put(db.KEY_CUSTOMER_NO, response.getCustomerID());
                                db.updateData(db.NEW_CUSTOMER_POST, map, filter);
                            }
                            break;
                        }
                        case ConfigStore.EndDayFunction: {

                            break;
                        }
                        case ConfigStore.UnloadFunction + "U": {
                            if (!response.getOrderID().equals("9999999999")) {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                // filter.put(db.KEY_CUSTOMER_NO,response.getCustomerID());
                                Helpers.logData(getApplication(), "Going for Update" + "Unload Function");
                                db.updateData(db.UNLOAD_VARIANCE, map, filter);
                                db.updateData(db.UNLOAD_TRANSACTION, map, filter);
                            } else {
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

                                HashMap<String, String> filter = new HashMap<>();
                                filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                                //filter.put(db.KEY_CUSTOMER_NO,response.getCustomerID());
                                db.updateData(db.UNLOAD_VARIANCE, map, filter);
                            }
                            break;
                        }
                    }
                }
                Settings.setString(App.IS_DATA_SYNCING, "false");
//                syncData();
            } catch (Exception e) {
                Settings.setString(App.IS_DATA_SYNCING, "false");
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }

    public int getSyncCount(String function) {
        int syncCount = 0;
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(db.KEY_TIME_STAMP, "");

            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

            switch (function) {
                case ConfigStore.BeginDayFunction: {
                    filter.put(db.KEY_FUNCTION, ConfigStore.BeginDayFunction);
                    Cursor beginDayRequest = db.getData(db.BEGIN_DAY, map, filter);
                    syncCount = beginDayRequest.getCount();
                    break;
                }
                case ConfigStore.OdometerFunction: {
                    Cursor odometerRequest = db.getData(db.ODOMETER, map, filter);
                    syncCount = odometerRequest.getCount();
                    break;
                }
                case ConfigStore.LoadConfirmationFunction: {
                    Cursor loadConfirmationRequest = db.getData(db.LOAD_CONFIRMATION_HEADER, map, filter);
                    syncCount = loadConfirmationRequest.getCount();
                    Log.e("LCO Count", "" + syncCount);
                    break;
                }
                case ConfigStore.LoadVarianceFunction + "D": {
                    filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceDebit);
                    Cursor loadVarianceDebitRequest = db.getData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                    syncCount = loadVarianceDebitRequest.getCount();
                    break;
                }
                case ConfigStore.LoadVarianceFunction + "C": {
                    filter.put(db.KEY_DOCUMENT_TYPE, ConfigStore.LoadVarianceCredit);
                    Cursor loadVarianceCreditRequest = db.getData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
                    syncCount = loadVarianceCreditRequest.getCount();
                    break;
                }
                case ConfigStore.LoadRequestFunction: {
                    Cursor loadRequest = db.getData(db.LOAD_REQUEST, map, filter);
                    syncCount = loadRequest.getCount();
                    break;
                }
                case ConfigStore.VisitListFunction: {
                    Cursor visitListRequest = db.getData(db.VISIT_LIST_POST, map, filter);
                    syncCount = visitListRequest.getCount();
                    break;
                }
                case ConfigStore.CustomerOrderRequestFunction + "O": {
                    Cursor orderRequest = db.getData(db.ORDER_REQUEST, map, filter);
                    syncCount = orderRequest.getCount();
                    break;
                }
                case ConfigStore.InvoiceRequestFunction: {
                    Cursor invoiceRequest = db.getData(db.CAPTURE_SALES_INVOICE, map, filter);
                    syncCount = invoiceRequest.getCount();
                    break;
                }
                case ConfigStore.CustomerDeliveryRequestFunction: {
                    Cursor deliveryRequest = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
                    syncCount = deliveryRequest.getCount();
                    break;
                }
                case ConfigStore.CustomerDeliveryDeleteRequestFunction: {
                    Cursor deliveryDeleteRequest = db.getData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, map, filter);
                    syncCount = deliveryDeleteRequest.getCount();
                    break;
                }
                //Case statement for Good Returns
                case ConfigStore.ReturnsFunction + "G": {
                    filter.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);
                    Cursor goodReturnsRequest = db.getData(db.RETURNS, map, filter);
                    syncCount = goodReturnsRequest.getCount();
                    Log.e("GR Sync Count", "" + syncCount);
                    break;
                }
                //Case Statement for Bad Returns
                case ConfigStore.ReturnsFunction + "B": {
                    filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                    Cursor goodReturnsRequest = db.getData(db.RETURNS, map, filter);
                    syncCount = goodReturnsRequest.getCount();
                    break;
                }
                case ConfigStore.CollectionFunction: {
                    filter.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_COMPLETE);
                    Cursor collectionRequest = db.getData(db.COLLECTION, map, filter);
                    syncCount = collectionRequest.getCount();
                    break;
                }
                case ConfigStore.PartialCollectionFunction: {
                    filter.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_PARTIAL);
                    Cursor collectionRequest = db.getData(db.COLLECTION, map, filter);
                    syncCount = collectionRequest.getCount();
                    break;
                }
            /*case ConfigStore.CollectionFunction+"D":{
                Cursor driverCollectionRequest = db.getData(db.DRIVER_COLLECTION,map,filter);
                syncCount = driverCollectionRequest.getCount();
                break;
            }*/
                case ConfigStore.AddCustomerFunction: {
                    Cursor newCustomerRequest = db.getData(db.NEW_CUSTOMER_POST, map, filter);
                    syncCount = newCustomerRequest.getCount();
                    break;
                }
                case ConfigStore.EndDayFunction: {

                    break;
                }
                case ConfigStore.UnloadFunction + "U": {
                    Cursor unloadFunction = db.getData(db.UNLOAD_VARIANCE, map, filter);
                    syncCount = unloadFunction.getCount();
                    Log.e("Unload Sync Count", "" + syncCount);
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }


        return syncCount;
    }

    public int getSyncCount() {
        int syncCount = 0;
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(db.KEY_TIME_STAMP, "");

            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);

            //Filter for Begin Day
            HashMap<String, String> bdFilter = new HashMap<>();
            bdFilter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
            bdFilter.put(db.KEY_FUNCTION, ConfigStore.BeginDayFunction);

            //Filter for good Return
            HashMap<String, String> grFilter = new HashMap<>();
            grFilter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
            grFilter.put(db.KEY_REASON_TYPE, App.GOOD_RETURN);

            //Filter for bad Return
            HashMap<String, String> brFilter = new HashMap<>();
            brFilter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
            brFilter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);

            Cursor beginDayRequest = db.getData(db.BEGIN_DAY, map, bdFilter);
            Cursor odometerRequest = db.getData(db.ODOMETER, map, filter);
            Cursor loadConfirmationRequest = db.getData(db.LOAD_CONFIRMATION_HEADER, map, filter);
            Cursor loadRequest = db.getData(db.LOAD_REQUEST, map, filter);
            Cursor orderRequest = db.getData(db.ORDER_REQUEST, map, filter);
            Cursor invoiceRequest = db.getData(db.CAPTURE_SALES_INVOICE, map, filter);
            Cursor deliveryRequest = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
            Cursor deliveryDeleteRequest = db.getData(db.CUSTOMER_DELIVERY_ITEMS_DELETE_POST, map, filter);
            Cursor goodReturnRequest = db.getData(db.RETURNS, map, grFilter);
            Cursor badReturnRequest = db.getData(db.RETURNS, map, brFilter);
            Cursor visitListCursor = db.getData(db.VISIT_LIST_POST, map, filter);
            Cursor newCustomerCursor = db.getData(db.NEW_CUSTOMER_POST, map, filter);
            Cursor loadVarianceCursor = db.getData(db.LOAD_VARIANCE_ITEMS_POST, map, filter);
            Cursor collectionCursor = db.getData(db.COLLECTION, map, filter);
            Cursor driverCollectionCursor = db.getData(db.DRIVER_COLLECTION, map, filter);
            Cursor unloadCursor = db.getData(db.UNLOAD_VARIANCE, map, filter);

            if (beginDayRequest.getCount() > 0) {
                syncCount += beginDayRequest.getCount();
            }
            if (odometerRequest.getCount() > 0) {
                syncCount += odometerRequest.getCount();
            }
            if (loadConfirmationRequest.getCount() > 0) {
                syncCount += loadConfirmationRequest.getCount();
            }
            if (loadRequest.getCount() > 0) {
                syncCount += loadRequest.getCount();
            }
            if (orderRequest.getCount() > 0) {
                syncCount += orderRequest.getCount();
            }
            if (invoiceRequest.getCount() > 0) {
                syncCount += invoiceRequest.getCount();
            }
            if (deliveryRequest.getCount() > 0) {
                syncCount += deliveryRequest.getCount();
            }
            if (deliveryDeleteRequest.getCount() > 0) {
                syncCount += deliveryDeleteRequest.getCount();
            }
            if (goodReturnRequest.getCount() > 0) {
                syncCount += goodReturnRequest.getCount();
            }
            if (badReturnRequest.getCount() > 0) {
                syncCount += badReturnRequest.getCount();
            }
            if (visitListCursor.getCount() > 0) {
                syncCount += visitListCursor.getCount();
            }
            if (loadVarianceCursor.getCount() > 0) {
                syncCount += loadVarianceCursor.getCount();
            }
            if (collectionCursor.getCount() > 0) {
                syncCount += collectionCursor.getCount();
            }
            if (driverCollectionCursor.getCount() > 0) {
                syncCount += driverCollectionCursor.getCount();
            }
            if (newCustomerCursor.getCount() > 0) {
                syncCount += newCustomerCursor.getCount();
            }
            if (unloadCursor.getCount() > 0) {
                syncCount += unloadCursor.getCount();
            }
            Log.e("Sync count", "" + syncCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return syncCount;
    }
}
