package com.ae.benchmark.activities;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.DialogPriceSurveyAdapter;
import com.ae.benchmark.adapters.PriceSurveyAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

import static com.ae.benchmark.utils.DatabaseHandler.PRICE_SURVEY_POST;

/**
 * Created by Rakshit on 26-Mar-17.
 */
public class PriceSurvey extends AppCompatActivity {

    ImageView iv_back;
    TextView tv_top_header;
    ListView list_items;
    FloatingActionButton fab;
    FloatingActionButton fabAnalytics;
    private static DatabaseHandler db;

    public static PriceSurveyAdapter adapter;
    public static DialogPriceSurveyAdapter adapterDialogPriceSurvey;
    public static ArrayList arrProductList=new ArrayList();
    public static ArrayList itemName =new ArrayList();
    public static ArrayList itemCode=new ArrayList();
    ArrayList companyName =new ArrayList();


    // data arrays
    public static ArrayList salesRep =new ArrayList();
    public static ArrayList compCode =new ArrayList();
    public static ArrayList itemsCode =new ArrayList();
    public static ArrayList compName =new ArrayList();
    public static ArrayList itemDesc =new ArrayList();
    public static ArrayList dateCaptured =new ArrayList();
    public static ArrayList price =new ArrayList();



    Customer object;
    LoadingSpinner loadingSpinner;
    static ArrayList<ArticleHeader> articles = new ArrayList<>();
    ArrayList<CustomerHeader> customers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_survey);

        Intent i = this.getIntent();
        object = (Customer) i.getParcelableExtra("headerObj");

        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabAnalytics = (FloatingActionButton) findViewById(R.id.fab_analytics);
        db = new DatabaseHandler(this);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText(getString(R.string.price_survey));
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemName.clear();
                itemCode.clear();

                new PostPriceSurvey().execute();
                finish();
                //new PostItemComplaint().execute();

            }
        });

        fabAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(PriceSurvey.this,PriceSurveyAnalytics.class);
                startActivity(i);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SalesInvoiceActivity.tab_position = 98;
                Settings.setString("from", "pricesurvey");
                Intent intent = new Intent(PriceSurvey.this, CategoryListActivity.class);
                startActivity(intent);
            }});


        list_items=(ListView)findViewById(R.id.list_items);
        adapter=new PriceSurveyAdapter(PriceSurvey.this,itemName, itemCode);

        list_items.setAdapter(adapter);

        customers = CustomerHeaders.get();
        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, object.getCustomerID());
        TextView tv_customer_name = (TextView) findViewById(R.id.tv_customer_id);
        TextView tv_customer_address = (TextView) findViewById(R.id.tv_customer_address);
        TextView tv_customer_pobox = (TextView) findViewById(R.id.tv_customer_pobox);
        TextView tv_customer_contact = (TextView) findViewById(R.id.tv_customer_contact);
        if (!(customerHeader == null)) {
            tv_customer_name.setText(StringUtils.stripStart(customerHeader.getCustomerNo(), "0") + " " + UrlBuilder.decodeString(customerHeader.getName1()));
            tv_customer_address.setText(UrlBuilder.decodeString(customerHeader.getStreet()));
            tv_customer_pobox.setText(getString(R.string.pobox) + " " + customerHeader.getPostCode());
            tv_customer_contact.setText(customerHeader.getPhone());
        } else {
            tv_customer_name.setText(StringUtils.stripStart(object.getCustomerID(),"0") + " " + UrlBuilder.decodeString(object.getCustomerName().toString()));
            tv_customer_address.setText(object.getCustomerAddress().toString());
            tv_customer_pobox.setText("");
            tv_customer_contact.setText("");
        }
        articles = ArticleHeaders.get();


        list_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(PriceSurvey.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.listview_price_survey);
                dialog.setCancelable(false);
                TextView tv = (TextView) dialog.findViewById(R.id.dv_title);
                tv.setText(itemName.get(position).toString());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                Button btn_save = (Button) dialog.findViewById(R.id.btn_save);
                ListView lv=(ListView)dialog.findViewById(R.id.list_items);


                DatabaseHandler db = new DatabaseHandler(PriceSurvey.this);
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_ITEM_NAME, "");
                map.put(db.KEY_ITEM_CODE, "");
                map.put(db.KEY_COMPANY_NAME, "");
                map.put(db.KEY_COMPANY_CODE, "");
                map.put(db.KEY_SALES_REP, "");
                HashMap<String, String> filters = new HashMap<>();
                filters.put(db.KEY_ITEM_NAME, itemName.get(position).toString());


                Cursor cursor = db.getData(db.PRICE_SURVEY, map, filters);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do
                    {
                        companyName.add(cursor.getString(cursor.getColumnIndex(db.KEY_COMPANY_NAME)).replaceAll("%20", " "));

                        salesRep.add(cursor.getString(cursor.getColumnIndex(db.KEY_SALES_REP)).replaceAll("%20", " "));
                        compCode.add(cursor.getString(cursor.getColumnIndex(db.KEY_COMPANY_CODE)).replaceAll("%20", " "));
                        itemsCode.add(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_CODE)).replaceAll("%20", " "));
                        compName.add(cursor.getString(cursor.getColumnIndex(db.KEY_COMPANY_NAME)).replaceAll("%20", " "));
                        itemDesc.add(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NAME)).replaceAll("%20", " "));
                        dateCaptured.add(new SimpleDateFormat("yyyyMMdd").format(new Date()));

                    }
                    while (cursor.moveToNext());
                }

                final String itemCases[]= new String[companyName.size()];

                adapterDialogPriceSurvey=new DialogPriceSurveyAdapter(PriceSurvey.this,companyName, itemCases);
                lv.setAdapter(adapterDialogPriceSurvey);

                btn_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseHandler db=new DatabaseHandler(PriceSurvey.this);

                        for(int i=0;i<companyName.size();i++)
                        {
                            if(itemCases[i]==null || itemCases[i].equals(""))
                            {
                                itemCases[i]="0";
                                price.add("0");

                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_ITEM_NAME, itemDesc.get(i).toString());
                                map.put(db.KEY_ITEM_CODE, itemsCode.get(i).toString());
                                map.put(db.KEY_COMPANY_NAME, compName.get(i).toString());
                                map.put(db.KEY_COMPANY_CODE, compCode.get(i).toString());
                                map.put(db.KEY_SALES_REP, salesRep.get(i).toString());
                                map.put(db.KEY_ITEM_PRICE, price.get(i).toString() );
                                map.put(db.KEY_DATE, dateCaptured.get(i).toString());
                                map.put(db.KEY_IS_POSTED, "0");
                                db.addData(db.PRICE_SURVEY_POST,map);

                                System.out.println("insered with price null");
                            }
                            else
                            {
                                price.add(itemCases[i].toString());
                                HashMap<String, String> map = new HashMap<>();
                                map.put(db.KEY_ITEM_NAME, itemDesc.get(i).toString());
                                map.put(db.KEY_ITEM_CODE, itemsCode.get(i).toString());
                                map.put(db.KEY_COMPANY_NAME, compName.get(i).toString());
                                map.put(db.KEY_COMPANY_CODE, compCode.get(i).toString());
                                map.put(db.KEY_SALES_REP, salesRep.get(i).toString());
                                map.put(db.KEY_ITEM_PRICE, price.get(i).toString() );
                                map.put(db.KEY_DATE, dateCaptured.get(i).toString());
                                map.put(db.KEY_IS_POSTED, "0");
                                db.addData(db.PRICE_SURVEY_POST,map);
                                System.out.println("insered with price");
                            }
                        }
                        companyName.clear();


                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }




    private class PostPriceSurvey extends AsyncTask<String, Void, Void> {

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
            postPriceSurvey();
            return null;

        }

        @Override
        protected void onPostExecute(Void args) {
            pDialog.dismiss();
        }
    }

    public void postPriceSurvey()
    {
        try
        {
            DatabaseHandler db = new DatabaseHandler(this);
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_SALES_REP,"");
            map.put(db.KEY_COMPANY_NAME ,"");
            map.put(db.KEY_COMPANY_CODE,"");
            map.put(db.KEY_ITEM_NAME,"");
            map.put(db.KEY_ITEM_CODE,"");
            map.put(db.KEY_ITEM_PRICE,"");
            map.put(db.KEY_DATE,"");
            map.put(db.KEY_IS_POSTED,"");

            HashMap<String, String> filters = new HashMap<>();
            filters.put(db.KEY_IS_POSTED,"0");
            Cursor cursor = db.getData(PRICE_SURVEY_POST,map,filters);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                do
                {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject object = new JSONObject();
                    object.put("sales_Rep",cursor.getString(cursor.getColumnIndex(db.KEY_SALES_REP)));
                    object.put("comp_Code",cursor.getString(cursor.getColumnIndex(db.KEY_COMPANY_CODE)));
                    object.put("item_Code",cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_CODE)));
                    object.put("comp_Name",cursor.getString(cursor.getColumnIndex(db.KEY_COMPANY_NAME)));
                    object.put("item_Desc",cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NAME)));
                    object.put("price",cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_PRICE)));
                    object.put("date_Capt",cursor.getString(cursor.getColumnIndex(db.KEY_DATE)));
                    jsonArray.put(object);
                    System.out.println("Posting data : "+jsonArray);
                    httpRequest(jsonArray);

                }
                while (cursor.moveToNext());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String getValue(String tag, Element element) {
        if(element.getElementsByTagName(tag).item(0)!=null) {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            Node node = nodeList.item(0);
            if (node == null)
                return "null";
            return node.getNodeValue();
        }
        return "";
    }


    private static void httpRequest(JSONArray jsonArray) {

        try
        {
            String entitySet = "priceSurveyCollection";
            String IvCall="P";
            String prefix= "http://" + App.HOST + ":" + App.PORT + App.MERCHANDIZING_URL + entitySet+"/?$filter=";
            String suffix=" IvCall eq '"+IvCall+"' and IvJson eq '"+jsonArray+"'";
            String priceSurveyPostUrl=prefix+ Uri.encode(suffix);
            System.out.println("orig URL : "+prefix+suffix);
            System.out.println("encoded URL : "+priceSurveyPostUrl);
            URL url = new URL(priceSurveyPostUrl);
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
                System.out.println("survey posted....");

                HashMap<String,String>map = new HashMap<>();
                map.put(db.KEY_IS_POSTED,"1");
                HashMap<String,String> filter = new HashMap<>();
                filter.put(db.KEY_IS_POSTED,"0");
                db.updateData(PRICE_SURVEY_POST, map, filter);


                salesRep.clear();
                compCode.clear();
                itemsCode.clear();
                compName.clear();
                itemDesc.clear();
                price.clear();
                dateCaptured.clear();


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