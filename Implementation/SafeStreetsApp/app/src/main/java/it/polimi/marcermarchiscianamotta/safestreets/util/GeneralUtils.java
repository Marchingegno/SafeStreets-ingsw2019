package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public final class GeneralUtils {

	/**
	 * Shows a snack bar with the specified message.
	 *
	 * @param containerView   the view where the message needs to be displayed.
	 * @param errorMessageRes the message to be displayed.
	 */
	public static void showSnackbar(View containerView, String errorMessageRes) {
		Snackbar.make(containerView, errorMessageRes, Snackbar.LENGTH_LONG).show();
	}

	public static int convertDpToPixel(int dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		int px;
		px = (int) (dp * metrics.density);
		return px;
	}

	public static int convertPixelsToDp(int px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		int dp;
		dp = (int) (px / metrics.density);
		return dp;
	}
}
