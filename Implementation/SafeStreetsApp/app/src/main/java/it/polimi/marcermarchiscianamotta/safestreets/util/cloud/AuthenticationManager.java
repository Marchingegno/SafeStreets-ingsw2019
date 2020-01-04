package it.polimi.marcermarchiscianamotta.safestreets.util.cloud;

import android.app.Activity;
import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;

import java.util.Collections;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.R;

/**
 * Handles the authentication with the cloud.
 *
 * @author Desno365
 */
public class AuthenticationManager {

	/**
	 * Returns true if and only if a session with the server is currently opened.
	 *
	 * @return true if and only if a session with the server is currently opened.
	 */
	public static boolean isSignedIn() {
		FirebaseAuth auth = FirebaseAuth.getInstance();
		return auth.getCurrentUser() != null;
	}

	/**
	 * Returns the launchable intent for authenticating the user.
	 *
	 * @return the launchable intent for authenticating the user.
	 */
	public static Intent getLaunchableAuthenticationIntent() {
		// Choose authentication providers
		// TODO choose to use email or phone.
		List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build()/*, new AuthUI.IdpConfig.PhoneBuilder().build()*/);

		// Create sign-in intent using the Firebase UI library.
		return AuthUI.getInstance()
				.createSignInIntentBuilder()
				.setAvailableProviders(providers)
				.setLogo(R.drawable.ic_launcher)
				.setIsSmartLockEnabled(false, false)
				.setTosAndPrivacyPolicyUrls(
						"https://example.com/terms.html",
						"https://example.com/privacy.html")
				.build();
	}

	/**
	 * Closes the current session.
	 *
	 * @param listenerActivity   the activity that will listen for success or failure events.
	 * @param onCompleteListener the code to execute once disconnected.
	 */
	public static void signOut(Activity listenerActivity, OnCompleteListener<Void> onCompleteListener) {
		AuthUI.getInstance()
				.signOut(listenerActivity)
				.addOnCompleteListener(listenerActivity, onCompleteListener);
	}

	/**
	 * Deletes the current account from the server.
	 *
	 * @param listenerActivity   the activity that will listen for success or failure events.
	 * @param onCompleteListener the code to execute once disconnected.
	 */
	public static void deleteAccount(Activity listenerActivity, OnCompleteListener<Void> onCompleteListener) {
		AuthUI.getInstance()
				.delete(listenerActivity)
				.addOnCompleteListener(listenerActivity, onCompleteListener);
	}

	/**
	 * Returns the info of the current user.
	 *
	 * @return the info of the current user.
	 */
	public static UserInfo getUser() {
		return FirebaseAuth.getInstance().getCurrentUser();
	}

	/**
	 * Returns the Uid of the current user.
	 *
	 * @return the Uid of the current user.
	 */
	public static String getUserUid() {
		return getUser().getUid();
	}
}
