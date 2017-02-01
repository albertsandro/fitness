package com.fitforbusiness.oauth.dwolla;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.fitforbusiness.nafc.R;

public class DwollaButton extends Button {

    private DwollaApp mDwollaApp;
    private Context mContext;
    private DwollaConnectListener dwollaConnectListener;
    private DwollaApp.CONNECT_MODE mConnectMode = DwollaApp.CONNECT_MODE.DIALOG;

    public DwollaButton(Context context) {
        super(context);
        mContext = context;
        setupButton();
    }

    public DwollaButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setupButton();
    }

    public DwollaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupButton();
    }

    private void setupButton() {

        if (mDwollaApp == null) {
            setButtonText(R.string.btnDwollaConnectText);
        }

        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));

        setClickable(true);
        setBackgroundResource(R.drawable.custom_btn_orange);
        Drawable img = getContext().getResources().getDrawable(R.drawable.dwolla_app_icon);
        img.setBounds(0, 0, dpToPx(32), dpToPx(32));
        setCompoundDrawables(img, null, null, null);
        setTextColor(getResources().getColor(R.color.white));
        setTypeface(Typeface.DEFAULT_BOLD);

        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mDwollaApp == null) {
                    Toast.makeText(mContext,
                            "DwollaApp obect needed. Call DwollaButton.setDwollaApp()",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mDwollaApp.isConnected()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            mContext);
                    builder.setMessage(
                            getResources().getString(R.string.dialogDisconnectText))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.btnDialogYes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            mDwollaApp.resetAccessToken();
                                            setButtonText(R.string.btnDwollaConnectText);
                                            mDwollaApp.getOAuthAuthenticationListener().onSuccess();
                                        }
                                    }
                            )
                            .setNegativeButton(getResources().getString(R.string.btnDialogNo),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }
                            );
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    if (mConnectMode == DwollaApp.CONNECT_MODE.DIALOG) {
                        // mDwollaApp.displayDialog();
                    } else {
                        Activity parent = (Activity) mContext;
                        Intent i = new Intent(getContext(), DwollaActivity.class);
                        i.putExtra("url", mDwollaApp.getAuthUrl());
                        i.putExtra("callbackUrl", mDwollaApp.getCallbackUrl());
                        i.putExtra("tokenUrl", mDwollaApp.getTokenUrl());
                        i.putExtra("secretKey", mDwollaApp.getSecretKey());
                        i.putExtra("clientId", mDwollaApp.getClientId());
                        parent.startActivityForResult(i, DwollaApp.DWOLLA_CONNECT_REQUEST_CODE);
                    }
                }

            }

        });

    }

    private void setButtonText(int resourceId) {
        setText(resourceId);
    }

    /**
     * @param connectMode
     */
    public void setConnectMode(DwollaApp.CONNECT_MODE connectMode) {
        mConnectMode = connectMode;
    }

    /**
     * @param dwollaApp
     */
    public void setDwollaApp(DwollaApp dwollaApp) {
        mDwollaApp = dwollaApp;
        mDwollaApp.setListener(getOAuthAuthenticationListener());

        if (mDwollaApp.isConnected()) {
            setButtonText(R.string.btnDisconnectText);
        } else {
            setButtonText(R.string.btnDwollaConnectText);
        }
    }


    /**
     * @param dwollaConnectListener
     */
    public void addDwollaConnectListener(DwollaConnectListener dwollaConnectListener) {
        dwollaConnectListener = dwollaConnectListener;
        if (mDwollaApp != null) {
            mDwollaApp.setListener(getOAuthAuthenticationListener());
        }
    }

    private DwollaApp.OAuthAuthenticationListener getOAuthAuthenticationListener() {

        return new DwollaApp.OAuthAuthenticationListener() {

            @Override
            public void onSuccess() {
                Log.d("DwollaButton", "Calling OAuthAuthenticationListener.onSuccess()");
                if (dwollaConnectListener != null) {
                    if (mDwollaApp.isConnected()) {
                        Log.d("DwollaButton", "Connected");
                        setButtonText(R.string.btnDisconnectText);
                        Log.d("DwollaButton", "Calling dwollaConnectListener.onConnected()");
                        dwollaConnectListener.onConnected();
                    } else {
                        Log.d("DwollaButton", "Disconnected");
                        Log.d("DwollaButton", "Calling dwollaConnectListener.onDisconnected()");
                        setButtonText(R.string.btnDwollaConnectText);
                        dwollaConnectListener.onDisconnected();
                    }
                } else {
                    Log.d("DwollaButton", "dwollaConnectListener is null");
                }
            }

            @Override
            public void onFail(String error) {
                Log.i("DwollaButton", "Calling OAuthAuthenticationListener.onFail()");
                if (dwollaConnectListener != null) {
                    dwollaConnectListener.onError(error);
                }
            }
        };
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

}
