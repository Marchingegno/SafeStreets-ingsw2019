package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
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

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.model.Cluster;
import it.polimi.marcermarchiscianamotta.safestreets.model.Group;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;
import it.polimi.marcermarchiscianamotta.safestreets.util.MapManager;
import it.polimi.marcermarchiscianamotta.safestreets.util.cloud.DatabaseConnection;
import it.polimi.marcermarchiscianamotta.safestreets.util.interfaces.MapUser;

public class ClusterActivity extends AppCompatActivity implements MapUser {

	private final static String TAG = "ClusterActivity";
	@BindView(R.id.cluster_root)
	View rootView;
	@BindView(R.id.cluster_cards_container)
	LinearLayout cardsContainer;
	private String municipality;
	private int numberOfGroupsRetrievedSoFar;
	private List<Group> groups = new ArrayList<>();
	private Cluster cluster;

	//region Static methods
	//================================================================================
	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, ClusterActivity.class);
	}
	//endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cluster);

		ButterKnife.bind(this);// Needed for @BindView attributes.

		cluster = (Cluster) getIntent().getSerializableExtra("cluster");

		startGettingGroups();

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

	private void startGettingGroups() {
		MapManager.getAddressFromLocation(this, this, new LatLng(cluster.getLatitude(), cluster.getLongitude()));
	}

	private void displayObtainedReports() {
		findViewById(R.id.cluster_loading_view).setVisibility(View.GONE);
		if (groups.size() == 0)
			findViewById(R.id.cluster_no_groups).setVisibility(View.VISIBLE);
		else
			createReportsCards(groups);
	}

	private void createReportsCards(final List<Group> groups) {
		runOnUiThread(() -> {
			for (Group group : groups) {
				ReportCardView card = new ReportCardView(this, this.getLayoutInflater(), group, municipality);
				card.getParentView().setAnimation(getIntroAnimationSet(750 + (25 * groups.size()), 0));
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

	@Override
	public void onLocationFound(LatLng location) {
		//Not used
	}

	@Override
	public void onAddressFound(Address address) {
		if (address != null) {
			municipality = address.getLocality();
			numberOfGroupsRetrievedSoFar = 0;
			for (String groupID : cluster.getGroups()) {
				DatabaseConnection.getGroup(this, groupID, municipality,
						// On success.
						groupResult -> {
							Log.d(TAG, "Retrieved group:" + groupResult.toString());
							numberOfGroupsRetrievedSoFar++;
							groups.add(groupResult);
							if (numberOfGroupsRetrievedSoFar == cluster.numberOfGroups()) {
								displayObtainedReports();
							}
						},
						// On failure.
						e -> {
							findViewById(R.id.cluster_loading_view).setVisibility(View.GONE);
							findViewById(R.id.cluster_connection_error).setVisibility(View.VISIBLE);
							GeneralUtils.showSnackbar(rootView, "Failed to retrieve groups, please try again later.");
							Log.e(TAG, "Failed to retrieve groups", e);
						});
			}
		} else
			Log.d(TAG, "address is null in onAddressFound");

	}
}
