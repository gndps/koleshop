package com.koleshop.appkoleshop.activities.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.util.CommonUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

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
        setThatShitUp();
        checkGpsEnabled();
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

        // Add a marker in Sydney and move the camera
        LatLng gpsLatLng;
        if ((gpsLat == null && gpsLong == null) || (gpsLat == 0 && gpsLong == 0)) {
            gpsLatLng = new LatLng(28.6139, 77.2090);
        } else {
            gpsLatLng = new LatLng(gpsLat, gpsLong);
        }
        //refreshMarkerPosition(gpsLatLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(gpsLatLng));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLng geoLocation = cameraPosition.target;
                if(geoLocation!=null) {
                    gpsLat = geoLocation.latitude;
                    gpsLong = geoLocation.longitude;
                }
            }

        });
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpsLat, gpsLong), 14.0f));
        mMap.setPadding(0, 0, 0, 56);
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
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

    private void setThatShitUp() {
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
}
