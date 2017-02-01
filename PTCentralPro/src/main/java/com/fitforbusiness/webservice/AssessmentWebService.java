package com.fitforbusiness.webservice;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjeet on 19-Jun-14.
 */
public class AssessmentWebService {


    static String trainerWebServiceURL = "http://ec2-54-184-179-46.us-west-2.compute.amazonaws.com/" +
            "PTCentralPro-Dev/TrainingAppService.svc/";

    public static void getAll(Context context, String trainer_id) {
        try {
            Map<String, String> param = new HashMap<String, String>();
            param.put("Trainer_Id", trainer_id);
            WebService w = new WebService();
            String response = w.webGet(trainerWebServiceURL,
                    "GetAllTrainerDetails", param);
             JSONObject obj = new JSONObject(response);
            obj = obj.getJSONObject("GetAllTrainerDetailsResult");
            Log.d("JSON obj is ", obj.toString());
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
                    values.put(Table.TrainerProfileDetails.DATE_OF_BIRTH, trainer.getString("DOB"));

                    values.put(Table.TrainerProfileDetails.GENDER, trainer.getString("Gender"));
                    values.put(Table.TrainerProfileDetails.TWITTER_ID, trainer.getString("Twitterlink"));
                    values.put(Table.TrainerProfileDetails.FACEBOOK_ID, trainer.getString("Facebooklink"));

                    Utils.decodeFromBase64(trainer.getString("ImagePath"),
                            Environment.getExternalStorageDirectory()
                                    + "/.ffb/" + "trainer" + ".JPG"
                    );
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
                    values.put(Table.TrainerProfileDetails.INSURANCE_EXPIRY_DATE, insurance.getString("Expiry_Date"));
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
                    values.put(Table.TrainerProfileDetails.PT_LICENSE_RENEWAL_DATE, business.getString("PT_LicenseRenewal_Date"));
                    values.put(Table.TrainerProfileDetails.FIRST_AID_CERT_RENEWAL_DATE, business.getString("First_AidCertRenewal"));


                    values.put(Table.TrainerProfileDetails.CPR_CERT_RENEWAL_DATE, business.getString("CPR_CertRenewal"));
                    values.put(Table.TrainerProfileDetails.AED_CERT_RENEWAL_DATE, business.getString("AED_CertRenewal"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d("inserted trainer id is", "" + DBOHelper.insert(context, Table.TrainerProfileDetails.TABLE_NAME, values));


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
