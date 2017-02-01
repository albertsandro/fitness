package com.fitforbusiness.nafc.payment.stripe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.appboy.Appboy;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.oauth.stripe.StripeApp;
import com.fitforbusiness.oauth.stripe.StripeButton;
import com.fitforbusiness.oauth.stripe.StripeConnectListener;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.TuneInitialize;
import com.fitforbusiness.stripe.compat.AsyncTask;
import com.fitforbusiness.webservice.TrainerWebService;
import com.mobileapptracker.MobileAppTracker;
import com.stripe.Stripe;

import java.util.HashMap;
import java.util.Map;

public class StripOAuthActivity extends ActionBarActivity {

    private StripeApp mApp;
    private TextView tvSummary;
    private MobileAppTracker mobileAppTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        mobileAppTracker = TuneInitialize.initialize(this);
        mApp = new StripeApp(this, "StripeAccount", ApplicationData.CLIENT_ID,
                ApplicationData.SECRET_KEY, ApplicationData.CALLBACK_URL);

        tvSummary = (TextView) findViewById(R.id.tvSummary);
        if (mApp.isConnected()) {
            tvSummary.setText("Connected as " + mApp.getAccessToken());
        }

        StripeButton mStripeButton = (StripeButton) findViewById(R.id.btnConnect1);
        mStripeButton.setStripeApp(mApp);
        mStripeButton.addStripeConnectListener(new StripeConnectListener() {

            @Override
            public void onConnected() {
                tvSummary.setText("Connected as " + mApp.getAccessToken());
            }

            @Override
            public void onDisconnected() {
                tvSummary.setText("Disconnected");
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StripOAuthActivity.this, error, Toast.LENGTH_SHORT).show();
            }

        });

        StripeApp mApp2 = new StripeApp(this, "StripeAccount", ApplicationData.CLIENT_ID,
                ApplicationData.SECRET_KEY, ApplicationData.CALLBACK_URL);
        StripeButton mStripeButton2 = (StripeButton) findViewById(R.id.btnConnect2);
        mStripeButton2.setStripeApp(mApp2);
        mStripeButton2.setConnectMode(StripeApp.CONNECT_MODE.ACTIVITY);

        Stripe.apiKey = mApp.getAccessToken();
        mApp.getAccountName();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mobileAppTracker.measureSession();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (resultCode) {
            case StripeApp.RESULT_CONNECTED:
                Log.d("mApp.getAccessToken()", mApp.getAccessToken());
                tvSummary.setText("Connected as " + mApp.getAccessToken());
                // tellling appboy that stripe is connected.
                Appboy.getInstance(StripOAuthActivity.this).getCurrentUser().
                        setCustomUserAttribute(
                                "Stripe Account Identifier",
                                true
                        );

               /* ContentValues contentValues = new ContentValues();
                DBOHelper.update(this, Table.TrainerProfileDetails.TABLE_NAME, contentValues, Utils.getTrainerId(this));*/
                updateTrainerTokenDetails();
                break;
            case StripeApp.RESULT_ERROR:
                String error_description = data.getStringExtra("error_description");
                Toast.makeText(StripOAuthActivity.this, error_description, Toast.LENGTH_SHORT).show();
                Appboy.getInstance(StripOAuthActivity.this).getCurrentUser().
                        setCustomUserAttribute(
                                "Stripe Account Identifier",
                                false
                        );
                break;
        }

    }

    private void updateTrainerTokenDetails() {

        final Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("Trainer_Id", Utils.getTrainerId(this));
        objectMap.put("DwollaAccessToken", "");
        objectMap.put("StripeAccessToken", mApp.getAccessToken());


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                TrainerWebService.updateTrainerTokenDetails(objectMap, "UpdateTrainerTokenDetailsResult");
                return null;
            }
        }.execute();
        /*{"Trainer_Id":"383","DwollaAccessToken":"xcgvbjhdvjhdf","StripeAccessToken":"dhfdgjhdfbjghgfjkftbkj"}*/
    }


}
