package it.polimi.marcermarchiscianamotta.safestreets.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Date;

import it.polimi.marcermarchiscianamotta.safestreets.R;
import it.polimi.marcermarchiscianamotta.safestreets.model.Group;
import it.polimi.marcermarchiscianamotta.safestreets.model.ReportStatusEnum;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;

/**
 * Used to display a list of view. In this case list of groups and list of reports.
 *
 * @author Desno365
 * @author Marcer
 */
public class ReportCardView {

	private Context context;
	private View parentView;

	//region Constructors
	//================================================================================
	public ReportCardView(Context context, LayoutInflater layoutInflater, Date timestamp, String municipality, ReportStatusEnum status, @Nullable String statusMotivation) {
		this.context = context;

		// Create card.
		parentView = layoutInflater.inflate(R.layout.view_card_report, null); // this fixes the "Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag" error when clicking links

		// Set card content.
		((TextView) parentView.findViewById(R.id.card_report_timestamp)).setText("🗓 " + timestamp.toLocaleString());
		((TextView) parentView.findViewById(R.id.card_report_municipality)).setText("📍 " + municipality);
		((TextView) parentView.findViewById(R.id.card_report_status)).setText("Status: " + status.toString());
		if (statusMotivation != null)
			((TextView) parentView.findViewById(R.id.card_extra_text)).setText("Motivation: " + statusMotivation);
		else
			parentView.findViewById(R.id.card_extra_text).setVisibility(View.GONE);
	}

	public ReportCardView(Context context, LayoutInflater layoutInflater, Group group, String municipality) {
		this.context = context;

		// Create card.
		parentView = layoutInflater.inflate(R.layout.view_card_report, null); // this fixes the "Calling startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag" error when clicking links

		// Set card content.
		((TextView) parentView.findViewById(R.id.card_report_timestamp)).setText("🗓 " + group.getLastTimestamp().toLocaleString());
		((TextView) parentView.findViewById(R.id.card_report_municipality)).setText("📍 " + municipality);
		((TextView) parentView.findViewById(R.id.card_report_status)).setText("Status: " + group.getGroupStatus());
		((TextView) parentView.findViewById(R.id.card_extra_text)).setText("Type of violation: " + group.getTypeOfViolation());
	}
	//endregion

	//region Public methods
	//================================================================================
	public View getParentView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(context.getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal), context.getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal), context.getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal), context.getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal) + GeneralUtils.convertDpToPixel(2, context));
		parentView.setLayoutParams(params);
		return parentView;
	}
	//endregion
}
