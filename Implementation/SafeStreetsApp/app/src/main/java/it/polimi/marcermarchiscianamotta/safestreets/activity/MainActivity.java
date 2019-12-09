package it.polimi.marcermarchiscianamotta.safestreets.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.util.Util;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /**
     * Custom request code
     */
    private static final int RC_SIGN_IN = 123;

    @BindView(R.id.main_root) View rootView;


    //region Static methods
    //================================================================================
    /**
     * Create intent for launching this activity.
     * @param context context from which to launch the activity.
     * @return intent to launch.
     */
    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }
    //endregion


    //region Overridden methods
    //================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this); // Needed for @BindView attributes.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If user is signed in then go to the signed in activity.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && getIntent().getExtras() == null) {
            startSignedInActivity(null);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If result of a sign-in is received then process it.
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }
    //endregion


    //region UI
    //================================================================================
    @OnClick(R.id.main_button_sign_in)
    public void onClickSignIn(View v)
    {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build()/*, new AuthUI.IdpConfig.PhoneBuilder().build()*/);

        // Create and launch sign-in intent using the Firebase UI library.
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ic_launcher_foreground)
                        .setIsSmartLockEnabled(false, false)
                        .setTosAndPrivacyPolicyUrls(
                                "https://example.com/terms.html",
                                "https://example.com/privacy.html")
                        .build(),
                RC_SIGN_IN);
    }
    //endregion


    //region Private methods
    //================================================================================
    private void handleSignInResponse(int resultCode, @Nullable Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // If successfully signed in then start sing in activity, otherwise sign in failed
        if (resultCode == RESULT_OK) {
            startSignedInActivity(response);
            finish();
        } else {
            if (response == null) {
                // User pressed back button
                Util.showSnackbar(rootView, "Sign in cancelled");
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                Util.showSnackbar(rootView, "No Internet Connection");
                return;
            }

            Util.showSnackbar(rootView, "An unknown error occurred");
            Log.e(TAG, "Sign-in error: ", response.getError());
        }
    }

    private void startSignedInActivity(@Nullable IdpResponse response) {
        startActivity(SignedInActivity.createIntent(this, response));
    }
    //endregion
}
