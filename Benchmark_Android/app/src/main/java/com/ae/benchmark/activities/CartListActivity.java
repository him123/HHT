package com.ae.benchmark.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.ae.benchmark.R;
import com.ae.benchmark.adapters.CustomCartListAdapter;
import com.ae.benchmark.utils.AlertDialogClass;
import com.ae.benchmark.utils.NetWorkCheck;

/**
 * Created by Dell on 13-02-2018.
 */

public class CartListActivity extends AppCompatActivity{

    private ProgressDialog mProgress;
    public static AlertDialogClass ad;
    Context mctx;
    public static NetWorkCheck nk;
    ListView lv_catelogue;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_layout);


        mctx = CartListActivity.this;
        nk = new NetWorkCheck(mctx);
        ad = new AlertDialogClass(mctx);
        lv_catelogue = (ListView) findViewById(R.id.lv_catalogue);

        CustomCartListAdapter adapter = new CustomCartListAdapter(this, CatelogueListActiviy.list_Cart, true);
        lv_catelogue.setAdapter(adapter);
    }
}
