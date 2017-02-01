package com.fitforbusiness.nafc.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by Adeel on 4/10/2015.
 */
public class NativeSync {

    private static final String TAG = "Calendar";

    public static ArrayList<HashMap<String, Object>> getAllEvents () {
        SQLiteDatabase sqlDB = null;
        ArrayList<HashMap<String, Object>> mapSessionArray = new ArrayList<HashMap<String, Object>>();
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  * "
                    + " from " +
                    Table.Sessions.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 "
                    + " and " + Table.Sessions.END_DATE + " >= date(\'now\') ";

            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);
            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {
                row = new LinkedHashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.ID)));
                row.put(Table.Sessions.TITLE, cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.TITLE)));
                row.put(Table.Sessions.VENUE, cursor.getString(cursor
                        .getColumnIndex(Table.Sessions.VENUE)));
                row.put(Table.Sessions.NATIVE_ID, cursor.getLong(cursor
                        .getColumnIndex(Table.Sessions.NATIVE_ID)));
                row.put(Table.Sessions.IS_NATIVE, cursor.getInt(cursor
                        .getColumnIndex(Table.Sessions.IS_NATIVE)));
                row.put(Table.Sessions.SESSION_TYPE, cursor.getInt(cursor
                        .getColumnIndex(Table.Sessions.SESSION_TYPE)));
                row.put(Table.Sessions.START_DATE, Utils.dateConversionFromString(
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.START_DATE))));
                row.put(Table.Sessions.END_DATE, Utils.dateConversionFromString(
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.END_DATE))));
                row.put(Table.Sessions.START_TIME, Utils.timeFromString(
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.START_TIME))));
                row.put(Table.Sessions.END_TIME, Utils.timeFromString(
                        cursor.getString(cursor.getColumnIndex(Table.Sessions.END_TIME))));
                mapSessionArray.add(row);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return mapSessionArray;
    }

    public static Set<Long> getAllNativeEventIdsFromApp () {
        SQLiteDatabase sqlDB = null;
        Set<Long> ids = new HashSet<>();
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            String query = "select  " + Table.Sessions.NATIVE_ID
                    + " from " +
                    Table.Sessions.TABLE_NAME +
                    " where " + Table.DELETED + " = 0 "
                    + " and " + Table.Sessions.IS_NATIVE + " = 1 ";

            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(cursor
                        .getColumnIndex(Table.Sessions.NATIVE_ID)));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return ids;
    }

//    public static void addEventToNative(Context c, HashMap<String, Object> map) {
//        Log.d("newtest", "in add to native funcion");
//        long calID = NativeSync.getLocalCalendarId(c);
//        Log.d("newtest", "calendar id = " + calID);
//        if (calID == -1) return;
//
//        Date startDate = (Date) map.get(Table.Sessions.START_DATE);
//        Date endDate = (Date) map.get(Table.Sessions.END_DATE);
//
////        if (!startDate.equals(endDate)) {
////            Log.d("newtest", "start date was equal end date");
////            return;
////        }
//
//        Date startTime = (Date) map.get(Table.Sessions.START_TIME);
//        Date endTime = (Date) map.get(Table.Sessions.END_TIME);
//
//        int baseYear = 1900;
//
//        long startMillis = 0;
//        long endMillis = 0;
//        Calendar beginTimeCall = Calendar.getInstance();
//        beginTimeCall.set(startDate.getYear() + baseYear,
//                startDate.getMonth(), startDate.getDate(),
//                startTime.getHours(), startTime.getMinutes());
//
//        Log.d("Calendar", (startDate.getYear() + baseYear) + " , " +
//                startDate.getMonth() + " , " + startDate.getDate() + " , " +
//                startTime.getHours() + " , " + startTime.getMinutes());
//
//        startMillis = beginTimeCall.getTimeInMillis();
//
//        Calendar endTimeCal = Calendar.getInstance();
//        endTimeCal.set(endDate.getYear()  + baseYear,
//                endDate.getMonth(), endDate.getDate(),
//                endTime.getHours(), endTime.getMinutes());
//
//        Log.d("Calendar", (endDate.getYear() + baseYear) + " , " +
//                endDate.getMonth() + " , " + endDate.getDate() + " , " +
//                endTime.getHours() + " , " + endTime.getMinutes());
//
//        endMillis = endTimeCal.getTimeInMillis();
//
//        if (Build.VERSION.SDK_INT >= 14) {
//
//            ContentResolver cr = c.getContentResolver();
//            ContentValues values = new ContentValues();
//            values.put(CalendarContract.Events.DTSTART, startMillis);
//            values.put(CalendarContract.Events.DTEND, endMillis);
//            values.put(CalendarContract.Events.TITLE, (String) map.get(Table.Sessions.TITLE));
//            String str = "";
//            if (map.get(Table.Sessions.SESSION_TYPE) == Utils.FLAG_CLIENT) {
//                str = "Client workout session, " + map.get(Table.Sessions.VENUE);
//            } else {
//                str = "Group workout session, " + map.get(Table.Sessions.VENUE);
//            }
//            values.put(CalendarContract.Events.DESCRIPTION, str);
//            values.put(CalendarContract.Events.CALENDAR_ID, calID);
//            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
//
//            Uri uri;
//            if (((long) map.get(Table.Sessions.NATIVE_ID)) == 0) {
//                uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
//            } else {
//                uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
//                        (Long) map.get(Table.Sessions.NATIVE_ID));
//                c.getContentResolver().update(uri, values, null, null);
//            }
//
//
//
//            // get the event ID that is the last element in the Uri
//            long eventID = Long.parseLong(uri.getLastPathSegment());
//            //
//            // ... do something with event ID
//            Log.d("Calendar", "created " + eventID);
//            Log.d("newtest", "was created");
//            setNativeEventId((String) map.get(Table.Sessions.ID), eventID);
//        }
//
//    }

    public static long getLocalCalendarId(Context c) {
        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;

        long id = -1;

        if (Build.VERSION.SDK_INT >= 14) {
            // Run query
            Cursor cur = null;
            ContentResolver cr = c.getContentResolver();
            Uri uri = CalendarContract.Calendars.CONTENT_URI;
//            String selection = "(("
//                    + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
//            String[] selectionArgs = new String[]{"LOCAL"};

            // Submit the query and get a Cursor object back.
//            cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
            cur = cr.query(uri, EVENT_PROJECTION, null, null, null);
            cur.moveToFirst();
            if (cur.getCount() > 0) {
                id = (int) cur.getLong(PROJECTION_ID_INDEX);
                Log.d("Calendar", "there are local calenders " + id);
            }
            if (cur != null && !cur.isClosed())
                cur.close();
        }
        return  id;
    }

    public static void setNativeEventId(String row_id, long eventId) {
        ContentValues values = new ContentValues();
        values.put(Table.Sessions.NATIVE_ID, eventId);
        DBOHelper.update(Table.Sessions.TABLE_NAME, values, row_id);
    }

    public static void getAllNativeEvents (Context c) {

        Set<Long> nativeIds = new HashSet<>();

        final String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events._ID,                   // 0
                CalendarContract.Events.TITLE,                // 1
                CalendarContract.Events.DTSTART,         // 2
                CalendarContract.Events.DTEND,             //3
                CalendarContract.Events.RRULE             //4
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_TITLE_INDEX = 1;
        final int PROJECTION_DTSTART_INDEX = 2;
        final int PROJECTION_DTEND_INDEX = 3;
        final int PROJECTION_RRULE_INDEX = 4;

        // Run query
        Cursor cur = null;
        ContentResolver cr = c.getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        try {
            // Submit the query and get a Cursor object back.
            cur = cr.query(uri, EVENT_PROJECTION, null, null, null);
            cur.moveToFirst();

            do {
                nativeIds.add(cur.getLong(PROJECTION_ID_INDEX));

                Date startDate = new Date(cur.getLong(PROJECTION_DTSTART_INDEX));
                Date startTime = new Date();

                startTime.setHours(startDate.getHours());
                startTime.setMinutes(startDate.getMinutes());
                startDate.setHours(0);
                startDate.setMinutes(0);

                Date endDate = new Date(cur.getLong(PROJECTION_DTEND_INDEX));
                Date endTime = new Date();

                endTime.setHours(endDate.getHours());
                endTime.setMinutes(endDate.getMinutes());
                endDate.setHours(0);
                endDate.setMinutes(0);

                ContentValues contentValues = new ContentValues();
                contentValues.put(Table.Sessions.START_DATE, Utils.formatConversionLocale(startDate));
                contentValues.put(Table.Sessions.END_DATE, Utils.formatConversionLocale(endDate));
                contentValues.put(Table.Sessions.START_TIME, Utils.timeFormat24(startTime));
                contentValues.put(Table.Sessions.END_TIME, Utils.timeFormat24(endTime));
                contentValues.put(Table.Sessions.RECURRENCE_RULE, cur.getString(PROJECTION_RRULE_INDEX));

                // check if event with this id exist,
                if (DBOHelper.eventExists(cur.getLong(PROJECTION_ID_INDEX))) {
                    //  if yes then update time and date
                    DBOHelper.update(Table.Sessions.TABLE_NAME, contentValues,
                            Table.Sessions.NATIVE_ID, cur.getLong(PROJECTION_ID_INDEX));
                } else {
                    //  else insert this as a new event
                    contentValues.put(Table.Sessions.NATIVE_ID, cur.getLong(PROJECTION_ID_INDEX));
                    contentValues.put(Table.Sessions.IS_NATIVE, 1);
                    contentValues.put(Table.Sessions.TITLE, cur.getString(PROJECTION_TITLE_INDEX));

                    DBOHelper.insert(c, Table.Sessions.TABLE_NAME, contentValues);
                }
            } while (cur.moveToNext());

            if (cur != null && !cur.isClosed())
                cur.close();

            // get all native ids from local which don't exist in calendar now
            // and delete these rows
            Set<Long> localIds = getAllNativeEventIdsFromApp();
            localIds.removeAll(nativeIds);

            for (Long id : localIds) {
                DBOHelper.delete(Table.Sessions.TABLE_NAME, Table.Sessions.NATIVE_ID, id + "");
            }
        } catch (Exception ignored) {}
    }

    public static void deleteEventFromNative (Context c, long id) {
        ContentResolver cr = c.getContentResolver();
        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id);
        int rows = cr.delete(deleteUri, null, null);
        Log.i("NativeCal", "Rows deleted: " + rows);
    }
}
