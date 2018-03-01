package com.ae.benchmark.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.models.ItemList;
import com.ae.benchmark.models.Message;

/**
 * Created by eheuristic on 22/12/2016.
 */

public class PriceListAdapter extends BaseAdapter implements Filterable {
    Activity activity;
    ArrayList<ItemList> arrayList;
    ArrayList<ItemList> arrayListSearch;
   ItemFilter mFilter = new ItemFilter();
    public PriceListAdapter(Activity activity,ArrayList<ItemList> arrayList)
    {
        this.arrayList = arrayList;
        this.arrayListSearch = arrayList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            view = inflater.inflate(R.layout.item_price_list,viewGroup,false);
        }

        ItemList itemListModel = arrayList.get(i);

        TextView txt_item_number = (TextView)view.findViewById(R.id.txt_item_number);
        TextView txt_item_dec = (TextView)view.findViewById(R.id.txt_item_dec);
        TextView txt_item_case_price = (TextView)view.findViewById(R.id.txt_item_case_price);


        txt_item_number.setText("" +itemListModel.getItem_number());
        txt_item_dec.setText(itemListModel.getItem_des());
        txt_item_case_price.setText(""+itemListModel.getCase_price());


        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
    public class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();
            Log.v("DataAdapter", "constratinst : " + constraint);
            Log.v("DataAdapter", "constratinst : " + arrayList.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<ItemList> filteredItems = new ArrayList<>();
                for (int i = 0, l = arrayList.size(); i < l; i++) {
                    String p = arrayList.get(i).getItem_des().toLowerCase();
                    if (p.contains(constraint)) {
                        filteredItems.add(arrayList.get(i));
                        Log.v("DataAdapter", p + " -- " + arrayList.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = arrayListSearch.size();
                    result.values = arrayListSearch;
                }
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrayList = (ArrayList<ItemList>) results.values;
            Log.v("datalist", arrayList.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }
}
