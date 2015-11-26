package com.kolshop.kolshopmaterial.common.constant;

/**
 * Created by gundeepsingh on 17/08/14.
 */
public class Constants {

    //base url for server requests
    public static String BASE_URL = "http://192.168.1.4:8080/";

    //device type = 0 for Android Devices
    public static int DEVICE_TYPE = 0;

    //google project id
    public static String GOOGLE_PROJECT_ID = "210363682882";

    public static String SHOP_SETTINGS_PREFIX = "shop_settings_";

    public static String BUYER_SETTINGS_PREFIX = "buyer_settings_";

    public static String SESSION_TYPE_SELLER = "1";
    public static String SESSION_TYPE_BUYER = "2";

    public static String SERVER_URL = "https://kol-server.appspot.com/_ah/api/"; //production url
    //public static String SERVER_URL = "10.0.3.2"; //local url

    // The authority for the sync adapter's content provider
    public static String AUTHORITY = "com.kolshop.kolshop.provider";
    // An account type, in the form of a domain name
    public static String ACCOUNT_TYPE = "com.kolshop.kolshop";
    // The account name
    public static String ACCOUNT = "dummyaccount_kolshop";
    // Instance fields
    public static String GOOGLE_USER_TOKEN = "google_user_token";

    //configurable constants
    public static boolean RESET_REALM = false;


    //Broadcast Actions
    public static final String ACTION_PRODUCT_CATEGORIES_LOAD_SUCCESS = "action_product_categories_load_success";
    public static final String ACTION_PRODUCT_CATEGORIES_LOAD_FAILED = "action_product_categories_load_failed";
    public static final String ACTION_MEASURING_UNITS_LOAD_SUCCESS = "action_measuring_units_load_success";
    public static final String ACTION_MEASURING_UNITS_LOAD_FAILED = "action_measuring_units_load_failed";
    public static final String ACTION_ADD_VARIETY = "action_add_variety";
    public static final String ACTION_DELETE_VARIETY = "action_delete_variety";
    public static final String ACTION_PROPERTY_MODIFIED = "action_property_modified";
    public static final String ACTION_REQUEST_OTP = "action_request_otp";
    public static final String ACTION_REQUEST_OTP_SUCCESS = "action_request_otp_success";
    public static final String ACTION_REQUEST_OTP_FAILED = "action_request_otp_fail";
    public static final String ACTION_VERIFY_OTP = "action_verify_otp";
    public static final String ACTION_VERIFY_OTP_SUCCESS = "action_verify_otp_success";
    public static final String ACTION_VERIFY_OTP_FAILED = "action_verify_otp_fail";
    public static final String ACTION_OTP_RECEIVED = "action_otp_received";
    public static final String ACTION_SAVE_PRODUCT = "action_save_product";
    public static final String ACTION_UPDATE_PRODUCT_VARIETY_UI = "action_update_product_variety_ui" ;
    public static final String ACTION_FETCH_INVENTORY_CATEGORIES = "action_fetch_inventory_categories" ;
    public static final String ACTION_FETCH_INVENTORY_CATEGORIES_SUCCESS = "action_fetch_inventory_categories_success" ;
    public static final String ACTION_FETCH_INVENTORY_CATEGORIES_FAILED = "action_fetch_inventory_categories_failed" ;
    public static final String ACTION_FETCH_INVENTORY_SUBCATEGORIES = "action_fetch_inventory_subcategories" ;
    public static final String ACTION_FETCH_INVENTORY_SUBCATEGORIES_SUCCESS = "action_fetch_inventory_subcategories_success" ;
    public static final String ACTION_FETCH_INVENTORY_SUBCATEGORIES_FAILED = "action_fetch_inventory_subcategories_failed" ;
    public static final String ACTION_FETCH_INVENTORY_PRODUCTS = "action_fetch_inventory_products" ;
    public static final String ACTION_FETCH_INVENTORY_PRODUCTS_SUCCESS = "action_fetch_inventory_products_success" ;
    public static final String ACTION_FETCH_INVENTORY_PRODUCTS_FAILED = "action_fetch_inventory_products_failed" ;
    public static final String ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION = "action_update_inventory_product_selection" ;
    public static final String ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_SUCCESS = "action_update_inventory_product_selection_success" ;
    public static final String ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION_FAILURE = "action_update_inventory_product_selection_failure" ;
    public static final String ACTION_NOTIFY_PRODUCT_SELECTION_VARIETY_TO_PARENT = "action_notify_product_selection_variety_to_parent" ;
    public static final String ACTION_COLLAPSE_EXPANDED_PRODUCT = "action_collapse_expanded_product" ;
    //gcm broadcast actions
    public static final String ACTION_GCM_BROADCAST_INVENTORY_CREATED = "action_gcm_broadcast_inventory_created";

    //Shared preferences keys
    public static final String KEY_REG_ID = "registration_id";
    public static final String KEY_APP_VERSION = "appVersion";
    public static final String KEY_USER_SESSION_TYPE = "pref_user_type";
    public static final String KEY_USER_PHONE_NUMBER = "pref_user_phone";
    public static final String KEY_SKIP_ALLOWED = "pref_skip_allowed";
    public static final String KEY_USER_ID = "pref_user_id";
    public static final String KEY_SESSION_ID = "pref_session_id";

    //Preferences Flags
    public static final String FLAG_PRODUCT_CATEGORIES_LOADED = "product_categories_loaded";
    public static final String FLAG_MEASURING_UNITS_LOADED = "measuring_units_loaded";
    public static final String FLAG_BRANDS_LOADED = "brands_loaded";

    public static final String PRICE_PROPERTY_NAME = "price";

    public static final int APP_CACHE_VERSION = 1;
    public static final String CACHE_ID = "cache_id_string";
    public static final String CACHE_ID_DATE = "cache_id_date";
    public static final int RAM_CACHE_SIZE = 5 * 1024 * 1024; // 5 mb
    public static final int DISK_CACHE_SIZE = 15 * 1024 * 1024; //15 mb
    public static final int RAM_CACHE_SIZE_DATE = 1 * 1024 * 1024; // 5 mb
    public static final int DISK_CACHE_SIZE_DATE = 3 * 1024 * 1024; //15 mb


    //DualCache keys and expiration time
    //global inventory cache constants
    public static final String CACHE_INVENTORY_CATEGORIES = "cache_inventory_categories";
    public static final int TIME_TO_LIVE_INV_CAT = 5; //cache_inventory_categories will expire in 5 minutes
    public static final String CACHE_INVENTORY_SUBCATEGORIES = "cache_inventory_subcategories_"; //this key will be extended by the parent category id
    public static final int TIME_TO_LIVE_INV_SUBCAT = 5; //cache_inventory_subcategories_x will expire in 5 minutes
    public static final String CACHE_INVENTORY_PRODUCTS = "cache_inventory_products_"; //this key will be extended by the category id
    public static final int TIME_TO_LIVE_INV_PRODUCT = 7; //cache_inventory_subcategories_x will expire in 5 minutes
    //my inventory cache constants
    public static final String CACHE_MY_INVENTORY_CATEGORIES = "cache_my_inventory_categories";
    public static final int TIME_TO_LIVE_MY_INV_CAT = 5; //cache_my_inventory_categories will expire in 5 minutes
    public static final String CACHE_MY_INVENTORY_SUBCATEGORIES = "cache_my_inventory_subcategories_"; //this key will be extended by the parent category id
    public static final int TIME_TO_LIVE_MY_INV_SUBCAT = 5; //cache_my_inventory_subcategories_x will expire in 5 minutes
    public static final String CACHE_MY_INVENTORY_PRODUCTS = "cache_my_inventory_products_"; //this key will be extended by the category id
    public static final int TIME_TO_LIVE_MY_INV_PRODUCT = 7; //cache_my_inventory_subcategories_x will expire in 5 minutes

    //server client response statuses other than success and failure
    public static final String STATUS_KOLE_RESPONSE_CREATING_INVENTORY = "status_kole_response_creating_inventory";

    //Symbols
    public static final String INDIAN_RUPEE_SYMBOL = "\u20B9";

}
