package com.fitforbusiness.nafc.dashboard;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fitforbusiness.Parse.Models.Client;
import com.fitforbusiness.Parse.Models.Exercise;
import com.fitforbusiness.Parse.Models.Session;
import com.fitforbusiness.Parse.Models.SessionExercise;
import com.fitforbusiness.Parse.Models.Workout;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.oauth.stripe.StripeApp;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.payment.stripe.activity.ApplicationData;
import com.fitforbusiness.stripe.compat.AsyncTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFeedsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String WEEK_FILTER = "-7 day";
    private static final String WEEK_MONTH = "-1 month";
    private static final String WEEK_YEAR = "-1 year";

    TextView newClients, sessions, memberships;
    TextView clientName, contactNumber, scheduledSession;

    private ImageView clientImage;

    private ArrayList<Map<String, String>> mapArrayList;
    private ArrayList<HashMap<String, Object>>  mapSumList=new ArrayList<HashMap<String, Object>>();
    HashMap<String, Object> row=new HashMap<>();
    private Workout workout=new Workout();
    StripeApp mApp;
    int count=0;
    HashMap<String, Object> maxMap=new HashMap<>();

    public NewsFeedsFragment() {
    }

    public static NewsFeedsFragment newInstance(int position) {
        NewsFeedsFragment fragment = new NewsFeedsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news_feeds, container, false);
        newClients = (TextView) rootView.findViewById(R.id.tvClientCount);
        sessions = (TextView) rootView.findViewById(R.id.tvSessionCount);
        memberships = (TextView) rootView.findViewById(R.id.tvMembershipCount);

        clientName = (TextView) rootView.findViewById(R.id.tvName);
        contactNumber = (TextView) rootView.findViewById(R.id.tvNumber);
        scheduledSession = (TextView) rootView.findViewById(R.id.tvSessions);

        clientImage = (ImageView) rootView.findViewById(R.id.ivClientImage);

        //newClients(newClients);
        TextView topWorkout = (TextView) rootView.findViewById(R.id.tvTopWorkout);
//        topWorkout.setText(""+topWorkout());
        topWorkout(topWorkout);

        TextView totalWorkout = (TextView) rootView.findViewById(R.id.tvTotalWorkouts);
       // totalWorkout.setText(DBOHelper.totalWorkouts() + "");
        //topWorkout.setText(""+totalWorkout());
        totalWorkout(totalWorkout);

        TextView topExercise = (TextView) rootView.findViewById(R.id.tvTopExercise);
//        topExercise.setText(DBOHelper.topExercise() + "");
        topExercise(topExercise);

        TextView totalEx = (TextView) rootView.findViewById(R.id.tvTotalExercise);
//        totalExercise.setText(DBOHelper.totalExercises() + "");
        totalExcercise(totalEx);
        setAtAGlance(WEEK_FILTER);
        //latestClientDetails();
        getLatestClientName();
        setQualificationAlerts();
        TextView detailFilter = (TextView) rootView.findViewById(R.id.bDayFilter);
        detailFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOption(v);
            }
        });


        mApp = new StripeApp(getActivity(), "StripeAccount", ApplicationData.CLIENT_ID,
                ApplicationData.SECRET_KEY, ApplicationData.CALLBACK_URL);
        Stripe.apiKey =  //mApp.getAccessToken();
       "sk_live_jc5ZCJp17mmVDeW9SwcDY2gF";
        return rootView;
    }

    private void setAtAGlance(String filter) {
        final Calendar calendar = Calendar.getInstance();
        // memberships.setText("$" + DBOHelper.membershipPackageTotal(filter));
        if (filter.contains("day")) {
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            testStripePaymentDetails(calendar.getTimeInMillis() / 1000);
        } else if (filter.contains("month")) {
            calendar.add(Calendar.MONTH, -1);
            testStripePaymentDetails(calendar.getTimeInMillis() / 1000);
        } else if (filter.contains("year")) {
            calendar.add(Calendar.YEAR,-1);
            testStripePaymentDetails(calendar.getTimeInMillis() / 1000);
        }
        newClients(newClients, calendar);
        newSessions(sessions,calendar);
    }
//    private void setAtAGlance(String filter) {
//        newClients.setText(DBOHelper.newClientsTotal(filter));
//        sessions.setText(DBOHelper.scheduledSessionTotal(filter));
//        // memberships.setText("$" + DBOHelper.membershipPackageTotal(filter));
//        if (filter.contains("day")) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DAY_OF_MONTH, -7);
//            testStripePaymentDetails(calendar.getTimeInMillis() / 1000);
//        } else if (filter.contains("month")) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.MONTH, -1);
//            testStripePaymentDetails(calendar.getTimeInMillis() / 1000);
//        } else if (filter.contains("year")) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.YEAR, -1);
//            testStripePaymentDetails(calendar.getTimeInMillis() / 1000);
//        }
//
//    }

    private void getLatestClientName(){
        ParseQuery parseQuery = new ParseQuery(Client.class);
        parseQuery.fromLocalDatastore();
        parseQuery.orderByDescending("updatedAt");
        parseQuery.findInBackground(new FindCallback<Client>() {
            @Override
            public void done(List<Client> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Client.class);
                        parseQuery.orderByDescending("updatedAt");
                        parseQuery.findInBackground(new FindCallback<Client>() {
                            @Override
                            public void done(List<Client> list, ParseException e) {
                                if (e == null && list != null) {
                                    clientName.setText(list.get(0).getFirstName()+list.get(0).getLastName());
                                    contactNumber.setText(list.get(0).getContactPhone());

                                }
                            }
                        });
                    } else {
                        clientName.setText(list.get(0).getFirstName() + list.get(0).getLastName());
                        contactNumber.setText(list.get(0).getContactPhone());
                        if (list.get(0).getImageFile()!=null){
                            Bitmap bmp;
                            String fileObjectStr = list.get(0).getImageFile();
                            byte[] fileObject = android.util.Base64.decode(fileObjectStr, 1);
                            bmp = BitmapFactory.decodeByteArray(fileObject, 0, fileObject.length);
                            clientImage.setImageBitmap(bmp);
                        }
                    }
                }
            }
        });
    }

    private void newClients(final TextView textView, Calendar calendar){
       final Date limitDate=calendar.getTime();
        ParseQuery parseQuery = new ParseQuery(Client.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereGreaterThan("updatedAt", limitDate);
        parseQuery.findInBackground(new FindCallback<Client>() {
            @Override
            public void done(List<Client> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Client.class);
                        parseQuery.whereGreaterThan("updatedAt", limitDate);
                        parseQuery.findInBackground(new FindCallback<Client>() {
                            @Override
                            public void done(List<Client> list, ParseException e) {
                                if (e == null && list != null) {
                                    textView.setText(list.size()+"");
                                }
                            }
                        });
                    } else {
                        textView.setText(list.size()+"");
                    }
                }
            }
        });
    }

    private void newSessions(final TextView textView, Calendar calendar){
        final Date limitDate=calendar.getTime();
        ParseQuery parseQuery = new ParseQuery(Session.class);
        parseQuery.fromLocalDatastore();
        parseQuery.whereGreaterThan("updatedAt", limitDate);
        parseQuery.findInBackground(new FindCallback<Session>() {
            @Override
            public void done(List<Session> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Session.class);
                        parseQuery.whereGreaterThan("updatedAt",limitDate);
                        parseQuery.findInBackground(new FindCallback<Session>() {
                            @Override
                            public void done(List<Session> list, ParseException e) {
                                if (e == null && list != null) {
                                    textView.setText(list.size() + "");
                                }
                            }
                        });
                    } else {
                        textView.setText(list.size() + "");
                    }
                }
            }
        });
    }

    private void topExercise(final TextView textView){

        ParseQuery parseQuery = new ParseQuery(SessionExercise.class);
        //parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<SessionExercise>() {
            @Override
            public void done(List<SessionExercise> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                    } else {
                        mapSumList=new ArrayList<HashMap<String, Object>>();
                        boolean flag = false;
                       for (SessionExercise sessionExercise:list){
                           if (sessionExercise.getExercise()!=null){
                               if (flag == false) {
                                   row.put("exercise", sessionExercise.getExercise());
                                   row.put("sum", 1 + "");
                                   mapSumList.add(row);
                                   flag = true;
                               }
                               boolean existflag = false;
                               for (HashMap map : mapSumList) {
                                   if (map.get("exercise").equals(sessionExercise.getExercise())) {
                                       int sum = Integer.parseInt(map.get("sum").toString()) + 1;
                                       map.put("sum", sum + "");
                                       existflag = true;
                                       break;
                                   }
                               }
                               if (existflag == false) {
                                   HashMap maplistitem = new HashMap<>();
                                   maplistitem.put("exercise", sessionExercise.getExercise());
                                   maplistitem.put("sum", 1 + "");
                                   mapSumList.add(maplistitem);
                               }
                           }
                       }
                        if (mapSumList != null && mapSumList.size() != 0) {
                            maxMap = mapSumList.get(0);
                            for (HashMap map : mapSumList) {
                                String str = maxMap.get("sum").toString();
                                String str1 = map.get("sum").toString();
                                if (Integer.parseInt(maxMap.get("sum").toString()) < Integer.parseInt(map.get("sum").toString())) {
                                    maxMap = map;
                                }
                            }
                        }
                        textView.setText(((Exercise)maxMap.get("exercise")).getName().toString());
                    }
                }
            }
        });
    }

    private void topWorkout(final TextView textView){
        ParseQuery parseQuery = new ParseQuery(Session.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Session>() {
            @Override
            public void done(List<Session> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Session.class);
                        parseQuery.findInBackground(new FindCallback<Session>() {
                            @Override
                            public void done(List<Session> list, ParseException e) {
                                if (e == null && list != null) {
                                    Session.pinAllInBackground(list);
                                    mapSumList = new ArrayList<HashMap<String, Object>>();
                                    boolean flag = false;
                                    // String str = list.get(0).getWorkout();
                                    for (Session session : list) {
                                        if (session.getWorkout()!=null) {
                                            if (!session.getWorkout().equals("-1")) {
                                                if (flag == false) {
                                                    row.put("workout", session.getWorkout());
                                                    row.put("sum", 1 + "");
                                                    mapSumList.add(row);
                                                    flag = true;
                                                }
                                                boolean existflag = false;
                                                for (HashMap map : mapSumList) {
                                                    if (map.get("workout").equals(session.getWorkout())) {
                                                        int sum = Integer.parseInt(map.get("sum").toString()) + 1;
                                                        map.put("sum", sum + "");
                                                        existflag = true;
                                                        break;
                                                    }
                                                }
                                                if (existflag == false) {
                                                    HashMap maplistitem = new HashMap<>();
                                                    maplistitem.put("workout", session.getWorkout());
                                                    maplistitem.put("sum", 1 + "");
                                                    mapSumList.add(maplistitem);
                                                }
                                            }
                                        }
                                    }
                                    if (mapSumList != null && mapSumList.size() != 0) {
                                        maxMap = mapSumList.get(0);
                                        for (HashMap map : mapSumList) {
                                            String str = maxMap.get("sum").toString();
                                            String str1 = map.get("sum").toString();
                                            if (Integer.parseInt(maxMap.get("sum").toString()) < Integer.parseInt(map.get("sum").toString())) {
                                                maxMap = map;
                                            }
                                        }
                                        ParseQuery parseQuery = new ParseQuery(Workout.class);
                                        parseQuery.fromLocalDatastore();
                                        parseQuery.whereEqualTo("objectId", maxMap.get("workout"));
                                        parseQuery.findInBackground(new FindCallback<Workout>() {
                                            @Override
                                            public void done(List<Workout> list, ParseException e) {
                                                if (e == null && list != null) {
                                                    if (list.size() == 0) {
                                                        ParseQuery parseQuery = new ParseQuery(Workout.class);
                                                        parseQuery.whereEqualTo("objectId", maxMap.get("workout"));
                                                        parseQuery.findInBackground(new FindCallback<Workout>() {
                                                            @Override
                                                            public void done(List<Workout> list, ParseException e) {
                                                                if (e == null && list != null) {
                                                                    workout = list.get(0);
                                                                    textView.setText(workout.getName());
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        workout = list.get(0);
                                                        textView.setText(workout.getName());
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    } else {
                        mapSumList = new ArrayList<HashMap<String, Object>>();
                        boolean flag = false;
                        // String str = list.get(0).getWorkout();
                        for (Session session : list) {
                            if (session.getWorkout()!=null) {
                                if (!session.getWorkout().equals("-1")) {
                                    if (flag == false) {
                                        row.put("workout", session.getWorkout());
                                        row.put("sum", 1 + "");
                                        mapSumList.add(row);
                                        flag = true;
                                    }
                                    boolean existflag = false;
                                    for (HashMap map : mapSumList) {
                                        if (map.get("workout").equals(session.getWorkout())) {
                                            int sum = Integer.parseInt(map.get("sum").toString()) + 1;
                                            map.put("sum", sum + "");
                                            existflag = true;
                                            break;
                                        }
                                    }
                                    if (existflag == false) {
                                        HashMap maplistitem = new HashMap<>();
                                        maplistitem.put("workout", session.getWorkout());
                                        maplistitem.put("sum", 1 + "");
                                        mapSumList.add(maplistitem);
                                    }
                                }
                            }
                        }
                        if (mapSumList != null && mapSumList.size() != 0) {
                            maxMap = mapSumList.get(0);
                            for (HashMap map : mapSumList) {
                                String str = maxMap.get("sum").toString();
                                String str1 = map.get("sum").toString();
                                if (Integer.parseInt(maxMap.get("sum").toString()) < Integer.parseInt(map.get("sum").toString())) {
                                    maxMap = map;
                                }
                            }
                            ParseQuery parseQuery = new ParseQuery(Workout.class);
                            parseQuery.fromLocalDatastore();
                            parseQuery.whereEqualTo("objectId", maxMap.get("workout"));
                            parseQuery.findInBackground(new FindCallback<Workout>() {
                                @Override
                                public void done(List<Workout> list, ParseException e) {
                                    if (e == null && list != null) {
                                        if (list.size() == 0) {
//                                            ParseQuery parseQuery = new ParseQuery(Workout.class);
//                                            parseQuery.whereEqualTo("objectId", maxMap.get("workout"));
//                                            parseQuery.findInBackground(new FindCallback<Workout>() {
//                                                @Override
//                                                public void done(List<Workout> list, ParseException e) {
//                                                    if (e == null && list != null) {
//                                                        workout = list.get(0);
//                                                        textView.setText(workout.getName());
//                                                    }
//                                                }
//                                            });
                                        } else {
                                            workout = list.get(0);
                                            textView.setText(workout.getName());
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }
    private void totalWorkout(final TextView textView){
        ParseQuery parseQuery = new ParseQuery(Workout.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Workout>() {
            @Override
            public void done(List<Workout> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Workout.class);
                        parseQuery.findInBackground(new FindCallback<Workout>() {
                            @Override
                            public void done(List<Workout> list, ParseException e) {
                                if (e == null && list != null) {
                                    count=list.size();
                                    textView.setText(count + "");
                                    Workout.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        count=list.size();
                        textView.setText(count + "");
                    }
                }
            }
        });
    }

    private void totalExcercise(final TextView textView){
        ParseQuery parseQuery = new ParseQuery(Exercise.class);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Exercise>() {
            @Override
            public void done(List<Exercise> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Exercise.class);
                        parseQuery.findInBackground(new FindCallback<Exercise>() {
                            @Override
                            public void done(List<Exercise> list, ParseException e) {
                                if (e == null && list != null) {
                                    count=list.size();
                                    textView.setText(count+"");
                                    Exercise.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        count=list.size();
                        textView.setText(count+"");
                    }
                }
            }
        });
    }
    public void showOption(final View view) {

        final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.duration_option, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ((TextView) view).setText(menuItem.getTitle());
                switch (menuItem.getItemId()) {
                    case R.id.menuWeek:
                        setAtAGlance(WEEK_FILTER);
                        break;
                    case R.id.menuMonth:
                        setAtAGlance(WEEK_MONTH);
                        break;
                    case R.id.menuYear:
                        setAtAGlance(WEEK_YEAR);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void latestClientDetails() {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            String query = " select c.*, count(s._id) as session_count  from client c left join sessions s on " +
                    "s.group_id=c._id where c.deleted=0 and " +
                    "c._id=( select  _id from (select * from client where deleted=0 order by _id desc limit 1))";
            Log.d("query string", query);
            sqLiteDatabase = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {

                if (cursor.getString(cursor.getColumnIndex(Table.Client.FIRST_NAME)) != null) {
                    clientName.setText(cursor.getString(cursor.getColumnIndex(Table.Client.FIRST_NAME)) +
                            " " + cursor.getString(cursor.getColumnIndex(Table.Client.LAST_NAME)));
                    contactNumber.setText(cursor.getString(cursor.getColumnIndex(Table.Client.CONTACT_NO)));
                    try {
                        clientImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + cursor.getString(cursor
                                .getColumnIndex(Table.Client.PHOTO_URL))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    scheduledSession.setText(cursor.getString(cursor.getColumnIndex("session_count")) + " scheduled sessions");
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            assert sqLiteDatabase != null;
            sqLiteDatabase.close();
        } finally {
            assert sqLiteDatabase != null;
            sqLiteDatabase.close();
        }


    }


    private void setQualificationAlerts() {

        // mapArrayList = new ArrayList<Map<String, String>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select course_name, julianday(completed_date)-julianday('now','-2 year') " +
                    "as current_qualifications  from trainer_profile_accreditation where date(completed_date)>=" +
                    "date('now','-2 year');";

            Log.d("query is ", query);
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            LinkedHashMap<String, String> row;
            while (cursor.moveToNext()) {
                float expiryDay = cursor.getFloat(cursor
                        .getColumnIndex("current_qualifications"));
                if ((int) expiryDay <= 90 && (int) expiryDay >= 0) {
                    row = new LinkedHashMap<String, String>();
                    row.put("title", cursor.getString(cursor
                            .getColumnIndex("course_name")) + " is expiring in ");
                    row.put("no_of_days", (int) cursor.getFloat(cursor
                            .getColumnIndex("current_qualifications")) + " day");
                    mapArrayList.add(row);
                }

            }
            cursor.close();
        } catch (Exception e) {
            assert sqlDB != null;
            sqlDB.close();
            e.printStackTrace();
        } finally {
            assert sqlDB != null;
            sqlDB.close();
        }
       /* adapter = new SimpleAdapter(getActivity(), mapArrayList, R.layout.custom_list_row_assesssment,
                new String[]{"title", "current_qualifications"}, new int[]{R.id.tvFormName, R.id.tvNoOfFields});
*/

    }

    private void testStripePaymentDetails(final long filter) {

        new AsyncTask<Void, Void, ChargeCollection>() {

            @Override
            protected ChargeCollection doInBackground(Void... params) {
                ChargeCollection chargeCollection = null;
                Map<String, Object> chargeParams = new HashMap<String, Object>();
                chargeParams.put("limit", 100);
                Map<String, Object> created = new HashMap<String, Object>();
                created.put("gte", filter);
                try {
                    chargeParams.put("created", created);
                    chargeCollection = Charge.all(chargeParams);
                    Log.d("chargeCollection", chargeCollection.toString());
                    Log.d("filter", filter + "");
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                } catch (InvalidRequestException e) {
                    e.printStackTrace();
                } catch (APIConnectionException e) {
                    e.printStackTrace();
                } catch (CardException e) {
                    e.printStackTrace();
                } catch (APIException e) {
                    e.printStackTrace();
                }
                return chargeCollection;
            }

            @Override
            protected void onPostExecute(ChargeCollection chargeCollection) {
                super.onPostExecute(chargeCollection);
                int amountSum = 0;
                if (chargeCollection != null) {
                    List<Charge> charges = chargeCollection.getData();
                    for (Charge aCharge : charges) {
                        amountSum = amountSum + aCharge.getAmount();
                        Log.d("aCharge date"+aCharge.getCreated(),aCharge.getAmount()+"/t"+amountSum);
                    }
                    Log.d("aCharge",charges.size()+"");
                }
                memberships.setText("$" + (amountSum / 100));
            }
        }.execute();
    }

}
