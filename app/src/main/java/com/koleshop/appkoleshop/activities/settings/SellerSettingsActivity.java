package com.koleshop.appkoleshop.activities.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.common.util.CommonUtils;
import com.koleshop.appkoleshop.common.util.PreferenceUtils;
import com.koleshop.appkoleshop.fragments.settings.DeliverySettingsFragment;
import com.koleshop.appkoleshop.fragments.settings.ShopSettingsFragment;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.services.SettingsIntentService;

import org.parceler.Parcels;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SellerSettingsActivity extends AppCompatActivity implements ShopSettingsFragment.OnFragmentInteractionListener,
        DeliverySettingsFragment.OnFragmentInteractionListener,SettingsIntentService.SettingsReceiver,OnMapReadyCallback {

    private static int VIEW_FLIPPER_CHILD_LOADING = 0;
    private static int VIEW_FLIPPER_CHILD_ERROR = 1;
    private static int VIEW_FLIPPER_CHILD_SETTINGS = 2;

    @BindString(R.string.navigation_drawer_settings)
    String titleSellerSettings;
    @Bind(R.id.switch_home_delivery_toggle)
    Switch switchHomeDeliveryToggle;
    @Bind(R.id.ib_delivery_settings)
    ImageButton imageButtonDeliverySettings;
    @Bind(R.id.vf_seller_settings)
    ViewFlipper viewFlipper;

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
        } else {
            //if settings available in cache, then load from cache, else load from internet
            if(extras!=null) {
                setupMode = extras.getBoolean("setupMode");
            }
            settings = getSettingsFromCache();
        }

        if(settings!=null) {
            loadSettingsIntoUi();
        } else {
            requestSettingsFromInternet();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_seller_settings, menu);
        saveMenuButton = menu.findItem(R.id.action_save_settings);
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
                showingDeliverySettings = false;
            } else if(settingsModified) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                builder.setMessage("Save the changed settings?")
                        .setPositiveButton(R.string.save_changes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (validateSettingsBeforeSaving()) {
                                    saveSettings();
                                }
                            }
                        })
                        .setNegativeButton(R.string.dont_save_changes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SellerSettingsActivity.super.onBackPressed();
                            }
                        })
                        .setNeutralButton(R.string.save_cancel, null);
                builder.create().show();
            } else {
                super.onBackPressed();
            }
        } else {

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
        showingDeliverySettings = true;
    }

    private SellerSettings getSettingsFromCache() {
        String settingsString = PreferenceUtils.getPreferences(mContext, Constants.KEY_SELLER_SETTINGS);
        if(settingsString!=null && !settingsString.isEmpty()) {
            //load settings from cache
            SellerSettings sellerSettings = new Gson().fromJson(settingsString, SellerSettings.class);
            return sellerSettings;
        } else {
            return null;
        }
    }

    private void requestSettingsFromInternet() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_LOADING);
        requestId = CommonUtils.randomString(6);
        SettingsIntentService.requestSellerSettings(this, requestId);
    }

    private void loadSettingsIntoUi() {
        if(settings==null) {
            settings = new SellerSettings();
            settings.setAddress(new Address());
        }

        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_SETTINGS);

        //shop settings fragment
        shopSettingsFragment.setSellerSettings(settings, setupMode);
        switchHomeDeliveryToggle.setChecked(settings.isHomeDelivery());

        //delivery settings fragment
        deliverySettingsFragment.setSellerSettings(settings);

    }

    private boolean validateSettingsBeforeSaving() {
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
            processing.setMessage("Saving Product...");
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
        if(settings.getShopOpenTime()!=null) {
            openTime.setTime(settings.getShopOpenTime());
            hour = openTime.get(Calendar.HOUR_OF_DAY);
            minute = openTime.get(Calendar.MINUTE);
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setShopOpenTime(CommonUtils.fixTheTimeForSettings(selectedHour, selectedMinute));
                timeChanged();
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Shop Open Time");
        mTimePicker.show();
    }

    public void editShopCloseTime(View view) {
        Calendar closeTime = Calendar.getInstance();
        int hour,minute;
        if(settings.getShopCloseTime()!=null) {
            closeTime.setTime(settings.getShopCloseTime());
            hour = closeTime.get(Calendar.HOUR_OF_DAY);
            minute = closeTime.get(Calendar.MINUTE);
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setShopCloseTime(CommonUtils.fixTheTimeForSettings(selectedHour, selectedMinute));
                timeChanged();
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Shop Close Time");
        mTimePicker.show();
    }

    public void editDeliveryStartTime(View view) {
        Calendar deliveryStartTime = Calendar.getInstance();
        int hour,minute;
        if(settings.getDeliveryStartTime()!=null) {
            deliveryStartTime.setTime(settings.getDeliveryStartTime());
            hour = deliveryStartTime.get(Calendar.HOUR_OF_DAY);
            minute = deliveryStartTime.get(Calendar.MINUTE);
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setDeliveryStartTime(CommonUtils.fixTheTimeForSettings(selectedHour, selectedMinute));
                timeChanged();
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.setTitle("Deliver Start Time");
        mTimePicker.show();
    }

    public void editDeliveryEndTime(View view) {
        Calendar deliveryEndTime = Calendar.getInstance();
        int hour,minute;
        if(settings.getDeliveryEndTime()!=null) {
            deliveryEndTime.setTime(settings.getDeliveryEndTime());
            hour = deliveryEndTime.get(Calendar.HOUR_OF_DAY);
            minute = deliveryEndTime.get(Calendar.MINUTE);
        } else {
            hour = 0;
            minute = 0;
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(SellerSettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                settings.setDeliveryEndTime(CommonUtils.fixTheTimeForSettings(selectedHour, selectedMinute));
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
        }
        mapsActivityIntent.putExtra("title", "Shop Location");
        mapsActivityIntent.putExtra("actionButtonTitle", "DONE");
        if(settings!=null && settings.getAddress()!=null) {
            mapsActivityIntent.putExtra("gpsLat", settings.getAddress().getGpsLat());
            mapsActivityIntent.putExtra("gpsLong", settings.getAddress().getGpsLong());
            String shopName = settings.getAddress().getName();
            if(shopName!=null && !shopName.isEmpty()) {
                mapsActivityIntent.putExtra("markerTitle", shopName);
            }
        }
        startActivityForResult(mapsActivityIntent, MapsActivity.REQUEST_CODE_GET_LOCATION);

    }


    @Override
    public void onSettingsSaveSuccess() {
        processingAnimation(false);
        settingsModified = false;
        Snackbar.make(toolbar, "Settings saved", Snackbar.LENGTH_SHORT).setAction("action", null).show();
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
        settings = getSettingsFromCache();
        loadSettingsIntoUi();
    }

    @Override
    public void onSettingsFetchFailed() {
        viewFlipper.setDisplayedChild(VIEW_FLIPPER_CHILD_ERROR);
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void reloadSettings(View view) {
        requestSettingsFromInternet();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }
}
