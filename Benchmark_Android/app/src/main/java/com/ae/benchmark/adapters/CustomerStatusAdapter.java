package com.ae.benchmark.adapters;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.LoadSummaryActivity;
import com.ae.benchmark.activities.LoadVerifyActivity;
import com.ae.benchmark.models.CustomerStatus;
import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by Rakshit on 20-Jan-17.
 */
public class CustomerStatusAdapter extends ArrayAdapter<CustomerStatus> {
    private ArrayList<CustomerStatus> statuses;
    public CustomerStatusAdapter(Context context, ArrayList<CustomerStatus> customerStatuses){
        super(context, R.layout.badge_customer_status, customerStatuses);
        this.statuses = customerStatuses;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;

        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.badge_customer_status,parent,false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        CustomerStatus status = getItem(position);
        Log.e("Status","" + status.getReasonDescription());
        holder.item_description.setText(UrlBuilder.decodeString(status.getReasonDescription()));
        return convertView;
    }
    private class ViewHolder{
        private TextView item_code;
        private TextView item_description;
        public ViewHolder(View v) {
            item_description = (TextView) v.findViewById(R.id.tv_shop_status);
        }
    }
}
