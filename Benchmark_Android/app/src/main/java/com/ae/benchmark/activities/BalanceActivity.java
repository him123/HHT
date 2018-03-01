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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.R;
import com.ae.benchmark.adapters.BalanceAdapter;
import com.ae.benchmark.adapters.BalanceBadgeAdapter;
import com.ae.benchmark.adapters.PriceListAdapter;
import com.ae.benchmark.models.BAlanceList;
import com.ae.benchmark.models.Collection;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.ItemList;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
/************************************************************
 @ This class is called when u click on the balances by expanding
 @ the button on the customer detail screen. It gives the outstanding
 @ balances for the customer
 ************************************************************/
public class BalanceActivity extends AppCompatActivity {


    View view1;
    DatabaseHandler db = new DatabaseHandler(this);
    ListView listView;
    LoadingSpinner loadingSpinner;
    ArrayList<Collection> arrayList = new ArrayList<>();
    BalanceBadgeAdapter adapter;
    Customer object;

    ImageView toolbar_iv_back;
    ImageView iv_search;
    EditText et_search;
    TextView tv_top_header;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        try{
            loadingSpinner = new LoadingSpinner(this);
            Intent i = this.getIntent();
            Helpers.logData(BalanceActivity.this, "On Balance Activity");
            object = (Customer) i.getParcelableExtra("headerObj");
            toolbar_iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
            tv_top_header = (TextView) findViewById(R.id.tv_top_header);
            toolbar_iv_back.setVisibility(View.VISIBLE);
            tv_top_header.setVisibility(View.VISIBLE);
            tv_top_header.setText(getString(R.string.balance));
            toolbar_iv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helpers.logData(BalanceActivity.this, "Back click on Balance Activity Screen");
                    finish();
                }
            });
            listView = (ListView) findViewById(R.id.list_price_list);
            adapter = new BalanceBadgeAdapter(this, arrayList);
            listView.setAdapter(adapter);
            new loadBalance().execute();


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
                    adapter.getFilter().filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.v("addtext", "change");

                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
    public class loadBalance extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try{
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_CUSTOMER_NO, "");
                map.put(db.KEY_INVOICE_NO, "");
                map.put(db.KEY_INVOICE_AMOUNT, "");
                map.put(db.KEY_DUE_DATE, "");
                map.put(db.KEY_INVOICE_DATE, "");
                map.put(db.KEY_AMOUNT_CLEARED, "");
                map.put(db.KEY_IS_INVOICE_COMPLETE, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                Cursor cursor = db.getData(db.COLLECTION, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    setCollectionData(cursor);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            adapter.notifyDataSetChanged();
        }
    }
    private void setCollectionData(Cursor cursor) {
        try{
            Cursor c = cursor;
            do {
                Collection collection = new Collection();
                collection.setInvoiceNo(c.getString(c.getColumnIndex(db.KEY_INVOICE_NO)));
                collection.setInvoiceDate(c.getString(c.getColumnIndex(db.KEY_INVOICE_DATE)));
                collection.setInvoiceAmount(c.getString(c.getColumnIndex(db.KEY_INVOICE_AMOUNT)));
                collection.setAmountCleared(c.getString(c.getColumnIndex(db.KEY_AMOUNT_CLEARED)));
                collection.setInvoiceDueDate(c.getString(c.getColumnIndex(db.KEY_DUE_DATE)));
                arrayList.add(collection);
            }
            while (c.moveToNext());
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
}
