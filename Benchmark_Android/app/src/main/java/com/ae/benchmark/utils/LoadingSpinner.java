package com.ae.benchmark.utils;
/**
 * Created by Rakshit on 15-Nov-16.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;

import com.ae.benchmark.R;

public class LoadingSpinner {

    private Context context;

    private ProgressDialog progressDialog;

    public LoadingSpinner(Context context) {
        this.context = context;
        try {
            progressDialog = new ProgressDialog(context);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.setMessage(context.getString(R.string.loading));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LoadingSpinner(Context context, String message) {
        this.context = context;
        try {
            progressDialog = new ProgressDialog(context);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isShowing() {
        return progressDialog.isShowing();
    }

    public void show() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (((Activity) context).isDestroyed()) return;
            }
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        progressDialog.show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hide() {
        try {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
