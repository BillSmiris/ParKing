package com.telikhergasia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DatabaseReference ref;
    private TextView title, address, phone, hours, slots, rating, hourly, threehours, allday,distance;
    private ImageView garage,carwash,security;
    GoogleMap map;
    LatLng garagepos;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar topbar = findViewById(R.id.top_bar);
        setSupportActionBar(topbar);
        getSupportActionBar().setTitle("ParKing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        garage= findViewById(R.id.garage_status);
        carwash= findViewById(R.id.carwash_status);
        security= findViewById(R.id.security_status);

        title = findViewById(R.id.title);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone);
        hours = findViewById(R.id.hours);
        slots = findViewById(R.id.slots);
        rating = findViewById(R.id.rating);
        hourly = findViewById(R.id.hourly);
        threehours = findViewById(R.id.threehours);
        allday = findViewById(R.id.allday);
        distance = findViewById(R.id.distance);
        ref = FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot item = dataSnapshot.child(getIntent().getExtras().getString("id"));
                title.setText(item.child("Title").getValue().toString());
                float[] res={0};
               Location.distanceBetween((double)item.child("Lat").getValue(),(double)item.child("Lon").getValue(),MapsActivity.myLatLng.latitude,MapsActivity.myLatLng.longitude,res);
                distance.setText(res[0]+" m");
                address.setText(item.child("Address").getValue().toString() + ", " + item.child("TK").getValue().toString());
                phone.setText(item.child("Phone").getValue().toString());
                if(item.child("Opening time").getValue().toString().equals(item.child("Closing Time").getValue().toString())){
                    hours.setText("24hrs");
                }
                else{
                    hours.setText(item.child("Opening time").getValue().toString() + " - " + item.child("Closing Time").getValue().toString());
                }
                slots.setText((Integer.parseInt(item.child("Total Slots").getValue().toString()) - Integer.parseInt(item.child("Reserved Slots").getValue().toString())) + "/" + item.child("Total Slots").getValue().toString());
                rating.setText(item.child("Rating").getValue().toString() + "/5");
                String[] pricing = item.child("Pricing").getValue().toString().split("\\|");
                if(item.child("Covered").getValue().toString().contains("TRUE")){
                    garage.setAlpha(1f);
                }else{
                    garage.setAlpha(0.2f);
                }
                if(item.child("Car Wash").getValue().toString().contains("TRUE")){
                    carwash.setAlpha(1f);
                }else{
                    carwash.setAlpha(0.2f);
                }
                if(item.child("Security").getValue().toString().contains("TRUE")){
                    security.setAlpha(1f);
                }else{
                    security.setAlpha(0.2f);
                }
                if(Locale.getDefault().getLanguage().equals("el")){
                    hourly.setText("Ωριαία: " + pricing[5] + "\u20ac");
                    threehours.setText("3 Ώρες: " + pricing[3] + "\u20ac");
                    allday.setText("Όλη μέρα: " + pricing[1] + "\u20ac");
                }
                else {
                    hourly.setText("Hourly: " + pricing[5] + "\u20ac");
                    threehours.setText("3 Hours: " + pricing[3] + "\u20ac");
                    allday.setText("All Day: " + pricing[1] + "\u20ac");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (MapsActivity.gpsstatuss){
        Button b=findViewById(R.id.details_nav_Button);
        b.setAlpha(1f);
        }
        else{
            Button b=findViewById(R.id.details_nav_Button);
            b.setAlpha(0.2f);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_button:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot item = dataSnapshot.child(getIntent().getExtras().getString("id"));

                garagepos = new LatLng((double)item.child("Lat").getValue(), (double)item.child("Lon").getValue());
                map.addMarker(new MarkerOptions().position(garagepos));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(garagepos, 17.0f));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void navigate(View view){
        if (MapsActivity.gpsstatuss) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + garagepos.latitude + "," + garagepos.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        }else{

        }
    }}
