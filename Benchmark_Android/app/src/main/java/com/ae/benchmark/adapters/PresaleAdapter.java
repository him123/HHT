package com.ae.benchmark.adapters;

import android.content.Context;
import android.drm.ProcessedData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.ae.benchmark.R;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.PreSaleProceed;

/**
 * Created by eheuristic on 12/5/2016.
 */

public class PresaleAdapter extends BaseAdapter {


    Context context;
    int resource;

    int item;


    public PresaleAdapter(Context context, int resource,int item)
    {
        Log.v("called","adapter");
        this.context=context;
        this.resource=resource;

        this.item=item;



    }


    @Override
    public int getCount() {
        return item;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, null);
            TextView tv=(TextView)convertView.findViewById(R.id.tv_delivery);
            TextView tv1=(TextView)convertView.findViewById(R.id.tv_del_date);
            if(item>0) {
                tv.setText("Order#"+position);
                tv1.setText("Order date : 2/1/2017");
            }

            return convertView;
    }


}
