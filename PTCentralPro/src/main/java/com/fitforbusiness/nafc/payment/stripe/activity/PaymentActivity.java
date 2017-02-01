package com.fitforbusiness.nafc.payment.stripe.activity;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.fitforbusiness.nafc.R;
import com.fitforbusiness.nafc.payment.stripe.PaymentForm;
import com.fitforbusiness.nafc.payment.stripe.TokenList;
import com.fitforbusiness.nafc.payment.stripe.dialog.ErrorDialogFragment;
import com.fitforbusiness.nafc.payment.stripe.dialog.ProgressDialogFragment;
import com.fitforbusiness.stripe.Stripe;
import com.fitforbusiness.stripe.TokenCallback;
import com.fitforbusiness.stripe.model.Card;
import com.fitforbusiness.stripe.model.Token;


public class PaymentActivity extends FragmentActivity {

    /*
     * Change this to your publishable key.
     *
     * You can get your key here: https://manage.stripe.com/account/apikeys
     */
    //  public static final String PUBLISHABLE_KEY = "pk_test_6pRNASCoBOKtIshFeQd4XMUh";
    public static final String PUBLISHABLE_KEY = "pk_test_lD1GnJcc5x9UCdRpf7Z9nlxq";
    private ProgressDialogFragment progressFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);

        progressFragment = ProgressDialogFragment.newInstance(R.string.progressMessage);
    }

    public void saveCreditCard(PaymentForm form) {

        Card card = new Card(
                form.getCardNumber(),
                form.getExpMonth(),
                form.getExpYear(),
                form.getCvc());

        boolean validation = card.validateCard();
        if (validation) {
            startProgress();
            new Stripe().createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            getTokenList().addToList(token);
                            finishProgress();
                        }

                        public void onError(Exception error) {
                            handleError(error.getLocalizedMessage());
                            finishProgress();
                        }
                    }
            );
        } else if (!card.validateNumber()) {
            handleError("The card number that you entered is invalid");
        } else if (!card.validateExpiryDate()) {
            handleError("The expiration date that you entered is invalid");
        } else if (!card.validateCVC()) {
            handleError("The CVC code that you entered is invalid");
        } else {
            handleError("The card details that you entered are invalid");
        }
    }

    private void startProgress() {
        progressFragment.show(getSupportFragmentManager(), "progress");
    }

    private void finishProgress() {
        progressFragment.dismiss();
    }

    private void handleError(String error) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(R.string.validationErrors, error);
        fragment.show(getSupportFragmentManager(), "error");
    }

    private TokenList getTokenList() {
        return (TokenList) (getSupportFragmentManager().findFragmentById(R.id.token_list));
    }
}
