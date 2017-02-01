package com.fitforbusiness.oauth.dropbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.fitforbusiness.nafc.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sanjeet on 6/5/14.
 */


public class DropBoxFileList extends ActionBarActivity {

    DropboxAPI<AndroidAuthSession> mApi;
    private final String PHOTO_DIR = "/Photos/";

    final static private String APP_KEY = "4pqqfp0k0ruso0y";
    final static private String APP_SECRET = "37bs5lqcx9nr29a";

    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private String mPath = "/";
    private ArrayList<DropboxAPI.Entry> files;
    String[] fNames = null;
    ListView listView;
    SimpleAdapter adapter;
    ArrayList<Map<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropboc_directory_list);
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);


        new getDropboxDirectory().execute(mPath);

        // gv.setBackgroundResource(R.drawable.black_cloud1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                // TODO Auto-generated method stub
               /* Toast.makeText(DropBoxFileList.this,
                        listView.getItemAtPosition(arg2).toString(), Toast.LENGTH_SHORT).show();*/
                Log.d("list is", list.toString());

                if (arg2 > 0) {
                    DropboxAPI.Entry ent = files.get(arg2 - 1);
                    // String path = ent.path;
                    if (ent.isDir) {
                        new getDropboxDirectory().execute(ent.path);
                        mPath = ent.parentPath();
                    } else {

                        Intent intent = new Intent();
                        intent.putExtra("path", ent.path);
                        intent.putExtra("name", ent.fileName());
                        setResult(123, intent);
                        finish();
                    }
                } else {

                    if (!mPath.equalsIgnoreCase("/")) {
                        String example = mPath.substring(mPath.lastIndexOf("/") + 1);
                        mPath = mPath.replace(example, "");
                        Log.d("mPath is", mPath);
                        new getDropboxDirectory().execute(mPath);
                    } else {


                    }
                }
                //.setData(fnames, gv.getItemAtPosition(arg2).toString());


            }

        });
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    private String[] listDropboxDir() {


        return null;
    }


    public class getDropboxDirectory extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            list = null;
            list = new ArrayList<Map<String, String>>();
            listView = (ListView) findViewById(R.id.lvDropBoxDirectory);
            LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();
            row.put("path", mPath);
            row.put("name", "../");
            list.add(0, row);
        }

        @Override
        protected Void doInBackground(String... params) {

            DropboxAPI.Entry dirent = null;
            try {
                dirent = mApi.metadata(params[0], 1000, null, true, null);
                int i = 0;
                files = new ArrayList<DropboxAPI.Entry>();

                ArrayList<String> dir = new ArrayList<String>();
                LinkedHashMap<String, String> row;

                // dir.add(0, mPath);

                for (DropboxAPI.Entry ent : dirent.contents) {
                    row = new LinkedHashMap<String, String>();
                    row.put("path", ent.path);
                    row.put("name", ent.fileName());
                    files.add(ent);// Add it to the list of thumbs we can choose from
                    //dir = new ArrayList<String>();
                    list.add(row);
                    // dir.add(new String(files.get(i++).path));

                }

                //fNames = dir.toArray(new String[dir.size()]);

                Log.d("f names", dir.toString());

            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (list != null) {

                adapter = new SimpleAdapter(DropBoxFileList.this, list
                        , android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});

                listView.setAdapter(adapter);
            }
        }
    }


}
