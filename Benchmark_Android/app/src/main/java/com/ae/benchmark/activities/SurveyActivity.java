package com.ae.benchmark.activities;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ae.benchmark.R;
import com.ae.benchmark.data.CustomerHeaders;
import com.ae.benchmark.models.Customer;
import com.ae.benchmark.models.CustomerHeader;
import com.ae.benchmark.models.Survey;
import com.ae.benchmark.utils.DatabaseHandler;

/**
 * Created by Rakshit on 06-Mar-17.
 */
public class SurveyActivity extends AppCompatActivity {
    private ArrayList<Survey> arrayList = new ArrayList<>();
    LinearLayout surveyLayout;
    private static int viewsCount = 0;
    private List<View> allViews = new ArrayList<View>();
    String[] values = new String[] { "Berain", "Fayha", "Aquafina",
            "Oasis", "Lulu", "Bisleri", "Mai Dubai"};
    LinearLayout.LayoutParams params;
    ImageView iv_back;
    TextView tv_top_header;
    Button btn_Save;
    Customer object;
    ArrayList<CustomerHeader> customers;

    int count=0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Intent i = this.getIntent();
        object = (Customer) i.getParcelableExtra("headerObj");
        iv_back = (ImageView) findViewById(R.id.toolbar_iv_back);
        tv_top_header = (TextView) findViewById(R.id.tv_top_header);
        iv_back.setVisibility(View.VISIBLE);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText("Survey");
        // To set Margin for the child Views
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 5, 5, 5);
        surveyLayout = (LinearLayout) findViewById(R.id.surveyLayout);
        btn_Save = (Button)findViewById(R.id.btnProcess);
        generateSurveyData();
        btn_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i=0;i<allViews.size();i++) {
                    if(allViews.get(i).getClass().getName().equals("android.widget.EditText"))
                    {
                        EditText et=(EditText) allViews.get(i);
                        System.out.println("edittext : " + et.getText().toString());
                    }
                    if(allViews.get(i).getClass().getName().equals("android.widget.Spinner"))
                    {
                        Spinner sp=(Spinner) allViews.get(i);
                        System.out.println("spinner : " + sp.getSelectedItem().toString());
                    }
                    if(allViews.get(i).getClass().getName().equals("android.widget.CheckBox"))
                    {
                        CheckBox cb=(CheckBox) allViews.get(i);
                        if(cb.isChecked())
                            System.out.println("checkbox enabled : " + cb.getText().toString());
                    }
                    if(allViews.get(i).getClass().getName().equals("android.widget.RadioButton"))
                    {

                        RadioButton rb=(RadioButton) allViews.get(i);
                        if(rb.isChecked())
                            System.out.println("radio enabled : " + rb.getText().toString());
//                        RadioGroup rg=(RadioGroup) allViews.get(i);
//                        if(rg.getCheckedRadioButtonId()!=-1)
//                        {
//                            int id= rg.getCheckedRadioButtonId();
//                            View radioButton = rg.findViewById(id);
//                            int radioId = rg.indexOfChild(radioButton);
//                            final RadioButton btn = (RadioButton) rg.getChildAt(radioId);
//
//                            btn.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    String selection = (String) btn.getText();
//                                    System.out.println("radio group : " +selection);
//                                }
//                            });
//                        }

                    }
                }              // finish();
            }
        });
        customers = CustomerHeaders.get();
//        CustomerHeader customerHeader = CustomerHeader.getCustomer(customers, object.getCustomerID());
//        TextView tv_customer_name = (TextView) findViewById(R.id.tv_customer_id);
//        TextView tv_customer_address = (TextView) findViewById(R.id.tv_customer_address);
//        TextView tv_customer_pobox = (TextView) findViewById(R.id.tv_customer_pobox);
//        TextView tv_customer_contact = (TextView) findViewById(R.id.tv_customer_contact);
//        if (!(customerHeader == null)) {
//            tv_customer_name.setText(StringUtils.stripStart(customerHeader.getCustomerNo(), "0") + " " + UrlBuilder.decodeString(customerHeader.getName1()));
//            tv_customer_address.setText(UrlBuilder.decodeString(customerHeader.getStreet()));
//            tv_customer_pobox.setText(getString(R.string.pobox) + " " + customerHeader.getPostCode());
//            tv_customer_contact.setText(customerHeader.getPhone());
//        } else {
//            tv_customer_name.setText(StringUtils.stripStart(object.getCustomerID(),"0") + " " + UrlBuilder.decodeString(object.getCustomerName().toString()));
//            tv_customer_address.setText(object.getCustomerAddress().toString());
//            tv_customer_pobox.setText("");
//            tv_customer_contact.setText("");
//        }
    }

    private ArrayList<Survey> generateSurveyData(){

        DatabaseHandler db = new DatabaseHandler(SurveyActivity.this);
        HashMap<String, String> map = new HashMap<>();
        map.put(db.KEY_CUSTOMER_NO, "");
        map.put(db.KEY_CUSTOMER_NAME, "");
        map.put(db.KEY_DRIVER_ID, "");
        map.put(db.KEY_DRIVER_NAME, "");
        map.put(db.KEY_QUESTION_ID, "");
        map.put(db.KEY_QUESTION_TEXT, "");
        map.put(db.KEY_QUESTION_DATE, "");

        HashMap<String, String> filters = new HashMap<>();
        Cursor cursor = db.getData(db.SURVEY, map, filters);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do
            {
               if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_ID)).replaceAll("%20", " ").equals("22"))
               {
                    if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " ").equals(""))
                    {
                        createTextView("Q) Question text not found");
                        createEditText("",count);
                        System.out.println("et id = "+count);
                    }
                    else
                    {
                        createTextView("Q) "+cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " "));
                        createEditText("",count);
                        System.out.println("et id = "+viewsCount);
                    }
               }
               else if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_ID)).replaceAll("%20", " ").equals("4"))
               {
                   if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " ").equals(""))
                   {
                       createTextView("Q) Question text not found");
                       createSpinner(values,count);
                       System.out.println("spinner id = "+count);
                   }
                   else
                   {
                       createTextView("Q) "+cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " "));
                       createSpinner(values,count);
                       System.out.println("spinner id = "+count);
                   }

               }

               else if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_ID)).replaceAll("%20", " ").equals("2"))
               {
                   if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " ").equals(""))
                   {
                       createTextView("Q) Question text not found");
                       String []options={"option 1","option 2","option 3"};
                       for(int i=0;i<options.length;i++)
                       {
                           createCheckBox(options[i],count);
                           System.out.println("cb id = "+count);
                       }
                   }
                   else
                   {
                       createTextView("Q) "+cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " "));
                       String []options={"option 1","option 2","option 3"};
                       for(int i=0;i<options.length;i++)
                       {
                           createCheckBox(options[i],count);
                           System.out.println("cb id = "+count);
                       }

                   }

               }

               else if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_ID)).replaceAll("%20", " ").equals("5"))
               {
                   if(cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " ").equals(""))
                   {
                       createTextView("Q) Question text not found");
                      // String []options={"option 1","option 2","option 3"};
                      // createRadioGroup(options,count);

                       String []options={"option 1","option 2","option 3"};
                       for(int i=0;i<options.length;i++)
                       {
                           createRadioButton(options[i],count);
                           System.out.println("rb id = "+count);
                       }

                   }
                   else
                   {
                       createTextView("Q) "+cursor.getString(cursor.getColumnIndex(db.KEY_QUESTION_TEXT)).replaceAll("%20", " "));
                     //  String []options={"option 1","option 2","option 3"};
                       //createRadioGroup(options,count);

                       String []options={"option 1","option 2","option 3"};
                       for(int i=0;i<options.length;i++)
                       {
                           createRadioButton(options[i],count);
                           System.out.println("rb id = "+count);
                       }
                   }
               }
                count++;
            }
            while (cursor.moveToNext());
        }
        return arrayList;
    }

    public void createTextView(String hint) {
        TextView textView = new TextView(this);
        textView.setId(viewsCount++);
        textView.setText(hint);
       // allViews.add(textView);
        surveyLayout.addView(textView,params);
    }

    public void createEditText(String hint,int id) {
        EditText editText = new EditText(this);
        editText.setId(id);
        editText.setHint(hint);
        allViews.add(editText);

        surveyLayout.addView(editText,params);
    }

    public void createSpinner(String[] spinnerList,int id) {

        Spinner spinner = new Spinner(this);
        spinner.setId(id);
        //spinner.setBackgroundResource(R.drawable.dropdown_normal_holo_light);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        spinner.setAdapter(spinnerArrayAdapter);
        allViews.add(spinner);
        surveyLayout.addView(spinner,params);
    }

//    public void createRadioGroup(String[] radioGroupOptions,int id)
//    {
//        RadioGroup radioGroup = new RadioGroup(this);
//        radioGroup.setOrientation(RadioGroup.HORIZONTAL);//or RadioGroup.VERTICAL
//        radioGroup.setId(id);
//
//        for(int i=0; i<radioGroupOptions.length; i++)
//        {
//            final RadioButton rb  = new RadioButton(this);
//            rb.setId((id-2)*radioGroupOptions.length);
//            rb.setText(radioGroupOptions[i]);
//
//            rb.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//            radioGroup.addView(rb);
//        }
//        allViews.add(radioGroup);
//        surveyLayout.addView(radioGroup,params);
//    }


    public void createRadioButton(String label,int id) {
        final RadioButton radioButton = new RadioButton(this);
        radioButton.setId(id);
        radioButton.setText(label);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        allViews.add(radioButton);
        surveyLayout.addView(radioButton,params);
    }

    public void createCheckBox(String label,int id) {
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setId(id);
        checkBox.setText(label);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        allViews.add(checkBox);
        surveyLayout.addView(checkBox,params);
    }
}
