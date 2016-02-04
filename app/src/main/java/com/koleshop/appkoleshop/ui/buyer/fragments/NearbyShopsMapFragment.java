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
import com.koleshop.appkoleshop.model.demo.SellerInfo;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 03/02/16.
 */
public class NearbyShopsMapFragment extends SupportMapFragment implements OnMapReadyCallback {

    public GoogleMap mGoogleMap;
    List<SellerInfo> sellers;

    private static String TAG = "fns_map";


    public static NearbyShopsMapFragment newInstance(List<SellerInfo> sellers) {
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
        List<Marker> markers = new ArrayList<>();
        for(SellerInfo seller : sellers) {
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(seller.getGpsLat(), seller.getGpsLong()))
                    .title(seller.getName())
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
    }
}
