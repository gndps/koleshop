package com.koleshop.koleshopbackend.common;

public class Constants {

	public static String GCM_API_KEY_OLD = "AIzaSyC-8kfTUQ47_KlN6MRvoC4Sigdc0kSY3D0";
	public static String GCM_API_KEY = "AIzaSyDA0RGdxcUDTwmEnUa8RioynJVgJCgX914";

	public static int USE_GATEWAY_NUMBER = 3;

	public static String SMS_GATEWAY_URL = "http://alerts.sinfini.com/api/web2sms.php?workingkey=A09ab6ac4f4a123f3b2b887e877f7e631&sender=SIDEMO";
	public static String SMS_GATEWAY_URL_2 = "https://control.msg91.com/api/sendhttp.php?authkey=93914AeekgtISLEs4560ba93c&sender=GNDPSS&route=4&country=91";
	public static String SMS_GATEWAY_URL_3 = "http://api.textlocal.in/send/?hash=d5e932e2bc681c6e9f26152a08c814f02ab8e70d&user=gndpsingh@gmail.com&sender=TXTLCL";

	//public static String ANDROID_CLIENT_ID = "210363682882-3kf1gq0536du05c7o4gi0itssaun7mp1.apps.googleusercontent.com";

	//String web_secret_key = "nIBgWk7djMkenJ8lVh0N6LnK";

	public static int COUNTRY_CODE = 91;

	//public static String SERVER_DOMAIN_NAME = "koleshop.com";
	//public static String SERVER_OWNER_NAME = "koleshopserver";

	public static boolean USE_LOCAL_DATABASE = true;

	//Excluded Categories : Home & Kitchen Ware, Appliances, Frozen Foods, Fruits & Veg, Popular
	public static String EXCLUDED_INVENTORY_CATEGORIES_IDS = "'9','8','13','17','138'";

	public static String OTP_TIME_TO_LIVE = "5"; // 5 minutes

    public static int USER_SESSION_TYPE_SELLER = 1; //these constants are same in the mysql db
    public static int USER_SESSION_TYPE_BUYER = 2;

	public static String GCM_NOTI_USER_INVENTORY_CREATED = "gcm_noti_user_inventory_created";
    public static String GCM_NOTI_COLLAPSE_KEY_INVENTORY_CREATED = "gcm_coll_inventory_created";
	public static String GCM_NOTI_DELETE_OLD_SETTINGS_CACHE = "gcm_noti_delete_old_settings_cache";
	public static String GCM_NOTI_COLLAPSE_KEY_DELETE_OLD_SETTINGS_CACHE = "gcm_coll_delete_old_settings_cache";

	public static String GCM_DEMO_MESSAGE_COLLAPSE_KEY = "demo_coll_key";
	public static String GCM_DEMO_MESSAGE = "demo_key";

	//server client response statuses other than success and failure
    public static String STATUS_KOL_RESPONSE_CREATING_INVENTORY = "status_kol_response_creating_inventory";

	public static String ADDRESS_TYPE_CUSTOMER = "0";
	public static String ADDRESS_TYPE_SELLER = "1";
	public static String SELLER_ADDRESS_NICKNAME = "Seller Address";
	public static final String NEARBY_SHOPS_DISTANCE = "10000";
	public static String PUBLIC_PRODUCT_IMAGE_URL_PREFIX = "https://storage.googleapis.com/koleshop-bucket/uploads/";
	public static String PUBLIC_PROFILE_IMAGE_URL_PREFIX = "https://storage.googleapis.com/koleshop-bucket/profile/";

}
