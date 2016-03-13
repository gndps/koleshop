package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import com.andexert.library.RippleView;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;


public class ChooseDeliveryTimeFragment extends Fragment {
    private static final int CHOOSE_DELIVERY_BUTTON_HAS_BEEN_CLICKED = 6;
    private static final String TIME_KEY = "key";
    private static boolean TOMORROW_CLICKED = false;
    private static final int ASAP_BUTTON = 3;
    private static final int CHOOSE_DELIVERY_TIME_BUTTON_SELECTED = 4;

    View view;
    private Context context;

    final static int IMAGE_BUTTON_PADDING = 64;
    private ImageButton asapButton;
    private ImageButton chooseDeliveryTimeButton;
    private TextView asapTextView;
    private TextView chooseDeliverTimeTextView;
    private RippleView rippleChooseDeliverButton;
    private RippleView rippleAsapButton;
    private ViewFlipper asapViewFlipper;
    private ViewFlipper chooseDeliveryViewFlipper;
    Animation flipAnim;
    private BackPressedListener backPressedListener;
    int hour, minutes;
    public int currentState = 0;
    private final static int STATE_ONE_SHOW_NORMAL_BUTTONS = 1;
    public final static int ASAP_BUTTON_CLICKED = 3;
    public final static int CHOOSE_DELIVERY_BUTTON_CLICKED = 4;
    private ImageButton todayImageButton;
    private ImageButton tomorrowImageButton;
    private RippleView rippleTodayButton;
    private RippleView rippleTomorrowButton;
    private boolean ASAP_CLICKED = false;
    private String time = "";
    private final int SHOW_TIME_PICKER = 5;
    private boolean TIME_FETCHED = false;
    private boolean TIME_APPEARED = false;
    private int flag = 0;
    private static final String DELIVERY_TIME_SELECTION_KEY = "12";
    private static final int ANIMATION_TIME=180;
    private boolean TimePicked=false;
    private Animation flip;
    private final float pivotXValue=200f;
    private final float pivotYValue=200f;
    private Animation notTimePickedAnimation;
    private int deliveryTimeHours;
    private int deliveryTimeMinutes;

    public ChooseDeliveryTimeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_choose_delivery_time_fragment, container, false);
        context = getActivity();

        asapButton = (ImageButton) view.findViewById(R.id.asap_button);
        chooseDeliveryTimeButton = (ImageButton) view.findViewById(R.id.choose_delivery_time_button);

        asapTextView = (TextView) view.findViewById(R.id.asap_textview);
        chooseDeliverTimeTextView = (TextView) view.findViewById(R.id.choose_deliver_time_text_view);
        //View Flipper
        asapViewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper_asap_section);
        chooseDeliveryViewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper_choose_delivery_section);

        rippleChooseDeliverButton = (RippleView) view.findViewById(R.id.ripple_effect_choose_delivery_time_button);
        rippleAsapButton = (RippleView) view.findViewById(R.id.ripple_effect_asap_button);

        todayImageButton = (ImageButton) view.findViewById(R.id.today_button);
        tomorrowImageButton = (ImageButton) view.findViewById(R.id.tomorrow_button);

        rippleTodayButton = (RippleView) view.findViewById(R.id.ripple_for_today_button);
        rippleTomorrowButton = (RippleView) view.findViewById(R.id.ripple_for_tomorrow_button);
        currentState = STATE_ONE_SHOW_NORMAL_BUTTONS;
        refreshTheButtons();
        setClickListenersOnButtons();
        Bundle bundle = getArguments();

        flipAnim = new ScaleAnimation(1f, 0f, 1f, 1f, Animation.ABSOLUTE, pivotXValue, Animation.ABSOLUTE, pivotYValue);
        notTimePickedAnimation=new ScaleAnimation(1f, 0f, 1f, 1f, Animation.ABSOLUTE, pivotXValue, Animation.ABSOLUTE, pivotYValue);

        notTimePickedAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                asapViewFlipper.setDisplayedChild(0);
                chooseDeliveryViewFlipper.setDisplayedChild(0);
                asapButton.startAnimation(flip);
                chooseDeliveryTimeButton.startAnimation(flip);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        flipAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                    currentState = CHOOSE_DELIVERY_BUTTON_CLICKED;
                    refreshTheButtons();
                    flip = new ScaleAnimation(0f, 1f, 1f, 1f, Animation.ABSOLUTE, pivotXValue, Animation.ABSOLUTE, pivotYValue);
                    showTodayTomorrowAnimation(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        int getSelectedButton = bundle.getInt(DELIVERY_TIME_SELECTION_KEY);
        if (getSelectedButton == ASAP_BUTTON) {
            currentState = ASAP_BUTTON_CLICKED;
        } else if (getSelectedButton == CHOOSE_DELIVERY_TIME_BUTTON_SELECTED) {
            currentState = CHOOSE_DELIVERY_BUTTON_HAS_BEEN_CLICKED;
        }
        String time = bundle.getString(TIME_KEY);
        this.time = time;
        fixImageButtonsSize();
        ASAP_CLICKED = true;
        return view;
    }

    private void setClickListenersOnButtons() {
        rippleAsapButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                currentState = ASAP_BUTTON_CLICKED;
                refreshTheButtons();
            }
        });
        rippleChooseDeliverButton.setOnRippleCompleteListener(
                new RippleView.OnRippleCompleteListener() {
                    @Override
                    public void onComplete(RippleView rippleView) {
                            showTodayTomorrowAnimation(true);

                    }
                });
        rippleTodayButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                currentState = SHOW_TIME_PICKER;
                TOMORROW_CLICKED = false;
                refreshTheButtons();
            }
        });
        rippleTomorrowButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                currentState = SHOW_TIME_PICKER;
                TOMORROW_CLICKED = true;
                refreshTheButtons();
            }
        });
    }

    private void showTodayTomorrowAnimation(boolean show) {
        if(!show) {
            TimePicked=true;
            flip.setDuration(ANIMATION_TIME);
            todayImageButton.startAnimation(flip);
            tomorrowImageButton.startAnimation(flip);
            asapTextView.setVisibility(View.VISIBLE);
            chooseDeliverTimeTextView.setVisibility(View.VISIBLE);
        }
        else {
            flipAnim.setDuration(ANIMATION_TIME);
             asapButton.startAnimation(flipAnim);
            chooseDeliveryTimeButton.startAnimation(flipAnim);
            asapTextView.setVisibility(View.INVISIBLE);
            chooseDeliverTimeTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshTheButtons() {
        switch (currentState) {
            case STATE_ONE_SHOW_NORMAL_BUTTONS:
                if (ASAP_CLICKED) {
                    asapButton.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_asap_button));
                }

                asapButton.setImageDrawable(KoleshopUtils.getTextDrawable(context, "ASAP", (int) CommonUtils.getScreenWidth(context), true, Color.TRANSPARENT, Color.WHITE));
                asapViewFlipper.setDisplayedChild(0);
                chooseDeliveryViewFlipper.setDisplayedChild(0);
                break;
            case ASAP_BUTTON_CLICKED:
                flag = ASAP_BUTTON_CLICKED;
                asapButton.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_asap_button_selected));
                fixImageButtonsSize();
                chooseDeliveryTimeButton.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_choose_delivery_button));
                ASAP_CLICKED = true;
                if (TIME_APPEARED) {
                    chooseDeliverTimeTextView.setText("   Choose   " + "\r\n" + "  Delivery  " + "\n\r" + "Time");
                }

                break;
            case CHOOSE_DELIVERY_BUTTON_CLICKED:
                fixImageButtonsSizes(todayImageButton, tomorrowImageButton, context);
                flag = CHOOSE_DELIVERY_BUTTON_CLICKED;
                if (backPressedListener == null) {
                    backPressedListener = (BackPressedListener) context;
                }
                asapButton.setBackground(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.shape_asap_button));
                backPressedListener.setBackPressedHandledByFragment(true);
                asapViewFlipper.setDisplayedChild(1);
                chooseDeliveryViewFlipper.setDisplayedChild(1);
                int width = (int) getImageButtonHeight() - 60;
                todayImageButton.setImageDrawable(KoleshopUtils.getTextDrawable(context, "TODAY", width / 5, true, Color.TRANSPARENT, Color.WHITE));
                tomorrowImageButton.setImageDrawable(KoleshopUtils.getTextDrawable(context, "TOMORROW", width / 5, true, Color.TRANSPARENT, R.color.text_grey_on_yellow));
                break;
            case SHOW_TIME_PICKER:
                if(flag == ASAP_BUTTON_CLICKED) {
                    flag = 0;
                }
                getTimePicker();
                break;
            case CHOOSE_DELIVERY_BUTTON_HAS_BEEN_CLICKED:
                flag =  CHOOSE_DELIVERY_BUTTON_CLICKED;
                if (backPressedListener == null) {
                    backPressedListener = (BackPressedListener) context;
                }
                backPressedListener.setBackPressedHandledByFragment(false);

                asapViewFlipper.setDisplayedChild(0);

                if (ASAP_CLICKED) {
                    chooseDeliveryTimeButton.setBackground(AndroidCompatUtil.getDrawable(context, R.drawable.shape_choose_delivery_time_button));
                    chooseDeliveryViewFlipper.setDisplayedChild(0);
                }

                fixImageButtonsSize();
                chooseDeliverTimeTextView.setText(time);

        }
        ASAP_CLICKED = false;
    }

    private void fixImageButtonsSizes(ImageButton todayImageButton, ImageButton tomorrowImageButton, Context context) {

        float imageButtonWidthHeight = getImageButtonHeight();

        ViewGroup.LayoutParams parameterPickButton = todayImageButton.getLayoutParams();
        parameterPickButton.width = (int) imageButtonWidthHeight;
        parameterPickButton.height = (int) imageButtonWidthHeight;

        int asapButtonPadding = (int) (imageButtonWidthHeight * 0.2);
        todayImageButton.setPadding(asapButtonPadding, asapButtonPadding, asapButtonPadding, asapButtonPadding);
        todayImageButton.setLayoutParams(parameterPickButton);


        ViewGroup.LayoutParams paramDeliveryButton = tomorrowImageButton.getLayoutParams();
        paramDeliveryButton.height = (int) imageButtonWidthHeight;
        paramDeliveryButton.width = (int) imageButtonWidthHeight;

        tomorrowImageButton.setPadding(asapButtonPadding, asapButtonPadding, asapButtonPadding, asapButtonPadding);
        tomorrowImageButton.setLayoutParams(paramDeliveryButton);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTheButtons();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("fragment_state", currentState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BackPressedListener) {
            backPressedListener = (BackPressedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void getTimePicker() {

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                int hour = selectedHour;
                int minutes = selectedMinute;

                String timeSet = "";
                if (hour > 12) {
                    hour -= 12;
                    timeSet = "PM";
                } else if (hour == 0) {
                    hour += 12;
                    timeSet = "AM";
                } else if (hour == 12)
                    timeSet = "PM";
                else
                    timeSet = "AM";

                String min = "";
                if (minutes < 10)
                    min = "0" + minutes;
                else
                    min = String.valueOf(minutes);

                time = new StringBuilder().append(hour).append(':')
                        .append(min).append(" ").append(timeSet).toString();

                if (backPressedListener == null) {
                    backPressedListener = (BackPressedListener) context;
                }
                backPressedListener.setBackPressedHandledByFragment(false);

                if (!time.equals("")) {
                    if (TOMORROW_CLICKED) {
                        deliveryTimeHours = 24 + selectedHour;
                        deliveryTimeMinutes = selectedMinute;
                        time = "Tomorrow" + "\r\n" + time;
                        chooseDeliverTimeTextView.setText(time);

                    } else {
                        deliveryTimeHours = selectedHour;
                        deliveryTimeMinutes = selectedMinute;
                        time = "Today" + "\r\n" + time;
                        chooseDeliverTimeTextView.setText(time);
                    }
                    chooseDeliveryTimeButton.setBackground(AndroidCompatUtil.getDrawable(context, R.drawable.shape_choose_delivery_time_button));
                    fixImageButtonsSize();
                    TIME_FETCHED = true;
                    TIME_APPEARED = true;

                    currentState = STATE_ONE_SHOW_NORMAL_BUTTONS;
                    refreshTheButtons();
                }
            }
        }, hour, minutes, false);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void fixImageButtonsSize() {
        float imageButtonWidthHeight = getImageButtonHeight();

        ViewGroup.LayoutParams parameterPickButton = asapButton.getLayoutParams();
        parameterPickButton.width = (int) imageButtonWidthHeight;
        parameterPickButton.height = (int) imageButtonWidthHeight;

        int asapButtonPadding = (int) (imageButtonWidthHeight * 0.2);
        asapButton.setPadding(asapButtonPadding, asapButtonPadding, asapButtonPadding, asapButtonPadding);
        asapButton.setLayoutParams(parameterPickButton);


        ViewGroup.LayoutParams paramDeliveryButton = chooseDeliveryTimeButton.getLayoutParams();
        paramDeliveryButton.height = (int) imageButtonWidthHeight;
        paramDeliveryButton.width = (int) imageButtonWidthHeight;

        chooseDeliveryTimeButton.setPadding(asapButtonPadding, asapButtonPadding, asapButtonPadding, asapButtonPadding);
        chooseDeliveryTimeButton.setLayoutParams(paramDeliveryButton);

    }

    private float getImageButtonHeight() {
        float width = CommonUtils.getScreenWidthInPixels(context);
        float height = CommonUtils.getScreenHeightInPixels(context);
        float dim = Math.min(width, height) / 2;
        return dim - IMAGE_BUTTON_PADDING;
    }

    public void onBackPressed() {
        notTimePickedAnimation.setDuration(ANIMATION_TIME);
        todayImageButton.startAnimation(notTimePickedAnimation);
        tomorrowImageButton.startAnimation(notTimePickedAnimation);
    }

    public int getFlag() {
        return this.flag;
    }

    public int getDeliveryTimeHours() {
        return deliveryTimeHours;
    }

    public int getDeliveryTimeMinutes() {
        return deliveryTimeMinutes;
    }

    public interface BackPressedListener {
        void setBackPressedHandledByFragment(boolean isBackPressedHandled);
    }

    public String getTime() {
        return time;
    }
}
