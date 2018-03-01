package com.ae.benchmark.adapters;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.models.LoadRequest;
import com.ae.benchmark.models.LoadSummary;

/**
 * Created by Rakshit on 29-Dec-16.
 */
public class LoadRequestBadgeAdapter extends ArrayAdapter<LoadRequest> implements Filterable {
    private ArrayList<LoadRequest> loadRequestList;
    ArrayList<LoadRequest> loadRequestListone;
    ItemFilter mFilter = new ItemFilter();
    private int pos;

    public LoadRequestBadgeAdapter(Context context, ArrayList<LoadRequest> loadRequests){

        super(context, R.layout.activity_loadrequest_items, loadRequests);
        this.loadRequestList = loadRequests;
        this.loadRequestListone = loadRequests;
    }
    @Override
    public int getCount() {
        return loadRequestList.size();
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ArrayList<LoadRequest> dataList = this.loadRequestList;
        pos = position;
        ViewHolder holder;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_loadrequest_items,parent,false);
            // get all UI view
            holder = new ViewHolder();
            //holder.rl_item = (LinearLayout)convertView.findViewById(R.id.rl_item);
            holder.itemCode = (TextView)convertView.findViewById(R.id.tv_item);
            holder.itemName = (TextView) convertView.findViewById(R.id.tvItemName);
            holder.cases = (TextView) convertView.findViewById(R.id.tvCases);
            holder.units = (TextView) convertView.findViewById(R.id.tvUnit);
            holder.categoryImage = (ImageView) convertView.findViewById(R.id.categoryImage);
            holder.item_price = (TextView)convertView.findViewById(R.id.tv_price);
            // set tag for holder
            convertView.setTag(holder);
        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }
        final LoadRequest loadRequest = loadRequestList.get(pos);
        holder.itemCode.setText(getContext().getString(R.string.item_code) + " - " + StringUtils.stripStart(loadRequest.getMaterialNo(), "0"));
        holder.itemName.setText(loadRequest.getItemName());

        /*else if(loadRequestList.get(pos).getUom().equals(App.BOTTLES_UOM)){
            holder.cases.setEnabled(false);
        }*/

        holder.cases.setText(loadRequestList.get(pos).getCases());
        holder.units.setText(loadRequestList.get(pos).getUnits());
        if(!loadRequestList.get(pos).getCases().equals("0")){
            holder.item_price.setText(getContext().getString(R.string.price) + " - " + String.valueOf(Float.parseFloat(loadRequestList.get(pos).getPrice())*Float.parseFloat(loadRequestList.get(pos).getCases())));
        }
        else{
            holder.item_price.setText(getContext().getString(R.string.price) + " - " + String.valueOf(Float.parseFloat(loadRequestList.get(pos).getPrice())));
        }
       // holder.units.setText(loadRequest.getUnits());
        // Set the results into ImageView
//        String uri = "@drawable/a"+StringUtils.stripStart(loadRequestList.get(pos).getMaterialNo(),"0");
        String uri = "@mipmap/ic_launcher_new";
        int imageResource = getContext().getResources().getIdentifier(uri,null,getContext().getPackageName());
        Drawable res = getContext().getResources().getDrawable(imageResource);
        holder.categoryImage.setImageDrawable(res);
        //holder.categoryImage.setImageResource(R.drawable.beraincategory);
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
            Log.v("DataAdapter", "constratinst : " + loadRequestList.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<LoadRequest> filteredItems = new ArrayList<>();
                for (int i = 0, l = loadRequestListone.size(); i < l; i++) {
                    String p = loadRequestListone.get(i).getItemName().toLowerCase();
                    String m = loadRequestListone.get(i).getMaterialNo().toLowerCase();

                    if (p.contains(constraint) || m.contains(constraint)) {
                        filteredItems.add(loadRequestListone.get(i));
                        Log.v("DataAdapter", p + " -- " + loadRequestListone.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = loadRequestListone.size();
                    result.values = loadRequestListone;
                }
            }
            return result;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            loadRequestList = (ArrayList<LoadRequest>) results.values;
            Log.v("datalist", loadRequestList.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }

    public class ViewHolder {
        TextView itemCode;
        TextView itemName;
        TextView cases;
        TextView units;
        ImageView categoryImage;
        LinearLayout rl_item;
        public TextWatcher casestextWatcher;
        public TextWatcher unitsTextWatcher;
        TextView item_price;

        /*public ViewHolder(View v) {
            rl_item = (LinearLayout)v.findViewById(R.id.rl_item);
            itemName = (TextView) v.findViewById(R.id.tvItemName);
            cases = (EditText) v.findViewById(R.id.tvCases);
            units = (EditText) v.findViewById(R.id.tvUnit);
            categoryImage = (ImageView) v.findViewById(R.id.categoryImage);
        }*/
    }

}
