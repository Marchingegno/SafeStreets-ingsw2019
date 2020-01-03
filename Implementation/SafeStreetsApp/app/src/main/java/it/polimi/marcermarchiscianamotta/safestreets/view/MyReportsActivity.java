package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReportRepresentation;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.DatabaseConnection;

/**
 * Displays the reports of the user.
 *
 * @author Desno365
 */
public class MyReportsActivity extends AppCompatActivity {

	//Log tag
	private static final String TAG = "MyReportsActivity";

	//Constants
	final long MIN_LOADING_DISPLAY_TIME = 500; // 0.5 seconds.

	//UI
	@BindView(R.id.my_reports_root)
	View rootView;
	@BindView(R.id.my_reports_cards_container)
	LinearLayout cardsContainer;

	//region Static methods
	//================================================================================
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, MyReportsActivity.class);
	}
	//endregion

	//region Overridden methods
	//================================================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_my_reports);
		ButterKnife.bind(this); // Needed for @BindView attributes.

		// Obtain content.
		startGettingReports();

		// Add back button to action bar.
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


	//region Private methods
	//================================================================================
	private void startGettingReports() {
		final Activity activity = this;
		final long startTime = System.currentTimeMillis();
		DatabaseConnection.getUserViolationReports(activity,
				// On success.
				reportsResult -> {
					final long endTime = System.currentTimeMillis();
					final long time = (endTime - startTime);
					Log.i(TAG, "Getting the reports took " + time + " milliseconds");
					if (time < MIN_LOADING_DISPLAY_TIME) {
						new android.os.Handler().postDelayed(() -> runOnUiThread(() -> displayObtainedReports(activity, reportsResult)), MIN_LOADING_DISPLAY_TIME - time);
					} else {
						displayObtainedReports(activity, reportsResult);
					}
				},
				// On failure.
				e -> {
					findViewById(R.id.my_reports_loading_view).setVisibility(View.GONE);
					findViewById(R.id.my_reports_connection_error).setVisibility(View.VISIBLE);
					GeneralUtils.showSnackbar(rootView, "Failed to retrieve reports, please try again later.");
					Log.e(TAG, "Failed to retrieve reports", e);
				});
	}

	private void displayObtainedReports(Activity activity, List<ViolationReportRepresentation> reportsResult) {
		findViewById(R.id.my_reports_loading_view).setVisibility(View.GONE);
		if (reportsResult.size() == 0)
			findViewById(R.id.my_reports_no_reports).setVisibility(View.VISIBLE);
		else
			createReportsCards(activity, reportsResult);
	}

	private void createReportsCards(final Activity activity, final List<ViolationReportRepresentation> reports) {
		runOnUiThread(() -> {
			for (ViolationReportRepresentation report : reports) {
				ReportCardView card = new ReportCardView(activity.getApplicationContext(), activity.getLayoutInflater(), report.getUploadTimestamp(), report.getMunicipality(), report.getReportStatus(), report.getStatusMotivation());
				card.getParentView().setAnimation(getIntroAnimationSet(750 + (25 * reports.size()), 0));
				cardsContainer.addView(card.getParentView());
				card.getParentView().animate();
			}
		});
	}

	private AnimationSet getIntroAnimationSet(int duration, int startOffset) {
		AlphaAnimation animation1 = new AlphaAnimation(0, 1);

		TranslateAnimation animation2 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_PARENT, -1,
				Animation.RELATIVE_TO_SELF, 0);

		final AnimationSet set = new AnimationSet(false);
		set.addAnimation(animation1);
		set.addAnimation(animation2);
		set.setDuration(duration);
		set.setStartOffset(startOffset);

		return set;
	}
	//endregion
}
