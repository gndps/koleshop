package com.kolshop.kolshopmaterial.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kolshop.kolshopmaterial.common.constant.Prefs;
import com.kolshop.kolshopmaterial.model.ShopSettings;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationActivity extends Activity {

    private GoogleMap googleMap;
    private String locationName;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kolshop.kolshopmaterial.R.layout.activity_location);
        setUpLocationName();
        setUpMapIfNeeded();
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.kolshop.kolshopmaterial.R.menu.location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == com.kolshop.kolshopmaterial.R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(com.kolshop.kolshopmaterial.R.id.map))
                    .getMap();
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setAllGesturesEnabled(true);
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (marker != null) {
                            marker.remove();
                        }
                        marker = googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(locationName)
                                .draggable(true));
                    }
                });
                googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        centerMapOnMyLocation();
                        return true;

                    }
                });
            }
        }
    }

    private void centerMapOnMyLocation() {

        googleMap.setMyLocationEnabled(true);

        Location location = googleMap.getMyLocation();
        LatLng myLocation = null;
        if (location != null) {
            myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                16));
        if (marker != null) {
            marker.remove();
        }
        marker = googleMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title(locationName));
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("GPS is disabled");
        alertDialog.setMessage("Do you want to enable GPS ? (You can also set location manually)");
        //alertDialog.setIcon(R.drawable.delete);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUpLocationName() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        locationName = settings.getString("shop_name", "Your Location");
    }

    public void saveLocation(View view) {
        if (marker != null) {
            Double lat = marker.getPosition().latitude;
            Double lon = marker.getPosition().longitude;
            ShopSettings shopSettings = new ShopSettings();
            shopSettings.setUsername(locationName);
            //request to server with registrationId, username, password, email
            String registrationId;
            SharedPreferences prefs = getSharedPreferences(Prefs.USER_INFO, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("longitude", marker.getPosition().longitude + "");
            editor.putString("latitude", marker.getPosition().latitude + "");
            editor.commit();
        }
    }
}
