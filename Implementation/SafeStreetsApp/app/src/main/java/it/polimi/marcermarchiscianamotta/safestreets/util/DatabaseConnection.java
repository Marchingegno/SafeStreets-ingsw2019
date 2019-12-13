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
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReport;

public class DatabaseConnection {

    public static void uploadViolationReport(final ViolationReport violationReport, Activity listenerActivity, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        final String violationReportId = UUID.randomUUID().toString();
        final DocumentReference violationReportDocRef = FirebaseFirestore.getInstance().collection("violationReports").document(violationReportId);
        final DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(AuthenticationManager.getUserUid());
        FirebaseFirestore.getInstance()
                .runTransaction((Transaction.Function<Void>) transaction -> {
                    // Get user document.
                    DocumentSnapshot userDocumentSnap = transaction.get(userDocRef);

                    // Modify user document by adding the new id of the violation report.
                    User user = userDocumentSnap.toObject(User.class);
                    if(user == null) // If user does not have a user document.
                        user = new User(violationReportId);
                    else
                        user.addReference(violationReportId);

                    // Upload violation report.
                    transaction.set(violationReportDocRef, violationReport);

                    // Update user document.
                    transaction.set(userDocRef, user);

                    // Success.
                    return null;
                })
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

}
