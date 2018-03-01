package com.ae.benchmark.activities;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.Fragment.AllCustomerFragment;
import com.ae.benchmark.Fragment.VisitAllFragment;
import com.ae.benchmark.models.Sales;
import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.OrderRequestBadgeAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.DriverRouteFlags;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.OrderList;
import com.ae.benchmark.models.OrderRequest;
import com.ae.benchmark.models.PreSaleProceed;
import com.ae.benchmark.sap.DataListener;
import com.ae.benchmark.sap.IntegrationService;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.PrinterHelper;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
public class PreSaleOrderProceedActivity extends AppCompatActivity implements DataListener{
    ArrayList<PreSaleProceed> preSaleProceeds = new ArrayList<>();
    ImageView iv_back;
    TextView tv_top_header;
    TextView tv_date;
    ImageView iv_calendar, iv_search;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    Button btn_confirm;
    EditText et_search;
    ListView list;
    OrderRequestBadgeAdapter adapter;
    Customer object;
    LoadingSpinner loadingSpinner;
    public static String from = "";
    LinearLayout ll_bottom;
    FloatingActionButton fb_print;
    FloatingActionButton fb_edit;
    List<LoadRequestConstants> preSaleOrderArraylist;
    int position;
    HashMap<Integer, List<LoadRequestConstants>> constantsHashMap = new HashMap<>();
    DatabaseHandler db = new DatabaseHandler(this);
    ArrayList<OrderRequest> arraylist = new ArrayList<>();
    OrderList orderList;
    float orderTotalValue = 0;
    float totalamnt = 0;
    float discount = 0;
    int count=0;
    String customerPO;
    public ArrayList<ArticleHeader> articles;
    boolean isPrint = false;
    App.DriverRouteControl flag = new App.DriverRouteControl();
    boolean isConfirm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_sale_order_proceed);
        View v = (View) findViewById(R.id.inc_common_header);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        myCalendar = Calendar.getInstance();
        flag = DriverRouteFlags.get();
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText(getString(R.string.presalesorder));
        articles = new ArrayList<>();
        articles = ArticleHeaders.get();
//        v.setVisibility(View.INVISIBLE);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_date.setText(Helpers.formatDate(new Date(), App.DATE_PICKER_FORMAT));
        iv_calendar = (ImageView) findViewById(R.id.iv_calander);
        iv_search = (ImageView) findViewById(R.id.iv_search);
        iv_search.setVisibility(View.VISIBLE);
        et_search = (EditText) findViewById(R.id.et_search_customer);
        et_search.setHint("Select Product..");
        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_search.setVisibility(View.GONE);
                et_search.setVisibility(View.VISIBLE);
                iv_back.setVisibility(View.GONE);
                tv_top_header.setVisibility(View.GONE);
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
                        iv_back.setVisibility(View.VISIBLE);
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

        iv_back.setVisibility(View.VISIBLE);
        btn_confirm = (Button) findViewById(R.id.btn_confirm_delivery_presale_proceed);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        fb_print = (FloatingActionButton) findViewById(R.id.fab_print);
        fb_edit = (FloatingActionButton) findViewById(R.id.fab_edit);
        fb_edit.setVisibility(View.GONE);
        loadingSpinner = new LoadingSpinner(this);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConfirm) {
                    isConfirm = false;
                    list.setAdapter(null);
                    adapter = new OrderRequestBadgeAdapter(PreSaleOrderProceedActivity.this, arraylist, from, "yes");
                    list.setAdapter(adapter);
                    btn_confirm.setText("CONFIRM ITEM LIST");
                }else {
                    finish();
                }
            }
        });
        Intent i = this.getIntent();
        object = (Customer) i.getParcelableExtra("headerObj");
        orderList = (OrderList) i.getParcelableExtra("orderList");
        customerPO = i.getStringExtra("customerpo");
        Log.e("I have PO","" + customerPO);
        if (getIntent().getExtras() != null) {
            from = getIntent().getStringExtra("from");
        }
        Helpers.logData(PreSaleOrderProceedActivity.this,"Came to order request screen from" + from);
        list = (ListView) findViewById(R.id.listview);
        list.setItemsCanFocus(true);
        if (from.equals("list")) {
            adapter = new OrderRequestBadgeAdapter(this, arraylist, from, "no");
        } else {
            adapter = new OrderRequestBadgeAdapter(this, arraylist, from, "yes");
        }

        setTitle(getString(R.string.presalesorder));
        //  list.setItemsCanFocus(true);
        if (from.equalsIgnoreCase("button")) {
            if(flag != null){
            if( flag.getDefaultDeliveryDays() != null && !flag.getDefaultDeliveryDays().equals("")&&!flag.getDefaultDeliveryDays().equals("0")){
                Helpers.logData(PreSaleOrderProceedActivity.this,"Default delivery days from flag is" + flag.getDefaultDeliveryDays());
                iv_calendar.setEnabled(false);
                String days = flag.getDefaultDeliveryDays();
                myCalendar.setTime(new Date());
                myCalendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(days));
                updateLabel();
                new loadItems().execute();
            }
            else{
                new loadItems().execute();
            }
            }
            else{
                new loadItems().execute();
            }

            //list.setEnabled(true);
        } else if (from.equalsIgnoreCase("list")) {
            Helpers.logData(PreSaleOrderProceedActivity.this,"Loading items for order" + orderList.getOrderId());
            new loadItemsOrder(orderList.getOrderId());
            iv_calendar.setEnabled(false);
            //list.setEnabled(false);
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                try{
                    if(!isConfirm) {
                        TextView itemtv = (TextView) view.findViewById(R.id.tv_item);
                        int pos = position;
                        for (OrderRequest data : arraylist) {

                            if (itemtv.getText().toString().contains(data.getItemCode())) {
                                pos = arraylist.indexOf(data);
                            }
                        }

                        final OrderRequest item = arraylist.get(pos);
                        Helpers.logData(PreSaleOrderProceedActivity.this, "Adding order for item" + item.getMaterialNo());
                        final Dialog dialog = new Dialog(PreSaleOrderProceedActivity.this);
                        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_with_crossbutton_editprice);
                        dialog.setCancelable(false);
                        TextView tv = (TextView) dialog.findViewById(R.id.dv_title);
                        final TextView tv_Vat = (TextView) dialog.findViewById(R.id.tv_Total);
                        tv.setText(item.getItemName());
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                        ImageView iv_cancle = (ImageView) dialog.findViewById(R.id.imageView_close);
                        Button btn_save = (Button) dialog.findViewById(R.id.btn_save);
                        final EditText ed_cases = (EditText) dialog.findViewById(R.id.ed_cases);
                        final EditText ed_pcs = (EditText) dialog.findViewById(R.id.ed_pcs);
                        final EditText ed_cases_inv = (EditText) dialog.findViewById(R.id.ed_cases_inv);
                        final EditText ed_pcs_inv = (EditText) dialog.findViewById(R.id.ed_pcs_inv);
                        final EditText ed_cases_price = (EditText) dialog.findViewById(R.id.ed_cases_price);
                        final EditText ed_pcs_price = (EditText) dialog.findViewById(R.id.ed_pcs_price);
                        LinearLayout ll1 = (LinearLayout) dialog.findViewById(R.id.ll_1);
                        ll1.setVisibility(View.GONE);
                        RelativeLayout rl_specify = (RelativeLayout) dialog.findViewById(R.id.rl_specify_reason);
                        rl_specify.setVisibility(View.GONE);
                        /*if (item.isAltUOM()) {
                            ed_pcs.setEnabled(true);
                        } else {
                            ed_pcs.setEnabled(false);
                        }*/

                        ed_cases.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                Double Vat = calculateVatTotal(Double.parseDouble(ed_cases.getText().toString().equals("")?"0":ed_cases.getText().toString()),Double.parseDouble(ed_cases_price.getText().toString().equals("")?"0":ed_cases_price.getText().toString()),Double.parseDouble(ed_pcs.getText().toString().equals("")?"0":ed_pcs.getText().toString()),Double.parseDouble(ed_pcs_price.getText().toString().equals("")?"0":ed_pcs_price.getText().toString()));
                                tv_Vat.setText("Total = "+String.format("%.2f",Vat));
                            }
                        });
                        ed_cases_price.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                if(ed_cases_price.hasFocus()) {

                                    Double Price = calculateItemPriceFromCases(Double.parseDouble(ed_cases_price.getText().toString().equals("") ? "0" : ed_cases_price.getText().toString()), Double.parseDouble(item.getDenominator()));
                                    ed_pcs_price.setText(String.format("%.2f", Price));
                                    Double Vat = calculateVatTotal(Double.parseDouble(ed_cases.getText().toString().equals("") ? "0" : ed_cases.getText().toString()), Double.parseDouble(ed_cases_price.getText().toString().equals("") ? "0" : ed_cases_price.getText().toString()), Double.parseDouble(ed_pcs.getText().toString().equals("") ? "0" : ed_pcs.getText().toString()), Double.parseDouble(ed_pcs_price.getText().toString().equals("") ? "0" : ed_pcs_price.getText().toString()));
                                    tv_Vat.setText("Total = " + String.format("%.2f", Vat));

                                }
                            }
                        });
                        ed_pcs.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                Double Vat = calculateVatTotal(Double.parseDouble(ed_cases.getText().toString().equals("")?"0":ed_cases.getText().toString()),Double.parseDouble(ed_cases_price.getText().toString().equals("")?"0":ed_cases_price.getText().toString()),Double.parseDouble(ed_pcs.getText().toString().equals("")?"0":ed_pcs.getText().toString()),Double.parseDouble(ed_pcs_price.getText().toString().equals("")?"0":ed_pcs_price.getText().toString()));
                                tv_Vat.setText("Total = "+String.format("%.2f",Vat));
                            }
                        });
                        ed_pcs_price.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                if(ed_pcs_price.hasFocus()) {


                                    Double Price = calculateItemPriceFromPcs(Double.parseDouble(ed_pcs_price.getText().toString().equals("") ? "0" : ed_pcs_price.getText().toString()), Double.parseDouble(item.getDenominator()));
                                    ed_cases_price.setText(String.format("%.2f", Price));
                                    Double Vat = calculateVatTotal(Double.parseDouble(ed_cases.getText().toString().equals("") ? "0" : ed_cases.getText().toString()), Double.parseDouble(ed_cases_price.getText().toString().equals("") ? "0" : ed_cases_price.getText().toString()), Double.parseDouble(ed_pcs.getText().toString().equals("") ? "0" : ed_pcs.getText().toString()), Double.parseDouble(ed_pcs_price.getText().toString().equals("") ? "0" : ed_pcs_price.getText().toString()));
                                    tv_Vat.setText("Total = " + String.format("%.2f", Vat));
                                }
                            }
                        });

                        ed_cases_price.setText(item.getPricecase());
                        ed_pcs_price.setText(item.getPricepcs());

                        ed_cases.setText(item.getCases().trim().equals("0")?"":item.getCases().trim());
                        ed_pcs.setText(item.getUnits().trim().equals("0")?"":item.getUnits().trim());


                        LinearLayout ll_1 = (LinearLayout) dialog.findViewById(R.id.ll_1);
                        iv_cancle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        if (!from.equalsIgnoreCase("list")) {
                            dialog.show();
                        }

                        final int finalPos = pos;
                        btn_save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String strCase = ed_cases.getText().toString();
                                String strpcs = ed_pcs.getText().toString();
                                String strCasePrice = ed_cases_price.getText().toString();
                                String strpcsPrice = ed_pcs_price.getText().toString();
                                String strcaseinv = ed_cases_inv.getText().toString();
                                String strpcsinv = ed_pcs_inv.getText().toString();
                        /*TextView tv_cases = (TextView) view.findViewById(R.id.tv_cases_value);
                        TextView tv_pcs = (TextView) view.findViewById(R.id.tv_pcs_value);
                        tv_cases.setText(strCase);
                        tv_pcs.setText(strpcs);*/
                                if (strCase.isEmpty() || strCase == null || strCase.trim().equals("")) {
                                    strCase = String.valueOf(0);
                                }
                                if (strpcs.isEmpty() || strpcs == null || strpcs.trim().equals("")) {
                                    strpcs = String.valueOf(0);
                                }
                                if (strcaseinv.isEmpty() || strcaseinv == null || strcaseinv.trim().equals("")) {
                                    strcaseinv = String.valueOf(0);
                                }
                                if (strpcsinv.isEmpty() || strpcsinv == null || strpcsinv.trim().equals("")) {
                                    strpcsinv = String.valueOf(0);
                                }

                                if (strCasePrice.equals("")) {
                                    strCasePrice = "0";
                                }
                                if (strpcsPrice.equals("")) {
                                    strpcsPrice = "0";
                                }

                                //item.setCases(strCase);
                                //item.setUnits(strpcs);

                                if((Double.parseDouble(item.getPricemin()) > Double.parseDouble(strpcsPrice))){
                                    Toast.makeText(PreSaleOrderProceedActivity.this,"You can not sale item Min Price: "+item.getPricemin(),Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if((Double.parseDouble(item.getPricemax()) < Double.parseDouble(strpcsPrice))){
                                    Toast.makeText(PreSaleOrderProceedActivity.this,"You can not sale item Max Price: "+item.getPricemax(),Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                item.setPricecase(strCasePrice);
                                item.setPricepcs(strpcsPrice);
                                item.setCases(strCase.trim().equals("") ? "0" : strCase);
                                item.setUnits(strpcs.trim().equals("") ? "0" : strpcs);
                                Helpers.logData(PreSaleOrderProceedActivity.this, "Order Details for item" + item.getMaterialNo() + "-" + item.getCases() + "/" + item.getUnits());

                                arraylist.remove(finalPos);
                                arraylist.add(finalPos, item);
                                hideSoftKeyboard();
                                dialog.dismiss();
                            }
                        });
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Helpers.logData(PreSaleOrderProceedActivity.this, "Exception caught");
                }

            }
        });

        iv_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!isConfirm) {
                DatePickerDialog d=  new DatePickerDialog(PreSaleOrderProceedActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                d.show();
//                }
            }
        });
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        // Locate the ListView in listview_main.xml
        if (from.equals("button")) {
            ll_bottom.setVisibility(View.GONE);
            btn_confirm.setVisibility(View.VISIBLE);
            // Pass results to ListViewAdapter Class
        } else if (from.equals("list")) {
            ll_bottom.setVisibility(View.VISIBLE);
            btn_confirm.setVisibility(View.GONE);
        }

        btn_confirm.setText("CONFIRM ITEM LIST");

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(PreSaleOrderProceedActivity.this,"Order request confirm clicked");
                try{
                    if(isConfirm) {
                        new setConfirmItems().execute();
                    }else{
                        new setConfirmList().execute();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }
        });
        fb_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(PreSaleOrderProceedActivity.this,"Printing button clicked");
                final Dialog dialog = new Dialog(PreSaleOrderProceedActivity.this);
                dialog.setContentView(R.layout.dialog_doprint);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
                LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);
                dialog.show();
                btn_print.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createPrintout(true,orderList.getOrderDate(),orderList.getOrderId(),false);
                        //finish();
                    }
                });
                btn_notprint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        });
        fb_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter = new OrderRequestBadgeAdapter(PreSaleOrderProceedActivity.this, arraylist, from, "yes");
                list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private Double calculateItemPriceFromCases(Double CaseA,Double Deno){
        Double Price = CaseA / Deno;
        return Price;
    }
    private Double calculateItemPriceFromPcs(Double PcsA,Double Deno){
        Double Price = PcsA * Deno;
        return Price;
    }
    private Double calculateVatTotal(Double CaseA,Double CaseQ,Double PcsA,Double PcsQ){
        Double Total_Case = CaseA * CaseQ;
        Double Total_Pcs = PcsA * PcsQ;
        return (Total_Case + Total_Pcs)+(Total_Case + Total_Pcs)*Double.parseDouble(Settings.getString(App.VATValue));
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) PreSaleOrderProceedActivity.this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                PreSaleOrderProceedActivity.this.getCurrentFocus().getWindowToken(), 0);
    }
    private void updateLabel() {
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
        tv_date.setText(sdf.format(myCalendar.getTime()));
        PreSaleProceed proceed = new PreSaleProceed();
        proceed.setDATE(tv_date.getText().toString());
        Helpers.logData(PreSaleOrderProceedActivity.this, "Order date set to" + tv_date.getText().toString());
        //Log.e("Date", "" + tv_date.getText().toString());
        // Const.proceedArrayList.add(Const.id, proceed);
    }
   /* @Override
    public void onBackPressed() {
        finish();
    }*/
    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
    @Override
    public void onProcessingComplete() {
        new postData().execute();
    }
    @Override
    public void onProcessingComplete(String source) {
    }
    public class loadItems extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_TRIP_ID, "");
            map.put(db.KEY_MATERIAL_GROUPA_DESC, "");
            map.put(db.KEY_MATERIAL_GROUPB_DESC, "");
            map.put(db.KEY_MATERIAL_DESC2, "");
            map.put(db.KEY_BATCH_MANAGEMENT, "");
            map.put(db.KEY_PRODUCT_HIERARCHY, "");
            map.put(db.KEY_VOLUME_UOM, "");
            map.put(db.KEY_VOLUME, "");
            map.put(db.KEY_WEIGHT_UOM, "");
            map.put(db.KEY_NET_WEIGHT, "");
            map.put(db.KEY_GROSS_WEIGHT, "");
            map.put(db.KEY_ARTICLE_CATEGORY, "");
            map.put(db.KEY_ARTICLE_NO, "");
            map.put(db.KEY_BASE_UOM, "");
            map.put(db.KEY_MATERIAL_GROUP, "");
            map.put(db.KEY_MATERIAL_TYPE, "");
            map.put(db.KEY_MATERIAL_DESC1, "");
            map.put(db.KEY_MATERIAL_NO, "");
            HashMap<String, String> filter = new HashMap<>();
            Cursor cursor = db.getData(db.ARTICLE_HEADER, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                setLoadItems(cursor, false);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    public class setConfirmList extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;

        ArrayList<OrderRequest> tempArraylist = new ArrayList<>();
        @Override
        protected void onPreExecute() {

            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            String purchaseNum = Helpers.generateNumber(db, ConfigStore.OrderRequest_PR_Type);
            for (OrderRequest loadRequest : arraylist) {
                try {
                    if (loadRequest.getCases().equals("") || loadRequest.getCases().isEmpty() || loadRequest.getCases() == null) {
                        loadRequest.setCases("0");
                    }
                    if (loadRequest.getUnits().equals("") || loadRequest.getUnits().isEmpty() || loadRequest.getUnits() == null) {
                        loadRequest.setUnits("0");
                    }
                    orderTotalValue = orderTotalValue + Float.parseFloat(loadRequest.getPrice());
                    if (Float.parseFloat(loadRequest.getCases()) > 0 || Float.parseFloat(loadRequest.getUnits()) > 0) {
                        tempArraylist.add(loadRequest);
                    }

                    Helpers.logData(PreSaleOrderProceedActivity.this,"Order Total" + orderTotalValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            if(tempArraylist.size() > 0) {
                btn_confirm.setText("CONFIRM ORDER");
                isConfirm = true;
                list.setAdapter(null);
                adapter = new OrderRequestBadgeAdapter(PreSaleOrderProceedActivity.this, tempArraylist, from, "yes");
                list.setAdapter(adapter);
            }else{
                Toast.makeText(PreSaleOrderProceedActivity.this,"Select at-least one item",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class setConfirmItems extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;
        String strdate = "";
        @Override
        protected void onPreExecute() {
            strdate = tv_date.getText().toString();
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            String purchaseNum = Helpers.generateNumber(db, ConfigStore.OrderRequest_PR_Type);
            for (OrderRequest loadRequest : arraylist) {
                try {
                    if (loadRequest.getCases().equals("") || loadRequest.getCases().isEmpty() || loadRequest.getCases() == null) {
                        loadRequest.setCases("0");
                    }
                    if (loadRequest.getUnits().equals("") || loadRequest.getUnits().isEmpty() || loadRequest.getUnits() == null) {
                        loadRequest.setUnits("0");
                    }
                    orderTotalValue = orderTotalValue + Float.parseFloat(loadRequest.getPrice());
                    if (Float.parseFloat(loadRequest.getCases()) > 0 || Float.parseFloat(loadRequest.getUnits()) > 0) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                        map.put(db.KEY_DATE, strdate);
                        map.put(db.KEY_ITEM_NO, loadRequest.getItemCode());
                        map.put(db.KEY_MATERIAL_DESC1, loadRequest.getItemName());
                        map.put(db.KEY_MATERIAL_NO, loadRequest.getMaterialNo());
                        map.put(db.KEY_MATERIAL_GROUP, loadRequest.getItemCategory());
                        map.put(db.KEY_CASE, loadRequest.getCases());
                        map.put(db.KEY_UNIT, loadRequest.getUnits());
                        map.put(db.KEY_UOM, loadRequest.getUom());
                        map.put(db.KEY_PRICE, loadRequest.getPrice());
                        map.put(db.KEY_PRICECASE, loadRequest.getPricecase());
                        map.put(db.KEY_PRICEPCS, loadRequest.getPricepcs());
                        map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        map.put(db.KEY_IS_PRINTED, "");
                        map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        map.put(db.KEY_ORDER_ID, purchaseNum);
                        map.put(db.KEY_PURCHASE_NUMBER, purchaseNum);
                        map.put(db.KEY_CUSTOMER_PO, customerPO);
                        Helpers.logData(PreSaleOrderProceedActivity.this, "Order Request Data" + loadRequest.getMaterialNo() + "-" + loadRequest.getItemName() + "-"
                                + loadRequest.getCases() + "-" + loadRequest.getUnits() + "-" + loadRequest.getUom() + "-" + loadRequest.getPrice());


                        //Log.e("Insert","BROOOOOOO");
                        db.addData(db.ORDER_REQUEST, map);
                    }

                    Helpers.logData(PreSaleOrderProceedActivity.this,"Order Total" + orderTotalValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            final Dialog dialog = new Dialog(PreSaleOrderProceedActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_doprint);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
            LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);
            dialog.show();
            btn_print.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //new postData().execute();
                    if (!checkforNullBeforePost()) {
                        Helpers.logData(PreSaleOrderProceedActivity.this,"Print clicked without any data input");
                        dialog.dismiss();
                        Toast.makeText(PreSaleOrderProceedActivity.this,getString(R.string.no_data),Toast.LENGTH_SHORT).show();
                    } else {
                        isPrint = true;
                        dialog.dismiss();
                        Helpers.logData(PreSaleOrderProceedActivity.this, "Printing and posting");
                        new loadData().execute();
                    }
                }
            });
            btn_notprint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //new postData().execute();
                    if (!checkforNullBeforePost()) {
                        Helpers.logData(PreSaleOrderProceedActivity.this,"Do not print clicked without any data input");
                        dialog.dismiss();
                        Toast.makeText(PreSaleOrderProceedActivity.this,getString(R.string.no_data),Toast.LENGTH_SHORT).show();
                    } else {
                        Helpers.logData(PreSaleOrderProceedActivity.this,"Do not Print and posting");
                        dialog.dismiss();
                        new loadData().execute();
                    }
                }
            });
        }
    }

    public class updateConfirmItems extends AsyncTask<Void, Void, Void> {
        ProgressDialog pd;

        private String orderId,PurNo;
        private updateConfirmItems(String orderId,String PurchashNo) {
            this.orderId = orderId;
            this.PurNo = PurchashNo;
            execute();
        }

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            if (orderId.equals(PurNo)) {
                for (OrderRequest loadRequest : arraylist) {
                    if(Double.parseDouble(loadRequest.getCases())> 0 || Double.parseDouble(loadRequest.getUnits())>0){
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        map.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        map.put(db.KEY_ORDER_ID, orderId);
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                        filter.put(db.KEY_MATERIAL_NO, loadRequest.getMaterialNo());
                        //filter.put(db.KEY_ORDER_ID,tokens[1].toString());
                        filter.put(db.KEY_PURCHASE_NUMBER, PurNo);
                        db.updateData(db.ORDER_REQUEST, map, filter);
                    }
                }
            }else {
                for (OrderRequest loadRequest : arraylist) {
                    if(Double.parseDouble(loadRequest.getCases())> 0 || Double.parseDouble(loadRequest.getUnits())>0) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        map.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                        map.put(db.KEY_ORDER_ID, orderId);
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                        filter.put(db.KEY_MATERIAL_NO, loadRequest.getMaterialNo());
                        //filter.put(db.KEY_ORDER_ID,tokens[1].toString());
                        filter.put(db.KEY_PURCHASE_NUMBER, PurNo);
                        db.updateData(db.ORDER_REQUEST, map, filter);
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            if (orderId.equals(PurNo)) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PreSaleOrderProceedActivity.this);
                alertDialogBuilder  /*.setTitle(getString(R.string.message))*/
                        //.setMessage("Request with reference " + tokens[1].toString() + " has been saved")
                        .setMessage(getString(R.string.request_created))
                        // .setMessage("Request with reference " + tokens[0].toString() + " has been saved")
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Helpers.isNetworkAvailable(getApplicationContext())) {
                                    Helpers.createBackgroundJob(getApplicationContext());
                                }
                                if (isPrint) {
                                    Helpers.logData(PreSaleOrderProceedActivity.this, "Creating data for printout");
                                    dialog.dismiss();
                                    createPrintout(false, tv_date.getText().toString(), orderId, false);
                                       /* Intent intent = new Intent(PreSaleOrderProceedActivity.this, PreSaleOrderActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("headerObj", object);
                                        startActivity(intent);
                                        finish();*/
                                } else {
                                    Helpers.logData(PreSaleOrderProceedActivity.this, "No print so going back to Order Listscreen");
                                    dialog.dismiss();
                                    createPrintout(false, tv_date.getText().toString(), orderId, true);
                                    Intent intent = new Intent(PreSaleOrderProceedActivity.this, PreSaleOrderActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("headerObj", object);
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }else{
                if (this.orderId.isEmpty() || this.orderId.equals("") || this.orderId == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();
                } else if (this.orderId.contains("Error")) {
                    Toast.makeText(getApplicationContext(), this.orderId.replaceAll("Error", "").trim(), Toast.LENGTH_SHORT).show();
                } else {
                    //Logic to go Back
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PreSaleOrderProceedActivity.this);
                    alertDialogBuilder.setTitle("Message")
                            .setMessage("Request " + PurNo + " has been created")
                            //.setMessage("Request " + tokens[0].toString() + " has been created")
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            }
        }
    }

    public class loadItemsOrder extends AsyncTask<Void, Void, Void> {
        private String orderId;
        Cursor cursor;
        private loadItemsOrder(String orderId) {
            this.orderId = orderId;
            execute();
        }
        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_DATE,"");
            map.put(db.KEY_ITEM_NO, "");
            map.put(db.KEY_MATERIAL_DESC1, "");
            map.put(db.KEY_MATERIAL_NO, "");
            map.put(db.KEY_MATERIAL_GROUP, "");
            map.put(db.KEY_CASE, "");
            map.put(db.KEY_UNIT, "");
            map.put(db.KEY_UOM, "");
            map.put(db.KEY_PRICE, "");
            map.put(db.KEY_PRICECASE, "");
            map.put(db.KEY_PRICEPCS, "");
            HashMap<String, String> filter = new HashMap<>();
            // filter.put(db.KEY_ORDER_ID, this.orderId);
            filter.put(db.KEY_PURCHASE_NUMBER, this.orderId);
            cursor = db.getData(db.ORDER_REQUEST, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                setLoadItems(cursor, true);
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
           loadingSpinner.show();
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (cursor.getCount() > 0) {
                                cursor.moveToFirst();
                                tv_date.setText(cursor.getString(cursor.getColumnIndex(db.KEY_DATE)));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            adapter.notifyDataSetChanged();
            list.setAdapter(adapter);

        }
    }
    public void setLoadItems(Cursor loadItemsCursor, Boolean isPosted) {
        try{
            final Cursor cursor = loadItemsCursor;

            do {
                OrderRequest loadRequest = new OrderRequest();
                loadRequest.setItemCode(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                loadRequest.setItemName(UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));

                ArticleHeader articleHeader = ArticleHeader.getArticle(articles, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                if(articleHeader!=null){
                    loadRequest.setItemNameAr(articleHeader.getMaterialDesc2().equals("")?App.ARABIC_TEXT_MISSING:articleHeader.getMaterialDesc2());
                }
                else{
                    loadRequest.setItemNameAr(App.ARABIC_TEXT_MISSING);
                }

                if (isPosted) {
                    String EditPrice = cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS));
                    String EditPriceCases = cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE));

                    loadRequest.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                    loadRequest.setUnits(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                    //loadRequest.setUnits("0");
                    loadRequest.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                    HashMap<String, String> priceMap = new HashMap<>();
                    priceMap.put(db.KEY_AMOUNT, "");
                    priceMap.put(db.KEY_AMOUNTMIN,"");
                    priceMap.put(db.KEY_AMOUNTMAX,"");
                    HashMap<String, String> filterPrice = new HashMap<>();
                    filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    filterPrice.put(db.KEY_PRIORITY, "2");
                    Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                    if (priceCursor.getCount() > 0) {
                        priceCursor.moveToFirst();

                        String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                        loadRequest.setPrice( price);
                        price = !price.equals(EditPrice)?EditPrice:price;
                        loadRequest.setPricepcs(price);
                        loadRequest.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                        loadRequest.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));

                    } else {
                        loadRequest.setPrice("0");
                        loadRequest.setPricemin("0");
                        loadRequest.setPricemax("0");
                        loadRequest.setPricepcs("0");
                    }
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter);
                    if (altUOMCursor.getCount() > 0) {
                        altUOMCursor.moveToFirst();
                        loadRequest.setDenominator(""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR)));
                    } else {
                        loadRequest.setDenominator("0");
                        loadRequest.setIsAltUOM(false);
                    }

                    try {
                        Double casePrice = Double.parseDouble(loadRequest.getPrice()) * Double.parseDouble(loadRequest.getDenominator());
                        String price = String.format("%.2f", casePrice);
                        price = !price.equals(EditPriceCases)?EditPriceCases:price;
                        loadRequest.setPricecase(price);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } else {
                    // loadRequest.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_BASE_UOM)).equals(App.CASE_UOM) ? "0" : "0");
                    // loadRequest.setUnits(cursor.getString(cursor.getColumnIndex(db.KEY_BASE_UOM)).equals(App.BOTTLES_UOM) ? "0" : "0");


                    loadRequest.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_BASE_UOM)));
                    loadRequest.setCases("0");
                    loadRequest.setUnits("0");
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter);
                    if (altUOMCursor.getCount() > 0) {
                        altUOMCursor.moveToFirst();
                        if (cursor.getString(cursor.getColumnIndex(db.KEY_BASE_UOM)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                            loadRequest.setIsAltUOM(false);
                        } else {
                            loadRequest.setIsAltUOM(true);
                        }
                        loadRequest.setDenominator(""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR)));
                    } else {
                        loadRequest.setDenominator("0");
                        loadRequest.setIsAltUOM(false);
                    }
                    HashMap<String, String> priceMap = new HashMap<>();
                    priceMap.put(db.KEY_AMOUNT, "");
                    priceMap.put(db.KEY_AMOUNTMIN,"");
                    priceMap.put(db.KEY_AMOUNTMAX,"");
                    HashMap<String, String> filterPrice = new HashMap<>();
                    filterPrice.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    filterPrice.put(db.KEY_PRIORITY, "2");
                    Cursor priceCursor = db.getData(db.PRICING, priceMap, filterPrice);
                    if (priceCursor.getCount() > 0) {
                        priceCursor.moveToFirst();
                        loadRequest.setPrice(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        loadRequest.setPricepcs(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT)));
                        loadRequest.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                        loadRequest.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                    } else {
                        loadRequest.setPrice("0");
                        loadRequest.setPricemin("0");
                        loadRequest.setPricemax("0");
                        loadRequest.setPricepcs("0");
                    }

                    try {
                        Double casePrice = Double.parseDouble(loadRequest.getPrice()) * Double.parseDouble(loadRequest.getDenominator());
                        loadRequest.setPricecase(String.format("%.2f", casePrice));
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                loadRequest.setMaterialNo(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                arraylist.add(loadRequest);
            }
            while (cursor.moveToNext());
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    public class postData extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> returnList;
        private String orderId = "";
        private String[] tokens = new String[2];
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            //this.returnList = IntegrationService.RequestToken(LoadRequestActivity.this);

            this.orderId = postData();
            this.tokens = orderId.split(",");
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.e("Order id", "" + this.orderId);
            try{
                if (this.tokens[0].toString().equals(this.tokens[1].toString())) {


                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    new updateConfirmItems(this.tokens[0].toString(),this.tokens[1].toString()).execute();

                } else {


                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    new updateConfirmItems(this.tokens[0].toString(),this.tokens[1].toString()).execute();

                }
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }
    private String postData() {
        String orderID = "";
        String purchaseNumber = "";
        float orderTotalValue = 0;
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("Function", ConfigStore.CustomerOrderRequestFunction);
            map.put("OrderId", "");
            map.put("DocumentType", ConfigStore.CustomerOrderRequestDocumentType);
            // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
            // map.put("DocumentDate", null);
            map.put("CustomerId", object.getCustomerID());
            map.put("SalesOrg", Settings.getString(App.SALES_ORG));
            map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
            map.put("Division", Settings.getString(App.DIVISION));

            map.put("Currency", App.CURRENCY);
            // map.put("PurchaseNum", Helpers.generateNumber(db, ConfigStore.LoadRequest_PR_Type));
            JSONArray deepEntity = new JSONArray();
            HashMap<String, String> itemMap = new HashMap<>();
            itemMap.put(db.KEY_DATE,"");
            itemMap.put(db.KEY_ITEM_NO, "");
            itemMap.put(db.KEY_MATERIAL_NO, "");
            itemMap.put(db.KEY_MATERIAL_DESC1, "");
            itemMap.put(db.KEY_CASE, "");
            itemMap.put(db.KEY_UNIT, "");
            itemMap.put(db.KEY_UOM, "");
            itemMap.put(db.KEY_PRICE, "");
            itemMap.put(db.KEY_ORDER_ID, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            Cursor cursor = db.getData(db.ORDER_REQUEST, itemMap, filter);
            //Log.e("Cursor count", "" + cursor.getCount());
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                map.put("PurchaseNum", cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                map.put("DocumentDate", Helpers.parseDateforPost(cursor.getString(cursor.getColumnIndex(db.KEY_DATE))));
                purchaseNumber = map.get("PurchaseNum");
                int itemno = 10;
                do {
                    if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM)||cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("Storagelocation", "");
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        orderTotalValue+= Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)))*Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        deepEntity.put(jo);
                    }
                    else {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("Storagelocation", "");
                        jo.put("Route", Settings.getString(App.ROUTE));
                        orderTotalValue+= Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)))*Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    }
                }
                while (cursor.moveToNext());
                //map.put("OrderValue", String.valueOf(orderTotalValue));
                map.put("OrderValue",discount<=0?String.valueOf(totalamnt+discount):String.valueOf(totalamnt-discount));
                //map.put("OrderValue", String.valueOf(totalamnt-discount));
            }
            Log.e("Map", "" + map);
            //Log.e("Deep Entity", "" + deepEntity);
            orderID = IntegrationService.postData(PreSaleOrderProceedActivity.this, App.POST_COLLECTION, map, deepEntity);
           // orderID = IntegrationService.postDataBackup(PreSaleOrderProceedActivity.this, App.POST_COLLECTION, map, deepEntity);
            //Storing Order Activity for Logging
            HashMap<String,String>logMap = new HashMap<>();
            logMap.put(db.KEY_TIME_STAMP,Helpers.getCurrentTimeStamp());
            logMap.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_ORDER);
            logMap.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
            logMap.put(db.KEY_ORDER_ID,orderID);
            logMap.put(db.KEY_PRICE,map.get("OrderValue"));
            db.addData(db.DAYACTIVITY,logMap);

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return orderID + "," + purchaseNumber;
    }
    public class loadData extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            recalculateTotal();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            if(loadingSpinner.isShowing()){
                loadingSpinner.hide();
            }
            new postData().execute();
//            new loadPromotions(App.Promotions02);
//            new loadPromotions(App.Promotions05);
//            new loadPromotions(App.Promotions07);
        }
    }
    public class loadPromotions extends AsyncTask<Void,Void,Void>{
        private String promoCode;
        private loadPromotions(String promoCode) {
            // Log.e("I m in for Load Promotions", "" + source);
            this.promoCode = promoCode;
            execute();
        }
        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String,String>map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO,"");
            map.put(db.KEY_MATERIAL_NO,"");
            map.put(db.KEY_AMOUNT,"");
            HashMap<String,String>filter = new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
            if(promoCode.equals(App.Promotions02)){
                filter.put(db.KEY_PROMOTION_TYPE,App.Promotions02);
            }
            else if(promoCode.equals(App.Promotions05)){
                filter.put(db.KEY_PROMOTION_TYPE,App.Promotions05);
            }
            else if(promoCode.equals(App.Promotions07)){
                filter.put(db.KEY_PROMOTION_TYPE,App.Promotions07);
            }
            Cursor cursor = db.getData(db.PROMOTIONS,map,filter);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                applyPromotions(cursor);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            count++;
            if(count==3){
                count=0;
                if(loadingSpinner.isShowing()){
                    loadingSpinner.hide();
                }
                onProcessingComplete();
            }
        }
    }
    private void applyPromotions(Cursor cursor){
        try{
            Cursor promotionCursor = cursor;
            promotionCursor.moveToFirst();
            do{
                for(OrderRequest request:arraylist){
                    if(request.getMaterialNo().equals(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_MATERIAL_NO)))){
                        if(request.getUom().equals(App.CASE_UOM)||request.getUom().equals(App.BOTTLES_UOM)){
                            float cases = Float.parseFloat(request.getCases());
                            discount += cases*(Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT))));
                        }
                    }
                }
            }
            while (promotionCursor.moveToNext());
            Log.e("Discount","" + discount);
            Helpers.logData(PreSaleOrderProceedActivity.this, "Discount is" + discount);
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }


    }
    public void recalculateTotal(){
        try{
            float amount=0;
            for(OrderRequest order:arraylist){
                float tempPrice = 0;
                HashMap<String,String> filterComp = new HashMap<>();
                filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                filterComp.put(db.KEY_MATERIAL_NO, order.getMaterialNo());
                HashMap<String,String> map = new HashMap<>();
                map.put(db.KEY_MATERIAL_NO,"");
                map.put(db.KEY_AMOUNT,"");
                if(db.checkData(db.PRICING,filterComp)){
                    Cursor customerPriceCursor = db.getData(db.PRICING,map,filterComp);
                    if(customerPriceCursor.getCount()>0){
                        customerPriceCursor.moveToFirst();
                        tempPrice = Float.parseFloat(customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT)));
                    }
                    if(order.getUom().equals(App.CASE_UOM)||order.getUom().equals(App.BOTTLES_UOM)){
                        amount += tempPrice*Float.parseFloat(order.getCases());
                    }
                    else{
                        amount += tempPrice*Float.parseFloat(order.getUnits());
                    }
                }
                else{
                    if(order.getUom().equals(App.CASE_UOM)||order.getUom().equals(App.BOTTLES_UOM)){
                        amount += Float.parseFloat(order.getPrice())*Float.parseFloat(order.getCases());
                        //amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                    }
                    else {
                        amount += Float.parseFloat(order.getPrice())*Float.parseFloat(order.getUnits());
                    }
                }
            }
            totalamnt = amount;
            Log.e("Total Amount","" + totalamnt);
            Helpers.logData(PreSaleOrderProceedActivity.this, "Total Amount" + totalamnt);
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    public boolean checkforNullBeforePost(){
        HashMap<String,String>map = new HashMap<>();
        map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
        map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
        map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
        return db.checkData(db.ORDER_REQUEST,map);
    }
    private void createPrintout(boolean fromList,String orderDate,String orderNo,boolean isDelayPrint){
        Log.e("Came for Print", "Came for");
        Helpers.logData(PreSaleOrderProceedActivity.this, "Creating printount for" + fromList + orderDate + orderNo);
        if(!isDelayPrint){
            if(fromList){
                JSONArray jsonArray = createPrintData(orderDate,orderNo);
                PrinterHelper object = new PrinterHelper(PreSaleOrderProceedActivity.this,PreSaleOrderProceedActivity.this);
                object.execute("", jsonArray);
            }
            else{
                try{
                    JSONArray jsonArray = createPrintData(orderDate,orderNo);
                    JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);
                    Helpers.logData(PreSaleOrderProceedActivity.this, "JSON Data string for print" + data.toString());
                    HashMap<String,String>map = new HashMap<>();
                    map.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                    map.put(db.KEY_ORDER_ID,orderNo);
                    map.put(db.KEY_DOC_TYPE,ConfigStore.OrderRequest_TR);
                    map.put(db.KEY_DATA,data.toString());
                    db.addDataPrint(db.DELAY_PRINT,map);

                    PrinterHelper object = new PrinterHelper(PreSaleOrderProceedActivity.this,PreSaleOrderProceedActivity.this);
                    object.execute("", jsonArray);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }
        }
        else{
            if(fromList){
                /*try{
                    JSONArray jsonArray = createPrintData(orderDate,orderNo);
                    JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);

                    HashMap<String,String>map = new HashMap<>();
                    map.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                    map.put(db.KEY_ORDER_ID,orderNo);
                    map.put(db.KEY_DOC_TYPE,ConfigStore.OrderRequest_TR);
                    map.put(db.KEY_DATA,data.toString());
                    db.addDataPrint(db.DELAY_PRINT,map);
                }
                catch (Exception e){
                    e.printStackTrace();
                }*/

            }
            else{
                try{
                    JSONArray jsonArray = createPrintData(orderDate,orderNo);
                    JSONObject data = new JSONObject();
                    data.put("data",(JSONArray)jsonArray);
                    Helpers.logData(PreSaleOrderProceedActivity.this, "JSON Data string for print" + data.toString());
                    HashMap<String,String>map = new HashMap<>();
                    map.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                    map.put(db.KEY_ORDER_ID,orderNo);
                    map.put(db.KEY_DOC_TYPE,ConfigStore.OrderRequest_TR);
                    map.put(db.KEY_DATA,data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT, map);
                }
                catch (Exception e){
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                /*JSONArray jsonArray = createPrintData(orderDate,orderNo);
                PrinterHelper object = new PrinterHelper(PreSaleOrderProceedActivity.this,PreSaleOrderProceedActivity.this);
                object.execute("", jsonArray);*/
            }
        }

    }
    public JSONArray createPrintData(String orderDate,String orderNo){
        JSONArray jArr = new JSONArray();
        try{
            double totalPcs = 0;
            double totalAmount = 0;
            double totalAmountwithVat = 0;
            double totalVat = 0;
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST,App.ORDER_REQUEST);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE",Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", tv_date.getText().toString());
            mainArr.put("TIME",Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO","1234");
            mainArr.put("DOCUMENT NO",orderNo);  //Load Summary No
           // mainArr.put("TRIP START DATE",Helpers.formatDate(new Date(),"dd-MM-yyyy"));
            mainArr.put("supervisorname","-");
            mainArr.put("LANG",Settings.getString(App.LANGUAGE));
            mainArr.put("INVOICETYPE","ORDER REQUEST");
            mainArr.put("ORDERNO",orderNo);
            mainArr.put("invoicepaymentterms","3");
//            String testAr = "هذا هو اختبار النص العربي";

            String strCustomer = object.getCustomerName();

            if(object.getCustomer_name_ar() != null){
                strCustomer = strCustomer + " - " + object.getCustomer_name_ar();
            }

            mainArr.put("CUSTOMERID", object.getCustomerID());
            mainArr.put("CUSTOMER", strCustomer);
            mainArr.put("ADDRESS",object.getCustomerAddress().equals("")?object.getCustomerAddress():"-");
            mainArr.put("ARBADDRESS",object.getCustomerAddress());
            mainArr.put("TripID",Settings.getString(App.TRIP_ID));
            mainArr.put("DELIVERY DATE",orderDate);


            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();

            HEADERS.put("SI No");
            HEADERS.put("Item Code");
            HEADERS.put("Description");
            HEADERS.put("PKG CTN      PCS");
            HEADERS.put("UNIT Price");
            HEADERS.put("Total amount");
            HEADERS.put("Total Disc");
            HEADERS.put("Vat Amt");
            HEADERS.put("Vat %");
            HEADERS.put("Amount AED");

            mainArr.put("HEADERS",HEADERS);


            JSONArray jData = new JSONArray();
            for(OrderRequest obj:arraylist){
                if(Double.parseDouble(obj.getCases())> 0 || Double.parseDouble(obj.getUnits())>0){
                    JSONArray data = new JSONArray();
                    data.put(""+(jData.length() + 1));
                    data.put(obj.getMaterialNo());
                    data.put(obj.getItemName());
//                    data.put("PCS");

                    double amount = 0.0,qty = 0.0;
                    String deno = "0";
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filtera = new HashMap<>();
                    filtera.put(db.KEY_MATERIAL_NO, obj.getMaterialNo());
                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                    if (altUOMCursor.getCount() > 0) {
                        altUOMCursor.moveToFirst();
                        deno = ""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                    }
                    if (Double.parseDouble(obj.getCases()) >= 1) {



                        Double casePrice = Double.parseDouble(obj.getPricecase());
                        amount += casePrice * Float.parseFloat(obj.getCases());
                        qty = Float.parseFloat(obj.getCases()) * Double.parseDouble(deno);
                    }
                    if(Double.parseDouble(obj.getUnits()) >= 1){
                        amount += Float.parseFloat(obj.getPricepcs()) * Float.parseFloat(obj.getUnits());
                        qty += Double.parseDouble(obj.getUnits());
                    }
                    double itemValue = amount/qty;

                    int intCases = 0;
                    int intpcs = 0;
                    if(Double.parseDouble(deno) > 0) {
                    if(qty > Double.parseDouble(deno)) {
                        double Allitem = qty / Double.parseDouble(deno);
                        intCases = (int) Allitem;
                        intpcs = (int) qty - (intCases * Integer.parseInt(deno));
                    }else{
                        intpcs = (int)qty;
                    }
                    }else{
                        intpcs = (int)qty;
                    }


                    Double vat = amount*Double.parseDouble(Settings.getString(App.VATValue));
                    Double totalAmt = amount+vat;
                    data.put(String.valueOf(intCases)+"      "+String.valueOf(intpcs));
                    data.put(String.format("%.2f",itemValue));
                    data.put(String.format("%.2f",amount));
                    data.put("0");
                    data.put(String.format("%.2f",vat));
                    data.put(Settings.getString(App.VATPR));
                    data.put(Double.parseDouble(String.format("%.2f",totalAmt)));
                    jData.put(data);

                    totalPcs += Double.parseDouble(String.format("%.2f",qty));
                    totalAmount += Double.parseDouble(String.format("%.2f",amount));
                    totalVat += Double.parseDouble(String.format("%.2f",vat));
                    totalAmountwithVat += Double.parseDouble(String.format("%.2f",totalAmt));
                }
            }
            JSONObject totalObj = new JSONObject();
            totalObj.put("Total Befor TAX(AED)", String.format("%.2f",totalAmount));
            totalObj.put("VAT(AED)",String.format("%.2f",totalVat));
            totalObj.put("Total Amount(AED)", String.format("%.2f",totalAmountwithVat));
            TOTAL.put(totalObj);
            mainArr.put("TOTAL",TOTAL);
            mainArr.put("data",jData);

            jDict.put("mainArr",mainArr);
            jInter.put(jDict);
            jArr.put(jInter);

            // jArr.put(HEADERS);
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return jArr;
    }
    public void callback(){
        Helpers.logData(PreSaleOrderProceedActivity.this, "Call back function for order request from print");
        Intent intent = new Intent(PreSaleOrderProceedActivity.this, PreSaleOrderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("headerObj", object);
        startActivity(intent);
        finish();
    }
}
