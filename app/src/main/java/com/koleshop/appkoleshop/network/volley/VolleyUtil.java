package com.koleshop.appkoleshop.network.volley;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by gundeepsingh on 31/08/14.
 */
public class VolleyUtil extends Application {

    public static final String TAG = VolleyUtil.class
            .getSimpleName();
    private static VolleyUtil mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    public static synchronized VolleyUtil getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        RealmConfiguration config = new RealmConfiguration.Builder(mInstance)
                //.deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        setupPicassoDiskCache();
        Constants.SERVER_URL = getApplicationContext().getResources().getString(R.string.server_url);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public void cancelRequestsWithTag(String tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private void setupPicassoDiskCache() {
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        //built.setIndicatorsEnabled(true);
        //built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }

}
