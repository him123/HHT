package com.ae.benchmark.adapters;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ae.benchmark.Fragment.AllCustomerFragment;
import com.ae.benchmark.Fragment.BListFragment;
import com.ae.benchmark.Fragment.BeginDayFragment;
import com.ae.benchmark.Fragment.CaptureImageFragment;
import com.ae.benchmark.Fragment.CategoryFragment;
import com.ae.benchmark.Fragment.FocFragment;
import com.ae.benchmark.Fragment.GListFragment;
import com.ae.benchmark.Fragment.MessageFragment;
import com.ae.benchmark.Fragment.ProductFragment;
import com.ae.benchmark.Fragment.SalesFragment;
import com.ae.benchmark.Fragment.ShelfFragment;
import com.ae.benchmark.Fragment.StoreFragment;
import com.ae.benchmark.Fragment.VisitAllFragment;
/**
 * Created by eheuristic on 12/2/2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    String activity;
    Bundle bundle;
    public PagerAdapter(FragmentManager fm, int NumOfTabs, String activtyname) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        activity = activtyname;
    }
    public PagerAdapter(FragmentManager fm, int NumOfTabs, String activtyname, Bundle bundle) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.bundle = bundle;
        activity = activtyname;
    }
    @Override
    public Fragment getItem(int position) {
        if (activity.equals("b")) {
            switch (position) {
                case 0:
                    BeginDayFragment tab1 = new BeginDayFragment();
                    if (bundle != null) {
                        tab1.setArguments(this.bundle);
                    }
                    return tab1;
                case 1:
                    MessageFragment tab2 = new MessageFragment();
                    return tab2;
                default:
                    return null;
            }
        } else if (activity.equals("s")) {
            switch (position) {
                case 0:
                    VisitAllFragment tab1 = new VisitAllFragment();
                    return tab1;
                case 1:
                    AllCustomerFragment tab2 = new AllCustomerFragment();
                    return tab2;
                default:
                    return null;
            }
        } else if (activity.equals("sales")) {
            switch (position) {
                case 0:
                    SalesFragment tab1 = new SalesFragment();
                    if (bundle != null) {
                        tab1.setArguments(this.bundle);
                    }
                    return tab1;
                case 1:
                    FocFragment tab2 = new FocFragment();
                    if (bundle != null) {
                        tab2.setArguments(this.bundle);
                    }
                    return tab2;
                case 2:
                    GListFragment tab3 = new GListFragment();
                    if (bundle != null) {
                        tab3.setArguments(this.bundle);
                    }
                    return tab3;
                case 3:
                    BListFragment tab4 = new BListFragment();
                    if (bundle != null) {
                        tab4.setArguments(this.bundle);
                    }
                    return tab4;
                default:
                    return null;
            }
        } else if (activity.equals("shelf")) {
            switch (position) {
                case 0:
                    ShelfFragment tab1 = new ShelfFragment();
                    return tab1;
                case 1:
                    StoreFragment tab2 = new StoreFragment();
                    return tab2;
                default:
                    return null;
            }
        } else if (activity.equals("capture")) {
            switch (position) {
                case 0:
                    CaptureImageFragment tab1 = new CaptureImageFragment();
                    return tab1;
                case 1:
                    CaptureImageFragment tab2 = new CaptureImageFragment();
                    return tab2;
                default:
                    return null;
            }
        } else if (activity.equals("category")) {
            switch (position) {
                case 0:
                    ProductFragment tab1 = new ProductFragment();
                    return tab1;
               /* case 1:
                    CategoryFragment tab2 = new CategoryFragment();
                    return tab2;*/
                default:
                    return null;
            }
        }
        return null;
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}