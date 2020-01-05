package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.AuthenticationManager;

/**
 * The Settings menu.
 *
 * @author Desno 365
 */
public class SettingsActivity extends AppCompatActivity {

	private static final String TAG = "SettingsActivity";

	//region Static methods
	//================================================================================
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, SettingsActivity.class);
	}
	//endregion


	//region Overridden methods
	//================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings, new SettingsFragment())
				.commit();

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	//endregion


	//region SettingsFragment
	//================================================================================
	public static class SettingsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings_preferences, rootKey);
			setPreferenceBehaviour();
		}

		private void setPreferenceBehaviour() {
			Preference deleteAccountPreference = findPreference("settings_delete_account");
			assert deleteAccountPreference != null;
			deleteAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					new AlertDialog.Builder(preference.getContext())
							.setMessage("Are you sure you want to delete this account?")
							.setPositiveButton("Yes, nuke it!", (dialogInterface, i) -> deleteAccount())
							.setNegativeButton("No", null)
							.show();
					return false;
				}
			});
		}

		private void deleteAccount() {
			final Activity activity = getActivity();
			assert activity != null;
			AuthenticationManager.deleteAccount(activity, task -> {
				if (task.isSuccessful()) {
					startActivity(StartupActivity.createIntent(activity));
					activity.finish();
				} else {
					Log.w(TAG, "deleteAccount:failure", task.getException());
					GeneralUtils.showSnackbar(activity.findViewById(R.id.settings_root), "Delete account failed");
				}
			});
		}
	}
	//endregion
}