package com.ae.benchmark.utils;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ae.benchmark.App;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.Banks;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.data.DriverRouteFlags;
import com.ae.benchmark.data.OrderReasons;
import com.ae.benchmark.sap.BackgroundJob;
import com.ae.benchmark.sap.BackgroundJobLower;
/**
 * Created by Rakshit on 17-Dec-16.
 */
public class Helpers {
    private static int kJobId = 0;
    private static final String arabic = "\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9";
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
    private static final String PHOTO_PREFIX = "GBC_IMG_";
    private static final String PHOTO_SUFFIX = ".gbc";
    private static final String TAG = "Helpers";
    private static final String ERROR_PHOTO_DELETE = "Unable to delete image file: ";
    private static final String ERROR_PHOTO_PERMISSIONS = "Unable to set as writable: ";
    public static final int TYPE_CAMERA_B1 = 40;
    public static final int TYPE_CAMERA_B2 = 41;
    public static final int TYPE_CAMERA_B3 = 42;
    public static final int TYPE_CAMERA_B4 = 43;
    private static final String FILE_CAMERA_LOCATION = "/DCIM/Camera";

    public static final int QUALITY_LOW = 20;
    public static final int QUALITY_MEDIUM = 40;
    public static final int QUALITY_HIGH = 80;

    public static String formatDate(Date date, String format) {
        if (date == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return dateFormat.format(date);
    }
    public static String convertArabicText(String arabicText){
        char[] chars = new char[arabicText.length()];
        for(int i=0;i<arabicText.length();i++) {
            char ch = arabicText.charAt(i);
            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';
            chars[i] = ch;
        }
        return new String(chars);
    }
    public static String formatTime(Date date, String format) {
        if (date == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return dateFormat.format(date);
    }

    public static String StringformatChange(String strDate, String original,String target) throws ParseException {
        DateFormat originalFormat = new SimpleDateFormat(original);
        DateFormat targetFormat = new SimpleDateFormat(target);
        Date date = originalFormat.parse(strDate);
        String formattedDate = targetFormat.format(date);
        return formattedDate;
    }

    public static String getCurrentTimeStamp() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return Settings.getString(App.LANGUAGE).equals("ar")? convertArabicText(timeStamp):timeStamp;
    }
    public static String getCurrentTimeStampAccessCode() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        return Settings.getString(App.LANGUAGE).equals("ar")? convertArabicText(timeStamp):timeStamp;
    }
    public static String getCurrentTimeStampformate() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return Settings.getString(App.LANGUAGE).equals("ar")? convertArabicText(timeStamp):timeStamp;
    }
    public static String getCurrentTimeStampformateddmmyy() {
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        return Settings.getString(App.LANGUAGE).equals("ar")? convertArabicText(timeStamp):timeStamp;
    }
    public static String getCurrentTime() {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        return Settings.getString(App.LANGUAGE).equals("ar")? convertArabicText(timeStamp):timeStamp;
    }
    public static String[] parseTimeStamp(String timeStamp) {
        //This method is used for parsing the timestamp and format to send to SAP
        String date = timeStamp.substring(0, 8);
        date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8) + "T"+getCurrentTime();//+"-"+date.substring(8,10);
        String time = timeStamp.substring(8, timeStamp.length());
        time = "PT" + time.substring(0, 2) + "H" + time.substring(2, 4) + "M" + time.substring(4, 6) + "S";
        String[] tokens = new String[]{date, time};
        return tokens;
    }
    public static String parseDateTimeforPost(String date) {
        String dateStr = "";
        if(date.contains("-")) {
            String[] dateArr = date.split("\\-");
            dateStr = dateArr[2] + "-" + dateArr[1] + "-" + dateArr[0] + "T00:00:00";
        }else{
            date = date.substring(0, 8);
            dateStr = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8) + "T00:00:00";//+"-"+date.substring(8,10);

        }
        return dateStr;
    }
    public static String parseDateforPost(String date) {
        String dateStr = "";
try {
    dateStr = date.substring(0, 10);
}catch (Exception e){
    e.printStackTrace();
}

        return dateStr;
    }

    public static String parseFullDateforPost(String date) {
        Date formateddate = null;
        String strdate=date;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
            formateddate = fmt.parse(date);
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            strdate= ft.format(formateddate);
        } catch (ParseException e) {
            strdate = parseHalfDateforPostyyyymmdd(date);
            e.printStackTrace();
        }
        return strdate;
    }
    public static String parseHalfDateforPost(String date) {
        Date formateddate = null;
        String strdate=date+" "+getCurrentTime();
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            formateddate = fmt.parse(strdate);
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            strdate= ft.format(formateddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strdate;
    }
    public static String parseHalfDateforPostyyyymmdd(String date) {
        String strdate=date+" "+getCurrentTime();
        return strdate;
    }

    public static void backupDatabase() {
        File dbFile = new File(App.APP_DB_PATH);
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(dbFile);
            outputStream = new FileOutputStream(App.APP_DB_BACKUP_PATH);
            while (true) {
                int i = inputStream.read();
                if (i != -1) {
                    outputStream.write(i);
                } else {
                    break;
                }
            }
            outputStream.flush();
            Log.e("Backup ok", "Backup ok");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void restoreDatabase() {
        File file = new File(App.APP_DB_BACKUP_PATH);
        Date lastModDate = new Date(file.lastModified());
        Log.e("Last modified date", "" + lastModDate);
    }
    public static Date formatDate(String date) {
        Date formatDate = null;
            String pattern1 = "/Date(";
            String pattern2 = ")/";
            Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
            Matcher m = p.matcher(date);
            while (m.find()) {
                long milli = Long.parseLong(date);
                formatDate = new Date(milli);
            }

        return formatDate;
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public static String generateVisitList(DatabaseHandler db){
        try{
            int numRange = 100;
            int length = 3;
            HashMap<String, String> search = new HashMap<>();
            search.put(db.KEY_DOC_TYPE, "VISITLIST");
            boolean checkPRNo = db.checkData(db.VISIT_LIST_ID_GENERATE, search);
            if (checkPRNo) {
                HashMap<String, String> prData = new HashMap<>();
                prData.put(db.KEY_VISITLISTID, "");
                Cursor cursor = db.getData(db.VISIT_LIST_ID_GENERATE, prData, search);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    numRange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(db.KEY_VISITLISTID)));
                    Log.e("Num Range From DB", "" + numRange);
                    numRange = numRange + 1;
                    HashMap<String, String> valueMap = new HashMap<>();
                    valueMap.put(db.KEY_VISITLISTID, String.valueOf(numRange));
                    db.updateData(db.VISIT_LIST_ID_GENERATE, valueMap, search);
                }
            } else {
                numRange = numRange <= 100 ? numRange + 1 : numRange;
                HashMap<String, String> valueMap = new HashMap<>();
                valueMap.put(db.KEY_DOC_TYPE, "VISITLIST");
                valueMap.put(db.KEY_VISITLISTID, String.valueOf(numRange));
                Log.e("Adding Data Num Range", "" + valueMap);
                db.addData(db.VISIT_LIST_ID_GENERATE, valueMap);
            }
            return StringUtils.leftPad(String.valueOf(numRange), length, "0");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
    public static String generateNumber(DatabaseHandler db, String documentType) {
        String route = Settings.getString(App.ROUTE);
        // int routeId = Integer.parseInt(route);
        int docTypeId = Integer.parseInt(getDocumentTypeNo(documentType));
        int numRange = 0;
        //int length = 5;
        HashMap<String, String> search = new HashMap<>();
        search.put(db.KEY_DOC_TYPE, documentType);
        boolean checkPRNo = db.checkData(db.PURCHASE_NUMBER_GENERATION, search);
        if (checkPRNo) {
            HashMap<String, String> prData = new HashMap<>();
            prData.put(db.KEY_PURCHASE_NUMBER, "");
            Cursor cursor = db.getData(db.PURCHASE_NUMBER_GENERATION, prData, search);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                numRange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                Log.e("Num Range From DB", "" + numRange);
                numRange = numRange + 1;
                HashMap<String, String> valueMap = new HashMap<>();
                valueMap.put(db.KEY_PURCHASE_NUMBER, String.valueOf(numRange));
                db.updateData(db.PURCHASE_NUMBER_GENERATION, valueMap, search);
            }
        } else {
            numRange = numRange <= 0 ? numRange + 1 : numRange;
            HashMap<String, String> valueMap = new HashMap<>();
            valueMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
            valueMap.put(db.KEY_ROUTE, Settings.getString(App.ROUTE));
            valueMap.put(db.KEY_DOC_TYPE, documentType);
            valueMap.put(db.KEY_PURCHASE_NUMBER, String.valueOf(numRange));
            Log.e("Adding Data Num Range", "" + valueMap);
            db.addData(db.PURCHASE_NUMBER_GENERATION, valueMap);
        }
        String tripID = "123";
        try {
            tripID = Settings.getString(App.SEQ);
        }catch (Exception e){

        }
        int length = tripID.length();
       return tripID + StringUtils.leftPad(String.valueOf(docTypeId),2,"0") + StringUtils.leftPad(String.valueOf(numRange), length, "0");
    }
    public static String generateCustomer(DatabaseHandler db, String documentType) {
        String route = Settings.getString(App.ROUTE);
        String customer = "CUS";
        // int routeId = Integer.parseInt(route);
        int docTypeId = Integer.parseInt(getDocumentTypeNo(documentType));
        int numRange = 0;
        //int length = 4;  //Commented to have unique customer number taking last 4 digits of Trip ID
        HashMap<String, String> search = new HashMap<>();
        search.put(db.KEY_DOC_TYPE, documentType);
        boolean checkPRNo = db.checkData(db.PURCHASE_NUMBER_GENERATION, search);
        if (checkPRNo) {
            HashMap<String, String> prData = new HashMap<>();
            prData.put(db.KEY_PURCHASE_NUMBER, "");
            Cursor cursor = db.getData(db.PURCHASE_NUMBER_GENERATION, prData, search);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                numRange = Integer.parseInt(cursor.getString(cursor.getColumnIndex(db.KEY_PURCHASE_NUMBER)));
                Log.e("Num Range From DB", "" + numRange);
                numRange = numRange + 1;
                HashMap<String, String> valueMap = new HashMap<>();
                valueMap.put(db.KEY_PURCHASE_NUMBER, String.valueOf(numRange));
                db.updateData(db.PURCHASE_NUMBER_GENERATION, valueMap, search);
            }
        } else {
            numRange = numRange <= 0 ? numRange + 1 : numRange;
            HashMap<String, String> valueMap = new HashMap<>();
            valueMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
            valueMap.put(db.KEY_ROUTE, Settings.getString(App.ROUTE));
            valueMap.put(db.KEY_DOC_TYPE, documentType);
            valueMap.put(db.KEY_PURCHASE_NUMBER, String.valueOf(numRange));
            Log.e("Adding Data Num Range", "" + valueMap);
            db.addData(db.PURCHASE_NUMBER_GENERATION, valueMap);
        }
        String tripID = Settings.getString(App.SEQ);
        int length = tripID.length();
        return customer + tripID + StringUtils.leftPad(String.valueOf(numRange), length-(String.valueOf(numRange).length()), "0");
    }
    public static String getDocumentTypeNo(String documentType) {
        String docTypeNo = "";
        switch (documentType) {
            case ConfigStore.LoadRequest_PR_Type: {
                docTypeNo = ConfigStore.LoadRequest_PR;
                break;
            }
            case ConfigStore.OrderRequest_PR_Type: {
                docTypeNo = ConfigStore.OrderRequest_PR;
                break;
            }
            case ConfigStore.InvoiceRequest_PR_Type: {
                docTypeNo = ConfigStore.InvoiceRequest_PR;
                break;
            }
            case ConfigStore.CustomerDeliveryRequest_PR_Type: {
                docTypeNo = ConfigStore.CustomerDeliveryRequest_PR;
                break;
            }
            case ConfigStore.CustomerNew_PR_Type: {
                docTypeNo = ConfigStore.CustomerNew_PR;
                break;
            }
            case ConfigStore.BeginDay_PR_Type: {
                docTypeNo = ConfigStore.BeginDay_PR;
                break;
            }
            case ConfigStore.EndDay_PR_Type: {
                docTypeNo = ConfigStore.EndDay_PR;
                break;
            }
            case ConfigStore.Odometer_PR_Type: {
                docTypeNo = ConfigStore.Odometer_PR;
                break;
            }
            case ConfigStore.GoodReturns_PR_Type: {
                docTypeNo = ConfigStore.GoodReturns_PR;
                break;
            }
            case ConfigStore.BadReturns_PR_Type: {
                docTypeNo = ConfigStore.BadReturns_PR;
                break;
            }
            case ConfigStore.TheftorTruck_PR_Type: {
                docTypeNo = ConfigStore.TheftorTruck_PR;
                break;
            }
            case ConfigStore.Excess_PR_Type: {
                docTypeNo = ConfigStore.Excess_PR;
                break;
            }
            case ConfigStore.EndingInventory_PR_Type: {
                docTypeNo = ConfigStore.EndingInventory_PR;
                break;
            }
            case ConfigStore.FreshUnload_PR_Type: {
                docTypeNo = ConfigStore.FreshUnload_PR;
                break;
            }
            case ConfigStore.CustomerDeliveryDelete_PR_Type:{
                docTypeNo = ConfigStore.CustomerDeliveryDelete_PR;
                break;
            }
            case ConfigStore.LoadVarianceDebit_PR_Type:{
                docTypeNo = ConfigStore.LoadVarianceDebit_PR;
                break;
            }
            case ConfigStore.LoadVarianceCredit_PR_Type:{
                docTypeNo = ConfigStore.LoadVarianceCredit_PR;
                break;
            }
            case ConfigStore.Collection_PR_Type:{
                docTypeNo = ConfigStore.Collection_PR;
                break;
            }
            case ConfigStore.Unload_PR_Type:{
                docTypeNo = ConfigStore.Unload_PR;
                break;
            }
        }
        return docTypeNo;
    }
    public static String getDayofWeek(String dateString) {
        int dayOfWeek = 0;
        String day = "";
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(dateString));
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            Log.e("Day of Week", "" + dayOfWeek);
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (dayOfWeek) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
        }
        return day;
    }
    public static String getMaskedValue(String value, int length) {
        return StringUtils.leftPad(value, length, "0");
    }
    public static void loadData(Context context) {
        Log.e("Helper Load", "Load Data");
        DriverRouteFlags.loadData(context);
        ArticleHeaders.loadData(context);
        CustomerHeaders.loadData(context);
        OrderReasons.loadData(context);
        Banks.loadData(context);

    }
    public static HashMap<String, String> buildHeaderMapLtLn(String function, String orderId, String documentType, String customerId,
                                                         String orderValue, String purchaseNumber, String documentDate, String latitude, String longitude) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Function", function);
        map.put("TripId",Settings.getString(App.TRIP_ID));
        map.put("OrderId", orderId.equals("") ? "0" : orderId);
        map.put("DocumentType", function.equals(ConfigStore.LoadRequestFunction)&&customerId.equals(Settings.getString(App.DRIVER))
                ?getDocumentTypefromDate(stringToDate(documentDate,App.DATE_PICKER_FORMAT))
                :documentType);
        if (!documentDate.equals("")) {
            map.put("DocumentDate", Helpers.parseDateforPost(documentDate));
        }

        // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
        // map.put("DocumentDate", null);
           /* map.put("PurchaseNum", Helpers.generateNumber(db,ConfigStore.LoadRequest_PR_Type));
            purchaseNumber = map.get("PurchaseNum");*/
        map.put("CustomerId", customerId);
        map.put("SalesOrg", Settings.getString(App.SALES_ORG));
        map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
        map.put("Division", Settings.getString(App.DIVISION));
        map.put("OrderValue", orderValue.equals("") ? "2000" : orderValue);
        map.put("Currency", App.CURRENCY);
        map.put("PurchaseNum", purchaseNumber);
        map.put("Latitude", latitude);
        map.put("Longitude",longitude);
        return map;
    }


    public static HashMap<String, String> buildHeaderMap(String function, String orderId, String documentType, String customerId,
                                                         String orderValue, String purchaseNumber, String documentDate) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Function", function);
        map.put("TripId",Settings.getString(App.TRIP_ID));
        map.put("uniqueid",Settings.getString(App.SEQ));
        map.put("OrderId", orderId.equals("") ? "0" : orderId);
        map.put("DocumentType", function.equals(ConfigStore.LoadRequestFunction)&&customerId.equals(Settings.getString(App.DRIVER))
                ?getDocumentTypefromDate(stringToDate(documentDate,App.DATE_PICKER_FORMAT))
                :documentType);
        if (!documentDate.equals("")) {
            switch (function) {
                case ConfigStore.InvoiceRequestFunction:
                    map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    break;
                case ConfigStore.UnloadFunction:
                    if(documentDate.length() == 10){
                        map.put("DocumentDate", Helpers.parseHalfDateforPostyyyymmdd(documentDate));
                    }else{
                        map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    }
                    break;
                case ConfigStore.LoadRequestFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CustomerOrderRequestFunction + "O":
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CollectionFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                default:
                    map.put("DocumentDate", Helpers.parseDateforPost(documentDate));
                    break;
            }
        }
        switch (function) {
            case ConfigStore.LoadRequestFunction:
                map.put("TransactionDate", Helpers.parseFullDateforPost(getCurrentTimeStamp()));
                break;
            default:
                break;
        }

        // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
        // map.put("DocumentDate", null);
           /* map.put("PurchaseNum", Helpers.generateNumber(db,ConfigStore.LoadRequest_PR_Type));
            purchaseNumber = map.get("PurchaseNum");*/
        map.put("CustomerId", customerId);
        map.put("SalesOrg", Settings.getString(App.SALES_ORG));
        map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
        map.put("Division", Settings.getString(App.DIVISION));
        map.put("OrderValue", orderValue.equals("") ? "0" : String.format("%.2f",Double.parseDouble(orderValue)));

        switch (function) {
            case ConfigStore.LoadRequestFunction:

                break;
            case ConfigStore.UnloadFunction:

                break;
            default:
                map.put("Vat", String.format("%.2f",Double.parseDouble(orderValue)*Double.parseDouble(Settings.getString(App.VATValue))));
                break;
        }
        map.put("Currency", App.CURRENCY);
        map.put("PurchaseNum", purchaseNumber);

        return map;
    }

    public static HashMap<String, String> buildHeaderMapOrder(String function, String orderId, String documentType, String customerId,
                                                         String orderValue, String purchaseNumber, String documentDate,String customerPO) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Function", function);
        Log.e("CustomerPO", "" + customerPO);
        map.put("TripId",customerPO.equals("000000")?Settings.getString(App.TRIP_ID):customerPO);
        map.put("uniqueid",Settings.getString(App.SEQ));
        map.put("OrderId", orderId.equals("") ? "0" : orderId);
        map.put("DocumentType", function.equals(ConfigStore.LoadRequestFunction)&&customerId.equals(Settings.getString(App.DRIVER))
                ?getDocumentTypefromDate(stringToDate(documentDate,App.DATE_PICKER_FORMAT))
                :documentType);
        if(!documentDate.equals("")){
            switch (function) {
                case ConfigStore.ReturnsFunction:
                    map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    break;
                case ConfigStore.InvoiceRequestFunction:
                    map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    break;
                case ConfigStore.LoadRequestFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CustomerOrderRequestFunction + "O":
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CollectionFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                default:
                    map.put("DocumentDate", Helpers.parseDateforPost(documentDate));
                    break;
            }
        }

        switch (function) {
            case ConfigStore.LoadRequestFunction:
                map.put("TransactionDate", Helpers.parseFullDateforPost(getCurrentTimeStamp()));
                break;

            default:
                break;
        }

        // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
        // map.put("DocumentDate", null);
           /* map.put("PurchaseNum", Helpers.generateNumber(db,ConfigStore.LoadRequest_PR_Type));
            purchaseNumber = map.get("PurchaseNum");*/
        map.put("CustomerId", customerId);
        map.put("SalesOrg", Settings.getString(App.SALES_ORG));
        map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
        map.put("Division", Settings.getString(App.DIVISION));
        map.put("OrderValue", orderValue.equals("") ? "0" : String.format("%.2f",Double.parseDouble(orderValue)));
        map.put("Vat", String.format("%.2f",Double.parseDouble(orderValue)*Double.parseDouble(Settings.getString(App.VATValue))));
        map.put("Currency", App.CURRENCY);
        map.put("PurchaseNum", purchaseNumber);
        return map;
    }

    public static HashMap<String, String> buildHeaderMapVisitList(String function, String startDateTimeStamp,
                                                                  String endDateTimeStamp, String visitID,
                                                                  String activityID,String visitReason,
                                                                  String customerId) {
        String[]startDateTime = new String[2];
        startDateTime = parseTimeStamp(startDateTimeStamp);
        String[]endDateTime = new String[2];
        endDateTime = parseTimeStamp(endDateTimeStamp);

        HashMap<String, String> map = new HashMap<>();
        map.put("Function", function);
        map.put("StartDate",startDateTime[0]);
        map.put("StartTime",startDateTime[1]);
        map.put("EndDate",endDateTime[0]);
        map.put("EndTime",endDateTime[1]);
        map.put("VisitID", StringUtils.leftPad(StringUtils.stripStart(visitID, "0"),5-visitID.length(),"0"));

        map.put("ActivityId",activityID);
        map.put("TripId",Settings.getString(App.TRIP_ID));
        map.put("uniqueid",Settings.getString(App.SEQ));
        map.put("VisitReason",visitReason);
        map.put("CustomerId",customerId);

        // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
        // map.put("DocumentDate", null);
           /* map.put("PurchaseNum", Helpers.generateNumber(db,ConfigStore.LoadRequest_PR_Type));
            purchaseNumber = map.get("PurchaseNum");*/
        return map;
    }

    public static HashMap<String, String> buildHeaderMapReason(String function, String orderId, String documentType, String customerId,
                                                         String orderValue, String purchaseNumber, String documentDate,String reasonCode) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Function", function);
        map.put("OrdReason",reasonCode);
        map.put("TripId",Settings.getString(App.TRIP_ID));
        map.put("uniqueid",Settings.getString(App.SEQ));
        map.put("OrderId", orderId.equals("") ? "" : orderId);
        map.put("DocumentType", function.equals(ConfigStore.LoadRequestFunction)&&customerId.equals(Settings.getString(App.DRIVER))
                ?getDocumentTypefromDate(stringToDate(documentDate,App.DATE_PICKER_FORMAT))
                :documentType);
        if(!documentDate.equals("")){
            switch (function) {
                case ConfigStore.ReturnsFunction:
                    map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    break;
                case ConfigStore.InvoiceRequestFunction:
                    map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    break;
                case ConfigStore.LoadRequestFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CustomerOrderRequestFunction + "O":
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CollectionFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                default:
                    map.put("DocumentDate", Helpers.parseDateforPost(documentDate));
                    break;
            }
        }

        // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
        // map.put("DocumentDate", null);
           /* map.put("PurchaseNum", Helpers.generateNumber(db,ConfigStore.LoadRequest_PR_Type));
            purchaseNumber = map.get("PurchaseNum");*/
        map.put("CustomerId", customerId);
        map.put("SalesOrg", Settings.getString(App.SALES_ORG));
        map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
        map.put("Division", Settings.getString(App.DIVISION));
        map.put("OrderValue", orderValue.equals("") ? "0" : String.format("%.2f",Double.parseDouble(orderValue)));
        map.put("Vat", String.format("%.2f",Double.parseDouble(orderValue)*Double.parseDouble(Settings.getString(App.VATValue))));
        map.put("Currency", App.CURRENCY);
        map.put("PurchaseNum", purchaseNumber);
        return map;
    }

    public static HashMap<String, String> buildBeginDayHeader(String function, String tripId,String createdBy,
                                                         String timestamp, String purchaseNumber) {
        String[] tokens = new String[2];
        tokens = Helpers.parseTimeStamp(timestamp);
        HashMap<String, String> map = new HashMap<>();
        map.put("Function", function);
        map.put("TripId",tripId);
        map.put("uniqueid",Settings.getString(App.SEQ));
        map.put("StartDate", tokens[0].toString());
        map.put("StartTime", tokens[1].toString());
        map.put("CreatedBy", createdBy);
        return map;
    }

    public static HashMap<String, String> buildCollectionHeader(String function, String customerId, String orderValue,String documentDate, String purchaseNumber){
        HashMap<String, String> map = new HashMap<>();
        map.put("Function", function);
        map.put("CustomerId", customerId);
        map.put("TripId",Settings.getString(App.TRIP_ID));
        map.put("uniqueid",Settings.getString(App.SEQ));
        map.put("RefCust",Settings.getString(App.DRIVER));
        map.put("OrderValue",orderValue);
        map.put("PurchaseNum", purchaseNumber);
        if(!documentDate.equals("")){
            switch (function) {
                case ConfigStore.ReturnsFunction:
                    map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    break;
                case ConfigStore.InvoiceRequestFunction:
                    map.put("DocumentDate", Helpers.parseFullDateforPost(documentDate));
                    break;
                case ConfigStore.LoadRequestFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CustomerOrderRequestFunction + "O":
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.CollectionFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                case ConfigStore.PartialCollectionFunction:
                    map.put("DocumentDate", Helpers.parseHalfDateforPost(documentDate));
                    break;
                default:
                    map.put("DocumentDate", Helpers.parseDateforPost(documentDate));
                    break;
            }
        }
        return map;
    }
    public static HashMap<String, String> buildOdometerHeader(String tripId,String value) {
        HashMap<String, String> map = new HashMap<>();
        map.put("TripID", tripId);
        map.put("uniqueid",Settings.getString(App.SEQ));
        map.put("Value", value);
        return map;
    }
    public static HashMap<String,String> buildnewCustomerHeader(String customerid,String ownername,String ownername_ar,
                                                                String tradename,String tradename_ar,String area,
                                                                String street,String crno, String pobox,
                                                                String email,String telephone,String fax,
                                                                String salesArea,String distribution,String division,
                                                                String latitude,String longitude){
        HashMap<String, String> map = new HashMap<>();
        map.put("Kunnr",customerid);
        map.put("Name1",ownername.replaceAll("%20"," "));
        map.put("Name2",ownername_ar);
        map.put("Name3",tradename.replaceAll("%20"," "));
        map.put("Name4",tradename_ar);
        map.put("Stras",street);
        map.put("Comments",crno);
        map.put("Pfach",pobox);
        map.put("EMail",email);
        map.put("Telf1",telephone);
        map.put("Telfx",fax);
        map.put("Vkorg",salesArea);
        map.put("Vtweg",distribution);
        map.put("Spart",division);
        map.put("Driver",Settings.getString(App.DRIVER));
        map.put("Latitude",latitude);
        map.put("Longitude",longitude);
        return map;
    }

    public static HashMap<String, String> buildLoadConfirmationHeader(String function,String orderID,String customerID) {
        HashMap<String, String> map = new HashMap<>();
        map.put("Function",function);
        map.put("OrderId", orderID);
        map.put("CustomerId", customerID);
        map.put("uniqueid",Settings.getString(App.SEQ));
try {
        map.put("table_flag", Settings.getString(orderID));

}catch (Exception e){
    e.printStackTrace();
}
        return map;
    }
    public static void createBackgroundJob(Context context){

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ComponentName mServiceComponent = new ComponentName(context, BackgroundJob.class);
            JobInfo.Builder builder = null;
            builder = new JobInfo.Builder(kJobId++, mServiceComponent);
            builder.setMinimumLatency(2 * 1000); // wait at least
            builder.setOverrideDeadline(5 * 1000); // maximum delay
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            builder.setRequiresDeviceIdle(false); // device should be idle
            builder.setRequiresCharging(false); // we don't care if the device is charging or not
            JobScheduler jobScheduler = (JobScheduler) context.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }
        else{
            long time = 2 * 1000;
            Intent intentAlarm = new Intent(context, BackgroundJobLower.class);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP,time, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        }

    }

    public static String addInvoiceDayToDate(String dtformat,String creditlimit)
    {
        Log.v("addInvoiceDayToDate",creditlimit+"--");
        SimpleDateFormat sdf = new SimpleDateFormat(dtformat);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date.
        c.add(Calendar.DATE, Integer.parseInt(creditlimit)); // Adding 5 days
        return sdf.format(c.getTime());
    }

    public static Date stringToDate(String strDate,String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = null;
        try{
            date = simpleDateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    //to check for Load Request
    public static String getDocumentTypefromDate(Date loadDate){
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date today = new Date();
            String todayDateStr = simpleDateFormat.format(today);
            Date todayDate = simpleDateFormat.parse(todayDateStr);
            return loadDate.compareTo(todayDate)==0?ConfigStore.LoadRequestCurrentDocumentType:loadDate.after(today)?ConfigStore.LoadRequestFutureDocumentType:"";
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
    public static void logData(Context context,String data){
        Logger logger = new Logger();
        if(context instanceof Activity){
            logger.appendLog((Activity)context,data);
        }
        else{
            logger.appendLog(context,data);
        }

    }
    public static String takePhoto(Activity activity,String buttonNumber) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = File.createTempFile(PHOTO_PREFIX, PHOTO_SUFFIX, activity.getCacheDir());
            if (!file.setWritable(true, false)) {
                Log.e(TAG, ERROR_PHOTO_PERMISSIONS + file.getAbsolutePath());
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            if(buttonNumber.equals("1")){
                activity.startActivityForResult(intent, TYPE_CAMERA_B1);
            }
            else if(buttonNumber.equals("2")){
                activity.startActivityForResult(intent, TYPE_CAMERA_B2);
            }
            else if(buttonNumber.equals("3")){
                activity.startActivityForResult(intent, TYPE_CAMERA_B3);
            }
            else if(buttonNumber.equals("4")){
                activity.startActivityForResult(intent, TYPE_CAMERA_B4);
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String imageToBase64(Context context, String path, int quality, boolean recycle) throws IOException {
        Bitmap image = getBitmap(context, path, quality);

        System.out.println("Origional size : "+image.getHeight() +" * "+image.getWidth());
        image= Bitmap.createScaledBitmap(image,100, 100, true);


        System.out.println("New size : "+image.getHeight() +" * "+image.getWidth());
        return bitmapToBase64(image, false, recycle);
    }
    public static String bitmapToBase64(Bitmap image, boolean isPNG, boolean recycle) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (isPNG) {
            image.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        } else {
            image.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        }

        byte[] byteArray = byteArrayOutputStream.toByteArray();

        if (recycle) {
            image.recycle();
        }

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
    public static Bitmap getBitmap(Context context, String path, int quality) throws IOException {
        File file = new File(path);

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        float ratio = (float) options.outWidth / (float) options.outHeight;

        boolean isLandscape = options.outWidth > options.outHeight;

        int width;
        int height;

        if (isLandscape) {
            width = quality * 15;
            height = Math.round(width * ratio);
        } else {
            height = quality * 15;
            width = Math.round(height * ratio);
        }

        int sampleSize = 1;

        if (options.outWidth > options.outHeight) {
            sampleSize = Math.round((float) options.outHeight / (float) height);
        } else if (options.outHeight > options.outWidth) {
            sampleSize = Math.round((float) options.outWidth / (float) width);
        }

        options.inSampleSize = sampleSize;
        options.inScaled = false;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        if (!file.delete()) {
            Log.e(TAG, ERROR_PHOTO_DELETE + file.getAbsolutePath());
        }

        ////new stamp
        Bitmap workingBitmap = Bitmap.createBitmap(image);
        Bitmap stampedBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(stampedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.RIGHT);

        String currentDateandTime = sdf.format(new Date());

        try {
            canvas.drawText(Settings.getString(App.DRIVER) +" - " + currentDateandTime,
                    stampedBitmap.getWidth() - 20, stampedBitmap.getHeight() - 28, paint);
        } catch (Exception e) {
            e.printStackTrace();
        }

        removeFromGallery(context);
        return stampedBitmap;
    }
    private static void removeFromGallery(Context context) {
        String bucketName = Environment.getExternalStorageDirectory().toString() + FILE_CAMERA_LOCATION;
        String bucketId = String.valueOf(bucketName.toLowerCase().hashCode());

        final String[] projection = {MediaStore.Images.Media.DATA};

        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";

        final String[] selectionArgs = {bucketId};

        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor.moveToFirst()) {
            try {
                final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                do {
                    final String path = cursor.getString(dataColumn);

                    File file = new File(path);

                    if (!file.delete()) {
                        Log.e(TAG, ERROR_PHOTO_DELETE + file.getAbsolutePath());
                    }
                } while (cursor.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cursor.close();
    }
}
