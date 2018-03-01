package com.ae.benchmark.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.DeliveryItem;
import com.ae.benchmark.models.DeliveryOrderList;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.sap.DataListener;
import com.ae.benchmark.sap.IntegrationService;
import com.ae.benchmark.sap.IntegrationServiceJSON;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.PrinterHelper;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
import com.crashlytics.android.Crashlytics;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by eheuristic on 12/6/2016.
 */
public class PromotioninfoActivity extends AppCompatActivity implements DataListener {
    ImageView iv_back;
    TextView tv_top_header;
    TextView tv_promotion;
    LinearLayout ll_bottom;
    String str_promotion_message = "";
    Customer object;
    String invoiceAmount;
    String from;
    DatabaseHandler db = new DatabaseHandler(this);
    LoadingSpinner loadingSpinner;
    float totalamnt = 0;
    float discount = 0;
    float goodreturntotal = 0;
    float goodreturndiscount = 0;
    float badreturntotal = 0;
    float badreturndiscount = 0;
    TextView tv_current_invoice;
    ArrayList<Sales> arraylist = new ArrayList<>();
    ArrayList<Sales> arraylistGR = new ArrayList<>();
    ArrayList<Sales> arraylistBR = new ArrayList<>();
    ArrayList<Sales> focList = new ArrayList<>();
    ArrayList<Sales> grList = new ArrayList<>();
    ArrayList<Sales> brList = new ArrayList<>();
    ArrayList<DeliveryItem> deliveryArrayList = new ArrayList<>();
    int count = 0;
    int returnCount = 0;
    int referenceCount = 0;
    boolean isPrint = false;
    boolean isDataAdd = false;
    EditText tv_discount;
    EditText tv_net_invoice;
    EditText tv_net_invoiceVat;
    EditText tv_Vat;
    DeliveryOrderList delivery;
    ArrayList<ArticleHeader> articles;
    String orderID_Global;
    String orderID_GlobalGR="";
    String orderID_GlobalBR="";
    String data = "";

    String tempOrderId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        articles = ArticleHeaders.get();
        loadingSpinner = new LoadingSpinner(this);
        Intent i = this.getIntent();
        object = (Customer) i.getParcelableExtra("headerObj");
        delivery = (DeliveryOrderList) i.getParcelableExtra("delivery");

        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        Helpers.logData(PromotioninfoActivity.this, "On Promotion Info Activity");
        tv_top_header.setText(getString(R.string.promo_details));
        tv_promotion = (TextView) findViewById(R.id.tv_promotion);
        tv_current_invoice = (TextView) findViewById(R.id.tv_invoice_amount);
        tv_current_invoice.setTextColor(Color.BLACK);
        tv_discount = (EditText) findViewById(R.id.et_discount);
        tv_discount.setTextColor(Color.BLACK);
        tv_net_invoice = (EditText) findViewById(R.id.tv_net_invoice);
        tv_net_invoiceVat = (EditText) findViewById(R.id.tv_net_invoiceVat);
        tv_Vat = (EditText) findViewById(R.id.et_Vat);
        tv_net_invoice.setTextColor(Color.BLACK);
        tv_net_invoiceVat.setTextColor(Color.BLACK);
        tv_Vat.setTextColor(Color.BLACK);

        //Display Vat After 31 Des
        if (Const.isDisplayVatTax) {
            tv_Vat.setVisibility(View.VISIBLE);
            tv_net_invoiceVat.setVisibility(View.VISIBLE);

            tv_net_invoice.setVisibility(View.GONE);
        } else {
            tv_net_invoice.setVisibility(View.VISIBLE);

            tv_Vat.setVisibility(View.GONE);
            tv_net_invoiceVat.setVisibility(View.GONE);
        }

        if (getIntent().getExtras() != null) {
            str_promotion_message = getIntent().getExtras().getString("msg", "extra Promotion");
            from = getIntent().getExtras().getString("from", "");
            tv_promotion.setText(str_promotion_message.substring(0, 1).toUpperCase() + str_promotion_message.substring(1).toLowerCase());
            int pos = getIntent().getIntExtra("pos", 10);
            if (from.equals("Final Invoice")) {
                ll_bottom.setVisibility(View.VISIBLE);
            } else {
                if (from.equals("delivery")) {
                    ll_bottom.setVisibility(View.VISIBLE);
                } else {
                    ll_bottom.setVisibility(View.GONE);
                }
            }
        }
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please complete invoice", Toast.LENGTH_LONG).show();

                //  finish();
            }
        });
        ll_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.logData(PromotioninfoActivity.this, "User clicked on Complete Invoice button");
                try {


                    if (from.equals("Final Invoice")) {
                        final Dialog dialog = new Dialog(PromotioninfoActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_doprint);
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
                        LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);
                        dialog.show();
                        Const.siBundle = null;
                        Const.grBundle = null;
                        Const.brBundle = null;
                        Const.focBundle = null;
                        btn_print.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isPrint = true;
                                Helpers.logData(PromotioninfoActivity.this, "User clicked on Print button");
                                dialog.cancel();
                                if (returnExist("")) {
                                    if (returnExist(App.GOOD_RETURN) && returnExist(App.BAD_RETURN)) {
                                        referenceCount = 2;
                                        new postReturns(App.GOOD_RETURN);
                                        new postReturns(App.BAD_RETURN);
                                    } else if (returnExist(App.GOOD_RETURN)) {
                                        referenceCount = 1;
                                        new postReturns(App.GOOD_RETURN);
                                    } else if (returnExist(App.BAD_RETURN)) {
                                        referenceCount = 1;
                                        new postReturns(App.BAD_RETURN);
                                    }
                                } else {
                                    new postData().execute();
                                }
                            }
                        });
                        btn_notprint.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                           /* Intent intent = new Intent(PromotioninfoActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();*/
                                isPrint = false;
                                Helpers.logData(PromotioninfoActivity.this, "User clicked on Do Not Print button");
                                dialog.cancel();
                                if (returnExist("")) {
                                    if (returnExist(App.GOOD_RETURN) && returnExist(App.BAD_RETURN)) {
                                        referenceCount = 2;
                                        new postReturns(App.GOOD_RETURN);
                                        new postReturns(App.BAD_RETURN);
                                    } else if (returnExist(App.GOOD_RETURN)) {
                                        referenceCount = 1;
                                        new postReturns(App.GOOD_RETURN);
                                    } else if (returnExist(App.BAD_RETURN)) {
                                        referenceCount = 1;
                                        new postReturns(App.BAD_RETURN);
                                    }
                                } else {
                                    new postData().execute();
                                }
                            }
                        });
                    } else if (from.equals("delivery")) {
                        //Logging sales for todays Summary

                        if (!isDataAdd) {
                            isDataAdd = true;
                            HashMap<String, String> todaysSummary = new HashMap<>();
                            todaysSummary.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            todaysSummary.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            todaysSummary.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                            todaysSummary.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_DELIVERY);
                            todaysSummary.put(db.KEY_ORDER_TOTAL, tv_net_invoiceVat.getText().toString());
                            todaysSummary.put(db.KEY_ORDER_DISCOUNT, tv_discount.getText().toString());
                            todaysSummary.put(db.KEY_ORDER_ID, delivery.getOrderId());
                            db.addData(db.TODAYS_SUMMARY, todaysSummary);

                            HashMap<String, String> todaysSalesSummary = new HashMap<>();
                            todaysSalesSummary.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            todaysSalesSummary.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            todaysSalesSummary.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                            todaysSalesSummary.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_DELIVERY);
                            todaysSalesSummary.put(db.KEY_ORDER_TOTAL, tv_net_invoiceVat.getText().toString());
                            todaysSalesSummary.put(db.KEY_ORDER_NET_TOTAL, tv_current_invoice.getText().toString());
                            todaysSalesSummary.put(db.KEY_RETURN_TOTAL, String.format("%.2f", goodreturntotal + badreturntotal));
                            todaysSalesSummary.put(db.KEY_GOOD_RETURN_TOTAL, String.format("%.2f", goodreturntotal));
                            todaysSalesSummary.put(db.KEY_ORDER_DISCOUNT, tv_discount.getText().toString());
                            todaysSalesSummary.put(db.KEY_ORDER_ID, delivery.getOrderId());
                            db.addData(db.TODAYS_SUMMARY_SALES, todaysSalesSummary);
                        }
                        final Dialog dialog = new Dialog(PromotioninfoActivity.this);
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
                                dialog.dismiss();
                                Helpers.logData(PromotioninfoActivity.this, "User clicked on Print button");
                                isPrint = true;
                                if (data.equals("")) {
                                    new postDeliveryData().execute();
                                } else {
                                    setData(data.split(","));
                                }
                                /*Intent intent = new Intent(PaymentDetails.this, DashboardActivity.class);
                                startActivity(intent);
                                finish();*/
                            }
                        });
                        btn_notprint.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                isPrint = false;
                                Helpers.logData(PromotioninfoActivity.this, "User clicked on Do Not Print button");
                                if (data.equals("")) {
                                    new postDeliveryData().execute();
                                } else {
                                    setData(data.split(","));
                                }
                            /*dialog.dismiss();
                            Intent intent = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                            intent.putExtra("headerObj", object);
                            intent.putExtra("msg", "visit");
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);*/
                            }
                        });
                    } else {
                        if (from.equals("")) {
                            final Dialog dialog = new Dialog(PromotioninfoActivity.this);
                            dialog.setContentView(R.layout.dialog_doprint);
                            dialog.setCancelable(true);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            LinearLayout btn_print = (LinearLayout) dialog.findViewById(R.id.ll_print);
                            LinearLayout btn_notprint = (LinearLayout) dialog.findViewById(R.id.ll_notprint);
                            dialog.show();
                            btn_print.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.cancel();
                                }
                            });
                            btn_notprint.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.cancel();
                                    Intent intent = new Intent(PromotioninfoActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            finish();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }

            }
        });
        if (from.equals("Final Invoice")) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            if (db.checkData(db.CAPTURE_SALES_INVOICE, map)) {
                new loadData().execute();
            } else if (returnExist("")) {
                if (returnExist(App.GOOD_RETURN)) {
                    new loadReturns(App.GOOD_RETURN);
                } else if (returnExist(App.BAD_RETURN)) {
                    new loadReturns(App.BAD_RETURN);
                }
            }
        } else if (from.equals("delivery")) {


            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            map.put(db.KEY_DELIVERY_NO, delivery.getOrderId());
            if (db.checkData(db.CUSTOMER_DELIVERY_ITEMS_POST, map)) {
                new loadDeliveryData().execute();
            }

        }
    }

    private boolean returnExist(String returnType) {
        Helpers.logData(PromotioninfoActivity.this, "Checking if return exist for order");
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_TIME_STAMP, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            if (returnType.equals("") || returnType == null) {
            } else {
                filter.put(db.KEY_REASON_TYPE, returnType);
            }
            if (db.checkData(db.RETURNS, filter)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return false;
        }

    }

    private boolean invoiceExist() {
        Helpers.logData(PromotioninfoActivity.this, "Checking if invoice exist for order");
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_TIME_STAMP, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            if (db.checkData(db.CAPTURE_SALES_INVOICE, filter)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return false;
        }

    }

    public void recalculateTotal(Cursor cursor, String from, String source) {
        try {
            if (from.equals("Final Invoice")) {
                if (source.equals("Sales Invoice")) {
                    Cursor saleCursor = cursor;
                    saleCursor.moveToFirst();
                    float amount = 0;
                    do {
                        try {
                            float tempPrice = 0;
                            HashMap<String, String> filterComp = new HashMap<>();
                            filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filterComp.put(db.KEY_MATERIAL_NO, saleCursor.getString(saleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_MATERIAL_NO, "");
                            map.put(db.KEY_AMOUNT, "");
                            /*if (db.checkData(db.PRICING, filterComp)) {
                                Cursor customerPriceCursor = db.getData(db.PRICING, map, filterComp);
                                if (customerPriceCursor.getCount() > 0) {
                                    customerPriceCursor.moveToFirst();
                                    tempPrice = Float.parseFloat(customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT)));
                                }

                                if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_ORG_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = ""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = tempPrice * Double.parseDouble(deno);
                                    amount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                                }
                                if(Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_ORG_UNITS))) >= 1){
                                    amount += tempPrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                                }

                            } else {*/
                            if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_ORG_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_UOM, "");
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                Double casePrice = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTCASE)));
                                amount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                            }
                            if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_ORG_UNITS))) >= 1) {
                                amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTPCS))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                            }
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                    while (saleCursor.moveToNext());
                    totalamnt = amount;
                } else if (source.equals(App.GOOD_RETURN)) {
                    Cursor saleCursor = cursor;
                    saleCursor.moveToFirst();
                    float amount = 0;
                    do {
                        try {
                            float tempPriceCases = 0, tempPrice = 0;
                            HashMap<String, String> filterComp = new HashMap<>();
                            filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filterComp.put(db.KEY_MATERIAL_NO, saleCursor.getString(saleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_MATERIAL_NO, "");
                            map.put(db.KEY_AMOUNT, "");
                            /*if (db.checkData(db.PRICING, filterComp)) {
                                Cursor customerPriceCursor = db.getData(db.PRICING, map, filterComp);
                                if (customerPriceCursor.getCount() > 0) {
                                    customerPriceCursor.moveToFirst();
                                    tempPrice = Float.parseFloat(customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT)));
                                }
                                if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = ""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = tempPrice * Double.parseDouble(deno);
                                    amount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                                }
                                if(Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_UNIT))) >= 1){
                                    amount += tempPrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                                }
                            } else {*/

                            if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_UOM, "");
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                Double casePrice = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE)));
                                amount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                                tempPriceCases = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE)));
                            }
                            if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                                tempPrice = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS)));
                            }
//                            }
                            //Added only for printout
                            Sales sales = new Sales();
                            sales.setItem_code(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_ITEM_NO)));
                            sales.setMaterial_no(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            sales.setMaterial_description(UrlBuilder.decodeString(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                            sales.setCases(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_CASE)));
                            sales.setPic(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_UNIT)));
                            sales.setPrice(String.format("%.2f", tempPrice));
                            sales.setPricepcs(String.format("%.2f", tempPrice));
                            sales.setPricecase(String.format("%.2f", tempPriceCases));
                            grList.add(sales);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }

                    }
                    while (saleCursor.moveToNext());
                    goodreturntotal = amount;
                } else if (source.equals(App.BAD_RETURN)) {
                    Cursor saleCursor = cursor;
                    saleCursor.moveToFirst();
                    float amount = 0;
                    do {
                        try {
                            float tempPriceCases = 0, tempPrice = 0;
                            HashMap<String, String> filterComp = new HashMap<>();
                            filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filterComp.put(db.KEY_MATERIAL_NO, saleCursor.getString(saleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_MATERIAL_NO, "");
                            map.put(db.KEY_AMOUNT, "");
                            /*if (db.checkData(db.PRICING, filterComp)) {
                                Cursor customerPriceCursor = db.getData(db.PRICING, map, filterComp);
                                if (customerPriceCursor.getCount() > 0) {
                                    customerPriceCursor.moveToFirst();
                                    tempPrice = Float.parseFloat(customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT)));
                                }
                                if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                    String deno = "0";
                                    HashMap<String, String> altMap = new HashMap<>();
                                    altMap.put(db.KEY_UOM, "");
                                    altMap.put(db.KEY_DENOMINATOR, "");
                                    HashMap<String, String> filtera = new HashMap<>();
                                    filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                    if (altUOMCursor.getCount() > 0) {
                                        altUOMCursor.moveToFirst();
                                        deno = ""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                    }

                                    Double casePrice = tempPrice * Double.parseDouble(deno);
                                    amount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                                }
                                if(Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_UNIT))) >= 1){
                                    amount += tempPrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                                }
                            } else {*/

                            if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_CASE))) >= 1) {
                                String deno = "0";
                                HashMap<String, String> altMap = new HashMap<>();
                                altMap.put(db.KEY_UOM, "");
                                altMap.put(db.KEY_DENOMINATOR, "");
                                HashMap<String, String> filtera = new HashMap<>();
                                filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                                if (altUOMCursor.getCount() > 0) {
                                    altUOMCursor.moveToFirst();
                                    deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                                }

                                Double casePrice = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE)));
                                amount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                                tempPriceCases = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE)));
                            }
                            if (Float.parseFloat(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_UNIT))) >= 1) {
                                amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                                tempPrice = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS)));
                            }
//                            }
                            //Added only for printout
                            Sales sales = new Sales();
                            sales.setItem_code(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_ITEM_NO)));
                            sales.setMaterial_no(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            sales.setMaterial_description(UrlBuilder.decodeString(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                            sales.setCases(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_CASE)));
                            sales.setPic(saleCursor.getString(saleCursor.getColumnIndex(db.KEY_UNIT)));
                            sales.setPrice(String.format("%.2f", tempPrice));
                            sales.setPricepcs(String.format("%.2f", tempPrice));
                            sales.setPricecase(String.format("%.2f", tempPriceCases));
                            brList.add(sales);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                    while (saleCursor.moveToNext());
                    badreturntotal = amount;
                }
            } else {
                Cursor deliveryCursor = cursor;
                deliveryCursor.moveToFirst();
                do {
                    try {
                        float tempPrice = 0;
                        HashMap<String, String> filterComp = new HashMap<>();
                        filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filterComp.put(db.KEY_MATERIAL_NO, deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_AMOUNT, "");
                        if (db.checkData(db.PRICING, filterComp)) {
                            Cursor customerPriceCursor = db.getData(db.PRICING, map, filterComp);
                            if (customerPriceCursor.getCount() > 0) {
                                customerPriceCursor.moveToFirst();
                                tempPrice = Float.parseFloat(customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT)));
                            }
                            if (deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM_NEW) || deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                totalamnt += tempPrice * Float.parseFloat(deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_CASE)));
                                //amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                            }
                        } else {
                            if (deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM_NEW) || deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                totalamnt += Float.parseFloat(deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_AMOUNT))) * Float.parseFloat(deliveryCursor.getString(deliveryCursor.getColumnIndex(db.KEY_CASE)));
                                //amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }

                }
                while (deliveryCursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    @Override
    public void onProcessingComplete() {
        tv_discount.setText(String.format("%.2f", discount + goodreturndiscount + badreturndiscount));
        Double vat = totalamnt * Double.parseDouble(Settings.getString(App.VATValue));
        tv_Vat.setText(String.format("%.2f", vat));
        tv_net_invoiceVat.setText(String.format("%.2f", (totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount) + vat));
        tv_net_invoice.setText(String.format("%.2f", totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount));
    }

    @Override
    public void onProcessingComplete(String source) {
        try {
            if (source.equals("Invoice")) {
                if (returnExist("")) {
                    if (returnExist(App.GOOD_RETURN)) {
                        new loadReturns(App.GOOD_RETURN);
                    } else if (returnExist(App.BAD_RETURN)) {
                        new loadReturns(App.BAD_RETURN);
                    }
                } else {
                    tv_discount.setText(String.format("%.2f", discount + goodreturndiscount + badreturndiscount));
                    Double salesvat = totalamnt * Double.parseDouble(Settings.getString(App.VATValue));
                    Double brvat = badreturntotal * Double.parseDouble(Settings.getString(App.VATValue));
                    Double grvat = goodreturntotal * Double.parseDouble(Settings.getString(App.VATValue));

                    Double vat = salesvat - (brvat + grvat);
                    tv_Vat.setText(String.format("%.2f", vat));
                    tv_net_invoiceVat.setText(String.format("%.2f", (totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount) + vat));
                    tv_net_invoice.setText(String.format("%.2f", totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount));

                }
            } else if (source.equals("Delivery")) {
                tv_discount.setText(String.format("%.2f", discount + goodreturndiscount + badreturndiscount));
                Double salesvat = totalamnt * Double.parseDouble(Settings.getString(App.VATValue));
                Double brvat = badreturntotal * Double.parseDouble(Settings.getString(App.VATValue));
                Double grvat = goodreturntotal * Double.parseDouble(Settings.getString(App.VATValue));

                Double vat = salesvat - (brvat + grvat);
                tv_Vat.setText(String.format("%.2f", vat));
                tv_net_invoiceVat.setText(String.format("%.2f", (totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount) + vat));
                tv_net_invoice.setText(String.format("%.2f", totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount));
            } else if (source.equals(App.GOOD_RETURN)) {
                if (returnExist(App.BAD_RETURN)) {
                    new loadReturns(App.BAD_RETURN);
                } else {
                    tv_discount.setText(String.format("%.2f", discount + goodreturndiscount + badreturndiscount));
                    Double salesvat = totalamnt * Double.parseDouble(Settings.getString(App.VATValue));
                    Double brvat = badreturntotal * Double.parseDouble(Settings.getString(App.VATValue));
                    Double grvat = goodreturntotal * Double.parseDouble(Settings.getString(App.VATValue));

                    Double vat = salesvat - (brvat + grvat);
                    tv_Vat.setText(String.format("%.2f", vat));
                    tv_net_invoiceVat.setText(String.format("%.2f", (totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount) + vat));
                    tv_net_invoice.setText(String.format("%.2f", totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount));
                }
            } else if (source.equals(App.BAD_RETURN)) {
                tv_discount.setText(String.format("%.2f", discount + goodreturndiscount + badreturndiscount));
                Double salesvat = totalamnt * Double.parseDouble(Settings.getString(App.VATValue));
                Double brvat = badreturntotal * Double.parseDouble(Settings.getString(App.VATValue));
                Double grvat = goodreturntotal * Double.parseDouble(Settings.getString(App.VATValue));

                Double vat = salesvat - (brvat + grvat);
                tv_Vat.setText(String.format("%.2f", vat));
                tv_net_invoiceVat.setText(String.format("%.2f", (totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount) + vat));
                tv_net_invoice.setText(String.format("%.2f", totalamnt + discount - goodreturntotal + goodreturndiscount - badreturntotal + badreturndiscount));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    public class loadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_ORG_CASE, "");
            map.put(db.KEY_ORG_UNITS, "");
            map.put(db.KEY_AMOUNT, "");
            map.put(db.KEY_AMOUNTPCS, "");
            map.put(db.KEY_AMOUNTCASE, "");
            map.put(db.KEY_UOM, "");
            map.put(db.KEY_MATERIAL_NO, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            Cursor cursor = db.getData(db.CAPTURE_SALES_INVOICE, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                recalculateTotal(cursor, from, "Sales Invoice");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            new loadPromotions(App.Promotions02, "Invoice");
            new loadPromotions(App.Promotions05, "Invoice");
            new loadPromotions(App.Promotions07, "Invoice");
            tv_current_invoice.setText(String.format("%.2f", totalamnt));
        }
    }

    public class loadReturns extends AsyncTask<Void, Void, Void> {
        private String returnType = "";

        private loadReturns(String returnType) {
            this.returnType = returnType;
            execute();
        }

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_TIME_STAMP, "");
            map.put(db.KEY_TRIP_ID, "");
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_REASON_TYPE, "");
            map.put(db.KEY_REASON_CODE, "");
            map.put(db.KEY_ITEM_NO, "");
            map.put(db.KEY_MATERIAL_DESC1, "");
            map.put(db.KEY_MATERIAL_NO, "");
            map.put(db.KEY_MATERIAL_GROUP, "");
            map.put(db.KEY_CASE, "");
            map.put(db.KEY_UNIT, "");
            map.put(db.KEY_UOM, "");
            map.put(db.KEY_PRICE, "");
            map.put(db.KEY_PRICEPCS, "");
            map.put(db.KEY_PRICECASE, "");
            map.put(db.KEY_ORDER_ID, "");
            map.put(db.KEY_PURCHASE_NUMBER, "");
            map.put(db.KEY_IS_POSTED, "");
            map.put(db.KEY_IS_PRINTED, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            filter.put(db.KEY_REASON_TYPE, this.returnType);
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            Cursor cursor = db.getData(db.RETURNS, map, filter);
            recalculateTotal(cursor, from, this.returnType);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            new loadPromotions(App.Promotions02, this.returnType);
            new loadPromotions(App.Promotions05, this.returnType);
            new loadPromotions(App.Promotions07, this.returnType);
            tv_current_invoice.setText(String.format("%.2f", totalamnt - goodreturntotal - badreturntotal));
        }
    }

    public class loadDeliveryData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_CASE, "");
            map.put(db.KEY_UNIT, "");
            map.put(db.KEY_AMOUNT, "");
            map.put(db.KEY_UOM, "");
            map.put(db.KEY_MATERIAL_NO, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            filter.put(db.KEY_DELIVERY_NO, delivery.getOrderId());
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            Cursor cursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                recalculateTotal(cursor, from, "Delivery");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (loadingSpinner.isShowing()) {
                loadingSpinner.hide();
            }
            new loadPromotions(App.Promotions02, "Delivery");
            new loadPromotions(App.Promotions05, "Delivery");
            new loadPromotions(App.Promotions07, "Delivery");
            tv_current_invoice.setText(String.format("%.2f", totalamnt));
        }
    }

    public class postReturns extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> returnList;
        private String orderID = "";
        private String[] tokens = new String[2];
        private String returnType = "";

        private postReturns(String returnType) {
            this.returnType = returnType;
            execute();
        }

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //this.returnList = IntegrationService.RequestToken(LoadRequestActivity.this);
            this.orderID = postReturns(this.returnType);
            this.tokens = orderID.split(",");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                if (loadingSpinner.isShowing()) {
                    loadingSpinner.hide();
                }
                Log.e("Order ID Post Returns", "" + this.orderID);
                orderID_Global = tokens[0].toString();
                if (returnType.equals(App.GOOD_RETURN)) {
                    orderID_GlobalGR = tokens[0].toString();
                } else {
                    orderID_GlobalBR = tokens[0].toString();
                }
                returnCount++;
                arraylist.clear();
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_TIME_STAMP, "");
                map.put(db.KEY_TRIP_ID, "");
                map.put(db.KEY_CUSTOMER_NO, "");
                map.put(db.KEY_REASON_TYPE, "");
                map.put(db.KEY_REASON_CODE, "");
                map.put(db.KEY_ITEM_NO, "");
                map.put(db.KEY_MATERIAL_DESC1, "");
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_MATERIAL_GROUP, "");
                map.put(db.KEY_CASE, "");
                map.put(db.KEY_UNIT, "");
                map.put(db.KEY_UOM, "");
                map.put(db.KEY_PRICE, "");
                map.put(db.KEY_PRICEPCS, "");
                map.put(db.KEY_PRICECASE, "");
                map.put(db.KEY_ORDER_ID, "");
                map.put(db.KEY_PURCHASE_NUMBER, "");
                map.put(db.KEY_IS_POSTED, "");
                map.put(db.KEY_IS_PRINTED, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                filter.put(db.KEY_REASON_TYPE, this.returnType);
                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                Cursor cursor = db.getData(db.RETURNS, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        Sales sale = new Sales();
                        sale.setMaterial_no(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        sale.setMaterial_description(UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                        sale.setPic(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        sale.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        sale.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        sale.setPricecase(cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE)));
                        sale.setPricepcs(cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS)));
                        if (returnType.equals(App.GOOD_RETURN)) {
                            arraylistGR.add(sale);
                        } else {
                            arraylistBR.add(sale);
                        }
                        //arraylist.add(sale);
                    }
                    while (cursor.moveToNext());
                }
                if (this.tokens[0].toString().equals(this.tokens[1].toString())) {
                    if (returnType.equals(App.GOOD_RETURN)) {
                        for (Sales sale : arraylistGR) {
                            HashMap<String, String> postmap = new HashMap<String, String>();
                            postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            postmap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                            postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                            HashMap<String, String> filtermap = new HashMap<>();
                            filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                            filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filtermap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                            filtermap.put(db.KEY_REASON_TYPE, this.returnType);
                            db.updateData(db.RETURNS, postmap, filtermap);
                        }
                    } else {
                        for (Sales sale : arraylistBR) {
                            HashMap<String, String> postmap = new HashMap<String, String>();
                            postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            postmap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                            postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                            HashMap<String, String> filtermap = new HashMap<>();
                            filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                            filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filtermap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                            filtermap.put(db.KEY_REASON_TYPE, this.returnType);
                            db.updateData(db.RETURNS, postmap, filtermap);
                        }
                    }

                    if (returnCount == referenceCount) {
                        if (invoiceExist()) {
                            if (loadingSpinner.isShowing()) {
                                loadingSpinner.hide();
                            }
                            if (returnType.equals(App.GOOD_RETURN)) {
                                updateStockinVan(true);
                            }
                            new postData().execute();
                        } else {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PromotioninfoActivity.this);
                            alertDialogBuilder.setTitle(getString(R.string.message))
                                    .setMessage(getString(R.string.request_created))
                                    //.setMessage("Request with reference " + tokens[0].toString() + " has been saved")
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {


                                            if (returnType.equals(App.GOOD_RETURN)) {
                                                orderID_GlobalGR = "";
                                                HashMap<String, String> todaysSalesSummary = new HashMap<>();
                                                todaysSalesSummary.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                                todaysSalesSummary.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                todaysSalesSummary.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                                                todaysSalesSummary.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                                                todaysSalesSummary.put(db.KEY_ORDER_TOTAL, String.format("%.2f", goodreturntotal));
                                                todaysSalesSummary.put(db.KEY_ORDER_NET_TOTAL, String.format("%.2f", goodreturntotal));
                                                todaysSalesSummary.put(db.KEY_RETURN_TOTAL, String.format("%.2f", goodreturntotal));
                                                todaysSalesSummary.put(db.KEY_GOOD_RETURN_TOTAL, String.format("%.2f", goodreturntotal));
                                                todaysSalesSummary.put(db.KEY_ORDER_DISCOUNT, tv_discount.getText().toString());
                                                todaysSalesSummary.put(db.KEY_ORDER_ID, tokens[0].toString());
                                                db.addData(db.TODAYS_SUMMARY_SALES, todaysSalesSummary);

                                                updateStockinVan(true);
                                            } else {
                                                orderID_GlobalBR = "";
                                                HashMap<String, String> todaysSalesSummary = new HashMap<>();
                                                todaysSalesSummary.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                                todaysSalesSummary.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                todaysSalesSummary.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                                                todaysSalesSummary.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                                                todaysSalesSummary.put(db.KEY_ORDER_TOTAL, String.format("%.2f", badreturntotal));
                                                todaysSalesSummary.put(db.KEY_ORDER_NET_TOTAL, String.format("%.2f", badreturntotal));
                                                todaysSalesSummary.put(db.KEY_RETURN_TOTAL, String.format("%.2f", badreturntotal));
                                                todaysSalesSummary.put(db.KEY_GOOD_RETURN_TOTAL, "0");
                                                todaysSalesSummary.put(db.KEY_ORDER_DISCOUNT, tv_discount.getText().toString());
                                                todaysSalesSummary.put(db.KEY_ORDER_ID, tokens[0].toString());
                                                db.addData(db.TODAYS_SUMMARY_SALES, todaysSalesSummary);
                                            }
                                            dialog.dismiss();
                                            if (object.getPaymentMethod().equalsIgnoreCase(App.CREDIT_CUSTOMER) || object.getPaymentMethod().equals(App.TC_CUSTOMER)) {

                                                HashMap<String, String> invoiceMap = new HashMap<>();
                                                invoiceMap.put(db.KEY_COLLECTION_TYPE, App.COLLECTION_INVOICE);
                                                invoiceMap.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                                                invoiceMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                invoiceMap.put(db.KEY_INVOICE_NO, tokens[0].toString());
                                                invoiceMap.put(db.KEY_INVOICE_AMOUNT, tv_net_invoiceVat.getText().toString());
                                                invoiceMap.put(db.KEY_INVOICE_DATE, Helpers.formatDate(new Date(), App.DATE_FORMAT));
                                                invoiceMap.put(db.KEY_DUE_DATE, Helpers.addInvoiceDayToDate(App.DATE_FORMAT_HYPHEN, Const.creditLimitDays));
                                                invoiceMap.put(db.KEY_AMOUNT_CLEARED, "0");
                                                invoiceMap.put(db.KEY_CHEQUE_AMOUNT, "0");
                                                invoiceMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL, "0");
                                                invoiceMap.put(db.KEY_CHEQUE_NUMBER, "0000");
                                                invoiceMap.put(db.KEY_CHEQUE_DATE, "0000");
                                                invoiceMap.put(db.KEY_CHEQUE_BANK_CODE, "0000");
                                                invoiceMap.put(db.KEY_CHEQUE_BANK_NAME, "0000");
                                                invoiceMap.put(db.KEY_CASH_AMOUNT, "0");
                                                invoiceMap.put(db.KEY_INDICATOR, Double.parseDouble(tv_net_invoiceVat.getText().toString()) > 0 ? App.ADD_INDICATOR : App.SUB_INDICATOR);
                                                invoiceMap.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_INCOMPLETE);
                                                invoiceMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                                                invoiceMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                                                db.addData(db.COLLECTION, invoiceMap);

                                                HashMap<String, String> logMap = new HashMap<>();
                                                logMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                                logMap.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                                                logMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                logMap.put(db.KEY_ORDER_ID, orderID_Global);
                                                logMap.put(db.KEY_PRICE, tv_net_invoiceVat.getText().toString());
                                                db.addData(db.DAYACTIVITY, logMap);

                                            /*Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent1.putExtra("headerObj", object);
                                            intent1.putExtra("msg", "all");
                                            startActivity(intent1);
                                            finish();*/

                                            } else if (object.getPaymentMethod().equalsIgnoreCase(App.CASH_CUSTOMER)) {
                                                //Go to payment details screen

                                                HashMap<String, String> invoiceMap = new HashMap<>();
                                                invoiceMap.put(db.KEY_COLLECTION_TYPE, App.COLLECTION_INVOICE_CASH);
                                                invoiceMap.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                                                invoiceMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                invoiceMap.put(db.KEY_INVOICE_NO, tokens[0].toString());
                                                invoiceMap.put(db.KEY_DUE_DATE, Helpers.addInvoiceDayToDate(App.DATE_FORMAT_HYPHEN, Const.creditLimitDays));
                                                invoiceMap.put(db.KEY_INVOICE_AMOUNT, tv_net_invoiceVat.getText().toString());
                                                invoiceMap.put(db.KEY_INDICATOR, Double.parseDouble(tv_net_invoiceVat.getText().toString()) > 0 ? App.ADD_INDICATOR : App.SUB_INDICATOR);
                                                invoiceMap.put(db.KEY_INVOICE_DATE, Helpers.formatDate(new Date(), App.DATE_FORMAT));
                                                invoiceMap.put(db.KEY_AMOUNT_CLEARED, "0");
                                                invoiceMap.put(db.KEY_CHEQUE_AMOUNT, "0");
                                                invoiceMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL, "0");
                                                invoiceMap.put(db.KEY_CHEQUE_NUMBER, "0000");
                                                invoiceMap.put(db.KEY_CHEQUE_DATE, "0000");
                                                invoiceMap.put(db.KEY_CHEQUE_BANK_CODE, "0000");
                                                invoiceMap.put(db.KEY_CHEQUE_BANK_NAME, "0000");
                                                invoiceMap.put(db.KEY_CASH_AMOUNT, "0");
                                                invoiceMap.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_INCOMPLETE);
                                                invoiceMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                                                invoiceMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                                                db.addData(db.COLLECTION, invoiceMap);
                                                Log.e("Going on", "Foo" + tokens[0].toString());
                                                HashMap<String, String> logMap = new HashMap<>();
                                                logMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                                logMap.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                                                logMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                logMap.put(db.KEY_ORDER_ID, orderID_Global);
                                                logMap.put(db.KEY_PRICE, tv_net_invoiceVat.getText().toString());
                                                db.addData(db.DAYACTIVITY, logMap);

                                            /*Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                                            intent.putExtra("msg", str_promotion_message);
                                            intent.putExtra("from", from);
                                            intent.putExtra("headerObj", object);
                                            intent.putExtra("invoiceno", tokens[0].toString());
                                            intent.putExtra("amountdue", tv_net_invoice.getText().toString());
                                            startActivity(intent);
                                            finish();*/
                                            }
                                            if (isPrint) {
                                                try {
                                                    JSONArray jsonArray = createPrintGrBrData(returnType, "", tokens[0].toString());
                                                    JSONObject data = new JSONObject();
                                                    data.put("data", (JSONArray) jsonArray);

                                                    HashMap<String, String> map = new HashMap<>();
                                                    map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                    map.put(db.KEY_ORDER_ID, orderID_Global);
                                                    map.put(db.KEY_DOC_TYPE, returnType.equals(App.GOOD_RETURN) ? ConfigStore.GoodReturns_TR : ConfigStore.BadReturns_TR);
                                                    map.put(db.KEY_DATA, data.toString());
                                                    //map.put(db.KEY_DATA,jsonArray.toString());
                                                    db.addDataPrint(db.DELAY_PRINT, map);


                                                    Log.v("JsonArray", jsonArray.toString() + "-");

                                                    PrinterHelper object = new PrinterHelper(PromotioninfoActivity.this, PromotioninfoActivity.this);
                                                    object.execute("", jsonArray);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Crashlytics.logException(e);
                                                }
                                            } else {
                                                try {
                                                    JSONArray jsonArray = createPrintGrBrData(returnType, "", tokens[0].toString());
                                                    JSONObject data = new JSONObject();
                                                    data.put("data", (JSONArray) jsonArray);

                                                    HashMap<String, String> map = new HashMap<>();
                                                    map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                    map.put(db.KEY_ORDER_ID, orderID_Global);
                                                    map.put(db.KEY_DOC_TYPE, returnType.equals(App.GOOD_RETURN) ? ConfigStore.GoodReturns_TR : ConfigStore.BadReturns_TR);
                                                    map.put(db.KEY_DATA, data.toString());
                                                    //map.put(db.KEY_DATA,jsonArray.toString());
                                                    db.addDataPrint(db.DELAY_PRINT, map);

                                                    Log.v("JsonArray", jsonArray.toString() + "-");

                                                    if (object.getPaymentMethod().equalsIgnoreCase(App.CASH_CUSTOMER)) {
                                                        Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                                                        intent.putExtra("msg", str_promotion_message);
                                                        intent.putExtra("from", from);
                                                        intent.putExtra("headerObj", object);
                                                        intent.putExtra("invoiceno", orderID_Global);
                                                        if (Const.isDisplayVatTax) {
                                                            intent.putExtra("amountdue", tv_net_invoiceVat.getText().toString());
                                                        } else {
                                                            intent.putExtra("amountdue", tv_net_invoice.getText().toString());
                                                        }
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                                                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        intent1.putExtra("headerObj", object);
                                                        intent1.putExtra("msg", "all");
                                                        startActivity(intent1);
                                                        finish();
                                                    }

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Crashlytics.logException(e);
                                                }
                                            }

                                        }
                                    });
                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            // show it
                            alertDialog.show();
                        }
                    } else {
                        if (returnType.equals(App.GOOD_RETURN)) {
                            updateStockinVan(true);
                        }
                    }
                } else {
                    for (Sales sale : arraylist) {
                        HashMap<String, String> postmap = new HashMap<String, String>();
                        postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        postmap.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                        postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                        HashMap<String, String> filtermap = new HashMap<>();
                        filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                        filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filtermap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                        filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                        filtermap.put(db.KEY_REASON_TYPE, this.returnType);
                        db.updateData(db.RETURNS, postmap, filtermap);
                    }
                    Log.e("Return Count", "" + returnCount + "/" + referenceCount);
                    if (returnCount == referenceCount) {
                        if (invoiceExist()) {
                            if (loadingSpinner.isShowing()) {
                                loadingSpinner.hide();
                            }
                            if (returnType.equals(App.GOOD_RETURN)) {
                                updateStockinVan(true);
                            }
                            new postData().execute();
                        } else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PromotioninfoActivity.this);
                            alertDialogBuilder.setTitle("Message")
                                    .setMessage("Request " + tokens[1].toString() + " has been created")
                                    // .setMessage("Request " + tokens[0].toString() + " has been created")
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (returnType.equals(App.GOOD_RETURN)) {
                                                updateStockinVan(true);
                                            }
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
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    if (this.orderID.isEmpty() || this.orderID.equals("") || this.orderID == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();
                    } else if (this.orderID.contains("Error")) {
                        Toast.makeText(getApplicationContext(), this.orderID.replaceAll("Error", "").trim(), Toast.LENGTH_SHORT).show();
                    } else {
                        // show it
                        //  alertDialog.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public String postReturns(String returnType) {
        String orderID = "";
        String purchaseNumber = "";
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("Function", ConfigStore.ReturnsFunction);
            map.put("OrderId", "");
            map.put("DocumentType", returnType.equals(App.GOOD_RETURN) ? ConfigStore.GoodReturnType : ConfigStore.BadReturnType);
            // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
            // map.put("DocumentDate", null);
            map.put("CustomerId", object.getCustomerID());
            map.put("SalesOrg", Settings.getString(App.SALES_ORG));
            map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
            map.put("Division", Settings.getString(App.DIVISION));
            map.put("OrderValue", String.format("%.2f", totalamnt));
            map.put("Currency", App.CURRENCY);
            //map.put("PurchaseNum", returnType.equals(App.GOOD_RETURN) ? Helpers.generateNumber(db, ConfigStore.GoodReturns_PR_Type) : Helpers.generateNumber(db, ConfigStore.BadReturns_PR_Type));
            JSONArray deepEntity = new JSONArray();
            HashMap<String, String> itemMap = new HashMap<>();
            itemMap.put(db.KEY_ITEM_NO, "");
            itemMap.put(db.KEY_MATERIAL_NO, "");
            itemMap.put(db.KEY_MATERIAL_DESC1, "");
            itemMap.put(db.KEY_CASE, "");
            itemMap.put(db.KEY_UNIT, "");
            itemMap.put(db.KEY_UOM, "");
            itemMap.put(db.KEY_PRICE, "");
            itemMap.put(db.KEY_ORDER_ID, "");
            itemMap.put(db.KEY_REASON_CODE, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            filter.put(db.KEY_REASON_TYPE, returnType);
            Cursor cursor = db.getData(db.RETURNS, itemMap, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                map.put("PurchaseNum", cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                map.put("OrdReason", cursor.getString(cursor.getColumnIndex(db.KEY_REASON_CODE)));
                purchaseNumber = map.get("PurchaseNum");
                int itemno = 10;
                do {
                    if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", App.PLANT);
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("Storagelocation", App.STORAGE_LOCATION);
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    } else {
                        JSONObject jo = new JSONObject();
                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", App.PLANT);
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_PRICE)));
                        jo.put("Storagelocation", App.STORAGE_LOCATION);
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    }
                }
                while (cursor.moveToNext());
            }
            orderID = IntegrationServiceJSON.postData(PromotioninfoActivity.this, App.POST_COLLECTION, map, deepEntity);
            //orderID = IntegrationService.postDataBackup(PromotioninfoActivity.this, App.POST_COLLECTION, map, deepEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderID + "," + purchaseNumber;
    }

    public class postData extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> returnList;
        private String orderID = "";
        private String[] tokens = new String[2];

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //this.returnList = IntegrationService.RequestToken(LoadRequestActivity.this);
            this.orderID = postData();
            this.tokens = orderID.split(",");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                Log.e("Order ID Post Data", "" + this.orderID);
                Helpers.logData(PromotioninfoActivity.this, "Order ID Post Data" + this.orderID);
                if (this.orderID.trim().equals(",")) {
                    Log.e("OMG", "OMG");
                    Helpers.logData(PromotioninfoActivity.this, "OMG");
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    /*Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent1.putExtra("headerObj", object);
                    intent1.putExtra("msg", "all");
                    startActivity(intent1);
                    finish();*/

                    if (isPrint) {
                        try {
                            JSONArray jsonArray = createPrintData("SALES INVOICE", "", tempOrderId);
                            JSONObject data = new JSONObject();
                            data.put("data", (JSONArray) jsonArray);

                            Log.v("JsonArray", jsonArray.toString() + "-");

                            PrinterHelper object = new PrinterHelper(PromotioninfoActivity.this, PromotioninfoActivity.this);
                            object.execute("", jsonArray);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    } else {
                        if (object.getPaymentMethod().equalsIgnoreCase(App.CREDIT_CUSTOMER)) {
                            Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.putExtra("headerObj", object);
                            intent1.putExtra("msg", "all");
                            startActivity(intent1);
                            finish();
                        } else if (object.getPaymentMethod().equalsIgnoreCase(App.CASH_CUSTOMER)) {
                            //Go to payment details screen
                            Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                            intent.putExtra("msg", str_promotion_message);
                            intent.putExtra("from", from);
                            intent.putExtra("invoiceno", tempOrderId);
                            intent.putExtra("headerObj", object);
                            if (Const.isDisplayVatTax) {
                                intent.putExtra("amountdue", tv_net_invoiceVat.getText().toString());
                            } else {
                                intent.putExtra("amountdue", tv_net_invoice.getText().toString());
                            }
                            startActivity(intent);
                            finish();
                        } else if (object.getPaymentMethod().equalsIgnoreCase(App.TC_CUSTOMER)) {
                            Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.putExtra("headerObj", object);
                            intent1.putExtra("msg", "all");
                            startActivity(intent1);
                            finish();
                        }
                    }

                } else {
                    tempOrderId = tokens[0].toString();
                    orderID_Global = tokens[0].toString();
                    arraylist.clear();
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_CUSTOMER_NO, "");
                    map.put(db.KEY_ITEM_NO, "");
                    map.put(db.KEY_ITEM_CATEGORY, "");
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_MATERIAL_GROUP, "");
                    map.put(db.KEY_MATERIAL_DESC1, "");
                    map.put(db.KEY_ORG_CASE, "");
                    map.put(db.KEY_UOM, "");
                    map.put(db.KEY_ORG_UNITS, "");
                    map.put(db.KEY_AMOUNT, "");
                    map.put(db.KEY_AMOUNTPCS, "");
                    map.put(db.KEY_AMOUNTCASE, "");
                    map.put(db.KEY_ORDER_ID, "");
                    map.put(db.KEY_PURCHASE_NUMBER, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    Cursor cursor = db.getData(db.CAPTURE_SALES_INVOICE, map, filter);
//                    Cursor foccursor = db.getData(db.FOC_INVOICE,map,filter);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            Sales sale = new Sales();
                            sale.setMaterial_no(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            sale.setMaterial_description(UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                            sale.setPic(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                            sale.setCases(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                            sale.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                            float tempPrice = 0;
                            HashMap<String, String> filterComp = new HashMap<>();
                            filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filterComp.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            HashMap<String, String> priceMap = new HashMap<>();
                            priceMap.put(db.KEY_MATERIAL_NO, "");
                            priceMap.put(db.KEY_AMOUNT, "");
                            if (db.checkData(db.PRICING, filterComp)) {
                                Cursor customerPriceCursor = db.getData(db.PRICING, priceMap, filterComp);
                                if (customerPriceCursor.getCount() > 0) {
                                    customerPriceCursor.moveToFirst();
                                    tempPrice = Float.parseFloat(customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT)));
                                }
                            } else {
                                if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    tempPrice = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                                    //amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                                } else {
                                    tempPrice = 0;
                                }
                            }
                            sale.setPrice(String.format("%.2f", tempPrice));
                            sale.setPricepcs(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTPCS)));
                            sale.setPricecase(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNTCASE)));

                            arraylist.add(sale);
                        }
                        while (cursor.moveToNext());
                    }
                    /*if(foccursor.getCount()>0){
                        foccursor.moveToFirst();
                        do {
                            Sales sale = new Sales();
                            sale.setMaterial_no(foccursor.getString(foccursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            sale.setMaterial_description(UrlBuilder.decodeString(foccursor.getString(foccursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                            sale.setPic(foccursor.getString(foccursor.getColumnIndex(db.KEY_ORG_UNITS)));
                            sale.setCases(foccursor.getString(foccursor.getColumnIndex(db.KEY_ORG_CASE)));
                            sale.setUom(foccursor.getString(foccursor.getColumnIndex(db.KEY_UOM)));
                            float tempPrice = 0;
                            HashMap<String, String> filterComp = new HashMap<>();
                            filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filterComp.put(db.KEY_MATERIAL_NO, foccursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            sale.setPrice(String.format( "%.2f",tempPrice));
                            focList.add(sale);
                        }
                        while (foccursor.moveToNext());
                    }*/
                    if (this.tokens[0].toString().equals(this.tokens[1].toString())) {
                        for (Sales sale : arraylist) {
                            HashMap<String, String> postmap = new HashMap<String, String>();
                            postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            postmap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                            postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                            HashMap<String, String> filtermap = new HashMap<>();
                            filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            // filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                            filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filtermap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                            db.updateData(db.CAPTURE_SALES_INVOICE, postmap, filtermap);

                        }
                        for (Sales sale : focList) {
                            HashMap<String, String> postmap = new HashMap<String, String>();
                            postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            postmap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                            postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                            HashMap<String, String> filtermap = new HashMap<>();
                            filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            // filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                            filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filtermap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                            //Update foc table on Generation of Invoice
                            db.updateData(db.FOC_INVOICE, postmap, filtermap);
                        }
                        //Creating an invoice for customer
                        if (object.getPaymentMethod().equals(App.CREDIT_CUSTOMER) || object.getPaymentMethod().equals(App.TC_CUSTOMER)) {
                            HashMap<String, String> invoiceMap = new HashMap<>();
                            invoiceMap.put(db.KEY_COLLECTION_TYPE, App.COLLECTION_INVOICE);
                            invoiceMap.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                            invoiceMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            invoiceMap.put(db.KEY_INVOICE_NO, tokens[0].toString());
                            invoiceMap.put(db.KEY_INVOICE_AMOUNT, tv_net_invoiceVat.getText().toString());
                            invoiceMap.put(db.KEY_INVOICE_DATE, Helpers.formatDate(new Date(), App.DATE_FORMAT));
                            invoiceMap.put(db.KEY_DUE_DATE, Helpers.addInvoiceDayToDate(App.DATE_FORMAT_HYPHEN, Const.creditLimitDays));
                            invoiceMap.put(db.KEY_AMOUNT_CLEARED, "0");
                            invoiceMap.put(db.KEY_CHEQUE_AMOUNT, "0");
                            invoiceMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL, "0");
                            invoiceMap.put(db.KEY_CHEQUE_NUMBER, "0000");
                            invoiceMap.put(db.KEY_CHEQUE_DATE, "0000");
                            invoiceMap.put(db.KEY_CHEQUE_BANK_CODE, "0000");
                            invoiceMap.put(db.KEY_CHEQUE_BANK_NAME, "0000");
                            invoiceMap.put(db.KEY_CASH_AMOUNT, "0");
                            invoiceMap.put(db.KEY_INDICATOR, Double.parseDouble(tv_net_invoiceVat.getText().toString()) > 0 ? App.ADD_INDICATOR : App.SUB_INDICATOR);
                            invoiceMap.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_INCOMPLETE);
                            invoiceMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            invoiceMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                            db.addData(db.COLLECTION, invoiceMap);
                            Log.e("Going on", "Foo");
                            HashMap<String, String> logMap = new HashMap<>();
                            logMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            logMap.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                            logMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            logMap.put(db.KEY_ORDER_ID, tokens[0].toString());
                            logMap.put(db.KEY_PRICE, tv_net_invoiceVat.getText().toString());
                            db.addData(db.DAYACTIVITY, logMap);
                        } else {
                            HashMap<String, String> invoiceMap = new HashMap<>();
                            invoiceMap.put(db.KEY_COLLECTION_TYPE, App.COLLECTION_INVOICE_CASH);
                            invoiceMap.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                            invoiceMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            invoiceMap.put(db.KEY_INVOICE_NO, tokens[0].toString());
                            invoiceMap.put(db.KEY_DUE_DATE, Helpers.addInvoiceDayToDate(App.DATE_FORMAT_HYPHEN, Const.creditLimitDays));
                            invoiceMap.put(db.KEY_INVOICE_AMOUNT, tv_net_invoiceVat.getText().toString());
                            invoiceMap.put(db.KEY_INDICATOR, Double.parseDouble(tv_net_invoiceVat.getText().toString()) > 0 ? App.ADD_INDICATOR : App.SUB_INDICATOR);
                            invoiceMap.put(db.KEY_INVOICE_DATE, Helpers.formatDate(new Date(), App.DATE_FORMAT));
                            invoiceMap.put(db.KEY_AMOUNT_CLEARED, "0");
                            invoiceMap.put(db.KEY_CHEQUE_AMOUNT, "0");
                            invoiceMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL, "0");
                            invoiceMap.put(db.KEY_CHEQUE_NUMBER, "0000");
                            invoiceMap.put(db.KEY_CHEQUE_DATE, "0000");
                            invoiceMap.put(db.KEY_CHEQUE_BANK_CODE, "0000");
                            invoiceMap.put(db.KEY_CHEQUE_BANK_NAME, "0000");
                            invoiceMap.put(db.KEY_CASH_AMOUNT, "0");
                            invoiceMap.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_INCOMPLETE);
                            invoiceMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            invoiceMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                            db.addData(db.COLLECTION, invoiceMap);
                            HashMap<String, String> logMap = new HashMap<>();
                            logMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            logMap.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                            logMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            logMap.put(db.KEY_ORDER_ID, tokens[0].toString());
                            logMap.put(db.KEY_PRICE, tv_net_invoiceVat.getText().toString());
                            db.addData(db.DAYACTIVITY, logMap);
                        }
                        //Logging sales for todays Summary
                        HashMap<String, String> todaysSummary = new HashMap<>();
                        todaysSummary.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        todaysSummary.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        todaysSummary.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                        todaysSummary.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                        todaysSummary.put(db.KEY_ORDER_TOTAL, tv_net_invoiceVat.getText().toString());
                        todaysSummary.put(db.KEY_ORDER_DISCOUNT, tv_discount.getText().toString());
                        todaysSummary.put(db.KEY_ORDER_ID, tokens[0].toString());
                        db.addData(db.TODAYS_SUMMARY, todaysSummary);

                        HashMap<String, String> todaysSalesSummary = new HashMap<>();
                        todaysSalesSummary.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        todaysSalesSummary.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        todaysSalesSummary.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                        todaysSalesSummary.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                        todaysSalesSummary.put(db.KEY_ORDER_TOTAL, tv_net_invoiceVat.getText().toString());
                        todaysSalesSummary.put(db.KEY_ORDER_NET_TOTAL, tv_current_invoice.getText().toString());
                        todaysSalesSummary.put(db.KEY_RETURN_TOTAL, String.format("%.2f", goodreturntotal + badreturntotal));
                        todaysSalesSummary.put(db.KEY_GOOD_RETURN_TOTAL, String.format("%.2f", goodreturntotal));
                        todaysSalesSummary.put(db.KEY_ORDER_DISCOUNT, tv_discount.getText().toString());
                        todaysSalesSummary.put(db.KEY_ORDER_ID, tokens[0].toString());
                        db.addData(db.TODAYS_SUMMARY_SALES, todaysSalesSummary);

                        if (loadingSpinner.isShowing()) {
                            loadingSpinner.hide();
                        }
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PromotioninfoActivity.this);
                        alertDialogBuilder/*.setTitle("Message")*/
                                .setMessage(getString(R.string.request_created))
                                //.setMessage("Request with reference " + tokens[0].toString() + " has been saved")
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        updateStockinVan(false);
                                        if (Helpers.isNetworkAvailable(getApplicationContext())) {
                                            Helpers.createBackgroundJob(getApplicationContext());
                                        }
                                        dialog.dismiss();
                                        orderID = tokens[0].toString();
                                        if (isPrint) {
                                            try {
                                                JSONArray jsonArray = createPrintData("SALES INVOICE", "", tokens[0].toString());
                                                JSONObject data = new JSONObject();
                                                data.put("data", (JSONArray) jsonArray);


                                                Log.v("JsonArray", jsonArray.toString() + "-");

                                                HashMap<String, String> map = new HashMap<>();
                                                map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                map.put(db.KEY_ORDER_ID, tokens[0].toString());
                                                map.put(db.KEY_DOC_TYPE, ConfigStore.SalesInvoice_TR);
                                                map.put(db.KEY_DATA, data.toString());
                                                //map.put(db.KEY_DATA,jsonArray.toString());
                                                db.addDataPrint(db.DELAY_PRINT, map);

                                                PrinterHelper object = new PrinterHelper(PromotioninfoActivity.this, PromotioninfoActivity.this);
                                                object.execute("", jsonArray);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            try {
                                                JSONArray jsonArray = createPrintData("SALES INVOICE", "", tokens[0].toString());
                                                JSONObject data = new JSONObject();
                                                data.put("data", (JSONArray) jsonArray);


                                                Log.v("JsonArray", jsonArray.toString() + "-");

                                                HashMap<String, String> map = new HashMap<>();
                                                map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                                map.put(db.KEY_ORDER_ID, tokens[0].toString());
                                                map.put(db.KEY_DOC_TYPE, ConfigStore.SalesInvoice_TR);
                                                map.put(db.KEY_DATA, data.toString());
                                                //map.put(db.KEY_DATA,jsonArray.toString());
                                                db.addDataPrint(db.DELAY_PRINT, map);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                            if (object.getPaymentMethod().equalsIgnoreCase(App.CREDIT_CUSTOMER)) {
                                                Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                intent1.putExtra("headerObj", object);
                                                intent1.putExtra("msg", "all");
                                                startActivity(intent1);
                                                finish();
                                            } else if (object.getPaymentMethod().equalsIgnoreCase(App.CASH_CUSTOMER)) {
                                                //Go to payment details screen
                                                Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                                                intent.putExtra("msg", str_promotion_message);
                                                intent.putExtra("from", from);
                                                intent.putExtra("invoiceno", tokens[0].toString());
                                                intent.putExtra("headerObj", object);
                                                if (Const.isDisplayVatTax) {
                                                    intent.putExtra("amountdue", tv_net_invoiceVat.getText().toString());
                                                } else {
                                                    intent.putExtra("amountdue", tv_net_invoice.getText().toString());
                                                }
                                                startActivity(intent);
                                                finish();
                                            } else if (object.getPaymentMethod().equalsIgnoreCase(App.TC_CUSTOMER)) {
                                                Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                intent1.putExtra("headerObj", object);
                                                intent1.putExtra("msg", "all");
                                                startActivity(intent1);
                                                finish();
                                            }
                                        }


                                    }
                                });
                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();
                    } else {
                        for (Sales sale : arraylist) {
                            HashMap<String, String> postmap = new HashMap<String, String>();
                            postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            postmap.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                            postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                            HashMap<String, String> filtermap = new HashMap<>();
                            filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                            filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filtermap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                            db.updateData(db.CAPTURE_SALES_INVOICE, postmap, filtermap);
                        }
                        if (loadingSpinner.isShowing()) {
                            loadingSpinner.hide();
                        }
                        if (this.orderID.isEmpty() || this.orderID.equals("") || this.orderID == null) {
                            Toast.makeText(getApplicationContext(), getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();
                        } else if (this.orderID.contains("Error")) {
                            Toast.makeText(getApplicationContext(), this.orderID.replaceAll("Error", "").trim(), Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PromotioninfoActivity.this);
                            alertDialogBuilder.setTitle("Message")
                                    .setMessage("Request " + tokens[1].toString() + " has been created")
                                    // .setMessage("Request " + tokens[0].toString() + " has been created")
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            updateStockinVan(false);
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

            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }
    }

    public String postData() {
        String orderID = "";
        String purchaseNumber = "";
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("Function", ConfigStore.InvoiceRequestFunction);
            map.put("OrderId", "");
            map.put("DocumentType", ConfigStore.InvoiceDocumentType);
            // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
            // map.put("DocumentDate", null);
            map.put("CustomerId", object.getCustomerID());
            map.put("SalesOrg", Settings.getString(App.SALES_ORG));
            map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
            map.put("Division", Settings.getString(App.DIVISION));
            map.put("OrderValue", String.format("%.2f", totalamnt));
            map.put("Currency", App.CURRENCY);
            // map.put("PurchaseNum", Helpers.generateNumber(db, ConfigStore.InvoiceRequest_PR_Type));
            JSONArray deepEntity = new JSONArray();
            HashMap<String, String> itemMap = new HashMap<>();
            itemMap.put(db.KEY_ITEM_NO, "");
            itemMap.put(db.KEY_MATERIAL_NO, "");
            itemMap.put(db.KEY_MATERIAL_DESC1, "");
            itemMap.put(db.KEY_ORG_CASE, "");
            itemMap.put(db.KEY_ORG_UNITS, "");
            itemMap.put(db.KEY_UOM, "");
            itemMap.put(db.KEY_AMOUNT, "");
            itemMap.put(db.KEY_ORDER_ID, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            Cursor cursor = db.getData(db.CAPTURE_SALES_INVOICE, itemMap, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                map.put("PurchaseNum", cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                purchaseNumber = map.get("PurchaseNum");
                int itemno = 10;
                do {
                    Double amount = 0.0, qty = 0.0;
                    if (Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE))) >= 1) {
                        String deno = "0";
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        altMap.put(db.KEY_DENOMINATOR, "");
                        HashMap<String, String> filtera = new HashMap<>();
                        filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                        }

                        Double casePrice = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT))) * Double.parseDouble(deno);
                        amount += casePrice * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
                        qty = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE))) * Double.parseDouble(deno);
                    }
                    if (Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS))) >= 1) {
                        amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT))) * Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                        qty += Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_ORG_UNITS)));
                    }


//                    if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
//                        JSONObject jo = new JSONObject();
//                        jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
//                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
//                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
//                        jo.put("Plant", App.PLANT);
//                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_ORG_CASE)));
//                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
//                        jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
//                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
//                        jo.put("Storagelocation", App.STORAGE_LOCATION);
//                        jo.put("Route", Settings.getString(App.ROUTE));
//                        itemno = itemno + 10;
//                        deepEntity.put(jo);
//                    } else {
                    JSONObject jo = new JSONObject();
                    jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                    jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                    jo.put("Plant", App.PLANT);
                    jo.put("Quantity", String.format("%.2f", qty));
                    jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                    jo.put("UoM", cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                    jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                    jo.put("Storagelocation", App.STORAGE_LOCATION);
                    jo.put("Route", Settings.getString(App.ROUTE));
                    itemno = itemno + 10;
                    deepEntity.put(jo);
//                    }
                }
                while (cursor.moveToNext());
            }
            orderID = IntegrationService.postData(PromotioninfoActivity.this, App.POST_COLLECTION, map, deepEntity);
            // orderID = IntegrationService.postDataBackup(PromotioninfoActivity.this, App.POST_COLLECTION, map, deepEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderID + "," + purchaseNumber;
    }

    public void updateStockinVan(boolean add) {
        try {
            if (add) {
                loadingSpinner.show();
                //Log.e("ArrayList Size", "" + arraylist.size());
                //for (Sales sale : arraylist) {  //Changes for good stock not getting added to van stock. Comment line
                for (Sales sale : arraylistGR) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_REMAINING_QTY_CASE, "");
                    map.put(db.KEY_REMAINING_QTY_UNIT, "");
                    HashMap<String, String> filter = new HashMap<>();
                    //Log.e("Filter MN", "" + sale.getMaterial_no());
                    filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                    //Log.e("Cursor count", "" + cursor.getCount());
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            HashMap<String, String> updateDataMap = new HashMap<>();
                            float remainingCase = 0;
                            float remainingUnit = 0;
                            remainingCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                            remainingUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                            //Log.e("RemainingCs", "" + remainingCase + sale.getCases());
                            //Log.e("RemainingPc", "" + remainingUnit + sale.getPic());
                            String deno = "0";
                            HashMap<String, String> altMap = new HashMap<>();
                            altMap.put(db.KEY_UOM, "");
                            altMap.put(db.KEY_DENOMINATOR, "");
                            HashMap<String, String> filtera = new HashMap<>();
                            filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                            if (altUOMCursor.getCount() > 0) {
                                altUOMCursor.moveToFirst();
                                deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                            }
                            if (!(sale.getCases().isEmpty() || sale.getCases().equals("") || sale.getCases() == null || sale.getCases().equals("0")) && !(sale.getPic().isEmpty() || sale.getPic().equals("") || sale.getPic() == null || sale.getPic().equals("0"))) {
                                remainingCase = remainingCase + (Float.parseFloat(sale.getCases()) + Float.parseFloat(sale.getPic()) / Float.parseFloat(deno));

                                remainingUnit = remainingUnit + (Float.parseFloat(sale.getPic()) + Float.parseFloat(sale.getCases()) * Float.parseFloat(deno));
                            } else {
                                if (!(sale.getCases().isEmpty() || sale.getCases().equals("") || sale.getCases() == null || sale.getCases().equals("0"))) {
                                    remainingCase = remainingCase + Float.parseFloat(sale.getCases());
                                    remainingUnit = remainingUnit + (Float.parseFloat(sale.getCases()) * Float.parseFloat(deno));
                                }

                                if (!(sale.getPic().isEmpty() || sale.getPic().equals("") || sale.getPic() == null || sale.getPic().equals("0"))) {
                                    remainingUnit = remainingUnit + Float.parseFloat(sale.getPic());
                                    remainingCase = remainingCase + (Float.parseFloat(sale.getPic()) / Float.parseFloat(deno));
                                }
                            }
                            updateDataMap.put(db.KEY_REMAINING_QTY_CASE, String.format("%.2f", remainingCase));
                            updateDataMap.put(db.KEY_REMAINING_QTY_UNIT, String.format("%.2f", remainingUnit));


                            HashMap<String, String> filterInter = new HashMap<>();
                            filterInter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            db.updateData(db.VAN_STOCK_ITEMS, updateDataMap, filterInter);
                        }
                        while (cursor.moveToNext());
                    }
                    //If there is no item present in van stock for that material add it
                    else {
                        HashMap<String, String> addMap = new HashMap<>();
                        addMap.put(db.KEY_ENTRY_TIME, Helpers.getCurrentTimeStamp());
                        addMap.put(db.KEY_DELIVERY_NO, "");
                        addMap.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                        addMap.put(db.KEY_ITEM_NO, sale.getItem_code());
                        addMap.put(db.KEY_MATERIAL_DESC1, sale.getMaterial_description());
                        if (!(sale.getCases().isEmpty() || sale.getCases().equals("") || sale.getCases() == null || sale.getCases().equals("0"))) {
                            addMap.put(db.KEY_ACTUAL_QTY_CASE, sale.getCases());
                            addMap.put(db.KEY_RESERVED_QTY_CASE, "0");
                            addMap.put(db.KEY_REMAINING_QTY_CASE, sale.getCases());
                        } else {
                            addMap.put(db.KEY_ACTUAL_QTY_CASE, "0");
                            addMap.put(db.KEY_RESERVED_QTY_CASE, "0");
                            addMap.put(db.KEY_REMAINING_QTY_CASE, "0");
                        }
                        if (!(sale.getPic().isEmpty() || sale.getPic().equals("") || sale.getPic() == null || sale.getPic().equals("0"))) {
                            addMap.put(db.KEY_ACTUAL_QTY_UNIT, sale.getPic());
                            addMap.put(db.KEY_RESERVED_QTY_UNIT, "0");
                            addMap.put(db.KEY_REMAINING_QTY_UNIT, sale.getPic());
                        } else {
                            addMap.put(db.KEY_ACTUAL_QTY_UNIT, "0");
                            addMap.put(db.KEY_RESERVED_QTY_UNIT, "0");
                            addMap.put(db.KEY_REMAINING_QTY_UNIT, "0");
                        }
                        if (sale.getUom().equals(App.CASE_UOM) || sale.getUom().equals(App.CASE_UOM_NEW) || sale.getUom().equals(App.BOTTLES_UOM)) {
                            addMap.put(db.KEY_UOM_CASE, App.CASE_UOM);
                            addMap.put(db.KEY_UOM_UNIT, App.BOTTLES_UOM);
                        }
                        db.addData(db.VAN_STOCK_ITEMS, addMap);
                    }
                }
                loadingSpinner.hide();
            } else {
                loadingSpinner.show();
                //Log.e("ArrayList Size", "" + arraylist.size());
                for (Sales sale : arraylist) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_REMAINING_QTY_CASE, "");
                    map.put(db.KEY_REMAINING_QTY_UNIT, "");
                    HashMap<String, String> filter = new HashMap<>();
                    // Log.e("Filter MN", "" + sale.getMaterial_no());
                    filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                    // Log.e("Cursor count", "" + cursor.getCount());
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            HashMap<String, String> updateDataMap = new HashMap<>();
                            float remainingCase = 0;
                            float remainingUnit = 0;
                            remainingCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                            remainingUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                            //Log.e("RemainingCs", "" + remainingCase + sale.getCases());
                            //Log.e("RemainingPc", "" + remainingUnit + sale.getPic());
                            String deno = "0";
                            HashMap<String, String> altMap = new HashMap<>();
                            altMap.put(db.KEY_UOM, "");
                            altMap.put(db.KEY_DENOMINATOR, "");
                            HashMap<String, String> filtera = new HashMap<>();
                            filtera.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                            if (altUOMCursor.getCount() > 0) {
                                altUOMCursor.moveToFirst();
                                deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                            }
                            if (!(sale.getCases().isEmpty() || sale.getCases().equals("") || sale.getCases() == null || sale.getCases().equals("0")) && !(sale.getPic().isEmpty() || sale.getPic().equals("") || sale.getPic() == null || sale.getPic().equals("0"))) {
                                remainingCase = remainingCase - (Float.parseFloat(sale.getCases()) + Float.parseFloat(sale.getPic()) / Float.parseFloat(deno));

                                remainingUnit = remainingUnit - (Float.parseFloat(sale.getPic()) + Float.parseFloat(sale.getCases()) * Float.parseFloat(deno));
                            } else {
                                if (!(sale.getCases().isEmpty() || sale.getCases().equals("") || sale.getCases() == null || sale.getCases().equals("0"))) {
                                    remainingCase = remainingCase - Float.parseFloat(sale.getCases());
                                    remainingUnit = remainingUnit - (Float.parseFloat(sale.getCases()) * Float.parseFloat(deno));
                                }

                                if (!(sale.getPic().isEmpty() || sale.getPic().equals("") || sale.getPic() == null || sale.getPic().equals("0"))) {
                                    remainingUnit = remainingUnit - Float.parseFloat(sale.getPic());
                                    remainingCase = remainingCase - (Float.parseFloat(sale.getPic()) / Float.parseFloat(deno));
                                }
                            }
                            updateDataMap.put(db.KEY_REMAINING_QTY_CASE, String.format("%.2f", remainingCase));
                            updateDataMap.put(db.KEY_REMAINING_QTY_UNIT, String.format("%.2f", remainingUnit));
                            HashMap<String, String> filterInter = new HashMap<>();
                            filterInter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            db.updateData(db.VAN_STOCK_ITEMS, updateDataMap, filterInter);
                        }
                        while (cursor.moveToNext());
                    }
                }
                for (Sales sale : focList) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_REMAINING_QTY_CASE, "");
                    map.put(db.KEY_REMAINING_QTY_UNIT, "");
                    HashMap<String, String> filter = new HashMap<>();
                    // Log.e("Filter MN", "" + sale.getMaterial_no());
                    filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                    // Log.e("Cursor count", "" + cursor.getCount());
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            HashMap<String, String> updateDataMap = new HashMap<>();
                            float remainingCase = 0;
                            float remainingUnit = 0;
                            remainingCase = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                            remainingUnit = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT)));
                            //Log.e("RemainingCs", "" + remainingCase + sale.getCases());
                            //Log.e("RemainingPc", "" + remainingUnit + sale.getPic());
                            if (!(sale.getCases().isEmpty() || sale.getCases().equals("") || sale.getCases() == null || sale.getCases().equals("0"))) {
                                remainingCase = remainingCase - Float.parseFloat(sale.getCases());
                            }
                            if (!(sale.getPic().isEmpty() || sale.getPic().equals("") || sale.getPic() == null || sale.getPic().equals("0"))) {
                                remainingUnit = remainingUnit - Float.parseFloat(sale.getPic());
                            }
                            updateDataMap.put(db.KEY_REMAINING_QTY_CASE, String.format("%.2f", remainingCase));
                            updateDataMap.put(db.KEY_REMAINING_QTY_UNIT, String.format("%.2f", remainingUnit));
                            HashMap<String, String> filterInter = new HashMap<>();
                            filterInter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                            db.updateData(db.VAN_STOCK_ITEMS, updateDataMap, filterInter);
                        }
                        while (cursor.moveToNext());
                    }
                }
                loadingSpinner.hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class loadPromotions extends AsyncTask<Void, Void, Void> {
        private String promoCode;
        private String source;

        private loadPromotions(String promoCode, String source) {
            // Log.e("I m in for Load Promotions", "" + source);
            this.promoCode = promoCode;
            this.source = source;
            execute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_MATERIAL_NO, "");
            map.put(db.KEY_AMOUNT, "");
            HashMap<String, String> filter = new HashMap<>();
            HashMap<String, String> filter1 = new HashMap<>();
            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            String blankCust = "";
            filter1.put(db.KEY_CUSTOMER_NO, blankCust);
            if (promoCode.equals(App.Promotions02)) {
                filter.put(db.KEY_PROMOTION_TYPE, App.Promotions02);
                filter1.put(db.KEY_PROMOTION_TYPE, App.Promotions02);
            } else if (promoCode.equals(App.Promotions05)) {
                filter.put(db.KEY_PROMOTION_TYPE, App.Promotions05);
                filter1.put(db.KEY_PROMOTION_TYPE, App.Promotions05);
            } else if (promoCode.equals(App.Promotions07)) {
                filter.put(db.KEY_PROMOTION_TYPE, App.Promotions07);
                filter1.put(db.KEY_PROMOTION_TYPE, App.Promotions07);
            }
            /*Cursor cursor = db.getData(db.PROMOTIONS, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                applyPromotions(cursor, from, source);
            }*/
            if (db.checkData(db.PROMOTIONS, filter)) {
                Cursor cursor = db.getData(db.PROMOTIONS, map, filter);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    applyPromotions(cursor, from, source);
                }

            } else {
                Cursor cursor = db.getData(db.PROMOTIONS, map, filter1);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    applyPromotions(cursor, from, source);
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            count++;
            if (count == 3) {
                count = 0;
                if (loadingSpinner.isShowing()) {
                    loadingSpinner.hide();
                }
                onProcessingComplete(source);
            }
        }
    }

    private void applyPromotions(Cursor cursor, String from, String source) {
        try {
            Cursor promotionCursor = cursor;
            promotionCursor.moveToFirst();
            //Log.e("I m in for Apply Promotions", "" + source);
            if (from.equals("Final Invoice")) {
                if (source.equals("Invoice")) {
                    do {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_ORG_CASE, "");
                        map.put(db.KEY_ORG_UNITS, "");
                        map.put(db.KEY_AMOUNT, "");
                        map.put(db.KEY_UOM, "");
                        map.put(db.KEY_IS_POSTED, "");
                        map.put(db.KEY_IS_PRINTED, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filter.put(db.KEY_MATERIAL_NO, promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        Cursor ivCursor = db.getData(db.CAPTURE_SALES_INVOICE, map, filter);
                        if (ivCursor.getCount() > 0) {
                            ivCursor.moveToFirst();
                            do {
                                if (ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    float cases = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_ORG_CASE)));
                                    discount += cases * (Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT))));
                                }
                        /* else if(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)){
                            float bottles = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                            discount += bottles*Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT)));
                        }*/
                            }
                            while (ivCursor.moveToNext());
                        }
                    }
                    while (promotionCursor.moveToNext());
                } else if (source.equals(App.GOOD_RETURN)) {
                    do {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_PRICE, "");
                        map.put(db.KEY_UOM, "");
                        map.put(db.KEY_IS_POSTED, "");
                        map.put(db.KEY_IS_PRINTED, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filter.put(db.KEY_MATERIAL_NO, promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        filter.put(db.KEY_REASON_TYPE, source);
                        Cursor ivCursor = db.getData(db.RETURNS, map, filter);
                        if (ivCursor.getCount() > 0) {
                            ivCursor.moveToFirst();
                            do {
                                if (ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    float cases = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_CASE)));
                                    goodreturndiscount += cases * (Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT))));
                                }
                        /* else if(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)){
                            float bottles = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                            discount += bottles*Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT)));
                        }*/
                            }
                            while (ivCursor.moveToNext());
                        }
                    }
                    while (promotionCursor.moveToNext());
                } else if (source.equals(App.BAD_RETURN)) {
                    do {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, "");
                        map.put(db.KEY_MATERIAL_NO, "");
                        map.put(db.KEY_CASE, "");
                        map.put(db.KEY_UNIT, "");
                        map.put(db.KEY_PRICE, "");
                        map.put(db.KEY_UOM, "");
                        map.put(db.KEY_IS_POSTED, "");
                        map.put(db.KEY_IS_PRINTED, "");
                        HashMap<String, String> filter = new HashMap<>();
                        filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filter.put(db.KEY_MATERIAL_NO, promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        filter.put(db.KEY_REASON_TYPE, source);
                        Cursor ivCursor = db.getData(db.RETURNS, map, filter);
                        if (ivCursor.getCount() > 0) {
                            ivCursor.moveToFirst();
                            do {
                                if (ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                    float cases = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_CASE)));
                                    badreturndiscount += cases * (Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT))));
                                }
                        /* else if(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)){
                            float bottles = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                            discount += bottles*Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT)));
                        }*/
                            }
                            while (ivCursor.moveToNext());
                        }
                    }
                    while (promotionCursor.moveToNext());
                }
            } else if (from.equals("delivery")) {
                do {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_CUSTOMER_NO, "");
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_CASE, "");
                    map.put(db.KEY_UNIT, "");
                    map.put(db.KEY_AMOUNT, "");
                    map.put(db.KEY_UOM, "");
                    map.put(db.KEY_IS_POSTED, "");
                    map.put(db.KEY_IS_PRINTED, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    filter.put(db.KEY_MATERIAL_NO, promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    filter.put(db.KEY_DELIVERY_NO, delivery.getOrderId());
                    Cursor ivCursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
                    if (ivCursor.getCount() > 0) {
                        ivCursor.moveToFirst();
                        do {
                            if (ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM_NEW) || ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                float cases = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_CASE)));
                                discount += cases * (Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT))));
                            }
                        /*else if(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)){
                            float bottles = Float.parseFloat(ivCursor.getString(ivCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                            discount += bottles*Float.parseFloat(promotionCursor.getString(promotionCursor.getColumnIndex(db.KEY_AMOUNT)));
                        }*/
                        }
                        while (ivCursor.moveToNext());
                    }
                }
                while (promotionCursor.moveToNext());
            }
            Log.e("Discount", "" + discount);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String postDeliveryData() {
        String orderID = "";
        String purchaseNumber = "";
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("Function", ConfigStore.CustomerDeliveryRequestFunction);
            map.put("OrderId", delivery.getOrderId());
            map.put("DocumentType", ConfigStore.DeliveryDocumentType);
            // map.put("DocumentDate", Helpers.formatDate(new Date(),App.DATE_FORMAT_WO_SPACE));
            // map.put("DocumentDate", null);
            map.put("CustomerId", object.getCustomerID());
            map.put("SalesOrg", Settings.getString(App.SALES_ORG));
            map.put("DistChannel", Settings.getString(App.DIST_CHANNEL));
            map.put("Division", Settings.getString(App.DIVISION));
            map.put("OrderValue", invoiceAmount);
            map.put("Currency", App.CURRENCY);
            /*map.put("PurchaseNum", Helpers.generateNumber(db, ConfigStore.CustomerDeliveryRequest_PR_Type));*/
            JSONArray deepEntity = new JSONArray();
            HashMap<String, String> itemMap = new HashMap<>();
            itemMap.put(db.KEY_ITEM_NO, "");
            itemMap.put(db.KEY_MATERIAL_NO, "");
            itemMap.put(db.KEY_MATERIAL_DESC1, "");
            itemMap.put(db.KEY_CASE, "");
            itemMap.put(db.KEY_UNIT, "");
            itemMap.put(db.KEY_AMOUNT, "");
            itemMap.put(db.KEY_ORDER_ID, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_DELIVERY_NO, delivery.getOrderId());
            Cursor cursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, itemMap, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                map.put("PurchaseNum", cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                purchaseNumber = map.get("PurchaseNum");
                int itemno = 10;
                do {
                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    if (articleHeader.getBaseUOM().equals(App.CASE_UOM) || articleHeader.getBaseUOM().equals(App.BOTTLES_UOM)) {
                        JSONObject jo = new JSONObject();
                        //jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Item", cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("UoM", articleHeader.getBaseUOM());
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("Storagelocation", "");
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    } else {
                        JSONObject jo = new JSONObject();
                        //jo.put("Item", Helpers.getMaskedValue(String.valueOf(itemno), 4));
                        jo.put("Item", cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                        jo.put("Material", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        jo.put("Description", cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        jo.put("Plant", "");
                        jo.put("Quantity", cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        jo.put("ItemValue", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("UoM", articleHeader.getBaseUOM());
                        jo.put("Value", cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        jo.put("Storagelocation", "");
                        jo.put("Route", Settings.getString(App.ROUTE));
                        itemno = itemno + 10;
                        deepEntity.put(jo);
                    }
                }
                while (cursor.moveToNext());
            }
            orderID = IntegrationService.postData(PromotioninfoActivity.this, App.POST_COLLECTION, map, deepEntity);
            //orderID = IntegrationService.postDataBackup(PaymentDetails.this, App.POST_COLLECTION, map, deepEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderID + "," + purchaseNumber;
    }

    public class postDeliveryData extends AsyncTask<Void, Void, Void> {
        private ArrayList<String> returnList;
        private String orderID = "";
        private String[] tokens = new String[2];

        @Override
        protected void onPreExecute() {
            loadingSpinner.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //this.returnList = IntegrationService.RequestToken(LoadRequestActivity.this);
            this.orderID = postDeliveryData();
            this.tokens = orderID.split(",");
            data = orderID;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Log.e("Order ID", "" + this.orderID);
            try {
                orderID_Global = tokens[0].toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(db.KEY_TIME_STAMP, "");
                map.put(db.KEY_DELIVERY_NO, "");
                map.put(db.KEY_ITEM_NO, "");
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_MATERIAL_DESC1, "");
                map.put(db.KEY_CASE, "");
                map.put(db.KEY_UNIT, "");
                map.put(db.KEY_UOM, "");
                map.put(db.KEY_AMOUNT, "");
                map.put(db.KEY_ORDER_ID, "");
                map.put(db.KEY_PURCHASE_NUMBER, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                Cursor cursor = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        DeliveryItem deliveryItem = new DeliveryItem();
                        deliveryItem.setItemCode(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                        deliveryItem.setMaterialNo(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        deliveryItem.setItemDescription(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                        deliveryItem.setItemCase(cursor.getString(cursor.getColumnIndex(db.KEY_CASE)));
                        deliveryItem.setItemUnits(cursor.getString(cursor.getColumnIndex(db.KEY_UNIT)));
                        deliveryItem.setItemUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM)));
                        //deliveryItem.setAmount(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));

                        /*Changes to get customer price in printout
                        * Changes by Rakshit on 27/02/2017
                        * */
                        float tempPrice = 0;
                        HashMap<String, String> filterComp = new HashMap<>();
                        filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filterComp.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        HashMap<String, String> priceMap = new HashMap<>();
                        priceMap.put(db.KEY_MATERIAL_NO, "");
                        priceMap.put(db.KEY_AMOUNT, "");
                        if (db.checkData(db.PRICING, filterComp)) {
                            Cursor customerPriceCursor = db.getData(db.PRICING, priceMap, filterComp);
                            if (customerPriceCursor.getCount() > 0) {
                                customerPriceCursor.moveToFirst();
                                tempPrice = Float.parseFloat(customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT)));
                            }
                        } else {
                            if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || cursor.getString(cursor.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                                tempPrice = Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                                //amount += Float.parseFloat(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                            } else {
                                tempPrice = 0;
                            }
                        }
                        //sale.setPrice(String.valueOf(tempPrice));
                        // deliveryItem.setAmount(cursor.getString(cursor.getColumnIndex(db.KEY_AMOUNT)));
                        deliveryItem.setAmount(String.format("%.2f", tempPrice));
                        /*End of changes*/
                        Log.e("New Del Price", "" + deliveryItem.getMaterialNo() + "-" + deliveryItem.getAmount());
                        deliveryArrayList.add(deliveryItem);
                    }
                    while (cursor.moveToNext());

                }
                if (this.tokens[0].toString().equals(this.tokens[1].toString())) {
                    for (DeliveryItem item : deliveryArrayList) {
                        HashMap<String, String> postmap = new HashMap<String, String>();
                        postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        postmap.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
                        postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                        HashMap<String, String> filtermap = new HashMap<>();
                        filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        // filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                        filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filtermap.put(db.KEY_MATERIAL_NO, item.getMaterialNo());
                        filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                        db.updateData(db.CUSTOMER_DELIVERY_ITEMS_POST, postmap, filtermap);
                    }
                    removeReservedQuantity(delivery.getOrderId());  //Removing reserved quantity from van stock
                    if (object.getPaymentMethod().equals(App.CREDIT_CUSTOMER) || object.getPaymentMethod().equals(App.TC_CUSTOMER)) {
                        HashMap<String, String> invoiceMap = new HashMap<>();
                        invoiceMap.put(db.KEY_COLLECTION_TYPE, App.COLLECTION_DELIVERY);
                        invoiceMap.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                        invoiceMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        invoiceMap.put(db.KEY_INVOICE_NO, tokens[0].toString());
                        invoiceMap.put(db.KEY_INVOICE_AMOUNT, tv_net_invoiceVat.getText().toString());
                        invoiceMap.put(db.KEY_INVOICE_DATE, Helpers.formatDate(new Date(), App.DATE_FORMAT));
                        invoiceMap.put(db.KEY_AMOUNT_CLEARED, "0");
                        invoiceMap.put(db.KEY_CHEQUE_AMOUNT, "0");
                        invoiceMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL, "0");
                        invoiceMap.put(db.KEY_CHEQUE_NUMBER, "0000");
                        invoiceMap.put(db.KEY_CHEQUE_DATE, "0000");
                        invoiceMap.put(db.KEY_CHEQUE_BANK_CODE, "0000");
                        invoiceMap.put(db.KEY_CHEQUE_BANK_NAME, "0000");
                        invoiceMap.put(db.KEY_CASH_AMOUNT, "0");
                        invoiceMap.put(db.KEY_INDICATOR, Double.parseDouble(tv_net_invoiceVat.getText().toString()) > 0 ? App.ADD_INDICATOR : App.SUB_INDICATOR);
                        invoiceMap.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_INCOMPLETE);
                        invoiceMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        invoiceMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                        db.addData(db.COLLECTION, invoiceMap);

                        HashMap<String, String> logMap = new HashMap<>();
                        logMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        logMap.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                        logMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        logMap.put(db.KEY_ORDER_ID, orderID_Global);
                        logMap.put(db.KEY_PRICE, tv_net_invoiceVat.getText().toString());
                        db.addData(db.DAYACTIVITY, logMap);
                    } else if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
                        HashMap<String, String> invoiceMap = new HashMap<>();
                        invoiceMap.put(db.KEY_COLLECTION_TYPE, App.COLLECTION_DELIVERY);
                        invoiceMap.put(db.KEY_CUSTOMER_TYPE, object.getPaymentMethod());
                        invoiceMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        invoiceMap.put(db.KEY_INVOICE_NO, tokens[0].toString());
                        invoiceMap.put(db.KEY_INVOICE_AMOUNT, tv_net_invoiceVat.getText().toString());
                        invoiceMap.put(db.KEY_INVOICE_DATE, Helpers.formatDate(new Date(), App.DATE_FORMAT));
                        invoiceMap.put(db.KEY_AMOUNT_CLEARED, "0");
                        invoiceMap.put(db.KEY_CHEQUE_AMOUNT, "0");
                        invoiceMap.put(db.KEY_CHEQUE_AMOUNT_INDIVIDUAL, "0");
                        invoiceMap.put(db.KEY_CHEQUE_NUMBER, "0000");
                        invoiceMap.put(db.KEY_CHEQUE_DATE, "0000");
                        invoiceMap.put(db.KEY_CHEQUE_BANK_CODE, "0000");
                        invoiceMap.put(db.KEY_CHEQUE_BANK_NAME, "0000");
                        invoiceMap.put(db.KEY_CASH_AMOUNT, "0");
                        invoiceMap.put(db.KEY_INDICATOR, Double.parseDouble(tv_net_invoiceVat.getText().toString()) > 0 ? App.ADD_INDICATOR : App.SUB_INDICATOR);
                        invoiceMap.put(db.KEY_IS_INVOICE_COMPLETE, App.INVOICE_INCOMPLETE);
                        invoiceMap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        invoiceMap.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                        db.addData(db.COLLECTION, invoiceMap);

                        HashMap<String, String> logMap = new HashMap<>();
                        logMap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        logMap.put(db.KEY_ACTIVITY_TYPE, App.ACTIVITY_INVOICE);
                        logMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        logMap.put(db.KEY_ORDER_ID, orderID_Global);
                        logMap.put(db.KEY_PRICE, tv_net_invoiceVat.getText().toString());
                        db.addData(db.DAYACTIVITY, logMap);
                    }
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(PromotioninfoActivity.this);
                    alertDialogBuilder/*.setTitle("Message")*/
                            //.setMessage("Request with reference " + tokens[0].toString() + " has been saved")
                            .setMessage(getString(R.string.request_created))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(db.KEY_IS_DELIVERED, "true");
                                    HashMap<String, String> filter = new HashMap<>();
                                    filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                    filter.put(db.KEY_DELIVERY_NO, delivery.getOrderId());
                                    db.updateData(db.CUSTOMER_DELIVERY_HEADER, map, filter);
                                    dialog.dismiss();
                                    setData(tokens);
                                }
                            });
                    // create alert dialog
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                } else {
                    for (DeliveryItem item : deliveryArrayList) {
                        HashMap<String, String> postmap = new HashMap<String, String>();
                        postmap.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                        postmap.put(db.KEY_IS_POSTED, App.DATA_IS_POSTED);
                        postmap.put(db.KEY_ORDER_ID, tokens[0].toString());
                        HashMap<String, String> filtermap = new HashMap<>();
                        filtermap.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                        //  filtermap.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                        filtermap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        filtermap.put(db.KEY_MATERIAL_NO, item.getMaterialNo());
                        filtermap.put(db.KEY_PURCHASE_NUMBER, tokens[1].toString());
                        db.updateData(db.CUSTOMER_DELIVERY_ITEMS_POST, postmap, filtermap);
                    }
                    if (loadingSpinner.isShowing()) {
                        loadingSpinner.hide();
                    }
                    if (this.orderID.isEmpty() || this.orderID.equals("") || this.orderID == null) {
                        // Toast.makeText(getApplicationContext(), getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();
                    } else if (this.orderID.contains("Error")) {
                        Toast.makeText(getApplicationContext(), this.orderID.replaceAll("Error", "").trim(), Toast.LENGTH_SHORT).show();
                    } else {
                        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(PromotioninfoActivity.this);
                        alertDialogBuilder.setTitle("Message")
                                .setMessage("Request " + tokens[1].toString() + " has been created")
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        HashMap<String, String> map = new HashMap<String, String>();
                                        map.put(db.KEY_IS_DELIVERED, "true");
                                        HashMap<String, String> filter = new HashMap<>();
                                        filter.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                        filter.put(db.KEY_DELIVERY_NO, delivery.getOrderId());
                                        db.updateData(db.CUSTOMER_DELIVERY_HEADER, map, filter);
                                        dialog.dismiss();
                                        if (Helpers.isNetworkAvailable(getApplicationContext())) {
                                            Helpers.createBackgroundJob(getApplicationContext());
                                        }
                                        if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
                                            Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                                            intent.putExtra("msg", str_promotion_message);
                                            intent.putExtra("from", from);
                                            intent.putExtra("headerObj", object);
                                            intent.putExtra("delivery", delivery);
                                            intent.putExtra("invoiceamount", tv_current_invoice.getText().toString());
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Intent intent = new Intent(PromotioninfoActivity.this, DeliveryActivity.class);
                                            intent.putExtra("headerObj", object);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                        // create alert dialog
                        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                        // show it
                        alertDialog.show();
                    }
                /*Intent intent = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                intent.putExtra("headerObj", object);
                intent.putExtra("msg","all");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();*/
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void setData(String[] tokens) {
        if (Helpers.isNetworkAvailable(getApplicationContext())) {
            Helpers.createBackgroundJob(getApplicationContext());
        }
        if (isPrint) {
            try {
                /*************************************************
                 * Changes by Rakshit to include Payment Reference
                 * **************************
                 */
                if (Settings.getString(App.DIST_CHANNEL).equals("30")) {
                    JSONArray jsonArray = createPrintDeliveryDataWithPaymentRef("DELIVERY", delivery.getOrderDate(), delivery.getOrderId(), delivery.getOrderReferenceNo());
                    JSONObject data = new JSONObject();
                    data.put("data", (JSONArray) jsonArray);

                    HashMap<String, String> delayMap = new HashMap<>();
                    delayMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    delayMap.put(db.KEY_ORDER_ID, tokens[0].toString());
                    delayMap.put(db.KEY_DOC_TYPE, ConfigStore.DeliveryRequest_TR);
                    delayMap.put(db.KEY_DATA, data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT, delayMap);

                    PrinterHelper object = new PrinterHelper(PromotioninfoActivity.this, PromotioninfoActivity.this);
                    object.execute("", jsonArray);
                } else {
                    JSONArray jsonArray = createPrintDeliveryData("DELIVERY", delivery.getOrderDate(), delivery.getOrderId());
                    JSONObject data = new JSONObject();
                    data.put("data", (JSONArray) jsonArray);

                    HashMap<String, String> delayMap = new HashMap<>();
                    delayMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    delayMap.put(db.KEY_ORDER_ID, tokens[0].toString());
                    delayMap.put(db.KEY_DOC_TYPE, ConfigStore.DeliveryRequest_TR);
                    delayMap.put(db.KEY_DATA, data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT, delayMap);

                    PrinterHelper object = new PrinterHelper(PromotioninfoActivity.this, PromotioninfoActivity.this);
                    object.execute("", jsonArray);
                }

                /*****************************************
                 ********************End of Changes********
                 *****************************************/

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            try {

                /*************************************************
                 * Changes by Rakshit to include Payment Reference
                 * **************************
                 */
                if (Settings.getString(App.DIST_CHANNEL).equals("30")) {
                    JSONArray jsonArray = createPrintDeliveryDataWithPaymentRef("DELIVERY", delivery.getOrderDate(), delivery.getOrderId(), delivery.getOrderReferenceNo());
                    JSONObject data = new JSONObject();
                    data.put("data", (JSONArray) jsonArray);

                    HashMap<String, String> delayMap = new HashMap<>();
                    delayMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    delayMap.put(db.KEY_ORDER_ID, tokens[0].toString());
                    delayMap.put(db.KEY_DOC_TYPE, ConfigStore.DeliveryRequest_TR);
                    delayMap.put(db.KEY_DATA, data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT, delayMap);

//                    if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
                    if (delivery.getOrderReferenceNo().isEmpty()) {
                        Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                        intent.putExtra("msg", str_promotion_message);
                        intent.putExtra("from", from);
                        intent.putExtra("headerObj", object);
                        intent.putExtra("delivery", delivery);
                        intent.putExtra("invoiceno", tokens[0].toString());
                        intent.putExtra("invoiceamount", tv_current_invoice.getText().toString());
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(PromotioninfoActivity.this, DeliveryActivity.class);
                        intent.putExtra("headerObj", object);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    /*} else {
                        Intent intent = new Intent(PromotioninfoActivity.this, DeliveryActivity.class);
                        intent.putExtra("headerObj", object);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }*/
                } else {
                    JSONArray jsonArray = createPrintDeliveryData("DELIVERY", delivery.getOrderDate(), delivery.getOrderId());
                    JSONObject data = new JSONObject();
                    data.put("data", (JSONArray) jsonArray);

                    HashMap<String, String> delayMap = new HashMap<>();
                    delayMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    delayMap.put(db.KEY_ORDER_ID, tokens[0].toString());
                    delayMap.put(db.KEY_DOC_TYPE, ConfigStore.DeliveryRequest_TR);
                    delayMap.put(db.KEY_DATA, data.toString());
                    //map.put(db.KEY_DATA,jsonArray.toString());
                    db.addDataPrint(db.DELAY_PRINT, delayMap);

                    if (delivery.getOrderReferenceNo().isEmpty()) {
                        Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                        intent.putExtra("msg", str_promotion_message);
                        intent.putExtra("from", from);
                        intent.putExtra("headerObj", object);
                        intent.putExtra("delivery", delivery);
                        intent.putExtra("invoiceno", tokens[0].toString());
                        intent.putExtra("invoiceamount", tv_current_invoice.getText().toString());
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(PromotioninfoActivity.this, DeliveryActivity.class);
                        intent.putExtra("headerObj", object);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }

                /*************************************************
                 * End of Changes
                 * **************************
                 */


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void removeReservedQuantity(String deliveryNo) {
        //ArrayList<DeliveryItem>deliveryList = new ArrayList<>();
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_TIME_STAMP, "");
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_DELIVERY_NO, "");
            map.put(db.KEY_ITEM_NO, "");
            map.put(db.KEY_MATERIAL_NO, "");
            map.put(db.KEY_MATERIAL_DESC1, "");
            map.put(db.KEY_CASE, "");
            map.put(db.KEY_UNIT, "");
            map.put(db.KEY_UOM, "");
            map.put(db.KEY_ORDER_ID, "");
            map.put(db.KEY_PURCHASE_NUMBER, "");
            map.put(db.KEY_AMOUNT, "");
            map.put(db.KEY_IS_POSTED, "");
            map.put(db.KEY_IS_PRINTED, "");
            HashMap<String, String> filter = new HashMap<>();
            filter.put(db.KEY_DELIVERY_NO, delivery.getOrderId());
            filter.put(db.KEY_IS_POSTED, App.DATA_MARKED_FOR_POST);
            Cursor c = db.getData(db.CUSTOMER_DELIVERY_ITEMS_POST, map, filter);
            if (c.getCount() > 0) {
                c.moveToFirst();
                do {
                    HashMap<String, String> vanStock = new HashMap<>();
                    vanStock.put(db.KEY_MATERIAL_NO, "");
                    vanStock.put(db.KEY_ACTUAL_QTY_CASE, "");
                    vanStock.put(db.KEY_ACTUAL_QTY_UNIT, "");
                    vanStock.put(db.KEY_RESERVED_QTY_CASE, "");
                    vanStock.put(db.KEY_RESERVED_QTY_UNIT, "");
                    vanStock.put(db.KEY_REMAINING_QTY_CASE, "");
                    vanStock.put(db.KEY_REMAINING_QTY_UNIT, "");
                    HashMap<String, String> vanStockFilter = new HashMap<>();
                    vanStockFilter.put(db.KEY_MATERIAL_NO, c.getString(c.getColumnIndex(db.KEY_MATERIAL_NO)));
                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, vanStock, vanStockFilter);
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        double reservedCases = 0;
                        double reservedUnits = 0;
                        if (c.getString(c.getColumnIndex(db.KEY_UOM)).equals(App.CASE_UOM) || c.getString(c.getColumnIndex(db.KEY_UOM)).equals(App.BOTTLES_UOM)) {
                            reservedCases = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_CASE))) - Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_CASE)));
                        } else {
                            reservedUnits = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_UNIT))) - Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_UNIT)));
                        }
                        HashMap<String, String> updateMap = new HashMap<>();
                        updateMap.put(db.KEY_RESERVED_QTY_CASE, String.format("%.2f", reservedCases));
                        updateMap.put(db.KEY_RESERVED_QTY_UNIT, String.format("%.2f", reservedUnits));
                        db.updateData(db.VAN_STOCK_ITEMS, updateMap, vanStockFilter);
                    }
                }
                while (c.moveToNext());
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return true;
    }

    //This will be used for Sales Invoice
    public JSONArray createPrintData(String type, String orderDate, String orderNo) {
        JSONArray jArr = new JSONArray();
        String invoiceFooter = "";
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_USERNAME, "");
        map.put(db.KEY_STRUCTURE, "");
        map.put(db.KEY_MESSAGE, "");
        map.put(db.KEY_DRIVER, "");

        HashMap<String, String> filterMap = new HashMap<>();
        filterMap.put(db.KEY_USERNAME, Settings.getString(App.DRIVER));
        filterMap.put(db.KEY_STRUCTURE, App.INVOICE_FOOTER_KEY);

        Cursor cursor = db.getData(db.MESSAGES, map, filterMap);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            invoiceFooter = UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MESSAGE)));
        }

        try {
            double totalPcs = 0;
            double totalAmount = 0;
            double totalAmountwithVat = 0;
            double totalVat = 0;
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST, App.SALES_INVOICE);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO", "1234");
            mainArr.put("DOCUMENT NO", orderNo);  //Load Summary No
            mainArr.put("ORDERNO", orderNo);  //Load Summary No
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("supervisorname", "Mr ABC");
            mainArr.put("supervisorno", "056123456");
            mainArr.put("invoicefooter", invoiceFooter);
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            mainArr.put("invheadermsg", "");
            mainArr.put("LANG", "en");
            mainArr.put("invoicepaymentterms", "2");
            mainArr.put("invoicenumber", orderNo);
            mainArr.put("INVOICETYPE", type);
            mainArr.put("CUSTOMERID", object.getCustomerID());
            //String arabicCustomer = "اللولو هايبر ماركت";
//            String arabicCustomer = "لا يوجد نص عربي";

            String strCustomer = object.getCustomerName();

            if (object.getCustomer_name_ar() != null) {
                strCustomer = strCustomer + " - " + object.getCustomer_name_ar();
            }

            mainArr.put("CUSTOMER", UrlBuilder.decodeString(strCustomer));

            if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
                mainArr.put("customertype", "1");
            } else if (object.getPaymentMethod().equals(App.CREDIT_CUSTOMER)) {
                mainArr.put("customertype", "2");
            } else if (object.getPaymentMethod().equals(App.TC_CUSTOMER)) {
                mainArr.put("customertype", "3");
            }
            mainArr.put("ADDRESS", object.getCustomerAddress());
            mainArr.put("ARBADDRESS", "");
            mainArr.put("displayupc", "0");
            mainArr.put("invoicepriceprint", "1");
            mainArr.put("SUB TOTAL", tv_current_invoice.getText().toString());
            mainArr.put("VAT", tv_Vat.getText().toString());
            Log.e("Discount is", "" + tv_discount.getText().toString());
            mainArr.put("INVOICE DISCOUNT", String.format("%.2f", discount + goodreturndiscount + badreturndiscount));
            if (Const.isDisplayVatTax) {
                mainArr.put("NET SALES", tv_net_invoiceVat.getText().toString());
            } else {
                mainArr.put("NET SALES", tv_net_invoice.getText().toString());
            }

            //mainArr.put("Load Number","1");
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

            mainArr.put("HEADERS", HEADERS);
            JSONArray jData = new JSONArray();
            for (Sales obj : arraylist) {
                if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {

                    JSONArray data = new JSONArray();
                    data.put("" + (jData.length() + 1));
                    data.put(obj.getMaterial_no());
                    data.put(obj.getMaterial_description());
//                    data.put("PCS");

                    double amount = 0.0, qty = 0.0;
                    String deno = "0";
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filtera = new HashMap<>();
                    filtera.put(db.KEY_MATERIAL_NO, obj.getMaterial_no());
                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                    if (altUOMCursor.getCount() > 0) {
                        altUOMCursor.moveToFirst();
                        deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                    }
                    if (Double.parseDouble(obj.getCases()) >= 1) {

                        Double casePrice = Double.parseDouble(obj.getPricecase());
                        amount += casePrice * Float.parseFloat(obj.getCases());
                        qty = Float.parseFloat(obj.getCases()) * Double.parseDouble(deno);
                    }
                    if (Double.parseDouble(obj.getPic()) >= 1) {
                        amount += Float.parseFloat(obj.getPricepcs()) * Float.parseFloat(obj.getPic());
                        qty += Double.parseDouble(obj.getPic());
                    }
                    double itemValue = amount / qty;
                    /*int intCases = 0;
                    int intpcs = 0;
                    if (Double.parseDouble(deno) > 0) {
                        if (qty > Double.parseDouble(deno)) {
                            double Allitem = qty / Double.parseDouble(deno);
                            intCases = (int) Allitem;
                            intpcs = (int) qty - (intCases * Integer.parseInt(deno));
                        } else {
                            intpcs = (int) qty;
                        }
                    } else {
                        intpcs = (int) qty;
                    }*/

                    Double vat = amount * Double.parseDouble(Settings.getString(App.VATValue));
                    Double totalAmt = amount + vat;

                    data.put(obj.getCases() + "      " + obj.getPic());

                    data.put(String.format("%.2f", itemValue));
                    data.put(String.format("%.2f", amount));
                    data.put("0");
                    data.put(String.format("%.2f", vat));
                    data.put(Settings.getString(App.VATPR));
                    data.put(String.format("%.2f", totalAmt));
                    jData.put(data);


                    totalPcs += Double.parseDouble(String.format("%.2f", qty));
                    totalAmount += Double.parseDouble(String.format("%.2f", amount));
                    totalVat += Double.parseDouble(String.format("%.2f", vat));
                    totalAmountwithVat += Double.parseDouble(String.format("%.2f", totalAmt));

                }
            }
            /*JSONArray focData = new JSONArray();
            for (Sales obj : focList) {
                if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {



                    JSONArray data = new JSONArray();
                    data.put(StringUtils.stripStart(obj.getMaterial_no(), "0"));
                    data.put(UrlBuilder.decodeString(obj.getMaterial_description()));
                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,obj.getMaterial_no());
                    if(articleHeader!=null){
                        data.put(articleHeader.getMaterialDesc2().equals("")?App.ARABIC_TEXT_MISSING:articleHeader.getMaterialDesc2());
                    }
                    else{
                        data.put(App.ARABIC_TEXT_MISSING);
                    }
                    //data.put("شد 48*200مل بيرين PH8");
                    //data.put("شد 48*200مل بيرين PH8");
                    data.put("1");
                    data.put("+" + obj.getCases());
                    totalPcs += Double.parseDouble(obj.getCases());
                    data.put(obj.getPrice());
                    data.put("-" + String.format( "%.2f",Double.parseDouble(obj.getCases()) * Double.parseDouble(obj.getPrice())));
                    totalAmount -= Double.parseDouble(obj.getCases()) * Double.parseDouble(obj.getPrice());
                    focData.put(data);
                }
            }
            JSONArray grData = new JSONArray();
            for (Sales obj : grList) {
                if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {

                    JSONArray data = new JSONArray();
                    data.put(""+(grData.length()+1));
                    data.put(obj.getMaterial_no());
                    data.put(obj.getMaterial_description());
//                    data.put("PCS");


                    double amount = 0.0,qty = 0.0;
                    String deno = "0";
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filtera = new HashMap<>();
                    filtera.put(db.KEY_MATERIAL_NO, obj.getMaterial_no());
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
                    if(Double.parseDouble(obj.getPic()) >= 1){
                        amount += Float.parseFloat(obj.getPricepcs()) * Float.parseFloat(obj.getPic());
                        qty += Double.parseDouble(obj.getPic());
                    }
                    double itemValue = amount/qty;

                    Double vat = amount*Double.parseDouble(Settings.getString(App.VATValue));
                    Double totalAmt = amount+vat;

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

                    data.put(String.valueOf(intCases)+"      "+String.valueOf(intpcs));
                    data.put(String.format("%.2f",itemValue));
                    data.put(String.format("%.2f",amount));
                    data.put("0");
                    data.put(String.format("%.2f",vat));
                    data.put(Settings.getString(App.VATPR));
                    data.put(String.format("%.2f",totalAmt));

                    grData.put(data);

                    totalPcs += Double.parseDouble(String.format("%.2f",qty));
                    totalAmount -= Double.parseDouble(String.format("%.2f",amount));
                    totalVat -= Double.parseDouble(String.format("%.2f",vat));
                    totalAmountwithVat -= Double.parseDouble(String.format("%.2f",totalAmt));

                }
            }
            //grData.put(jData1);
            // grData.put(jData2);
            // grData.put(jData3);
            JSONArray brData = new JSONArray();
            for (Sales obj : brList) {
                if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {

                    JSONArray data = new JSONArray();
                    data.put(""+(brData.length()+1));
                    data.put(obj.getMaterial_no());
                    data.put(obj.getMaterial_description());
//                    data.put("PCS");

                    double amount = 0.0,qty = 0.0;
                    String deno = "0";
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filtera = new HashMap<>();
                    filtera.put(db.KEY_MATERIAL_NO, obj.getMaterial_no());
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
                    if(Double.parseDouble(obj.getPic()) >= 1){
                        amount += Float.parseFloat(obj.getPricepcs()) * Float.parseFloat(obj.getPic());
                        qty += Double.parseDouble(obj.getPic());
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
                    data.put(String.format("%.2f",totalAmt));

                    brData.put(data);

                    totalPcs += Double.parseDouble(String.format("%.2f",qty));
                    totalAmount -= Double.parseDouble(String.format("%.2f",amount));
                    totalVat -= Double.parseDouble(String.format("%.2f",vat));
                    totalAmountwithVat -= Double.parseDouble(String.format("%.2f",totalAmt));

                }
            }*/
            JSONObject totalObj = new JSONObject();
            totalObj.put("Total Befor TAX(AED)", String.format("%.2f", totalAmount));
            totalObj.put("VAT(AED)", String.format("%.2f", totalVat));
            totalObj.put("Total Amount(AED)", String.format("%.2f", totalAmountwithVat));
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);
            mainArr.put("data", jData);
//            mainArr.put("foc", focData);
//            mainArr.put("gr", grData);
//            mainArr.put("br", brData);
            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);
            jArr.put(HEADERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jArr;
    }


    public JSONArray createPrintGrBrData(String type, String orderDate, String orderNo) {
        JSONArray jArr = new JSONArray();
        String invoiceFooter = "";
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_USERNAME, "");
        map.put(db.KEY_STRUCTURE, "");
        map.put(db.KEY_MESSAGE, "");
        map.put(db.KEY_DRIVER, "");

        HashMap<String, String> filterMap = new HashMap<>();
        filterMap.put(db.KEY_USERNAME, Settings.getString(App.DRIVER));
        filterMap.put(db.KEY_STRUCTURE, App.INVOICE_FOOTER_KEY);

        Cursor cursor = db.getData(db.MESSAGES, map, filterMap);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            invoiceFooter = UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MESSAGE)));
        }

        try {
            double totalPcs = 0;
            double totalAmount = 0;
            double totalAmountwithVat = 0;
            double totalVat = 0;
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST, type);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO", "1234");
            mainArr.put("DOCUMENT NO", orderNo);  //Load Summary No
            mainArr.put("ORDERNO", orderNo);  //Load Summary No
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("supervisorname", "Mr ABC");
            mainArr.put("supervisorno", "056123456");
            mainArr.put("invoicefooter", invoiceFooter);
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            mainArr.put("invheadermsg", "");
            mainArr.put("LANG", "en");
            mainArr.put("invoicepaymentterms", "2");
            mainArr.put("invoicenumber", orderNo);
            mainArr.put("INVOICETYPE", type);
            mainArr.put("CUSTOMERID", object.getCustomerID());
            //String arabicCustomer = "اللولو هايبر ماركت";
//            String arabicCustomer = "لا يوجد نص عربي";

            String strCustomer = object.getCustomerName();

            if (object.getCustomer_name_ar() != null) {
                strCustomer = strCustomer + " - " + object.getCustomer_name_ar();
            }

            mainArr.put("CUSTOMER", UrlBuilder.decodeString(strCustomer));

            if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
                mainArr.put("customertype", "1");
            } else if (object.getPaymentMethod().equals(App.CREDIT_CUSTOMER)) {
                mainArr.put("customertype", "2");
            } else if (object.getPaymentMethod().equals(App.TC_CUSTOMER)) {
                mainArr.put("customertype", "3");
            }
            mainArr.put("ADDRESS", object.getCustomerAddress());
            mainArr.put("ARBADDRESS", "");
            mainArr.put("displayupc", "0");
            mainArr.put("invoicepriceprint", "1");
            mainArr.put("SUB TOTAL", tv_current_invoice.getText().toString());
            mainArr.put("VAT", tv_Vat.getText().toString());
            Log.e("Discount is", "" + tv_discount.getText().toString());
            mainArr.put("INVOICE DISCOUNT", String.format("%.2f", discount + goodreturndiscount + badreturndiscount));
            if (Const.isDisplayVatTax) {
                mainArr.put("NET SALES", tv_net_invoiceVat.getText().toString());
            } else {
                mainArr.put("NET SALES", tv_net_invoice.getText().toString());
            }

            //mainArr.put("Load Number","1");
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

            mainArr.put("HEADERS", HEADERS);
            /*JSONArray jData = new JSONArray();
            for (Sales obj : arraylist) {
                if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {

                    JSONArray data = new JSONArray();
                    data.put(""+(jData.length()+1));
                    data.put(obj.getMaterial_no());
                    data.put(obj.getMaterial_description());
//                    data.put("PCS");

                    double amount = 0.0,qty = 0.0;
                    String deno = "0";
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filtera = new HashMap<>();
                    filtera.put(db.KEY_MATERIAL_NO, obj.getMaterial_no());
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
                    if(Double.parseDouble(obj.getPic()) >= 1){
                        amount += Float.parseFloat(obj.getPricepcs()) * Float.parseFloat(obj.getPic());
                        qty += Double.parseDouble(obj.getPic());
                    }
                    double itemValue = amount/qty;
                    int intCases = 0;
                    int intpcs = 0;
                    if(Double.parseDouble(deno) > 0) {
                        if (qty > Double.parseDouble(deno)) {
                            double Allitem = qty / Double.parseDouble(deno);
                            intCases = (int) Allitem;
                            intpcs = (int) qty - (intCases * Integer.parseInt(deno));
                        } else {
                            intpcs = (int) qty;
                        }
                    }else {
                        intpcs = (int) qty;
                    }

                    Double vat = amount*Double.parseDouble(Settings.getString(App.VATValue));
                    Double totalAmt = amount+vat;

                    data.put(String.valueOf(intCases)+"      "+String.valueOf(intpcs));

                    data.put(String.format("%.2f",itemValue));
                    data.put(String.format("%.2f",amount));
                    data.put("0");
                    data.put(String.format("%.2f",vat));
                    data.put(Settings.getString(App.VATPR));
                    data.put(String.format("%.2f",totalAmt));
                    jData.put(data);


                    totalPcs += Double.parseDouble(String.format("%.2f",qty));
                    totalAmount += Double.parseDouble(String.format("%.2f",amount));
                    totalVat += Double.parseDouble(String.format("%.2f",vat));
                    totalAmountwithVat += Double.parseDouble(String.format("%.2f",totalAmt));

                }
            }*/
            /*JSONArray focData = new JSONArray();
            for (Sales obj : focList) {
                if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {



                    JSONArray data = new JSONArray();
                    data.put(StringUtils.stripStart(obj.getMaterial_no(), "0"));
                    data.put(UrlBuilder.decodeString(obj.getMaterial_description()));
                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles,obj.getMaterial_no());
                    if(articleHeader!=null){
                        data.put(articleHeader.getMaterialDesc2().equals("")?App.ARABIC_TEXT_MISSING:articleHeader.getMaterialDesc2());
                    }
                    else{
                        data.put(App.ARABIC_TEXT_MISSING);
                    }
                    //data.put("شد 48*200مل بيرين PH8");
                    //data.put("شد 48*200مل بيرين PH8");
                    data.put("1");
                    data.put("+" + obj.getCases());
                    totalPcs += Double.parseDouble(obj.getCases());
                    data.put(obj.getPrice());
                    data.put("-" + String.format( "%.2f",Double.parseDouble(obj.getCases()) * Double.parseDouble(obj.getPrice())));
                    totalAmount -= Double.parseDouble(obj.getCases()) * Double.parseDouble(obj.getPrice());
                    focData.put(data);
                }
            }*/
            JSONArray grData = new JSONArray();
            if (type.equals(App.GOOD_RETURN)) {

                for (Sales obj : grList) {
                    if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {

                        JSONArray data = new JSONArray();
                        data.put("" + (grData.length() + 1));
                        data.put(obj.getMaterial_no());
                        data.put(obj.getMaterial_description());
//                    data.put("PCS");


                        double amount = 0.0, qty = 0.0;
                        String deno = "0";
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        altMap.put(db.KEY_DENOMINATOR, "");
                        HashMap<String, String> filtera = new HashMap<>();
                        filtera.put(db.KEY_MATERIAL_NO, obj.getMaterial_no());
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                        }
                        if (Double.parseDouble(obj.getCases()) >= 1) {


                            Double casePrice = Double.parseDouble(obj.getPricecase());
                            amount += casePrice * Float.parseFloat(obj.getCases());
                            qty = Float.parseFloat(obj.getCases()) * Double.parseDouble(deno);
                        }
                        if (Double.parseDouble(obj.getPic()) >= 1) {
                            amount += Float.parseFloat(obj.getPricepcs()) * Float.parseFloat(obj.getPic());
                            qty += Double.parseDouble(obj.getPic());
                        }
                        double itemValue = amount / qty;

                        Double vat = amount * Double.parseDouble(Settings.getString(App.VATValue));
                        Double totalAmt = amount + vat;

                        /*int intCases = 0;
                        int intpcs = 0;
                        if (Double.parseDouble(deno) > 0) {
                            if (qty > Double.parseDouble(deno)) {
                                double Allitem = qty / Double.parseDouble(deno);
                                intCases = (int) Allitem;
                                intpcs = (int) qty - (intCases * Integer.parseInt(deno));
                            } else {
                                intpcs = (int) qty;
                            }
                        } else {
                            intpcs = (int) qty;
                        }*/

                        data.put(obj.getCases() + "      " + obj.getPic());
                        data.put(String.format("%.2f", itemValue));
                        data.put(String.format("%.2f", amount));
                        data.put("0");
                        data.put(String.format("%.2f", vat));
                        data.put(Settings.getString(App.VATPR));
                        data.put(String.format("%.2f", totalAmt));

                        grData.put(data);

                        totalPcs += Double.parseDouble(String.format("%.2f", qty));
                        totalAmount -= Double.parseDouble(String.format("%.2f", amount));
                        totalVat -= Double.parseDouble(String.format("%.2f", vat));
                        totalAmountwithVat -= Double.parseDouble(String.format("%.2f", totalAmt));

                    }
                }
            }
            //grData.put(jData1);
            // grData.put(jData2);
            // grData.put(jData3);
            JSONArray brData = new JSONArray();
            if (type.equals(App.BAD_RETURN)) {

                for (Sales obj : brList) {
                    if (Double.parseDouble(obj.getCases()) > 0 || Double.parseDouble(obj.getPic()) > 0) {

                        JSONArray data = new JSONArray();
                        data.put("" + (brData.length() + 1));
                        data.put(obj.getMaterial_no());
                        data.put(obj.getMaterial_description());
//                    data.put("PCS");

                        double amount = 0.0, qty = 0.0;
                        String deno = "0";
                        HashMap<String, String> altMap = new HashMap<>();
                        altMap.put(db.KEY_UOM, "");
                        altMap.put(db.KEY_DENOMINATOR, "");
                        HashMap<String, String> filtera = new HashMap<>();
                        filtera.put(db.KEY_MATERIAL_NO, obj.getMaterial_no());
                        Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filtera);
                        if (altUOMCursor.getCount() > 0) {
                            altUOMCursor.moveToFirst();
                            deno = "" + altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR));
                        }
                        if (Double.parseDouble(obj.getCases()) >= 1) {


                            Double casePrice = Double.parseDouble(obj.getPricecase());
                            amount += casePrice * Float.parseFloat(obj.getCases());
                            qty = Float.parseFloat(obj.getCases()) * Double.parseDouble(deno);
                        }
                        if (Double.parseDouble(obj.getPic()) >= 1) {
                            amount += Float.parseFloat(obj.getPricepcs()) * Float.parseFloat(obj.getPic());
                            qty += Double.parseDouble(obj.getPic());
                        }
                        double itemValue = amount / qty;
                        /*int intCases = 0;
                        int intpcs = 0;
                        if (Double.parseDouble(deno) > 0) {
                            if (qty > Double.parseDouble(deno)) {
                                double Allitem = qty / Double.parseDouble(deno);
                                intCases = (int) Allitem;
                                intpcs = (int) qty - (intCases * Integer.parseInt(deno));
                            } else {
                                intpcs = (int) qty;
                            }
                        } else {
                            intpcs = (int) qty;
                        }*/

                        Double vat = amount * Double.parseDouble(Settings.getString(App.VATValue));
                        Double totalAmt = amount + vat;

                        data.put(obj.getCases() + "      " + obj.getPic());

                        data.put(String.format("%.2f", itemValue));
                        data.put(String.format("%.2f", amount));
                        data.put("0");
                        data.put(String.format("%.2f", vat));
                        data.put(Settings.getString(App.VATPR));
                        data.put(String.format("%.2f", totalAmt));

                        brData.put(data);

                        totalPcs += Double.parseDouble(String.format("%.2f", qty));
                        totalAmount -= Double.parseDouble(String.format("%.2f", amount));
                        totalVat -= Double.parseDouble(String.format("%.2f", vat));
                        totalAmountwithVat -= Double.parseDouble(String.format("%.2f", totalAmt));

                    }
                }
            }
            JSONObject totalObj = new JSONObject();
            totalObj.put("Total Befor TAX(AED)", String.format("%.2f", totalAmount));
            totalObj.put("VAT(AED)", String.format("%.2f", totalVat));
            totalObj.put("Total Amount(AED)", String.format("%.2f", totalAmountwithVat));
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);
//            mainArr.put("data", jData);
//            mainArr.put("foc", focData);

            if (type.equals(App.GOOD_RETURN)) {
                mainArr.put("gr", grData);
            } else {
                mainArr.put("br", brData);
            }


            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);
            jArr.put(HEADERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jArr;
    }


    //This will be used for Delivery
    public JSONArray createPrintDeliveryData(String type, String orderDate, String orderNo) {
        JSONArray jArr = new JSONArray();
        String invoiceFooter = "";
        String payee = "";
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_USERNAME, "");
        map.put(db.KEY_STRUCTURE, "");
        map.put(db.KEY_MESSAGE, "");
        map.put(db.KEY_DRIVER, "");

        HashMap<String, String> filterMap = new HashMap<>();
        filterMap.put(db.KEY_USERNAME, Settings.getString(App.DRIVER));
        filterMap.put(db.KEY_STRUCTURE, App.INVOICE_FOOTER_KEY);

        Cursor cursor = db.getData(db.MESSAGES, map, filterMap);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            invoiceFooter = UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MESSAGE)));
        }
        HashMap<String, String> payMap = new HashMap<>();
        payMap.put(db.KEY_PAYER_NO, "");

        HashMap<String, String> filterPayMap = new HashMap<>();
        filterPayMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());

        Cursor payCursor = db.getData(db.CUSTOMER_SALES_AREAS, payMap, filterPayMap);
        if (payCursor.getCount() > 0) {
            payCursor.moveToFirst();
            payee = payCursor.getString(payCursor.getColumnIndex(db.KEY_PAYER_NO));
        }
        try {
            double totalPcs = 0;
            double totalAmount = 0;
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST, App.DELIVERY);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO", "1234");
            mainArr.put("DELIVERYDATE", orderDate);
            mainArr.put("DOCUMENT NO", orderNo);  //Load Summary No
            mainArr.put("ORDERNO", orderNo);  //Load Summary No
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("supervisorname", "Mr ABC");
            mainArr.put("supervisorno", "-");
            mainArr.put("invoicefooter", invoiceFooter);
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            mainArr.put("invheadermsg", "");
            mainArr.put("LANG", "en");
            mainArr.put("invoicepaymentterms", "5");
            mainArr.put("invoicenumber", orderNo);
            mainArr.put("INVOICETYPE", type);
            mainArr.put("payee", payee.equals("") ? "-" : payee);
            mainArr.put("PAYMENTREF", "-");
            mainArr.put("PAYSOURCE", "" + object.getPaysource() == null ? "" : object.getPaysource());
//            String arabicCustomer = "اللولو هايبر ماركت";
            String strCustomer = object.getCustomerName();

            if (object.getCustomer_name_ar() != null) {
                strCustomer = strCustomer + " - " + object.getCustomer_name_ar();
            }

            mainArr.put("CUSTOMER", UrlBuilder.decodeString(strCustomer));
            mainArr.put("CUSTOMERID", object.getCustomerID());
            //mainArr.put("customertype", "4");
            if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
                mainArr.put("customertype", "1");
            } else if (object.getPaymentMethod().equals(App.CREDIT_CUSTOMER)) {
                mainArr.put("customertype", "2");
            } else if (object.getPaymentMethod().equals(App.TC_CUSTOMER)) {
                mainArr.put("customertype", "3");
            }
            mainArr.put("ADDRESS", object.getCustomerAddress());
            mainArr.put("ARBADDRESS", "");
            mainArr.put("displayupc", "0");
            mainArr.put("invoicepriceprint", "1");
            mainArr.put("SUB TOTAL", tv_current_invoice.getText().toString());
            mainArr.put("VAT", tv_Vat.getText().toString());
            mainArr.put("INVOICE DISCOUNT", tv_discount.getText().toString().equals("") ? "0" : tv_discount.getText().toString());
            if (Const.isDisplayVatTax) {
                mainArr.put("NET SALES", tv_net_invoiceVat.getText().toString());
            } else {
                mainArr.put("NET SALES", tv_net_invoice.getText().toString());
            }
            //mainArr.put("Load Number","1");
            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();
            HEADERS.put("ITEM NO");
            HEADERS.put("ENGLISH DESCRIPTION");
            HEADERS.put("ARABIC DESCRIPTION");
            HEADERS.put("UPC ");
            HEADERS.put("TOTAL UNITS");
            HEADERS.put("UNIT PRICE");
            HEADERS.put("AMOUNT");
            //HEADERS.put("Description");
            //HEADERS.put(obj1);
            // HEADERS.put(obj2);
            mainArr.put("HEADERS", HEADERS);
            JSONArray jData = new JSONArray();
            for (DeliveryItem obj : deliveryArrayList) {
                if (Double.parseDouble(obj.getItemCase()) > 0 || Double.parseDouble(obj.getItemUnits()) > 0) {
                    JSONArray data = new JSONArray();
                    data.put(StringUtils.stripStart(obj.getMaterialNo(), "0"));
                    data.put(UrlBuilder.decodeString(obj.getItemDescription()));
                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles, obj.getMaterialNo());
                    if (articleHeader != null) {
                        data.put(articleHeader.getMaterialDesc2().equals("") ? App.ARABIC_TEXT_MISSING : articleHeader.getMaterialDesc2());
                    } else {
                        data.put(App.ARABIC_TEXT_MISSING);
                    }
                    //data.put("شد 48*200مل بيرين PH8");
                    data.put("1");
                    data.put("+" + obj.getItemCase());
                    totalPcs += Double.parseDouble(obj.getItemCase());
                    data.put(obj.getAmount());
                    data.put("+" + String.format("%.2f", Double.parseDouble(obj.getItemCase()) * Double.parseDouble(obj.getAmount())));
                    totalAmount += Double.parseDouble(obj.getItemCase()) * Double.parseDouble(obj.getAmount());
                    jData.put(data);
                }
            }
            JSONObject totalObj = new JSONObject();
            totalObj.put("TOTAL UNITS", "+" + String.format("%.2f", totalPcs));
            totalObj.put("UNIT PRICE", "");
            totalObj.put("AMOUNT", "+" + String.format("%.2f", totalAmount));
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);
            mainArr.put("data", jData);
            //mainArr.put("gr",grData);
            //mainArr.put("br",brData);
            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);
            jArr.put(HEADERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jArr;
    }

    public JSONArray createPrintDeliveryDataWithPaymentRef(String type, String orderDate, String orderNo, String paymentReference) {
        Log.e("Payment Ref", paymentReference);
        JSONArray jArr = new JSONArray();
        String invoiceFooter = "";
        String payee = "";
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_USERNAME, "");
        map.put(db.KEY_STRUCTURE, "");
        map.put(db.KEY_MESSAGE, "");
        map.put(db.KEY_DRIVER, "");

        HashMap<String, String> filterMap = new HashMap<>();
        filterMap.put(db.KEY_USERNAME, Settings.getString(App.DRIVER));
        filterMap.put(db.KEY_STRUCTURE, App.INVOICE_FOOTER_KEY);

        Cursor cursor = db.getData(db.MESSAGES, map, filterMap);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            invoiceFooter = UrlBuilder.decodeString(cursor.getString(cursor.getColumnIndex(db.KEY_MESSAGE)));
        }
        HashMap<String, String> payMap = new HashMap<>();
        payMap.put(db.KEY_PAYER_NO, "");

        HashMap<String, String> filterPayMap = new HashMap<>();
        filterPayMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());

        Cursor payCursor = db.getData(db.CUSTOMER_SALES_AREAS, payMap, filterPayMap);
        if (payCursor.getCount() > 0) {
            payCursor.moveToFirst();
            payee = payCursor.getString(payCursor.getColumnIndex(db.KEY_PAYER_NO));
        }
        try {
            double totalPcs = 0;
            double totalAmount = 0;
            JSONArray jInter = new JSONArray();
            JSONObject jDict = new JSONObject();
            jDict.put(App.REQUEST, App.DELIVERY);
            JSONObject mainArr = new JSONObject();
            mainArr.put("ROUTE", Settings.getString(App.ROUTE));
            mainArr.put("DOC DATE", Helpers.formatDate(new Date(), App.PRINT_DATE_FORMAT));
            mainArr.put("TIME", Helpers.formatTime(new Date(), "hh:mm"));
            mainArr.put("SALESMAN", Settings.getString(App.DRIVER));
            mainArr.put("CONTACTNO", "1234");
            mainArr.put("DELIVERYDATE", orderDate);
            mainArr.put("DOCUMENT NO", orderNo);  //Load Summary No
            mainArr.put("ORDERNO", orderNo);  //Load Summary No
            mainArr.put("TRIP START DATE", Helpers.formatDate(new Date(), "dd-MM-yyyy"));
            mainArr.put("supervisorname", "Mr ABC");
            mainArr.put("supervisorno", "-");
            mainArr.put("invoicefooter", invoiceFooter);
            mainArr.put("TripID", Settings.getString(App.TRIP_ID));
            mainArr.put("invheadermsg", "");
            mainArr.put("LANG", "en");
            mainArr.put("invoicepaymentterms", "5");
            mainArr.put("invoicenumber", orderNo);
            mainArr.put("INVOICETYPE", type);
            mainArr.put("PAYMENTREF", paymentReference.equals("") ? "-" : paymentReference);
            mainArr.put("PAYSOURCE", "" + object.getPaysource() == null ? "" : object.getPaysource());
            mainArr.put("payee", payee.equals("") ? "-" : payee);
//            String arabicCustomer = "اللولو هايبر ماركت";
            String strCustomer = object.getCustomerName();

            if (object.getCustomer_name_ar() != null) {
                strCustomer = strCustomer + " - " + object.getCustomer_name_ar();
            }

            mainArr.put("CUSTOMER", UrlBuilder.decodeString(strCustomer));
            mainArr.put("CUSTOMERID", object.getCustomerID());
            //mainArr.put("customertype", "4");
            if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
                mainArr.put("customertype", "1");
            } else if (object.getPaymentMethod().equals(App.CREDIT_CUSTOMER)) {
                mainArr.put("customertype", "2");
            } else if (object.getPaymentMethod().equals(App.TC_CUSTOMER)) {
                mainArr.put("customertype", "3");
            }
            mainArr.put("ADDRESS", object.getCustomerAddress());
            mainArr.put("ARBADDRESS", "");
            mainArr.put("displayupc", "0");
            mainArr.put("invoicepriceprint", "1");
            mainArr.put("SUB TOTAL", tv_current_invoice.getText().toString());
            mainArr.put("VAT", tv_Vat.getText().toString());
            mainArr.put("INVOICE DISCOUNT", tv_discount.getText().toString().equals("") ? "0" : tv_discount.getText().toString());
            if (Const.isDisplayVatTax) {
                mainArr.put("NET SALES", tv_net_invoiceVat.getText().toString());
            } else {
                mainArr.put("NET SALES", tv_net_invoice.getText().toString());
            }
            //mainArr.put("Load Number","1");
            JSONArray HEADERS = new JSONArray();
            JSONArray TOTAL = new JSONArray();
            HEADERS.put("ITEM NO");
            HEADERS.put("ENGLISH DESCRIPTION");
            HEADERS.put("ARABIC DESCRIPTION");
            HEADERS.put("UPC ");
            HEADERS.put("TOTAL UNITS");
            HEADERS.put("UNIT PRICE");
            HEADERS.put("AMOUNT");
            //HEADERS.put("Description");
            //HEADERS.put(obj1);
            // HEADERS.put(obj2);
            mainArr.put("HEADERS", HEADERS);
            JSONArray jData = new JSONArray();
            for (DeliveryItem obj : deliveryArrayList) {
                if (Double.parseDouble(obj.getItemCase()) > 0 || Double.parseDouble(obj.getItemUnits()) > 0) {
                    JSONArray data = new JSONArray();
                    data.put(StringUtils.stripStart(obj.getMaterialNo(), "0"));
                    data.put(UrlBuilder.decodeString(obj.getItemDescription()));
                    ArticleHeader articleHeader = ArticleHeader.getArticle(articles, obj.getMaterialNo());
                    if (articleHeader != null) {
                        data.put(articleHeader.getMaterialDesc2().equals("") ? App.ARABIC_TEXT_MISSING : articleHeader.getMaterialDesc2());
                    } else {
                        data.put(App.ARABIC_TEXT_MISSING);
                    }
                    //data.put("شد 48*200مل بيرين PH8");
                    data.put("1");
                    data.put("+" + obj.getItemCase());
                    totalPcs += Double.parseDouble(obj.getItemCase());
                    data.put(obj.getAmount());
                    data.put("+" + String.format("%.2f", Double.parseDouble(obj.getItemCase()) * Double.parseDouble(obj.getAmount())));
                    totalAmount += Double.parseDouble(obj.getItemCase()) * Double.parseDouble(obj.getAmount());
                    jData.put(data);
                }
            }
            JSONObject totalObj = new JSONObject();
            totalObj.put("TOTAL UNITS", "+" + String.format("%.2f", totalPcs));
            totalObj.put("UNIT PRICE", "");
            totalObj.put("AMOUNT", "+" + String.format("%.2f", totalAmount));
            TOTAL.put(totalObj);
            mainArr.put("TOTAL", TOTAL);
            mainArr.put("data", jData);
            //mainArr.put("gr",grData);
            //mainArr.put("br",brData);
            jDict.put("mainArr", mainArr);
            jInter.put(jDict);
            jArr.put(jInter);
            jArr.put(HEADERS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jArr;
    }

    public void callback(String callingFunction) {
        //rakshit
        if (callingFunction.equals(App.SALES_INVOICE)) {
            if (!orderID_GlobalGR.equals("")) {
                    try {
                        JSONArray jsonArray = createPrintGrBrData(App.GOOD_RETURN, "", orderID_GlobalGR);
                        JSONObject data = new JSONObject();
                        data.put("data", (JSONArray) jsonArray);

                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        map.put(db.KEY_ORDER_ID, orderID_GlobalGR);
                        map.put(db.KEY_DOC_TYPE, ConfigStore.GoodReturns_TR);
                        map.put(db.KEY_DATA, data.toString());
                        //map.put(db.KEY_DATA,jsonArray.toString());
                        db.addDataPrint(db.DELAY_PRINT, map);


                        Log.v("JsonArray", jsonArray.toString() + "-");

                        PrinterHelper object = new PrinterHelper(PromotioninfoActivity.this, PromotioninfoActivity.this);
                        object.execute("", jsonArray);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                orderID_GlobalGR = "";
            }else if (!orderID_GlobalBR.equals("")) {
                    try {
                        JSONArray jsonArray = createPrintGrBrData(App.BAD_RETURN, "", orderID_GlobalBR);
                        JSONObject data = new JSONObject();
                        data.put("data", (JSONArray) jsonArray);

                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                        map.put(db.KEY_ORDER_ID, orderID_GlobalBR);
                        map.put(db.KEY_DOC_TYPE, ConfigStore.BadReturns_TR);
                        map.put(db.KEY_DATA, data.toString());
                        //map.put(db.KEY_DATA,jsonArray.toString());
                        db.addDataPrint(db.DELAY_PRINT, map);


                        Log.v("JsonArray", jsonArray.toString() + "-");

                        PrinterHelper object = new PrinterHelper(PromotioninfoActivity.this, PromotioninfoActivity.this);
                        object.execute("", jsonArray);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                orderID_GlobalBR = "";
            }else if (object.getPaymentMethod().equalsIgnoreCase(App.CASH_CUSTOMER)) {
                //Go to payment details screen
                Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                intent.putExtra("msg", str_promotion_message);
                intent.putExtra("from", from);
                intent.putExtra("invoiceno", orderID_Global);
                intent.putExtra("headerObj", object);
                if (Const.isDisplayVatTax) {
                    intent.putExtra("amountdue", tv_net_invoiceVat.getText().toString());
                } else {
                    intent.putExtra("amountdue", tv_net_invoice.getText().toString());
                }

                startActivity(intent);
                finish();
            } else {
                Intent intent1 = new Intent(PromotioninfoActivity.this, CustomerDetailActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent1.putExtra("headerObj", object);
                intent1.putExtra("msg", "all");
                startActivity(intent1);
                finish();
            }

        } else if (callingFunction.equals(App.DELIVERY)) {
//            if (object.getPaymentMethod().equals(App.CASH_CUSTOMER)) {
            /*************************
             * ********************Changes by Rakshit for Payment Reference
             *********************** **/
//                if(Settings.getString(App.DIST_CHANNEL).equals("30")){
            if (delivery.getOrderReferenceNo().isEmpty()) {
                Intent intent = new Intent(PromotioninfoActivity.this, PaymentDetails.class);
                intent.putExtra("msg", str_promotion_message);
                intent.putExtra("from", from);
                intent.putExtra("headerObj", object);
                intent.putExtra("delivery", delivery);
                intent.putExtra("invoiceno", orderID_Global);
                intent.putExtra("invoiceamount", tv_current_invoice.getText().toString());
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(PromotioninfoActivity.this, DeliveryActivity.class);
                intent.putExtra("headerObj", object);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

        }
    }

    @Override
    public void onBackPressed() {
        // Do not allow hardware back navigation
    }
}