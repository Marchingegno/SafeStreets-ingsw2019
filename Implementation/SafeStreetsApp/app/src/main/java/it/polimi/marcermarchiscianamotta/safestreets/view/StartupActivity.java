package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.AuthenticationManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;

/**
 * Handles the login.
 *
 * @author Desno365
 */
public class StartupActivity extends AppCompatActivity {

    private static final String TAG = "StartupActivity";

    /**
     * Custom request code
     */
    private static final int RC_SIGN_IN = 123;

    @BindView(R.id.startup_root) View rootView;


    //region Static methods
    //================================================================================

    /**
     * Create intent for launching this activity.
     * @param context context from which to launch the activity.
     * @return intent to launch.
     */
    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, StartupActivity.class);
    }
    //endregion


    //region Overridden methods
    //================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_startup);
        ButterKnife.bind(this); // Needed for @BindView attributes.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If user is signed in then go directly to the main menu activity.
        if (AuthenticationManager.isSignedIn() && getIntent().getExtras() == null) {
            launchMainMenuActivity(null);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If a result of a sign-in is received then process it.
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }
    //endregion


    //region UI
    //================================================================================
    @OnClick(R.id.startup_button_sign_in)
    public void onClickSignIn(View v) {
        // Launch sign-in intent. The intent will give a result to onActivityResult() using the request code RC_SIGN_IN.
        startActivityForResult(AuthenticationManager.getLaunchableAuthenticationIntent(), RC_SIGN_IN);
    }
    //endregion


    //region Private methods
    //================================================================================
    private void handleSignInResponse(int resultCode, @Nullable Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // If successfully signed in then start main menu activity, otherwise the sign-in failed
        if (resultCode == RESULT_OK) {
            launchMainMenuActivity(response);
            finish();
        } else {
            if (response == null || response.getError() == null) {
                // User pressed back button
                GeneralUtils.showSnackbar(rootView, "Sign in cancelled");
            } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                GeneralUtils.showSnackbar(rootView, "No Internet Connection");
            } else {
                GeneralUtils.showSnackbar(rootView, "An unknown error occurred");
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    private void launchMainMenuActivity(@Nullable IdpResponse response) {
        startActivity(MainMenuActivity.createIntent(this, response));
    }
    //endregion
}
