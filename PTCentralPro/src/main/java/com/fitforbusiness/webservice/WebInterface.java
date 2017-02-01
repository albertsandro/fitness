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
 * Created by Sanjeet on 5/15/14.
 */
public class WebInterface extends AsyncTask {

    private TaskCallBack mCallback;
    private Map<String, Object> params;
    private String url;
    private JSONParser jParser = null;
    private ProgressDialog pDialog;
    private Context context;
    private Result result;
    private String method;
    private String objectKey;


    public WebInterface(Context context, Map<String, Object> params, String url, String method, String objectKey) {

        this.params = params;
        this.url = url;
        jParser = new JSONParser();
        this.context = context;
        result = new Result();
        mCallback = (TaskCallBack) context;
        this.method = method;
        this.objectKey = objectKey;


    }
   /* public WebInterface(Context context,TrainerDetailFragment trainerDetailFragment, Map<String, Object> params, String url, String method, String objectKey) {

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
        try {
            pDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Result doInBackground(Object[] objects) {

        try {
            WebService w = new WebService();
            String response = w.webInvoke(url, method,
                    params);
            JSONObject json;
            Log.d("value of json is", response);

            json = new JSONObject(response);
            json = json.getJSONObject(objectKey);
            if (json.has("Result")) {
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
