package it.polimi.marcermarchiscianamotta.safestreets.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;

public class ViolationReport {

    private User user;
    private int violationType;
    private String description;
    private ArrayList<String> pictures;
    private String licensePlate;
    private Date timestamp;
    private String location;

    //Coordinates

    private double latitude;
    private double longitude;
    public ViolationReport() {
        // Needed for Firebase
    }

    public ViolationReport(@NonNull User user, @NonNull int violationType, @Nullable String description, @NonNull ArrayList<String> pictures, @NonNull String licensePlate, @NonNull String location) {
        this.user = user;
        this.violationType = violationType;
        this.description = description;
        this.pictures = pictures;
        this.licensePlate = licensePlate;
        this.location = location;
    }

    public ViolationReport(@NonNull User user, @NonNull int violationType, @Nullable String description, @NonNull ArrayList<String> pictures, @NonNull String licensePlate, @NonNull double latitude, @NonNull double longitude) {
        this.user = user;
        this.violationType = violationType;
        this.description = description;
        this.pictures = pictures;
        this.licensePlate = licensePlate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    public User getUser() {
        return user;
    }

    @NonNull
    public int getViolationType() {
        return violationType;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @NonNull
    public ArrayList<String> getPictures() {
        return pictures;
    }

    @NonNull
    public String getLicensePlate() {
        return licensePlate;
    }

    @NonNull
    public double getLatitude() {
        return latitude;
    }

    @NonNull
    public double getLongitude() {
        return longitude;
    }

    @ServerTimestamp
    @NonNull
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@NonNull Date timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    public String getLocation() {
        return location;
    }
}
