package com.ae.benchmark.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.utils.NumberToWord;
import com.ae.benchmark.utils.Settings;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
 * Created by Rakshit on 26-Dec-16.
 */
public class SplashScreen extends Activity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView gifImageView = (ImageView) findViewById(R.id.imageView);
//        gifImageView.startAnimation();

        Ion.with(gifImageView)
                .error(R.drawable.logo_fresh)
                .animateGif(AnimateGifMode.ANIMATE)
                .load("file:///android_asset/animation.gif");


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                if( Settings.getString("ITripId")==null) {
                    Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(i);
                }else{
                    Settings.setString(App.IS_DATA_SYNCING,"false");
                    Intent i = new Intent(SplashScreen.this, DashboardActivity.class);
                    startActivity(i);
                }
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }


}

