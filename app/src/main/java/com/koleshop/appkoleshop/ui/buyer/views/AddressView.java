package com.koleshop.appkoleshop.ui.buyer.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Created by Gundeep on 04/02/16.
 */
public class AddressView extends CardView implements OnMapReadyCallback {

    @Bind(R.id.tv_vat_nickname)
    TextView textViewNickname;
    @Bind(R.id.mv_vat)
    MapView mapView;
    @Bind(R.id.ll_vat_default)
    LinearLayout linearLayoutDefaultNoAddress;
    @Bind(R.id.tv_vat_address)
    TextView textViewAddress;
    @Bind(R.id.ib_vat_delete)
    ImageButton buttonDelete;
    @Bind(R.id.button_vat_edit_address)
    Button buttonEdit;
    @BindString(R.string.default_address_nickname)
    String defaultNickName;
    @Bind(R.id.ll_vat)
    LinearLayout linearLayout;

    Context mContext;
    Address address;
    GoogleMap mGoogleMap;
    boolean currentlySelected;
    boolean activateMaps;
    View view;
    private static final String TAG = "AddressView";

    public AddressView(Context context) {
        super(context);
    }

    public AddressView(Context context, Address address, boolean currentlySelected, View view, boolean activateMaps) {
        this(context);
        this.mContext = context;
        this.address = address;
        this.currentlySelected = currentlySelected;
        this.view = view;
        this.activateMaps = activateMaps;
        ButterKnife.bind(this, view);
        loadAddressDataIntoUi();
    }

    private void loadAddressDataIntoUi() {

        boolean nickNameExists = false;
        boolean addressExists = false;

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        linearLayout.setMinimumWidth(width-8*16);

        //load map view
        try {
            if (activateMaps) {
                mapView.onCreate(null);
                mapView.onResume();
                mapView.getMapAsync(this);
            } else {
                mapView.onPause();
                mapView.onDestroy();
            }
        }catch (Exception e) {
            Log.d(TAG, "maps activation problem", e);
        }

        //set nickname
        if(!TextUtils.isEmpty(address.getNickname())) {
            textViewNickname.setText(address.getNickname());
            textViewNickname.setTextColor(AndroidCompatUtil.getColor(mContext, R.color.primary_text));
            nickNameExists = true;
        } else {
            textViewNickname.setText(defaultNickName);
            textViewNickname.setTextColor(AndroidCompatUtil.getColor(mContext, R.color.secondary_text));
        }

        //set address
        String addressString  = !TextUtils.isEmpty(address.getName())?address.getName()+"\n":"";
        addressString += !TextUtils.isEmpty(address.getAddress())?address.getAddress()+"\n":"";
        addressString += !TextUtils.isEmpty(address.getPhoneNumber()+"")?"Ph. " + address.getPhoneNumber():"";
        if(!TextUtils.isEmpty(addressString)) {
            textViewAddress.setText(addressString);
            textViewAddress.setVisibility(VISIBLE);
            linearLayoutDefaultNoAddress.setVisibility(GONE);
            addressExists = true;
        } else {
            textViewAddress.setVisibility(GONE);
            linearLayoutDefaultNoAddress.setVisibility(VISIBLE);
        }

        //load buttons
        if(nickNameExists || addressExists) {
            buttonEdit.setText("EDIT ADDRESS");
        } else {
            buttonEdit.setText("SET ADDRESS");
        }

        buttonEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressEditIntent = new Intent(Constants.ACTION_EDIT_ADDRESS);
                Parcelable parcelableAddress = Parcels.wrap(address);
                addressEditIntent.putExtra("address", parcelableAddress);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(addressEditIntent);
            }
        });

        buttonDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressEditIntent = new Intent(Constants.ACTION_EDIT_ADDRESS);
                Parcelable parcelableAddress = Parcels.wrap(address);
                addressEditIntent.putExtra("address", parcelableAddress);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(addressEditIntent);
            }
        });

        refreshAddressHighlight();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(mContext);
        } catch (Exception e) {
            Log.d(TAG, "exception while initializing map", e);
        }
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.setOnMapClickListener(null);
        mGoogleMap.setOnMarkerClickListener(null);
        LatLng latlng = new LatLng(address.getGpsLat(), address.getGpsLong());
        mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        .draggable(false)
        .position(latlng));
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latlng, 16.0f);
        mGoogleMap.moveCamera(cu);
    }

    private void refreshAddressHighlight() {
        if(currentlySelected) {
            linearLayout.setBackgroundColor(AndroidCompatUtil.getColor(mContext, R.color.light_green_background));
        } else {
            linearLayout.setBackgroundColor(AndroidCompatUtil.getColor(mContext, R.color.offwhite));
        }
    }
}
