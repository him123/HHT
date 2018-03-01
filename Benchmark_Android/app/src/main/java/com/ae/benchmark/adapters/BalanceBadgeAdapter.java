package com.ae.benchmark.adapters;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ae.benchmark.R;
import com.ae.benchmark.models.Collection;
import com.ae.benchmark.models.Message;
import com.ae.benchmark.utils.Helpers;
/**
 * Created by Rakshit on 03-Feb-17.
 */
public class BalanceBadgeAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private ArrayList<Collection> collectionsList;
    private ArrayList<Collection> collectionsListSearch;
    private int pos;
    ItemFilter mFilter = new ItemFilter();
    public BalanceBadgeAdapter(Context context, ArrayList<Collection> values){
        this.context = context;
        this.collectionsList = values;
        this.collectionsListSearch = values;
    }

    @Override
    public int getCount() {
        return collectionsList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ArrayList<Collection> dataList = this.collectionsList;
        pos = position;
        ViewHolder holder;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.balance_badge,parent,false);
            // get all UI view
            holder = new ViewHolder();
           // holder.tv_customer = (TextView) convertView.findViewById(R.id.tv_customer);
            holder.tv_invoice_no = (TextView) convertView.findViewById(R.id.tv_inv_no);
            holder.tv_invoice_date = (TextView)convertView.findViewById(R.id.tv_inv_date);
            holder.tv_invoice_due_date = (TextView) convertView.findViewById(R.id.tv_due_date);
            holder.tv_invoice_amount = (TextView) convertView.findViewById(R.id.tv_amount);
            holder.tv_amount_due = (TextView)convertView.findViewById(R.id.tv_amount);
            holder.img_item_due_date = (ImageView)convertView.findViewById(R.id.img_item_due_date);

            convertView.setTag(holder);

        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_invoice_no.setText(StringUtils.stripStart(collectionsList.get(pos).getInvoiceNo(), "0"));
        //holder.tv_price.setText("Price:54.00/2.25");
        holder.tv_invoice_date.setText(collectionsList.get(pos).getInvoiceDate());
        holder.tv_invoice_due_date.setText(collectionsList.get(pos).getInvoiceDueDate());
        //holder.tv_invoice_amount.setText(collectionsList.get(pos).getInvoiceAmount() + " AED ");
        float amountdue = Float.parseFloat(collectionsList.get(pos).getInvoiceAmount());
        holder.tv_invoice_amount.setText(collectionsList.get(pos).getAmountCleared());
        holder.tv_amount_due.setText(String.valueOf(amountdue));
        try {
            Date invoiceDate = new SimpleDateFormat("yyyy-MM-dd").parse(collectionsList.get(pos).getInvoiceDate());
            Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(collectionsList.get(pos).getInvoiceDueDate());
            long invoiceDays = (long) (dueDate.getTime() - invoiceDate.getTime()/(86400.0 * 1000.0));
            int days = dueDate.compareTo(invoiceDate);
            String todaysDate = Helpers.formatDate(new Date(), "yyyy-MM-dd");
            Date today = new SimpleDateFormat("yyyy-MM-dd").parse(todaysDate);
            long currentDays = (long) (dueDate.getTime() - today.getTime()/(86400.0 * 1000.0));
            int dayfromToday = today.compareTo(dueDate);

            if((invoiceDays-currentDays)>15){
                holder.img_item_due_date.setBackgroundColor(Color.YELLOW);
            }
            else{

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
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
            Log.v("DataAdapter", "constratinst : " + collectionsList.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<Collection> filteredItems = new ArrayList<>();
                for (int i = 0, l = collectionsList.size(); i < l; i++) {
                    String p = collectionsList.get(i).getInvoiceNo().toLowerCase();
                    if (p.contains(constraint)) {
                        filteredItems.add(collectionsList.get(i));
                        Log.v("DataAdapter", p + " -- " + collectionsList.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = collectionsListSearch.size();
                    result.values = collectionsListSearch;
                }
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            collectionsList = (ArrayList<Collection>) results.values;
            Log.v("datalist", collectionsList.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }

    public class ViewHolder {
        TextView tv_customer,tv_invoice_no, tv_invoice_date, tv_invoice_due_date, tv_invoice_amount, tv_amount_due;
        ImageView img_item_due_date;
    }
}
