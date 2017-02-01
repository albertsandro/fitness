package com.fitforbusiness.oauth.dwolla;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebIconDatabase;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DwollaActivity extends ActionBarActivity {

    private static final String TAG = "DwollaActivity";

    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private String mUrl;
    private String mCallBackUrl;
    private String mTokenUrl;
    private String mSecretKey;
    private String mAccountName;
    private String mClientId;

    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUrl = getIntent().getStringExtra("url");
        mCallBackUrl = getIntent().getStringExtra("callbackUrl");
        mTokenUrl = getIntent().getStringExtra("tokenUrl");
        mSecretKey = getIntent().getStringExtra("secretKey");
        mClientId = getIntent().getStringExtra("clientId");
        // mAccountName = getIntent().getStringExtra("accountName");

        setUpWebView();

    }

    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    private void setUpWebView() {

        mSpinner = new ProgressDialog(this);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");

        mWebView = new WebView(this);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new OAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
        mContent = new LinearLayout(this);
        mContent.setOrientation(LinearLayout.VERTICAL);
        mContent.addView(mWebView);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.FROYO) {
            addContentView(mContent, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        } else {
            addContentView(mContent, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT));
        }

        DwollaUtils.removeAllCookies(this);

    }

    private class OAuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            URL tempUrl = null;
            try {
                tempUrl = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (tempUrl != null) {
                url = tempUrl.toString();
            }
            AppLog.d(TAG, "OAuthWebViewClient.shouldOverrideUrlLoading", "Redirecting URL " + url);
            if (url.startsWith(mCallBackUrl)) {

                String queryString = url.replace(mCallBackUrl + "/?", "");
                AppLog.d(TAG, "OAuthWebViewClient.shouldOverrideUrlLoading", "queryString:" + queryString);
                Map<String, String> parameters = DwollaUtils.splitQuery(queryString);
                if (!url.contains("error")) {
                    onComplete(parameters);
                } else {
                    onError(parameters);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            AppLog.e(TAG, "OAuthWebViewClient.onReceivedError", "Page error[errorCode=" + errorCode + "]: " + description);

            super.onReceivedError(view, errorCode, description, failingUrl);
            Map<String, String> error = new LinkedHashMap<String, String>();
            error.put("error", String.valueOf(errorCode));
            error.put("error_description", description);
            onError(error);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            getSupportActionBar().setIcon((new BitmapDrawable(getResources(), favicon)));
            AppLog.d(TAG, "OAuthWebViewClient.onPageStarted", "url: " + url);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            AppLog.d(TAG, "OAuthWebViewClient.onPageFinished", "url: " + url);
            getSupportActionBar().setTitle(view.getTitle());

            mSpinner.dismiss();
        }

    }

    private void getAccessToken(String code) {
        try {
            code = code.replace("=", "");
            URL url = new URL(mTokenUrl);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("client_secret", mSecretKey));
            params.add(new BasicNameValuePair("client_id", mClientId));
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("redirect_uri", mCallBackUrl));

/*
            String urlParameters = "code=" + code
                    + "&client_secret=" + mSecretKey
                    + "&client_id=" + mClientId
                    + "&grant_type=authorization_code"
                    + "&redirect_uri=" + mCallBackUrl;*/
            AppLog.i(TAG, "getAccessToken", "Getting access token with code:" + code);
            AppLog.i(TAG, "getAccessToken", "Opening URL " + url.toString() + "?" + params.toString());

            String response = DwollaUtils.executePost(mTokenUrl, params);
            AppLog.i(TAG, "getAccessToken", "response: " + response);
            JSONObject obj = new JSONObject(response);

            AppLog.i(TAG, "JSON", "String data[JSON]:			" + obj.toString());
           /* AppLog.i(TAG, "getAccessToken", "String data[access_token]:			" + obj.getString("access_token"));
            AppLog.i(TAG, "getAccessToken", "String data[livemode]:				" + obj.getBoolean("livemode"));
            AppLog.i(TAG, "getAccessToken", "String data[refresh_token]:			" + obj.getString("refresh_token"));
            AppLog.i(TAG, "getAccessToken", "String data[token_type]:			" + obj.getString("token_type"));
            AppLog.i(TAG, "getAccessToken", "String data[stripe_publishable_key]: " + obj.getString("stripe_publishable_key"));
            AppLog.i(TAG, "getAccessToken", "String data[stripe_user_id]:		" + obj.getString("stripe_user_id"));
            AppLog.i(TAG, "getAccessToken", "String data[scope]:					" + obj.getString("scope"));*/

            DwollaSession mSession = new DwollaSession(this, mAccountName);
            if (obj.toString().contains("access_token")) {
                mSession.storeAccessToken(obj.getString("access_token"));
            }

         /*   mSession.storePublishableKey(obj.getString("stripe_publishable_key"));
            mSession.storeUserId(obj.getString("stripe_user_id"));
            mSession.storeLiveMode(obj.getBoolean("livemode"));
            mSession.storeTokenType(obj.getString("token_type"))*/
            ;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> query_pairs = new LinkedHashMap<String, String>();
            query_pairs.put("error", "UnsupportedEncodingException");
            query_pairs.put("error_description", e.getMessage());
            onError(query_pairs);
        }
    }

    private void onComplete(Map<String, String> parameters) {

        final String code = parameters.get("code");


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                getAccessToken(code);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent returnIntent = new Intent();
                setResult(DwollaApp.RESULT_CONNECTED, returnIntent);
                finish();
            }
        }.execute();

    }

    private void onError(Map<String, String> parameters) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("error", parameters.get("error"));
        returnIntent.putExtra("error_description", parameters.get("error_description"));
        setResult(DwollaApp.RESULT_ERROR, returnIntent);
        finish();
    }


}
