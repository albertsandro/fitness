package com.fitforbusiness.nafc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.appboy.Appboy;
import com.appboy.support.ValidationUtils;
import com.appboy.ui.support.StringUtils;
import com.fitforbusiness.framework.FFBFragment;

public class FeedbackFragment extends FFBFragment {
    //  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, FeedbackFragment.class.getName());
    private static final String ARG_SECTION_NUMBER = "section_number";
    Button mCancelButton;
    Button mSendButton;
    private CheckBox mIsBugCheckBox;
    private EditText mMessageEditText;
    private EditText mEmailEditText;
    private TextWatcher mSendButtonWatcher;
    private View.OnClickListener mCancelListener;
    private View.OnClickListener mSendListener;
    private FeedbackFinishedListener mFeedbackFinishedListener;
    private int mOriginalSoftInputMode;
    private boolean mErrorMessageShown;

    public FeedbackFragment() {

    }

    public static FeedbackFragment newInstance(int section) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
        mSendButtonWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable sequence) {
                if (mErrorMessageShown) {
                    // Only show error messages after the user has clicked the send button at least once.
                    ensureSendButton();
                }
            }
        };
        mCancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
                if (mFeedbackFinishedListener != null) {
                    mFeedbackFinishedListener.onFeedbackFinished();
                }
                clearData();
            }
        };
        mSendListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ensureSendButton()) {
                    hideSoftKeyboard();
                    boolean isBug = mIsBugCheckBox.isChecked();
                    String message = mMessageEditText.getText().toString();
                    String email = mEmailEditText.getText().toString();
                    boolean result = Appboy.getInstance(getActivity()).submitFeedback(email, message, isBug);
                    if (mFeedbackFinishedListener != null) {
                        mFeedbackFinishedListener.onFeedbackFinished();
                    }
                    clearData();
                } else {
                    mErrorMessageShown = true;
                }
            }
        };
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_feedback, container, false);
        mCancelButton = (Button) view.findViewById(R.id.feedback_cancel);
        mSendButton = (Button) view.findViewById(R.id.feedback_send);
        mIsBugCheckBox = (CheckBox) view.findViewById(R.id.feedback_is_bug);
        mMessageEditText = (EditText) view.findViewById(R.id.feedback_message);
        mEmailEditText = (EditText) view.findViewById(R.id.feedback_email);
        mEmailEditText.setText(Appboy.getInstance(getActivity()).getCurrentUser().getUserId());
        mMessageEditText.addTextChangedListener(mSendButtonWatcher);
        mEmailEditText.addTextChangedListener(mSendButtonWatcher);
        mCancelButton.setOnClickListener(mCancelListener);
        mSendButton.setOnClickListener(mSendListener);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Appboy.getInstance(getActivity()).logFeedbackDisplayed();

        Activity activity = getActivity();
        Window window = activity.getWindow();

        // Overriding the soft input mode of the Window so that the Send and Cancel buttons appear above
        // the soft keyboard when either EditText field gains focus. We cache the mode in order to set it
        // back to the original value when the Fragment is paused.
        mOriginalSoftInputMode = window.getAttributes().softInputMode;
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Appboy.getInstance(activity).logFeedbackDisplayed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMessageEditText.removeTextChangedListener(mSendButtonWatcher);
        mEmailEditText.removeTextChangedListener(mSendButtonWatcher);
    }

    public void setFeedbackFinishedListener(FeedbackFinishedListener feedbackFinishedListener) {
        mFeedbackFinishedListener = feedbackFinishedListener;
    }

    private boolean validatedMessage() {
        boolean validMessage = mMessageEditText.getText() != null && !StringUtils.isNullOrBlank(mMessageEditText.getText().toString());
        if (validMessage) {
            mMessageEditText.setError(null);
        } else {
            // Display error message in the message box
            mMessageEditText.setError(getResources().getString(R.string.com_appboy_feedback_form_invalid_message));
        }
        return validMessage;
    }

    private boolean validatedEmail() {
        boolean validEmail = mEmailEditText.getText() != null && !StringUtils.isNullOrBlank(mEmailEditText.getText().toString()) &&
                ValidationUtils.isValidEmailAddress(mEmailEditText.getText().toString());
        if (validEmail) {
            mEmailEditText.setError(null);
        } else {
            // Display error message in the email box
            mEmailEditText.setError(getResources().getString(R.string.com_appboy_feedback_form_invalid_email));
        }
        return validEmail;
    }

    private boolean ensureSendButton() {
        // Both validators should run, so don't short-circuit this AND statement.
        return (validatedMessage() & validatedEmail());
    }

    private void clearData() {
        mEmailEditText.setText(StringUtils.EMPTY_STRING);
        mMessageEditText.setText(StringUtils.EMPTY_STRING);
        mIsBugCheckBox.setChecked(false);
        mErrorMessageShown = false;
        mEmailEditText.setError(null);
        mMessageEditText.setError(null);
    }

    private void hideSoftKeyboard() {
        Activity activity = getActivity();
        activity.getWindow().setSoftInputMode(mOriginalSoftInputMode);

        // Hide keyboard when paused.
        View currentFocusView = activity.getCurrentFocus();

        if (currentFocusView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feedback, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.bSend)
            mSendButton.performClick();
        return super.onOptionsItemSelected(item);
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

    /**
     * Listener to be called after the feedback has been submitted or cancelled. You must set the
     * static listener before creating an instance of the AppboyFeedbackFragment.
     */
    public interface FeedbackFinishedListener {
        void onFeedbackFinished();
    }
}