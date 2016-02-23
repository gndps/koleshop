package com.koleshop.appkoleshop.ui.seller.fragments.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ShopSettingsFragment extends Fragment {

    private static final String TAG = "ShopSettingsFrag";

    @Bind(R.id.met_seller_shop_name)
    MaterialEditText materialEditTextShopName;

    @Bind(R.id.met_seller_shop_phone)
    MaterialEditText materialEditTextShopPhone;

    @Bind(R.id.met_seller_shop_address)
    MaterialEditText materialEditTextShopAddress;

    @Bind(R.id.button_change_shop_gps_location)
    Button buttonChangeGpsLocation;

    @Bind(R.id.tv_shop_settings_open_time)
    TextView textViewOpenTime;

    @Bind(R.id.tv_shop_settings_close_time)
    TextView textViewCloseTime;

    @Bind(R.id.cv_home_delivery)
    CardView cardViewHomeDelivery;

    @Bind(R.id.rl_gps_location)
    RelativeLayout relativeLayoutGpsLocationButton;

    private SellerSettings sellerSettings;
    private Address address;

    private OnFragmentInteractionListener mListener;

    private boolean setupMode;

    public ShopSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void settingsModified();
    }

    public void setSellerSettings(SellerSettings sellerSettings, boolean setupMode) {
        this.setupMode = setupMode;
        this.sellerSettings = sellerSettings;
        loadDataIntoUi();
        initializeChangeListeners();
    }

    public String validateStep1() {
        if(materialEditTextShopName.getText().toString().trim().isEmpty()) {
            return "Shop name can't be empty";
        }
        if(materialEditTextShopPhone.getText().toString().trim().isEmpty()) {
            return "Phone number can't be empty";
        }
        if(materialEditTextShopAddress.getText().toString().trim().isEmpty()) {
            return "Address can't be empty";
        }
        if(sellerSettings.getShopOpenTime()==0 && sellerSettings.getShopCloseTime()==0) {
            return "Please set a shop open/close time";
        }
        return "";
    }

    public boolean validateGpsLocation() {
        if(sellerSettings==null || (sellerSettings.getAddress().getGpsLong() == 0 && sellerSettings.getAddress().getGpsLong() == 0)) {
            return false;
        } else {
            return true;
        }
    }

    private void loadDataIntoUi() {
        if (sellerSettings != null) {
            address = sellerSettings.getAddress();
            if (address != null) {
                String phoneString = address.getPhoneNumber() == null ? "" : address.getPhoneNumber() + "";
                String nameString = address.getName() == null ? "" : address.getName();
                String addressString = address.getAddress() == null ? "" : address.getAddress();
                materialEditTextShopName.setText(nameString);
                materialEditTextShopPhone.setText(phoneString);
                materialEditTextShopAddress.setText(addressString);
                textViewOpenTime.setText(CommonUtils.getTimeStringFromMinutes(sellerSettings.getShopOpenTime(), false));
                textViewCloseTime.setText(CommonUtils.getTimeStringFromMinutes(sellerSettings.getShopCloseTime(), false));
            }
        }

        if (setupMode) {
            cardViewHomeDelivery.setVisibility(View.GONE);
            relativeLayoutGpsLocationButton.setVisibility(View.GONE);
        }
    }

    private void initializeChangeListeners() {
        materialEditTextShopName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                address.setName(s.toString());
                mListener.settingsModified();
            }
        });
        materialEditTextShopPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Long phoneNumberLong = null;
                try {
                    phoneNumberLong = Long.parseLong(s.toString());
                } catch (Exception e) {
                    //Log.d(TAG, "whatever");
                }
                address.setPhoneNumber(phoneNumberLong);
                mListener.settingsModified();
            }
        });
        materialEditTextShopAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                address.setAddress(s.toString());
                mListener.settingsModified();
            }
        });
    }
}
