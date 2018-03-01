package com.ae.benchmark.activities;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.ae.benchmark.R;
import com.ae.benchmark.adapters.GalleryImageAdapter;
import com.ae.benchmark.models.Advertisement;
import com.ae.benchmark.utils.DatabaseHandler;

/**
 * Created by Rakshit on 16-Apr-17.
 */
public class AdvertizingActivity extends AppCompatActivity {

    DatabaseHandler db = new DatabaseHandler(this);
    ImageView iv_back;
    TextView tv_top_header;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);

        iv_back=(ImageView)findViewById(R.id.toolbar_iv_back);
        tv_top_header=(TextView)findViewById(R.id.tv_top_header);
        iv_back.setVisibility(View.VISIBLE);
        tv_top_header.setVisibility(View.VISIBLE);
        tv_top_header.setText("Advertizement");


        LinearLayout layout = (LinearLayout)findViewById(R.id.image_container);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        List<Advertisement> advertisement = db.getAdvertisementDetails(DatabaseHandler.ADVERTISEMENT);

        boolean errorFlag=false;


        for ( Advertisement ad: advertisement)
        {
            String base64=ad.getPictureData();
            try
            {
                System.out.println(base64);
                ImageView imageView = new ImageView(this);
                byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                layoutParams.setMargins(10, 10, 10, 10);
                layoutParams.gravity = Gravity.CENTER;
                imageView.setImageBitmap(decodedByte);
                imageView.setLayoutParams(layoutParams);
                layout.addView(imageView);
            }
            catch(IllegalArgumentException e)
            {
                errorFlag=true;
            }
        }
        if(errorFlag==true)
        {
            Toast.makeText(getApplicationContext(),"Not a proper base64 data",Toast.LENGTH_LONG).show();
        }
    }
}
