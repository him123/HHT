package com.ae.benchmark.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.DataPostingAuditActivity;
import com.ae.benchmark.models.DataPoustingAudit;

/**
 * Created by ehs on 22/12/16.
 */

public class DataPoustingAuditAdapter extends BaseAdapter {

    ArrayList<DataPoustingAudit> arrayList;
    Activity activity;
    boolean isChecked=false;

    public DataPoustingAuditAdapter(Activity activity, ArrayList<DataPoustingAudit> arrayList,boolean isChecked)
    {
        this.activity = activity;
        this.arrayList = arrayList;
        this.isChecked=isChecked;
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
            view = inflater.inflate(R.layout.item_data_pousting_audit,viewGroup,false);

        }

        DataPoustingAudit dataPoustingAudit = arrayList.get(i);

        TextView txt_print_doc_customer_id = (TextView)view.findViewById(R.id.txt_print_doc_customer_id);
        TextView txt_print_doc_customer_trans = (TextView)view.findViewById(R.id.txt_print_doc_customer_trans);
        TextView txt_print_doc_customer_type = (TextView)view.findViewById(R.id.txt_print_doc_customer_type);
        ImageView ImageView = (ImageView)view.findViewById(R.id.img_item_data_pousting);
        CheckBox checkbox_print_doc=(CheckBox)view.findViewById(R.id.checkbox_print_doc);
        LinearLayout ll=(LinearLayout)view.findViewById(R.id.ll);



        ll.setOnTouchListener(DataPostingAuditActivity.swipeDetector);
//        ll.setOnClickListener();



        if(isChecked)
        {
            checkbox_print_doc.setChecked(true);
        }
        else {
            checkbox_print_doc.setChecked(false);
        }




        if (i == 1){
            ImageView.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        if (i == 4){
            ImageView.setBackgroundColor(Color.parseColor("#00FF00"));
        }
        if (i == 6) {
            ImageView.setBackgroundColor(Color.parseColor("#00FF00"));
        }

        txt_print_doc_customer_id.setText(""+dataPoustingAudit.getCustomer_id());
        txt_print_doc_customer_trans.setText(""+dataPoustingAudit.getCustomer_transection());
        txt_print_doc_customer_type.setText(dataPoustingAudit.getType());

        return view;
    }
}
