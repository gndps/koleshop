package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.andexert.library.RippleView;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;


public class ChooseDeliveryOptionFragment extends Fragment {

    final static int IMAGE_BUTTON_PADDING = 64;
    public static final String KEY_SELECTED_BUTTON = "key_pickup_delivery_selection";
    public final static int PICK_UP_BUTTON = 3;
    public final static int DELIVERY_BUTTON = 4;
    public static final String HOME_DELIVERY_BUTTON_DISABLED = "key_home_delivery_disabled";

    Context context;
    ImageButton pickUp;
    ImageButton deliveryBoy;
    RippleView ripplePickUp;
    RippleView rippleDeliveryBoyButton;
    int selectedButton = 0;
    boolean homeDeliveryDisabled;

    public ChooseDeliveryOptionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_delivery_option, container, false);
        context = getActivity();

        //inflate views
        pickUp = (ImageButton) view.findViewById(R.id.pick_up);
        deliveryBoy = (ImageButton) view.findViewById(R.id.delivery_boy);
        ripplePickUp = (RippleView) view.findViewById(R.id.ripple_effect_pick_button);
        rippleDeliveryBoyButton = (RippleView) view.findViewById(R.id.ripple_effect_deliver_button);

        if(getArguments()!=null) {
            selectedButton = getArguments().getInt(KEY_SELECTED_BUTTON);
            homeDeliveryDisabled = getArguments().getBoolean(HOME_DELIVERY_BUTTON_DISABLED);
        }

        fixImageButtonsSize();
        setupRippleClickListeners();

        updateButtonSelection();

        return view;
    }

    private void setupRippleClickListeners() {
        ripplePickUp.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                selectedButton = PICK_UP_BUTTON;
                updateButtonSelection();

            }
        });
        if(!homeDeliveryDisabled) {
            rippleDeliveryBoyButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    selectedButton = DELIVERY_BUTTON;
                    updateButtonSelection();
                }
            });
        }
    }

    public void updateButtonSelection() {
        switch (selectedButton) {
            case PICK_UP_BUTTON:
                pickUp.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_pickup_button_selected));
                fixImageButtonsSize();
                    deliveryBoy.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_delivery_button));
                if(homeDeliveryDisabled) {
                    deliveryBoy.setAlpha(0.3f);
                }
                break;
            case DELIVERY_BUTTON:
                deliveryBoy.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_delivery_boy_button_selected));
                pickUp.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_pick_button));
                fixImageButtonsSize();
                break;
        }
    }


   public int getSelectedButton() {
        return selectedButton;
    }

    public void fixImageButtonsSize() {
        float imageButtonWidthHeight = getImageButtonHeight();

        ViewGroup.LayoutParams parameterPickButton = this.pickUp.getLayoutParams();
        parameterPickButton.width = (int) imageButtonWidthHeight;
        parameterPickButton.height = (int) imageButtonWidthHeight;
        int pickupButtonPadding = (int) (imageButtonWidthHeight * 0.2);
        pickUp.setPadding(pickupButtonPadding, pickupButtonPadding, pickupButtonPadding, pickupButtonPadding);
        pickUp.setLayoutParams(parameterPickButton);

        ViewGroup.LayoutParams paramDeliveryButton = deliveryBoy.getLayoutParams();
        paramDeliveryButton.height = (int) imageButtonWidthHeight;
        paramDeliveryButton.width = (int) imageButtonWidthHeight;
        deliveryBoy.setPadding(pickupButtonPadding, pickupButtonPadding, pickupButtonPadding, pickupButtonPadding);
        deliveryBoy.setLayoutParams(paramDeliveryButton);

    }

    private float getImageButtonHeight() {
        float width = CommonUtils.getScreenWidthInPixels(context);
        float height = CommonUtils.getScreenHeightInPixels(context);
        float dim = Math.min(width, height)/2;
        return dim - IMAGE_BUTTON_PADDING;
    }
}
