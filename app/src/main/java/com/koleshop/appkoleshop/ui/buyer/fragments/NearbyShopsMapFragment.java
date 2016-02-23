package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gundeep on 03/02/16.
 */
public class NearbyShopsMapFragment extends SupportMapFragment implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {

    public GoogleMap mGoogleMap;
    List<SellerSettings> sellers;
    Map<String, SellerSettings> markers;
    Context mContext;
    Marker userMarker;
    Marker nearestShopMarker;

    // Declare a variable for the cluster manager.
    //ClusterManager<MyItem> mClusterManager;

    private static String TAG = "fns_map";


    public static NearbyShopsMapFragment newInstance(List<SellerSettings> sellers) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("sellers", Parcels.wrap(sellers));
        NearbyShopsMapFragment nearbyShopsMapFragment = new NearbyShopsMapFragment();
        nearbyShopsMapFragment.setArguments(bundle);
        return nearbyShopsMapFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mContext = getActivity();
        try {
            sellers = Parcels.unwrap(getArguments().getParcelable("sellers"));
        } catch (Exception e) {
            //some problem while accepting parcel
            Log.d(TAG, "problem in accepting sellers parcel", e);
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleMap = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mGoogleMap == null) {
            getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //00. initialize variables
        mGoogleMap = googleMap;
        markers = new HashMap<>();


        //01. add the seller markers
        int index = 0;
        for (SellerSettings seller : sellers) {
            Marker marker = addTheSellerMarker(seller);
            if (index == 0) {
                nearestShopMarker = marker;
            }
            index++;
        }

        //02. add the marker for delivery location
        addTheUserMarker();


        //03. setup google map
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.setOnInfoWindowClickListener(this);


        //04. adjust camera to show all markers
        adjustCameraToShowShopMarkers();

        //set marker on click listener
        /*mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                SellerSettings sellerSettings = markers.get(marker);
                if(sellerSettings!=null) {
                    ((NearbyShopsFragment) getParentFragment()).openSeller(sellerSettings);
                }
                //open the shop menu fragment
                return true;
            }
        });*/
    }

    private void addTheUserMarker() {
        if (markers != null && userMarker != null && markers.containsKey(userMarker)) {
            markers.remove(userMarker);
        }
        Double deliveryLocationGpsLat = PreferenceUtils.getGpsLat(mContext);
        Double deliveryLocationGpsLong = PreferenceUtils.getGpsLong(mContext);
        Bitmap userMarkerBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.ic_user_gps_marker);
        Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(deliveryLocationGpsLat, deliveryLocationGpsLong))
                .title("You are here")
                        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                .icon(BitmapDescriptorFactory.fromBitmap(userMarkerBitmap))
                .draggable(false)
                .alpha(0.95f));
        //markers.put("userMarker", null);
        userMarker = marker;
    }

    private Marker addTheSellerMarker(SellerSettings sellerSettings) {

        if (sellerSettings != null) {

            Bitmap sellerMarkerBitmap = null;
            String deliveryPickupInfo;
            String title;
            String openOrClose;


            //01 EXTRACT THE SELLER TITLE AND MARKER INFORMATION

            //01.01 FIND SHOP TITLE
            title = sellerSettings.getAddress().getName();

            //01.02 GET SHOP DISTANCE FROM USER
            float[] results = new float[3];
            Double userLat = PreferenceUtils.getGpsLat(mContext);
            Double userLong = PreferenceUtils.getGpsLong(mContext);
            Location.distanceBetween(userLat, userLong, sellerSettings.getAddress().getGpsLat(), sellerSettings.getAddress().getGpsLong(), results);
            float userDistanceFromShopInMeters = results[0];

            //01.03 FIND DELIVERY / PICKUP INFORMATION AND CHOOSE IMAGE FOR MARKER
            if (sellerSettings.isHomeDelivery()) {
                if ((sellerSettings.getMaximumDeliveryDistance() + Constants.DELIVERY_DISTANCE_APPROXIMATION_ERROR) >= userDistanceFromShopInMeters) {
                    //home delivery is available to this location
                    deliveryPickupInfo = KoleshopUtils.getDeliveryTimeStringFromOpenAndCloseTime(sellerSettings.getDeliveryStartTime(), sellerSettings.getDeliveryEndTime());
                    if (KoleshopUtils.willSellerDeliverNow(sellerSettings.getDeliveryEndTime())) {
                        //seller will delivery to the user - ONLINE + DELIVERY
                        sellerMarkerBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                R.drawable.ic_seller_online_delivery);
                    } else {
                        //seller will not delivery to user at this time - ONLINE + PICKUP
                        sellerMarkerBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                                R.drawable.ic_seller_online_pickup);
                    }
                } else {
                    //seller don't delivery at this location - ORANGE ICON
                    deliveryPickupInfo = "No delivery to your location";
                    sellerMarkerBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.ic_seller_online_pickup);
                }
            } else {
                //only pickup available - ORANGE ICON
                deliveryPickupInfo = "Pickup Only";
                sellerMarkerBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.ic_seller_online_pickup);
            }

            if (!sellerSettings.isShopOpen()) {
                //seller is offline - GREY ICON
                sellerMarkerBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.ic_seller_offline);
                openOrClose = "Closed";
            } else {
                openOrClose = "Open";
            }


            //02 ADD THE SELLER MARKER TO GOOGLE MAP
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(sellerSettings.getAddress().getGpsLat(), sellerSettings.getAddress().getGpsLong()))
                    .title(title)
                    .snippet(openOrClose + ", " + deliveryPickupInfo)
                    .icon(BitmapDescriptorFactory.fromBitmap(sellerMarkerBitmap))
                    .draggable(false)
                    .alpha(0.95f));
            String markerRecognizeString = marker.getTitle() + "" + marker.getSnippet();
            markers.put(markerRecognizeString, sellerSettings);
            return marker;

        } else {
            return null;
        }

    }

    private void adjustCameraToShowShopMarkers() {
        //make position bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(userMarker.getPosition());
        builder.include(nearestShopMarker.getPosition());
        /* Bounds for showing all shops
        for (Marker markeru : markers.keySet()) {
            builder.include(markeru.getPosition());
        }*/

        LatLngBounds bounds = builder.build();

        //camera update
        int padding = 200; // offset from edges of the maep in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mGoogleMap.moveCamera(cu);
    }

    public void moreSellersFetched(List<SellerSettings> moreSellers) {
        sellers.addAll(moreSellers);
        for (SellerSettings seller : moreSellers) {
            addTheSellerMarker(seller);
        }
    }

    public void couldNotLoadMoreSellers() {
    }

    private void checkGpsEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

    @Override
    public void onInfoWindowClick(Marker marker) {
        String markerRecognizeString = marker.getTitle() + "" + marker.getSnippet();
        SellerSettings seller = markers.get(markerRecognizeString);
        if(seller!=null) {
            ((NearbyShopsFragment) getParentFragment()).openSeller(seller);
        } else {
            Toast.makeText(mContext, "wtf", Toast.LENGTH_LONG).show();
        }
    }

    /*private void setUpClusterer() {

        // Position the map.
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(getActivity(), mGoogleMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mGoogleMap.setOnCameraChangeListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MyItem offsetItem = new MyItem(lat, lng);
            mClusterManager.addItem(offsetItem);
        }
    }

    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }*/

}
