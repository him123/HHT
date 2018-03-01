package com.ae.benchmark.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.ae.benchmark.R;
import com.ae.benchmark.models.Promotions;

import java.util.ArrayList;

/**
 * Created by Rakshit on 17-Jan-17.
 */

public class PromotionsAdapter extends BaseAdapter implements Filterable {
    private ArrayList<Promotions> promotionList;
    private ArrayList<Promotions> promotionListSearch;
    Context context;
    ItemFilter mFilter = new ItemFilter();

    public PromotionsAdapter(Context context, ArrayList<Promotions> promotionList) {
        this.context = context;
        this.promotionList = promotionList;
        this.promotionListSearch = promotionList;
    }

    @Override
    public int getCount() {
        return promotionList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_promotionlist, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            /*holder.tv_promotion = (TextView) convertView.findViewById(R.id.tv_product);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.chk_product);
            LinearLayout ll_promotion = (LinearLayout) convertView.findViewById(R.id.ll_promotion);
            holder.view = (View) convertView.findViewById(R.id.view);
            */// set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        Promotions promotions = promotionList.get(position);

        holder.tv_promotion.setText(promotions.getPromotionDescription());
        if (promotions.isMandatory()) {
            holder.view.setBackgroundColor(Color.RED);
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.view.setBackgroundColor(Color.BLUE);
            holder.checkBox.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }


    private class ViewHolder {
        View view;
        TextView tv_promotion;
        CheckBox checkBox;
        //  TextView tv_message_title;

        public ViewHolder(View v) {

            tv_promotion = (TextView) v.findViewById(R.id.tv_product);
            checkBox = (CheckBox) v.findViewById(R.id.chk_product);
            view = (View) v.findViewById(R.id.view);
            //   tv_message_title = (TextView) v.findViewById(R.id.tv_message_title);
        }
    }


    public class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();
            Log.v("DataAdapter", "constratinst : " + constraint);
            Log.v("DataAdapter", "constratinst : " + promotionList.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<Promotions> filteredItems = new ArrayList<>();
                for (int i = 0, l = promotionList.size(); i < l; i++) {
                    String p = promotionList.get(i).getPromotionDescription().toLowerCase();
                    if (p.contains(constraint)) {
                        filteredItems.add(promotionList.get(i));
                        Log.v("DataAdapter", p + " -- " + promotionList.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = promotionListSearch.size();
                    result.values = promotionListSearch;
                }
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            promotionList = (ArrayList<Promotions>) results.values;
            Log.v("datalist", promotionList.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }
}
