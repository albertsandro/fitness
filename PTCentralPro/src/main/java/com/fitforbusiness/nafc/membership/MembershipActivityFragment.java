package com.fitforbusiness.nafc.membership;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.fitforbusiness.Parse.Models.AssessmentForm;
import com.fitforbusiness.Parse.Models.Membership;
import com.fitforbusiness.database.DatabaseHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.framework.MembershipListAdapter;
import com.fitforbusiness.framework.SwipeDetector;
import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MembershipActivityFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private ListView saleList;
    private ArrayList<HashMap<String, Object>> mapArrayList;

    public MembershipActivityFragment() {

    }

/*    public static MembershipActivityFragment newInstance(int flag, String client_group_id) {
        MembershipActivityFragment fragment = new MembershipActivityFragment();
        Bundle args = new Bundle();
        args.putInt(Utils.ARG_GROUP_OR_CLIENT, flag);
        args.putString(Utils.ARG_GROUP_OR_CLIENT_ID, client_group_id);
        fragment.setArguments(args);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


     // testStripePaymentDetails();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_membership_activity, container, false);
        saleList = (ListView) rootView.findViewById(R.id.lvPreWorkOutAssessment);

//        loadMemberships();
        final SwipeDetector swipeDetector = new SwipeDetector(getActivity());
        saleList.setOnTouchListener(swipeDetector);
        saleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map map = mapArrayList.get(i);

                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        // showDeleteAlert(map.get("_id").toString());
                        // Toast.makeText(getActivity(), "Action Right to left", Toast.LENGTH_LONG).show();
                    } else {

                    }
                } else {
                    startActivity(new Intent(getActivity(), ViewMemberShipActivity.class)
                            .putExtra(Utils.ARG_MEMBERSHIP_ID, map.get("_id").toString())
                            .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID))
                            .putExtra(Utils.ARG_GROUP_OR_CLIENT, getArguments().getInt(Utils.ARG_GROUP_OR_CLIENT)));
                }
            }
        });

        return rootView;
    }


    private void loadParseMembership() {
        mapArrayList = new ArrayList<HashMap<String, Object>>();
        ParseQuery parseQuery = new ParseQuery(Membership.class);
        Log.v("groupId","kkk"+getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
        parseQuery.whereEqualTo("groupId", getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<Membership>() {
            @Override
            public void done(List<Membership> list, ParseException e) {
                if (e == null && list != null) {
                    if (list.size() == 0) {
                        ParseQuery parseQuery = new ParseQuery(Membership.class);
                        parseQuery.whereEqualTo("groupId", getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID));
                        parseQuery.findInBackground(new FindCallback<Membership>() {
                            @Override
                            public void done(List<Membership> list, ParseException e) {
                                if (e == null && list != null) {
                                    loadIntoAssessmentFieldListView(list);
                                    Membership.pinAllInBackground(list);
                                }
                            }
                        });
                    } else {
                        loadIntoAssessmentFieldListView(list);
                    }
                }
            }
        });
    }

    private void loadIntoAssessmentFieldListView(List<Membership> list) {
        LinkedHashMap<String, Object> row;
        for (Membership membership: list) {
            row = new LinkedHashMap<String, Object>();
            row.put("_id", membership.getObjectId());
            row.put("firstLabel", membership.getDescription());
            row.put("secondLabel",membership.getNumberOfSessions());
            row.put("thirdLabel",membership.getCurrency());
            row.put("fourthLabel",membership.getTotalPackage());
//            row.put("type", assessmentForm.getType());
            mapArrayList.add(row);
        }
        MembershipListAdapter adapter = new MembershipListAdapter(getActivity(),
                R.layout.custom_training_row, R.id.tvText1,
                R.id.tvText2, R.id.tvText3, R.id.tvText4, mapArrayList);
        saleList.setAdapter(adapter);
    }


    private void loadMemberships() {
        mapArrayList = new ArrayList<HashMap<String, Object>>();
        SQLiteDatabase sqlDB = null;
        try {
            sqlDB = DatabaseHelper.instance().getReadableDatabase();

            String query = "select  * "
                    + " from " +
                    Table.Membership.TABLE_NAME +
                    " where " + Table.DELETED + " = 0  and "
                    + Table.Membership.GROUP_ID + " = " + getArguments().getString(Utils.ARG_GROUP_OR_CLIENT_ID)
                    + " and " + Table.Membership.MEMBERSHIP_TYPE + " = " + getArguments().getInt(Utils.ARG_GROUP_OR_CLIENT);

            Log.d("query is ", query);
            assert sqlDB != null;
            Cursor cursor = sqlDB
                    .rawQuery(query
                            , null);

            LinkedHashMap<String, Object> row;
            while (cursor.moveToNext()) {
                row = new LinkedHashMap<String, Object>();
                row.put("_id", cursor.getString(cursor
                        .getColumnIndex(Table.Membership.ID)));
                row.put("firstLabel", cursor.getString(cursor
                        .getColumnIndex(Table.Membership.DESCRIPTION)));
                row.put("secondLabel", cursor.getString(cursor
                        .getColumnIndex(Table.Membership.SESSION)));
                String symbol = Utils.getCurrencySymbol(cursor.getString(cursor
                        .getColumnIndex(Table.Membership.CURRENCY)));
                row.put("thirdLabel", symbol + " " + cursor.getString(cursor
                        .getColumnIndex(Table.Membership.COST_PER_SESSION)));
                row.put("fourthLabel", symbol + " " + cursor.getString(cursor
                        .getColumnIndex(Table.Membership.PACKAGE_TOTAL)));
                mapArrayList.add(row);
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
        MembershipListAdapter adapter = new MembershipListAdapter(getActivity(),
                R.layout.custom_training_row, R.id.tvText1,
                R.id.tvText2, R.id.tvText3, R.id.tvText4, mapArrayList);
        saleList.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onResume(){
        super.onResume();
        loadParseMembership();
    }
}
