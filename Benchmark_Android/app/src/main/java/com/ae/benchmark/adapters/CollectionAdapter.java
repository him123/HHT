package com.ae.benchmark.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import com.ae.benchmark.R;
import com.ae.benchmark.models.Collection;

/**
 * Created by Rakshit on 23-Jan-17.
 */
public class CollectionAdapter extends ArrayAdapter<Collection> {
    private Context context;
    private ArrayList<Collection> collectionsList;
    private int pos;

    public CollectionAdapter(Context context, ArrayList<Collection> values){
        super(context, R.layout.badge_collection_list_item, values);
        this.context = context;
        this.collectionsList = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ArrayList<Collection> dataList = this.collectionsList;
        pos = position;
        ViewHolder holder;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.badge_collection_list_item,parent,false);
            // get all UI view
            holder = new ViewHolder();
            holder.tv_invoice_no = (TextView) convertView.findViewById(R.id.tv_invoice_no);
            holder.tv_invoice_date = (TextView)convertView.findViewById(R.id.tv_invoice_date);
            holder.tv_invoice_due_date = (TextView) convertView.findViewById(R.id.tv_due_date);
            holder.tv_invoice_amount = (TextView) convertView.findViewById(R.id.tv_invoice_amount);
            holder.tv_amount_due = (TextView)convertView.findViewById(R.id.tv_amount_due);
            convertView.setTag(holder);

        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_invoice_no.setText(context.getString(R.string.invoice_no) + " : " + collectionsList.get(pos).getInvoiceNo());
        //holder.tv_price.setText("Price:54.00/2.25");
        holder.tv_invoice_date.setText(getContext().getString(R.string.inv_date) + " : " + collectionsList.get(pos).getInvoiceDate());
        holder.tv_invoice_due_date.setText(getContext().getString(R.string.due_date) + " : " + collectionsList.get(pos).getInvoiceDueDate());
        holder.tv_invoice_amount.setText(collectionsList.get(pos).getAmountCleared());
        Double due = (Double.parseDouble(collectionsList.get(pos).getInvoiceAmount()));
        holder.tv_amount_due.setText(getContext().getString(R.string.amout_due) + " : " + String.format("%.2f",due) + " AED ");
        return convertView;
    }

    public class ViewHolder {
        TextView tv_invoice_no, tv_invoice_date, tv_invoice_due_date, tv_invoice_amount, tv_amount_due;
    }
}
