package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.firebase.auth.UserInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.util.AuthenticationManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = "MainMenuActivity";

    @BindView(android.R.id.content) View rootView;

    @BindView(R.id.main_menu_welcome_text) TextView welcomeText;

    @BindView(R.id.user_email) TextView mUserEmail;
    @BindView(R.id.user_display_name) TextView mUserDisplayName;
    @BindView(R.id.user_phone_number) TextView mUserPhoneNumber;
    @BindView(R.id.user_is_new) TextView mIsNewUser;


    //region Static methods
    //================================================================================
    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable IdpResponse response) {
        return new Intent().setClass(context, MainMenuActivity.class).putExtra(ExtraConstants.IDP_RESPONSE, response);
    }
    //endregion


    //region Overridden methods
    //================================================================================
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Close activity if user is not signed in (Android launchers can launch activities singularly)
        if(!AuthenticationManager.isSignedIn()) {
            startActivity(StartupActivity.createIntent(this));
            finish();
            return;
        }

        setContentView(R.layout.activity_main_menu);
        ButterKnife.bind(this); // Needed for @BindView attributes.

        // Handle sign-in response.
        IdpResponse response = getIntent().getParcelableExtra(ExtraConstants.IDP_RESPONSE);
        handleResponse(response);
    }
    //endregion


    //region UI methods
    //================================================================================
    @OnClick(R.id.signed_in_report_violation)
    public void onClickReportViolation(View v) {
        startActivity(ReportViolationActivity.createIntent(v.getContext()));
    }

    @OnClick(R.id.signed_in_see_violations)
    public void onClickSeeViolations(View v) {
        startActivity(SafeStreetsDataActivity.createIntent(v.getContext()));
    }

    @OnClick(R.id.signed_in_sign_out)
    public void onClickSignOut(View v) {
        AuthenticationManager.signOut(this, task -> {
            if (task.isSuccessful()) {
                startActivity(StartupActivity.createIntent(this));
                finish();
            } else {
                Log.w(TAG, "onClickSignOut:failure", task.getException());
                GeneralUtils.showSnackbar(rootView, "Sign out failed");
            }
        });
    }

    @OnClick(R.id.signed_in_delete_account)
    public void onClickDeleteAccount(View v) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", (dialogInterface, i) -> deleteAccount())
                .setNegativeButton("No", null)
                .show();
    }
    //endregion


    //region Private methods
    //================================================================================
    private void deleteAccount() {
        AuthenticationManager.deleteAccount(this, task -> {
            if (task.isSuccessful()) {
                startActivity(StartupActivity.createIntent(this));
                finish();
            } else {
                Log.w(TAG, "deleteAccount:failure", task.getException());
                GeneralUtils.showSnackbar(rootView, "Delete account failed");
            }
        });
    }

    private void handleResponse(@Nullable IdpResponse response) {
        UserInfo user = AuthenticationManager.getUser();

        if(TextUtils.isEmpty(user.getEmail())) {
            if(TextUtils.isEmpty(user.getPhoneNumber())) {
                welcomeText.setText("Welcome back");
            } else {
                welcomeText.setText("Welcome back " + user.getPhoneNumber());
            }
        } else {
            welcomeText.setText("Welcome back " + user.getEmail());
        }

        mUserEmail.setText(
                TextUtils.isEmpty(user.getEmail()) ? "No email" : user.getEmail());
        mUserPhoneNumber.setText(
                TextUtils.isEmpty(user.getPhoneNumber()) ? "No phone number" : user.getPhoneNumber());
        mUserDisplayName.setText(
                TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : user.getDisplayName());

        if (response == null)
            mIsNewUser.setText("Existing user with a saved sign-in");
        else
            mIsNewUser.setText(response.isNewUser() ? "New user" : "Existing user");
    }
    //endregion

}
