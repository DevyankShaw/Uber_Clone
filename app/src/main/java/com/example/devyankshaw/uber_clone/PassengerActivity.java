package com.example.devyankshaw.uber_clone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.text.TimeZoneFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;//we need to have the location of the passenger

    private Button btnRequestCar;

    private boolean isUberCancelled = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCar = findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(this);

        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size() > 0 && e == null){
                    isUberCancelled = false;
                    btnRequestCar.setText("Cancel Your Request");
                }
            }
        });
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

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {//this locationListener is an interface with 4 abstract methods
            @Override
            public void onLocationChanged(Location location) {//This onLocationChanged() will be called when the location of the passenger is changed

                updateCameraPassengerLocation(location);
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
        };

        if(Build.VERSION.SDK_INT < 23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }else if(Build.VERSION.SDK_INT >= 23){
            //Asking for runtime time permission
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(PassengerActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    //By this onRequestPermissionsResult() we are handling the permission when it is accepted or denied by the user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    private void updateCameraPassengerLocation(Location pLocation){
        LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());//By this we get access to passenger location
        mMap.clear();//this clear() is called because whenever onLocationChanged() is called we want to show marker then we must clear the previous markers before updating to the new marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));/*By this we are updating the users/passengers location on the map
                                                                                    or we can say moving the camera of the map where passenger exist*/
        mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!!!"));
    }

    @Override
    public void onClick(View v) {

        if(isUberCancelled) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (passengerCurrentLocation != null) {

                    ParseObject requestCar = new ParseObject("RequestCar");//Create a class which store objects
                    requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());
                    requestCar.put("passengerLocation", userLocation);

                    requestCar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(PassengerActivity.this, "Car Requested", Toast.LENGTH_SHORT).show();

                                btnRequestCar.setText("Cancel Your Ride");
                                isUberCancelled = false;
                            }
                        }
                    });
                } else {

                    Toast.makeText(this, "Unknown Error. Something went wrong!!!", Toast.LENGTH_SHORT).show();
                }
            }
        }else{

            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> requestList, ParseException e) {
                    if(requestList.size() > 0 && e == null){
                        isUberCancelled = true;
                        btnRequestCar.setText("Request Car");

                        for(ParseObject uberRequest : requestList){

                            uberRequest.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null){
                                        Toast.makeText(PassengerActivity.this, "Request's deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
