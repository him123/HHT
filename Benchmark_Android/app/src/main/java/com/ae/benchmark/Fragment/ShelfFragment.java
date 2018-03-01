package com.ae.benchmark.Fragment;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ae.benchmark.R;
import com.ae.benchmark.activities.CategoryListActivity;
import com.ae.benchmark.activities.SelectCustomerActivity;
import com.ae.benchmark.activities.ShelfStockActivity;
import com.ae.benchmark.adapters.ShelfAdapter;
import com.ae.benchmark.data.Const;
import com.ae.benchmark.models.ShelfProduct;
import com.ae.benchmark.utils.DatabaseHandler;
import com.ae.benchmark.utils.PostData;
import com.ae.benchmark.utils.Settings;

/**
 * Created by eheuristic on 12/5/2016.
 */
public class ShelfFragment extends Fragment {

    public static DatabaseHandler db;
    PostData pObject=new PostData();

    View viewmain;
    ListView listShelf;
    public static ShelfAdapter adapter;
    FloatingActionButton fab;
//    ArrayList<CustomerData> dataArrayList;
    public static ArrayList<ShelfProduct> arrayList;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewmain = inflater.inflate(R.layout.fragment_shelf, container, false);
        listShelf = (ListView) viewmain.findViewById(R.id.list_shelf);
        fab = (FloatingActionButton) viewmain.findViewById(R.id.fab);
        String strProductname[] = {"AM5508 LARGE SWISS ROLL VANILL (24x110G)", "BM5508 SMALL SWISS ROLL VANILL (24x110G)", "CM5508 BIG SWISS ROLL VANILL (24x110G)", "DM5508 SWISS ROLL VANILL (24x110G)", "EM5508 ROLL VANILL (24x110G)"};
        Const.addlist.clear();
        arrayList = new ArrayList<>();
        adapter = new ShelfAdapter(getActivity().getBaseContext(), arrayList, R.layout.sales_list);
        listShelf.setAdapter(adapter);
        listShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {


                final ShelfProduct product = arrayList.get(position);
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_with_crossbutton);
                TextView tv = (TextView)dialog.findViewById(R.id.dv_title);
                tv.setText(product.getProductname());
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                ImageView iv_cancle = (ImageView) dialog.findViewById(R.id.imageView_close);
                Button btn_save = (Button) dialog.findViewById(R.id.btn_save);
                final EditText ed_cases = (EditText) dialog.findViewById(R.id.ed_cases);
                final EditText ed_pcs = (EditText) dialog.findViewById(R.id.ed_pcs);
                LinearLayout ll_1 = (LinearLayout) dialog.findViewById(R.id.ll_1);
                ll_1.setVisibility(View.GONE);
                RelativeLayout rl_specify=(RelativeLayout) dialog.findViewById(R.id.rl_specify_reason);
                rl_specify.setVisibility(View.GONE);

                iv_cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
                btn_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strCase = ed_cases.getText().toString();
                        String strpcs = ed_pcs.getText().toString();
                        TextView tv_cases = (TextView) view.findViewById(R.id.tv_cases_value);
                        TextView tv_pcs = (TextView) view.findViewById(R.id.tv_pcs_value);


                        if(ed_cases.getText().toString().isEmpty()||ed_cases.getText().toString()==null){
                            strCase = "0";


                        }

                        if(ed_pcs.getText().toString().isEmpty()||ed_pcs.getText().toString()==null){
                            strpcs = "0";

                        }

                        tv_cases.setText(strCase);
                        tv_pcs.setText(strpcs);


                        product.setPro_pcs(Integer.parseInt(strpcs));
                        product.setPro_case(Integer.parseInt(strCase));
                        double total = 0;
                        db=new DatabaseHandler(getActivity());
                        for (int i = 0; i < arrayList.size(); i++) {
                            ShelfProduct product1 = arrayList.get(i);
                            total = total + (product1.getPro_case() * 54 + product1.getPro_pcs() * 2.25);
                            Log.v("pos", product1.getPro_case() + " - " + product1.getPro_pcs() + " - " + total);



                            String customerID= SelectCustomerActivity.distributionCustomerNo;

                            System.out.println("Customer id :"+customerID);
                            String shelfType="1";
                            String shelfDescription="shelf";
                            String materialNo= ""+ShelfStockActivity.distributionsData;
                            String materialName=product.getProductname();
                            String distributionCases= ""+product1.getPro_case();
                            String distributionPieces=""+product1.getPro_pcs();
                            String distributionDate=new SimpleDateFormat("yyyyMMdd").format(new Date());

                            if(distributionCases.equals("0") || distributionPieces.equals("0"))
                            {
//                                HashMap<String, String> headerParams = new HashMap<>();
//                                headerParams.put(db.KEY_CUSTOMER  ,customerID);
//                                headerParams.put(db.KEY_SHELF_TYPE ,shelfType);
//                                headerParams.put(db.KEY_SHELF_DESCRIPTION ,shelfDescription);
//                                headerParams.put(db.KEY_MATERIAL_NUMBER ,materialNo);
//                                headerParams.put(db.KEY_MATERIAL_NAME  ,materialName);
//                                headerParams.put(db.KEY_DISTRIBUTION_CASES  ,distributionCases);
//                                headerParams.put(db.KEY_DISTRIBUTION_PIECES ,distributionPieces);
//                                headerParams.put(db.KEY_DISTRIBUTION_DATE ,distributionDate);
//                                headerParams.put(db.KEY_IS_POSTED ,"1");
//                                db.addData(db.DISTRIBUTIONS, headerParams);
                                break;
                            }
                            else
                            {
                                HashMap<String, String> headerParams = new HashMap<>();
                                headerParams.put(db.KEY_CUSTOMER  ,customerID);
                                headerParams.put(db.KEY_SHELF_TYPE ,shelfType);
                                headerParams.put(db.KEY_SHELF_DESCRIPTION ,shelfDescription);
                                headerParams.put(db.KEY_MATERIAL_NUMBER ,materialNo);
                                headerParams.put(db.KEY_MATERIAL_NAME  ,materialName);
                                headerParams.put(db.KEY_DISTRIBUTION_CASES  ,distributionCases);
                                headerParams.put(db.KEY_DISTRIBUTION_PIECES ,distributionPieces);
                                headerParams.put(db.KEY_DISTRIBUTION_DATE ,distributionDate);
                                headerParams.put(db.KEY_IS_POSTED ,"0");
                                db.addData(db.DISTRIBUTIONS, headerParams);
                            }
                        }
                        TextView tv = (TextView) viewmain.findViewById(R.id.tv_total);
                        tv.setText(String.valueOf(total));

                        dialog.dismiss();

                       // new PostDistribution().execute();

                    }
                });
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Settings.setString("from","shelf");
                ShelfStockActivity.tab_position=0;
                Intent intent = new Intent(getActivity(), CategoryListActivity.class);

                getActivity().startActivity(intent);
            }
        });
        return viewmain;
    }


}
