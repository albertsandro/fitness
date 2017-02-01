package com.fitforbusiness.framework;

import android.support.v4.app.Fragment;

import com.appboy.Appboy;

/**
 * Created by Sanjeet on 22-Aug-14.
 */
public class FFBFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();
        Appboy.getInstance(getActivity()).requestSlideupRefresh();
    }
}
