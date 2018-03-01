package com.ae.benchmark.activities;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ae.benchmark.App;

import com.ae.benchmark.Fragment.ShelfFragment;
import com.ae.benchmark.Fragment.StoreFragment;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.PagerAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.Distributions;
import com.ae.benchmark.models.ShelfProduct;
import com.ae.benchmark.utils.DatabaseHandler;

import static com.ae.benchmark.utils.DatabaseHandler.DISTRIBUTIONS;

/**
 * Created by eheuristic on 12/5/2016.
 */
public class ShelfStockActivity extends AppCompatActivity {

    public static String distributionsData="";
    private static DatabaseHandler db;
    ViewPager viewPager;
    TabLayout tabLayout;
    ImageView iv_back;
    TextView tv_top_header;
    ImageView toolbar_iv_back;
    ImageView iv_search;
    EditText et_search;
   public static int tab_position=0;
    FloatingActionButton button;
    FloatingActionButton fab;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_trip);
        db=new DatabaseHandler(this);

        button = (FloatingActionButton) findViewById(R.id.float_map);
        fab = (FloatingActionButton)findViewById(R.id.addCustomer);
        button.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        et_search = (EditText) findViewById(R.id.et_search_customer);
        viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Shelf"));
        tabLayout.addTab(tabLayout.newTab().setText("Store"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText("Shelf Stock");
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new PostDistribution().execute();
                finish();
            }
        });
        toolbar_iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        if (toolbar_iv_back != null) {
            toolbar_iv_back.setVisibility(View.VISIBLE);
        }
        iv_search = (ImageView) findViewById(R.id.iv_search2);
        if (iv_search != null) {
            iv_search.setVisibility(View.VISIBLE);
        }
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_search.setVisibility(View.GONE);
                et_search.setVisibility(View.VISIBLE);
                et_search.setHint("Search Products");
                toolbar_iv_back.setVisibility(View.GONE);
                tv_top_header.setVisibility(View.GONE);
            }
        });
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), "shelf");
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tab_position = tab.getPosition();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                tab_position = tab.getPosition();


            }
        });
        et_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (et_search.getRight() - et_search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        et_search.setVisibility(View.GONE);
                        iv_search.setVisibility(View.VISIBLE);
                        toolbar_iv_back.setVisibility(View.VISIBLE);
                        tv_top_header.setVisibility(View.VISIBLE);
                        if (tab_position == 0) {
                            ShelfFragment.adapter.getFilter().filter("");
                        } else {
                            StoreFragment.adapter.getFilter().filter("");
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v("addtext", "change");
                if (tab_position == 0) {
                    ShelfFragment.adapter.getFilter().filter(s.toString());
                } else {
                    StoreFragment.adapter.getFilter().filter(s.toString());
                }
                //planBadgeAdapter.notifyDataSetChanged();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (tab_position == 0) {
            if (ShelfFragment.arrayList != null && ShelfFragment.adapter != null) {
                ArrayList<ShelfProduct> productArrayList = new ArrayList<>();
                for (int i = 0; i < Const.addlist.size(); i++) {
                    ShelfProduct product = new ShelfProduct();
                    product.setProductname(Const.addlist.get(i));
                    product.setPro_case(0);
                    product.setPro_pcs(0);
                    productArrayList.add(product);

                    String param = Const.addlist.get(i).replaceAll(" ","%20");
                    distributionsData=db.getDistributionsData(DatabaseHandler.ARTICLE_HEADER ,param);

                }
                ShelfFragment.arrayList.addAll(productArrayList);
                ShelfFragment.adapter.notifyDataSetChanged();
            }
        } else {
            if (StoreFragment.arrayList != null && StoreFragment.adapter != null) {
                ArrayList<ShelfProduct> productArrayList = new ArrayList<>();
                for (int i = 0; i < Const.addlist.size(); i++) {
                    ShelfProduct product = new ShelfProduct();
                    product.setProductname(Const.addlist.get(i));
                    product.setPro_case(0);
                    product.setPro_pcs(0);
                    productArrayList.add(product);
                    String param = Const.addlist.get(i).replaceAll(" ","%20");
                    distributionsData=db.getDistributionsData(DatabaseHandler.ARTICLE_HEADER ,param);
                }
                StoreFragment.arrayList.addAll(productArrayList);
                StoreFragment.adapter.notifyDataSetChanged();
            }
        }
    }


    private class PostDistribution extends AsyncTask<String, Void, Void> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getApplicationContext());
            pDialog.setMessage("Posting...");
            pDialog.setIndeterminate(false);
            //  pDialog.show();
        }

        @Override
        protected Void doInBackground(String... Url)
        {


            postDistributions();

            return null;

        }

        @Override
        protected void onPostExecute(Void args) {

            pDialog.dismiss();

        }
    }





    public void postDistributions() {

        try {
            List<Distributions> dist = db.getDistributionPostData(DISTRIBUTIONS, "0");

            for (Distributions ds : dist) {
                JSONArray jsonArray = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("customer", ds.getCustomerID());
                object.put("shelf_Type", ds.getShelfType());
                object.put("shelf_Desc", ds.getShelfDescription());
                object.put("matnr", ds.getMaterialNo());
                object.put("maktx", ds.getMaterialName());
                object.put("comp_Quant", ds.getCases());
                object.put("quantity", ds.getPieces());
                object.put("quan_Date", ds.getDate());
                jsonArray.put(object);

                System.out.println("Posting data : "+jsonArray);
                httpRequest(jsonArray);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        if (node == null)
            return "null";
        return node.getNodeValue();
    }


    private static void httpRequest(JSONArray jsonArray) {

        try {

            String tripID = "Y000010000000001";
            String entitySet = "distributionCollection";
            String distributionPostUrl = "http://" + App.HOST + ":" + App.PORT + App.MERCHANDIZING_URL + entitySet + "/?$filter=%20IvCall%20eq%20%27P%27%20and%20IvJson%20eq%20%27" + Uri.encode(jsonArray.toString()) + "%27";

            System.out.println("URL : "+distributionPostUrl);

            URL url = new URL(distributionPostUrl);
            URI uri = url.toURI();
            HttpClient lHttpClient = new DefaultHttpClient();
            HttpGet lHttpGet = new HttpGet();
            lHttpGet.setURI(uri);
            lHttpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(App.MERCHANDIZING_SERVICE_USER, App.MERCHANDIZING_SERVICE_PASSWORD), "UTF-8", false));

            HttpResponse lHttpResponse = null;
            lHttpResponse = lHttpClient.execute(lHttpGet);
            InputStream lInputStream = null;
            lInputStream = lHttpResponse.getEntity().getContent();

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(lInputStream);

            Element element = doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("entry");

            String statusMessage="";

            for (int i = 0; i < nList.getLength(); i++) {

                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                    statusMessage= getValue("d:EMessage", element2);

                }
            }
            if(statusMessage.equals("null"))
            {

            }
            else
            {
                System.out.println("Distribution posted....");



                HashMap<String,String>map = new HashMap<>();
                map.put(db.KEY_IS_POSTED,"1");
                HashMap<String,String> filter = new HashMap<>();
                filter.put(db.KEY_IS_POSTED,"0");
                db.updateData(DISTRIBUTIONS, map, filter);
                System.out.println("record updated....");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
