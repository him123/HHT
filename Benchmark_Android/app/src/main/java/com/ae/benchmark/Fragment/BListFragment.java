package com.ae.benchmark.Fragment;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.activities.CategoryListActivity;
import com.ae.benchmark.activities.SalesInvoiceActivity;
import com.ae.benchmark.adapters.ReasonAdapter;
import com.ae.benchmark.adapters.SalesInvoiceAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.Reasons;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by eheuristic on 12/5/2016.
 */
public class  BListFragment extends Fragment {
    View viewmain;
    ListView listSales;
    public static SalesInvoiceAdapter adapter;
    public static ArrayList<Sales> arrProductList;
    FloatingActionButton fab;
    FloatingActionButton addProducts;
    LinearLayout ll_top;
    private static DatabaseHandler db;
    private ArrayList<Reasons> reasonsList = new ArrayList<>();
    ArrayAdapter<Reasons> myAdapter;
    String orderID = "";
    Customer object;
    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("Step1", "Step1");
        try{
            if (savedInstanceState != null) {
                Log.e("i am here", "here");
                arrProductList = savedInstanceState.getParcelableArrayList("br");
                setBadReturns(arrProductList);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }*/
    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try{
            outState.putParcelableArrayList("br", arrProductList);
            Const.brBundle = new Bundle();
            Const.brBundle.putParcelableArrayList("br", arrProductList);
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }*/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            viewmain = inflater.inflate(R.layout.fragment_salesinvoice, container, false);
            object = getArguments().getParcelable("data");
            Log.e("Bad Return Frag", "" + object.getCustomerID());
            listSales = (ListView) viewmain.findViewById(R.id.list_sales);
            ll_top = (LinearLayout) viewmain.findViewById(R.id.ll_top);
            ll_top.setVisibility(View.GONE);
            db = DatabaseHandler.getInstance(getActivity());
            //OrderReasons.loadData(getActivity());
            //reasonsList = OrderReasons.get();
            myAdapter = new ReasonAdapter(getActivity(), android.R.layout.simple_spinner_item, reasonsList);

            // fab = (FloatingActionButton) viewmain.findViewById(R.id.fab);
            fab = (FloatingActionButton) viewmain.findViewById(R.id.add);
            addProducts = (FloatingActionButton) viewmain.findViewById(R.id.fab);
            fab.setImageDrawable(getResources().getDrawable(R.drawable.btn_select_all));
            fab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_select_all));
            addProducts.setImageDrawable(getResources().getDrawable(R.drawable.ic_white_add));
            addProducts.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_white_add));
            addProducts.setVisibility(View.VISIBLE);
            if(App.CustomerRouteControl.isDamaged()){
                addProducts.setEnabled(true);
            }
            else{
                addProducts.setEnabled(false);
                addProducts.setAlpha(.5f);
            }
            fab.setVisibility(View.GONE);

            arrProductList = new ArrayList<>();

            listSales.setAdapter(null);
            adapter = new SalesInvoiceAdapter(getActivity(), arrProductList);
            listSales.setAdapter(adapter);



                if(Const.brBundle!=null){
                    Log.e("i am here","here");
                    try {
                        arrProductList = Const.brBundle.getParcelableArrayList("br");

                    }catch (Exception e){
                        arrProductList = new ArrayList<>();
                        e.printStackTrace();
                    }
                    try {
                        Log.e("i am here", "here" + arrProductList.size());
                        if (arrProductList.size() > 0) {
                            listSales.setAdapter(null);
                            adapter = new SalesInvoiceAdapter(getActivity(), arrProductList);
                            listSales.setAdapter(adapter);
                            calculateCost();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                    filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                    if (db.checkData(db.RETURNS, filter)) {
                        Log.e("I am inside", "" + "Hello");
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ORDER_ID, "");
                        Cursor cursor = db.getData(db.RETURNS, map, filter);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            orderID = cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID));
                            Log.e("ORDER ID", "" + orderID);
                            new loadBadReturns(orderID);
                        }
                    }
                }


            new loadReasons().execute();
            registerForContextMenu(listSales);
            listSales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                    TextView item = (TextView) view.findViewById(R.id.tv_item_code);
                    int pos = position;
                    for(Sales data : arrProductList){

                        if(item.getText().toString().equals(data.getMaterial_no())){
                            pos = arrProductList.indexOf(data);
                        }
                    }

                    final Sales sales = arrProductList.get(pos);
                    final Dialog dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_with_crossbutton_editprice);
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    TextView tv = (TextView) dialog.findViewById(R.id.dv_title);
                    final TextView tv_Vat = (TextView) dialog.findViewById(R.id.tv_Total);
                    tv.setText(sales.getName());
                    ImageView iv_cancle = (ImageView) dialog.findViewById(R.id.imageView_close);
                    Button btn_save = (Button) dialog.findViewById(R.id.btn_save);
                    final EditText ed_cases = (EditText) dialog.findViewById(R.id.ed_cases);
                    final EditText ed_pcs = (EditText) dialog.findViewById(R.id.ed_pcs);
                    final EditText ed_cases_inv = (EditText) dialog.findViewById(R.id.ed_cases_inv);
                    final EditText ed_pcs_inv = (EditText) dialog.findViewById(R.id.ed_pcs_inv);
                    final EditText ed_cases_price = (EditText) dialog.findViewById(R.id.ed_cases_price);
                    final EditText ed_pcs_price = (EditText) dialog.findViewById(R.id.ed_pcs_price);
                    RelativeLayout rl_specify = (RelativeLayout) dialog.findViewById(R.id.rl_specify_reason);
                    rl_specify.setVisibility(View.VISIBLE);
                    final Spinner spin = (Spinner) dialog.findViewById(R.id.spin);
                    spin.setAdapter(myAdapter);
                    if (sales.getReasonCode() != null) {
                        spin.setSelection(getIndex(sales.getReasonCode()));
                    }

                    spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Reasons reason = myAdapter.getItem(position);
                            sales.setReasonCode(reason.getReasonID());
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    /*ed_cases_inv.setText("10");
                    ed_pcs_inv.setText("3");
                    ed_cases_inv.setEnabled(false);
                    ed_pcs_inv.setEnabled(false);
                    if (sales.isAltUOM()) {
                        ed_pcs.setEnabled(true);
                    } else {
                        ed_pcs.setEnabled(false);
                    }*/
                    //ed_cases.setText(sales.getCases());
                    //ed_pcs.setText(sales.getPic());


                    /*String strpc="",strcase="0.00";
                    strpc = ""+sales.getPrice();
                    try {
                        Double casePrice = Double.parseDouble(sales.getPrice()) * Double.parseDouble(sales.getDenominator());
                        strcase = "" + String.format("%.2f", casePrice);
                    }catch (Exception e){
                        e.printStackTrace();
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

                                Double Price = calculateItemPriceFromCases(Double.parseDouble(ed_cases_price.getText().toString().equals("") ? "0" : ed_cases_price.getText().toString()), Double.parseDouble(sales.getDenominator()));
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


                                Double Price = calculateItemPriceFromPcs(Double.parseDouble(ed_pcs_price.getText().toString().equals("") ? "0" : ed_pcs_price.getText().toString()), Double.parseDouble(sales.getDenominator()));
                                ed_cases_price.setText(String.format("%.2f", Price));
                                Double Vat = calculateVatTotal(Double.parseDouble(ed_cases.getText().toString().equals("") ? "0" : ed_cases.getText().toString()), Double.parseDouble(ed_cases_price.getText().toString().equals("") ? "0" : ed_cases_price.getText().toString()), Double.parseDouble(ed_pcs.getText().toString().equals("") ? "0" : ed_pcs.getText().toString()), Double.parseDouble(ed_pcs_price.getText().toString().equals("") ? "0" : ed_pcs_price.getText().toString()));
                                tv_Vat.setText("Total = " + String.format("%.2f", Vat));
                            }
                        }
                    });

                    ed_cases_price.setText(sales.getPricecase());
                    ed_pcs_price.setText(sales.getPricepcs());

                    ed_cases.setText(sales.getCases().trim().equals("0")?"":sales.getCases().trim());
                    ed_pcs.setText(sales.getPic().trim().equals("0")?"":sales.getPic().trim());

                    LinearLayout ll_1 = (LinearLayout) dialog.findViewById(R.id.ll_1);
                    ll_1.setVisibility(View.GONE);
                    iv_cancle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    arrProductList.remove(pos);
                    arrProductList.add(pos, sales);
                    final int finalPos = pos;
                    btn_save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (spin.getSelectedItem().toString().equals("")) {
                                ((TextView) spin.getSelectedView()).setError("select reason");
                            } else {
                                String strCase = ed_cases.getText().toString();
                                String strpcs = ed_pcs.getText().toString();
                                String strCasePrice = ed_cases_price.getText().toString();
                                String strpcsPrice = ed_pcs_price.getText().toString();
                                String strcaseinv = ed_cases_inv.getText().toString();
                                String strpcsinv = ed_pcs_inv.getText().toString();
                                if (strCase.equals("")) {
                                    strCase = "0";
                                }
                                if (strpcs.equals("")) {
                                    strpcs = "0";
                                }
                                if (strCasePrice.equals("")) {
                                    strCasePrice = "0";
                                }
                                if (strpcsPrice.equals("")) {
                                    strpcsPrice = "0";
                                }
                                TextView tv_cases = (TextView) view.findViewById(R.id.tv_cases_value);
                                TextView tv_pcs = (TextView) view.findViewById(R.id.tv_pcs_value);
                                //tv_cases.setText(strCase);
                                //tv_pcs.setText(strpcs);

                                tv_cases.setText(strCase.trim().equals("")?"0":strCase.trim());
                                tv_pcs.setText(strpcs.trim().equals("")?"0":strpcs.trim());

                                if((Double.parseDouble(sales.getPricemin()) > Double.parseDouble(strpcsPrice))){
                                    Toast.makeText(getActivity(),"You can not sale item Min Price: "+sales.getPricemin(),Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if((Double.parseDouble(sales.getPricemax()) < Double.parseDouble(strpcsPrice))){
                                    Toast.makeText(getActivity(),"You can not sale item Max Price: "+sales.getPricemax(),Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Sales sales = arrProductList.get(finalPos);
                                sales.setPic(strpcs);
                                sales.setCases(strCase);
                                sales.setPricepcs(strpcsPrice);
                                sales.setPricecase(strCasePrice);
                                double total = 0;
                                int salesTotal = 0;
                                int pcsTotal = 0;
                                for (Sales sale : arrProductList) {
                                    double itemPrice = 0;
                                    if (sale.getUom().equals(App.CASE_UOM) || sale.getUom().equals(App.CASE_UOM_NEW) || sale.getUom().equals(App.BOTTLES_UOM)) {
                                        itemPrice = Double.parseDouble(sale.getCases()) * Double.parseDouble(sale.getPrice());
                                    } /*else if (sale.getUom().equals(App.BOTTLES_UOM)) {
                                    itemPrice = Double.parseDouble(sale.getPic()) * Double.parseDouble(sale.getPrice());
                                }*/
                                    total += itemPrice;
                                    salesTotal = salesTotal + Integer.parseInt(sale.getCases());
                                    pcsTotal = pcsTotal + Integer.parseInt(sale.getPic());
                                }
                                TextView tv = (TextView) viewmain.findViewById(R.id.tv_amt);
                                tv.setText(String.valueOf(total));
                                TextView tvsales = (TextView) viewmain.findViewById(R.id.tv_sales_qty);
                                tvsales.setText(salesTotal + "/" + pcsTotal);
                                calculateCost();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
            addProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Settings.setString("from", "blist");
                    SalesInvoiceActivity.tab_position = 3;
                    Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                    getActivity().startActivity(intent);
                }
            });
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (orderID.equals("") || orderID == null) {
                        String purchaseNumber = Helpers.generateNumber(db, ConfigStore.BadReturns_PR_Type);
                        for (Sales sale : arrProductList) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                            map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            map.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                            map.put(db.KEY_REASON_CODE, sale.getReasonCode());
                            map.put(db.KEY_ITEM_NO, sale.getItem_code());
                            map.put(db.KEY_MATERIAL_DESC1, sale.getName());
                            map.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            map.put(db.KEY_MATERIAL_GROUP, "");
                            map.put(db.KEY_CASE, sale.getCases());
                            map.put(db.KEY_UNIT, sale.getPic());
                            map.put(db.KEY_UOM, sale.getUom());
                            map.put(db.KEY_PRICE, sale.getPrice());
                            map.put(db.KEY_ORDER_ID, purchaseNumber);
                            map.put(db.KEY_PURCHASE_NUMBER, purchaseNumber);
                            map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            map.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                            db.addData(db.RETURNS, map);
                        }
                    } else {
                        Log.e("In Update", "In update");
                        for (Sales sale : arrProductList) {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                            map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                            map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            map.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                            map.put(db.KEY_REASON_CODE, sale.getReasonCode());
                            map.put(db.KEY_ITEM_NO, sale.getItem_code());
                            map.put(db.KEY_MATERIAL_DESC1, sale.getName());
                            map.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            map.put(db.KEY_MATERIAL_GROUP, "");
                            map.put(db.KEY_CASE, sale.getCases());
                            map.put(db.KEY_UNIT, sale.getPic());
                            map.put(db.KEY_UOM, sale.getUom());
                            map.put(db.KEY_PRICE, sale.getPrice());
                            map.put(db.KEY_ORDER_ID, orderID);
                            map.put(db.KEY_PURCHASE_NUMBER, orderID);
                            map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            map.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                            HashMap<String, String> filter = new HashMap<String, String>();
                            filter.put(db.KEY_PURCHASE_NUMBER, orderID);
                            filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                            filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                            filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                            if (db.checkData(db.RETURNS, filter)) {
                                db.updateData(db.RETURNS, map, filter);
                            } else {
                                db.addData(db.RETURNS, map);
                            }
                       /* db.updateData(db.RETURNS, map, filter);*/
                        }
                    }
                    getActivity().finish();
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return viewmain;
    }
    private int getIndex(String myString) {
        int index = 0;
        for (int i = 0; i < reasonsList.size(); i++) {
            Reasons reason = reasonsList.get(i);
            if (reason.getReasonID().equals(myString)) {
                index = i;
            }
        }
        return index;
    }
    public static ArrayList<Sales> setProductList() {
        try{
            if (arrProductList.size() > 0) {
                for (int i = 0; i < Const.addlist.size(); i++) {
                    HashMap<String, String> searchMap = new HashMap<>();
                    searchMap.put(db.KEY_MATERIAL_NO, "");
                    searchMap.put(db.KEY_BASE_UOM, "");
                    searchMap.put(db.KEY_MATERIAL_DESC1, "");
                    HashMap<String, String> filterSearch = new HashMap<>();
                    filterSearch.put(db.KEY_MATERIAL_DESC1, UrlBuilder.clean(Const.addlist.get(i)));
                    Cursor articleCursor = db.getData(db.ARTICLE_HEADER, searchMap, filterSearch);
                    articleCursor.moveToFirst();
                    Sales sale = new Sales();
                    sale.setMaterial_no(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setItem_code(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setUom(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_BASE_UOM)));
                    sale.setName(Const.addlist.get(i));
                    sale.setCases("0");
                    sale.setPic("0");
                    HashMap<String, String> filterPart = new HashMap<>();
                    filterPart.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_AMOUNT, "");
                    map.put(db.KEY_AMOUNTMIN,"");
                    map.put(db.KEY_AMOUNTMAX,"");
                    if (db.checkData(db.PRICING, filterPart)) {
                        //Pricing exists for Product for customer
                        //Pricing exists for Product for customer
                        Cursor priceCursor = db.getData(db.PRICING, map, filterPart);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                            sale.setPrice(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM)?price:price);
                            sale.setPricepcs(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM)?price:price);
                            sale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                            sale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                        }
                    } else {
                        sale.setPrice("0");
                        sale.setPricemin("0");
                        sale.setPricemax("0");
                        sale.setPricepcs("0");
                    }

                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
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
                        sale.setDenominator(""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR)));
                    } else {
                        sale.setIsAltUOM(false);
                        sale.setDenominator("0");
                    }


                    try {
                        Double casePrice = Double.parseDouble(sale.getPrice()) * Double.parseDouble(sale.getDenominator());
                        sale.setPricecase(String.format("%.2f", casePrice));
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    arrProductList.add(sale);
                }
            } else {
                arrProductList.clear();
                for (int i = 0; i < Const.addlist.size(); i++) {
                    HashMap<String, String> searchMap = new HashMap<>();
                    searchMap.put(db.KEY_MATERIAL_NO, "");
                    searchMap.put(db.KEY_BASE_UOM, "");
                    searchMap.put(db.KEY_MATERIAL_DESC1, "");
                    HashMap<String, String> filterSearch = new HashMap<>();
                    filterSearch.put(db.KEY_MATERIAL_DESC1, UrlBuilder.clean(Const.addlist.get(i)));
                    Cursor articleCursor = db.getData(db.ARTICLE_HEADER, searchMap, filterSearch);
                    articleCursor.moveToFirst();
                    Sales sale = new Sales();
                    sale.setMaterial_no(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setItem_code(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    sale.setUom(articleCursor.getString(articleCursor.getColumnIndex(db.KEY_BASE_UOM)));
                    sale.setName(Const.addlist.get(i));
                    sale.setCases("0");
                    sale.setPic("0");
                    HashMap<String, String> filterPart = new HashMap<>();
                    filterPart.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                    HashMap<String, String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO, "");
                    map.put(db.KEY_AMOUNT, "");
                    map.put(db.KEY_AMOUNTMIN,"");
                    map.put(db.KEY_AMOUNTMAX,"");
                    if (db.checkData(db.PRICING, filterPart)) {
                        //Pricing exists for Product for customer
                        //Pricing exists for Product for customer
                        Cursor priceCursor = db.getData(db.PRICING, map, filterPart);
                        if (priceCursor.getCount() > 0) {
                            priceCursor.moveToFirst();
                            String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                            sale.setPrice(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM) ? price : price);
                            sale.setPricepcs(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM) ? price : price);
                            sale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                            sale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                        }
                    } else {
                        sale.setPrice("0");
                        sale.setPricemin("0");
                        sale.setPricemax("0");
                        sale.setPricepcs("0");
                    }
                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter);
                    if (altUOMCursor.getCount() > 0) {
                        altUOMCursor.moveToFirst();
                        sale.setDenominator(""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR)));
                    } else {
                        sale.setDenominator("0");
                    }
                    try {
                        Double casePrice = Double.parseDouble(sale.getPrice()) * Double.parseDouble(sale.getDenominator());
                        sale.setPricecase(String.format("%.2f", casePrice));
                    }catch (Exception e){
                        e.printStackTrace();
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
    public class loadReasons extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_REASON_TYPE, "");
            map.put(db.KEY_REASON_DESCRIPTION, "");
            map.put(db.KEY_REASON_CODE, "");
            HashMap<String, String> filter = new HashMap<>();
            Cursor cursor = db.getData(db.REASONS, map, filter);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Reasons reasons = new Reasons();
                    reasons.setReasonID(cursor.getString(cursor.getColumnIndex(db.KEY_REASON_CODE)));
                    reasons.setReasonType(cursor.getString(cursor.getColumnIndex(db.KEY_REASON_TYPE)));
                    reasons.setReasonDescription(cursor.getString(cursor.getColumnIndex(db.KEY_REASON_DESCRIPTION)));
                    if(reasons.getReasonID().contains("ZB")){
                        reasonsList.add(reasons);
                    }
                    //reasonsList.add(reasons);
                }
                while (cursor.moveToNext());
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            myAdapter.notifyDataSetChanged();
        }
    }
    public class loadBadReturns extends AsyncTask<Void, Void, Void> {
        private String orderID;
        private loadBadReturns(String orderID) {
            SalesInvoiceActivity.tab_position = 3;
            this.orderID = orderID;
            execute();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
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
                map.put(db.KEY_PRICECASE, "");
                map.put(db.KEY_PRICEPCS, "");
                map.put(db.KEY_ORDER_ID, "");
                map.put(db.KEY_PURCHASE_NUMBER, "");
                map.put(db.KEY_IS_POSTED, "");
                map.put(db.KEY_IS_PRINTED, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_ORDER_ID, orderID);
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                filter.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                filter.put(db.KEY_REASON_TYPE, App.BAD_RETURN);
                Cursor cursor = db.getData(db.RETURNS, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    setProductListBR(cursor);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            listSales.setAdapter(null);
            adapter = new SalesInvoiceAdapter(getActivity(), arrProductList);
            listSales.setAdapter(adapter);
            calculateCost();
        }
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
    private void calculateCost() {
        try{
            int salesTotal = 0;
            int pcsTotal = 0;
            double total = 0;
            for (Sales sale : arrProductList) {
                double itemPrice = 0;
                /*if (sale.getUom().equals(App.CASE_UOM) || sale.getUom().equals(App.CASE_UOM_NEW) || sale.getUom().equals(App.BOTTLES_UOM)) {
                    itemPrice = Double.parseDouble(sale.getCases()) * Double.parseDouble(sale.getPrice());
                } *//*else if (sale.getUom().equals(App.BOTTLES_UOM)) {
                itemPrice = Double.parseDouble(sale.getPic()) * Double.parseDouble(sale.getPrice());
            }*/

                if(Double.parseDouble(sale.getPic()) >= 1){
                    itemPrice =itemPrice+ Double.parseDouble(sale.getPic())*Double.parseDouble(sale.getPricepcs());
                }
                if(Double.parseDouble(sale.getCases()) >= 1){
                    Double casePrice = Double.parseDouble(sale.getPricecase());
                    itemPrice =itemPrice+ Double.parseDouble(sale.getCases())*casePrice;
                }

                total += itemPrice;
                salesTotal = salesTotal + Integer.parseInt(sale.getCases());
                pcsTotal = pcsTotal + Integer.parseInt(sale.getPic());
            }
            Const.brBundle = new Bundle();
            Const.brBundle.putParcelableArrayList("br", arrProductList);
            TextView tv = (TextView) viewmain.findViewById(R.id.tv_amt);
            tv.setText(String.format("%.2f",total+total*Double.parseDouble(Settings.getString(App.VATValue))));
            TextView tvsales = (TextView) viewmain.findViewById(R.id.tv_sales_qty);
            tvsales.setText(salesTotal + "/" + pcsTotal);
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    private void setProductListBR(Cursor cursor) {
        try{
            Cursor grCursor = cursor;
            grCursor.moveToFirst();
            do {
                Sales sale = new Sales();
                sale.setMaterial_no(grCursor.getString(grCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                sale.setItem_code(grCursor.getString(grCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                sale.setUom(grCursor.getString(grCursor.getColumnIndex(db.KEY_UOM)));
                sale.setName(UrlBuilder.decodeString(grCursor.getString(grCursor.getColumnIndex(db.KEY_MATERIAL_DESC1))));
                sale.setCases(grCursor.getString(grCursor.getColumnIndex(db.KEY_CASE)));
                sale.setPic(grCursor.getString(grCursor.getColumnIndex(db.KEY_UNIT)));
                sale.setReasonCode(grCursor.getString(grCursor.getColumnIndex(db.KEY_REASON_CODE)));

                String EditPrice = cursor.getString(cursor.getColumnIndex(db.KEY_PRICEPCS));
                String EditPriceCases = cursor.getString(cursor.getColumnIndex(db.KEY_PRICECASE));

                HashMap<String, String> filterPart = new HashMap<>();
                filterPart.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_AMOUNT, "");
                map.put(db.KEY_AMOUNTMIN,"");
                map.put(db.KEY_AMOUNTMAX,"");
                if (db.checkData(db.PRICING, filterPart)) {
                    //Pricing exists for Product for customer
                    //Pricing exists for Product for customer
                    Cursor priceCursor = db.getData(db.PRICING, map, filterPart);
                    if (priceCursor.getCount() > 0) {
                        priceCursor.moveToFirst();
                        String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                        sale.setPrice(sale.getUom().equals(App.CASE_UOM) ? String.valueOf(Float.parseFloat(price) * 10) : price);
                        price = !price.equals(EditPrice)?EditPrice:price;
                        sale.setPricepcs(sale.getUom().equals(App.CASE_UOM) ? String.valueOf(Float.parseFloat(price) * 10) : price);
                        sale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                        sale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                    }
                } else {
                    sale.setPrice("0");
                    sale.setPricemin("0");
                    sale.setPricemax("0");
                    sale.setPrice(EditPrice);
                }
                HashMap<String, String> altMap = new HashMap<>();
                altMap.put(db.KEY_DENOMINATOR, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_MATERIAL_NO, grCursor.getString(grCursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter);
                if (altUOMCursor.getCount() > 0) {
                    altUOMCursor.moveToFirst();
                    sale.setDenominator(""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR)));
                } else {
                    sale.setDenominator("0");
                }

                try {
                    Double casePrice = Double.parseDouble(sale.getPrice()) * Double.parseDouble(sale.getDenominator());
                    String price = String.format("%.2f", casePrice);
                    price = !price.equals(EditPriceCases)?EditPriceCases:price;
                    sale.setPricecase(price);
                }catch (Exception e){
                    e.printStackTrace();
                }


                arrProductList.add(sale);
            }
            while (grCursor.moveToNext());
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }
    private void setBadReturns(ArrayList<Sales> arrayList) {
        adapter = new SalesInvoiceAdapter(getActivity(), arrayList);
//        Const.brBundle = new Bundle();
//        Const.brBundle.putParcelableArrayList("br", arrProductList);
        listSales.setAdapter(adapter);
        calculateCost();
    }
}
