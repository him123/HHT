package com.ae.benchmark.adapters;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.DashboardActivity;
import com.ae.benchmark.activities.SelectOperationActivity;
import com.ae.benchmark.activities.ShopStatusActivity;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.ShopStatus;
import com.ae.benchmark.views.TextViewWithLabel;
/**
 * Created by Rakshit on 16-Nov-16.
 */
public class ShopStatusBadgeAdapter extends ArrayAdapter<ShopStatus> {
    public ShopStatusBadgeAdapter(Context context, ArrayList<ShopStatus> shopstatus){
        super(context, R.layout.badge_shop_status,shopstatus);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.badge_shop_status,parent,false);
        }

        final ShopStatus status = getItem(position);
        TextView status_text = (TextView)convertView.findViewById(R.id.lbl_shop_status);
        RadioButton radio_selected = (RadioButton)convertView.findViewById(R.id.radio_btn_shopstatus);
        status_text.setText(status.getStatusText());
        radio_selected.setSelected(status.isSelected());

        radio_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),SelectOperationActivity.class);
                getContext().startActivity(intent);
               // ShopStatusActivity.callback(status.getStatusID());
            }
        });

        return convertView;
    }
}
