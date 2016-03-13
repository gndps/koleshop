package com.koleshop.appkoleshop.ui.buyer.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.util.RealmUtils;

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
    BuyerAddress address;
    GoogleMap mGoogleMap;
    boolean activateMaps;
    View view;
    AddressViewListener mListener;
    private boolean showOnlyDefaultAddress;

    private static final String TAG = "AddressView";

    public AddressView(Context context) {
        super(context);
    }

    public AddressView(Context context, BuyerAddress address, View view, boolean activateMaps, AddressViewListener listener, boolean showOnlyDefaultAddress) {
        this(context);
        this.mContext = context;
        this.address = address;
        this.view = view;
        this.activateMaps = activateMaps;
        this.mListener = listener;
        this.showOnlyDefaultAddress = showOnlyDefaultAddress;
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
        if(address.getPhoneNumber()!=null && address.getPhoneNumber()>100000) {
            addressString += !TextUtils.isEmpty(address.getPhoneNumber() + "") ? "Ph. " + address.getPhoneNumber() : "";
        }
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

        if(!showOnlyDefaultAddress) {
            buttonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure to delete?")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mListener.deleteAddress(address);
                                }
                            })
                            .setNegativeButton("CANCEL", null);
                    builder.create().show();
                }
            });
        } else {
            buttonDelete.setVisibility(GONE);
        }

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
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latlng, 14.0f);
        mGoogleMap.moveCamera(cu);
    }

    private void refreshAddressHighlight() {
        if(address.isDefaultAddress()) {
            linearLayout.setBackground(AndroidCompatUtil.getDrawable(mContext, R.drawable.address_fragment_selected));
        } else {
            linearLayout.setBackgroundColor(AndroidCompatUtil.getColor(mContext, R.color.offwhite));
        }
    }

    public interface AddressViewListener {
        void deleteAddress(BuyerAddress address);
    }
}
