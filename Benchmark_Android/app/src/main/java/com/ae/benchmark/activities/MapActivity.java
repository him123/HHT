package com.ae.benchmark.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import com.ae.benchmark.App;
import com.ae.benchmark.R;

/**
 * Created by Muhammad Umair on 05-09-2017.
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList markerPoints = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            LatLng location = new LatLng(Double.parseDouble(App.Latitude), Double.parseDouble(App.Longitude));
            String title = "Location";
            mMap.addMarker(new MarkerOptions().position(location).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
        }catch (Exception e){
            e.printStackTrace();
        }


        System.out.println("lat : "+App.Latitude+ "long : "+App.Longitude);
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        LatLngBounds bounds = builder.build();
//        int width = getResources().getDisplayMetrics().widthPixels;
//        int height = getResources().getDisplayMetrics().heightPixels;
//        int padding = (int) (width * 0.30);
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
//        this.mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
//        this.mMap.animateCamera(cu);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (markerPoints.size() > 1) {
                    markerPoints.clear();
                    mMap.clear();
                }
                markerPoints.add(latLng);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);

            }
        });

    }
}