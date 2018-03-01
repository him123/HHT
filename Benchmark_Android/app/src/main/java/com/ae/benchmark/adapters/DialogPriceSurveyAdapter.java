package com.ae.benchmark.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.ae.benchmark.R;

/**
 * Created by Muhammad Umair on 23-09-2017.
 */

public class DialogPriceSurveyAdapter extends ArrayAdapter<String> {

    private final Activity context;

    private final ArrayList itemName;
    private final String[] itemCases;

    public DialogPriceSurveyAdapter (Activity context,ArrayList itemName,String []itemCases) {
        super(context, R.layout.dialog_price_survey, itemName);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemName=itemName;
        this.itemCases=itemCases;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.dialog_price_survey, null,true);

        TextView tv_itemName = (TextView) rowView.findViewById(R.id.tv_comp_name);

        EditText et_itemCases = (EditText) rowView.findViewById(R.id.et_cs);

        tv_itemName.setText(itemName.get(position).toString());

        et_itemCases.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                itemCases[position] = "";
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                itemCases[position] = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return rowView;
    };
}


