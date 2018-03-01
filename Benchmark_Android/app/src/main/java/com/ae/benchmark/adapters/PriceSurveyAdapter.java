package com.ae.benchmark.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.ae.benchmark.R;

/**
 * Created by Muhammad Umair on 22-09-2017.
 */

public class PriceSurveyAdapter extends ArrayAdapter<String> {

    private final Activity context;

    ArrayList<String> itemName;
    ArrayList<String> itemCode;

    public PriceSurveyAdapter(Activity context, ArrayList<String> itemName, ArrayList<String> itemCode) {
        super(context, R.layout.lv_price_survey_items, itemName);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemName=itemName;
        this.itemCode=itemCode;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.item_complaint, null,true);

        TextView tv_itemName = (TextView) rowView.findViewById(R.id.tv_title);
        TextView tv_itemNo = (TextView) rowView.findViewById(R.id.tv_item_code);


        TextView tv_price = (TextView) rowView.findViewById(R.id.tv_price);
        TextView tv_cases = (TextView) rowView.findViewById(R.id.tv_cases);
        TextView tv_cases_value = (TextView) rowView.findViewById(R.id.tv_cases_value);
        TextView tv_pcs = (TextView) rowView.findViewById(R.id.tv_pcs);
        TextView tv_pcs_value = (TextView) rowView.findViewById(R.id.tv_pcs_value);
        
        tv_price.setVisibility(View.GONE);
        tv_cases.setVisibility(View.GONE);
        tv_cases_value.setVisibility(View.GONE);
        tv_pcs.setVisibility(View.GONE);
        tv_pcs_value.setVisibility(View.GONE);
        ImageView red_arrow = (ImageView)rowView.findViewById(R.id.red_arrow);


        tv_itemName.setText(itemName.get(position));
        tv_itemNo.setText("Item code : "+itemCode.get(position));
        return rowView;

    };
}

