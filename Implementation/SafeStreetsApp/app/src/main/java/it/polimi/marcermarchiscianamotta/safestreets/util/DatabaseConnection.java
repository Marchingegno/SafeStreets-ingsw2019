package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.app.Activity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import it.polimi.marcermarchiscianamotta.safestreets.model.User;
import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReport;

public class DatabaseConnection {

    public static void uploadViolationReport(ViolationReport violationReport, Activity listenerActivity, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Violation reports are inserted in path: /violationReports/
        FirebaseFirestore.getInstance().collection("violationReports").add(violationReport)
                .addOnSuccessListener(listenerActivity, violationReportDocRef -> {
                    addViolationReportReferenceToUser(violationReportDocRef.getId(), listenerActivity, onSuccessListener, onFailureListener);
                })
                .addOnFailureListener(listenerActivity, onFailureListener);
    }

    private static void addViolationReportReferenceToUser(final String violationReportId, Activity listenerActivity, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        final DocumentReference userDocumentRef = FirebaseFirestore.getInstance().collection("users").document(AuthenticationManager.getUserUid());
        FirebaseFirestore.getInstance()
                .runTransaction((Transaction.Function<Void>) transaction -> {
                    // Get user document.
                    DocumentSnapshot userDocumentSnap = transaction.get(userDocumentRef);

                    // Modify user document by adding the new id of the violation report.
                    User user = userDocumentSnap.toObject(User.class);
                    if(user == null)
                        user = new User(violationReportId);
                    else
                        user.addReference(violationReportId);

                    // Update user document.
                    transaction.set(userDocumentRef, user);

                    // Success.
                    return null;
                })
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);



        /*FirebaseFirestore.getInstance().collection("users").document(AuthenticationManager.getUserUid()).get()
                .addOnSuccessListener(listenerActivity, userDocument -> {
                    updateViolationReportReferencesOfUser(userDocument, violationReportId, listenerActivity, onSuccessListener, onFailureListener);
                })
                .addOnFailureListener(listenerActivity, onFailureListener);*/
    }

    /*private static void updateViolationReportReferencesOfUser(DocumentSnapshot userDocument, String violationReportReference, Activity listenerActivity, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        User user = userDocument.toObject(User.class);
        if(user == null)
            user = new User(violationReportReference);
        else
            user.addReference(violationReportReference);

        FirebaseFirestore.getInstance().collection("users").document(AuthenticationManager.getUserUid()).set(user)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(listenerActivity, onFailureListener);
    }*/
}
