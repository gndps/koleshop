package com.koleshop.appkoleshop.ui.seller.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.ui.common.activities.MapsActivity;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.koleshop.appkoleshop.ui.seller.fragments.settings.DeliverySettingsFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.settings.HomeDeliveryFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.settings.ShopSettingsFragment;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.services.SettingsIntentService;

import org.parceler.Parcels;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SellerSettingsActivity extends AppCompatActivity implements ShopSettingsFragment.OnFragmentInteractionListener,
        DeliverySettingsFragment.OnFragmentInteractionListener,SettingsIntentService.SettingsReceiver {

    private static int VIEW_FLIPPER_CHILD_LOADING = 0;
    private static int VIEW_FLIPPER_CHILD_ERROR = 1;
    private static int VIEW_FLIPPER_CHILD_SETTINGS = 2;

    @BindString(R.string.navigation_drawer_settings)
    String titleSellerSettings;
    @BindView(R.id.switch_home_delivery_toggle)
    Switch switchHomeDeliveryToggle;
    @BindView(R.id.ib_delivery_settings)
    ImageButton imageButtonDeliverySettings;
    @BindView(R.id.vf_seller_settings)
    ViewFlipper viewFlipper;
    @BindView(R.id.button_bar_back_next)
    RelativeLayout buttonBarBackNext;
    @BindView(R.id.reusable_back_button)
    Button buttonBack;
    @BindView(R.id.reusable_next_button)
    Button buttonNext;

    boolean showingDeliverySettings;
    ShopSettingsFragment shopSettingsFragment;
    DeliverySettingsFragment deliverySettingsFragment;
    Toolbar toolbar;
    SellerSettings settings;
    Context mContext;
    boolean settingsModified;
    boolean setupMode;
    ProgressDialog processing;
    String requestId;
    MenuItem saveMenuButton;
    int currentStep;
    private BroadcastReceiver settingsBroadcastReceiver;
    private HomeDeliveryFragment homeDeliveryFragment;
    private boolean homeDeliveryOptionSelected;
    private boolean loadSettingsIntoUiOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_seller_settings);
        ButterKnife.bind(this);
        doInitialStuff();
        setupToolbar();

        Bundle extras = getIntent().getExtras();
        if(savedInstanceState!=null && savedInstanceState.getParcelable("settings")!=null) {
            settings = Parcels.unwrap(savedInstanceState.getParcelable("settings"));
            setupMode = savedInstanceState.getBoolean("setupMode");
            settingsModified = savedInstanceState.getBoolean("settingsModified");
            requestId = savedInstanceState.getString("requestId");
            currentStep = savedInstanceState.getInt("currentStep");
        } else {
            //if settings available in cache, then load from cache, else load from internet
            if(extras!=null) {
                setupMode = extras.getBoolean("setupMode");
                currentStep = extras.getInt("currentStep");
            }

            if(!setupMode) {
                settings = KoleshopUtils.getSettingsFromCache(this);
            }
        }

        if(settings!=null || setupMode) {
            loadSettingsIntoUi();
        } else {
            requestSettingsFromInternet();
        }

        initializeBroadcastReceivers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_seller_settings, menu);
        saveMenuButton = menu.findItem(R.id.action_save_settings);
        if(setupMode) {
            saveMenuButton.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save_settings:
                if (validateSettingsBeforeSaving()) {
                    saveSettings();
                }
                return true;
            /*case R.id.action_discard_product_changes:
                processingAnimation(false);
                onBackPressed();*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!setupMode) {
            if (showingDeliverySettings) {
                toggleDeliveryFragmentVisibility(false, true);
            } else if(settingsModified) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                builder.setMessage("Save the changed settings?")
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (validateSettingsBeforeSaving()) {
                                    saveSettings();
                                }
                            }
                        })
                        .setNegativeButton(R.string.dont_save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SellerSettingsActivity.super.onBackPressed();
                            }
                        })
                        .setNeutralButton(R.string.cancel, null);
                builder.create().show();
            } else {
                super.onBackPressed();
            }
        } else {
            if(currentStep == 3) {
                currentStep--;
                changeGpsLocation(null);
            } else if(currentStep == 4) {
                currentStep--;
                buttonNext.setText("NEXT");
                //load home delivery full screen fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(homeDeliveryFragment)
                        .hide(shopSettingsFragment)
                        .hide(deliverySettingsFragment)
                        .commit();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(settings!=null) {
            Parcelable settingsParcel = Parcels.wrap(SellerSettings.class, settings);
            outState.putParcelable("settings", settingsParcel);
            outState.putBoolean("setupMode", setupMode);
            outState.putBoolean("settingsModified", settingsModified);
            outState.putString("requestId", requestId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(settingsBroadcastReceiver, new IntentFilter(Constants.ACTION_RELOAD_SETTINGS));
        if(loadSettingsIntoUiOnResume) {
            loadSettingsIntoUiOnResume = false;
            loadSettingsIntoUi();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(settingsBroadcastReceiver);
    }

    private void initializeBroadcastReceivers() {
        settingsBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION_RELOAD_SETTINGS)) {
                    reloadSettings(null);
                }
            }
        };
    }

    private void doInitialStuff() {

        shopSettingsFragment = (ShopSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.frag_shop_settings);
        deliverySettingsFragment = (DeliverySettingsFragment) getSupportFragmentManager().findFragmentById(R.id.frag_delivery_settings);

        setupUiForKeyboardHideOnOutsideTouch(shopSettingsFragment.getView());
        setupUiForKeyboardHideOnOutsideTouch(deliverySettingsFragment.getView());

        switchHomeDeliveryToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setHomeDelivery(isChecked);
                loadHomeDeliverySettingsState(isChecked);
            }
        });
        toggleDeliveryFragmentVisibility(false, false);
        loadHomeDeliverySettingsState(switchHomeDeliveryToggle.isChecked());

    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(titleSellerSettings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        TextView toolbarTextView = CommonUtils.getActionBarTextView(toolbar);
        if (toolbarTextView != null) {
            toolbarTextView.setTypeface(typeface);
        }
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);
        }
    }

    private void toggleDeliveryFragmentVisibility(boolean visible, boolean animate) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (animate) {
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        if (visible) {
            ft.show(deliverySettingsFragment)
                    .hide(shopSettingsFragment)
                    .commit();
            //toolbar.setTitle("Home Delivery Settings");
        } else {
            ft.show(shopSettingsFragment)
                    .hide(deliverySettingsFragment)
                    .commit();
            //toolbar.setTitle("Settings");
        }
        showingDeliverySettings = visible;
    }

    private void loadHomeDeliverySettingsState(boolean enabled) {
        if (enabled) {
            imageButtonDeliverySettings.setClickable(true);
            imageButtonDeliverySettings.setAlpha(1.0f);
        } else {
            imageButtonDeliverySettings.setClickable(false);
            imageButtonDeliverySettings.setAlpha(0.4f);
        }
    }

    public void goToDeliverySettings(View view) {
        toggleDeliveryFragmentVisibility(true, true);
    }

    private void requestSettingsFromInternet() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
        buttonBarBackNext.setVisibility(View.GONE);
        requestId = CommonUtils.randomString(6);
        SettingsIntentService.requestSellerSettings(this, requestId);
    }

    private void loadSettingsIntoUi() {
        if(settings==null) {
            settings = new SellerSettings();
            Address address = new Address();
            Long userId = PreferenceUtils.getUserId(mContext);
            address.setId(0l);
            address.setUserId(userId);
            settings.setAddress(address);
            settings.setPickupFromShop(true);
            settings.setUserId(userId);
            settings.setId(0l);
        }

        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SETTINGS);

        //shop settings fragment
        shopSettingsFragment.setSellerSettings(settings, setupMode);
        switchHomeDeliveryToggle.setChecked(settings.isHomeDelivery());

        //delivery settings fragment
        deliverySettingsFragment.setSellerSettings(settings);

        if(setupMode) {
            //show back/next button
            buttonBarBackNext.setVisibility(View.VISIBLE);
            if(currentStep == 1) {
                buttonBack.setVisibility(View.GONE);
            }

            if(homeDeliveryFragment==null) {
                homeDeliveryFragment = HomeDeliveryFragment.newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fl_settings_container, homeDeliveryFragment)
                        .hide(homeDeliveryFragment)
                        .commit();
            }

            if(currentStep==0) {
                currentStep = 1;
            }

            if(currentStep == 1) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(shopSettingsFragment)
                        .hide(deliverySettingsFragment)
                        .hide(homeDeliveryFragment)
                        .commit();
            } else if(currentStep == 2) {
                //it means that gps location activity should be selected
            } else if(currentStep == 3) {
                //load home delivery full screen fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(homeDeliveryFragment)
                        .hide(shopSettingsFragment)
                        .hide(deliverySettingsFragment)
                        .commit();

            } else if(currentStep == 4) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(deliverySettingsFragment)
                        .hide(shopSettingsFragment)
                        .hide(homeDeliveryFragment)
                        .commit();
            }

        } else {
            buttonBarBackNext.setVisibility(View.GONE);
        }

    }

    private boolean validateSettingsBeforeSaving() {
        hideSoftKeyboard(this);
        if(!shopSettingsFragment.validateStep1().isEmpty()) {
            Snackbar.make(toolbar, shopSettingsFragment.validateStep1(), Snackbar.LENGTH_LONG).setAction("action", null).show();
            if(!setupMode) {
                toggleDeliveryFragmentVisibility(false, false);
            }
            return false;
        }

        if(!shopSettingsFragment.validateGpsLocation()) {
            Snackbar.make(toolbar, "Please set shop GPS Location", Snackbar.LENGTH_LONG).setAction("action", null).show();
            return false;
        }

        if(switchHomeDeliveryToggle.isChecked() && !deliverySettingsFragment.validateData().isEmpty()) {
            Snackbar.make(toolbar, deliverySettingsFragment.validateData(), Snackbar.LENGTH_LONG).setAction("action", null).show();
            if(!setupMode) {
                toggleDeliveryFragmentVisibility(true, false);
            }
            return false;
        }
        return true;
    }

    private void saveSettings() {
        hideSoftKeyboard(this);
        processingAnimation(true);
        requestId = CommonUtils.randomString(6);
        SettingsIntentService.saveOrUpdateSettings(mContext, settings, requestId);
    }

    private void processingAnimation(boolean show) {
        if (processing == null) {
            processing = new ProgressDialog(mContext);
            processing.setCancelable(false);
            processing.setMessage("Saving Settings...");
        }
        if (show) {
            processing.show();
        } else {
            processing.hide();
        }
    }

    public void editShopOpenTime(View view) {
        Calendar openTime = Calendar.getInstance();
        int hour,minute;
        if(settings.getShopOpenTime()>=0) {
            hour = settings.getShopOpenTime()/60;
            minute = settings.getShopOpenTime()%60;
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setShopOpenTime(CommonUtils.getNumberOfMinutesFromHourAndMinutes(selectedHour, selectedMinute));
                timeChanged();
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Shop Open Time");
        mTimePicker.show();
    }

    public void editShopCloseTime(View view) {
        int hour,minute;
        if(settings.getShopCloseTime()>=0) {
            hour = settings.getShopCloseTime()/60;
            minute = settings.getShopCloseTime()%60;
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setShopCloseTime(CommonUtils.getNumberOfMinutesFromHourAndMinutes(selectedHour, selectedMinute));
                timeChanged();
            }
        }, hour, minute, false);
        mTimePicker.setTitle("Shop Close Time");
        mTimePicker.setIcon(R.drawable.ic_weather_night);
        mTimePicker.show();
    }

    public void editDeliveryStartTime(View view) {
        Calendar deliveryStartTime = Calendar.getInstance();
        int hour,minute;
        if(settings.getDeliveryStartTime()>=0) {
            hour = settings.getDeliveryStartTime()/60;
            minute = settings.getDeliveryStartTime()%60;
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setDeliveryStartTime(CommonUtils.getNumberOfMinutesFromHourAndMinutes(selectedHour, selectedMinute));
                timeChanged();
            }
        }, hour, minute, false);
        mTimePicker.setTitle("Deliver Start Time");
        mTimePicker.show();
    }

    public void editDeliveryEndTime(View view) {
        Calendar deliveryEndTime = Calendar.getInstance();
        int hour,minute;
        if(settings.getDeliveryEndTime()>=0) {
            hour = settings.getDeliveryEndTime()/60;
            minute = settings.getDeliveryEndTime()%60;
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setDeliveryEndTime(CommonUtils.getNumberOfMinutesFromHourAndMinutes(selectedHour, selectedMinute));
                timeChanged();
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Deliver End Time");
        mTimePicker.show();
    }

    public void timeChanged() {
        loadSettingsIntoUi();
    }

    public void changeGpsLocation(View view) {
        //start google maps activity for result
        Intent mapsActivityIntent = new Intent(mContext, MapsActivity.class);
        mapsActivityIntent.putExtra("twoButtonMode", true);
        if(setupMode) {
            mapsActivityIntent.putExtra("leftButtonTitle", "BACK");
            mapsActivityIntent.putExtra("rightButtonTitle", "NEXT");
        } else {
            mapsActivityIntent.putExtra("leftButtonTitle", "CANCEL");
            mapsActivityIntent.putExtra("rightButtonTitle", "DONE");
            mapsActivityIntent.putExtra("title", getString(R.string.title_shop_location));
            //mapsActivityIntent.putExtra("actionButtonTitle", "DONE");
            if(settings!=null && settings.getAddress()!=null) {
                mapsActivityIntent.putExtra("gpsLat", settings.getAddress().getGpsLat());
                mapsActivityIntent.putExtra("gpsLong", settings.getAddress().getGpsLong());
                String shopName = settings.getAddress().getName();
                if(shopName!=null && !shopName.isEmpty()) {
                    mapsActivityIntent.putExtra("markerTitle", shopName);
                }
            }
        }
        startActivityForResult(mapsActivityIntent, MapsActivity.REQUEST_CODE_GET_LOCATION);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MapsActivity.REQUEST_CODE_GET_LOCATION) {
            if (resultCode == RESULT_OK) {
                Double gpsLat = data.getExtras().getDouble("gpsLat");
                Double gpsLong = data.getExtras().getDouble("gpsLong");
                if(gpsLat!=null && gpsLong!=null) {
                    settings.getAddress().setGpsLong(gpsLong);
                    settings.getAddress().setGpsLat(gpsLat);
                    settingsModified();
                    if(setupMode) {
                        //shop location selected, now go to home delivery settings (step 3)
                        nextButtonClicked(null);
                    } else {
                        Snackbar.make(toolbar, "GPS location changed", Snackbar.LENGTH_SHORT).setAction("action", null).show();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                //ignore
                if(setupMode) {
                    currentStep = 1;
                    loadSettingsIntoUiOnResume = true;
                    buttonNext.setText("NEXT");
                }
            }
        }
    }


    @Override
    public void onSettingsSaveSuccess() {
        processingAnimation(false);
        settingsModified = false;
        if(setupMode) {
            Intent intent = new Intent(mContext, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Snackbar.make(toolbar, "Settings saved", Snackbar.LENGTH_SHORT).setAction("action", null).show();
            finish();
        }
    }

    @Override
    public void onSettingsSaveFailed() {
        processingAnimation(false);
        Snackbar.make(toolbar, "Problem while updating settings", Snackbar.LENGTH_SHORT).setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        }).show();
    }

    @Override
    public void onSettingsFetchSuccess() {
        settingsModified = false;
        settings = KoleshopUtils.getSettingsFromCache(this);
        loadSettingsIntoUi();
        settingsModified = false;
    }

    @Override
    public void onSettingsFetchFailed() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_ERROR);
        buttonBarBackNext.setVisibility(View.GONE);
    }

    @Override
    public void settingsModified() {
        settingsModified = true;
    }

    public void setupUiForKeyboardHideOnOutsideTouch(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(SellerSettingsActivity.this);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUiForKeyboardHideOnOutsideTouch(innerView);
            }
        }
    }

    public void backButtonClicked(View view) {
        onBackPressed();
    }

    public void nextButtonClicked(View view) {

        hideSoftKeyboard(this);

        if(currentStep == 1) {
            String firstStepValidity = shopSettingsFragment.validateStep1();
            if(firstStepValidity.isEmpty()) {
                //data is valid in first step
                currentStep++;
                //start the gps activity
                changeGpsLocation(null);
            } else {
                //some data is not valid in first step
                Snackbar.make(toolbar, firstStepValidity, Snackbar.LENGTH_SHORT).setAction("action", null).show();
            }
        } else if(currentStep == 2) {
            currentStep++;
            //load home delivery full screen fragment
            loadSettingsIntoUiOnResume = true;
        } else if(currentStep == 3) {
            if(!homeDeliveryOptionSelected) {
                Snackbar.make(toolbar, "Please select if home delivery is available or not", Snackbar.LENGTH_LONG).setAction("action", null).show();
            } else if(!settings.isHomeDelivery()) {
                saveSettings();
            } else {
                currentStep++;
                buttonNext.setText("FINISH");
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(deliverySettingsFragment)
                        .hide(shopSettingsFragment)
                        .hide(homeDeliveryFragment)
                        .commit();
            }

        } else if(currentStep == 4) {
            //settings setup finished
            String validityString = deliverySettingsFragment.validateData();
            if(validityString.isEmpty()) {
                saveSettings();
            } else {
                Snackbar.make(toolbar, validityString, Snackbar.LENGTH_SHORT).setAction("action", null).show();
            }
            //on settings save...go to HomeActivity
        }

        if(currentStep == 1) {
            buttonBack.setVisibility(View.GONE);
        } else {
            buttonBack.setVisibility(View.VISIBLE);
        }

        if(currentStep == 4) {
            buttonNext.setText("FINISH");
        } else {
            buttonNext.setText("NEXT");
        }
    }

    public void buttonHomeDeliverySettingsYesClicked(View view) {
        settings.setHomeDelivery(true);
        homeDeliveryFragment.yesButtonClicked();
        homeDeliveryOptionSelected = true;
        nextButtonClicked(null);
    }

    public void buttonHomeDeliverySettingsNoClicked(View view) {
        settings.setHomeDelivery(false);
        homeDeliveryFragment.noButtonClicked();
        homeDeliveryOptionSelected = true;
        buttonNext.setText("FINISH");
    }

    public static void hideSoftKeyboard(Activity activity) {
        if(activity!=null && activity.getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void reloadSettings(View view) {
        requestSettingsFromInternet();
    }
}
