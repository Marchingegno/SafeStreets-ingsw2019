package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.firebase.auth.UserInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.controller.AuthenticationManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;

/**
 * Main menu activity.
 *
 * @author Desno365
 */
public class MainMenuActivity extends AppCompatActivity {

	private static final String TAG = "MainMenuActivity";

	@BindView(android.R.id.content)
	View rootView;
	@BindView(R.id.main_menu_welcome_text)
	TextView welcomeText;
	@BindView(R.id.signed_in_report_violation)
	LinearLayout reportViolationButton;


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

		setContentView(R.layout.activity_main_menu);
		ButterKnife.bind(this); // Needed for @BindView attributes.

		// Display welcome text.
		displayWelcomeText();

		// Debug sign-in response.
		IdpResponse response = getIntent().getParcelableExtra(ExtraConstants.IDP_RESPONSE);
		debugSignInResponse(response);

		//If the device doesn't have a camera it could not post any report
		if (!GeneralUtils.hasCamera(this))
			reportViolationButton.setVisibility(View.GONE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Add items to the action bar.
		getMenuInflater().inflate(R.menu.menu_main_menu_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.main_menu_action_sign_out:
				signOut();
				return true;

			case R.id.main_menu_action_settings:
				startActivity(SettingsActivity.createIntent(this));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
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
		startActivity(MapActivity.createIntent(v.getContext()));
	}

	@OnClick(R.id.signed_in_my_reports)
	public void onClickMyReports(View v) {
		startActivity(MyReportsActivity.createIntent(v.getContext()));
	}
	//endregion


	//region Private methods
	//================================================================================
	private void signOut() {
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

	private void displayWelcomeText() {
		UserInfo user = AuthenticationManager.getUser();
		if (TextUtils.isEmpty(user.getEmail())) {
			if (TextUtils.isEmpty(user.getPhoneNumber()))
				welcomeText.setText("Welcome back");
			else
				welcomeText.setText("Welcome back " + user.getPhoneNumber());
		} else {
			welcomeText.setText("Welcome back " + user.getEmail());
		}
	}

	private void debugSignInResponse(@Nullable IdpResponse response) {
		if (response == null)
			Log.i(TAG, "Existing user with a saved sign-in");
		else
			Log.i(TAG, response.isNewUser() ? "New user" : "Existing user");
	}
	//endregion

}
