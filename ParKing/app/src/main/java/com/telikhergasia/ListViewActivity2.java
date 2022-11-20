package com.telikhergasia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.telikhergasia.MapsActivity.gpsstatuss;
import static com.telikhergasia.MapsActivity.markerss;
import static com.telikhergasia.MapsActivity.myLatLng;

public class ListViewActivity2 extends AppCompatActivity {

    private DatabaseReference ref;
    private ArrayList<String> titles, addresses, ids;
    private RecyclerView garagelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view2);
        titles = new ArrayList<>();
        addresses = new ArrayList<>();
        ids = new ArrayList<>();
        garagelist = findViewById(R.id.garage_list);
        Toolbar topbar = findViewById(R.id.top_bar);
        setSupportActionBar(topbar);
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("com.telikhergasia_preferences", 0); // 0 - for private mode
        ref = FirebaseDatabase.getInstance().getReference();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    float[] result={0};
                    Location.distanceBetween((double)childDataSnapshot.child("Lat").getValue(),(double)childDataSnapshot.child("Lon").getValue(),myLatLng.latitude,myLatLng.longitude,result);
                    if (!gpsstatuss){
                        titles.add(childDataSnapshot.child("Title").getValue().toString());
                        addresses.add(childDataSnapshot.child("Address").getValue().toString());
                        ids.add(childDataSnapshot.getKey());
                    }
                    else if (result[0]< Integer.parseInt(pref.getString("range","1000"))){


                    titles.add(childDataSnapshot.child("Title").getValue().toString());
                    addresses.add(childDataSnapshot.child("Address").getValue().toString());
                    ids.add(childDataSnapshot.getKey());
                    }
                }
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(titles, addresses, ids, ListViewActivity2.this);
                garagelist.setAdapter(adapter);
                garagelist.setLayoutManager(new LinearLayoutManager(ListViewActivity2.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_button:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.list_view_button:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
