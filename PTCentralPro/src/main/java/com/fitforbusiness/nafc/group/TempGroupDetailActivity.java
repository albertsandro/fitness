package com.fitforbusiness.nafc.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.assessment.AssessmentFormList;
import com.fitforbusiness.nafc.assessment.PreWorkoutAssessmentFragment;
import com.fitforbusiness.nafc.membership.AddMemberShipActivity;
import com.fitforbusiness.nafc.membership.MembershipActivityFragment;
import com.fitforbusiness.nafc.session.AddSessionActivity;
import com.fitforbusiness.nafc.session.PersonalTrainingFragment;
import com.fitforbusiness.nafc.session.SessionFragment;

public class TempGroupDetailActivity extends ActionBarActivity {


    String group_id = "-1";
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabhot_fragment_layout);

        try {
            group_id = this.getIntent().getStringExtra("_id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle bundle = new Bundle();
        bundle.putString(Utils.ARG_GROUP_OR_CLIENT_ID, group_id);
        bundle.putInt(Utils.ARG_GROUP_OR_CLIENT, Utils.FLAG_GROUP);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("My Group")
                        .setIndicator("", getResources().getDrawable(R.drawable.tab_client_group_detail_bg_selector)),
                GroupDrawerFragment.class, bundle
        );
        mTabHost.addTab(mTabHost.newTabSpec("Sessions")
                        .setIndicator("", getResources().getDrawable(R.drawable.tab_session_bg_selector)),
                SessionFragment.class, bundle
        );
        mTabHost.addTab(mTabHost.newTabSpec("Assessments")
                        .setIndicator("", getResources().getDrawable(R.drawable.tab_assessment_bg_selector)),
                PreWorkoutAssessmentFragment.class, bundle
        );
        mTabHost.addTab(mTabHost.newTabSpec("Memberships")
                        .setIndicator("", getResources().getDrawable(R.drawable.tab_membership_bg_selector)),
                MembershipActivityFragment.class, bundle
        );
        mTabHost.addTab(mTabHost.newTabSpec("Personal Training Activity")
                        .setIndicator("", getResources().getDrawable(R.drawable.tab_activity_bg_selector)),
                PersonalTrainingFragment.class, bundle
        );
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                restoreActionBar(tabId);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.client_detail, menu);

        return true;
    }


    public void restoreActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();

        try {
            actionBar.setCustomView(null);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuMembership:
                startActivityForResult(new Intent(this, AddMemberShipActivity.class)
                        .putExtra(Utils.ARG_GROUP_OR_CLIENT, Utils.FLAG_GROUP)
                        .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, group_id), Utils.MEMBERSHIP);
                break;
            case R.id.menuNewSession:
                startActivityForResult(new Intent(this, AddSessionActivity.class)
                        .putExtra(Utils.ARG_GROUP_OR_CLIENT, Utils.FLAG_GROUP)
                        .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, group_id), Utils.SESSION);
                break;
            case R.id.menuNewPreWorkOutAssessment:
                startActivityForResult(new Intent(this, AssessmentFormList.class)
                        .putExtra(Utils.ARG_GROUP_OR_CLIENT, Utils.FLAG_GROUP)
                        .putExtra(Utils.ARG_GROUP_OR_CLIENT_ID, group_id), Utils.PRE_WORKOUT_ASSESSMENT);
                break;
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case Utils.SESSION:
                mTabHost.setCurrentTab(0);
                mTabHost.setCurrentTab(1);
                break;
            case Utils.PRE_WORKOUT_ASSESSMENT:
                mTabHost.setCurrentTab(0);
                mTabHost.setCurrentTab(2);
                break;
            case Utils.MEMBERSHIP:
                mTabHost.setCurrentTab(0);
                mTabHost.setCurrentTab(3);
                break;


        }

    }
}




