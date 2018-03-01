package com.ae.benchmark.Fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

import com.ae.benchmark.R;
import com.ae.benchmark.adapters.CategoryExpandAdapter;
import com.ae.benchmark.adapters.ProductListAdapter;
import com.ae.benchmark.utils.AnimatedExpandableListView;

/**
 * Created by eheuristic on 12/21/2016.
 */

public class CategoryFragment extends Fragment {



   ExpandableListView list_product;
    View view;

    ArrayList<String> groupList;
   HashMap<String ,ArrayList<String >> childList;

    CategoryExpandAdapter adapter;
    FloatingActionButton button;
    RelativeLayout rl_pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view =inflater.inflate(R.layout.category_expand_adapter, container, false);
        list_product=(ExpandableListView) view.findViewById(R.id.list_product);



        rl_pager=(RelativeLayout)view.findViewById(R.id.rl_pager);


        button=(FloatingActionButton)view.findViewById(R.id.btn_float);


        groupList=new ArrayList<>();
        groupList.add("a");
        groupList.add("b");
        groupList.add("c");
        groupList.add("d");


        ArrayList<String> pen=new ArrayList<>();
        pen.add("AM5508 LARGE SWISS ROLL VANILL (24x110G)");
        pen.add("AM5508 SMALL SWISS ROLL VANILL (24x110G)");



        ArrayList<String> van=new ArrayList<>();
        van.add("BM5508 SMALL SWISS ROLL VANILL (24x110G)");
        van.add("BM5508 LARGE SWISS ROLL VANILL (24x110G)");



        ArrayList<String> san=new ArrayList<>();
        san.add("CM5508 BIG SWISS ROLL VANILL (24x110G)");
        san.add("CM5508 SMALL SWISS ROLL VANILL (24x110G)");



        ArrayList<String> can=new ArrayList<>();
        can.add("DM5508 BIG SWISS ROLL VANILL (24x110G)");
        can.add("DM5508 SMALL SWISS ROLL VANILL (24x110G)");



        childList=new HashMap<>();
        childList.put(groupList.get(0),pen);
        childList.put(groupList.get(1),van);
        childList.put(groupList.get(2),san);
        childList.put(groupList.get(3),can);



        adapter=new CategoryExpandAdapter(getActivity(),groupList,childList);
        list_product.setAdapter(adapter);


        list_product.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            return false;
        }
    });

        list_product.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });

        list_product.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });

        list_product.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {

            }
        });


         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 getActivity().finish();
             }
         });


        return view;

    }
}
