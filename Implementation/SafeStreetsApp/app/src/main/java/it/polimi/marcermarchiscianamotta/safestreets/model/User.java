package it.polimi.marcermarchiscianamotta.safestreets.model;

import androidx.annotation.NonNull;

public class User {

    private String uid;

    public User() {
        // Needed for Firebase
    }

    public User(@NonNull String uid) {
        this.uid = uid;
    }

    @NonNull
    public String getUid() {
        return uid;
    }
}
