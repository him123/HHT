package com.ae.benchmark.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ae.benchmark.R;
import com.ae.benchmark.models.Catelog_model;

import java.util.ArrayList;
import java.util.List;



public class CustomCartListAdapter extends ArrayAdapter<Catelog_model> {

    private final ArrayList<Catelog_model> list;
    private final Activity context;
    Boolean flag;
    InputMethodManager imm;
    TextView txt_no_items, tv_grand_total;
    ViewHolder holder;

    public CustomCartListAdapter(Activity context, ArrayList<Catelog_model> list,
                                 Boolean flag) {
        super(context, R.layout.custom_cartlistlayout, list);
        this.context = context;
        this.list = list;
        this.flag = flag;
        imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        txt_no_items = (TextView) context.findViewById(R.id.txt_no_items);
        tv_grand_total = (TextView) context.findViewById(R.id.tv_grand_total);


    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup viewGroup) {
        LayoutInflater inflator = null;
        View view = null;
        if (convertView == null) {
            holder = new ViewHolder();
            inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.custom_cartlistlayout, null);

            holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            holder.tv_productprice = (TextView) view.findViewById(R.id.tv_price);
            holder.ed_qty = (EditText) view.findViewById(R.id.ed_qty);
            holder.ed_qty.setFocusable(true);
            holder.ed_qty.setFocusableInTouchMode(true);
            holder.tv_total = (TextView) view.findViewById(R.id.tv_total);
            holder.tv_discount = (TextView) view.findViewById(R.id.tv_disc);
            holder.btn_remove = (ImageView) view.findViewById(R.id.btn_remove);

            view.setTag(holder);

        } else {
            view = convertView;
        }

        final ViewHolder holder = (ViewHolder) view.getTag();

        try {

            holder.ed_qty.setText("" + list.get(position).getQty());
            holder.ed_qty.setId(position);

            holder.tv_name.setText(list.get(position).getTitel());
            holder.tv_productprice.setTextColor(context.getResources().getColor(R.color.black));
            holder.tv_productprice.setText(list.get(position).getPrice());
            float total = Float.parseFloat(list.get(position).getPrice())
                    * Float.parseFloat("" + list.get(position).getQty());
            holder.tv_total.setText(total+"");
            list.get(position).setProductTotal(total+"");

//            if (Integer.parseInt(list.get(position).getItem_discount()) > 0) {
////                holder.tv_discount.setText(context.getResources().getString(R.string.rs)+""+list.get(position).getItem_discount()+" OFF");
//                holder.tv_productprice.setTextColor(context.getResources().getColor(R.color.lightgreay));
//                holder.tv_productprice.setPaintFlags(holder.tv_productprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                holder.tv_discount.setVisibility(View.VISIBLE);
//                holder.tv_discount.setTextColor(context.getResources().getColor(R.color.text_color));
//                holder.tv_discount.setText(context.getResources().getString(R.string.rs) + list.get(position).getItem_discounted_price());
//                holder.tv_productprice.setText(context.getResources().getString(R.string.rs) + list.get(position).getSale_price());
//
//            } else {
//                holder.tv_productprice.setPaintFlags(holder.tv_productprice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                holder.tv_discount.setVisibility(View.GONE);
//                holder.tv_productprice.setText(context.getResources().getString(R.string.rs) + list.get(position).getSale_price());
//
//            }
////            float total = Float.parseFloat(list.get(position).getItem_discounted_price())
////                    * Float.parseFloat("" + list.get(position).getQty());
////            list.get(position).setTotalAmount(String.valueOf(total));
////           holder.tv_total.setText(list.get(position).getTotalAmount());
//            try {
//                for (int i = 0; i < list.size(); i++) {
//                    float total1 = Float.parseFloat(list.get(i).getItem_discounted_price())
//                            * Float.parseFloat("" + list.get(i).getQty());
//                    list.get(i).setTotalAmount(String.valueOf(total1));
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            try {
//                for (int i = 0; i < list.size(); i++) {
//                    float total2 = Float.parseFloat(list.get(i).getSale_price())
//                            * Float.parseFloat("" + list.get(i).getQty());
//                    list.get(i).setItemLineTotal(String.valueOf(total2));
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            holder.tv_total.setText(list.get(position).getTotalAmount());
            try {
                float grand_total1 = 0;
                for (int i = 0; i < list.size(); i++) {
                    Log.d("TOTAL", "" + list.get(i).getProductTotal());
                    grand_total1 += Float.parseFloat(list.get(i).getProductTotal());
                    Log.d("GRAND TOTAL", "" + grand_total1);
                }
                tv_grand_total.setText("" + String.format("%.2f", grand_total1));
            } catch (Exception e) {
                e.printStackTrace();
            }
//
            try {
                int total_qty = 0;
                for (int i = 0; i < list.size(); i++) {
                    total_qty += Integer.parseInt(list.get(i).getQty());
                }
                txt_no_items.setText("" + total_qty);
            } catch (Exception e) {
                e.printStackTrace();
            }
//
//
////            getTotalValue(list.get(position).getItem_discount(),
////                    list.get(position).getItem_discounted_price(),
////                    list.get(position).getQty(),
////                    0, position);
//
//
            holder.btn_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    list.remove(position);
//                    if(list.size() > 0) {
                    float total = 0, grand_total = 0;
                    int total_qty = 0;

//                    for (int i = 0; i < list.size(); i++) {
//                        grand_total += Float.parseFloat(list.get(i).getTotalAmount());
//                    }
//                    tv_grand_total.setText("" + String.format("%.2f", grand_total));
//
//                    for (int i = 0; i < list.size(); i++) {
//                        total_qty += list.get(i).getQty();
//                    }
//                    txt_no_items.setText("" + total_qty);
//                    }
                    notifyDataSetChanged();
                }
            });


            holder.ed_qty.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // TODO Auto-generated method stub

                    if (!hasFocus) {
                        if (holder.ed_qty.getText().length() == 0) {
                            try {

                                final int position = v.getId();
                                final EditText Caption = (EditText) v;
                                Caption.setText("1");
                                list.get(position).setQty(Caption.getText().toString());
                                Log.d("RA", "FOCUS pos:" + position + " val:"
                                        + Caption.getText().toString());
//                                float total = Float.parseFloat(list.get(position).getPrice())
//                                        * Float.parseFloat("" + list.get(position).getQty());

                                notifyDataSetChanged();
//                                getTotalValue(list.get(position).getItem_discount(),
//                                        list.get(position).getItem_discounted_price(),
//                                        Integer.parseInt(Caption.getText()
//                                                .toString()),
//                                        0, position);

                            } catch (Exception e) {
                            }

                        }

                    }
                }
            });



            holder.ed_qty
                    .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId,
                                                      KeyEvent event) {

                            try {
                                if (actionId == EditorInfo.IME_ACTION_DONE
                                        || actionId == EditorInfo.IME_ACTION_NEXT) {

                                    final int position = v.getId();
                                    final EditText Caption = (EditText) v;
                                    if (Caption.getText().length() == 0) {
                                        Caption.setText("1");
                                    }
                                    if(Caption.getText().toString().equals("0")){
                                        Caption.setText("1");
                                    }
                                    list.get(position).setQty(Caption.getText().toString());
//                                    Log.d("RA", "DONE pos:" + position + " val:"
//                                            + Caption.getText().toString());
//
//                                    getTotalValue(list.get(position)
//                                                    .getItem_discount(), list.get(position)
//                                                    .getItem_discounted_price(), Integer.parseInt(Caption.getText()
//                                                    .toString()), 0,
//                                            position);

                                    imm.hideSoftInputFromWindow(
                                            holder.ed_qty.getWindowToken(),
                                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                                    notifyDataSetChanged();
                                    return true;
                                }
                            } catch (Exception e) {
                                // TODO: handle exception
                                e.printStackTrace();
                            }

                            return false;
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }


        return view;

    }

//    public float getTotalValue(String discount, String price, int qty,
//                               float conversionVal, int position)
//
//    {
//        Log.d("RA", "modified_qty:" + qty + " pos:" + position);
//        float total = 0, grand_total = 0, netitemlinetotal = 0;
//        int total_qty = 0;
//        list.get(position).setItem_discount(discount);
//        list.get(position).setItem_discounted_price(price);
//        list.get(position).setQty(qty);
//
//        float itemdiscount = Float.parseFloat(list.get(position).getItem_discount());
//        float itemPrice = Float.parseFloat(list.get(position)
//                .getItem_discounted_price());
//        float itemNetPrice = Float.parseFloat(list.get(position)
//                .getSale_price());
//
//        float itemQty = Float.parseFloat("" + list.get(position).getQty());
//
//        netitemlinetotal = (itemNetPrice * itemQty);
//        total = (itemPrice * itemQty);
//
//        list.get(position).setItemLineTotal(String.valueOf(netitemlinetotal));
//        list.get(position).setTotalAmount(String.valueOf(total));
//        Log.d("RA", "GET TOTAL" + list.get(position).getTotalAmount());
//        holder.tv_total.setText(context.getResources().getString(R.string.rs) + list.get(position).getTotalAmount());
//
//        for (int i = 0; i < list.size(); i++) {
//            grand_total += Float.parseFloat(list.get(i).getTotalAmount());
//        }
//        tv_grand_total.setText("" + String.format("%.2f", grand_total));
//
//        for (int i = 0; i < list.size(); i++) {
//            total_qty += list.get(i).getQty();
//        }
//        txt_no_items.setText("" + total_qty);
//
//        return total;
//    }

    class ViewHolder {

        protected TextView tv_name, tv_productprice, tv_total, tv_discount;
        ImageView iv_productpic, iv_readmore;
        EditText ed_qty;
        ImageView btn_remove;

    }

}
