package com.fitforbusiness.nafc.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fitforbusiness.framework.ItemPickListWithImage;
import com.fitforbusiness.framework.FFBFragment;
import com.fitforbusiness.nafc.MainActivity;
import com.fitforbusiness.nafc.R;

import java.util.List;

/**
 * Created by Sanjeet on 27-Jul-14.
 */
public class CalendarFragment extends FFBFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    FragmentTabHost mTabHost;

    public CalendarFragment() {
    }

    public static CalendarFragment newInstance(int section) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tabhot_fragment_layout, container, false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                NativeSync.getAllNativeEvents(getActivity().getApplication());

            }
        }).start();

        mTabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("Month")
                        .setIndicator("Month"),
                CalendarMonthViewFragment.class, null
        );
        mTabHost.addTab(mTabHost.newTabSpec("Week")
                        .setIndicator("Week"),
                CalendarWeekViewFragment.class, null
        );
        mTabHost.addTab(mTabHost.newTabSpec("Day")
                        .setIndicator("Day"),
                CalendarDayViewFragment.class, null
        );
        return rootView;
    }

    public void showAddSessionOption(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.add_session_options, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuClient:
                        startActivityForResult(new Intent(getActivity(), ItemPickListWithImage.class)
                                .putExtra("isClient", true), mTabHost.getCurrentTab());
                        break;
                    case R.id.menuGroup:
                        startActivityForResult(new Intent(getActivity(), ItemPickListWithImage.class)
                                .putExtra("isClient", false), mTabHost.getCurrentTab());
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAddSession:
                showAddSessionOption(getActivity().findViewById(item.getItemId()));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            setHasOptionsMenu(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
