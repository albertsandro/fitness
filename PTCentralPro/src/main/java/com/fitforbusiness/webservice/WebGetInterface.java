package com.fitforbusiness.webservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fitforbusiness.framework.Result;
import com.fitforbusiness.framework.TaskCallBack;
import com.fitforbusiness.jsonparser.JSONParser;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Sanjeet on 5/28/14.
 */
public class WebGetInterface extends AsyncTask {

    private TaskCallBack mCallback;
    private Map<String, String> params;
    private String url;
    private JSONParser jParser = null;
    private ProgressDialog pDialog;
    private Context context;
    private Result result;
    private String method;
    private String objectKey;


    public WebGetInterface(Context context, Map<String, String> params, String url, String method, String objectKey) {

        this.params = params;
        this.url = url;
        jParser = new JSONParser();
        this.context = context;
        result = new Result();
        mCallback = (TaskCallBack) context;
        this.method = method;
        this.objectKey = objectKey;


    }
 /*   public WebGetInterface(Context context,TrainerDetailFragment trainerDetailFragment,
                           Map<String, String> params, String url, String method, String objectKey) {

        this.params = params;
        this.url = url;
        jParser = new JSONParser();
        this.context = context;
        result = new Result();
        mCallback =  trainerDetailFragment;
        this.method = method;
        this.objectKey = objectKey;


    }*/

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected Result doInBackground(Object[] objects) {

        try {

            WebService w = new WebService();
            String response = w.webGet(url, method,
                    params);
            JSONObject json;
            Log.d("value of json is", response);

            json = new JSONObject(response);
            json = json.getJSONObject(objectKey);
            result.json = json;
            if (json.has(objectKey)) {
                result.success = json.getInt("Result");
                result.json = json;
                result.error = json.getString("Message");
            }

        } catch (Exception ex) {

            System.out.println("Error occurred in WebInterface : " + ex.toString());
        }

        return result;
    }


    @Override
    protected void onPostExecute(Object o) {
        //super.onPostExecute(o);
        try {
            pDialog.dismiss();
            mCallback.done();
        } catch (Exception ex) {

        }
    }

}
