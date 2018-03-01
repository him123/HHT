package com.ae.benchmark.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dell on 11-02-2018.
 */

public class CatelogueListActiviy extends AppCompatActivity {

    GridView gv_catalogue_list;
    public static ArrayList<Catelog_model> catalogue_list;
    public static ArrayList<Catelog_model> list_Cart;
    private ProgressDialog mProgress;
    public static AlertDialogClass ad;
    Context mctx;
    public static NetWorkCheck nk;
    ImageView iv_back,iv_cart,iv_cancle,search;
    LinearLayout action_search;
    EditText searchView;
    CatelougeAdapter  adpater;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalogue_list_activity);

        mctx = CatelogueListActiviy.this;
        nk = new NetWorkCheck(mctx);
        ad = new AlertDialogClass(mctx);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_cart = (ImageView) findViewById(R.id.iv_add_to_cart);
        gv_catalogue_list = (GridView) findViewById(R.id.gv_catalogue);
        action_search = (LinearLayout) findViewById(R.id.action_search);
        searchView = (EditText) findViewById(R.id.searchView);
        iv_cancle = (ImageView) findViewById(R.id.iv_cancle);
        search = (ImageView) findViewById(R.id.search_trade_in);
        catalogue_list = new ArrayList<>();
        adpater = new CatelougeAdapter((Activity) mctx, catalogue_list);



        try{

            if(nk.isConnectingToInternet()){
                makeSampleHttpRequest(mctx);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        gv_catalogue_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(mctx,CatalogueDetailsActivity.class);
                i.putExtra("item_id",catalogue_list.get(position).getItem_id());
                i.putExtra("image",catalogue_list.get(position).getImg_url());
                startActivity(i);
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_Cart = new ArrayList<>();
                for (int i = 0; i < catalogue_list.size(); i++) {

                    if(catalogue_list.get(i).getSelected()){
                        list_Cart.add(catalogue_list.get(i));
                    }

                }

                if(list_Cart.size() == 0){
                    ad.sHowDialog("Your Cart is Empty!");
                }else{
                    Intent i = new Intent(mctx,CartListActivity.class);
                    startActivity(i);
                }


            }
        });


        // CallWebServiceTradeIn();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action_search.setVisibility(View.VISIBLE);
                searchView.requestFocus();

            }
        });


        iv_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                action_search.setVisibility(View.GONE);
            }
        });
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                String text = s.toString();
                Log.i("","");
                //doSomething();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(adpater != null) {
                    adpater.getFilter().filter(s);
                }

            }
        });

    }

    public void makeSampleHttpRequest(final Context mctx) {

        showProgress();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, App.CATALOG_LIST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            Log.d("WSRESPONSE", " RES FOS : " + response.toString());
                            JSONObject job = new JSONObject(response.toString());
                            JSONObject job1 = job.getJSONObject("d");

//                                currentPage++;
                                JSONArray jry2 = job1.getJSONArray("results");
                                catalogue_list = new ArrayList<>();
                                for (int j = 0; j < jry2.length(); j++) {
                                    JSONObject job2 = jry2.getJSONObject(j);
                                    Catelog_model model = new Catelog_model();
                                    model.setTitel(job2.getString("Name"));
                                    model.setItem_id(job2.getString("item_id"));
                                    model.setPrice(job2.getString("price"));
                                    model.setImg_url(job2.getString("imag"));


//                                    model.setMedia_id(job2.getString("media_id"));
//                                    model.setTitle(job2.getString("title"));
//                                    model.setLink(job2.getString("file"));
//                                    model.setThumbnail(job2.getString("thumbnail"));
//                                    model.setFile_type(job2.getString("file_type"));
//                                    model.setUser_id(job2.getString("user_id"));
//                                    model.setType(job2.getString("type"));
//                                    model.setDesc(job2.getString("description"));
//                                    model.setRelease_date(job2.getString("datetime"));
//                                    JSONObject job_3 = job2.getJSONObject("user_data");
//                                    model.setFirstname(job_3.getString("firstname"));
//                                    model.setLastname(job_3.getString("lastname"));

                                    catalogue_list.add(model);
                                }


                                if (catalogue_list.size() > 0) {
                                    adpater = new CatelougeAdapter((Activity) mctx, catalogue_list);
                                    gv_catalogue_list.setAdapter(adpater);
                                    stopProgress();
                                }



                        } catch (Exception e) {
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
//                params.put("page", currentPage + "");
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
}
