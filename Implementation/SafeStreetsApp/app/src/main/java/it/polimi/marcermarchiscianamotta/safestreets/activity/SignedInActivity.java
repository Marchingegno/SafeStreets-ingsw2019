package it.polimi.marcermarchiscianamotta.safestreets.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.util.Util;

public class SignedInActivity extends AppCompatActivity {

    private static final String TAG = "SignedInActivity";

    @BindView(android.R.id.content) View rootView;

    @BindView(R.id.signed_in_welcome_text) TextView welcomeText;

    @BindView(R.id.user_email) TextView mUserEmail;
    @BindView(R.id.user_display_name) TextView mUserDisplayName;
    @BindView(R.id.user_phone_number) TextView mUserPhoneNumber;
    @BindView(R.id.user_enabled_providers) TextView mEnabledProviders;
    @BindView(R.id.user_is_new) TextView mIsNewUser;


    //region Static methods
    //================================================================================
    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable IdpResponse response) {
        return new Intent().setClass(context, SignedInActivity.class).putExtra(ExtraConstants.IDP_RESPONSE, response);
    }
    //endregion


    //region Overridden methods
    //================================================================================
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(MainActivity.createIntent(this));
            finish();
            return;
        }

        IdpResponse response = getIntent().getParcelableExtra(ExtraConstants.IDP_RESPONSE);

        setContentView(R.layout.activity_signed_in);
        ButterKnife.bind(this); // Needed for @BindView attributes.

        populateProfile(response);
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
        startActivity(SeeViolationsActivity.createIntent(v.getContext()));
    }

    @OnClick(R.id.signed_in_sign_out)
    public void onClickSignOut(View v) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(MainActivity.createIntent(SignedInActivity.this));
                        finish();
                    } else {
                        Log.w(TAG, "signOut:failure", task.getException());
                        Util.showSnackbar(rootView, "Sign out failed");
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
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(MainActivity.createIntent(SignedInActivity.this));
                        finish();
                    } else {
                        Util.showSnackbar(rootView, "Delete account failed");
                    }
                });
    }

    private void populateProfile(@Nullable IdpResponse response) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

        if (response == null) {
            mIsNewUser.setVisibility(View.GONE);
        } else {
            mIsNewUser.setVisibility(View.VISIBLE);
            mIsNewUser.setText(response.isNewUser() ? "New user" : "Existing user");
        }

        List<String> providers = new ArrayList<>();
        if (user.getProviderData().isEmpty()) {
            providers.add("Anonymous");
        } else {
            for (UserInfo info : user.getProviderData()) {
                switch (info.getProviderId()) {
                    case GoogleAuthProvider.PROVIDER_ID:
                        providers.add("Google");
                        break;
                    case EmailAuthProvider.PROVIDER_ID:
                        providers.add("Email");
                        break;
                    case PhoneAuthProvider.PROVIDER_ID:
                        providers.add("Phone");
                        break;
                    case FirebaseAuthProvider.PROVIDER_ID:
                        // Ignore this provider, it's not very meaningful
                        break;
                    default:
                        throw new IllegalStateException(
                                "Unknown provider: " + info.getProviderId());
                }
            }
        }

        mEnabledProviders.setText("Providers used: " + providers.toString());
    }
    //endregion

}
