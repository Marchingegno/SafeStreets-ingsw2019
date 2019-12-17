package it.polimi.marcermarchiscianamotta.safestreets.util;

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
}
