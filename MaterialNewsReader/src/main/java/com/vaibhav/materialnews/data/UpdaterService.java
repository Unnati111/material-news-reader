package com.vaibhav.materialnews.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import com.vaibhav.materialnews.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.vaibhav.materialnews.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.vaibhav.materialnews.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            JSONObject response = RemoteEndpointUtil.fetchJson().getJSONObject("response");
            JSONArray array = response.getJSONArray("results");
            if (array == null) {
                throw new JSONException("Invalid parsed item array" );
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                if(object.getJSONObject("fields").has("body") &&  object.getJSONObject("fields").has("thumbnail")) {
                    values.put(ItemsContract.Items.SERVER_ID, object.getString("id" ));
                    values.put(ItemsContract.Items.AUTHOR, "");
                    values.put(ItemsContract.Items.TITLE, object.getString("webTitle" ));
                    values.put(ItemsContract.Items.BODY, object.getJSONObject("fields").getString("body" ));
                    values.put(ItemsContract.Items.THUMB_URL, object.getJSONObject("fields").getString("thumbnail" ));
                    values.put(ItemsContract.Items.PHOTO_URL, object.getJSONObject("fields").getString("thumbnail" ));
//                    values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio" ));
                    values.put(ItemsContract.Items.PUBLISHED_DATE, object.getString("webPublicationDate"));
                    cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
                }
            }

            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);

        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating content.", e);
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }
}
