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
    Context context;
    private static final String DELIVERY_OPTIONS_SELECTIONS_KEY = "11";
    ImageButton pickUp;
    ImageButton deliveryBoy;
    RippleView ripplePickUp;
    RippleView rippleDeliveryBoyButton;
    int flag = 0;
    public final static int FLAG_PICK_UP_BUTTON_SELECTED = 3;
    public final static int FLAG_DELIVERY_BUTTON_SELECTED = 4;

    public ChooseDeliveryOptionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_delivery_option, container, false);
        pickUp = (ImageButton) view.findViewById(R.id.pick_up);
        deliveryBoy = (ImageButton) view.findViewById(R.id.delivery_boy);

        ripplePickUp = (RippleView) view.findViewById(R.id.ripple_effect_pick_button);
        rippleDeliveryBoyButton = (RippleView) view.findViewById(R.id.ripple_effect_deliver_button);

        context = getActivity();

        fixImageButtonsSize();
        ripplePickUp.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                selectImageButton(SelectedImageButton.BUTTON_PICKUP);

            }
        });
        rippleDeliveryBoyButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                selectImageButton(SelectedImageButton.BUTTON_DELIVERY);
            }
        });

        Bundle bundle = getArguments();
        int getSelectedButton = bundle.getInt(DELIVERY_OPTIONS_SELECTIONS_KEY);
        if (getSelectedButton == FLAG_PICK_UP_BUTTON_SELECTED) {
            selectImageButton(SelectedImageButton.BUTTON_PICKUP);
        } else if (getSelectedButton == FLAG_DELIVERY_BUTTON_SELECTED) {
            selectImageButton(SelectedImageButton.BUTTON_DELIVERY);
        }
        return view;
    }

    public void selectImageButton(SelectedImageButton selectedImageButton) {
        switch (selectedImageButton) {
            case BUTTON_PICKUP:
                flag = FLAG_PICK_UP_BUTTON_SELECTED;
                pickUp.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_pickup_button_selected));
                fixImageButtonsSize();
                deliveryBoy.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_delivery_button));
                break;
            case BUTTON_DELIVERY:
                deliveryBoy.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_delivery_boy_button_selected));
                pickUp.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_pick_button));
                fixImageButtonsSize();
                flag = FLAG_DELIVERY_BUTTON_SELECTED;
                break;
        }
    }


   public int getSelectedButton() {
        return this.flag;
    }


    enum SelectedImageButton {
        BUTTON_PICKUP, BUTTON_DELIVERY
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
