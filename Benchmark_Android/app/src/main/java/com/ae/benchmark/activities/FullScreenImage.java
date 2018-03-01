package com.ae.benchmark.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.ae.benchmark.R;
import com.ae.benchmark.views.TouchImageView;
import com.squareup.picasso.Picasso;


/**
 * Created by gaurangt on 1/23/2018.
 */

public class FullScreenImage extends AppCompatActivity {

    TouchImageView imageView;
    ImageView cancle;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);
        context = FullScreenImage.this;

        imageView = (TouchImageView) findViewById(R.id.imgDisplay);
        cancle = (ImageView)findViewById(R.id.cancel);
        Bundle extras = getIntent().getExtras();
        String image = extras.getString("photos");

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Picasso.with(context)
                .load(getIntent().getExtras().getString("photos"))
                .placeholder(R.drawable.test)
                .error(R.drawable.test)
                .into(imageView);


//        Glide.with(context)
//                .load(image).placeholder(R.drawable.profile_male)
//                .diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true)
//                .into(imageView);
    }
}
