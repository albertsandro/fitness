package com.fitforbusiness.nafc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.appboy.Appboy;
import com.fitforbusiness.Parse.Models.Group;
import com.fitforbusiness.Parse.Models.Trainer;
import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.NavigationListAdapter;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.webservice.WebInterface;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NavigationDrawerFragment extends Fragment {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    String[] list = new String[]{"Home", "News Feed", "My Calendar", "Clients", "Groups",
            "Exercises", "Workouts", "Qualifications", "Payment Details", "Settings", "Feedback", "Logout"};
    int[] image = new int[]{
            R.drawable.home_selected,
            R.drawable.news_feed_selected,
            R.drawable.calendar_selected,
            R.drawable.client_selected,
            R.drawable.group_selected,
            R.drawable.exercise_selected,
            R.drawable.workouts_selected,
            R.drawable.qualifications_selected,
            R.drawable.stripe_selected,
            R.drawable.settings_selected,
            R.drawable.ic_feedback,
            R.drawable.logout_selected
    };
    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private TextView profileName;
    private TextView profileEmail;
    private ImageView profileImage;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
        selectItem(mCurrentSelectedPosition);

    }

    private void loadTrainerImage() {
        try {
            profileImage.setImageBitmap(BitmapFactory.decodeFile(Utils.PROFILE_THUMBNAIL_PATH + "trainer.JPG"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView = (ListView) rootView.findViewById(R.id.lvNavigationDrawerDrawer);
        mDrawerListView.setCacheColorHint(Color.TRANSPARENT);
        TableRow trainer = (TableRow) rootView.findViewById(R.id.trTrainer);
        profileName = (TextView) rootView.findViewById(R.id.tvProfileName);
        profileEmail = (TextView) rootView.findViewById(R.id.tvEmail);

        profileImage = (ImageView) rootView.findViewById(R.id.ivTrainer);
        Trainer loginTrainer=getTrainer(Utils.getTrainerId(getActivity()));
        if (loginTrainer.getImageFile()!=null) {
            String fileObjectStr=loginTrainer.getImageFile();
            byte[] fileObject = android.util.Base64.decode(fileObjectStr,1);
            Bitmap bmp = BitmapFactory.decodeByteArray(fileObject,0,fileObject.length);
            profileImage.setImageBitmap(bmp);
        }
        else {
            profileImage.setImageBitmap(null);
        }
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(12);
            }
        });
        try {
            String id=Utils.getTrainerId(getActivity());
            String string=getTrainer(id).getFirstName()+getTrainer(id).getLastName();
            String str=getTrainer(id).getEmail();
            profileName.setText(string);
            profileEmail.setText(str);
//            getTrainerNameEmail(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        navigationListSetUp();
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        }

        trainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem(12);
            }
        });

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
//        loadTrainerImage();
        return rootView;
    }

    private void navigationListSetUp() {
        DBOHelper.loadSessions();
        ArrayList<HashMap<String, Object>> menuMapArrayList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        for (int i = 0; i < list.length; i++) {
            map = new HashMap<String, Object>();
            map.put("title", list[i]);
            map.put("icon", getResources().getDrawable(image[i]));
            switch (i) {
                case 3:
                    map.put("count", DBOHelper.totalParseClients() + "");
                    break;
                case 4:
                    map.put("count", DBOHelper.totalParseGroups() + "");
                    break;
                case 7:
                    map.put("count", DBOHelper.totalParseQualificatioins() + "");
                    break;
                case 5:
                    map.put("count", DBOHelper.totalParseExercises() + "");
                    break;
                case 6:
                    map.put("count", DBOHelper.totalParseWorkouts() + "");
                    DBOHelper.loadAssessmentField();
                    DBOHelper.loadAssessmentFormField();
                    DBOHelper.loadAssessmentForm();
                    DBOHelper.loadAssessmentFormType();
                    break;
                default:
                    map.put("count", "No");
            }
            menuMapArrayList.add(map);
        }
        NavigationListAdapter adapter = new NavigationListAdapter(getActivity(),
                R.layout.menu_row, R.id.ivMenuIcon, R.id.tvCategoryName,
                R.id.tvCount, menuMapArrayList);
        mDrawerListView.setAdapter(adapter);
    }

    private  static Trainer parseUser=new Trainer();
    private Trainer getTrainer(String trainer_id){
        SharedPreferences settings =getActivity().getSharedPreferences(Utils.TRAINER_PREFS, 0);

           List<Trainer> list = new ArrayList<Trainer>();
           ParseQuery parseQuery = new ParseQuery(Trainer.class);
           parseQuery.whereEqualTo("objectId", trainer_id);
           try {
               list = parseQuery.find();
           } catch (ParseException e) {

           }
           parseUser = list.get(0);
           SharedPreferences.Editor editor = settings.edit();
           editor.putString("trainer_parseusername", parseUser.getFirstName() + parseUser.getLastName());
           editor.putString("trainer_parseuseremail", parseUser.getEmail());
           editor.putString("trainer_parseuserimage", parseUser.getImageFile());
           editor.commit();

        return parseUser;
    }

    private String getTrainerNameEmail(String trainer_id) {
        SQLiteDatabase sqlDB = null;
        String trainerName = "", trainerEmail= "";
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();
            Cursor cursor = sqlDB
                    .rawQuery("select  *  from " + Table.TrainerProfileDetails.TABLE_NAME
                            + " where trainer_id = " + trainer_id, null);
            if (cursor.moveToFirst() && cursor.getString(cursor
                    .getColumnIndex(Table.TrainerProfileDetails.FIRST_NAME)) != null) {
                trainerName = cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileDetails.FIRST_NAME))
                        + " " + cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileDetails.LAST_NAME));
                trainerEmail = cursor.getString(cursor
                        .getColumnIndex(Table.TrainerProfileDetails.EMAIL_ID));
            }else {
                trainerName = "";
                trainerEmail = "";
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            assert sqlDB != null;
            sqlDB.close();
        } finally {
            assert sqlDB != null;
            sqlDB.close();
        }
        profileName.setText(trainerName);
        profileEmail.setText(trainerEmail);
        return trainerName;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(), mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
              //  getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }
                try {
//                    loadTrainerImage();
//                    getTrainerNameEmail(Utils.getTrainerId(getActivity()));
                    SharedPreferences settings =getActivity().getSharedPreferences(Utils.TRAINER_PREFS, 0);
                    String id=Utils.getTrainerId(getActivity());
                    String string=settings.getString("trainer_parseusername",null);
                    String str=settings.getString("trainer_parseuseremail", null);

                    profileName.setText(string);
                    profileEmail.setText(str);

                    if (settings.getString("trainer_parseuserimage",null)!=null) {
                        String fileObjectStr=settings.getString("trainer_parseuserimage",null);
                        byte[] fileObject = android.util.Base64.decode(fileObjectStr,1);
                        Bitmap bmp = BitmapFactory.decodeByteArray(fileObject,0,fileObject.length);
                        profileImage.setImageBitmap(bmp);
                    }
                    else {
                        profileImage.setImageBitmap(null);
                    }
                    navigationListSetUp();
                    if (mDrawerListView != null) {
                        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
             //   getActivity().supportInvalidateOptionsMenu();
            }
        };
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        Log.d("Main:menu", "navigaion select item : " + position);
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_example:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public static interface NavigationDrawerCallbacks {

        void onNavigationDrawerItemSelected(int position);
    }

    @Override
    public void onResume() {
        super.onResume();
        Appboy.getInstance(getActivity()).requestSlideupRefresh();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
