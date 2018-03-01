package com.ae.benchmark.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.OrderSummary;
import com.ae.benchmark.views.TextViewWithLabel;
/**
 * Created by Rakshit on 18-Nov-16.
 */
public class OrderSummaryBadgeAdapter extends ArrayAdapter<OrderSummary> {
    public OrderSummaryBadgeAdapter(Context context, ArrayList<OrderSummary> orders){
        super(context, R.layout.badge_order_summary,orders);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.badge_order_summary,parent,false);
        }

        OrderSummary orders = getItem(position);
        TextView product_description = (TextView)convertView.findViewById(R.id.lbl_product_description);
        TextView quantitycs = (TextView)convertView.findViewById(R.id.lbl_quantitycs);
        TextView quantitybt = (TextView)convertView.findViewById(R.id.lbl_quantitybt);
        TextView tradeprice = (TextView)convertView.findViewById(R.id.lbl_tradeprice);
        TextView discount = (TextView)convertView.findViewById(R.id.lbl_discount);

        product_description.setText(orders.getProductDescription());
        quantitycs.setText(orders.getQuantityCS());
        quantitybt.setText(orders.getQuantityBT());
        tradeprice.setText(orders.getTradeprice());
        discount.setText(orders.getDiscount());

        return convertView;
    }
}
