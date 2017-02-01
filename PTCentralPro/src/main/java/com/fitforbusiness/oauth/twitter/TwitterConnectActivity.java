package com.fitforbusiness.oauth.twitter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.appboy.Appboy;
import com.appboy.AppboyUser;
import com.appboy.models.outgoing.TwitterUser;
import com.fitforbusiness.framework.AlertDialogManager;
import com.fitforbusiness.framework.FFBActivity;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.TuneInitialize;
import com.fitforbusiness.webservice.TrainerWebService;
import com.mobileapptracker.MobileAppTracker;

import java.util.HashMap;
import java.util.Map;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterConnectActivity extends FFBActivity {

    static String TWITTER_CONSUMER_KEY = "pphJitgtwRtQ2lxfhEqAvg"; // place your
    static String TWITTER_CONSUMER_SECRET = "KrNr8StwMpoeRj57LYUvjC8hoDSbLQ9wMQk0jEFbl4"; // place


    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";


    Button btnLoginTwitter;

    ProgressDialog pDialog;

    // Twitter
    private Twitter twitter;
    private RequestToken requestToken;

    // Shared Preferences
    private SharedPreferences mSharedPreferences;
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();
    private MobileAppTracker mobileAppTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_connect);
        mobileAppTracker = TuneInitialize.initialize(this);
        if (!Utils.isNetworkAvailable(this)) {
            // Internet Connection is not present
            alert.showAlertDialog(TwitterConnectActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }


        // Check if twitter keys are set
        if (TWITTER_CONSUMER_KEY.trim().length() == 0
                || TWITTER_CONSUMER_SECRET.trim().length() == 0) {
            // Internet Connection is not present
            alert.showAlertDialog(TwitterConnectActivity.this, "Twitter oAuth tokens",
                    "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                "MyPref", 0);
        // All UI elements
        btnLoginTwitter = (Button) findViewById(R.id.bTwitterConnect);
        if (isTwitterLoggedInAlready())
            btnLoginTwitter.setText("Connected");
        /**
         * Twitter login button click event will call loginToTwitter() function
         * */
        btnLoginTwitter.setOnClickListener(new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View arg0) {
                // Call login twitter function
                try {
                    if (android.os.Build.VERSION.SDK_INT > 9) {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                    }
                    loginToTwitter();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        // getDetails(Uri.parse(""));
    }

    @Override
    public void onResume() {
        super.onResume();
        mobileAppTracker.measureSession();
    }

    private void getDetails(Uri uri) {

        if (!isTwitterLoggedInAlready()) {

            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = uri
                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
                try {
                    // Get the access token
                    AccessToken accessToken = twitter.getOAuthAccessToken(
                            requestToken, verifier);
                    Editor e = mSharedPreferences.edit();

                    e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                    e.putString(PREF_KEY_OAUTH_SECRET,
                            accessToken.getTokenSecret());

                    e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);

                    e.commit();

                    Appboy.getInstance(getApplicationContext()).getCurrentUser().
                            setCustomUserAttribute(
                                    "Twitter Account Identifier",
                                    true
                            );

                    TwitterUser twitterUser = new TwitterUser((int) accessToken.getUserId()
                            , "", accessToken.getScreenName(), "", 0, 0, 0, "");
                    AppboyUser appboyUser = Appboy.getInstance(this).getCurrentUser();
                    appboyUser.setTwitterData(twitterUser);
                    Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
                    Log.e("Twitter Token Secrete Token", "> " + accessToken.getTokenSecret());

                    btnLoginTwitter.setText("Connected");
                } catch (Exception e) {
                    // Check log for login errors
                    Log.e("Twitter Login Error", "> " + e.getMessage());
                }
            }
        }

    }


    /**
     * Function to login twitter
     */
    private void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();
            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter
                        .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                String authUrl = requestToken.getAuthenticationURL();
                Log.d("requestToken.getAuthenticationURL()", authUrl);
                this.startActivityForResult(new Intent(this, WebViewActivity.class)
                        .putExtra(WebViewActivity.DEFAULT_URL, authUrl
                        ), 1234);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            showLogOutAlert();
        }
    }

    private void showLogOutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("PTCentral Pro");
        builder.setMessage("Disconnect from twitter?");
        builder.setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutFromTwitter();
                btnLoginTwitter.setText("Connect");
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Function to update status
     */
    class updateTwitterStatus extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            Map<String, Object> tweeterDetails = new HashMap<String, Object>();
            tweeterDetails.put("trainerId", Utils.getTrainerId(TwitterConnectActivity.this));
            tweeterDetails.put("twitter_AccessToken", args[0]);
            tweeterDetails.put("twitter_AccessSecret", args[1]);
            TrainerWebService.updateTrainerTwitterDetails(tweeterDetails, "UpdateTrainerTwitterDetailsResult");

            return null;
        }
    }

    /**
     * Function to logout from twitter It will just clear the application shared
     * preferences
     */
    private void logoutFromTwitter() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();


        btnLoginTwitter.setVisibility(View.VISIBLE);
    }


    private boolean isTwitterLoggedInAlready() {

        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {

        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            getDetails(Uri.parse(data.getStringExtra(WebViewActivity.DEFAULT_URL)));
            if (isTwitterLoggedInAlready()) {
                new updateTwitterStatus().execute(mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "")
                        , mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, ""));
            }
        }
    }
}
