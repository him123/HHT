package com.ae.benchmark.activities;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.Banks;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.data.DriverRouteFlags;
import com.ae.benchmark.data.LoadDelivery;
import com.ae.benchmark.data.OrderReasons;
import com.ae.benchmark.data.Pricing;
import com.ae.benchmark.data.TripHeader;
import com.ae.benchmark.data.VisitList;
import com.ae.benchmark.data.VisitReasons;
import com.ae.benchmark.sap.IntegrationServiceJSON;
import com.ae.benchmark.utils.Chain;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.SecureStore;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.ae.benchmark.App.BASE_URL;

public class LoginActivity extends Activity {

//    public static boolean appStatus=false;
//    public static String globalData="";
    private LoadingSpinner loadingSpinner;

    private static final String COLLECTION_NAME = BASE_URL+"login.php";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";
    private static final String TRIP_ID = "ITripId";
    public String username = "";
    public String password = "";


    DatabaseHandler db = new DatabaseHandler(this);
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        db.getWritableDatabase();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        db = new DatabaseHandler(this);

        if(App.ENVIRONMENT.equals("Development")){
                ((EditText) findViewById(R.id.username)).setText("");
                ((EditText) findViewById(R.id.password)).setText("");
        }


        setAppInfo();
        if( Boolean.parseBoolean(Settings.getString(App.IS_LOGGED_ID))){
            Settings.setString(App.IS_LOGGED_ID, "false");
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);

            startActivityForResult(intent, 0);
            finish();

            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
        Settings.setString(App.IS_LOGGED_ID, "false");
        Settings.setString(App.LOGIN_DATE, "");
        if(Settings.getString(App.LANGUAGE)==null){
            Settings.setString(App.LANGUAGE,"en");
        }
        Helpers.logData(LoginActivity.this, "On Login Screen");
        loadingSpinner = new LoadingSpinner(this);
//        if(LoginActivity.appStatus)
//        {
//            ((EditText) findViewById(R.id.username)).setText(LoginActivity.globalData);
//            ((EditText) findViewById(R.id.password)).setText(LoginActivity.globalData);
//            Button loginClick=(Button)findViewById(R.id.btn_login);
//            loginClick.performClick();
//        }
    }

    public void setAppInfo(){
        TextView appinfo = (TextView)findViewById(R.id.tv_appinfo);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)appinfo.getLayoutParams();
        params.setMargins(0, 110, 0, -300);
        appinfo.setLayoutParams(params);*/
        StringBuilder sb = new StringBuilder();
        sb.append("App Ver:");
        sb.append(pInfo.versionName + " - " + pInfo.versionCode);
        sb.append("\t \t");
        sb.append("Build:");
        sb.append(App.ENVIRONMENT);
        /*sb.append("\n");
        sb.append("Copyright" + "\u00A9" + "; Engineering Office");*/
        // sb.append("\u00A9");
        appinfo.setTextSize(12);
        appinfo.setText(sb.toString());
    }


    public void login(View view)
    {
        String id = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        //id = "E102964";
        //password = "E102964";

        Helpers.logData(LoginActivity.this,"Login Credentials for user:" + id + "/" + password);
        if (id.isEmpty()) {
            Toast.makeText(this, R.string.enter_employee_id, Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show();
        } else {
            loadingSpinner.show();
            if(Helpers.isNetworkAvailable(LoginActivity.this)){
                Helpers.logData(LoginActivity.this,"Network Available. Logging in user");
                this.username = id;
                this.password = password;
                new LoginUser(id, password);
            }
            else{
                //Fetch Credentials from db for offline authentication
                Helpers.logData(LoginActivity.this,"Network is not available. And user is already logged in. Doing relogin");
                this.username = id;
                this.password = password;
                HashMap<String,String>map = new HashMap<>();
                map.put(db.KEY_USERNAME,"");
                map.put(db.KEY_PASSWORD,"");
                map.put(db.KEY_SYM, "");
                map.put(db.KEY_IV,"");
                HashMap<String,String>filter = new HashMap<>();
                filter.put(db.KEY_DATE,Helpers.formatDate(new Date(),App.DATE_FORMAT));
                Cursor c = db.getData(db.LOGIN_CREDENTIALS,map,filter);
                if(c.getCount()>0){
                    byte[]sym = c.getString(c.getColumnIndex(db.KEY_SYM)).getBytes();
                    byte[]iv = c.getString(c.getColumnIndex(db.KEY_IV)).getBytes();
                    String passwd = SecureStore.decryptData(sym,iv,c.getString(c.getColumnIndex(db.KEY_PASSWORD)));
                    if(this.username.equals(c.getString(c.getColumnIndex(db.KEY_USERNAME)))&&this.password.equals(passwd)){
                        Helpers.logData(LoginActivity.this,"User Authentication offline success");
                        Settings.setString(App.IS_DATA_SYNCING,"false");
                        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                        startActivityForResult(intent, 0);
                        finish();
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    }
                    else{
                        Helpers.logData(LoginActivity.this,"Wrong Credentials");
                        if(loadingSpinner.isShowing()){
                            loadingSpinner.hide();
                        }
                        Toast.makeText(getApplicationContext(),getString(R.string.credentials_mismatch),Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                    alertDialogBuilder.setTitle(R.string.internet_available_title)
                            .setMessage(R.string.internet_available_msg)
                            .setCancelable(false)
                            .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(loadingSpinner.isShowing()){
                                        loadingSpinner.hide();
                                    }
                                    dialog.dismiss();
                                }
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            }
        }
    }

    public class LoginUser extends AsyncTask<Void,Void,Void>{
        private String username;
        private String password;
        private String url;
        private ArrayList<String>returnList;

        public LoginUser(String username, String password) {
            this.username = username;
            this.password = password;
            this.returnList = new ArrayList<>();
            HashMap<String, String> map = new HashMap<>();
            map.put(USERNAME, username);
            map.put(PASSWORD, password);
            this.url = UrlBuilder.build(COLLECTION_NAME, null, map);
            execute();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                //Login the user
                System.out.println("ura arrays : "+this.url);
                this.returnList =  IntegrationServiceJSON.loginUser(LoginActivity.this,this.username,this.password,this.url);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
                //Log.e("Return List", "" + this.returnList.size());
                if(this.returnList.size()>0){
                    if(this.returnList.get(2).contains("Trip")){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                        alertDialogBuilder.setTitle(getString(R.string.message))
                                .setMessage(this.returnList.get(2))
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.Continue_text), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadingSpinner.show();

                                        dialog.dismiss();
                                        if(loadingSpinner.isShowing()){
                                            loadingSpinner.hide();
                                        }
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                    else if(this.returnList.get(2).contains("Incorrect")){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                        alertDialogBuilder.setTitle("Message")
                                .setMessage(this.returnList.get(2))
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
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
                    }else if(this.returnList.get(2).contains("null")) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                        alertDialogBuilder.setTitle(getString(R.string.message))
                                .setMessage("Incorrect username or password")
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                    else{
                        boolean checkTripID = checkTripID(this.returnList.get(2));
                        if(!checkTripID){
                            clearDatabase();
                            Settings.setString(App.IS_DATA_SYNCING,"false");
                            Settings.setString(TRIP_ID, this.returnList.get(2));

                            //0 = On, 1 = Off
                            Settings.setString(App.IS_MESSAGE_DISPLAY, "1");
                            Settings.setString(App.IS_ODDOMETER_DISPLAY, "0");

                            Settings.setString(App.LANGUAGE, "en");

                            //Settings.setString(App.IS_LOGGED_ID,"true");
                            Settings.setString(App.LOGIN_DATE,Helpers.formatDate(new Date(), App.DATE_FORMAT));
                            db.addLoginCredentials(username, password,Helpers.formatDate(new Date(),App.DATE_FORMAT));

                            Intent intent = new Intent(LoginActivity.this, DownloadingDataActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            Settings.setString(App.IS_DATA_SYNCING,"false");
                            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                            startActivityForResult(intent, 0);
                            finish();
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        }
                    }

                }
                else{
                    Toast.makeText(LoginActivity.this,R.string.request_timeout,Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
    public boolean checkTripID(String trip_Id){

        String tripID = "";
        tripID = Settings.getString(TRIP_ID);
        boolean returnVal = false;
        if(tripID == null){
            returnVal = false;
        }
        else if(tripID.isEmpty()){
            returnVal =  false;
        }
        else if(tripID.equals(trip_Id)){
            returnVal =  true;
        }
        else if(!tripID.equals(trip_Id)){
            returnVal = false;
        }

        return returnVal;
    }

    private void clearDatabase(){
        Settings.clearPreferenceStore();
        LoginActivity.this.deleteDatabase("benchmark.db");
    }



}