package com.ae.benchmark.adapters;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.models.LoadRequest;
import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.models.OrderList;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.models.VanStock;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by Rakshit on 03-Jan-17.
 */
public class VanStockBadgeAdapter extends BaseAdapter implements Filterable{
    private ArrayList<VanStock> vanStocks;
    private ArrayList<VanStock> vanStocksSearch;
    Context context;
    ItemFilter mFilter = new ItemFilter();
    public VanStockBadgeAdapter(Context context, ArrayList<VanStock> vanStocks){

        this.vanStocks = vanStocks;
        this.vanStocksSearch = vanStocks;
        this.context=context;
    }

    @Override
    public int getCount() {
        return vanStocks.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;

        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_vanstock_items,parent,false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }
        VanStock vanStock = vanStocks.get(position);

        holder.item_code.setText(context.getString(R.string.item_code) + " - " + StringUtils.stripStart(vanStock.getItem_code(), "0"));
        holder.item_description.setText(UrlBuilder.decodeString(vanStock.getItem_description()));
        holder.item_case.setText(vanStock.getItem_case());
        holder.item_unit.setText(vanStock.getItem_units());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }


    private class ViewHolder{
        private TextView item_code;
        private TextView item_description;
        private TextView item_case;
        private TextView item_unit;

        public ViewHolder(View v) {

            item_code = (TextView) v.findViewById(R.id.item);
            item_description = (TextView) v.findViewById(R.id.description);
            item_case = (TextView) v.findViewById(R.id.cases);
            item_unit = (TextView) v.findViewById(R.id.units);
        }
    }

    public class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();
            Log.v("DataAdapter", "constratinst : " + constraint);
            Log.v("DataAdapter", "constratinst : " + vanStocks.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<VanStock> filteredItems = new ArrayList<>();
                for (int i = 0, l = vanStocks.size(); i < l; i++) {
                    String p = vanStocks.get(i).getItem_code().toLowerCase();
                    String m = vanStocks.get(i).getItem_description().toLowerCase();

                    if (p.contains(constraint) || m.contains(constraint)) {
                        filteredItems.add(vanStocks.get(i));
                        Log.v("DataAdapter", p + " -- " + vanStocks.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = vanStocksSearch.size();
                    result.values = vanStocksSearch;
                }
            }
            return result;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            vanStocks = (ArrayList<VanStock>) results.values;
            Log.v("datalist", vanStocks.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }


}
