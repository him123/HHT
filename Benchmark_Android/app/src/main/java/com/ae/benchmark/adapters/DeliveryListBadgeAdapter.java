package com.ae.benchmark.adapters;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.models.DeliveryOrderList;
/**
 * Created by Rakshit on 03-Jan-17.
 */
public class DeliveryListBadgeAdapter extends ArrayAdapter<DeliveryOrderList> {
    private ArrayList<DeliveryOrderList> orders;
    Context context;
    public DeliveryListBadgeAdapter(Context context, ArrayList<DeliveryOrderList> orders){

        super(context, R.layout.custom_delivery, orders);
        this.orders = orders;
        this.context=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;

        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_delivery,parent,false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        DeliveryOrderList order = getItem(position);

        holder.order_id.setText("Delivery No : " + StringUtils.stripStart(order.getOrderId(), "0"));
        holder.order_date.setText("Delivery Date : " + order.getOrderDate());
        Log.v("Delivery",order.getOrderId()+" - "+order.getOrderReferenceNo());
        if(order.getOrderStatus().equals("true")){
            holder.red_arrow.setImageResource(R.drawable.green_tick_icon);
        }
        else if(order.getOrderStatus().equals("false")){

        }
        else if(order.getOrderStatus().equals("deleted")){
            holder.red_arrow.setImageResource(R.drawable.green_tick_icon_cross);
        }

        if(order.getOrderReferenceNo().isEmpty())
        {
            holder.ll_delivery.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.tv_paid.setVisibility(View.GONE);
        }
        else {
            holder.ll_delivery.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
            holder.tv_paid.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private class ViewHolder{
        private TextView order_id;
        private TextView order_date;
        private ImageView red_arrow;
        private LinearLayout ll_delivery;
        ImageView tv_paid;

        public ViewHolder(View v) {

            order_id = (TextView) v.findViewById(R.id.tv_delivery);
            order_date = (TextView) v.findViewById(R.id.tv_del_date);
            red_arrow = (ImageView)v.findViewById(R.id.red_arrow);
            ll_delivery = (LinearLayout) v.findViewById(R.id.ll_delivery);
            tv_paid = (ImageView) v.findViewById(R.id.tv_paid);
        }
    }
}
