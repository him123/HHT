package com.ae.benchmark.activities;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.R;
import com.ae.benchmark.adapters.PriceListAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.ItemList;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.UrlBuilder;
public class PriceListCustomerActivity extends AppCompatActivity {

    View view1;
    ListView listView;
    ArrayList<ItemList> arrayList = new ArrayList<>();
    PriceListAdapter adapter;
    Customer object;
    public ArrayList<ArticleHeader> articles;
    DatabaseHandler db = new DatabaseHandler(this);
    ImageView toolbar_iv_back;
    ImageView iv_search;
    EditText et_search;
    TextView tv_top_header;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_customer_list);
        articles = ArticleHeaders.get();
        Intent i = this.getIntent();
        object = (Customer) i.getParcelableExtra("headerObj");
        toolbar_iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        toolbar_iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText(getString(R.string.price_items));
        toolbar_iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        new loadMaterials().execute();


        iv_search = (ImageView) findViewById(R.id.iv_search);
        if (iv_search != null) {
            iv_search.setVisibility(View.VISIBLE);
        }

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_search.setVisibility(View.GONE);
                et_search.setVisibility(View.VISIBLE);
                toolbar_iv_back.setVisibility(View.GONE);
                tv_top_header.setVisibility(View.GONE);
            }
        });

        et_search = (EditText) findViewById(R.id.et_search_customer);
        et_search.setHint("Search");
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
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.v("addtext", "change");
                adapter.getFilter().filter(s.toString());
            }
        });


        listView = (ListView) findViewById(R.id.list_price_list);
        adapter = new PriceListAdapter(PriceListCustomerActivity.this, arrayList);
        listView.setAdapter(adapter);

    }
    public class loadMaterials extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String,String>map = new HashMap<>();
            map.put(db.KEY_MATERIAL_NO,"");
            map.put(db.KEY_AMOUNT,"");
            HashMap<String,String>filter= new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
            Cursor cursor = db.getData(db.PRICING,map,filter);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                setMaterials(cursor);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
        }
    }
    private void setMaterials(Cursor cursor){
        Cursor c = cursor;
        c.moveToFirst();
        do{
            ItemList model = new ItemList();
            ArticleHeader articleHeader = ArticleHeader.getArticle(articles,c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
            model.setItem_number(StringUtils.stripStart(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)), "0"));
            if(articleHeader!=null){
                model.setItem_des(UrlBuilder.decodeString(articleHeader.getMaterialDesc1()));
            }
            else{
                model.setItem_des(StringUtils.stripStart(c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)), "0"));
            }
            model.setCase_price(c.getString(c.getColumnIndex(db.KEY_AMOUNT)));
            arrayList.add(model);
        }
        while (c.moveToNext());
    }
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
}
