package com.ae.benchmark.adapters;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.LoadSummaryActivity;
import com.ae.benchmark.activities.LoadVerifyActivity;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.views.TextViewWithLabel;
/**
 * Created by Rakshit on 19-Nov-16.
 */
public class LoadVerifyBadgeAdapter extends BaseAdapter implements Filterable{
    private LoadVerifyActivity activity;
    private LoadVerifyActivity loadVerifyActivity;
    private ArrayList<LoadSummary> loadSummaryList;
    private ArrayList<LoadSummary> loadSummaryListSearch;
    private ArrayList<LoadSummary> tempList;
  ItemFilter mFilter = new ItemFilter();

    public LoadVerifyBadgeAdapter(Context context, ArrayList<LoadSummary> loadSummaries){

        this.activity = (LoadVerifyActivity)context;
        this.loadSummaryList = loadSummaries;
        this.loadSummaryListSearch = loadSummaries;
        this.tempList = new ArrayList<>();

    }

    @Override
    public int getCount() {
        return loadSummaryList.size();
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
            LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.badge_load_verify,parent,false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        LoadSummary loadSummary = loadSummaryList.get(position);
       /* TextView item_code = (TextView)convertView.findViewById(R.id.lbl_item_code);
        TextView item_description = (TextView)convertView.findViewById(R.id.lbl_item_description);
        TextView quantity_cs = (TextView)convertView.findViewById(R.id.lbl_quantity_cases);
        TextView quantity_bt = (TextView)convertView.findViewById(R.id.lbl_quantity_units);*/

        holder.item_code.setText(activity.getString(R.string.item_code) + " - " + StringUtils.stripStart(loadSummary.getMaterialNo(),"0"));
        holder.item_description.setText(loadSummary.getItemDescription());
        holder.quantity_cases.setText(loadSummary.getQuantityCases());
        holder.quantity_units.setText(loadSummary.getQuantityUnits());
        String[] caseArray = new String[2];
        caseArray = loadSummary.getQuantityUnits().split("\\|");
        holder.item_price.setText(activity.getString(R.string.price) + " - " + String.format("%.2f",Float.parseFloat(loadSummary.getPrice())*Float.parseFloat(loadSummary.getQuantityUnits())));
        return convertView;
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
            Log.v("DataAdapter", "constratinst : " + loadSummaryList.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<LoadSummary> filteredItems = new ArrayList<>();
                for (int i = 0, l = loadSummaryListSearch.size(); i < l; i++) {
                    String p = loadSummaryListSearch.get(i).getItemDescription().toLowerCase();
                    String m = loadSummaryListSearch.get(i).getMaterialNo().toLowerCase();

                    if (p.contains(constraint) || m.contains(constraint)) {
                        filteredItems.add(loadSummaryListSearch.get(i));
                        Log.v("DataAdapter", p + " -- " + loadSummaryListSearch.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = loadSummaryListSearch.size();
                    result.values = loadSummaryListSearch;
                }
            }
            return result;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            loadSummaryList = (ArrayList<LoadSummary>) results.values;
            Log.v("datalist", loadSummaryList.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }



    private class ViewHolder{
        private TextView item_code;
        private TextView item_description;
        private TextView quantity_cases;
        private TextView quantity_units;
        private TextView item_price;

        public ViewHolder(View v) {

            item_code = (TextView) v.findViewById(R.id.lbl_item_code);
            item_description = (TextView) v.findViewById(R.id.lbl_item_description);
            quantity_cases = (TextView) v.findViewById(R.id.lbl_quantity_cases);
            quantity_units = (TextView) v.findViewById(R.id.lbl_quantity_units);
            item_price = (TextView)v.findViewById(R.id.lbl_item_price);
        }
    }
}
