package com.ae.benchmark.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.ae.benchmark.R;

import static android.support.v4.app.ActivityCompat.startActivity;
import static java.security.AccessController.getContext;

/**
 * Created by Muhammad Umair on 05/12/2016.
 */

public class UnloadAdapter extends BaseAdapter {

    private Context context;
    private final String[] mobileValues;

    public UnloadAdapter(Context context, String[] mobileValues) {
        this.context = context;
        this.mobileValues = mobileValues;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null)
        {
            gridView = new View(context);
            gridView = inflater.inflate(R.layout.activity_unload_items, null);
            TextView textView = (TextView) gridView
                    .findViewById(R.id.badReturnCases);
            textView.setText("Cases : 100");
            String mobile = mobileValues[position];
            if (mobile.equals("Bad Return"))
            {
                TextView tv = (TextView) gridView
                        .findViewById(R.id.badReturnLabel);
                tv.setText("Bad Return");

                Button btn = (Button) gridView.findViewById(R.id.badReturnEdit);
                btn.setText("Edit");

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent i=new Intent(v.getContext(),RecyclerViewExample.class);
                        v.getContext().startActivity(i);

                    }

                });
            }
            else if (mobile.equals("Truck Damage"))
            {
                TextView tv = (TextView) gridView
                        .findViewById(R.id.badReturnLabel);
                tv.setText("Truck Damage");
                Button btn = (Button) gridView.findViewById(R.id.badReturnEdit);
                btn.setText("Edit");


                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent i=new Intent(v.getContext(),RecyclerViewExample.class);
                        v.getContext().startActivity(i);

                    }

                });
            }
            else if (mobile.equals("Fresh Unload"))
            {
                TextView tv = (TextView) gridView
                        .findViewById(R.id.badReturnLabel);
                tv.setText("Fresh Unload");
                Button btn = (Button) gridView.findViewById(R.id.badReturnEdit);
                btn.setText("Edit");


                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent i=new Intent(v.getContext(),RecyclerViewExample.class);
                        v.getContext().startActivity(i);

                    }

                });
            }
            else if (mobile.equals("Ending Inventory"))
            {
                TextView tv = (TextView) gridView
                        .findViewById(R.id.badReturnLabel);
                tv.setText("Ending Inventory");
                Button btn = (Button) gridView.findViewById(R.id.badReturnEdit);
                btn.setText("Edit");


                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent i=new Intent(v.getContext(),RecyclerViewExample.class);
                        v.getContext().startActivity(i);

                    }

                });
            }
            else if (mobile.equals("INV. Variance"))
            {
                TextView tv = (TextView) gridView
                        .findViewById(R.id.badReturnLabel);
                tv.setText("INV. Variance");
                Button btn = (Button) gridView.findViewById(R.id.badReturnEdit);
                btn.setText("View");

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        final Dialog dialog = new Dialog(v.getContext());
                        dialog.setContentView(R.layout.activity_variance_popup);
                        dialog.setCancelable(true);
                        Button btnPrint = (Button) dialog.findViewById(R.id.btnPrint);

                        Button btnOk = (Button) dialog.findViewById(R.id.btnOk);

                        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

                        dialog.show();

                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                            });


                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });

                        btnPrint.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                Dialog dialog = new Dialog(v.getContext());
                                dialog.setContentView(R.layout.activity_print);
                                dialog.setTitle("Print Dialog");
                                Button print = (Button) dialog.findViewById(R.id.btnPrint);
                                Button cancel = (Button) dialog.findViewById(R.id.btnCancel2);

                                dialog.setCancelable(true);
                                dialog.show();

                                print.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent intent= new Intent(context, MyCalendarActivity.class);
                                        context.startActivity(intent);
                                    }
                                });

                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent= new Intent(context, MyCalendarActivity.class);
                                        context.startActivity(intent);

                                    }
                                });




                            }

                        });

                    }

                });

            }
            else if (mobile.equals("Bad RTN. Variance"))
            {
                TextView tv = (TextView) gridView
                        .findViewById(R.id.badReturnLabel);
                tv.setText("Bad RTN. Variance");
                Button btn = (Button) gridView.findViewById(R.id.badReturnEdit);
                btn.setText("View");



                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final Dialog dialog = new Dialog(v.getContext());
                        dialog.setContentView(R.layout.activity_variance_popup);
                        dialog.setCancelable(true);
                        Button btnPrint = (Button) dialog.findViewById(R.id.btnPrint);

                        Button btnOk = (Button) dialog.findViewById(R.id.btnOk);

                        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

                        dialog.show();

                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });


                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });

                        btnPrint.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                Dialog dialog = new Dialog(v.getContext());
                                dialog.setContentView(R.layout.activity_print);
                                dialog.setTitle("Print Dialog");
                                Button print = (Button) dialog.findViewById(R.id.btnPrint);
                                Button cancel = (Button) dialog.findViewById(R.id.btnCancel2);

                                dialog.setCancelable(true);
                                dialog.show();

                                print.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent intent= new Intent(context, MyCalendarActivity.class);
                                        context.startActivity(intent);
                                    }
                                });

                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent= new Intent(context, MyCalendarActivity.class);
                                        context.startActivity(intent);

                                    }
                                });




                            }

                        });

                    }

                });


            }

        }
        else
        {
            gridView = (View) convertView;
        }
        return gridView;
    }

    public int getCount()
    {
        return mobileValues.length;
    }
    public Object getItem(int position)
    {
        return null;
    }
    public long getItemId(int position)
    {
        return 0;
    }
}
