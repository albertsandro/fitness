package com.fitforbusiness.nafc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appboy.Appboy;
import com.crashlytics.android.Crashlytics;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.framework.FFBActivity;
import com.fitforbusiness.framework.Result;
import com.fitforbusiness.framework.TaskCallBack;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.webservice.WebInterface;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Sanjeet on 5/26/14.
 */
public class LoginActivity extends FFBActivity implements TaskCallBack {
    String authCred = Utils.BASE_URL + Utils.TRAINING_APP_SERVICE;
    EditText email, password;
    Button login, register;
    WebInterface webInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.fragment_login);
        email = (EditText) findViewById(R.id.etUserName);
       // email.setText("sawan_s33@gmail.com");
        password = (EditText) findViewById(R.id.etPassword);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
      //  password.setText("1234");
        login = (Button) findViewById(R.id.btnLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable(LoginActivity.this) && validateFields()) {
                    // First check if you can login directly to parse
                    Trainer.logInInBackground(email.getText().toString(), password.getText().toString(),
                            new LogInCallback() {
                                @Override
                                public void done(ParseUser parseUser, ParseException e) {
                                    if (e == null) {
                                        SharedPreferences settings = getSharedPreferences(Utils.TRAINER_PREFS, 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putBoolean("hasLoggedIn", true);
                                        editor.putString("trainer_id", parseUser.getObjectId());
                                        editor.putBoolean("download_data", true);
                                        editor.commit();

                                        startActivity(new Intent
                                                (LoginActivity.this, MainActivity.class));
                    // else try loggingIn through old server and then transfer to parse
                                    } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                        Log.d("ParseKiException", e.getCode() + "");
                                        webInterface = new WebInterface(LoginActivity.this, setParams(),
                                                authCred, "ValidateTrainerCredentials",
                                                "ValidateTrainerCredentialsResult");
                                        webInterface.execute();
                                    } else {
                                        Toast.makeText(LoginActivity.this, e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "No Internet Connection.!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        register = (Button) findViewById(R.id.btnCreate);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        showLoginScreen();
    }

    private Map<String, Object> setParams() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("email", email.getText().toString());
        params.put("password", password.getText().toString());
        return params;
    }

    @Override
    public void done() {
        try {
            Result result = (Result) webInterface.get();
            if (result.success > 0) {
                SharedPreferences settings = getSharedPreferences(Utils.TRAINER_PREFS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("hasLoggedIn", true);
                editor.putString("trainer_id", String.valueOf(result.success));
                editor.putBoolean("download_data", true);
                editor.commit();
                // Setup AppBoy
                Appboy.getInstance(this).changeUser(email.getText().toString());
                setCustomeAppBoyAttributes();
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Utils.showToast(this, "Error : Cannot login ," + result.error);
                Log.d("The value of result is", result.success + ":" +
                        result.error + ":" + result.json.toString());
            }
        } catch (InterruptedException e) {
            Toast.makeText(this, "Error occurred !", Toast.LENGTH_LONG).show();
            System.out.println("Error occurred in done  :InterruptedException  " + e.toString());
            e.printStackTrace();
        } catch (ExecutionException e) {
            Toast.makeText(this, "Error occurred !", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            System.out.println("Error occurred in done  :ExecutionException  " + e.toString());
        }

    }

    public boolean validateFields() {
        boolean field_1 = true;
        boolean field_2 = true;
        if (email.getText().toString().length() == 0) {
            email.setError("Name cannot be blank");
            field_1 = false;
        }
        if (password.getText().toString().length() == 0) {
            password.setError("Email cannot be blank");
            field_2 = false;
        }
        return field_1 && field_2;
    }

    private void showLoginScreen() {
        SharedPreferences settings = getSharedPreferences(Utils.TRAINER_PREFS, 0);
        boolean hasLoggedIn = settings.getBoolean("hasLoggedIn", false);
        if (hasLoggedIn || Trainer.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void setCustomeAppBoyAttributes(){
        Appboy.getInstance(LoginActivity.this).getCurrentUser().
                setCustomUserAttribute(
                        "Dropbox Account Identifier",
                        false
                );
        Appboy.getInstance(LoginActivity.this).getCurrentUser().
                setCustomUserAttribute(
                        "Stripe Account Identifier",
                        false
                );
        Appboy.getInstance(getApplicationContext()).getCurrentUser().
                setCustomUserAttribute(
                        "Twitter Account Identifier",
                        false
                );
    }

}
