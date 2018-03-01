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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

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
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.ItemComplaintAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;

import static com.ae.benchmark.utils.DatabaseHandler.ITEM_COMPLAINTS;

/**
 * Created by Rakshit on 26-Mar-17.
 */
public class ItemComplaints extends AppCompatActivity {
    ImageView iv_back, iv_refresh;
    TextView tv_top_header;
    ListView list_items;
    public static ItemComplaintAdapter adapter;
    FloatingActionButton fab;
    ArrayList<Integer> proceedArrayList;
    Customer object;
    private static DatabaseHandler db;
    LoadingSpinner loadingSpinner;
    SwipeRefreshLayout refreshLayout;
    public static ArrayList<Sales> arrProductList;
    static ArrayList<ArticleHeader> articles = new ArrayList<>();
    private String imagePath = null;
    private String image = null;
    Button btn_imageFW;
    Button btn_imageRW;
    Button btn_imageLW;
    Button btn_imageBW;
    ArrayList<CustomerHeader> customers;
    public String graphicData="";

    public static String postingCustomerNo="";
    public static String postingMaterialNo="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_complaints);
        Intent i = this.getIntent();
        object = (Customer) i.getParcelableExtra("headerObj");
        arrProductList = new ArrayList<>();
        loadingSpinner = new LoadingSpinner(this);
        //refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        db = new DatabaseHandler(this);

        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText(getString(R.string.item_complaint));
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                new PostItemComplaint().execute();
                showCustomerSignCapture();
            }
        });
        customers = CustomerHeaders.get();
        if(object != null) {
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
                tv_customer_name.setText(StringUtils.stripStart(object.getCustomerID(), "0") + " " + UrlBuilder.decodeString(object.getCustomerName().toString()));
                tv_customer_address.setText(object.getCustomerAddress().toString());
                tv_customer_pobox.setText("");
                tv_customer_contact.setText("");
            }
        }
        list_items = (ListView)findViewById(R.id.list_items);
        adapter = new ItemComplaintAdapter(this, arrProductList);
        list_items.setAdapter(adapter);
        articles = ArticleHeaders.get();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SalesInvoiceActivity.tab_position = 99;
                Settings.setString("from", "itemcomplaint");
                Intent intent = new Intent(ItemComplaints.this, CategoryListActivity.class);
                startActivity(intent);
            }
        });

        System.out.println("Product List size initial : "+arrProductList.size());

        list_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Sales sales = arrProductList.get(position);
                final Dialog dialog = new Dialog(ItemComplaints.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_with_crossbutton_itemcomplaint);
                dialog.setCancelable(false);
                TextView tv = (TextView) dialog.findViewById(R.id.dv_title);
                final EditText etComment = (EditText) dialog.findViewById(R.id.description);
                tv.setText(sales.getName());
                postingMaterialNo=sales.getMaterial_no();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                ImageView iv_cancel = (ImageView) dialog.findViewById(R.id.imageView_close);
                Button btn_save = (Button) dialog.findViewById(R.id.btn_save);
                btn_imageFW = (Button)dialog.findViewById(R.id.capture1);
                btn_imageFW.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imagePath = Helpers.takePhoto(ItemComplaints.this,"1");
                    }
                });
                btn_imageLW = (Button)dialog.findViewById(R.id.capture2);
                btn_imageLW.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        imagePath = Helpers.takePhoto(ItemComplaints.this,"2");
                    }
                });
                btn_imageRW = (Button)dialog.findViewById(R.id.capture3);
                btn_imageRW.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        imagePath = Helpers.takePhoto(ItemComplaints.this,"3");
                    }
                });
                btn_imageBW = (Button)dialog.findViewById(R.id.capture4);
                btn_imageBW.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        imagePath = Helpers.takePhoto(ItemComplaints.this,"4");
                    }
                });
                iv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                btn_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();

                        String comment=etComment.getText().toString();
                        String customerNo=SelectCustomerActivity.distributionCustomerNo;
                        String graphicName=sales.getName();
                        if(comment.equals("") || comment==null)
                        {
                            comment="N-A";
                        }
                        HashMap<String, String> headerParams = new HashMap<>();
                        headerParams.put(db.KEY_CUSTOMER_NO  , customerNo);
                        headerParams.put(db.KEY_GRAPHIC_NAME ,graphicName);
                        headerParams.put(db.KEY_GRAPHIC_DATA ,graphicData);

                        System.out.println("insertion base64 size : "+graphicData.length());
                      //  headerParams.put(db.KEY_GRAPHIC_DATA, "sample pic");
                        headerParams.put(db.KEY_COMMENT ,comment);
                        headerParams.put(db.KEY_IS_POSTED ,"0");
                        db.addData(db.ITEM_COMPLAINTS, headerParams);

                        System.out.println("Data : "+headerParams);


                        System.out.println("added");

                    }
                });
                dialog.show();
            }
        });
    }

    private void showCustomerSignCapture(){
        final Dialog dialog = new Dialog(ItemComplaints.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_customer_sign);
        dialog.setCancelable(false);
        dialog.show();
        Button btn_save = (Button)dialog.findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
    }
    public static ArrayList<Sales> setProductList(){
        try{

            System.out.println("Product List size : "+arrProductList.size());
            if(arrProductList.size()>0){
                for(int i=0;i< Const.addlist.size();i++){
                    HashMap<String,String> searchMap = new HashMap<>();
                    searchMap.put(db.KEY_MATERIAL_NO,"");
                    searchMap.put(db.KEY_BASE_UOM,"");
                    searchMap.put(db.KEY_MATERIAL_DESC1,"");
                    HashMap<String,String>filterSearch = new HashMap<>();
                    filterSearch.put(db.KEY_MATERIAL_DESC1, UrlBuilder.clean(Const.addlist.get(i)));

                    Cursor articleCursor = db.getData(db.ARTICLE_HEADER,searchMap,filterSearch);
                    articleCursor.moveToFirst();

                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,UrlBuilder.encodeString(Const.addlist.get(i)));
                    Sales sale = new Sales();
                    sale.setMaterial_no(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setItem_code(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setUom(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_BASE_UOM)));
                    sale.setName(Const.addlist.get(i));
                    sale.setCases("0");
                    sale.setPic("0");

                    HashMap<String,String> filterPart = new HashMap<>();
                    filterPart.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());

                    HashMap<String,String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_AMOUNT, "");
                    if(db.checkData(db.PRICING,filterPart)){
                        //Pricing exists for Product for customer
                        //Pricing exists for Product for customer
                        Cursor priceCursor = db.getData(db.PRICING,map,filterPart);
                        if(priceCursor.getCount()>0){
                            priceCursor.moveToFirst();
                            String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                            sale.setPrice(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM)?price:price);
                        }
                    }
                    else{
                        sale.setPrice("0");
                    }

                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_MATERIAL_NO, articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter);
                    if (altUOMCursor.getCount() > 0) {
                        altUOMCursor.moveToFirst();
                        if (articleCursor.getString(articleCursor.getColumnIndex(db.KEY_BASE_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))
                                ||articleCursor.getString(articleCursor.getColumnIndex(db.KEY_BASE_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                            sale.setIsAltUOM(false);
                        } else {
                            sale.setIsAltUOM(true);
                        }
                    } else {
                        sale.setIsAltUOM(false);
                    }


                    arrProductList.add(sale);
                }
            }
            else{
                arrProductList.clear();
                for(int i=0;i<Const.addlist.size();i++){
                    HashMap<String,String>searchMap = new HashMap<>();
                    searchMap.put(db.KEY_MATERIAL_NO,"");
                    searchMap.put(db.KEY_BASE_UOM,"");
                    searchMap.put(db.KEY_MATERIAL_DESC1,"");
                    HashMap<String,String>filterSearch = new HashMap<>();
                    filterSearch.put(db.KEY_MATERIAL_DESC1, UrlBuilder.clean(Const.addlist.get(i)));

                    Cursor articleCursor = db.getData(db.ARTICLE_HEADER,searchMap,filterSearch);
                    articleCursor.moveToFirst();

                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,UrlBuilder.encodeString(Const.addlist.get(i)));
                    Sales sale = new Sales();
                    sale.setMaterial_no(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setItem_code(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setUom(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_BASE_UOM)));
                    sale.setName(Const.addlist.get(i));
                    sale.setCases("0");
                    sale.setPic("0");

                    HashMap<String,String> filterPart = new HashMap<>();
                    filterPart.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());

                    HashMap<String,String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_AMOUNT, "");
                    if(db.checkData(db.PRICING,filterPart)){
                        //Pricing exists for Product for customer
                        //Pricing exists for Product for customer
                        Cursor priceCursor = db.getData(db.PRICING,map,filterPart);
                        if(priceCursor.getCount()>0){
                            priceCursor.moveToFirst();
                            String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                            sale.setPrice(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM) ? price : price);
                        }
                    }
                    else{
                        sale.setPrice("0");
                    }
                    arrProductList.add(sale);
                }
            }

            Log.e("FInal", "" + arrProductList.size());
            adapter.notifyDataSetChanged();
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return null;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Helpers.TYPE_CAMERA_B1 && resultCode == RESULT_OK) {
            try {
                image = Helpers.imageToBase64(this, imagePath, Helpers.QUALITY_HIGH, true);

                graphicData=image;
                btn_imageFW.setBackgroundResource(R.drawable.icon_camera_prim_32);
                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == Helpers.TYPE_CAMERA_B2 && resultCode == RESULT_OK) {
            try {
                image = Helpers.imageToBase64(this, imagePath, Helpers.QUALITY_HIGH, true);
                graphicData=image;
                btn_imageLW.setBackgroundResource(R.drawable.icon_camera_prim_32);
                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == Helpers.TYPE_CAMERA_B3 && resultCode == RESULT_OK) {
            try {
                image = Helpers.imageToBase64(this, imagePath, Helpers.QUALITY_HIGH, true);
                graphicData=image;
                btn_imageRW.setBackgroundResource(R.drawable.icon_camera_prim_32);
                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == Helpers.TYPE_CAMERA_B4 && resultCode == RESULT_OK) {
            try {
                image = Helpers.imageToBase64(this, imagePath, Helpers.QUALITY_HIGH, true);
                graphicData=image;
                btn_imageBW.setBackgroundResource(R.drawable.icon_camera_prim_32);
                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class PostItemComplaint extends AsyncTask<String, Void, Void> {

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
            postItemComplaints();
            return null;

        }

        @Override
        protected void onPostExecute(Void args) {
            pDialog.dismiss();
        }
    }

    public void postItemComplaints() {

        try {
            DatabaseHandler db = new DatabaseHandler(this);
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_ID,"");
            map.put(db.KEY_CUSTOMER_NO ,"");
            map.put(db.KEY_GRAPHIC_NAME,"");
            map.put(db.KEY_GRAPHIC_DATA,"");
            map.put(db.KEY_COMMENT,"");
            map.put(db.KEY_IS_POSTED,"");


            HashMap<String, String> filters = new HashMap<>();
            filters.put(db.KEY_IS_POSTED,"0");
            Cursor cursor = db.getData(db.ITEM_COMPLAINTS,map,filters);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                do
                {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject object = new JSONObject();
                    object.put("kunnr",cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO)));
                    object.put("graphic_Name", cursor.getString(cursor.getColumnIndex(db.KEY_GRAPHIC_NAME)));
                    object.put("graphic_B64", cursor.getString(cursor.getColumnIndex(db.KEY_GRAPHIC_DATA)));
//                    object.put("graphic_B64", graphicData);
                    object.put("comments", cursor.getString(cursor.getColumnIndex(db.KEY_COMMENT)));



                    System.out.println("Retrieved base64 size : "+cursor.getString(cursor.getColumnIndex(db.KEY_GRAPHIC_DATA)).length());

                    jsonArray.put(object);
                    System.out.println("Posting data : "+jsonArray);
                    postingCustomerNo=""+cursor.getString(cursor.getColumnIndex(db.KEY_CUSTOMER_NO));
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
            String entitySet = "itemComplaintUploadCollection";
            String ivCustomerNo=postingCustomerNo;
            String ivMaterialNo=postingMaterialNo;
            String prefix= "http://" + App.HOST + ":" + App.PORT + App.MERCHANDIZING_URL + entitySet+"/?$filter=";
            String suffix=" IvKunnr eq '"+ivCustomerNo+"' and IvMatnr eq '"+ivMaterialNo+"' and IvJson eq '"+jsonArray+"'";
            String itemComplaintPostUrl=prefix+Uri.encode(suffix);
            System.out.println("orig URL : "+prefix+suffix);
            System.out.println("encoded URL : "+itemComplaintPostUrl);
            URL url = new URL(itemComplaintPostUrl);
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
            //System.out.println("d:EMessage :"+statusMessage);

            if(statusMessage.equals("null"))
            {

            }
            else
            {
                System.out.println("RFC Message : "+statusMessage);
                HashMap<String,String>map = new HashMap<>();
                map.put(db.KEY_IS_POSTED,"1");
                HashMap<String,String> filter = new HashMap<>();
                filter.put(db.KEY_IS_POSTED,"0");
                db.updateData(ITEM_COMPLAINTS, map, filter);
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
