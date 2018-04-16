package com.example.miljanamilena.pathfinder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    MarkerOptions currentLocationMarker;
    LocationRequest mLocationRequest;

    private Toolbar toolbar1,toolbar2;
    private TextView location, start,destination,distanceCovered,averageSpeed,locationDistance;
    private Button go;
    private Chronometer timer;
    private Boolean timerStarted = false, connected;
    private LatLng curenttLatLang;
    private LocationManager lm;
    private android.location.LocationListener locationListener;
    private LinearLayout layoutInfo;
    private ImageView clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Initializing
        MarkerPoints = new ArrayList<>();

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (!isOnline())
        {
            Toast.makeText(this, "Not connected to the network", Toast.LENGTH_LONG).show();
            finish();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        layoutInfo = findViewById(R.id.layout_info);

        locationDistance = findViewById(R.id.location_distance);

        clear = findViewById(R.id.btn_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutInfo.setVisibility(View.GONE);
                locationDistance.setText("-");
            }
        });

        toolbar1 = findViewById(R.id.my_toolbar1);
        setSupportActionBar(toolbar1);
        toolbar2 = findViewById(R.id.my_toolbar2);

        location = findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openAutocompleteActivity(1);
            }
        });

        ImageView flag = findViewById(R.id.flag);
        flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar1.setVisibility(View.GONE);
                toolbar2.setVisibility(View.VISIBLE);
                setSupportActionBar(toolbar2);

                location.setText(getString(R.string.location));
                mMap.clear();
                mMap.addMarker(currentLocationMarker);

                start = findViewById(R.id.start);
                start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAutocompleteActivity(2);
                    }
                });

                destination = findViewById(R.id.destination);
                destination.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAutocompleteActivity(3);
                    }
                });
            }
        });

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar2.setVisibility(View.GONE);
                toolbar1.setVisibility(View.VISIBLE);
                setSupportActionBar(toolbar1);
                mMap.clear();
                mMap.addMarker(currentLocationMarker);
                MarkerPoints.clear();
                start.setText("");
                destination.setText("");
            }
        });

        distanceCovered = findViewById(R.id.distance_covered);

        timer = findViewById(R.id.elapsed_time);

        go = findViewById(R.id.btn_start);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(go.getText().equals(getString(R.string.start)))
                {
                    switcher(1);
                }
                else if (go.getText().equals(getString(R.string.stop)))
                {
                    switcher(2);
                }
                else if (go.getText().equals(getString(R.string.reset)))
                {
                    switcher(3);
                }
            }
        });
    }

    public boolean isOnline() {
        try
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            return connected;
        }
        catch (Exception e)
        {
            Log.v("Connectivity", e.toString());
        }
        return connected;
    }

    private void switcher(int switchId)
    {
        switch (switchId)
        {
            case 1:
                startLocationUpdating();
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();
                go.setText(getString(R.string.stop));
                timerStarted = true;
                mMap.addMarker(currentLocationMarker);
                break;
            case 2:
                timer.stop();
                stopLocationUpdating();
                go.setText(getString(R.string.reset));
                timerStarted = false;
                int elapsedMillis = (int) (SystemClock.elapsedRealtime() - timer.getBase());
                double seconds = elapsedMillis/1000;
                double meters = Double.valueOf(distanceCovered.getText().toString());
                double speed = (double) Math.round(meters/seconds * 100)/360;
                averageSpeed = findViewById(R.id.average_speed);
                averageSpeed.setText(String.valueOf(speed)+" km/h");
                break;

            case 3:
                timer.setBase(SystemClock.elapsedRealtime());
                go.setText(getString(R.string.start));
                mMap.clear();
                mMap.addMarker(currentLocationMarker);
                distanceCovered.setText("0");
                averageSpeed.setText("-");
                break;
        }
    }

    private void openAutocompleteActivity(int request) {
        try
        {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, request);
        }
        catch (GooglePlayServicesRepairableException e)
        {
            System.out.println("ErroR: "+e.getMessage());
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),0 ).show();
        }
        catch (GooglePlayServicesNotAvailableException e)
        {
            String message = "Google Play Services is not available: " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
        {
            Place place = PlaceAutocomplete.getPlace(this, data);
            if (requestCode == 1)
            {
                mMap.clear();
                MarkerPoints.clear();
                location.setText(place.getAddress());
                mMap.addMarker(currentLocationMarker);
                setMarker(place.getLatLng());
                String url = getUrl(curenttLatLang, place.getLatLng());
                FetchUrl FetchUrl = new FetchUrl(mMap,locationDistance,layoutInfo);
                FetchUrl.execute(url);
            }
            else if (requestCode == 2)
            {
                start.setText(place.getAddress());
                MarkerPoints.clear();
                mMap.clear();
                destination.setEnabled(true);
                mMap.addMarker(currentLocationMarker);
                setMarker(place.getLatLng());
            }
            else if (requestCode == 3)
            {
                destination.setText(place.getAddress());
                setMarker(place.getLatLng());
            }
        }
        else if (resultCode == PlaceAutocomplete.RESULT_ERROR)
        {
            Status status = PlaceAutocomplete.getStatus(this, data);
            Log.i("POKUKA", status.getStatusMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                if (MarkerPoints.size() > 1)
                {
                    MarkerPoints.clear();
                    mMap.clear();
                    mMap.addMarker(currentLocationMarker);
                }
                setMarker(point);
            }
        });

    }

    private void setMarker(LatLng point)
    {
        MarkerPoints.add(point);
        MarkerOptions options = new MarkerOptions();
        options.position(point);

        if (MarkerPoints.size() == 1)
        {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        else if (MarkerPoints.size() == 2)
        {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            drawRoute();
        }

        mMap.addMarker(options);

    }

    private void drawRoute()
    {
        LatLng origin = MarkerPoints.get(0);
        LatLng dest = MarkerPoints.get(1);

        // Getting URL to the Google Directions API
        String url = getUrl(origin, dest);
        Log.d("onMapClick", url);
        FetchUrl FetchUrl = new FetchUrl(mMap, locationDistance, layoutInfo);

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

    private String getUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String key = "&key=" + getString(R.string.api_key);
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + key;

        return url;
    }

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        System.out.println("CONNECTED  >>>");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationChanged(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {}

                @Override
                public void onProviderEnabled(String s) {}

                @Override
                public void onProviderDisabled(String s) {}
            }, null);
        }

    }

    private void startLocationUpdating()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, locationListener = new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationChanged(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {}

                @Override
                public void onProviderEnabled(String s) {}

                @Override
                public void onProviderDisabled(String s) {}
            });
        }
    }
    private void stopLocationUpdating()
    {
        if(locationListener!=null)
            lm.removeUpdates(locationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void locationChanged(Location location)
    {
        System.out.println("LOCATION CHANGED !!!");
        if (currentLocationMarker != null)
        {
            currentLocationMarker = null;
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        currentLocationMarker = markerOptions;
        mMap.addMarker(currentLocationMarker);

        if(timerStarted)
        {
            String url = getUrl(curenttLatLang, latLng);
            Log.d("onMapClick", url);
            FetchUrl FetchUrl = new FetchUrl(mMap, distanceCovered);
            FetchUrl.execute(url);
        }
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        mLastLocation = location;
        curenttLatLang = latLng;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection Failed: "+ connectionResult.toString(), Toast.LENGTH_LONG).show();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public void checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Asking user for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (mGoogleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLocationUpdating();

    }
}