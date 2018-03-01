package com.ae.benchmark.adapters;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ae.benchmark.models.OrderRequest;
import com.daimajia.swipe.SwipeLayout;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.LoadSummaryActivity;
import com.ae.benchmark.activities.LoadVerifyActivity;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.LoadSummary;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.UrlBuilder;
import com.ae.benchmark.views.TextViewWithLabel;
/**
 * Created by Rakshit on 19-Nov-16.
 */
public class LoadSummaryBadgeAdapter extends ArrayAdapter<LoadSummary> implements Filterable {
    private LoadSummaryActivity activity;
    private LoadVerifyActivity loadVerifyActivity;
    private ArrayList<LoadSummary> loadSummaryList;
    ArrayList<LoadSummary> loadRequestListone;
    ItemFilter mFilter = new ItemFilter();
    private ArrayList<LoadSummary> tempList;

    DatabaseHandler db;
    public LoadSummaryBadgeAdapter(Context context, ArrayList<LoadSummary> loadSummaries){

        super(context, R.layout.badge_load_summary, loadSummaries);
        db = new DatabaseHandler(context);
        if(context instanceof LoadSummaryActivity){
            this.activity = (LoadSummaryActivity) context;

        }
        else if(context instanceof LoadVerifyActivity){
            this.loadVerifyActivity = (LoadVerifyActivity) context;
        }

        this.loadSummaryList = loadSummaries;
        this.loadRequestListone = loadSummaries;
        this.tempList = new ArrayList<>();

    }
    @Override
    public int getCount() {
        return loadSummaryList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;

        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.badge_load_summary,parent,false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        }
        else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        LoadSummary loadSummary = loadSummaryList.get(position);
       /* TextView item_code = (TextView)convertView.findViewById(R.id.lbl_item_code);
        TextView item_description = (TextView)convertView.findViewById(R.id.lbl_item_description);
        TextView quantity_cs = (TextView)convertView.findViewById(R.id.lbl_quantity_cases);
        TextView quantity_bt = (TextView)convertView.findViewById(R.id.lbl_quantity_units);*/

        holder.item_code.setText(getContext().getString(R.string.item_code) + " - " + StringUtils.stripStart(loadSummary.getMaterialNo(),"0"));
        holder.item_description.setText(loadSummary.getItemDescription());
        holder.quantity_cases.setText(loadSummary.getQuantityCases());
        holder.quantity_units.setText(loadSummary.getQuantityUnits());
        holder.item_price.setText(getContext().getString(R.string.price) + " - " + String.format("%.2f",Float.parseFloat(loadSummary.getPrice())*Float.parseFloat(loadSummary.getQuantityUnits())));
       // holder.btn_accept.setOnClickListener(onAcceptListener(position, holder));
       // holder.btn_reject.setOnClickListener(onRejectListener(position,holder));

        return convertView;
    }

    private View.OnClickListener onAcceptListener(final int position, final ViewHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showEditDialog(position, holder);
                tempList.add(loadSummaryList.get(position));
                LoadSummary loadSummary = loadSummaryList.get(position);
                loadSummaryList.remove(position);
             //   holder.swipeLayout.close();
                activity.updateAdapter(tempList);

                //Logic to update is verified flag
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put(db.KEY_IS_VERIFIED,"true");

                HashMap<String,String> filters = new HashMap<>();
                filters.put(db.KEY_MATERIAL_NO, loadSummary.getMaterialNo());
                db.updateData(db.LOAD_DELIVERY_ITEMS,parameters,filters);

            }
        };
    }

    private View.OnClickListener onRejectListener(final int position, final ViewHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVarianceDialog(position, holder);
            }
        };
    }

    private void showVarianceDialog(final int position, final ViewHolder holder) {

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.alert_dialog_variance_load);
        Button btn_update = (Button)dialog.findViewById(R.id.btn_update_alert);
        Button btn_cancel = (Button)dialog.findViewById(R.id.btn_cancel_alert);

        TextView tv_item_code = (TextView)dialog.findViewById(R.id.lbl_item_code_alert_variance);
        TextView tv_item_description = (TextView)dialog.findViewById(R.id.lbl_item_description_alert_variance);
        final EditText et_quantity_case = (EditText)dialog.findViewById(R.id.et_quantitycs_alert_variance);
        final EditText et_quantity_unit = (EditText)dialog.findViewById(R.id.et_quantitybt_alert_variance);

        tv_item_code.setText(StringUtils.stripStart(loadSummaryList.get(position).getMaterialNo(), "0"));
        tv_item_description.setText(loadSummaryList.get(position).getItemDescription());
        et_quantity_case.setText(loadSummaryList.get(position).getQuantityCases());
        et_quantity_unit.setText(loadSummaryList.get(position).getQuantityUnits());

       // dialog.getWindow().setLayout(500,550);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSummaryList.get(position).setQuantityCases(et_quantity_case.getText().toString().trim());
                loadSummaryList.get(position).setQuantityUnits(et_quantity_unit.getText().toString().trim());
                activity.hideKeyboard(v);
                activity.updateAdapter(tempList);
              //  holder.swipeLayout.close();
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   holder.swipeLayout.close();
                dialog.dismiss();
                activity.hideKeyboard(v);
            }
        });

        dialog.show();
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
            Log.v("DataAdapter", "constratinst : " + loadSummaryList.size());
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<LoadSummary> filteredItems = new ArrayList<>();
                for (int i = 0, l = loadRequestListone.size(); i < l; i++) {
                    String p = loadRequestListone.get(i).getItemDescription().toLowerCase();
                    String m = loadRequestListone.get(i).getMaterialNo().toLowerCase();

                    if (p.contains(constraint) || m.contains(constraint)) {
                        filteredItems.add(loadRequestListone.get(i));
                        Log.v("DataAdapter", p + " -- " + loadRequestListone.get(i));
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.count = loadRequestListone.size();
                    result.values = loadRequestListone;
                }
            }
            return result;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            loadSummaryList = (ArrayList<LoadSummary>) results.values;
            Log.v("datalist", loadSummaryList.size() + "--");
            notifyDataSetChanged();
            notifyDataSetInvalidated();
        }
    }


    private class ViewHolder{
        private TextView item_code;
        private TextView item_description;
        private TextView quantity_cases;
        private TextView quantity_units;
        private TextView item_price;
     //   private View btn_accept;
      //  private View btn_reject;
     //   private SwipeLayout swipeLayout;

        public ViewHolder(View v) {
          //  swipeLayout = (SwipeLayout)v.findViewById(R.id.swipe_layout);
          //  btn_accept = v.findViewById(R.id.accept);
         //   btn_reject = v.findViewById(R.id.reject);
            item_code = (TextView) v.findViewById(R.id.lbl_item_code);
            item_description = (TextView) v.findViewById(R.id.lbl_item_description);
            quantity_cases = (TextView) v.findViewById(R.id.lbl_quantity_cases);
            quantity_units = (TextView) v.findViewById(R.id.lbl_quantity_units);
            item_price = (TextView)v.findViewById(R.id.lbl_item_price);

          //  swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        }
    }
}
