package com.fitforbusiness.webservice;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

public class WebService {

    DefaultHttpClient httpClient;
    HttpContext localContext;
    private String ret;

    HttpResponse response = null;
    HttpPost httpPost = null;
    HttpGet httpGet = null;

    // The serviceName should be the name of the Service you are going to be
    // using.
    public WebService() {
        HttpParams myParams = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(myParams, 100000);
        HttpConnectionParams.setSoTimeout(myParams, 100000);
        httpClient = new DefaultHttpClient(myParams);
        localContext = new BasicHttpContext();
    }

    // Use this method to do a HttpPost\WebInvoke on a Web Service
    public String webInvoke(String webServiceUrl, String methodName,
                            Map<String, Object> params) {
        Gson gson = new Gson();

        return webInvoke(webServiceUrl, methodName, gson.toJson(params),
                "application/json");
    }

    public String webInvoke(String webServiceUrl, String methodName,
                            String params) {
        Gson gson = new Gson();
        return webInvoke(webServiceUrl, methodName, gson.toJson(params),
                "application/json");
    }


    private String webInvoke(String webServiceUrl, String methodName,
                             String data, String contentType) {
        Log.d("data is", data);
        ret = null;

        httpPost = new HttpPost(webServiceUrl + methodName);
        response = null;

        httpPost.setHeader("apikey", "85314139-B77A-4D1A-9692-2409663D5E6E");
        httpPost.setHeader("authtoken", "E2918D98-5525-4BF3-BD17-D13E88EEC586");

        StringEntity tmp = null;

        if (contentType != null) {
            httpPost.setHeader("Content-Type", contentType);

        } else {
            httpPost.setHeader("Content-Type",
                    "application/x-www-form-urlencoded");
        }

        try {
            // data="{\"trainerBusinessInsuranceDetails\":{\"Trainer\":{\"Trainer_Id\":\"383\",\"FirstName\":\"Test \",\"LastName\":\"Testing\",\"DOB\":\"1994-11-05 13:15:30Z\",\"Gender\":\"0\",\"ContactNo\":\"2145551\",\"EmergencyContact\":\"46577\",\"EmergencyNumber\":\"34745454\",\"Facebooklink\":\"facebook.com\",\"Twitterlink\":\"twitter.com\"},\"Business\":{\"Trainer_Id\":\"383\",\"PT_LicenseNo\":\"1994-11-05 13:15:30Z\",\"PT_LicenseRenewal_Date\":\"1994-11-05 13:15:30Z\",\"CEC_Renewal_Date\":\"1994-11-05 13:15:30Z\",\"First_AidCertRenewal\":\"1994-11-05 13:15:30Z\",\"CPR_CertRenewal\":\"1994-11-05 13:15:30Z\",\"AED_CertRenewal\":\"1994-11-05 13:15:30Z\",\"Website\":\"www.google.com\",\"Experience\":\"1\",\"Company_Name\":\"cvdgf\",\"Company_Id\":\"1234\",\"Company_TaxId\":\"132123\"},\"Insurance\":{\"Trainer_Id\":\"383\",\"Membership_No\":\"123123\",\"Expiry_Date\":\"1994-11-05 13:15:30Z\",\"Insurance_Provider\":\"0\"}}}";
            Log.d("overridden data  is", data);
            tmp = new StringEntity(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("FitnessApp", "HttpUtils : UnsupportedEncodingException : " + e);
        }

        httpPost.setEntity(tmp);

        try {
            response = httpClient.execute(httpPost, localContext);

            if (response != null) {
                ret = EntityUtils.toString(response.getEntity());
                Log.d("Response Code:", response.getStatusLine().getStatusCode() + "");
            }
        } catch (Exception e) {
            Log.e("FitnessApp", "HttpUtils: " + e);
        }

        return ret;
    }

    // Use this method to do a HttpGet/WebGet on the web service
    public String webGet(String webServiceUrl, String methodName,
                         Map<String, String> params) {
        String getUrl = webServiceUrl + methodName;

        int i = 0;
        if (null != params) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (i == 0) {
                    getUrl += "?";
                } else {
                    getUrl += "&";
                }

                try {
                    getUrl += param.getKey() + "="
                            + URLEncoder.encode(param.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                i++;
            }

        }

        Log.d("getUrl", getUrl);
        httpGet = new HttpGet(getUrl);

        httpGet.setHeader("apikey", "85314139-B77A-4D1A-9692-2409663D5E6E");
        httpGet.setHeader("authtoken", "B198645B-DF03-4E23-874B-4651E8132E75");
        //Log.e("WebGetURL: ", getUrl);

        try {
            response = httpClient.execute(httpGet);
        } catch (Exception e) {
            Log.e("FitnessApp:", e.toString());
        }

        // we assume that the response body contains the error message
        try {
            if (response != null) {
                if (response.getEntity() != null)
                    ret = EntityUtils.toString(response.getEntity());
                Log.d("Response Code:", response.getStatusLine().getStatusCode() + "");
            }
        } catch (IOException e) {
            Log.e("FitnessApp:", e.toString());
        }
        return ret;
    }

    public static JSONObject Object(Object o) {
        try {
            return new JSONObject(new Gson().toJson(o));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getHttpStream(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            response = httpConn.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception e) {
            throw new IOException("Error connecting");
        } // end try-catch

        return in;
    }

    public void clearCookies() {
        httpClient.getCookieStore().clear();
    }

    public void abort() {
        try {
            if (httpClient != null) {
                System.out.println("Abort.");
                httpPost.abort();
            }
        } catch (Exception e) {
            System.out.println("Your App Name Here" + e);
        }
    }
}
