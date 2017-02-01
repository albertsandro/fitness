package com.fitforbusiness.oauth.dwolla;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sanjeet on 03-Sep-14.
 */
public class DwollaApp {
    public static final int DWOLLA_CONNECT_REQUEST_CODE = 1;
    public static final int RESULT_CONNECTED = 1;
    public static final int RESULT_ERROR = 2;
    private static final String AUTH_URL = "https://www.dwolla.com/oauth/v2/authenticate?";
    private static final String TOKEN_URL = "https://www.dwolla.com/oauth/v2/token";
    private static final String SCOPE = "AccountInfoFull%7CSend%7CTransactions%7CBalance%7CRequest";
    /*AccountInfoFull Send
*/
    private static final String TAG = "DwollaApp";
    private static int SUCCESS = 0;
    private static int ERROR = 1;
    private static int PHASE1 = 1;
    private static int PHASE2 = 2;
    private final DwollaDialog mDialog;
    private DwollaSession mSession;
    private OAuthAuthenticationListener mListener;
    private ProgressDialog mProgress;
    private String mCallbackUrl;
    private String mAuthUrl;
    private String mSecretKey;
    private String mAccountName;
    private String mClientId;
    private String clientId;

    public DwollaApp(Context context, String accountName, String clientId, String clientKey, String callbackUrl, String scope) {
        mSession = new DwollaSession(context, accountName);
        mAccountName = accountName;
        mSecretKey = clientKey;
        mCallbackUrl = callbackUrl;
        mClientId = clientId;
        /*https://www.dwolla.com/oauth/v2/authenticate?
        client_id=abcd&
        response_type=code&
        redirect_uri=https%3A%2F%2Fwww.google.com%2Fredirect&
        scope=send%7Ctransactions%7CAccountInfoFull%7CBalance%7CRequest*/

        mAuthUrl = AUTH_URL + "client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + mCallbackUrl
                + "&scope=" + ((scope == null) ? SCOPE : scope)

        ;

        DwollaDialog.OAuthDialogListener listener = new DwollaDialog.OAuthDialogListener() {
            @Override
            public void onComplete(Map<String, String> parameters) {
                getAccessToken(parameters.get("code"));
            }

            @Override
            public void onError(Map<String, String> parameter) {
                mListener.onFail("Authorization failed");
            }
        };

        mDialog = new DwollaDialog(context, mAuthUrl, mCallbackUrl, listener);
        mProgress = new ProgressDialog(context);
        mProgress.setCancelable(false);
    }

    public DwollaApp(Context context, String accountName, String clientId, String clientKey, String callbackUrl) {
        this(context, accountName, clientId, clientKey, callbackUrl, null);
    }

    private void getAccessToken(final String code) {
        mProgress.setMessage("Connecting with Dwolla");
        mProgress.show();

        new Thread() {
            @Override
            public void run() {
                int what = SUCCESS;

                try {


                    URL url = new URL(TOKEN_URL);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("code", code));
                    params.add(new BasicNameValuePair("client_secret", mSecretKey));
                    params.add(new BasicNameValuePair("client_id", mClientId));
                    params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                    // params.add(new BasicNameValuePair("redirect_uri", mCallBackUrl));

                    String urlParameters = "code=" + code
                            + "&redirect_uri=" + mCallbackUrl
                            + "&client_id=" + mClientId
                            + "&client_secret=" + mSecretKey
                            + "&grant_type=authorization_code";
                    AppLog.i(TAG, "getAccessToken", "Getting access token with code:" + code);
                    AppLog.i(TAG, "getAccessToken", "Opening URL " + url.toString() + "?" + params);
                    String response = DwollaUtils.executePost(TOKEN_URL, params);
                    JSONObject obj = new JSONObject(response);
                    AppLog.i(TAG, "JSON is", "JSON::			" + obj.toString());
                    AppLog.i(TAG, "getAccessToken", "String data[access_token]:			" + obj.getString("access_token"));
                   /* AppLog.i(TAG, "getAccessToken", "String data[livemode]:				" + obj.getBoolean("livemode"));
                    AppLog.i(TAG, "getAccessToken", "String data[refresh_token]:			" + obj.getString("refresh_token"));
                    AppLog.i(TAG, "getAccessToken", "String data[token_type]:			" + obj.getString("token_type"));
                    AppLog.i(TAG, "getAccessToken", "String data[stripe_publishable_key]: " + obj.getString("stripe_publishable_key"));
                    AppLog.i(TAG, "getAccessToken", "String data[stripe_user_id]:		" + obj.getString("stripe_user_id"));
                    AppLog.i(TAG, "getAccessToken", "String data[scope]:					" + obj.getString("scope"));*/

                    mSession.storeAccessToken(obj.getString("access_token"));
                   /* mSession.storeRefreshToken(obj.getString("refresh_token"));
                    mSession.storePublishableKey(obj.getString("stripe_publishable_key"));
                    mSession.storeUserId(obj.getString("stripe_user_id"));
                    mSession.storeLiveMode(obj.getBoolean("livemode"));
                    mSession.storeTokenType(obj.getString("token_type"));*/

                } catch (Exception ex) {
                    what = ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, PHASE2, 0));
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == PHASE1) {
                if (msg.what == SUCCESS) {
                    getAccountData();
                } else {
                    mProgress.dismiss();
                    mListener.onFail("Failed to get access token");
                }
            } else {
                AppLog.i(TAG, "mHandler.handleMessage", "Calling mListener.onSuccess()");
                mProgress.dismiss();
                mListener.onSuccess();
            }
        }
    };

    private void getAccountData() {
        mProgress.setMessage("Finalizing ...");

        new Thread() {
            @Override
            public void run() {
                AppLog.i(TAG, "getAccountData", "Fetching user info");
                int what = SUCCESS;

                try {

                    /*Stripe.apiKey = mSession.getAccessToken();
                    Account account = Account.retrieve();

                    if (account != null) {

                    }*/

                } catch (Exception ex) {
                    what = ERROR;
                    ex.printStackTrace();
                }

                mHandler.sendMessage(mHandler.obtainMessage(what, PHASE2, 0));
            }
        }.start();
    }

    public void resetAccessToken() {
        if (isConnected()) {
            mSession.resetAccessToken();
            mListener.onSuccess();
        }
    }

    public void setListener(OAuthAuthenticationListener listener) {
        mListener = listener;
    }

    public OAuthAuthenticationListener getOAuthAuthenticationListener() {
        return mListener;
    }

    public boolean isConnected() {
        return getAccessToken() != null;
    }

    public String getAccessToken() {
        return mSession.getAccessToken();
    }

    public DwollaSession getStripeSession() {
        return mSession;
    }

    public String getAccountName() {
        return mAccountName;
    }

    protected String getAuthUrl() {
        return mAuthUrl;
    }

    protected String getCallbackUrl() {
        return mCallbackUrl;
    }

    protected String getTokenUrl() {
        return TOKEN_URL;
    }

    protected String getSecretKey() {
        return mSecretKey;
    }

    protected String getUserId() {
        return mSession.getUserId();
    }

    public String getClientId() {
        return mClientId;
    }

    public static enum CONNECT_MODE {DIALOG, ACTIVITY}

    public interface OAuthAuthenticationListener {
        public abstract void onSuccess();

        public abstract void onFail(String error);
    }
}
