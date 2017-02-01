package com.fitforbusiness.nafc.dashboard;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fitforbusiness.framework.Utils;
import com.fitforbusiness.framework.FFBFragment;
import com.fitforbusiness.nafc.MainActivity;
import com.fitforbusiness.nafc.R;

/**
 * Created by Sanjeet on 27-Jul-14.
 */
public class HomeFragment extends FFBFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    FragmentTabHost mTabHost;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(int section) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tabhot_fragment_layout, container, false);
        Button button = new Button(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                (int) Utils.convertPixelToDensityIndependentPixels(getActivity(), 100));
        button.setLayoutParams(layoutParams);
        button.setBackgroundResource(R.drawable.home_selected);
        mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("Dashboard")
                        .setIndicator("", getResources().getDrawable(R.drawable.tab_home_bg_selector)),
                //  .setIndicator(button),
                DashBoardFragment.class, null
        );
        mTabHost.addTab(mTabHost.newTabSpec("Notification")
                        .setIndicator("", getResources().getDrawable(R.drawable.tab_news_feed_bg_selector)),
                NewsFeedsFragment.class, null
        );
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

}
