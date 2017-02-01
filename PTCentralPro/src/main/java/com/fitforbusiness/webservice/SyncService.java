package com.fitforbusiness.webservice;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.DataDownloadAsyncTask;

import java.util.Timer;
import java.util.TimerTask;

public class SyncService extends Service {
    private final int UPDATE_INTERVAL = 900 * 1000;
    private Timer timer = new Timer();
    private static final int NOTIFICATION_EX = 1;
    private NotificationManager notificationManager;
    TimerTask doAsynchronousTask;
    private DataDownloadAsyncTask dataDownloadAsyncTask;

    public SyncService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callAsynchronousTask();
        timer.schedule(doAsynchronousTask, 0, UPDATE_INTERVAL);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int interval = intent.getIntExtra("interval", UPDATE_INTERVAL);
            if (interval > 0) {
                setInterval(interval);
            } else if (interval < 0) {
                stopService();
            } else {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                String listPreference = prefs.getString("sync_frequency", "15");
                setInterval(Integer.valueOf(listPreference) * 60 * 1000);
            }
        }
        Toast.makeText(this, "Sync Started!", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    private void stopService() {
        if (timer != null) timer.cancel();
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            String trainer_id = Utils.getTrainerId(getBaseContext());
                            if (trainer_id != null) {
                                Log.d("DataDownloadAsyncTask", "Called");
                                if (Utils.isNetworkAvailable(getBaseContext())
                                        && Integer.valueOf(Utils.getTrainerId(getBaseContext())) > 0) {
                                    dataDownloadAsyncTask = new DataDownloadAsyncTask(getBaseContext(), trainer_id + "");
                                    dataDownloadAsyncTask.execute();
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
    }

    private void setInterval(int updateInterval) {
        if (timer != null) {
            timer.cancel();
        }
        callAsynchronousTask();
        timer = new Timer();
        timer.schedule(doAsynchronousTask, 0, updateInterval);
        Log.d("updateInterval", updateInterval + "");
    }

}
