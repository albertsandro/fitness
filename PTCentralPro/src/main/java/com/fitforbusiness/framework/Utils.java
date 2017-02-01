package com.fitforbusiness.framework;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.fitforbusiness.nafc.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Sanjeet on 5/15/14.
 */
public class Utils {

    //TODO
//    public static final String BASE_URL = "http://ec2-54-188-127-111.us-west-2.compute.amazonaws.com" + "/PTCentralPro-Prod/";
    public static final String BASE_URL = "http://ec2-54-188-127-111.us-west-2.compute.amazonaws.com/PTCentralPro-DEV/";
//    public static final String ENV = "Prod";
    public static final String ENV = "Dev";
    public static final String TRAINING_APP_SERVICE = "TrainingAppService.svc/";
    public static final String CLIENT_MANAGEMENT_SERVICE = "ClientManagementService.svc/";
    public static final String CLIENT_GROUP_MANAGEMENT_SERVICE = "ClientGroupManagementService.svc/";
    public static final String PROGRAM_MANAGEMENT_SERVICE = "ProgramManagementService.svc/";
    public static final String SCHEDULE_MANAGEMENT_SERVICE = "ScheduleManagementService.svc/";
    public static final String TRAINER_PREFS = "MyPrefsFile";
    public static final String SYNC_PREFS = "sync_prefs";
    public static final int POST = 1;
    public static final int GET = 2;
    public static final int DASHBOARD = 1;
    public static final int CALENDAR = 2;
    public static final int CLIENTS = 3;
    public static final int GROUPS = 4;
    public static final int ACCREDITATION = 5;
    public static final int EXERCISES = 6;
    public static final int SETTINGS = 7;
    public static final int PRE_WORKOUT_ASSESSMENT = 8;
    public static final int SESSION = 9;
    public static final int MEMBERSHIP = 10;
    public static final String TITLE = "title";
    public static final String SELECTION_MODE = "selectionMode";
    public static final int MULTI_SELECT = 0;
    public static final int SINGLE_SELECT = 1;
    public static final int ACTION_CAPTURE_IMAGE = 1325;
    public static final int ACTION_PICK_IMAGE = 1326;

    public static final String LOCAL_RESOURCE_PATH =
            Environment.getExternalStorageDirectory() + "/.ffb/";
    public static final String THUMBNAIL_PATH = LOCAL_RESOURCE_PATH + ".thumbnails/";
    public static final String PROFILE_THUMBNAIL_PATH = LOCAL_RESOURCE_PATH + ".profile/";


    public static final int FLAG_CLIENT = 0;
    public static final int FLAG_GROUP = 1;

    public static final String ARG_GROUP_OR_CLIENT = "arg_group_or_client";
    public static final String ARG_GROUP_OR_CLIENT_ID = "arg_group_or_client_id";
    public static final String ARG_GROUP_OR_CLIENT_NAME = "arg_group_or_client_name";

    public static final String CUSTOM_EVENT_KEY_CREATE_CLIENT = "createclient";
    public static final String CUSTOM_EVENT_KEY_CREATE_GROUP = "creategroup";
    public static final String CUSTOM_EVENT_KEY_CREATE_EXERCISE = "createexercise";
    public static final String CUSTOM_EVENT_KEY_CREATE_WORKOUT = "createworkout";
    public static final String CUSTOM_EVENT_KEY_CREATE_QUALIFICATION = "createqualification";
    public static final String CUSTOM_EVENT_KEY_CREATE_ASSESSMENT = "createassessment";
    public static final String CUSTOM_EVENT_KEY_CREATE_SESSION = "createsession";
    public static final String CUSTOM_EVENT_KEY_CREATE_PAYMENT = "createpayment";
    public static final String ARG_MEMBERSHIP_ID = "arg_membership_id";
    public static final int MALE = 0;
    public static final int FEMALE = 1;


    public static boolean isNetworkAvailable(Context context) {
        // TODO Auto-generated method stub
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static Intent captureImage(String fileName, Context context) {

        File folder = new File(Utils.LOCAL_RESOURCE_PATH);

        String mPathImage = Utils.LOCAL_RESOURCE_PATH + fileName;

        if (!folder.exists())
            folder.mkdirs();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(mPathImage);
        Uri mImageCaptureUri = Uri.fromFile(file);
        try {
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    mImageCaptureUri);
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return intent;
    }

    public static Intent pickImage() {

        Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaIntent.setType("image/*");
        return mediaIntent;
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static String encodeBase64(String string) {
        try {
            if (string != null && !string.equalsIgnoreCase("")) {
                Bitmap bitmap = Utils.reduceImage(string);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byte_arr = stream.toByteArray();
                return Base64.encodeToString(byte_arr, 0);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("image encoding error", e.toString());
        }
        return "";
    }

    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context,
                message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 300);
        toast.show();
    }

    public static boolean isValidEmail(EditText target) {
        if (target == null) {
            //  target.setError("Provide a valid email !");
            return false;
        } else {
            boolean isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(target.getText().toString())
                    .matches();
            if (!isValid)
                target.setError("Provide a valid email !");
            return isValid;
        }
    }

    public static boolean validateFields(EditText textField) {

        if (textField.getText().toString().length() == 0) {
            textField.setError("Required");
            return false;
        } else return true;
    }

    public static String formatConversion(String date) {
        //String date_temp=date.replace("Z"," GMT");
        String DateStr = date.replace("Z", " GMT");
        SimpleDateFormat sim = new SimpleDateFormat("dd MMM yyyy");
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(DateStr);
            return sim.format(d);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return DateStr;
    }

    public static String formatConversionSQLite(String date) {
        //String date_temp=date.replace("Z"," GMT");
        String DateStr = date;
        SimpleDateFormat sim = new SimpleDateFormat("dd MMM yyyy");
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(DateStr);
            return sim.format(d);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return DateStr;
    }

    public static String dateConversionForRow(String dateString) {
        String formattedDate = dateString;
        try {
            if (dateString.contains("+")) {
                dateString = dateString.replace(
                        dateString.substring(dateString.indexOf("+"), dateString.length()), "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM");
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = parseFormat.parse(dateString);
            formattedDate = displayFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();

        }
        return formattedDate;
    }

    public static Date dateConversionFromString(String dateString) {
        try {
            if (dateString.contains("+")) {
                dateString = dateString.replace(
                        dateString.substring(dateString.indexOf("+"), dateString.length()), "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = parseFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return date;
    }
    public static Date dateConversionFromString2 (String dateString) {
        try {
            if (dateString.contains("+")) {
                dateString = dateString.replace(
                        dateString.substring(dateString.indexOf("+"), dateString.length()), "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat parseFormat = new SimpleDateFormat("dd MMM yyyy");
        Date date = null;
        try {
            date = parseFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return date;
    }

    public static String dateConversionDashBoard(String dateString) {
        String formattedDate = dateString;
        try {
            if (dateString.contains("+")) {
                dateString = dateString.replace(
                        dateString.substring(dateString.indexOf("+"), dateString.length()), "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = parseFormat.parse(dateString);
            formattedDate = displayFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();

        }
        return formattedDate;
    }

    public static String dateConversionForSessionRow(String dateString) {
        String formattedDate = dateString;

        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM hh:mm a");
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = parseFormat.parse(dateString);
            formattedDate = displayFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();

        }
        return formattedDate;
    }

    public static String timeFormat24(String time) {

        String formattedTime = time;
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        try {
            Date date = parseFormat.parse(time);
            formattedTime = displayFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();

        }
        return formattedTime;
    }

    public static String timeFormat24(Date time) {

        String formattedTime;
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm:ss");
        formattedTime = displayFormat.format(time);
        return formattedTime;
    }

    public static String timeFormatAMPM(String time) {

        String formattedTime = time;
        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm:ss");
        try {
            Date date = parseFormat.parse(time);
            formattedTime = displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedTime;
    }

    public static Date timeFromString(String time) {

        SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = null;
        try {
            date = parseFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatConversionUTC(String date) {
        //String date_temp=date.replace("Z"," GMT");
        String DateStr = date.replace("Z", " GMT");
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        try {
            Date d = new SimpleDateFormat("dd MMM yyyy").parse(DateStr);
            return sim.format(d);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return DateStr;
    }

    public static String formatConversionLocale(String date) {

        SimpleDateFormat sim = new SimpleDateFormat("dd MMM yyyy");
        try {
            Date d = new SimpleDateFormat("dd MMM yyyy").parse(date);
            return sim.format(d);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return date;
    }

    public static String formatConversionLocale(Date date) {
        SimpleDateFormat sim = new SimpleDateFormat("dd MMM yyyy");
        Date d = date;
        return sim.format(d);
    }

    public static String formatConversionDateOnly(Date date) {
        SimpleDateFormat sim = new SimpleDateFormat("dd MMM yyyy");
        Date d = date;
        return sim.format(d);
    }
    public static String formatConversionDateOnly2(Date date) {
        SimpleDateFormat sim = new SimpleDateFormat("dd MMM yyyy");
        Date d = date;
        return sim.format(d);
    }

    public static Bitmap getImage(String path) {
        try {
            File imageFile = new File(path);
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath());

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap reduceImage(String originalPath) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inPurgeable = true;
        o.inInputShareable = true;
        BitmapFactory.decodeFile(originalPath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 320;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inPurgeable = true;
        o2.inInputShareable = true;
        o2.inSampleSize = scale;
        Bitmap bitmapScaled = null;
        bitmapScaled = BitmapFactory.decodeFile(originalPath, o2);

        return bitmapScaled;
    }

    public static String encodeToBase64(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 480, 640, true);
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] byte_arr = stream.toByteArray();
            return Base64.encodeToString(byte_arr, Base64.DEFAULT);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("image encoding error", e.toString());
        }
        return null;
    }

    public static boolean decodeFromBase64(String imageString, String imageName) {
        try {
            byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (decodedByte != null) {
                saveImage(decodedByte, imageName);
            }
            decodedByte.recycle();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean saveImage(Bitmap bitmap, String imageName) {
        try {
            FileOutputStream outActual = new FileOutputStream(new File(LOCAL_RESOURCE_PATH + imageName));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outActual);
            outActual.flush();
            outActual.close();

            FileOutputStream outProfile = new FileOutputStream(new File(PROFILE_THUMBNAIL_PATH + imageName));
            Bitmap profileBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
            profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outProfile);
            outProfile.flush();
            outProfile.close();

            FileOutputStream outThumbnail = new FileOutputStream(new File(THUMBNAIL_PATH + imageName));
            Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outThumbnail);
            outThumbnail.flush();
            outThumbnail.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean saveImage(String path, String imageName) {
        try {
            Bitmap bitmap = reduceImage(path);

            if (bitmap != null) {
                FileOutputStream outOriginal = new FileOutputStream(new File(LOCAL_RESOURCE_PATH + imageName));
                //  Bitmap originalBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outOriginal);
                outOriginal.flush();
                outOriginal.close();

                FileOutputStream outProfile = new FileOutputStream(new File(PROFILE_THUMBNAIL_PATH + imageName));
                Bitmap profileBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
                profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outProfile);
                outProfile.flush();
                outProfile.close();

                FileOutputStream outThumbnail = new FileOutputStream(new File(THUMBNAIL_PATH + imageName));
                Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outThumbnail);
                outThumbnail.flush();
                outThumbnail.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean saveThumbnail(Bitmap bitmap, String imagePath) {
        try {
            File file = new File(imagePath);
            FileOutputStream out = new FileOutputStream(file);
            Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 30, 30, true);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getTrainerId(Context context) {

        try {
            SharedPreferences settings = context.getSharedPreferences(Utils.TRAINER_PREFS, 0);
            String trainer_id = settings.getString("trainer_id",null);
            return trainer_id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-1";
    }

    public static String getLastSyncTime(Context context) {

        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastSyncTime(Context context) {

        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLastExerciseSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastExerciseSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastExerciseSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastExerciseSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLastClientSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastClientSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastClientSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastClientSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getLastQualificationSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastQualificationSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastQualificationSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastQualificationSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLastWorkoutSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastWorkoutSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastWorkoutSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastWorkoutSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLastAssessmentSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastAssessmentSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastAssessmentSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastAssessmentSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLastSessionSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastSessionSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastSessionSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastSessionSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLastGroupSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            return lastSync.getString("lastGroupSyncTime", "1970-01-01 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastGroupSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastGroupSyncTime", getCurrentUTCTime());
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentUTCTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 5);
        return sdf.format(calendar.getTime());

    }


    public static void clearLastSyncTime(Context context) {
        try {
            SharedPreferences lastSync = context.getSharedPreferences(Utils.SYNC_PREFS, 0);
            SharedPreferences.Editor editor = lastSync.edit();
            editor.putString("lastSyncTime", "1970-01-01 00:00:00");
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void datePickerDialog(Context context, final Button mButton) {

        String defaultDate = mButton.getText().toString();
        Calendar cal = Calendar.getInstance();

        try {

            Date d = new SimpleDateFormat("dd MMM yyyy").parse(defaultDate);
            cal.setTime(d);
            Log.d("Date is", d + "");
        } catch (ParseException e) {
            Log.d("Error in date is", e.toString());
            e.printStackTrace();
        }

        DatePickerDialog dateDlg = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                        String formattedDate = sdf.format(c.getTime());
                        mButton.setText(formattedDate);
                    }

                }, cal
                .get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DAY_OF_MONTH)
        );

        dateDlg.show();


    }

    public static boolean validateFields(Button button) {

        if (button.getText().toString().length() > 0) {
            return true;
        } else {
            button.setError("Invalid Input!");
        }
        return false;
    }

    public static String getToday() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String formattedDate = sdf.format(c.getTime());
        return formattedDate;
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    public static Bitmap compressBitmapProfile(String fileName) {

        Bitmap profileImage = null;
        try {

            profileImage = ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeFile(THUMBNAIL_PATH + fileName), 100, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profileImage;
    }

    public static Bitmap compressBitmapTableRow(String path) {

        Bitmap profileImage = null;
        try {
            Bitmap bitmap = compressBitmapActualSize(path);
            profileImage = Bitmap.createScaledBitmap(bitmap, 30, 30,
                    true);
            profileImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 30, 30);
            Log.d("Image path to be compressed ", path);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Image path to be compressed ", path);
        }
        return BitmapFactory.decodeFile(path);
    }

    public static Bitmap compressBitmapActualSize(String path) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static boolean createLocalResourceDirectory(Context context) {
        try {
            File localResourceDirectory = new File(Environment.getExternalStorageDirectory() + "/.ffb");
            if (!localResourceDirectory.exists()) {
                boolean directoryCreated = localResourceDirectory.mkdir();
                if (!directoryCreated) {
                    Toast.makeText(context, "Please ensure External Storage on your device!", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createThumbNailDirectory(Context context) {
        try {
            File localResourceDirectory = new File(Environment.getExternalStorageDirectory() + "/.ffb/.thumbnails");
            if (!localResourceDirectory.exists()) {
                boolean directoryCreated = localResourceDirectory.mkdir();
                if (!directoryCreated) {
                    Toast.makeText(context, "Please ensure External Storage on your device!", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createProfileImageDirectory(Context context) {
        try {
            File localResourceDirectory = new File(Environment.getExternalStorageDirectory() + "/.ffb/.profile");
            if (!localResourceDirectory.exists()) {
                boolean directoryCreated = localResourceDirectory.mkdir();
                if (!directoryCreated) {
                    Toast.makeText(context, "Please ensure External Storage on your device!", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static float convertPixelToDensityIndependentPixels(Context context, int pixel) {
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                pixel, context.getResources().getDisplayMetrics());

        return pixels;
    }

    public static String getJSONData(Context context, String textFileName) {
        String strJSON;
        StringBuilder buf = new StringBuilder();
        InputStream json;
        try {
            json = context.getAssets().open(textFileName);

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));

            while ((strJSON = in.readLine()) != null) {
                buf.append(strJSON);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }


    public static void copyAssets(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("images");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            InputStream in;
            OutputStream out;
            try {
                in = assetManager.open("images/" + filename);
                File outFile = new File(path, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    public static String getCurrencySymbol(String currencyAbbr) {
        if (currencyAbbr!=null) {
            if (currencyAbbr.equalsIgnoreCase("USD")) {
                Currency currency = Currency.getInstance(Locale.US);
                return currency.getSymbol(Locale.US);
            } else if (currencyAbbr.equalsIgnoreCase("CAD")) {
                Currency currency = Currency.getInstance(Locale.CANADA);
                return currency.getSymbol(Locale.CANADA);
            } else if (currencyAbbr.equalsIgnoreCase("EUR")) {
                Currency currency = Currency.getInstance(Locale.FRANCE);
                return currency.getSymbol(Locale.FRANCE);
            } else if (currencyAbbr.equalsIgnoreCase("GBP")) {
                Currency currency = Currency.getInstance(Locale.UK);
                return currency.getSymbol(Locale.UK);
            } else if (currencyAbbr.equalsIgnoreCase("AUD")) {
                Currency currency = Currency.getInstance(new Locale("en", "AU"));
                return currency.getSymbol(new Locale("en", "AU"));
            }
        }
        return "";
    }

    public static ArrayList<String> getRTO(Context context) {
        ArrayList<String> RTO = new ArrayList<String>();
        try {
            JSONObject jsonObject = new JSONObject(Utils.getJSONData(context, "accredited_institutions.json"));
            JSONArray jsonArray = jsonObject.getJSONArray("accredited_institutions");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject institution = jsonArray.getJSONObject(i);
                RTO.add(institution.getString("institution") + " " + institution.getString("code"));
                //Log.d("\"institution\"", RTO.toString());
            }
        } catch (JSONException e) {
            Log.d("JSONException", e.toString());
            e.printStackTrace();
        }
        return RTO;
    }

    public static boolean isDateAfterLastSyncTime(String lastSyncDate, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date strDate = null;
        Date endDate = null;
        try {
            strDate = sdf.parse(lastSyncDate);
            endDate = sdf.parse(date);
        } catch (ParseException e) {
            Log.d("error in parsing", e.toString());
        }
        return endDate != null && (endDate.after(strDate) || endDate.equals(strDate));
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    public static String getUnit(Context context,String heading) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (heading.equalsIgnoreCase("time")) {
            return "("+prefs.getString("unit_time", context.getString(R.string.default_unit_time_value))+")";
        } else if (heading.equalsIgnoreCase("distance")) {
            return "("+prefs.getString("unit_distance", context.getString(R.string.default_unit_distance_value))+")";
        } else if (heading.equalsIgnoreCase("weight")) {
            return "("+prefs.getString("unit_weight", context.getString(R.string.default_unit_weight_value))+")";
        }
        return "";
    }

}
