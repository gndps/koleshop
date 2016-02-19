package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.koleshop.appkoleshop.model.demo.SellerInfo;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 03/02/16.
 */
public class NearbyShopsMapFragment extends SupportMapFragment implements OnMapReadyCallback {

    public GoogleMap mGoogleMap;
    List<SellerSettings> sellers;
    List<Marker> markers;

    // Declare a variable for the cluster manager.
    ClusterManager<MyItem> mClusterManager;

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
        try {
            sellers = Parcels.unwrap(getArguments().getParcelable("sellers"));
        } catch (Exception e) {
            //some problem while accepting parcel
            Log.d(TAG, "problem in accepting sellers parcel", e);
        }
        getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //add markers
        mGoogleMap = googleMap;

        markers = new ArrayList<>();
        for(SellerSettings seller : sellers) {
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(seller.getAddress().getGpsLat(), seller.getAddress().getGpsLong()))
                    .title(seller.getAddress().getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .draggable(false)
                    .alpha(0.8f));
            markers.add(marker);
        }

        //make position bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        //camera update
        int padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
        setUpClusterer();
    }

    public void moreSellersFetched(List<SellerSettings> moreSellers) {
        sellers.addAll(moreSellers);
        for(SellerSettings seller : moreSellers) {
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(seller.getAddress().getGpsLat(), seller.getAddress().getGpsLong()))
                    .title(seller.getAddress().getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .draggable(false)
                    .alpha(0.8f));
            markers.add(marker);
        }
    }

    public void couldNotLoadMoreSellers() {
    }

    private void setUpClusterer() {

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
    }

}
