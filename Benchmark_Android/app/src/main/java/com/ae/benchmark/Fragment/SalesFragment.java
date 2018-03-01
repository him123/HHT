package com.ae.benchmark.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.adapters.SalesAdapter;
import com.ae.benchmark.adapters.SalesInvoiceAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.utils.ConfigStore;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.GPSTracker;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.LoadingSpinner;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by eheuristic on 12/5/2016.
 */
public class SalesFragment extends Fragment {
    View viewmain;
    ListView listSales;
    public static SalesAdapter adapter;
    public static ArrayAdapter<Sales> myAdapter;
    public static ArrayList<Sales> salesarrayList;
    private ArrayList<Sales>goodsReturnList = new ArrayList<>();
    private ArrayList<Sales>badReturnList = new ArrayList<>();
    private ArrayList<Sales>focList = new ArrayList<>();
    FloatingActionButton fab;
    TextView tv_available_limit;
    FloatingActionButton add;
    boolean workStarted = false;
    DatabaseHandler db;
    Customer object;
    public ArrayList<ArticleHeader> articles;
    String orderID="";
    LoadingSpinner loadingSpinner;
    public static ArrayList<Sales> focArrayList = new ArrayList<>();
    boolean focValid = true;
    GPSTracker gps;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

    }

    double limit = 0;
    boolean isLimitAvailable = true;
    double totalInvoiceAmount = 0;
    public static String aviLimit = "0";
    public static String paymentMethod = "";

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try{
            if(savedInstanceState!=null){
                Log.e("i am here","here");
                ArrayList<Sales> temp = new ArrayList<>();
                salesarrayList = savedInstanceState.getParcelableArrayList("si");
                try{
                    goodsReturnList = Const.grBundle.getParcelableArrayList("gr");
                    badReturnList = Const.brBundle.getParcelableArrayList("br");
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                Log.e("GR Count in SI","" + goodsReturnList.size());
                Log.e("BR Count in SI","" + badReturnList.size());
                setLoadData(salesarrayList);
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
            Log.e("Step0", "Step0");
            outState.putParcelableArrayList("si", salesarrayList);
            Const.siBundle = new Bundle();
            Const.siBundle.putParcelableArrayList("si", salesarrayList);
            Const.customer = object;
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
            loadingSpinner = new LoadingSpinner(getActivity());
            gps=new GPSTracker(getActivity());

            paymentMethod = object.getPaymentMethod();

            Log.e("Sales Frag", "" + object.getCustomerID());

            db = new DatabaseHandler(getActivity());
            Activity activity = getActivity();
            //Log.e("Activity", "" + activity);
            articles = new ArrayList<>();
            articles = ArticleHeaders.get();
            listSales = (ListView) viewmain.findViewById(R.id.list_sales);
            tv_available_limit = (TextView) viewmain.findViewById(R.id.tv_available_limit);
            tv_available_limit.setText("");
            fab = (FloatingActionButton) viewmain.findViewById(R.id.fab);
            fab.setVisibility(View.GONE);
            fab.setImageDrawable(getResources().getDrawable(R.drawable.btn_select_all));
            fab.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_select_all));
            //fab.hide();
            salesarrayList = new ArrayList<>();

            listSales.setAdapter(null);
            myAdapter = new SalesInvoiceAdapter(getActivity(), salesarrayList);
            listSales.setAdapter(myAdapter);


                if(Const.siBundle!=null){
                    Log.e("i am here","here");
                    try {
                        salesarrayList = Const.siBundle.getParcelableArrayList("si");
                    }catch (Exception e){
                        salesarrayList = new ArrayList<>();
                        e.printStackTrace();
                    }
                    try {
                        if (salesarrayList.size() > 0) {
                            listSales.setAdapter(null);
                            myAdapter = new SalesInvoiceAdapter(getActivity(), salesarrayList);
                            listSales.setAdapter(myAdapter);
                            setLoadData(salesarrayList);
                        }
                    }catch (Exception e){
                        salesarrayList = new ArrayList<>();
                        new loadItems("");
                        e.printStackTrace();
                    }
                }
                else{


                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    filter.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                    if(db.checkData(db.CAPTURE_SALES_INVOICE,filter)){
                        HashMap<String,String>map = new HashMap<>();
                        map.put(db.KEY_ORDER_ID,"");
                        Cursor cursor = db.getData(db.CAPTURE_SALES_INVOICE,map,filter);
                        if(cursor.getCount()>0){
                            cursor.moveToFirst();
                            orderID = cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID));
                            Log.e("ORDER ID - SI","" + orderID);
                            new loadItems(cursor.getString(cursor.getColumnIndex(db.KEY_ORDER_ID)));
                        }
                    }else{
                        new loadItems("");
                    }
                }


            if(!App.CustomerRouteControl.isEnableIVCopy()){
                listSales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getActivity(), getString(R.string.feature_blocked), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                listSales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                        TextView item = (TextView) view.findViewById(R.id.tv_item_code);
                        int pos = position;
                        for(Sales data : salesarrayList){

                            if(item.getText().toString().equals(data.getMaterial_no())){
                                pos = salesarrayList.indexOf(data);
                            }
                        }

                        final Sales sales = salesarrayList.get(pos);
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_with_crossbutton_editprice);
                        dialog.setCancelable(false);
                        TextView tv = (TextView) dialog.findViewById(R.id.dv_title);
                        final TextView tv_Vat = (TextView) dialog.findViewById(R.id.tv_Total);
                        tv.setText(sales.getName());
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        ImageView iv_cancle = (ImageView) dialog.findViewById(R.id.imageView_close);
                        Button btn_save = (Button) dialog.findViewById(R.id.btn_save);
                        final EditText ed_cases = (EditText) dialog.findViewById(R.id.ed_cases);
                        final EditText ed_pcs = (EditText) dialog.findViewById(R.id.ed_pcs);
                        final EditText ed_cases_inv = (EditText) dialog.findViewById(R.id.ed_cases_inv);
                        final EditText ed_pcs_inv = (EditText) dialog.findViewById(R.id.ed_pcs_inv);
                        final EditText ed_cases_price = (EditText) dialog.findViewById(R.id.ed_cases_price);
                        final EditText ed_pcs_price = (EditText) dialog.findViewById(R.id.ed_pcs_price);

                        RelativeLayout rl_specify=(RelativeLayout)dialog.findViewById(R.id.rl_specify_reason);
                        rl_specify.setVisibility(View.GONE);

                        // ed_cases_inv.setText(sales.getInv_cases());
                        //  ed_pcs_inv.setText(sales.getInv_piece());

                        /*String strpc="",strcase="0.00";
                        strpc = ""+sales.getPrice();
                        try {
                            Double casePrice = Double.parseDouble(sales.getPrice()) * Double.parseDouble(sales.getDenominator());
                            strcase = "" + String.format("%.2f", casePrice);
                        }catch (Exception e){
                            e.printStackTrace();
                        }*/
                        ed_cases_price.setText(sales.getPricecase());
                        ed_pcs_price.setText(sales.getPricepcs());

                        ed_cases_inv.setText(Double.parseDouble(sales.getInv_cases())<0?"0":sales.getInv_cases());
                        ed_pcs_inv.setText(Double.parseDouble(sales.getInv_piece())<0?"0":sales.getInv_piece());

                        ed_cases_inv.setEnabled(false);
                        ed_pcs_inv.setEnabled(false);
//                        if (sales.isAltUOM()) {
//                            ed_pcs.setEnabled(true);
//                        } else {
//                            ed_pcs.setEnabled(false);
//                        }


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
                                    ed_pcs_price.setText(String.format("%.2f",Price));

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
//                                    Log.e("Price", "" + Price);
                                    ed_cases_price.setText(String.format("%.2f",Price));

                                    Double Vat = calculateVatTotal(Double.parseDouble(ed_cases.getText().toString().equals("") ? "0" : ed_cases.getText().toString()), Double.parseDouble(ed_cases_price.getText().toString().equals("") ? "0" : ed_cases_price.getText().toString()), Double.parseDouble(ed_pcs.getText().toString().equals("") ? "0" : ed_pcs.getText().toString()), Double.parseDouble(ed_pcs_price.getText().toString().equals("") ? "0" : ed_pcs_price.getText().toString()));
                                    tv_Vat.setText("Total = " + String.format("%.2f", Vat));

                                }
                            }
                        });




                /*if(sales.getUom().equals(App.BOTTLES_UOM)){
                    ed_cases.setEnabled(false);
                }
                else if(sales.getUom().equals(App.CASE_UOM)||sales.getUom().equals(App.CASE_UOM_NEW)){
                    ed_pcs.setEnabled(false);
                }*/
                        ed_cases.setText(sales.getCases().trim().equals("0")?"":sales.getCases());
                        ed_pcs.setText(sales.getPic().trim().equals("0")?"":sales.getPic());
                        LinearLayout ll_1 = (LinearLayout) dialog.findViewById(R.id.ll_1);
                        iv_cancle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                        btn_save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String strCase = ed_cases.getText().toString();
                                String strpcs = ed_pcs.getText().toString();
                                String strCasePrice = ed_cases_price.getText().toString();
                                String strpcsPrice = ed_pcs_price.getText().toString();
                                String strcaseinv = ed_cases_inv.getText().toString();
                                String strpcsinv = ed_pcs_inv.getText().toString();
                                TextView tv_cases = (TextView) view.findViewById(R.id.tv_cases_value);
                                TextView tv_pcs = (TextView) view.findViewById(R.id.tv_pcs_value);
                                //tv_cases.setText(strCase);
                                //tv_pcs.setText(strpcs);



                                tv_cases.setText(strCase.trim().equals("")?"0":strCase);
                                tv_pcs.setText(strpcs.trim().equals("")?"0":strpcs);


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

                                Double Pcs = Double.parseDouble(sales.getInv_piece());
                                Double finalEnter = Double.parseDouble(strCase) * Double.parseDouble(sales.getDenominator()) + Double.parseDouble(strpcs);
                                double totalMain = 0;
                                for(Sales sale:salesarrayList){
                                    double itemPrice = 0;
                                    if(!sales.getItem_code().equals(sale.getItem_code())) {
                                        if (Double.parseDouble(sale.getPic()) >= 1) {
                                            itemPrice = itemPrice + Double.parseDouble(sale.getPic()) * Double.parseDouble(sale.getPricepcs());
                                        }
                                        if (Double.parseDouble(sale.getCases()) >= 1) {
                                            Double casePrice = Double.parseDouble(sale.getPricecase());
                                            itemPrice = itemPrice + Double.parseDouble(sale.getCases()) * casePrice;
                                        }
                                    }else{
                                        if (Double.parseDouble(strpcs) >= 1) {
                                            itemPrice = itemPrice + Double.parseDouble(strpcs) * Double.parseDouble(strpcsPrice);
                                        }
                                        if (Double.parseDouble(strCase) >= 1) {
                                            Double casePrice = Double.parseDouble(strCasePrice);
                                            itemPrice = itemPrice + Double.parseDouble(strCase) * casePrice;
                                        }
                                    }

                                    totalMain+=itemPrice;
                                }
                                if (paymentMethod.equalsIgnoreCase(App.CASH_CUSTOMER)) {

                                }else if (Double.parseDouble(aviLimit) <= (totalMain+totalMain*Double.parseDouble(Settings.getString(App.VATValue)))) {
                                    ed_cases.setText("0");
                                    ed_pcs.setText("0");
                                    tv_cases.setText("0");
                                    tv_pcs.setText("0");
                                    Toast.makeText(getActivity(),""+getResources().getString(R.string.pending_invoice),Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if((Double.parseDouble(sales.getPricemin()) > Double.parseDouble(strpcsPrice))){
                                    Toast.makeText(getActivity(),"You can not sale item below PCS Price: "+sales.getPricemin(),Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if((Double.parseDouble(sales.getPricemax()) < Double.parseDouble(strpcsPrice))){
                                    Toast.makeText(getActivity(),"You can not sale item above PCS Price: "+sales.getPricemax(),Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (Float.parseFloat(strCase) > Float.parseFloat(strcaseinv)) {
                                    Toast.makeText(getActivity(), getString(R.string.input_larger), Toast.LENGTH_SHORT).show();
                                    strCase = "0";
                                    ed_cases.setText("0");
                                    sales.setCases(strCase);
                                    sales.setPricepcs(strpcsPrice);
                                    sales.setPricecase(strCasePrice);
                                    myAdapter.notifyDataSetChanged();
                                } else if (Float.parseFloat(strpcs) > Float.parseFloat(strpcsinv)) {
                                    Toast.makeText(getActivity(), getString(R.string.input_larger), Toast.LENGTH_SHORT).show();
                                    ed_pcs.setText("0");
                                    strpcs = "0";
                                    sales.setPic(strpcs);
                                    sales.setPricepcs(strpcsPrice);
                                    sales.setPricecase(strCasePrice);
                                    myAdapter.notifyDataSetChanged();
                                }else if(finalEnter > Pcs) {
                                    Toast.makeText(getActivity(), getString(R.string.input_larger), Toast.LENGTH_SHORT).show();
                                    strCase = "0";
                                    ed_cases.setText("0");
                                    sales.setCases(strCase);
                                    sales.setPricepcs(strpcsPrice);
                                    sales.setPricecase(strCasePrice);
                                    myAdapter.notifyDataSetChanged();
                                }else {
                                    int salesTotal = 0;
                                    int pcsTotal = 0;
                                    double total = 0;
                                    sales.setCases(strCase);
                                    sales.setPic(strpcs);
                                    sales.setPricepcs(strpcsPrice);
                                    sales.setPricecase(strCasePrice);
                                    for(Sales sale:salesarrayList){
                                        double itemPrice = 0;
                                        if(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.CASE_UOM_NEW)||sale.getUom().equals(App.BOTTLES_UOM)){
                                            itemPrice = Double.parseDouble(sale.getCases())*Double.parseDouble(sale.getPrice());
                                        }
                                /*else if(sale.getUom().equals(App.BOTTLES_UOM)){
                                    itemPrice = Double.parseDouble(sale.getPic())*Double.parseDouble(sale.getPrice());
                                }*/
                                        total+=itemPrice;
                                        salesTotal = salesTotal + Integer.parseInt(sale.getCases());
                                        pcsTotal = pcsTotal + Integer.parseInt(sale.getPic());
                                    }
                                    TextView tv = (TextView) viewmain.findViewById(R.id.tv_amt);
                                    tv.setText(String.format("%.2f",total));
                                    TextView tvsales = (TextView) viewmain.findViewById(R.id.tv_sales_qty);
                                    tvsales.setText(salesTotal + "/" + pcsTotal);
                                    calculateCost();
                                    //setFOC(salesarrayList);
                                    dialog.dismiss();
                                }


                            }
                        });
                    }
                });
            }
            if (!paymentMethod.equalsIgnoreCase(App.CASH_CUSTOMER)) {
                calculateAvailableLimit();
            }

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{

                        if (paymentMethod.equalsIgnoreCase(App.CASH_CUSTOMER)) {

                        }else{
                            if (Double.parseDouble(aviLimit) <= Double.parseDouble(((TextView) viewmain.findViewById(R.id.tv_amt)).getText().toString())) {
                                Toast.makeText(getActivity(),""+getResources().getString(R.string.pending_invoice),Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        if(focValid){
                            if(orderID.equals("")||orderID==null){
                                boolean value = false;
                                //To check if invoice doesnt contain any data
                                if(salesarrayList.size()>0){
                                    for (Sales sale : salesarrayList){
                                        if(Float.parseFloat(sale.getCases())>0||Float.parseFloat(sale.getPic())>0){
                                            value = true;
                                            break;
                                        }
                                    }
                                }
                                String purchaseNumber = "";
                                if(value){
                                    purchaseNumber = Helpers.generateNumber(db, ConfigStore.InvoiceRequest_PR_Type);
                                }

                                if(salesarrayList.size()>0){
                                    if(value){
                                        for (Sales sale : salesarrayList) {
                                            HashMap<String, String> map = new HashMap<>();
                                            map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                            map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                            map.put(db.KEY_ITEM_NO, sale.getItem_code());
                                            map.put(db.KEY_ITEM_CATEGORY, sale.getItem_category());
                                            map.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                                            map.put(db.KEY_MATERIAL_GROUP, "");
                                            map.put(db.KEY_MATERIAL_DESC1,sale.getName());
                                            map.put(db.KEY_ORG_CASE, sale.getCases());
                                            map.put(db.KEY_UOM,sale.getUom());
                                            map.put(db.KEY_ORG_UNITS, sale.getPic());
                                            map.put(db.KEY_AMOUNT, sale.getPrice());
                                            map.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                            map.put(db.KEY_IS_PRINTED,App.DATA_NOT_POSTED);
                                            map.put(db.KEY_ORDER_ID, purchaseNumber);
                                            map.put(db.KEY_PURCHASE_NUMBER, purchaseNumber);
                                            if(Float.parseFloat(sale.getCases())>0||Float.parseFloat(sale.getPic())>0){
                                                db.addData(db.CAPTURE_SALES_INVOICE, map);
                                            }
                                        }
                                        if(focArrayList.size()>0){
                                            for(Sales sale:focArrayList){
                                                HashMap<String,String>map = new HashMap<String, String>();
                                                map.put(db.KEY_TIME_STAMP,Helpers.getCurrentTimeStamp());
                                                map.put(db.KEY_TRIP_ID,"");
                                                map.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                                                map.put(db.KEY_ORDER_ID,purchaseNumber);
                                                map.put(db.KEY_PURCHASE_NUMBER,purchaseNumber);
                                                map.put(db.KEY_ITEM_NO, sale.getItem_code());
                                                map.put(db.KEY_ITEM_CATEGORY, sale.getItem_category());
                                                map.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                                                map.put(db.KEY_MATERIAL_GROUP, "");
                                                map.put(db.KEY_MATERIAL_DESC1,sale.getName());
                                                map.put(db.KEY_ORG_CASE, sale.getCases());
                                                map.put(db.KEY_UOM,sale.getUom());
                                                map.put(db.KEY_ORG_UNITS, sale.getPic());
                                                map.put(db.KEY_AMOUNT, sale.getPrice());
                                                map.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                                map.put(db.KEY_IS_PRINTED,App.DATA_NOT_POSTED);
                                                if(Float.parseFloat(sale.getCases())>0||Float.parseFloat(sale.getPic())>0){
                                                    db.addData(db.FOC_INVOICE, map);
                                                }
                                            }
                                        }
                                    }
                                }
                                if(goodsReturnList.size()>0){
                                    String grPRNo= "";
                                    if(value){
                                        grPRNo = purchaseNumber;
                                    }
                                    else{
                                        grPRNo = Helpers.generateNumber(db, ConfigStore.GoodReturns_PR_Type);
                                    }
                                    for(Sales sale:goodsReturnList){
                                        HashMap<String, String> map = new HashMap<>();
                                        map.put(db.KEY_TIME_STAMP,Helpers.getCurrentTimeStamp());
                                        map.put(db.KEY_TRIP_ID, Settings.getString(App.TRIP_ID));
                                        map.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                                        map.put(db.KEY_REASON_TYPE,App.GOOD_RETURN);
                                        map.put(db.KEY_REASON_CODE,sale.getReasonCode());
                                        map.put(db.KEY_ITEM_NO,sale.getItem_code());
                                        map.put(db.KEY_MATERIAL_DESC1,sale.getName());
                                        map.put(db.KEY_MATERIAL_NO,sale.getMaterial_no());
                                        map.put(db.KEY_MATERIAL_GROUP,"");
                                        map.put(db.KEY_CASE,sale.getCases());
                                        map.put(db.KEY_UNIT,sale.getPic());
                                        map.put(db.KEY_UOM,sale.getUom());
                                        map.put(db.KEY_PRICE,sale.getPrice());
                                        map.put(db.KEY_ORDER_ID,grPRNo);
                                        map.put(db.KEY_PURCHASE_NUMBER,grPRNo);
                                        map.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                        map.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                                        if(Float.parseFloat(sale.getCases())>0||Float.parseFloat(sale.getPic())>0){
                                            db.addData(db.RETURNS, map);
                                        }
                                    }
                                }
                                if(badReturnList.size()>0){
                                    String brPRNo= "";
                                    if(value){
                                        brPRNo = purchaseNumber;
                                    }
                                    else{
                                        brPRNo = Helpers.generateNumber(db, ConfigStore.GoodReturns_PR_Type);
                                    }
                                    for (Sales sale : badReturnList) {
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
                                        map.put(db.KEY_ORDER_ID, brPRNo);
                                        map.put(db.KEY_PURCHASE_NUMBER, brPRNo);
                                        map.put(db.KEY_IS_POSTED, App.DATA_NOT_POSTED);
                                        map.put(db.KEY_IS_PRINTED, App.DATA_NOT_POSTED);
                                        if(Float.parseFloat(sale.getCases())>0||Float.parseFloat(sale.getPic())>0){
                                            db.addData(db.RETURNS, map);
                                        }
                                    }
                                }
                            }
                            else{
                                for (Sales sale : salesarrayList) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                    map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                    map.put(db.KEY_ITEM_NO, sale.getItem_code());
                                    map.put(db.KEY_ITEM_CATEGORY, sale.getItem_category());
                                    map.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                                    map.put(db.KEY_MATERIAL_GROUP, "");
                                    map.put(db.KEY_MATERIAL_DESC1,sale.getName());
                                    map.put(db.KEY_ORG_CASE, sale.getCases());
                                    map.put(db.KEY_UOM,sale.getUom());
                                    map.put(db.KEY_ORG_UNITS, sale.getPic());
                                    map.put(db.KEY_AMOUNT, sale.getPrice());
                                    map.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                    map.put(db.KEY_IS_PRINTED,App.DATA_NOT_POSTED);
                                    map.put(db.KEY_ORDER_ID,orderID);
                                    map.put(db.KEY_PURCHASE_NUMBER,orderID);
                                    map.put(db.KEY_LATITUDE, String.valueOf(gps.getLatitude()));
                                    map.put(db.KEY_LONGITUDE, String.valueOf(gps.getLongitude()));
                                    HashMap<String, String> filter = new HashMap<>();
                                    filter.put(db.KEY_PURCHASE_NUMBER,orderID);
                                    filter.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                    filter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                                    filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                                    if(db.checkData(db.CAPTURE_SALES_INVOICE,filter)){
                                        db.updateData(db.CAPTURE_SALES_INVOICE, map,filter);
                                        if((Float.parseFloat(sale.getCases()) == 0 || Float.parseFloat(sale.getPic())==0)){
                                            HashMap<String, String> focFilter = new HashMap<>();
                                            focFilter.put(db.KEY_PURCHASE_NUMBER,orderID);
                                            focFilter.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                            focFilter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                                            focFilter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                                            db.deleteData(db.FOC_INVOICE,focFilter);
                                        }
                                    }
                                    else{
                                        //db.addData(db.CAPTURE_SALES_INVOICE,map);
                                        if(Float.parseFloat(sale.getCases())>0||Float.parseFloat(sale.getPic())>0){
                                            db.addData(db.CAPTURE_SALES_INVOICE, map);
                                        }
                                    }
                                }
                                if(focArrayList.size()>0){
                                    for (Sales sale : focArrayList) {
                                        HashMap<String, String> map = new HashMap<>();
                                        map.put(db.KEY_TIME_STAMP, Helpers.getCurrentTimeStamp());
                                        map.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                        map.put(db.KEY_ITEM_NO, sale.getItem_code());
                                        map.put(db.KEY_ITEM_CATEGORY, sale.getItem_category());
                                        map.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                                        map.put(db.KEY_MATERIAL_GROUP, "");
                                        map.put(db.KEY_MATERIAL_DESC1,sale.getName());
                                        map.put(db.KEY_ORG_CASE, sale.getCases());
                                        map.put(db.KEY_UOM,sale.getUom());
                                        map.put(db.KEY_ORG_UNITS, sale.getPic());
                                        map.put(db.KEY_AMOUNT, sale.getPrice());
                                        map.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                        map.put(db.KEY_IS_PRINTED,App.DATA_NOT_POSTED);
                                        map.put(db.KEY_ORDER_ID,orderID);
                                        map.put(db.KEY_PURCHASE_NUMBER,orderID);
                                        HashMap<String, String> filter = new HashMap<>();
                                        filter.put(db.KEY_PURCHASE_NUMBER,orderID);
                                        filter.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);
                                        filter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                                        filter.put(db.KEY_MATERIAL_NO, sale.getMaterial_no());
                                        if(db.checkData(db.FOC_INVOICE,filter)){
                                            db.updateData(db.FOC_INVOICE, map,filter);
                                        }
                                        else{
                                            db.addData(db.FOC_INVOICE,map);
                                        }
                                    }
                                }
                            }
                            //  Const.salesarrayList = salesarrayList;

                            getActivity().finish();
                        }
                        else{
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle(getString(R.string.message))
                                    .setMessage("Not enough quantity to give as free goods with quantity in van." +
                                            "Please reduce sale quantity to accomodate the free goods.")
                                    .setCancelable(false)
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            // show it
                            alertDialog.show();
                        }

                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return viewmain;
    }

    private void calculateAvailableLimit() {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_CUSTOMER_NO, "");
            map.put(db.KEY_CREDIT_LIMIT, "");
            map.put(db.KEY_CREDIT_DAYS, "");
            map.put(db.KEY_RECEIVABLES, "");
            HashMap<String, String> filters = new HashMap<>();
            filters.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
            Cursor cursor = db.getData(db.CUSTOMER_CREDIT, map, filters);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                try {
                    limit = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_CREDIT_LIMIT)));
                }catch (Exception e){
                    e.printStackTrace();
                }
                HashMap<String, String> map1 = new HashMap<>();
                map1.put(db.KEY_CUSTOMER_NO, "");
                map1.put(db.KEY_INVOICE_NO, "");
                map1.put(db.KEY_INVOICE_AMOUNT, "");
                map1.put(db.KEY_DUE_DATE, "");
                map1.put(db.KEY_INVOICE_DATE, "");
                map1.put(db.KEY_AMOUNT_CLEARED, "");
                map1.put(db.KEY_IS_INVOICE_COMPLETE, "");
                HashMap<String, String> filter = new HashMap<>();
                filter.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                Cursor c = db.getData(db.COLLECTION, map1, filter);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    do {
                        totalInvoiceAmount += Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_INVOICE_AMOUNT)));
                    }
                    while (c.moveToNext());
                }
                Log.e("Total Invoice", "" + totalInvoiceAmount);
                double availableLimit = 0.0;
                try{
                    availableLimit = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_RECEIVABLES)));
                }catch (Exception e){
                    e.printStackTrace();
                }
                /*if (limit - totalInvoiceAmount == 0) {
                    isLimitAvailable = false;
                }*/
                if ((limit - availableLimit - totalInvoiceAmount) <= 0) {
                    isLimitAvailable = false;
                }
                aviLimit = ""+(limit - availableLimit - totalInvoiceAmount);
                tv_available_limit.setText(String.format( "%.2f",(limit - availableLimit - totalInvoiceAmount)));
            } else {
                aviLimit = "0";
                isLimitAvailable = true;
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
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

    private void calculateCost(){
        try{
            int salesTotal = 0;
            int pcsTotal = 0;
            double total = 0;
            for(Sales sale:salesarrayList){
                double itemPrice = 0;
//                if(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.CASE_UOM_NEW) || sale.getUom().equals(App.BOTTLES_UOM)){

//                }
            /*else if(sale.getUom().equals(App.BOTTLES_UOM)){
                itemPrice = Double.parseDouble(sale.getPic())*Double.parseDouble(sale.getPrice());
            }*/

                if(Double.parseDouble(sale.getPic()) >= 1){
                    itemPrice =itemPrice+ Double.parseDouble(sale.getPic())*Double.parseDouble(sale.getPricepcs());
                }
                if(Double.parseDouble(sale.getCases()) >= 1){
                    Double casePrice = Double.parseDouble(sale.getPricecase());
                    itemPrice =itemPrice+ Double.parseDouble(sale.getCases())*casePrice;
                }


                total+=itemPrice;
                salesTotal = salesTotal + Integer.parseInt(sale.getCases());
                pcsTotal = pcsTotal + Integer.parseInt(sale.getPic());
            }

            Const.siBundle = new Bundle();
            Const.siBundle.putParcelableArrayList("si",salesarrayList);

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
    private class loadItems extends AsyncTask<Void, Void, Void> {
        private String orderID;
        private loadItems(String orderID) {
            this.orderID = orderID;
            execute();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingSpinner.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                HashMap<String, String> map = new HashMap<>();
                map.put(db.KEY_DELIVERY_NO, "");
                map.put(db.KEY_ITEM_NO, "");
                map.put(db.KEY_ITEM_CATEGORY, "");
                map.put(db.KEY_MATERIAL_NO, "");
                map.put(db.KEY_MATERIAL_DESC1,"");
                map.put(db.KEY_ACTUAL_QTY_CASE, "");
                map.put(db.KEY_REMAINING_QTY_CASE, "");
                map.put(db.KEY_ACTUAL_QTY_UNIT, "");
                map.put(db.KEY_REMAINING_QTY_UNIT, "");
                map.put(db.KEY_UOM_CASE, "");
                map.put(db.KEY_UOM_UNIT, "");
                map.put(db.KEY_IS_VERIFIED, "");
                map.put(db.KEY_RESERVED_QTY_CASE, "");
                map.put(db.KEY_RESERVED_QTY_UNIT, "");
                HashMap<String, String> filter = new HashMap<>();
                Cursor cursor = db.getData(db.VAN_STOCK_ITEMS, map, filter);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    if(this.orderID.equals("")||this.orderID==null){
                        setLoadItems(cursor, false, "");
                    }
                    else{
                        setLoadItems(cursor, true, this.orderID);
                    }

                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }


            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            // adapter.notifyDataSetChanged();
            try{
                if(loadingSpinner.isShowing()){
                    loadingSpinner.hide();
                }
                listSales.setAdapter(null);
                myAdapter = new SalesInvoiceAdapter(getActivity(), salesarrayList);
                listSales.setAdapter(myAdapter);

                calculateCost();
                //setFOC(salesarrayList);
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }
    private void setLoadItems(Cursor loadItems,boolean isSaved,String orderID) {
        try{
            workStarted = true;
            salesarrayList.clear();
            Cursor cursor = loadItems;
            cursor.moveToFirst();
            do {
                try {
                    LoadSummary loadItem = new LoadSummary();
                    Sales product = new Sales();
                    product.setItem_code(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NO)));
                    product.setItem_category(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_CATEGORY)));
                    product.setMaterial_no(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    product.setMaterial_description(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_DESC1)));
                    ArticleHeader article = ArticleHeader.getArticle(articles, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    //Log.e("Article IF", "" + article);
                    if (!(article == null)) {
                        loadItem.setItemDescription(UrlBuilder.decodeString(article.getMaterialDesc1()));
                        product.setName(UrlBuilder.decodeString(article.getMaterialDesc1()));
                        product.setUom(article.getBaseUOM());
                    } else {
                        loadItem.setItemDescription(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        product.setName(cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                        product.setUom(cursor.getString(cursor.getColumnIndex(db.KEY_UOM_CASE)));
                    }

                    String totalcase = String.valueOf(Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_CASE)))+
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE))));
                    String totalpiece = String.valueOf(Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_RESERVED_QTY_UNIT)))+
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_UNIT))));

                    product.setInv_cases( totalcase );
                    product.setInv_piece( totalpiece );
//                    product.setInv_piece("0");
                    product.setCases("0");
                    product.setPic("0");


                    HashMap<String, String> altMap = new HashMap<>();
                    altMap.put(db.KEY_UOM, "");
                    altMap.put(db.KEY_DENOMINATOR, "");
                    HashMap<String, String> filter = new HashMap<>();
                    filter.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));
                    Cursor altUOMCursor = db.getData(db.ARTICLE_UOM, altMap, filter);
                    if (altUOMCursor.getCount() > 0) {
                        altUOMCursor.moveToFirst();
                        try {
                            if (cursor.getString(cursor.getColumnIndex(db.KEY_UOM_CASE)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))
                                    || cursor.getString(cursor.getColumnIndex(db.KEY_UOM_UNIT)).equals(altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_UOM)))) {
                                product.setIsAltUOM(false);
                            } else {
                                product.setIsAltUOM(true);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        product.setDenominator(""+altUOMCursor.getString(altUOMCursor.getColumnIndex(db.KEY_DENOMINATOR)));
                    } else {
                        product.setIsAltUOM(false);
                        product.setDenominator("0");
                    }

                    HashMap<String,String> filterComp = new HashMap<>();
                    filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                    filterComp.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                    HashMap<String,String> filterPart = new HashMap<>();
                    filterPart.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                    HashMap<String,String> map = new HashMap<>();
                    map.put(db.KEY_MATERIAL_NO,"");
                    map.put(db.KEY_AMOUNT,"");
                    map.put(db.KEY_AMOUNTMIN,"");
                    map.put(db.KEY_AMOUNTMAX,"");

                /*if(db.checkData(db.PRICING,filterComp)){
                    //Pricing exists for Product for customer
                    Cursor customerPriceCursor = db.getData(db.PRICING,map,filterComp);
                    if(customerPriceCursor.getCount()>0){
                        customerPriceCursor.moveToFirst();
                        String price = customerPriceCursor.getString(customerPriceCursor.getColumnIndex(db.KEY_AMOUNT));
                        product.setPrice(product.getUom().equals(App.CASE_UOM)?String.valueOf(Float.parseFloat(price)*10):price);
//                        product.setPrice(cursor.getString(cursor.getColumnIndex(db.KEY_UOM_CASE)).equals(App.CASE_UOM) ? String.valueOf(Float.parseFloat(price) * 10) : price);
                    }
                }
                else */

                    if(db.checkData(db.PRICING,filterComp)){
                        //Pricing exists for Product for customer
                        //Pricing exists for Product for customer
                        Cursor priceCursor = db.getData(db.PRICING,map,filterComp);
                        if(priceCursor.getCount()>0){
                            priceCursor.moveToFirst();
                            String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                            product.setPrice(product.getUom().equals(App.CASE_UOM)||product.getUom().equals(App.BOTTLES_UOM)?price:price);
                            product.setPricepcs(product.getUom().equals(App.CASE_UOM)||product.getUom().equals(App.BOTTLES_UOM)?price:price);

                            product.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                            product.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                        }
                    }
                    else{
                        HashMap<String,String> filterBase = new HashMap<>();
                        filterBase.put(db.KEY_PRIORITY, "2");
                        filterBase.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                        if(db.checkData(db.PRICING,filterBase)){
                            //Pricing exists for Product for customer
                            //Pricing exists for Product for customer
                            Cursor priceCursor = db.getData(db.PRICING,map,filterBase);
                            if(priceCursor.getCount()>0){
                                priceCursor.moveToFirst();
                                String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                product.setPrice(product.getUom().equals(App.CASE_UOM)||product.getUom().equals(App.BOTTLES_UOM)?price:price);
                                product.setPricepcs(product.getUom().equals(App.CASE_UOM)||product.getUom().equals(App.BOTTLES_UOM)?price:price);
                                product.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                product.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                            }
                        }else {
                            product.setPrice("0");
                            product.setPricemin("0");
                            product.setPricemax("0");
                            product.setPricepcs("0");
                        }
                    }
                    try {
                        Double casePrice = Double.parseDouble(product.getPrice()) * Double.parseDouble(product.getDenominator());
                        product.setPricecase(String.format("%.2f", casePrice));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    salesarrayList.add(product);
                } catch (Exception e) {
                    Log.e("Exception 1","Exception 1");
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
            while (cursor.moveToNext());
            if(isSaved){
                HashMap<String,String> altMap = new HashMap<>();
                altMap.put(db.KEY_ITEM_NO,"");
                altMap.put(db.KEY_ITEM_CATEGORY,"");
                altMap.put(db.KEY_MATERIAL_NO,"");
                altMap.put(db.KEY_MATERIAL_DESC1,"");
                altMap.put(db.KEY_MATERIAL_GROUP,"");
                altMap.put(db.KEY_ORG_CASE,"");
                altMap.put(db.KEY_ORG_UNITS,"");
                altMap.put(db.KEY_AMOUNT,"");
                altMap.put(db.KEY_AMOUNTPCS,"");
                altMap.put(db.KEY_AMOUNTCASE,"");
                altMap.put(db.KEY_UOM,"");
                HashMap<String,String>filterMap = new HashMap<>();
                filterMap.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                filterMap.put(db.KEY_PURCHASE_NUMBER,orderID);
                filterMap.put(db.KEY_IS_POSTED,App.DATA_NOT_POSTED);

                Cursor savedDataCursor = db.getData(db.CAPTURE_SALES_INVOICE,altMap,filterMap);
                if (savedDataCursor.getCount()>0){
                    savedDataCursor.moveToFirst();
                    do{
                        for(int i=0;i<salesarrayList.size();i++){
                            Sales sale = salesarrayList.get(i);
                            if(sale.getMaterial_no().equals(savedDataCursor.getString(savedDataCursor.getColumnIndex(db.KEY_MATERIAL_NO)))){
                                sale.setCases(savedDataCursor.getString(savedDataCursor.getColumnIndex(db.KEY_ORG_CASE)));
                                sale.setPic(savedDataCursor.getString(savedDataCursor.getColumnIndex(db.KEY_ORG_UNITS)));
                                sale.setPrice(savedDataCursor.getString(savedDataCursor.getColumnIndex(db.KEY_AMOUNT)));
                                sale.setPricepcs(savedDataCursor.getString(savedDataCursor.getColumnIndex(db.KEY_AMOUNTPCS)));
                                sale.setPricecase(savedDataCursor.getString(savedDataCursor.getColumnIndex(db.KEY_AMOUNTCASE)));
                            }
                            salesarrayList.remove(i);
                            salesarrayList.add(i,sale);
                        }
                    }
                    while (savedDataCursor.moveToNext());
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        // adapter.notifyDataSetChanged();
    }
    private void setLoadData(ArrayList<Sales>arrayList){
        calculateCost();
        //setFOC(arrayList);
    }
    private void setFOC(ArrayList<Sales> arrayList){
        focArrayList.clear();
        Const.focList.clear();
        boolean value = false;
        for(Sales sales:arrayList){
            if(Double.parseDouble(sales.getCases())>0||Double.parseDouble(sales.getPic())>0){
                value = true;
            }
        }
        if(value){
            try{
                //if(Settings.getString(App.DIST_CHANNEL).equals("30")||Settings.getString(App.DIST_CHANNEL).equals("20"))
                // Changes by Rakshit on 09/04/2017
                // Changes suggested by CK that for Wholesale if there is special price
                // for customer, do not go for FOC
                // Change no 20170409
                if(Settings.getString(App.DIST_CHANNEL).equals("50")){
                    //Logic to check if Customer has special Price
                    for(Sales sale:arrayList){
                        if(Double.parseDouble(sale.getCases())>0||Double.parseDouble(sale.getPic())>0){
                            //Changes to check if customer has special price, if yes dont calculate FOC

                            HashMap<String,String>priceCheckMap = new HashMap<>();
                            priceCheckMap.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                           /* priceCheckMap.put(db.KEY_MATERIAL_NO,sale.getMaterial_no());
                            priceCheckMap.put(db.KEY_PRIORITY,"1");*/

                            if(!db.checkData(db.SPECIAL_CUSTOMER,priceCheckMap)){

                                HashMap<String,String>map = new HashMap<>();
                                map.put(db.KEY_FOC_ASSIGNING_ITEM,"");
                                map.put(db.KEY_FOC_QUALIFYING_QUANTITY,"");
                                map.put(db.KEY_FOC_ASSIGNING_QUANTITY,"");
                                map.put(db.KEY_FOC_DATE_FROM,"");
                                map.put(db.KEY_FOC_DATE_TO,"");
                                HashMap<String,String>filter = new HashMap<>();
                                //filter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                                filter.put(db.KEY_FOC_QUALIFYING_ITEM,sale.getMaterial_no());
                                filter.put(db.KEY_PRIORITY,"2");

                                HashMap<String, String> filterComp = new HashMap<>();
                                filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                                filterComp.put(db.KEY_FOC_QUALIFYING_ITEM, sale.getMaterial_no());

                                //filter.put(db.KEY_DIST_CHANNEL,Settings.getString(App.DIST_CHANNEL));
                                if (db.checkData(db.FOC_RULES, filterComp)) {
                                    Cursor c = db.getData(db.FOC_RULES,map,filterComp);
                                    List<String> qualifyingQuantities = new ArrayList<String>();
                                    if(c.getCount()>0){
                                        if(c.getCount()>1){
                                            do{
                                                qualifyingQuantities.add(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                            }
                                            while (c.moveToNext());
                                        }
                                        c.moveToFirst();

                                        do{
                                            boolean isInRange = checkDateRange(c.getString(c.getColumnIndex(db.KEY_FOC_DATE_FROM)),c.getString(c.getColumnIndex(db.KEY_FOC_DATE_TO)));
                                            if(isInRange){
                                                HashMap<String,String>vanStockCheckFilter = new HashMap<>();
                                                vanStockCheckFilter.put(db.KEY_MATERIAL_NO,c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                                double quantityVan = 0;
                                                if(db.checkData(db.VAN_STOCK_ITEMS,vanStockCheckFilter)){
                                                    HashMap<String,String>vanStockCheckMap = new HashMap<>();
                                                    vanStockCheckMap.put(db.KEY_REMAINING_QTY_CASE, "");
                                                    vanStockCheckMap.put(db.KEY_REMAINING_QTY_UNIT, "");
                                                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS,vanStockCheckMap,vanStockCheckFilter);
                                                    if(cursor.getCount()>0){
                                                        cursor.moveToFirst();
                                                        quantityVan = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                                                        if(quantityVan>0){
                                                            double inputQuantity = Double.parseDouble(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM)
                                                                    ?sale.getCases():sale.getPic());
                                                            double focQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                                            double assigningQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_QUANTITY)));
                                                            String freeCases = "0";
                                                            String factor ="1";
                                                            if(inputQuantity<focQuantity){

                                                            }
                                                            else {
                                                                if(inputQuantity>=focQuantity&&inputQuantity%focQuantity==0){
                                                                    factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                    freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                                }
                                                                else if(inputQuantity>=focQuantity&&inputQuantity%focQuantity>0){
                                                                    factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                    freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                                }
                                                    /*if(inputQuantity%focQuantity==0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }
                                                    else if(inputQuantity%focQuantity>0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }*/
                                                            }

                                                            ArticleHeader article = ArticleHeader.getArticle(articles, c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                                            if(article!=null){
                                                                Sales newSale = new Sales();
                                                                newSale.setItem_code(article.getArticleNo());
                                                                newSale.setItem_category(article.getArticleCategory());
                                                                newSale.setMaterial_description(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                                newSale.setMaterial_no(article.getMaterialNo());
                                                                newSale.setName(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                                newSale.setUom(article.getBaseUOM());
                                                                if(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)){
                                                                    newSale.setCases(freeCases);
                                                                    newSale.setPic("0");
                                                                }
                                                                newSale.setPrice("0.00");
                                                                HashMap<String,String> filterPart = new HashMap<>();
                                                                filterPart.put(db.KEY_MATERIAL_NO, article.getMaterialNo());

                                                                HashMap<String,String> priceMap = new HashMap<>();
                                                                priceMap.put(db.KEY_MATERIAL_NO, "");
                                                                priceMap.put(db.KEY_AMOUNT,"");
                                                                priceMap.put(db.KEY_AMOUNTMIN,"");
                                                                priceMap.put(db.KEY_AMOUNTMAX,"");
                                                                if(db.checkData(db.PRICING,filterPart)){
                                                                    //Pricing exists for Product for customer
                                                                    //Pricing exists for Product for customer
                                                                    Cursor priceCursor = db.getData(db.PRICING,priceMap,filterPart);
                                                                    if(priceCursor.getCount()>0){
                                                                        priceCursor.moveToFirst();
                                                                        String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                        newSale.setPrice(newSale.getUom().equals(App.CASE_UOM) || newSale.getUom().equals(App.BOTTLES_UOM) ? price : price);
                                                                        newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                        newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                    }
                                                                }
                                                                else{
                                                                    HashMap<String,String> filterBase = new HashMap<>();
                                                                    filterBase.put(db.KEY_PRIORITY, "2");
                                                                    filterBase.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                                                                    if(db.checkData(db.PRICING,filterBase)){
                                                                        //Pricing exists for Product for customer
                                                                        //Pricing exists for Product for customer
                                                                        Cursor priceCursor = db.getData(db.PRICING,priceMap,filterBase);
                                                                        if(priceCursor.getCount()>0){
                                                                            priceCursor.moveToFirst();
                                                                            String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                            newSale.setPrice(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)?price:price);
                                                                            newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                            newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                        }
                                                                    }else {
                                                                        newSale.setPrice("0");
                                                                        newSale.setPricemin("0");
                                                                        newSale.setPricemax("0");
                                                                    }
                                                                }
                                                    /* Changes on 22/03/2017
                                                    * Added to handle the free stock quota based on the current van stock
                                                    * Input quantity + Free goods should always be less than or equal to the
                                                    * van stock available
                                                    * */

                                                                if(freeCases!="0"&&(Double.parseDouble(freeCases)+inputQuantity)<=quantityVan){
                                                                    focValid = true;
                                                                    if(Const.focList.size()>0){
                                                                        for(int i=0;i<Const.focList.size();i++){
                                                                            Sales salesObj = Const.focList.get(i);
                                                                            if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                                Const.focList.remove(i);
                                                                            }
                                                                        }
                                                                    }
                                                                    focArrayList.add(newSale);
                                                                    if(Const.focList.size()==0){
                                                                        Const.focList = focArrayList;
                                                                    }
                                                                }
                                                                else{
                                                                    if(!freeCases.equals("0"))
                                                                    {
                                                                        focValid = false;
                                                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                                        alertDialogBuilder.setTitle(getString(R.string.message))
                                                                                .setMessage("Not enough quantity to give as free goods with quantity in van." +
                                                                                        "Please reduce sale quantity to accomodate the free goods.")
                                                                                .setCancelable(false)
                                                                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        dialog.dismiss();
                                                                                    }
                                                                                });
                                                                        // create alert dialog
                                                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                                                        // show it
                                                                        alertDialog.show();
                                                                        //Toast.makeText(getActivity(),"Not enough quantity to give as free goods with quantity in van.",5).show();
                                                                    }
                                                                    else if(freeCases.equals("0")){
                                                                        focValid = true;
                                                                    }

                                                                }
                                                    /*End of changes 22/03/2017
                                                    * */

                                                    /*Earlier code prior to changes 22/03/2017*/
                                                    /*if(freeCases!="0"&&Double.parseDouble(freeCases)<=quantityVan){
                                                        if(Const.focList.size()>0){
                                                            for(int i=0;i<Const.focList.size();i++){
                                                                Sales salesObj = Const.focList.get(i);
                                                                if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                   Const.focList.remove(i);
                                                                }
                                                            }
                                                        }
                                                        focArrayList.add(newSale);
                                                        if(Const.focList.size()==0){
                                                            Const.focList = focArrayList;
                                                        }
                                                    }*/
                                                    /*End of changes*/
                                                            }
                                                        }
                                                        else{
                                                            Toast.makeText(getActivity(),"Not enough quantity",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                                else{
                                                    Toast.makeText(getActivity(),"FOC Material not available in van",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        while (c.moveToNext());
                                    }
                                }
                                else{
                                    Cursor c = db.getData(db.FOC_RULES,map,filter);
                                    List<String> qualifyingQuantities = new ArrayList<String>();
                                    if(c.getCount()>0){
                                        if(c.getCount()>1){
                                            do{
                                                qualifyingQuantities.add(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                            }
                                            while (c.moveToNext());
                                        }
                                        c.moveToFirst();

                                        do{
                                            boolean isInRange = checkDateRange(c.getString(c.getColumnIndex(db.KEY_FOC_DATE_FROM)),c.getString(c.getColumnIndex(db.KEY_FOC_DATE_TO)));
                                            if(isInRange){
                                                HashMap<String,String>vanStockCheckFilter = new HashMap<>();
                                                vanStockCheckFilter.put(db.KEY_MATERIAL_NO,c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                                double quantityVan = 0;
                                                if(db.checkData(db.VAN_STOCK_ITEMS,vanStockCheckFilter)){
                                                    HashMap<String,String>vanStockCheckMap = new HashMap<>();
                                                    vanStockCheckMap.put(db.KEY_REMAINING_QTY_CASE, "");
                                                    vanStockCheckMap.put(db.KEY_REMAINING_QTY_UNIT, "");
                                                    Cursor cursor = db.getData(db.VAN_STOCK_ITEMS,vanStockCheckMap,vanStockCheckFilter);
                                                    if(cursor.getCount()>0){
                                                        cursor.moveToFirst();
                                                        quantityVan = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                                                        if(quantityVan>0){
                                                            double inputQuantity = Double.parseDouble(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM)
                                                                    ?sale.getCases():sale.getPic());
                                                            double focQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                                            double assigningQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_QUANTITY)));
                                                            String freeCases = "0";
                                                            String factor ="1";
                                                            if(inputQuantity<focQuantity){

                                                            }
                                                            else {
                                                                if(inputQuantity>=focQuantity&&inputQuantity%focQuantity==0){
                                                                    factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                    freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                                }
                                                                else if(inputQuantity>=focQuantity&&inputQuantity%focQuantity>0){
                                                                    factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                    freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                                }
                                                    /*if(inputQuantity%focQuantity==0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }
                                                    else if(inputQuantity%focQuantity>0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }*/
                                                            }

                                                            ArticleHeader article = ArticleHeader.getArticle(articles, c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                                            if(article!=null){
                                                                Sales newSale = new Sales();
                                                                newSale.setItem_code(article.getArticleNo());
                                                                newSale.setItem_category(article.getArticleCategory());
                                                                newSale.setMaterial_description(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                                newSale.setMaterial_no(article.getMaterialNo());
                                                                newSale.setName(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                                newSale.setUom(article.getBaseUOM());
                                                                if(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)){
                                                                    newSale.setCases(freeCases);
                                                                    newSale.setPic("0");
                                                                }
                                                                newSale.setPrice("0.00");
                                                                HashMap<String,String> filterPart = new HashMap<>();
                                                                filterPart.put(db.KEY_MATERIAL_NO, article.getMaterialNo());

                                                                HashMap<String,String> priceMap = new HashMap<>();
                                                                priceMap.put(db.KEY_MATERIAL_NO, "");
                                                                priceMap.put(db.KEY_AMOUNT,"");
                                                                priceMap.put(db.KEY_AMOUNTMIN,"");
                                                                priceMap.put(db.KEY_AMOUNTMAX,"");
                                                                if(db.checkData(db.PRICING,filterPart)){
                                                                    //Pricing exists for Product for customer
                                                                    //Pricing exists for Product for customer
                                                                    Cursor priceCursor = db.getData(db.PRICING,priceMap,filterPart);
                                                                    if(priceCursor.getCount()>0){
                                                                        priceCursor.moveToFirst();
                                                                        String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                        newSale.setPrice(newSale.getUom().equals(App.CASE_UOM) || newSale.getUom().equals(App.BOTTLES_UOM) ? price : price);
                                                                        newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                        newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                    }
                                                                }
                                                                else{
                                                                    HashMap<String,String> filterBase = new HashMap<>();
                                                                    filterBase.put(db.KEY_PRIORITY, "2");
                                                                    filterBase.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                                                                    if(db.checkData(db.PRICING,filterBase)){
                                                                        //Pricing exists for Product for customer
                                                                        //Pricing exists for Product for customer
                                                                        Cursor priceCursor = db.getData(db.PRICING,priceMap,filterBase);
                                                                        if(priceCursor.getCount()>0){
                                                                            priceCursor.moveToFirst();
                                                                            String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                            newSale.setPrice(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)?price:price);
                                                                            newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                            newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                        }
                                                                    }else {
                                                                        newSale.setPrice("0");
                                                                        newSale.setPricemin("0");
                                                                        newSale.setPricemax("0");
                                                                    }
                                                                }
                                                    /* Changes on 22/03/2017
                                                    * Added to handle the free stock quota based on the current van stock
                                                    * Input quantity + Free goods should always be less than or equal to the
                                                    * van stock available
                                                    * */

                                                                if(freeCases!="0"&&(Double.parseDouble(freeCases)+inputQuantity)<=quantityVan){
                                                                    focValid = true;
                                                                    if(Const.focList.size()>0){
                                                                        for(int i=0;i<Const.focList.size();i++){
                                                                            Sales salesObj = Const.focList.get(i);
                                                                            if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                                Const.focList.remove(i);
                                                                            }
                                                                        }
                                                                    }
                                                                    focArrayList.add(newSale);
                                                                    if(Const.focList.size()==0){
                                                                        Const.focList = focArrayList;
                                                                    }
                                                                }
                                                                else{
                                                                    if(!freeCases.equals("0"))
                                                                    {
                                                                        focValid = false;
                                                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                                        alertDialogBuilder.setTitle(getString(R.string.message))
                                                                                .setMessage("Not enough quantity to give as free goods with quantity in van." +
                                                                                        "Please reduce sale quantity to accomodate the free goods.")
                                                                                .setCancelable(false)
                                                                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        dialog.dismiss();
                                                                                    }
                                                                                });
                                                                        // create alert dialog
                                                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                                                        // show it
                                                                        alertDialog.show();
                                                                        //Toast.makeText(getActivity(),"Not enough quantity to give as free goods with quantity in van.",5).show();
                                                                    }
                                                                    else if(freeCases.equals("0")){
                                                                        focValid = true;
                                                                    }

                                                                }
                                                    /*End of changes 22/03/2017
                                                    * */

                                                    /*Earlier code prior to changes 22/03/2017*/
                                                    /*if(freeCases!="0"&&Double.parseDouble(freeCases)<=quantityVan){
                                                        if(Const.focList.size()>0){
                                                            for(int i=0;i<Const.focList.size();i++){
                                                                Sales salesObj = Const.focList.get(i);
                                                                if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                   Const.focList.remove(i);
                                                                }
                                                            }
                                                        }
                                                        focArrayList.add(newSale);
                                                        if(Const.focList.size()==0){
                                                            Const.focList = focArrayList;
                                                        }
                                                    }*/
                                                    /*End of changes*/
                                                            }
                                                        }
                                                        else{
                                                            Toast.makeText(getActivity(),"Not enough quantity",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                                else{
                                                    Toast.makeText(getActivity(),"FOC Material not available in van",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        while (c.moveToNext());
                                    }
                                }
                            }
                        }
                    }
                    Const.focBundle = new Bundle();
                    Const.focBundle.putParcelableArrayList("foc",focArrayList);
                    Log.e("FOC Array","" + focArrayList.size());
                    Const.focList = focArrayList;
                }
                //End of changes  Change no 20170409
                if(Settings.getString(App.DIST_CHANNEL).equals("20")||Settings.getString(App.DIST_CHANNEL).equals("30"))
                {
                    for(Sales sale:arrayList){
                        if(Double.parseDouble(sale.getCases())>0||Double.parseDouble(sale.getPic())>0){
                            HashMap<String,String>map = new HashMap<>();
                            map.put(db.KEY_FOC_ASSIGNING_ITEM,"");
                            map.put(db.KEY_FOC_QUALIFYING_QUANTITY,"");
                            map.put(db.KEY_FOC_ASSIGNING_QUANTITY,"");
                            map.put(db.KEY_FOC_DATE_FROM,"");
                            map.put(db.KEY_FOC_DATE_TO,"");
                            HashMap<String,String>filter = new HashMap<>();
                            //filter.put(db.KEY_CUSTOMER_NO,object.getCustomerID());
                            filter.put(db.KEY_FOC_QUALIFYING_ITEM,sale.getMaterial_no());
                            filter.put(db.KEY_PRIORITY,"2");

                            HashMap<String, String> filterComp = new HashMap<>();
                            filterComp.put(db.KEY_CUSTOMER_NO, object.getCustomerID());
                            filterComp.put(db.KEY_FOC_QUALIFYING_ITEM, sale.getMaterial_no());

                            //filter.put(db.KEY_DIST_CHANNEL,Settings.getString(App.DIST_CHANNEL));
                            if (db.checkData(db.FOC_RULES, filterComp)) {
                                Cursor c = db.getData(db.FOC_RULES,map,filterComp);
                                List<String> qualifyingQuantities = new ArrayList<String>();
                                if(c.getCount()>0){
                                    if(c.getCount()>1){
                                        do{
                                            qualifyingQuantities.add(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                        }
                                        while (c.moveToNext());
                                    }
                                    c.moveToFirst();

                                    do{
                                        boolean isInRange = checkDateRange(c.getString(c.getColumnIndex(db.KEY_FOC_DATE_FROM)),c.getString(c.getColumnIndex(db.KEY_FOC_DATE_TO)));
                                        if(isInRange){
                                            HashMap<String,String>vanStockCheckFilter = new HashMap<>();
                                            vanStockCheckFilter.put(db.KEY_MATERIAL_NO,c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                            double quantityVan = 0;
                                            if(db.checkData(db.VAN_STOCK_ITEMS,vanStockCheckFilter)){
                                                HashMap<String,String>vanStockCheckMap = new HashMap<>();
                                                vanStockCheckMap.put(db.KEY_REMAINING_QTY_CASE, "");
                                                vanStockCheckMap.put(db.KEY_REMAINING_QTY_UNIT, "");
                                                Cursor cursor = db.getData(db.VAN_STOCK_ITEMS,vanStockCheckMap,vanStockCheckFilter);
                                                if(cursor.getCount()>0){
                                                    cursor.moveToFirst();
                                                    quantityVan = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                                                    if(quantityVan>0){
                                                        double inputQuantity = Double.parseDouble(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM)
                                                                ?sale.getCases():sale.getPic());
                                                        double focQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                                        double assigningQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_QUANTITY)));
                                                        String freeCases = "0";
                                                        String factor ="1";
                                                        if(inputQuantity<focQuantity){

                                                        }
                                                        else {
                                                            if(inputQuantity>=focQuantity&&inputQuantity%focQuantity==0){
                                                                factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                            }
                                                            else if(inputQuantity>=focQuantity&&inputQuantity%focQuantity>0){
                                                                factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                            }
                                                    /*if(inputQuantity%focQuantity==0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }
                                                    else if(inputQuantity%focQuantity>0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }*/
                                                        }

                                                        ArticleHeader article = ArticleHeader.getArticle(articles, c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                                        if(article!=null){
                                                            Sales newSale = new Sales();
                                                            newSale.setItem_code(article.getArticleNo());
                                                            newSale.setItem_category(article.getArticleCategory());
                                                            newSale.setMaterial_description(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                            newSale.setMaterial_no(article.getMaterialNo());
                                                            newSale.setName(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                            newSale.setUom(article.getBaseUOM());
                                                            if(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)){
                                                                newSale.setCases(freeCases);
                                                                newSale.setPic("0");
                                                            }
                                                            newSale.setPrice("0.00");
                                                            HashMap<String,String> filterPart = new HashMap<>();
                                                            filterPart.put(db.KEY_MATERIAL_NO, article.getMaterialNo());

                                                            HashMap<String,String> priceMap = new HashMap<>();
                                                            priceMap.put(db.KEY_MATERIAL_NO, "");
                                                            priceMap.put(db.KEY_AMOUNT,"");
                                                            priceMap.put(db.KEY_AMOUNTMIN,"");
                                                            priceMap.put(db.KEY_AMOUNTMAX,"");
                                                            if(db.checkData(db.PRICING,filterPart)){
                                                                //Pricing exists for Product for customer
                                                                //Pricing exists for Product for customer
                                                                Cursor priceCursor = db.getData(db.PRICING,priceMap,filterPart);
                                                                if(priceCursor.getCount()>0){
                                                                    priceCursor.moveToFirst();
                                                                    String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                    newSale.setPrice(newSale.getUom().equals(App.CASE_UOM) || newSale.getUom().equals(App.BOTTLES_UOM) ? price : price);
                                                                    newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                    newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                }
                                                            }
                                                            else{
                                                                HashMap<String,String> filterBase = new HashMap<>();
                                                                filterBase.put(db.KEY_PRIORITY, "2");
                                                                filterBase.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                                                                if(db.checkData(db.PRICING,filterBase)){
                                                                    //Pricing exists for Product for customer
                                                                    //Pricing exists for Product for customer
                                                                    Cursor priceCursor = db.getData(db.PRICING,priceMap,filterBase);
                                                                    if(priceCursor.getCount()>0){
                                                                        priceCursor.moveToFirst();
                                                                        String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                        newSale.setPrice(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)?price:price);
                                                                        newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                        newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                    }
                                                                }else {
                                                                    newSale.setPrice("0");
                                                                    newSale.setPricemin("0");
                                                                    newSale.setPricemax("0");
                                                                }
                                                            }
                                                    /* Changes on 22/03/2017
                                                    * Added to handle the free stock quota based on the current van stock
                                                    * Input quantity + Free goods should always be less than or equal to the
                                                    * van stock available
                                                    * */

                                                            if(freeCases!="0"&&(Double.parseDouble(freeCases)+inputQuantity)<=quantityVan){
                                                                focValid = true;
                                                                if(Const.focList.size()>0){
                                                                    for(int i=0;i<Const.focList.size();i++){
                                                                        Sales salesObj = Const.focList.get(i);
                                                                        if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                            Const.focList.remove(i);
                                                                        }
                                                                    }
                                                                }
                                                                focArrayList.add(newSale);
                                                                if(Const.focList.size()==0){
                                                                    Const.focList = focArrayList;
                                                                }
                                                            }
                                                            else{
                                                                if(!freeCases.equals("0"))
                                                                {
                                                                    focValid = false;
                                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                                    alertDialogBuilder.setTitle(getString(R.string.message))
                                                                            .setMessage("Not enough quantity to give as free goods with quantity in van." +
                                                                                    "Please reduce sale quantity to accomodate the free goods.")
                                                                            .setCancelable(false)
                                                                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });
                                                                    // create alert dialog
                                                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                                                    // show it
                                                                    alertDialog.show();
                                                                    //Toast.makeText(getActivity(),"Not enough quantity to give as free goods with quantity in van.",5).show();
                                                                }
                                                                else if(freeCases.equals("0")){
                                                                    focValid = true;
                                                                }

                                                            }
                                                    /*End of changes 22/03/2017
                                                    * */

                                                    /*Earlier code prior to changes 22/03/2017*/
                                                    /*if(freeCases!="0"&&Double.parseDouble(freeCases)<=quantityVan){
                                                        if(Const.focList.size()>0){
                                                            for(int i=0;i<Const.focList.size();i++){
                                                                Sales salesObj = Const.focList.get(i);
                                                                if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                   Const.focList.remove(i);
                                                                }
                                                            }
                                                        }
                                                        focArrayList.add(newSale);
                                                        if(Const.focList.size()==0){
                                                            Const.focList = focArrayList;
                                                        }
                                                    }*/
                                                    /*End of changes*/
                                                        }
                                                    }
                                                    else{
                                                        Toast.makeText(getActivity(),"Not enough quantity",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                            else{
                                                Toast.makeText(getActivity(),"FOC Material not available in van",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    while (c.moveToNext());
                                }
                            }
                            else{
                                Cursor c = db.getData(db.FOC_RULES,map,filter);
                                List<String> qualifyingQuantities = new ArrayList<String>();
                                if(c.getCount()>0){
                                    if(c.getCount()>1){
                                        do{
                                            qualifyingQuantities.add(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                        }
                                        while (c.moveToNext());
                                    }
                                    c.moveToFirst();

                                    do{
                                        boolean isInRange = checkDateRange(c.getString(c.getColumnIndex(db.KEY_FOC_DATE_FROM)),c.getString(c.getColumnIndex(db.KEY_FOC_DATE_TO)));
                                        if(isInRange){
                                            HashMap<String,String>vanStockCheckFilter = new HashMap<>();
                                            vanStockCheckFilter.put(db.KEY_MATERIAL_NO,c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                            double quantityVan = 0;
                                            if(db.checkData(db.VAN_STOCK_ITEMS,vanStockCheckFilter)){
                                                HashMap<String,String>vanStockCheckMap = new HashMap<>();
                                                vanStockCheckMap.put(db.KEY_REMAINING_QTY_CASE, "");
                                                vanStockCheckMap.put(db.KEY_REMAINING_QTY_UNIT, "");
                                                Cursor cursor = db.getData(db.VAN_STOCK_ITEMS,vanStockCheckMap,vanStockCheckFilter);
                                                if(cursor.getCount()>0){
                                                    cursor.moveToFirst();
                                                    quantityVan = Double.parseDouble(cursor.getString(cursor.getColumnIndex(db.KEY_REMAINING_QTY_CASE)));
                                                    if(quantityVan>0){
                                                        double inputQuantity = Double.parseDouble(sale.getUom().equals(App.CASE_UOM)||sale.getUom().equals(App.BOTTLES_UOM)
                                                                ?sale.getCases():sale.getPic());
                                                        double focQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_QUALIFYING_QUANTITY)));
                                                        double assigningQuantity = Double.parseDouble(c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_QUANTITY)));
                                                        String freeCases = "0";
                                                        String factor ="1";
                                                        if(inputQuantity<focQuantity){

                                                        }
                                                        else {
                                                            if(inputQuantity>=focQuantity&&inputQuantity%focQuantity==0){
                                                                factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                            }
                                                            else if(inputQuantity>=focQuantity&&inputQuantity%focQuantity>0){
                                                                factor = String.valueOf((int)(inputQuantity/focQuantity));
                                                                freeCases = String.valueOf((int)(assigningQuantity)*(int)Double.parseDouble(factor));
                                                            }
                                                    /*if(inputQuantity%focQuantity==0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }
                                                    else if(inputQuantity%focQuantity>0){
                                                        freeCases = String.valueOf((int)(inputQuantity/focQuantity));
                                                    }*/
                                                        }

                                                        ArticleHeader article = ArticleHeader.getArticle(articles, c.getString(c.getColumnIndex(db.KEY_FOC_ASSIGNING_ITEM)));
                                                        if(article!=null){
                                                            Sales newSale = new Sales();
                                                            newSale.setItem_code(article.getArticleNo());
                                                            newSale.setItem_category(article.getArticleCategory());
                                                            newSale.setMaterial_description(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                            newSale.setMaterial_no(article.getMaterialNo());
                                                            newSale.setName(UrlBuilder.decodeString(article.getMaterialDesc1()));
                                                            newSale.setUom(article.getBaseUOM());
                                                            if(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)){
                                                                newSale.setCases(freeCases);
                                                                newSale.setPic("0");
                                                            }
                                                            newSale.setPrice("0.00");
                                                            HashMap<String,String> filterPart = new HashMap<>();
                                                            filterPart.put(db.KEY_MATERIAL_NO, article.getMaterialNo());

                                                            HashMap<String,String> priceMap = new HashMap<>();
                                                            priceMap.put(db.KEY_MATERIAL_NO, "");
                                                            priceMap.put(db.KEY_AMOUNT,"");
                                                            priceMap.put(db.KEY_AMOUNTMIN,"");
                                                            priceMap.put(db.KEY_AMOUNTMAX,"");
                                                            if(db.checkData(db.PRICING,filterPart)){
                                                                //Pricing exists for Product for customer
                                                                //Pricing exists for Product for customer
                                                                Cursor priceCursor = db.getData(db.PRICING,priceMap,filterPart);
                                                                if(priceCursor.getCount()>0){
                                                                    priceCursor.moveToFirst();
                                                                    String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                    newSale.setPrice(newSale.getUom().equals(App.CASE_UOM) || newSale.getUom().equals(App.BOTTLES_UOM) ? price : price);
                                                                    newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                    newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                }
                                                            }
                                                            else{
                                                                HashMap<String,String> filterBase = new HashMap<>();
                                                                filterBase.put(db.KEY_PRIORITY, "2");
                                                                filterBase.put(db.KEY_MATERIAL_NO, cursor.getString(cursor.getColumnIndex(db.KEY_MATERIAL_NO)));

                                                                if(db.checkData(db.PRICING,filterBase)){
                                                                    //Pricing exists for Product for customer
                                                                    //Pricing exists for Product for customer
                                                                    Cursor priceCursor = db.getData(db.PRICING,priceMap,filterBase);
                                                                    if(priceCursor.getCount()>0){
                                                                        priceCursor.moveToFirst();
                                                                        String price = priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNT));
                                                                        newSale.setPrice(newSale.getUom().equals(App.CASE_UOM)||newSale.getUom().equals(App.BOTTLES_UOM)?price:price);
                                                                        newSale.setPricemin(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMIN)));
                                                                        newSale.setPricemax(priceCursor.getString(priceCursor.getColumnIndex(db.KEY_AMOUNTMAX)));
                                                                    }
                                                                }else {
                                                                    newSale.setPrice("0");
                                                                    newSale.setPricemin("0");
                                                                    newSale.setPricemax("0");
                                                                }
                                                            }
                                                    /* Changes on 22/03/2017
                                                    * Added to handle the free stock quota based on the current van stock
                                                    * Input quantity + Free goods should always be less than or equal to the
                                                    * van stock available
                                                    * */

                                                            if(freeCases!="0"&&(Double.parseDouble(freeCases)+inputQuantity)<=quantityVan){
                                                                focValid = true;
                                                                if(Const.focList.size()>0){
                                                                    for(int i=0;i<Const.focList.size();i++){
                                                                        Sales salesObj = Const.focList.get(i);
                                                                        if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                            Const.focList.remove(i);
                                                                        }
                                                                    }
                                                                }
                                                                focArrayList.add(newSale);
                                                                if(Const.focList.size()==0){
                                                                    Const.focList = focArrayList;
                                                                }
                                                            }
                                                            else{
                                                                if(!freeCases.equals("0"))
                                                                {
                                                                    focValid = false;
                                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                                    alertDialogBuilder.setTitle(getString(R.string.message))
                                                                            .setMessage("Not enough quantity to give as free goods with quantity in van." +
                                                                                    "Please reduce sale quantity to accomodate the free goods.")
                                                                            .setCancelable(false)
                                                                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });
                                                                    // create alert dialog
                                                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                                                    // show it
                                                                    alertDialog.show();
                                                                    //Toast.makeText(getActivity(),"Not enough quantity to give as free goods with quantity in van.",5).show();
                                                                }
                                                                else if(freeCases.equals("0")){
                                                                    focValid = true;
                                                                }

                                                            }
                                                    /*End of changes 22/03/2017
                                                    * */

                                                    /*Earlier code prior to changes 22/03/2017*/
                                                    /*if(freeCases!="0"&&Double.parseDouble(freeCases)<=quantityVan){
                                                        if(Const.focList.size()>0){
                                                            for(int i=0;i<Const.focList.size();i++){
                                                                Sales salesObj = Const.focList.get(i);
                                                                if(salesObj.getMaterial_no().equals(newSale.getMaterial_no())){
                                                                   Const.focList.remove(i);
                                                                }
                                                            }
                                                        }
                                                        focArrayList.add(newSale);
                                                        if(Const.focList.size()==0){
                                                            Const.focList = focArrayList;
                                                        }
                                                    }*/
                                                    /*End of changes*/
                                                        }
                                                    }
                                                    else{
                                                        Toast.makeText(getActivity(),"Not enough quantity",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                            else{
                                                Toast.makeText(getActivity(),"FOC Material not available in van",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    while (c.moveToNext());
                                }
                            }

                        }

                    }
                    Const.focBundle = new Bundle();
                    Const.focBundle.putParcelableArrayList("foc",focArrayList);
                    Log.e("FOC Array","" + focArrayList.size());
                    Const.focList = focArrayList;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }
    private boolean checkDateRange(String dateMin,String dateMax){
        boolean isValid = false;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        String currentDate = Helpers.formatDate(new Date(),"yyyy.MM.dd");
        try {
            Date dateLow = formatter.parse(dateMin);
            Date dateHigh = formatter.parse(dateMax);
            Date today = formatter.parse(currentDate);
            // isValid = today.after(dateLow) && today.before(dateHigh);
            isValid = today.after(dateLow) && today.before(dateHigh)?true:today.equals(dateLow)||today.equals(dateHigh)?true:false;
            Log.e("Is Valid","" + isValid);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isValid;
    }
}