package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public final class Util {

    public static void showSnackbar(View containerView, String errorMessageRes) {
        Snackbar.make(containerView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
}
