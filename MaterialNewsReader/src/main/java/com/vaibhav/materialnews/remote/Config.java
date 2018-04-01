package com.vaibhav.materialnews.remote;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
    public static final URL BASE_URL;
    private static final String BASE_URL_STRING = "https://www.newsapi.org/dailynews";
    private static String TAG = Config.class.toString();

    static {
        URL url = null;
        try {
            url = new URL(BASE_URL_STRING );
        } catch (MalformedURLException ignored) {
            // TODO: throw a real error
            Log.e(TAG, "Please check your internet connection.");
        }

        BASE_URL = url;
    }
}
