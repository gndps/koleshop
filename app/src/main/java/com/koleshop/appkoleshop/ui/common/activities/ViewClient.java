package com.koleshop.appkoleshop.ui.common.activities;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Ankit on 3/17/2016.
 */
public class ViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView v, String url)
    {
        v.loadUrl(url);
        return true;
    }
}
