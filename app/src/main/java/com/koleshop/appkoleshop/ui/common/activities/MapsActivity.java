package com.koleshop.appkoleshop.ui.common.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.util.CommonUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    public static final int REQUEST_CODE_GET_LOCATION = 0x01;
    private boolean twoButtonMode;
    private String title;
    private String actionButtonTitle;
    private Double gpsLat, gpsLong;
    private String markerTitle;
    private String leftButtonTitle, rightButtonTitle;
    private Toolbar toolbar;
    private boolean locationChanged;
    private GoogleApiClient mGoogleApiClient;
    private boolean goToLastLocationWhenAvailable;
    LocationRequest mLocationRequest;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "MapsActivity";

    @Bind(R.id.fl_main_action_button)
    FrameLayout buttonMainAction;
    @Bind(R.id.rl_back_next_button)
    RelativeLayout buttonBackNext;
    @Bind(R.id.reusable_back_button)
    Button leftButton;
    @Bind(R.id.reusable_next_button)
    Button rightButton;
    @Bind(R.id.reusable_action_button)
    Button actionButton;
    @Bind(R.id.iv_gps_pin_icon)
    ImageView imageViewGpsPinIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (savedInstanceState != null) {
            twoButtonMode = savedInstanceState.getBoolean("twoButtonMode");
            title = savedInstanceState.getString("title");
            actionButtonTitle = savedInstanceState.getString("actionButtonTitle");
            gpsLat = savedInstanceState.getDouble("gpsLat");
            gpsLong = savedInstanceState.getDouble("gpsLong");
            markerTitle = savedInstanceState.getString("markerTitle");
            leftButtonTitle = savedInstanceState.getString("leftButtonTitle");
            rightButtonTitle = savedInstanceState.getString("rightButtonTitle");
            locationChanged = savedInstanceState.getBoolean("locationChanged");
        } else {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                twoButtonMode = bundle.getBoolean("twoButtonMode");
                title = bundle.getString("title");
                actionButtonTitle = bundle.getString("actionButtonTitle");
                gpsLat = bundle.getDouble("gpsLat");
                gpsLong = bundle.getDouble("gpsLong");
                markerTitle = bundle.getString("markerTitle");
                leftButtonTitle = bundle.getString("leftButtonTitle");
                rightButtonTitle = bundle.getString("rightButtonTitle");
            }
        }

        if (markerTitle == null || markerTitle.isEmpty()) {
            markerTitle = "My Location";
        }

        if (leftButtonTitle == null || leftButtonTitle.isEmpty()) {
            leftButtonTitle = "CANCEL";
        }

        if (rightButtonTitle == null || rightButtonTitle.isEmpty()) {
            rightButtonTitle = "DONE";
        }

        setupToolbar();
        setBackNextButtonUi();
        checkGpsEnabled();
        buildGoogleApiClient();
        createLocationRequest();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("twoButtonMode", twoButtonMode);
        outState.putString("title", title);
        outState.putString("actionButtonTitle", actionButtonTitle);
        if (gpsLat != null) {
            outState.putDouble("gpsLat", gpsLat);
        }
        if (gpsLong != null) {
            outState.putDouble("gpsLong", gpsLong);
        }
        outState.putString("markerTitle", markerTitle);
        outState.putBoolean("locationChanged", locationChanged);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_maps_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search_maps:
                startSearch();
                return true;
            /*case R.id.action_discard_product_changes:
                processingAnimation(false);
                onBackPressed();*/
        }

        return super.onOptionsItemSelected(item);
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

        // Add a pin to location if gps coordinates are provided
        LatLng gpsLatLng;
        if ((gpsLat == null && gpsLong == null) || (gpsLat == 0 && gpsLong == 0)) {
            //request gps location here
            mGoogleApiClient.connect();
            goToLastLocationWhenAvailable = true;
        } else {
            gpsLatLng = new LatLng(gpsLat, gpsLong);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsLatLng, 14.0f));
        }
        //refreshMarkerPosition(gpsLatLng);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLng geoLocation = cameraPosition.target;
                if (geoLocation != null) {
                    gpsLat = geoLocation.latitude;
                    gpsLong = geoLocation.longitude;
                }
                locationChanged = true;
            }

        });
        mMap.setPadding(0, 0, 0, 0);
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        TextView toolbarTextView = CommonUtils.getActionBarTextView(toolbar);
        if (toolbarTextView != null) {
            toolbarTextView.setTypeface(typeface);
        }
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);
        }
    }

    private void checkGpsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
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

    private void setBackNextButtonUi() {
        if (twoButtonMode) {
            buttonMainAction.setVisibility(View.GONE);
            leftButton.setText(leftButtonTitle);
            rightButton.setText(rightButtonTitle);
        } else {
            buttonBackNext.setVisibility(View.GONE);
            actionButton.setText(actionButtonTitle);
        }
    }

    public void backButtonClicked(View view) {
        //send result to the calling activity
        setResult(RESULT_CANCELED, null);
        finish();
    }

    public void nextButtonClicked(View view) {
        //send result to the calling activity
        sendResultToCallingActivity();
    }

    public void mainActionButtonClicked(View view) {
        //send result to the calling activity
        sendResultToCallingActivity();
    }

    private void sendResultToCallingActivity() {
        Intent intent = new Intent();
        intent.putExtra("gpsLat", gpsLat);
        intent.putExtra("gpsLong", gpsLong);
        setResult(RESULT_OK, intent);
        //close this Activity...
        finish();
    }

    private void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {

        Location gpsLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if(goToLastLocationWhenAvailable) {
            mMap.getMyLocation();
        }
        if (gpsLocation != null) {
            if (goToLastLocationWhenAvailable && mMap != null) {
                LatLng gpsLatLng = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsLatLng, 14.0f));
            }
        }

        startLocationUpdates(); //no need to start location updates coz google maps handle it automatically

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private void startSearch() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                    .build();

            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if(place!=null && place.getLatLng()!=null) {
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(place.getLatLng(), 16.0f)));
                }
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "result cancelled");
                // The user canceled the operation.
            }
        }
    }
}
