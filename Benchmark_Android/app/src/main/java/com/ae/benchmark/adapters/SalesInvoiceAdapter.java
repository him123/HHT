package com.ae.benchmark.adapters;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.App;
import com.ae.benchmark.Fragment.SalesFragment;
import com.ae.benchmark.R;
import com.ae.benchmark.models.Sales;
import com.ae.benchmark.utils.Settings;

/**
 * Created by Rakshit on 16-Jan-17.
 */
public class SalesInvoiceAdapter extends ArrayAdapter<Sales> implements Filterable {
    private ArrayList<Sales> salesArrayList;
    ArrayList<Sales> salesArrayListone;
    ItemFilter mFilter = new ItemFilter();
    private int pos;
    String type = "";
    public SalesInvoiceAdapter(Context context, ArrayList<Sales> salesArrayList){

        super(context, R.layout.sales_list, salesArrayList);
        this.salesArrayList = salesArrayList;
        this.salesArrayListone = salesArrayList;
    }
    public SalesInvoiceAdapter(Context context, ArrayList<Sales> salesArrayList,String Type){

        super(context, R.layout.sales_list, salesArrayList);
        this.type = Type;
        this.salesArrayList = salesArrayList;
        this.salesArrayListone = salesArrayList;
    }
    @Override
    public int getCount() {
        return salesArrayList.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try{
            final ArrayList<Sales> dataList = this.salesArrayList;
            Log.e("Sales Array","" + this.salesArrayList.size());
            pos = position;
            ViewHolder holder;
            if(convertView==null){
                LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.sales_list,parent,false);
                // get all UI view
                holder = new ViewHolder();
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                holder.tv_item_code = (TextView)convertView.findViewById(R.id.tv_item_code);
                holder.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
                holder.tv_cases = (TextView) convertView.findViewById(R.id.tv_cases);
                holder.tv_cases_value = (TextView) convertView.findViewById(R.id.tv_cases_value);
                holder.tv_pcs = (TextView) convertView.findViewById(R.id.tv_pcs);
                holder.tv_pcs_value = (TextView) convertView.findViewById(R.id.tv_pcs_value);
                convertView.setTag(holder);

            }
            else {
                // if holder created, get tag from view
                holder = (ViewHolder) convertView.getTag();
            }
            final Sales sales = salesArrayList.get(pos);
            holder.tv_title.setText(salesArrayList.get(position).getName());

            String strpc="0.00",strcase="0.00";
            if(!type.equals("Invoice")){
                strpc = "" + sales.getPrice();
                try {
                    Double casePrice = Double.parseDouble(sales.getPrice()) * Double.parseDouble(sales.getDenominator());
                    strcase = "" + String.format("%.2f", casePrice);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.tv_price.setText(getContext().getString(R.string.price_lbl) +" "+ strcase +"/"+ strpc);
            }else{
//                Double pcsPrice = Double.parseDouble(sales.getPricepcs())*Double.parseDouble(sales.getPic());
//                Double pc = pcsPrice+pcsPrice*Double.parseDouble(Settings.getString(App.VATValue));
//                strpc = String.format("%.2f",pc);
//
//                Double caseprice = Double.parseDouble(sales.getPricecase())*Double.parseDouble(sales.getCases());
//                Double cases = caseprice+caseprice*Double.parseDouble(Settings.getString(App.VATValue));
//                strcase = String.format("%.2f",cases);
                holder.tv_price.setText("Price/Piece = "+ sales.getPricepcs());
            }
            //Log.e("UOM", "" + position + sales.getMaterial_no() + sales.getUom());
            try{
                /*if(salesArrayList.get(pos).getUom().equals(App.CASE_UOM)||salesArrayList.get(pos).getUom().equals(App.CASE_UOM_NEW)||salesArrayList.get(pos).getUom().equals(App.BOTTLES_UOM)){
                    holder.tv_price.setText(getContext().getString(R.string.price_lbl) + salesArrayList.get(position).getPrice() + "/0.00");
                }
                else{*/

//                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
            //holder.tv_price.setText("Price:54.00/2.25");
            holder.tv_item_code.setText(getContext().getString(R.string.item_code) + " - " + StringUtils.stripStart(sales.getMaterial_no(), "0"));
            holder.tv_cases.setText(getContext().getString(R.string.cases));
            holder.tv_cases_value.setText(sales.getCases());
            holder.tv_pcs.setText(getContext().getString(R.string.pcs));
            holder.tv_pcs_value.setText(sales.getPic());
            return convertView;
        }
        catch (Exception e){
            e.printStackTrace();
            return convertView;
        }

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
            Log.v("DataAdapter", "constratinst : " + salesArrayList.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<Sales> filteredItems = new ArrayList<>();
                for (int i = 0, l = salesArrayListone.size(); i < l; i++) {
                    String p = salesArrayListone.get(i).getName().toLowerCase();
                    String m = salesArrayListone.get(i).getMaterial_no().toLowerCase();

                    if (p.contains(constraint) || m.contains(constraint)) {
                        filteredItems.add(salesArrayListone.get(i));
                        Log.v("DataAdapter", p + " -- " + salesArrayListone.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = salesArrayListone.size();
                    result.values = salesArrayListone;
                }
            }
            return result;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            salesArrayList = (ArrayList<Sales>) results.values;
            Log.v("datalist", salesArrayList.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }
    public class ViewHolder {
        TextView tv_title, tv_price, tv_cases, tv_cases_value, tv_pcs, tv_pcs_value, tv_item_code;
    }
}
