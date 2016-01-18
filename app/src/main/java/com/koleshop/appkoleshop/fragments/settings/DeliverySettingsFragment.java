package com.koleshop.appkoleshop.fragments.settings;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DeliverySettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SellerSettings sellerSettings;

    @Bind(R.id.met_max_delivery_distance)
    MaterialEditText metMaxDeliveryDistance;

    @Bind(R.id.met_min_order)
    MaterialEditText metMinOrder;

    @Bind(R.id.met_delivery_charges)
    MaterialEditText metDeliveryCharges;

    @Bind(R.id.tv_settings_deliery_start_time)
    TextView tvDeliveryStartTime;

    @Bind(R.id.tv_settings_deliery_end_time)
    TextView tvDeliveryEndTime;

    public DeliverySettingsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_delivery_settings, container, false);
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

    public void setSellerSettings(SellerSettings sellerSettings) {
        this.sellerSettings = sellerSettings;
        loadDataIntoUi();
        addChangeListeners();
    }

    private void loadDataIntoUi() {
        if(sellerSettings!=null) {
            Long maxDeliveryDistance = sellerSettings.getMaximumDeliveryDistance();
            if(maxDeliveryDistance!=null) {
                metMaxDeliveryDistance.setText(""+maxDeliveryDistance);
            }
            metMinOrder.setText(CommonUtils.getPriceStringFromFloat(sellerSettings.getMinimumOrder()));
            metDeliveryCharges.setText(CommonUtils.getPriceStringFromFloat(sellerSettings.getDeliveryCharges()));
            tvDeliveryStartTime.setText(CommonUtils.getSettingsTimeFromDate(sellerSettings.getDeliveryStartTime()));
            tvDeliveryEndTime.setText(CommonUtils.getSettingsTimeFromDate(sellerSettings.getDeliveryEndTime()));
        }
    }

    private void addChangeListeners() {
        metMaxDeliveryDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Long maxDelieryDistance = null;
                try{
                    maxDelieryDistance = Long.parseLong(s.toString());
                } catch (Exception e){
                    //whatever
                }
                sellerSettings.setMaximumDeliveryDistance(maxDelieryDistance);
                mListener.settingsModified();
            }
        });
        metMinOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Float minOrder = null;
                try{
                    minOrder = Float.parseFloat(s.toString());
                } catch (Exception e){
                    //whatever
                }
                sellerSettings.setMinimumOrder(minOrder);
                mListener.settingsModified();
            }
        });
        metDeliveryCharges.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Float deliveryCharges = null;
                try{
                    deliveryCharges = Float.parseFloat(s.toString());
                } catch (Exception e){
                    //whatever
                }
                sellerSettings.setDeliveryCharges(deliveryCharges);
                mListener.settingsModified();
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void settingsModified();
    }
}
