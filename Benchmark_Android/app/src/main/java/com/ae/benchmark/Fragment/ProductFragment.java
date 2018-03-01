package com.ae.benchmark.Fragment;
import android.content.ClipData;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.ItemComplaints;
import com.ae.benchmark.activities.PriceSurvey;
import com.ae.benchmark.activities.SalesInvoiceActivity;
import com.ae.benchmark.adapters.BalanceBadgeAdapter;
import com.ae.benchmark.adapters.ProductListAdapter;
import com.ae.benchmark.data.ArticleHeaders;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.ArticleHeader;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.Helpers;
import com.ae.benchmark.utils.UrlBuilder;
/**
 * Created by eheuristic on 12/21/2016.
 */
public class ProductFragment extends Fragment {
    ListView list_product;
    View view;
    View common_header;
    ArrayList<String> arrayList;
  public static   ProductListAdapter adapter;
    FloatingActionButton button;
    ArrayList<ArticleHeader> articles = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_product_list, container, false);
        list_product = (ListView) view.findViewById(R.id.list_product);
        articles = ArticleHeaders.get();
        common_header = (View) view.findViewById(R.id.inc_common_header);
        common_header.setVisibility(View.GONE);
        button = (FloatingActionButton) view.findViewById(R.id.btn_float);
        arrayList = new ArrayList<>();


        if (SalesInvoiceActivity.tab_position == 98)
        {
            DatabaseHandler db = new DatabaseHandler(getActivity());
            HashMap<String, String> map = new HashMap<>();
            map.put(db.KEY_ITEM_NAME,"");
            HashMap<String, String> filters = new HashMap<>();
            Cursor cursor = db.getData(db.PRICE_SURVEY,map,filters);
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                do
                {
                    arrayList.add(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NAME)).replaceAll("%20"," "));
                }
                while (cursor.moveToNext());
            }

        }
        else
        {
            for (ArticleHeader article : articles)
            {
                arrayList.add(UrlBuilder.decodeString(article.getMaterialDesc1()));
            }
        }
        adapter = new ProductListAdapter(getActivity(), arrayList, R.layout.checkable_productlist, "product");
        list_product.setAdapter(adapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("IN PRODUCT CLicked", "CLICK");
                if (SalesInvoiceActivity.tab_position == 2) {
                    GListFragment.setProductList();
                } else if (SalesInvoiceActivity.tab_position == 3) {
                    BListFragment.setProductList();
                }
                else if (SalesInvoiceActivity.tab_position == 98) {


                    for (int i = 0; i < PriceSurvey.arrProductList.size(); i++) {
                        DatabaseHandler db = new DatabaseHandler(getActivity());
                        HashMap<String, String> map = new HashMap<>();
                        map.put(db.KEY_ITEM_NAME, "");
                        map.put(db.KEY_ITEM_CODE, "");
                        HashMap<String, String> filters = new HashMap<>();
                        filters.put(db.KEY_ITEM_NAME, PriceSurvey.arrProductList.get(i).toString());

                        System.out.println("Size :" + PriceSurvey.arrProductList.size() + " ----- " + PriceSurvey.arrProductList.get(i));

                        Cursor cursor = db.getData(db.PRICE_SURVEY, map, filters);
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do
                            {
                                PriceSurvey.itemName.add(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_NAME)).replaceAll("%20", " "));
                                PriceSurvey.itemCode.add(cursor.getString(cursor.getColumnIndex(db.KEY_ITEM_CODE)).replaceAll("%20", " "));
                                PriceSurvey.adapter.notifyDataSetChanged();
                            }
                            while (cursor.moveToNext());
                        }


                    }
                    PriceSurvey.arrProductList.clear();
                }
                else if (SalesInvoiceActivity.tab_position == 99) {
                    ItemComplaints.setProductList();
                }

                getActivity().finish();
            }
        });
        return view;
    }
}
