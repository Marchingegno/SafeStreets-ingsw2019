package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.app.Activity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.UUID;

import it.polimi.marcermarchiscianamotta.safestreets.model.User;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReportRepresentation;

public class DatabaseConnection {

    //region Public methods
    //================================================================================
    /**
     * Uploads the violation report passed as parameter to the Database.
     * @param violationReportRep the violation report representation to upload.
     * @param listenerActivity the activity that will listen for success or failure events.
     * @param onSuccessListener the listener for a success event.
     * @param onFailureListener the listener for a failure event.
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
                    User newUser = addViolationReportIdToUser(userDocSnap, violationReportId);

                    // Upload violation report.
                    transaction.set(violationReportDocRef, violationReportRep);

                    // Update user document.
                    transaction.set(userDocRef, newUser);

                    // Success.
                    return null;
                })
                .addOnSuccessListener(listenerActivity, onSuccessListener)
                .addOnFailureListener(listenerActivity, onFailureListener);
    }
    //endregion


    //region Private methods
    //================================================================================
    private static User addViolationReportIdToUser(DocumentSnapshot userDocSnap, String violationReportId) {
        User user = userDocSnap.toObject(User.class);
        if(user == null) // If user does not have a user document.
            user = new User(violationReportId);
        else
            user.addReference(violationReportId);
        return user;
    }
    //endregion

}
