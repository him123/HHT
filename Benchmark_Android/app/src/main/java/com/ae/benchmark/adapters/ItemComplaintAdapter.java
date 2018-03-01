package com.ae.benchmark.adapters;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.models.Sales;
/**
 * Created by Rakshit on 16-Jan-17.
 */
public class ItemComplaintAdapter extends ArrayAdapter<Sales> {
    private ArrayList<Sales> salesArrayList;
    private int pos;
    public ItemComplaintAdapter(Context context, ArrayList<Sales> salesArrayList){

        super(context, R.layout.item_complaint, salesArrayList);
        this.salesArrayList = salesArrayList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try{
            final ArrayList<Sales> dataList = this.salesArrayList;
            Log.e("Sales Array","" + this.salesArrayList.size());
            pos = position;
            ViewHolder holder;
            if(convertView==null){
                LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_complaint,parent,false);
                // get all UI view
                holder = new ViewHolder();
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                holder.tv_item_code = (TextView)convertView.findViewById(R.id.tv_item_code);
                holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
                holder.tv_cases = (TextView) convertView.findViewById(R.id.tv_cases);
                holder.tv_cases_value = (TextView) convertView.findViewById(R.id.tv_cases_value);
                holder.tv_pcs = (TextView) convertView.findViewById(R.id.tv_pcs);
                holder.tv_pcs_value = (TextView) convertView.findViewById(R.id.tv_pcs_value);
                holder.tv_price.setVisibility(View.GONE);
                holder.tv_cases.setVisibility(View.GONE);
                holder.tv_cases_value.setVisibility(View.GONE);
                holder.tv_pcs.setVisibility(View.GONE);
                holder.tv_pcs_value.setVisibility(View.GONE);
                holder.red_arrow = (ImageView)convertView.findViewById(R.id.red_arrow);
                convertView.setTag(holder);

            }
            else {
                // if holder created, get tag from view
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_title.setText(salesArrayList.get(position).getName());
            final Sales sales = salesArrayList.get(pos);
            //Log.e("UOM", "" + position + sales.getMaterial_no() + sales.getUom());
            try{
                if(salesArrayList.get(pos).getUom().equals(App.CASE_UOM)||salesArrayList.get(pos).getUom().equals(App.CASE_UOM_NEW)||salesArrayList.get(pos).getUom().equals(App.BOTTLES_UOM)){
                    holder.tv_price.setText(getContext().getString(R.string.price_lbl) + salesArrayList.get(position).getPrice() + "/0.00");
                }
                else{
                    holder.tv_price.setText(getContext().getString(R.string.price_lbl) +"0.00/" + salesArrayList.get(position).getPrice());
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
            //holder.tv_price.setText("Price:54.00/2.25");
            holder.tv_item_code.setText(getContext().getString(R.string.item_code) + " - " + StringUtils.stripStart(salesArrayList.get(position).getMaterial_no(), "0"));
            holder.tv_cases.setText(getContext().getString(R.string.cases));
            holder.tv_cases_value.setText(salesArrayList.get(position).getCases());
            holder.tv_pcs.setText(getContext().getString(R.string.pcs));
            holder.tv_pcs_value.setText(salesArrayList.get(position).getPic());
            return convertView;
        }
        catch (Exception e){
            e.printStackTrace();
            return convertView;
        }

    }

    public class ViewHolder {
        TextView tv_title, tv_price, tv_cases, tv_cases_value, tv_pcs, tv_pcs_value, tv_item_code;
        ImageView red_arrow;
    }
}
