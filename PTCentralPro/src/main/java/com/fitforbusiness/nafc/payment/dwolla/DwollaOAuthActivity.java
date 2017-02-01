package com.fitforbusiness.nafc.payment.dwolla;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.fitforbusiness.oauth.dwolla.DwollaApp;
import com.fitforbusiness.oauth.dwolla.DwollaButton;
import com.fitforbusiness.oauth.dwolla.DwollaConnectListener;
import com.fitforbusiness.nafc.R;


public class DwollaOAuthActivity extends ActionBarActivity {

    private DwollaApp mApp;
    private TextView tvSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dwolla_oauth);
        /*Button btnConnect = (Button) findViewById(R.id.bDwollaConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DwollaOAuthActivity.this, DwollaWebViewActivity.class));
            }
        });*/

        mApp = new DwollaApp(this, "DwollaAccount", ApplicationData.CLIENT_ID,
                ApplicationData.SECRET_KEY, ApplicationData.CALLBACK_URL);

        tvSummary = (TextView) findViewById(R.id.tvSummary);
        if (mApp.isConnected()) {
            tvSummary.setText("Connected as " + mApp.getAccessToken());
        }

        DwollaButton mDwollaButton = (DwollaButton) findViewById(R.id.btnConnect1);
        mDwollaButton.setDwollaApp(mApp);
        mDwollaButton.addDwollaConnectListener(new DwollaConnectListener() {

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
                Toast.makeText(DwollaOAuthActivity.this, error, Toast.LENGTH_SHORT).show();
            }

        });

        DwollaApp mApp2 = new DwollaApp(this, "DwollaAccount", ApplicationData.CLIENT_ID,
                ApplicationData.SECRET_KEY, ApplicationData.CALLBACK_URL);
        DwollaButton mDwollaButton2 = (DwollaButton) findViewById(R.id.btnConnect2);
        mDwollaButton2.setDwollaApp(mApp2);
        mDwollaButton2.setConnectMode(DwollaApp.CONNECT_MODE.ACTIVITY);
        //Stripe.apiKey = mApp.getAccessToken();
        mApp.getAccountName();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //   getMenuInflater().inflate(R.menu.dwolla_oauth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (resultCode) {
            case DwollaApp.RESULT_CONNECTED:
                tvSummary.setText("Connected as " + mApp.getAccessToken());
               /* ContentValues contentValues = new ContentValues();
                DBOHelper.update(this, Table.TrainerProfileDetails.TABLE_NAME, contentValues, Utils.getTrainerId(this));*/
                //  updateTrainerTokenDetails();
                break;
            case DwollaApp.RESULT_ERROR:
                String error_description = data.getStringExtra("error_description");
                Toast.makeText(DwollaOAuthActivity.this, error_description, Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
