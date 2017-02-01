package com.fitforbusiness.nafc.membership;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.oauth.stripe.StripeSession;
import com.fitforbusiness.nafc.MainActivity;
import com.fitforbusiness.nafc.R;
import com.fitforbusiness.stripe.compat.AsyncTask;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaymentTabsFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ListView paymentList;
    private ArrayList<HashMap<String, Object>> mapArrayList;
    private ViewPager mViewPager;
    private PagerAdapter pagerAdapter;
    private PaymentListFragment[] fragments;
    private Button btnGrp;
    private Button btnClts;

    public static Fragment newInstance(int section) {
        PaymentTabsFragment fragment = new PaymentTabsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section);
        fragment.setArguments(args);

        return fragment;
    }

    public PaymentTabsFragment() {
        // Required empty public constructor
        fragments = new PaymentListFragment[2];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_payment_tabs, container, false);

//        if (DBOHelper.isStripePaymentsEmpty()) {
          downloadAll.execute();
//        } else{
//            long time = DBOHelper.getLastCreatedPayment();
//            Log.d("details", "last time was = " + time);
//            downloadNew.execute(time);
//        }

        pagerAdapter =
                new PagerAdapter(
                        getActivity().getSupportFragmentManager());
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mViewPager.setAdapter(pagerAdapter);

        btnGrp = (Button) rootView.findViewById(R.id.btnGroups);
        btnClts = (Button) rootView.findViewById(R.id.btnClients);

        btnGrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
            }
        });btnClts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        switch (position){
                            case 0:
                                btnGrp.setSelected(true);
                                btnClts.setSelected(false);
                                break;
                            case 1:
                                btnGrp.setSelected(false);
                                btnClts.setSelected(true);
                                break;
                        }
                    }
                });
        btnGrp.setSelected(true);
        btnClts.setSelected(false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    public void refresh(){
        for (PaymentListFragment f : fragments){
            f.refresh();
        }
    }

//    ______________________  Pager Adapter ___________________

    public class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new PaymentListFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(PaymentListFragment.ARG_TYPE, i + 1);
            fragment.setArguments(args);
            fragments[i] = (PaymentListFragment) fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

//    __________________ Async Tasks ______________

    AsyncTask<String, String, String> downloadAll = new AsyncTask<String, String, String>() {
        @Override
        protected String doInBackground(String... params) {
            String a = new StripeSession(getActivity(), "StripeAccount").getAccessToken();
            Log.d("details", "> token & id = " + a);
            if (a == null) return null;
            Stripe.apiKey = a;
            Map<String, Object> chargeParams = new HashMap<String, Object>();
//            chargeParams.put("limit", 20);
//            chargeParams.put("created[gte]", 1423859210);

            try {
                boolean has_next;
                Charge last = null;
                do {
                    Log.d("details", "do while loop");
                    ChargeCollection chargeCollection = Charge.all(chargeParams, a);
                    has_next = chargeCollection.getHasMore();
                    Log.d("details", "list_count = " + chargeCollection.getData().size());
                    for (int i = 0; i < chargeCollection.getData().size(); i++) {
                        Log.d("details", chargeCollection.getData().get(i).toString());
                        if (!DBOHelper.paymentExists(chargeCollection.getData().get(i).getId()))
                            DBOHelper.insertToStripePayment(chargeCollection.getData().get(i));
                        last = chargeCollection.getData().get(i);
                    }
                    if (last != null && has_next)
                        chargeParams.put("starting_after", last.getId());
                }while (has_next);
            } catch (AuthenticationException e) {
                e.printStackTrace();
                Log.d("details",e.toString());
            } catch (InvalidRequestException e) {
                e.printStackTrace();
                Log.d("details",e.toString());
            } catch (APIConnectionException e) {
                e.printStackTrace();
                Log.d("details",e.toString());
            } catch (CardException e) {
                e.printStackTrace();
                Log.d("details",e.toString());
            } catch (APIException e) {
                e.printStackTrace();
                Log.d("details",e.toString());
            }
            Log.d("details", "end");
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            refresh();
        }
    };

//    AsyncTask<Long, String, String> downloadNew = new AsyncTask<Long, String, String>() {
//        @Override
//        protected String doInBackground(Long... params) {
//            String a = new StripeSession(getActivity(), "StripeAccount").getAccessToken();
//            Log.d("details", "> token & id = " + a);
//            Stripe.apiKey = ApplicationData.SECRET_KEY;
//
//            Map<String, Object> chargeParams = new HashMap<String, Object>();
//
//            chargeParams.put("created[gt]", params[0]);
//            Log.d("details", "get after = " + params[0]);
//
//            try {
//                boolean has_next;
//                Charge last = null;
//                do {
//                    Log.d("details", "do while loop");
//                    ChargeCollection chargeCollection = Charge.all(chargeParams, a);
//                    has_next = chargeCollection.getHasMore();
//                    Log.d("details", "list_count = " + chargeCollection.getData().size());
//                    for (int i = 0; i < chargeCollection.getData().size(); i++) {
//                        Log.d("details", chargeCollection.getData().get(i).toString());
//                        DBOHelper.insertToStripePayment(chargeCollection.getData().get(i));
//                        last = chargeCollection.getData().get(i);
//                    }
//                    if (last != null && has_next)
//                        chargeParams.put("starting_after", last.getId());
//                }while (has_next);
//            } catch (AuthenticationException e) {
//                e.printStackTrace();
//                Log.d("details",e.toString());
//            } catch (InvalidRequestException e) {
//                e.printStackTrace();
//                Log.d("details",e.toString());
//            } catch (APIConnectionException e) {
//                e.printStackTrace();
//                Log.d("details",e.toString());
//            } catch (CardException e) {
//                e.printStackTrace();
//                Log.d("details",e.toString());
//            } catch (APIException e) {
//                e.printStackTrace();
//                Log.d("details",e.toString());
//            }
//            Log.d("details", "end");
//            return null;
//        }
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            refresh();
//        }
//    };
}
