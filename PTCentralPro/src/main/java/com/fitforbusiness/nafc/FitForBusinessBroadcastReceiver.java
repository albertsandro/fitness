package com.fitforbusiness.nafc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.appboy.Constants;
import com.fitforbusiness.nafc.session.AddSessionActivity;

public class FitForBusinessBroadcastReceiver extends BroadcastReceiver {
    public static final String SOURCE_KEY = "source";
    public static final String DESTINATION_VIEW = "destination";
    public static final String HOME = "home";
    public static final String FEED = "feed";
    public static final String FEEDBACK = "feedback";
    private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX,
            FitForBusinessBroadcastReceiver.class.getName());

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, String.format("onReceived called %s", intent.getAction()));
        String packageName = context.getPackageName();
        String pushReceivedAction = packageName + ".intent.APPBOY_PUSH_RECEIVED";
        String notificationOpenedAction = packageName + ".intent.APPBOY_NOTIFICATION_OPENED";
        String action = intent.getAction();
        Log.d(TAG, String.format("Received intent with action %s", action));

        if (pushReceivedAction.equals(action)) {

            Log.d(TAG, "Received push notification.");
        } else if (notificationOpenedAction.equals(action)) {
            String stringValue = intent.getExtras().getBundle("extra").getString("deeplink");
            if (stringValue != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(stringValue));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);

            }

        } else {
            Log.d(TAG, String.format("Ignoring intent with unsupported action %s", action));
        }
    }

    private void startDroidBoyWithIntent(Context context, Bundle extras) {
        Intent startActivityIntent = new Intent(context, AddSessionActivity.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityIntent.putExtra(SOURCE_KEY, Constants.APPBOY);
        if (extras != null) {
            startActivityIntent.putExtras(extras);
        }
        context.startActivity(startActivityIntent);
    }
}
