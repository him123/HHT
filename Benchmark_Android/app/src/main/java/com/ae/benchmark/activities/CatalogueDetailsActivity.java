package com.ae.benchmark.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.CatelougeAdapter;
import com.ae.benchmark.models.Catelog_model;
import com.ae.benchmark.utils.AlertDialogClass;
import com.ae.benchmark.utils.NetWorkCheck;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dell on 12-02-2018.
 */

public class CatalogueDetailsActivity extends AppCompatActivity{

    TextView tv_cat_title,cat_wet_weight_title_st,cat_wet_weight_title_dy,cat_barcode_st,cat_barcode_dy,
            cat_shelf_life_st,cat_shelf_life_dy,tv_cat_ingrediants_st,tv_cat_ingrediants_dy,
            tv_nutrition_facts,tv_cat_energy,tv_cat_Fat,
            tv_cat_protine,tv_cat_carbo,tv_cat_calcuim,tv_cat_sodium,tv_cat_potassium,
            tv_cat_crude_fibre,tv_cat_vitamin_d;
    ImageView iv_add_to_cart,iv_image;
    Catelog_model model;
    String item_id;
    private ProgressDialog mProgress;
    public static AlertDialogClass ad;
    Context mctx;
    public static NetWorkCheck nk;
    ImageView iv_back,iv_cart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_catelogue_design);

        mctx = CatalogueDetailsActivity.this;
        nk = new NetWorkCheck(mctx);
        ad = new AlertDialogClass(mctx);
        iv_back = (ImageView) findViewById(R.id.iv_back);


        tv_cat_title = (TextView) findViewById(R.id.tv_cat_title);
        cat_wet_weight_title_st = (TextView) findViewById(R.id.cat_wet_weight_title_st);
        cat_wet_weight_title_dy = (TextView) findViewById(R.id.cat_wet_weight_title_dy);
        cat_barcode_st = (TextView) findViewById(R.id.cat_barcode_st);
        cat_barcode_dy = (TextView) findViewById(R.id.cat_barcode_dy);
        cat_shelf_life_st = (TextView) findViewById(R.id.cat_shelf_life_st);
        cat_shelf_life_dy = (TextView) findViewById(R.id.cat_shelf_life_dy);
        tv_cat_ingrediants_st = (TextView) findViewById(R.id.tv_cat_ingrediants_st);
        tv_cat_ingrediants_dy = (TextView) findViewById(R.id.tv_cat_ingrediants_dy);
        tv_nutrition_facts = (TextView) findViewById(R.id.tv_nutrition_facts);
        tv_cat_energy = (TextView) findViewById(R.id.tv_cat_energy);
        tv_cat_Fat = (TextView) findViewById(R.id.tv_cat_Fat);
        tv_cat_protine = (TextView) findViewById(R.id.tv_cat_protine);
        tv_cat_carbo = (TextView) findViewById(R.id.tv_cat_carbo);
        tv_cat_calcuim = (TextView) findViewById(R.id.tv_cat_calcuim);
        tv_cat_sodium = (TextView) findViewById(R.id.tv_cat_sodium);
        tv_cat_potassium = (TextView) findViewById(R.id.tv_cat_potassium);
        tv_cat_crude_fibre = (TextView) findViewById(R.id.tv_cat_crude_fibre);
        tv_cat_vitamin_d = (TextView) findViewById(R.id.tv_cat_vitamin_d);
        iv_add_to_cart = (ImageView) findViewById(R.id.iv_add_to_cart);
        iv_image = (ImageView) findViewById(R.id.iv_image);
        item_id = getIntent().getExtras().getString("item_id");

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try{

            if(nk.isConnectingToInternet()){
                makeSampleHttpRequest(mctx);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!getIntent().getExtras().getString("image").equalsIgnoreCase("")){

                    Intent i = new Intent(mctx,FullScreenImage.class);
                    i.putExtra("photos",getIntent().getExtras().getString("image"));
                    startActivity(i);

                }

            }
        });


    }
    public void makeSampleHttpRequest(final Context mctx) {
        String url = App.CATALOG_DETAILS_LIST_URL+"item_id="+item_id;

        showProgress();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            Log.d("WSRESPONSE", " RES FOS : " + response.toString());
                            JSONObject job = new JSONObject(response.toString());
                            JSONObject job1 = job.getJSONObject("d");

                            JSONArray jry2 = job1.getJSONArray("results");
                            for (int j = 0; j < jry2.length(); j++) {
                                JSONObject job2 = jry2.getJSONObject(j);
                                model = new Catelog_model();
                                model.setTitel(job2.getString("Name"));
                                model.setItem_id(job2.getString("item_id"));
                                model.setNet_weight(job2.getString("weight"));
                                try{
                                    model.setBarcode(job2.getString("barcode"));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                model.setShelf_life(job2.getString("shelf_life"));
                                model.setIngredints(job2.getString("ingredients"));
                                model.setEnergy(job2.getString("energy"));
                                model.setFat(job2.getString("fat"));
                                model.setProtin(job2.getString("protein"));
                                model.setCarbohydrate(job2.getString("carbohydrate"));
                                model.setCalcuim(job2.getString("calcium"));
                                model.setSodium(job2.getString("sodium"));
                                model.setPotassium(job2.getString("potassium"));
                                model.setCurde_fibre(job2.getString("crude_fibre"));
                                model.setVitamin(job2.getString("vitamin_d"));
                                model.setFlawer(job2.getString("flawer"));

                            }

                            if (model != null ) {
                                try{
                                    setData();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                stopProgress();
                            }



                        } catch (Exception e) {
                            stopProgress();
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mctx, error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                String deviceId = Settings.Secure.getString(mctx.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Map<String, String> params = new HashMap<String, String>();
//                params.put("item_id", item_id+ "");
//                params.put("user_id", User_Id + "");
                return params;
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag("MYGIG_FINDER");
        RequestQueue requestQueue = Volley.newRequestQueue(mctx);
        requestQueue.add(stringRequest);

    }

    private void stopProgress() {
        try {
            mProgress.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgress() {
        try {
            mProgress = ProgressDialog.show(mctx, "", "Loading...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setData() {


        tv_cat_title.setText(model.getTitel());
        cat_wet_weight_title_dy.setText(model.getNet_weight());
        cat_barcode_dy.setText(model.getBarcode());
        cat_shelf_life_dy.setText(model.getShelf_life());
        tv_cat_ingrediants_dy.setText(model.getIngredints());
        tv_cat_energy.setText(getResources().getString(R.string.energy) + model.getEnergy());
        tv_cat_Fat.setText(getResources().getString(R.string.fat) + model.getFat());
        tv_cat_protine.setText(getResources().getString(R.string.protin) + model.getProtin());
        tv_cat_carbo.setText(getResources().getString(R.string.carbohydrate) + model.getCarbohydrate());
        tv_cat_calcuim.setText(getResources().getString(R.string.calcuim) + model.getCalcuim());
        tv_cat_sodium.setText(getResources().getString(R.string.sodium) + model.getSodium());
        tv_cat_potassium.setText(getResources().getString(R.string.potassium) + model.getPotassium());
        tv_cat_crude_fibre.setText(getResources().getString(R.string.crude_fibre) + model.getCurde_fibre());
        tv_cat_vitamin_d.setText(getResources().getString(R.string.vitamin) + model.getVitamin());
        if (model.getSelected()) {

        }
        if (getIntent().getExtras().getString("image").equalsIgnoreCase("")) {

        } else {
            Picasso.with(mctx)
                    .load(getIntent().getExtras().getString("image"))
                    .placeholder(R.drawable.test)
                    .error(R.drawable.test)
                    .into(iv_image);
        }
    }
}
