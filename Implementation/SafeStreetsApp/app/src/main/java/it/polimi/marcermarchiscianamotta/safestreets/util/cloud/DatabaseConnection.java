package it.polimi.marcermarchiscianamotta.safestreets.util.cloud;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import it.polimi.marcermarchiscianamotta.safestreets.model.UserRepresentation;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReportRepresentation;
import it.polimi.marcermarchiscianamotta.safestreets.util.GeneralUtils;

import static com.firebase.ui.auth.AuthUI.TAG;

/**
 * Handles the connection with the database.
 */
public class DatabaseConnection {

	//region Public methods
	//================================================================================

	/**
	 * Uploads the violation report passed as parameter to the Database.
	 *
	 * @param violationReportRep the violation report representation to upload.
	 * @param listenerActivity   the activity that will listen for success or failure events.
	 * @param onSuccessListener  the code to execute on success.
	 * @param onFailureListener  the code to execute on failure.
	 */
	public static void uploadViolationReport(final ViolationReportRepresentation violationReportRep, Activity listenerActivity, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {

		// Generate new ID for the violation report.
		final String violationReportId = UUID.randomUUID().toString();

		// Get document references for both the violation report document and the user document.
		final DocumentReference violationReportDocRef = FirebaseFirestore.getInstance().collection("violationReports").document(violationReportId);
		final DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(AuthenticationManager.getUserUid());

		// Transaction that uploads the violation report and add the id to the user document.
		FirebaseFirestore.getInstance()
				.runTransaction((Transaction.Function<Void>) transaction -> {
					// Get user document.
					DocumentSnapshot userDocSnap = transaction.get(userDocRef);

					// Modify user document by adding the new id of the violation report.
					UserRepresentation newUserRepresentation = addViolationReportIdToUser(userDocSnap, violationReportId);

					// Upload violation report.
					transaction.set(violationReportDocRef, violationReportRep);

					// Update user document.
					transaction.set(userDocRef, newUserRepresentation);

					// Success.
					return null;
				})
				.addOnSuccessListener(listenerActivity, onSuccessListener)
				.addOnFailureListener(listenerActivity, onFailureListener);
	}

	/**
	 * Gets all the violation reports made by the current user.
	 *
	 * @param listenerActivity the activity that will listen for success or failure events.
	 * @param onSuccessListener the code to execute on success.
	 * @param onFailureListener the code to execute on failure.
	 */
	public static void getUserViolationReports(Activity listenerActivity, OnSuccessListener<List<ViolationReportRepresentation>> onSuccessListener, OnFailureListener onFailureListener) {
		FirebaseFirestore.getInstance().collection("violationReports")
				.whereEqualTo("userUid", AuthenticationManager.getUserUid())
				.get()
				.addOnSuccessListener(listenerActivity, querySnapshot -> {
					List<ViolationReportRepresentation> reports = new ArrayList<>();
					for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
						ViolationReportRepresentation violationReportRepresentation = documentSnapshot.toObject(ViolationReportRepresentation.class);
						reports.add(violationReportRepresentation);
					}
					onSuccessListener.onSuccess(reports);
				})
				.addOnFailureListener(listenerActivity, onFailureListener);
	}
	//endregion

	//region Private methods
	//================================================================================

	/**
	 * Add the id of the violation report to the user representation which is created from the user document snapshot.
	 *
	 * @param userDocSnap       the user document snapshot.
	 * @param violationReportId the id of the violation report to be added.
	 * @return the user representation of the user document.
	 */
	private static UserRepresentation addViolationReportIdToUser(DocumentSnapshot userDocSnap, String violationReportId) {
		UserRepresentation userRepresentation = userDocSnap.toObject(UserRepresentation.class);
		if (userRepresentation == null) { // If userRepresentation does not have a userRepresentation document.
			userRepresentation = new UserRepresentation();
			userRepresentation.addReport(violationReportId);
		} else {
			userRepresentation.addReport(violationReportId);
		}
		return userRepresentation;
	}
	//endregion

}
