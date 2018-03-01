package com.ae.benchmark.adapters;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ae.benchmark.App;
import com.ae.benchmark.R;
import com.ae.benchmark.activities.CustomerDetailActivity;
import com.ae.benchmark.data.DriverRouteFlags;

/**
 * Created by eheuristic on 12/3/2016.
 */
public class CustomerOperationAdapter extends BaseAdapter {
    Context context;
    String strText[];
    String from = "";
    int resarr[];
    App.DriverRouteControl flag = new App.DriverRouteControl();
    public CustomerOperationAdapter(Context context, String[] strText, int resarr[], String from) {
        this.context = context;
        this.strText = strText;
        this.resarr = resarr;
        this.from = from;
        flag = DriverRouteFlags.get();
    }
    @Override
    public int getCount() {
        return strText.length;
    }
    @Override
    public Object getItem(int position) {
        return strText[position];
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if (from.equals("InformationsActivity") || from.equals("ReviewActivity")) {
                convertView = inflater.inflate(R.layout.information_grid, null);
            } else {
                convertView = inflater.inflate(R.layout.custom_grid, null);
            }
        }
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.ll_items);
        ImageView roundedImageView = (ImageView) convertView.findViewById(R.id.iv_round);
        roundedImageView.setImageResource(resarr[position]);
        ImageView lock_img = (ImageView)convertView.findViewById(R.id.img_lock);
        //ImageView lockView = (ImageView) convertView.findViewById(R.id.img_lock);
        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_iv_title);
        tv_title.setText(strText[position]);
        if(from.equals("InformationsActivity")){
            if(position==2||position==3) {
                // layout.setForeground(context.getDrawable(R.drawable.lock));
                lock_img.setVisibility(View.INVISIBLE);
            }

        }
        if(from.equals("CustomerDetailActivity")){
            if(position==2) {
                if (!CustomerDetailActivity.paymentMethod.equalsIgnoreCase(App.CASH_CUSTOMER)) {
                    try {
                        if (Double.parseDouble(CustomerDetailActivity.aviLimit.replaceAll("٫",".")) <= 0) {
                            roundedImageView.setColorFilter(ContextCompat.getColor(context, R.color.gray));
                        } else {
                            roundedImageView.setColorFilter(ContextCompat.getColor(context, R.color.iconcolor));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        roundedImageView.setColorFilter(ContextCompat.getColor(context, R.color.iconcolor));
                    }
                } else {
                    roundedImageView.setColorFilter(ContextCompat.getColor(context, R.color.iconcolor));
                }
            }
        }
        if(from.equals("ReviewActivity")){
            if(position==1||position==3||position==4){
                // layout.setForeground(context.getDrawable(R.drawable.lock));
                lock_img.setVisibility(View.INVISIBLE);
            }

        }
        else if (from.equals("SalesInvoiceOptionActivity")) {
            if(position==1){
                if(!(flag==null)){
                    if(!flag.isDisplayInvoiceSummary()){
                        lock_img.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        return convertView;
    }
}
