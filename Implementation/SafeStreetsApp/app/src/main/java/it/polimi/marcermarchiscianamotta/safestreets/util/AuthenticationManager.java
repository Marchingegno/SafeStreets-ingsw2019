package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import java.util.Arrays;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.R;

public class AuthenticationManager {

    public static boolean isSignedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null;
    }

    public static Intent getLaunchableAuthenticationIntent() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build()/*, new AuthUI.IdpConfig.PhoneBuilder().build()*/);

        // Create sign-in intent using the Firebase UI library.
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground)
                .setIsSmartLockEnabled(false, false)
                .setTosAndPrivacyPolicyUrls(
                        "https://example.com/terms.html",
                        "https://example.com/privacy.html")
                .build();
    }

    public static void signOut(Activity listenerActivity, OnCompleteListener<Void> onCompleteListener) {
        AuthUI.getInstance()
                .signOut(listenerActivity)
                .addOnCompleteListener(listenerActivity, onCompleteListener);
    }

    public static void deleteAccount(Activity listenerActivity, OnCompleteListener<Void> onCompleteListener) {
        AuthUI.getInstance()
                .delete(listenerActivity)
                .addOnCompleteListener(listenerActivity, onCompleteListener);
    }

    public static UserInfo getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getUserUid() {
        return getUser().getUid();
    }
}
