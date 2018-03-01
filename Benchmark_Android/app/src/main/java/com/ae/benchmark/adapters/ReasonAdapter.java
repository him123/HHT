package com.ae.benchmark.adapters;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.ae.benchmark.models.Reasons;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by Rakshit on 17-Jan-17.
 */
public class ReasonAdapter extends ArrayAdapter<Reasons> {
    private Context context;
    private ArrayList<Reasons> reasonList;

    public ReasonAdapter(Context context, int textViewResourceId, ArrayList<Reasons> values){
        super(context, android.R.layout.simple_spinner_item, values);
        this.context = context;
        this.reasonList = values;
    }

    public int getCount(){
        return this.reasonList.size();
    }

    public Reasons getItem(int position){
        return reasonList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = new TextView(context);
        label.setTextSize(16);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(UrlBuilder.decodeString(reasonList.get(position).getReasonDescription()));

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextSize(16);
        label.setTextColor(Color.BLACK);
        label.setText(UrlBuilder.decodeString(reasonList.get(position).getReasonDescription()));
        return label;
    }
}
