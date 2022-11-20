package com.telikhergasia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.ActivityChooserModel;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,SensorEventListener, LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private GoogleMap mMap;
    private DatabaseReference ref;
    private LocationManager locationManager;
    static final int REQ_CODE = 654;
    private Marker myMarker;
    private boolean firstgps=false;
    public static LatLng myLatLng=new LatLng(37.941515, 23.652863);
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static boolean gpsstatuss=false;
    public static ArrayList<Marker> markerss=new ArrayList<Marker>();
    public static Locale myLocale;
    private SensorManager sensorManager;
    private Sensor a,be;
    public Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {




        super.onCreate(savedInstanceState);

        Resources res = getResources();
        Configuration conf = res.getConfiguration();




        setContentView(R.layout.activity_maps);
        Toolbar topbar = findViewById(R.id.top_bar);
        setSupportActionBar(topbar);
        getSupportActionBar().setTitle("ParKing");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ref = FirebaseDatabase.getInstance().getReference();
        pref = getApplicationContext().getSharedPreferences("com.telikhergasia_preferences", 0); // 0 - for private mode
        editor = pref.edit();



        //ENABLE GPS FUNCTION
        {if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)  {

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0, 0, this);
            }
            editor.putBoolean("gps_status",true);
            editor.commit();
            gpsstatuss=true;
        }else
            editor.putBoolean("gps_status",false);
            editor.commit();
            gpsstatuss=false;
            ActivityCompat.requestPermissions(
                    this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ_CODE);
        }
        //END ENABLE GPS FUNCTION



        if(pref.getBoolean("gps_status",false)){
            gpsstatuss=true;

        }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);



        pref = getApplicationContext().getSharedPreferences("com.telikhergasia_preferences", 0); // 0 - for private mode
        System.out.println(pref.getString("language",""));
        myLocale=new Locale(pref.getString("language",""));
        setLocale(pref.getString("language",""));

        Button b=findViewById(R.id.big_butt);
        b.setAlpha(0.2f);



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        a = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        be = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorManager.registerListener(this, a, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, be, SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_button:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.list_view_button:

                startActivity(new Intent(this, ListViewActivity2.class));

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    LatLng pos=new LatLng((double)childDataSnapshot.child("Lat").getValue(), (double)childDataSnapshot.child("Lon").getValue());
                    float[] result={0};
                    try {
                        Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, pos.latitude, pos.longitude, result);
                    }
                    catch(Exception ae) {

                    }
                    Integer price = Integer.parseInt(childDataSnapshot.child("Pricing").getValue().toString().split("\\|")[5]);
                    IconGenerator icg = new IconGenerator(MapsActivity.this);
                    if(price < 4){
                        icg.setColor(Color.GREEN);
                    }
                    else if(price < 8){
                        icg.setColor(Color.parseColor("#ffa500"));
                    }
                    else{
                        icg.setColor(Color.RED);
                    }
                    icg.setTextAppearance(R.style.WhiteText);
                    Bitmap bm = icg.makeIcon(price + "\u20ac");
                    Marker marker = mMap.addMarker(new MarkerOptions().position(pos).title(childDataSnapshot.child("Title").getValue().toString()).snippet(childDataSnapshot.child("Address").getValue().toString()).icon(BitmapDescriptorFactory.fromBitmap(icg.makeIcon(price + "\u20ac"))));
                    marker.setTag(childDataSnapshot.getKey());
                    //if its in range put marker
                   if(!gpsstatuss){//put marker hidden
                        marker.setVisible(true);
                    }
                    else if (result[0]< Integer.parseInt(pref.getString("range","1000"))){
                        marker.setTag(childDataSnapshot.getKey());
                    }

                    else{
                        marker.setVisible(false);
                    }


                    markerss.add(marker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


        mMap.setOnMapClickListener((new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Button b=findViewById(R.id.big_butt);
                b.setAlpha(0.2f);
                selectedMarker=null;
            }
        }));

        mMap.setOnMarkerClickListener((new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                if ((marker!=myMarker)&&(gpsstatuss==true)){
                    Button b=findViewById(R.id.big_butt);
                    b.setAlpha(1f);
                    selectedMarker=marker;
                }
                return false;
            }
        }));


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent i = new Intent(MapsActivity.this, DetailsActivity.class);
                Bundle b = new Bundle();
                b.putString("id", marker.getTag().toString());
                i.putExtras(b);
                startActivity(i);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            myLatLng=new LatLng(location.getLatitude(),location.getLongitude());
            if (myMarker!=null){
                myMarker.remove();
            }
            myMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.nav)));
            if (firstgps==false){
                firstgps=true;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15.0f));
            }


        }
    }


    public void focusCurrLoc(View view){
        if(myLatLng != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }




    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {


        System.out.println(pref.getBoolean("gps_status",false));
        if(key.contains("gps_status")) {






            if(pref.getBoolean("gps_status",true)){

                Button b = findViewById(R.id.big_butt);
                b.setAlpha(0.2f);
                {if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)  {

                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, 0, 0, this);
                    }
                    editor.putBoolean("gps_status",true);
                    editor.commit();
                }else
                    editor.putBoolean("gps_status",false);
                    editor.commit();
                    ActivityCompat.requestPermissions(
                            this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ_CODE);
                }
                key="range";
                gpsstatuss=true;



            }
            else{
                Button b = findViewById(R.id.big_butt);
                b.setAlpha(0.2f);
                gpsstatuss=false;
                for(int i=0; i<markerss.size();i++){
                    markerss.get(i).setVisible(true);
                }
            }
        }
        if (key.contains("range")){
            for(int i=0; i<markerss.size();i++){
                float[] result={0};
                Location.distanceBetween(markerss.get(i).getPosition().latitude,markerss.get(i).getPosition().longitude,myLatLng.latitude,myLatLng.longitude,result);
                if (result[0]< Integer.parseInt(pref.getString("range","1000"))){
                    markerss.get(i).setVisible(true);
                }
                else{
                    markerss.get(i).setVisible(false);
                }
            }
        }
        if(key.contains("language")) {
            System.out.println(sharedPreferences.getString("language","en"));
            String lang=sharedPreferences.getString("language","en");
            if (lang.contains("en")){
                setLocale("en");

            }
            if (lang.contains("el")){
                setLocale("el");
            }
        }



    }
    public void setLocale(String lang) {

        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        //Intent refresh = new Intent(this, MapsActivity.class);

    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        System.out.println(event.sensor.toString());
        if (event.sensor==be){
            ((TextView)findViewById(R.id.temprr)).setText(event.values[0]+"");
        }
        if (event.sensor==a){
            ((TextView)findViewById(R.id.humid)).setText(event.values[0]+"");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void navigateBIG(View view){
        if ((MapsActivity.gpsstatuss)&&(selectedMarker!=null)) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + selectedMarker.getPosition().latitude + "," + selectedMarker.getPosition().longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

        }else{

        }
    }
}


