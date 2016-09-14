package com.sesame.onespace.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.sesame.onespace.R;
import com.sesame.onespace.views.SoftKeyboardListenedRelativeLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chongos on 8/28/15 AD.
 */
public class LoginFragment extends Fragment implements
        SoftKeyboardListenedRelativeLayout.SoftKeyboardListener,
        View.OnClickListener, View.OnFocusChangeListener {

    private OnLoginFragmentInteractionListener mListener;

    private SoftKeyboardListenedRelativeLayout keyboardListenedLayout;
    private CoordinatorLayout coordinatorLayout;
    private ImageView iconAppImageview;
    private TextInputLayout userInput;
    private TextInputLayout passInput;
    private Button loginButton;
    private Button signupButton;
    private ProgressBar progressBar;
    private ImageButton cancelButton;
    private View footer;
    private Snackbar snackbar;
    private Animation animFadein;

    public LoginFragment() {

    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initInstance(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnLoginFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
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

        // Username field wrapper
        userInput = (TextInputLayout) view.findViewById(R.id.text_input_username);
        userInput.getEditText().setOnFocusChangeListener(this);

        // Password field wrapper
        passInput = (TextInputLayout) view.findViewById(R.id.text_input_password);
        passInput.getEditText().setOnFocusChangeListener(this);

        // Login Button
        loginButton = (Button) view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        // Signup Button
        signupButton = (Button) view.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);

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

    public void setUsernameError(String msg) {
        userInput.setError(msg);
    }

    public void setPasswordError(String msg) {
        passInput.setError(msg);
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

    public void clearPassword() {
        EditText editText = passInput.getEditText();
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
        loginButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        setUsernameInputEnabled(!show);
        setPasswordInputEnabled(!show);
    }

    private void validate() {
        // Reset errors.
        setUsernameError(null);
        setPasswordError(null);

        // Store values at the time of the login attempt.
        String username = getUsername();
        String password = getPassword();

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

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            setPasswordError(getString(R.string.error_password_invalid));
            passwordRequestFocus();
            return;
        }

        mListener.onLogin(username, password);

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
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) loginButton.getLayoutParams();
        p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        loginButton.setLayoutParams(p);
    }

    @Override
    public void onSoftKeyboardHide() {
        footer.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) loginButton.getLayoutParams();
        p.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        p.addRule(RelativeLayout.ABOVE, R.id.empty_view);
        loginButton.setLayoutParams(p);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                validate();
                break;
            case R.id.signup_button:
                mListener.onOpenSignup();
                break;
            case R.id.cancel_button:
                showProgress(false);
                mListener.onCancel();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus && snackbar.isShown())
            snackbar.dismiss();
    }

    public interface OnLoginFragmentInteractionListener {
        void onLogin(String username, String password);
        void onOpenSignup();
        void onCancel();
    }
}
