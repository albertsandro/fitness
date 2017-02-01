package com.fitforbusiness.webservice;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjeet on 6/3/14.
 */
public class TrainerWebService {

    public static String trainerWebServiceURL = Utils.BASE_URL + Utils.TRAINING_APP_SERVICE;

    public static void getTrainerProfileDetails(Context context, String trainer_id) {
        try {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", trainer_id);
            WebService w = new WebService();
            String response = w.webGet(trainerWebServiceURL,
                    "GetAllTrainerDetails", param);
            JSONObject obj = null;
            if (response != null) {
                obj = new JSONObject(response);
                obj = obj.getJSONObject("GetAllTrainerDetailsResult");

                Log.d("JSON obj is ", response);
                ContentValues values = new ContentValues();


                JSONObject trainer = null;
                try {
                    trainer = obj.getJSONObject("Trainer");
                    if (trainer != null) {
                        values.put(Table.TrainerProfileDetails.TRAINER_ID, trainer_id);
                        values.put(Table.TrainerProfileDetails.FIRST_NAME, trainer.getString("FirstName"));
                        values.put(Table.TrainerProfileDetails.LAST_NAME, trainer.getString("LastName"));
                        values.put(Table.TrainerProfileDetails.EMAIL_ID, trainer.getString("EmailId"));
                        values.put(Table.TrainerProfileDetails.PHONE_NO, trainer.getString("ContactNo"));
                        values.put(Table.TrainerProfileDetails.EMERGENCY_CONTACT, trainer.getString("EmergencyContact"));
                        values.put(Table.TrainerProfileDetails.EMERGENCY_CONTACT_NO, trainer.getString("EmergencyNumber"));
                        values.put(Table.TrainerProfileDetails.DATE_OF_BIRTH, trainer.getString("DOB").replace("Z", ""));

                        values.put(Table.TrainerProfileDetails.GENDER, trainer.getString("Gender"));
                        values.put(Table.TrainerProfileDetails.TWITTER_ID, trainer.getString("Twitterlink"));
                        values.put(Table.TrainerProfileDetails.FACEBOOK_ID, trainer.getString("Facebooklink"));


                        if (trainer.getString("ImagePath") != null && !(trainer.getString("ImagePath")
                                .equalsIgnoreCase(""))
                                && Utils.createLocalResourceDirectory(context)) {
                            Utils.decodeFromBase64(trainer.getString("ImagePath"),
                                    "trainer" + ".JPG");
                        }

                        Log.d("JSON trainer is ", trainer.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                JSONObject insurance = null;
                try {
                    insurance = obj.getJSONObject("Insurance");
                    if (insurance != null) {
                        values.put(Table.TrainerProfileDetails.INSURANCE_MEMBERSHIP_NO, insurance.getString("Membership_No"));
                        values.put(Table.TrainerProfileDetails.INSURANCE_EXPIRY_DATE, insurance.getString("Expiry_Date").replace("Z", ""));
                        values.put(Table.TrainerProfileDetails.INSURANCE_PROVIDER, insurance.getString("Insurance_Provider"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                JSONObject business = null;
                try {
                    business = obj.getJSONObject("Business");
                    if (business != null) {
                        values.put(Table.TrainerProfileDetails.PT_LICENSE_NUMBER, business.getString("PT_LicenseNo"));
                        values.put(Table.TrainerProfileDetails.PT_LICENSE_RENEWAL_DATE, business.getString("PT_LicenseRenewal_Date").replace("Z", ""));
                        values.put(Table.TrainerProfileDetails.FIRST_AID_CERT_RENEWAL_DATE, business.getString("First_AidCertRenewal").replace("Z", ""));
                        values.put(Table.TrainerProfileDetails.CPR_CERT_RENEWAL_DATE, business.getString("CPR_CertRenewal").replace("Z", ""));
                        values.put(Table.TrainerProfileDetails.AED_CERT_RENEWAL_DATE, business.getString("AED_CertRenewal").replace("Z", ""));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d("inserted trainer id is", "" + DBOHelper.insert(context, Table.TrainerProfileDetails.TABLE_NAME, values));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public static void updateTrainerProfile(Context context, Map<String, Object> params, String objectKey) {

        try {
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "UpdateAllTrainerDetails",
                    params);
            JSONObject json;
            Log.d("value of json is", response);

            json = new JSONObject(response);
            json = json.getJSONObject(objectKey);
            Log.d("json is", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception ex) {

        }

    }

    public static void updateTrainerTokenDetails(Map<String, Object> params, String objectKey) {

        try {
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "UpdateTrainerTokenDetails",
                    params);
            JSONObject json;
            Log.d("UpdateTrainerTokenDetailsResult  is", response);

            json = new JSONObject(response);
            json = json.getJSONObject(objectKey);
            Log.d("Success is", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            /*{"UpdateTrainerTokenDetailsResult":{"Error":0,"Message":"trainer token saved successfully","Id":"382"}}*/
        } catch (Exception ex) {
            Log.d("Error in updating tokens  is", "");
        }

    }

    public static void updateTrainerTwitterDetails(Map<String, Object> params, String objectKey) {

        try {
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "UpdateTrainerTwitterDetails",
                    params);
            JSONObject json;
            Log.d("UpdateTrainerTwitterDetailsResult  is", response);

            json = new JSONObject(response);
            json = json.getJSONObject(objectKey);
            Log.d("Success is", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            /*{"UpdateTrainerTokenDetailsResult":{"Error":0,"Message":"trainer token saved successfully","Id":"382"}}*/
        } catch (Exception ex) {
            Log.d("Error in updating tokens  is", "");
        }

    }

    public static void updateTrainerImage(Context context, String trainerId, String objectKey) {

        try {
            Map<String, Object> details = new HashMap<String, Object>();
            details.put("trainerId", trainerId);
            details.put("imageName", Utils.encodeBase64(
                    Utils.LOCAL_RESOURCE_PATH + "trainer" + ".JPG"
            ));
            WebService w = new WebService();
            String response = w.webInvoke(trainerWebServiceURL, "UpdateTrainerImage",
                    details);
            JSONObject json;
            if (response != null) {
                Log.d("value of json is", response);
                json = new JSONObject(response);
                json = json.getJSONObject(objectKey);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void createPayment(Map<String, Object> params, String objectKey) {
        try {
            WebService w = new WebService();
            Log.d("CreatePaymentRequest:json", "Requesting...");
            Log.d("CreatePaymentRequest:json", ""+new Gson().toJson(params));
            String response = w.webInvoke(trainerWebServiceURL, "CreatePaymentRequest",
                    params);
            JSONObject json;
            Log.d("CreatePaymentRequest:json", response);
            json = new JSONObject(response);
            json = json.getJSONObject(objectKey);
            Log.d("CreatePaymentRequest:json", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
