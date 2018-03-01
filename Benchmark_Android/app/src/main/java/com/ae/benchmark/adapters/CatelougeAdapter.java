package com.ae.benchmark.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.CatelogueListActiviy;
import com.ae.benchmark.models.Catelog_model;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Dell on 11-02-2018.
 */

public class CatelougeAdapter extends ArrayAdapter<Catelog_model> implements Filterable {

    Activity context;
    ViewHolder holder;
    private ArrayList<Catelog_model> mFilteredList;
    private ArrayList<Catelog_model> itemList;
    public CatelougeAdapter(Activity context, ArrayList<Catelog_model> itemList) {
        super(context, R.layout.custom_catalogue_list, itemList);
//        mInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.itemList = itemList;
        this.mFilteredList = itemList;
//        this.visitList = visitList;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflator = null;
        View view = null;
        if (convertView == null) {
            holder = new ViewHolder();
            inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.custom_catalogue_list, null);
            holder.tv_title = (TextView) view.findViewById(R.id.tv_title_list);
            holder.tv_price = (TextView) view.findViewById(R.id.tv_price);
            holder.iv_cart = (ImageView) view.findViewById(R.id.iv_cart);
            holder.iv_product_image = (ImageView) view.findViewById(R.id.iv_image);
            view.setTag(holder);

        } else {
            view = convertView;
        }
        final ViewHolder holder = (ViewHolder) view.getTag();


        holder.tv_price.setText(itemList.get(position).getPrice());
        holder.tv_title.setText(itemList.get(position).getTitel());
        holder.iv_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemList.get(position).getSelected()) {
                    itemList.get(position).setSelected(false);
                    holder.iv_cart.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                }else{
                    itemList.get(position).setSelected(true);
                    holder.iv_cart.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                }

            }
        });

        if(itemList.get(position).getSelected()){
            holder.iv_cart.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        }else{
            holder.iv_cart.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if(!itemList.get(position).getImg_url().equalsIgnoreCase("")) {
            Picasso.with(context)
                    .load(CatelogueListActiviy.catalogue_list.get(position).getImg_url())
                    .placeholder(R.drawable.logo_fresh)
                    .error(R.drawable.logo_fresh)
                    .into(holder.iv_product_image);
        }else{
            holder.iv_product_image.setImageDrawable(context.getResources().getDrawable(R.drawable.logo_fresh));
        }
        return view;
    }

    class ViewHolder {
        //    TextViewWithLabel customerID;
//    TextViewWithLabel customerName;
//    TextViewWithLabel customerAddress;
        TextView tv_title,tv_price;
        ImageView iv_cart, iv_product_image;

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {

                    itemList = mFilteredList;
                } else {

                    ArrayList<Catelog_model> filteredList = new ArrayList<>();

                    for (Catelog_model Tradein : mFilteredList) {

                        if (Tradein.getTitel().toLowerCase().contains(charString.toLowerCase())) {

                            filteredList.add(Tradein);
                        }
                    }

                    itemList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = itemList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                itemList = (ArrayList<Catelog_model>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
