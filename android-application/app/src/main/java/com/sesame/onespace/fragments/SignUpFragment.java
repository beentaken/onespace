package com.sesame.onespace.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.sesame.onespace.R;
import com.sesame.onespace.views.SoftKeyboardListenedRelativeLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpFragment extends Fragment implements
        SoftKeyboardListenedRelativeLayout.SoftKeyboardListener,
        View.OnClickListener {

    private OnSignupFragmentInteractionListener mListener;

    private SoftKeyboardListenedRelativeLayout keyboardListenedLayout;
    private CoordinatorLayout coordinatorLayout;
    private ImageView iconAppImageview;
    private TextInputLayout userInput;
    private TextInputLayout passInput;
    private TextInputLayout passConfirmInput;
    private Button signupButton;
    private Button alreadyAccountButton;
    private ProgressBar progressBar;
    private ImageButton cancelButton;
    private View footer;
    private Snackbar snackbar;
    private Animation animFadein;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SignUpFragment.
     */
    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        initInstance(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSignupFragmentInteractionListener) {
            mListener = (OnSignupFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSignupFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initInstance(View view) {
        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);

        // logo
        iconAppImageview = (ImageView) view.findViewById(R.id.app_icon);

        // Username field
        userInput = (TextInputLayout) view.findViewById(R.id.text_input_username);

        // Password field
        passInput = (TextInputLayout) view.findViewById(R.id.text_input_password);
        EditText passwordEdittext = passInput.getEditText();

        // Confirm Password field
        passConfirmInput = (TextInputLayout) view.findViewById(R.id.text_input_password_confirm);
        EditText passwordConfirmEdittext = passConfirmInput.getEditText();

        // Login Button
        signupButton = (Button) view.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);

        // Signup Button
        alreadyAccountButton = (Button) view.findViewById(R.id.already_account_button);
        alreadyAccountButton.setOnClickListener(this);

        // ProgressBar
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        // Cancel Button
        cancelButton = (ImageButton) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);

        // Footer
        footer = view.findViewById(R.id.empty_view);

        // Root View
        keyboardListenedLayout = (SoftKeyboardListenedRelativeLayout) view.findViewById(R.id.soft_keyboard_listened_layout);
        keyboardListenedLayout.addSoftKeyboardLsner(this);

        snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);

        // load the fade in animation
        animFadein = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
//        iconAppImageview.setAnimation(animFadein);

    }

    public String getUsername() {
        EditText editText = userInput.getEditText();
        if(editText != null)
            return editText.getText().toString().trim();
        return "";
    }

    public String getPassword() {
        EditText editText = passInput.getEditText();
        if(editText != null)
            return editText.getText().toString().trim();
        return "";
    }

    public String getConfirmPassword() {
        EditText editText = passConfirmInput.getEditText();
        if(editText != null)
            return editText.getText().toString().trim();
        return "";
    }

    public void setUsernameInputEnabled(boolean enabled) {
        EditText editText = userInput.getEditText();
        if(editText != null)
            editText.setEnabled(enabled);
    }

    public void setPasswordInputEnabled(boolean enabled) {
        EditText editText = passInput.getEditText();
        if(editText != null)
            editText.setEnabled(enabled);
    }

    public void setConfirmPasswordInputEnabled(boolean enabled) {
        EditText editText = passConfirmInput.getEditText();
        if(editText != null)
            editText.setEnabled(enabled);
    }

    public void setUsernameError(String msg) {
        userInput.setError(msg);
    }

    public void setPasswordError(String msg) {
        passInput.setError(msg);
    }

    public void setConfirmPasswordError(String msg) {
        passConfirmInput.setError(msg);
    }

    public void usernameRequestFocus() {
        EditText editText = userInput.getEditText();
        if(editText != null)
            editText.requestFocus();
    }

    public void passwordRequestFocus() {
        EditText editText = passInput.getEditText();
        if(editText != null)
            editText.requestFocus();
    }

    public void confirmPasswordRequestFocus() {
        EditText editText = passConfirmInput.getEditText();
        if(editText != null)
            editText.requestFocus();
    }

    public void clearPassword() {
        EditText editText = passInput.getEditText();
        if(editText != null)
            editText.setText("");
    }

    public void clearConfirmPassword() {
        EditText editText = passConfirmInput.getEditText();
        if(editText != null)
            editText.setText("");
    }

    public void showSnackbar(String msg) {
        showSnackbar(msg, null, null);
    }

    public void showSnackbar(String msg, String action, View.OnClickListener listener) {
        snackbar.setText(msg);
        snackbar.setAction(action, listener);
        snackbar.show();
    }

    public void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        cancelButton.setVisibility(show ? View.VISIBLE : View.GONE);
        signupButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        setUsernameInputEnabled(!show);
        setPasswordInputEnabled(!show);
        setConfirmPasswordInputEnabled(!show);
    }

    private void validate() {
        // Reset errors.
        setUsernameError(null);
        setPasswordError(null);
        setConfirmPasswordError(null);

        // Store values at the time of the login attempt.
        String username = getUsername();
        String password = getPassword();
        String confirmPassword = getConfirmPassword();

        // Check for a valid title
        if (TextUtils.isEmpty(username)) {
            setUsernameError(getString(R.string.error_field_required));
            usernameRequestFocus();
            return;
        } else if (!isUsernameValid(username)) {
            setUsernameError(getString(R.string.error_username_invalid));
            usernameRequestFocus();
            return;
        }

        boolean isPasswordValid = isPasswordValid(password);
        if (!isPasswordValid) {
            setPasswordError(getString(R.string.error_password_invalid));
            clearPassword();
            clearConfirmPassword();
            passwordRequestFocus();
            return;
        }

        boolean isPasswordMatch = password.equals(confirmPassword);
        if(!isPasswordMatch) {
            setConfirmPasswordError(getString(R.string.error_password_not_match));
            clearConfirmPassword();
            confirmPasswordRequestFocus();
            return;
        }

        mListener.onSignup(username, password);

    }

    private boolean isUsernameValid(String username) {
        Pattern p = Pattern.compile("^[^\\\\<>]*$");
        Matcher m = p.matcher(username);
        return m.find() && username.length() >= 3;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 3;
    }

    @Override
    public void onSoftKeyboardShow() {
        footer.setVisibility(View.GONE);
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) signupButton.getLayoutParams();
        p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        signupButton.setLayoutParams(p);
    }

    @Override
    public void onSoftKeyboardHide() {
        footer.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) signupButton.getLayoutParams();
        p.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        p.addRule(RelativeLayout.ABOVE, R.id.empty_view);
        signupButton.setLayoutParams(p);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.already_account_button:
                mListener.onOpenLogin();
                break;
            case R.id.cancel_button:
                showProgress(false);
                mListener.onCancel();
                break;
            case R.id.signup_button:
                validate();
                break;
        }
    }

    public interface OnSignupFragmentInteractionListener {
        void onOpenLogin();
        void onSignup(String username, String pasword);
        void onCancel();
    }
}
