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
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.Banks;
import com.ae.benchmark.data.Const;
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
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.SpriteFactory;
import com.github.ybq.android.spinkit.Style;
import com.github.ybq.android.spinkit.sprite.Sprite;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.ae.benchmark.App.BASE_URL;
import static com.ae.benchmark.data.Const.TotalAPICOUNT;

public class DownloadingDataActivity extends Activity {

//    public static boolean appStatus=false;
//    public static String globalData="";
    private LoadingSpinner loadingSpinner;

    private static final String COLLECTION_NAME = BASE_URL+"login.php";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";
    private static final String TRIP_ID = "ITripId";
    public String username = "";
    public String password = "";
    SpinKitView spinKitView;

    DatabaseHandler db;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        db.getWritableDatabase();
    }

    TextView welcome;
    Handler h = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
//            welcome.setText("WELCOME "+ Const.APICOUNT);
            welcome.setText("WELCOME");
            h.postDelayed(r,100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloadingdata);
        db = DatabaseHandler.getInstance(getApplicationContext());
        spinKitView = (SpinKitView) findViewById(R.id.spin_kit_login);
        Style style = Style.values()[7];
        Sprite drawable = SpriteFactory.create(style);
        spinKitView.setIndeterminateDrawable(drawable);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        db = new DatabaseHandler(this);
        downloadData(Settings.getString(TRIP_ID));

        welcome = (TextView) findViewById(R.id.welcome);
        h.postDelayed(r,100);



    }


    public void downloadData(final String tripId){
        Helpers.logData(DownloadingDataActivity.this,"Downloading user data");
        db = DatabaseHandler.getInstance(getApplicationContext());
        db.getWritableDatabase();

        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_IS_BEGIN_DAY, App.FALSE);
        map.put(db.KEY_IS_LOAD_VERIFIED, App.FALSE);
        map.put(db.KEY_IS_END_DAY,App.FALSE);
        map.put(db.KEY_IS_UNLOAD,App.FALSE);
        db.addData(db.LOCK_FLAGS, map);

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
        TotalAPICOUNT = 14;
        chain.add(new Chain.Link() {
            @Override
            public void run() {
                        TripHeader.load(DownloadingDataActivity.this,tripId, db);

            }
        });

        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        LoadDelivery.load(DownloadingDataActivity.this,tripId, db);// JSON API Done

            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                CustomerHeaders.load(DownloadingDataActivity.this, tripId, db);// JSON API Done

            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        ArticleHeaders.load(DownloadingDataActivity.this, tripId, db);// JSON API Done

            }
        });

        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        VisitList.load(DownloadingDataActivity.this, tripId, db);

            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        DriverRouteFlags.load(DownloadingDataActivity.this,tripId,db);

            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        OrderReasons.load(DownloadingDataActivity.this,"",db);// JSON API Done


            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        OrderReasons.load(DownloadingDataActivity.this,App.REASON_REJECT,db);// JSON API Done


            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        VisitReasons.load(DownloadingDataActivity.this, "", db);// JSON API Done

            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        Pricing.load(DownloadingDataActivity.this, tripId, db);// JSON API Done

            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        Banks.load(DownloadingDataActivity.this,"",db);// JSON API Done

            }
        });
        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {
                CustomerHeaders.loadData(getApplicationContext());

            }
        });

        chain.add(new Chain.Link(){
            @Override
            public void run() throws Exception {

                        ArticleHeaders.loadData(getApplicationContext());

            }
        });

        chain.add(new Chain.Link(){
            @Override
            public void run() {
                OrderReasons.loadData(getApplicationContext());
            }
        });

        chain.start();

    }

    private void go() {
        h.removeCallbacks(r);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(DownloadingDataActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);

    }

    private void fail() {
        h.removeCallbacks(r);
            finish();
    }



}