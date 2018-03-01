package com.ae.benchmark.utils;
/**
 * Created by Rakshit on 14-Dec-16.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.ae.benchmark.data.Const;

import java.util.ArrayList;

public class Chain {

    private ArrayList<Link> links = new ArrayList<>();

    private Link done;
    private Link fail;

    private boolean running;
    public Chain(Link done) {
        this.done = done;
    }

    public void add(Link link) {
        if (!running) {
            links.add(link);
        }
    }

    public void setFail(Link fail) {
        this.fail = fail;
    }

    public void start() {
        running = true;

        new BladeRunner().execute(links.toArray(new Link[links.size()]));
    }

    public static class Link {
        public void run() throws Exception {
        }
    }

    private class BladeRunner extends AsyncTask<Link, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Link... params) {

            try {
                Const.APICOUNT = (Const.TotalAPICOUNT - links.size())+"/"+Const.TotalAPICOUNT;
                params[0].run();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean finished) {
            if(links.size()>1){
                links.remove(0);
                start();
            }else{
                links.clear();
                running = false;
                try {
                    if (finished) {
                        done.run();
                    } else {
                        fail.run();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }





        }
    }
}
