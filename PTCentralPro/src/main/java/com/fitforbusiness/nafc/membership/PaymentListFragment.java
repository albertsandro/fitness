package com.fitforbusiness.nafc.membership;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fitforbusiness.database.DBOHelper;
import com.fitforbusiness.database.Table;
import com.fitforbusiness.nafc.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Adeel on 3/1/2015.
 */
public class PaymentListFragment extends Fragment {
    public static String ARG_TYPE = "type";
    private ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_payment_list, container, false);
        lv = (ListView) v.findViewById(R.id.paymentList);
        lv.setAdapter(new PaymentAdapter(getActivity(),
                DBOHelper.getAllPayments(getArguments().getInt(ARG_TYPE))));
        return v;
    }

    public void refresh(){
        lv.setAdapter(new PaymentAdapter(getActivity(),
                DBOHelper.getAllPayments(getArguments().getInt(ARG_TYPE))));
        lv.invalidate();
    }

    class PaymentAdapter extends ArrayAdapter<ContentValues> {

        public PaymentAdapter(Context context, List<ContentValues> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.payment_row, null);
            }
            ContentValues o = getItem(position);
            if (o != null) {
                TextView txtMail = (TextView) v.findViewById(R.id.txtMail);
                TextView txtDesc = (TextView) v.findViewById(R.id.txtDesc);
                TextView txtAmount = (TextView) v.findViewById(R.id.txtAmount);
                TextView txtDate = (TextView) v.findViewById(R.id.txtDate);
                if (txtMail != null) {
                    txtMail.setText(o.get(Table.StripePayment.CUSTOMER_MAIL)==null?
                            "Details Not available" : o.get(Table.StripePayment.CUSTOMER_MAIL).toString());
                }if (txtAmount != null) {
                    txtAmount.setText("$" + (o.get(Table.StripePayment.PACKAGE_TOTAL)==null?
                            "0.0" : o.get(Table.StripePayment.PACKAGE_TOTAL).toString()));
                }if (txtDate != null) {
                    Date d = new Date(o.getAsLong(Table.StripePayment.CREATED) * 1000);
                    String dateStr = new SimpleDateFormat("MM/dd/yy").format(d);
                    txtDate.setText(dateStr);
                }if (txtDesc != null) {
                    txtDesc.setText(o.get(Table.StripePayment.DESCRIPTION)==null?
                            "   " : o.get(Table.StripePayment.DESCRIPTION).toString());
                }

            }
            return v;
        }
    }
}
