package com.ae.benchmark.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ae.benchmark.App;
import com.ae.benchmark.Fragment.AllCustomerFragment;
import com.ae.benchmark.Fragment.VisitAllFragment;
import com.ae.benchmark.R;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.utils.Settings;
import com.ae.benchmark.utils.UrlBuilder;
import com.ae.benchmark.views.TextViewWithLabel;

/**
 * Created by eheuristic on 10/10/2016.
 */

public class DataExpandAdapter extends BaseExpandableListAdapter {

    private ArrayList<Customer> dataList;
    private ArrayList<Customer> dataListOne;
    private Context mContext;

    private List<String> _listDataHeader; // header titles
    private List<String> originalHeader; // header titles

    private TreeMap<String, List<Customer>> _listDataChild;
    private TreeMap<String, List<Customer>> originalList;

    ExpandableListView list;
    String type;//AllCustomer , "VisitAll"

    public DataExpandAdapter(Context context, List<String> listDataHeader,
                             TreeMap<String, List<Customer>> listChildData, ExpandableListView listView,String typeFragment) {
        this.mContext = context;
        this._listDataHeader = new ArrayList<>();
        this._listDataHeader.addAll(listDataHeader);
        this.originalHeader = new ArrayList<>();
        this.originalHeader.addAll(listDataHeader);

        this._listDataChild = new TreeMap<>();
        this._listDataChild.putAll(listChildData);
        this.originalList = new TreeMap<>();
        this.originalList.putAll(listChildData);
        this.list = listView;
        this.type = typeFragment;
    }
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        LayoutInflater infalInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = infalInflater.inflate(R.layout.list_header, null);

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        if(headerTitle.equals("ZOthers")){
            lblListHeader.setText("Others");
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View view, ViewGroup parent) {

        ItemRowHolder holder = null;
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.badge_journey_plan, null);
//            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user_contact, null);
            holder = new ItemRowHolder();
            holder.horizontal_view=(View)view.findViewById(R.id.view);
            holder.customer_id = (TextViewWithLabel) view.findViewById(R.id.customer_id);
            holder.customer_name = (TextViewWithLabel) view.findViewById(R.id.customer_name);
            holder.customer_address = (TextViewWithLabel) view.findViewById(R.id.customer_address);
            holder.saleflag = (ImageView) view.findViewById(R.id.img_sales);
            holder.deliveryflag = (ImageView) view.findViewById(R.id.img_delivery);
            holder.collectionflag = (ImageView) view.findViewById(R.id.img_collection);
            holder.merchandizeFlag = (ImageView) view.findViewById(R.id.img_merchandize);
            holder.orderFlag = (ImageView) view.findViewById(R.id.img_order);
            holder.newCustomer = (ImageView)view.findViewById(R.id.img_customer_new);
            holder.pendingDelivery = (ImageView)view.findViewById(R.id.img_customer_delivery);
            holder.iv_preferred = (ImageView)view.findViewById(R.id.iv_preferred);
            holder.tv_prefer=(TextView)view.findViewById(R.id.tv_prefer);


            view.setTag(holder);
        } else {
            holder = (ItemRowHolder) view.getTag();
        }

        final Customer customer = (Customer) getChild(groupPosition, childPosition);
        holder.customer_id.setText(StringUtils.stripStart(customer.getCustomerID(), "0"));
        holder.customer_name.setText(UrlBuilder.decodeString(customer.getCustomerName()));
        holder.customer_address.setText(customer.getPhoneNumber());
        holder.tv_prefer.setText(customer.getZpreferred());
        holder.tv_prefer.setVisibility(View.GONE);


        if(customer.getZpreferred()!=null)
        {
            if(customer.getZpreferred().equalsIgnoreCase("Morning")) {
                holder.iv_preferred.setImageResource(R.drawable.ic_morning);
            }else if(customer.getZpreferred().equalsIgnoreCase("Evening")){
                holder.iv_preferred.setImageResource(R.drawable.ic_evening);
            }
            else {
                holder.iv_preferred.setImageResource(0);
            }

        }
        else {
            holder.iv_preferred.setImageResource(0);
        }



        if(customer.getPaymentMethod().equals(App.TC_CUSTOMER)){
            holder.horizontal_view.setBackgroundColor(Color.rgb(255,194,0));
        }
        else if(customer.getPaymentMethod().equals(App.CASH_CUSTOMER)){
            holder.horizontal_view.setBackgroundColor(Color.BLUE);
        }
        else if(customer.getPaymentMethod().equals(App.CREDIT_CUSTOMER)){
            holder.horizontal_view.setBackgroundColor(Color.RED);
        }
        else{
            holder.horizontal_view.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.orderFlag.setAlpha(customer.isOrder()?1f:.3f);
        holder.saleflag.setAlpha(customer.isSale()?1f:.3f);
        holder.collectionflag.setAlpha(customer.isCollection()?1f:.3f);
        holder.deliveryflag.setAlpha(customer.isDelivery()?1f:.3f);
        holder.merchandizeFlag.setAlpha(customer.isMerchandize()?1f:.3f);
        //holder.newCustomer.setVisibility(customer.isNewCustomer()?View.VISIBLE:View.INVISIBLE);
        holder.pendingDelivery.setVisibility(customer.isOpenDelivery()?View.VISIBLE:View.INVISIBLE);

        if(customer.getCustomerID().contains("CUS") && customer.getCustomerID().contains(Settings.getString(App.SEQ))){
            holder.newCustomer.setVisibility(View.VISIBLE);
        }else{
            holder.newCustomer.setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type.equals("VisitAll")){
                    VisitAllFragment.getInstance().viewCustomer(customer,true);
                }else{
                    AllCustomerFragment.getInstance().viewCustomer(customer,true);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(type.equals("VisitAll")){
                    VisitAllFragment.getInstance().viewCustomer(customer,false);
                }else{
                    AllCustomerFragment.getInstance().viewCustomer(customer,false);
                }
                return false;
            }
        });

        return view;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void filterData(final String query, Activity activity){
                Log.v("MyListAdapter", String.valueOf(_listDataChild.size()));
                _listDataChild.clear();
                _listDataHeader.clear();

                if(query.equals("")){
                    _listDataChild.putAll(originalList);
                    _listDataHeader.addAll(originalHeader);
                }
                else {
                    List<Customer> noAreaCustomer = new ArrayList<>();
                    for (Map.Entry<String, List<Customer>> entry : originalList.entrySet()){
                        Log.w("dd:", "Key = " + entry.getKey() + ", Value = " +
                                entry.getValue());
                        List<Customer> addCustomer = new ArrayList<>();
                        for(Customer data : entry.getValue()) {
                            if (data.getCustomerName().toLowerCase().contains(query.toLowerCase()) || data.getArea().toLowerCase().contains(query.toLowerCase()) || data.getCustomerID().toLowerCase().contains(query.toLowerCase())) {
                                if(!entry.getKey().equals("ZOthers")) {
                                    addCustomer.add(data);
                                }else{
                                    noAreaCustomer.add(data);
                                }
                            }
                        }

                        if (addCustomer.size() > 0) {
                            _listDataChild.put(entry.getKey(), addCustomer);
                            if (!_listDataHeader.contains(entry.getKey())) {
                                _listDataHeader.add(entry.getKey());
                            }
                        }


                    }
                    if(noAreaCustomer.size()>0){
                        _listDataChild.put("ZOthers",noAreaCustomer);
                        if(!_listDataHeader.contains("ZOthers")){
                            _listDataHeader.add("ZOthers");
                        }
                    }
                }
    }



    public class ItemRowHolder {
        ImageView saleflag;
        ImageView deliveryflag;
        ImageView collectionflag;
        ImageView merchandizeFlag;
        ImageView orderFlag;
        ImageView newCustomer;
        ImageView pendingDelivery;
        TextViewWithLabel customer_id;
        TextViewWithLabel customer_name;
        TextViewWithLabel customer_address;
        View horizontal_view;
        TextView tv_prefer;
        ImageView iv_preferred;
    }

}