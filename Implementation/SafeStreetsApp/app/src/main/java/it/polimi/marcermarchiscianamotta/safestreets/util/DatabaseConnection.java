package it.polimi.marcermarchiscianamotta.safestreets.util;

import android.app.Activity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import it.polimi.marcermarchiscianamotta.safestreets.model.ViolationReport;

public class DatabaseConnection {

    public static void uploadViolationReport(ViolationReport violationReport, Activity listenerActivity, OnSuccessListener<DocumentReference> onSuccessListener, OnFailureListener onFailureListener) {
        // Violation reports are inserted in path: /violationReports/
        FirebaseFirestore.getInstance().collection("violationReports").add(violationReport)
                .addOnSuccessListener(listenerActivity, onSuccessListener)
                .addOnFailureListener(listenerActivity, onFailureListener);
        // TODO add reference of violation report to user
        //FirebaseFirestore.getInstance().collection("users").document(AuthenticationManager.getUserUid()).collection("violationReports").add(violationReport)

    }
}
