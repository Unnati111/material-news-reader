package com.vaibhav.materialnews.remote;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class RemoteEndpointUtil {
    private static final String TAG = "RemoteEndpointUtil";
    private static String BASE_URL_STRING = "https://content.guardianapis.com/search?";

    private RemoteEndpointUtil() {
    }

    public static JSONObject fetchJson() {
        String itemsJson = null;
        try {
            itemsJson = fetchPlainText(createUrl());
        } catch (IOException e) {
            Log.e(TAG, "Error fetching items JSON", e);
            return null;
        }

        // Parse JSON
        try {
            JSONTokener tokener = new JSONTokener(itemsJson);
            Object val = tokener.nextValue();
            if (!(val instanceof JSONObject)) {
                throw new JSONException("Expected JSONArray");
            }
            return (JSONObject) val;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing items JSON", e);
        }

        return null;
    }

    static String fetchPlainText(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static URL createUrl(){

        URL url = null;

        Uri builtUri = Uri.parse(BASE_URL_STRING)
                .buildUpon()
                .appendQueryParameter("page-size", "20")
                .appendQueryParameter("api-key","test")
                .build();

        try{
            url = new URL(builtUri.toString() + "&show-fields=thumbnail,body");
        }catch (MalformedURLException e){
            Timber.e("problem building url from Uri");
        }

        Timber.i("This is the built url: " + url);
        return url;
    }
}
