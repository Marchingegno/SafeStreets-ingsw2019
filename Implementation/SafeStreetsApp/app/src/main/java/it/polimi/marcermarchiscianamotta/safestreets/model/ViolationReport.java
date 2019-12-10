package it.polimi.marcermarchiscianamotta.safestreets.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ViolationReport {

    private String userUid;
    private int violationType;
    private String description;
    private List<String> pictures;
    private String licensePlate;
    private Date uploadTimestamp;
    private ReportStatus reportStatus = ReportStatus.SUBMITTED;

    //Coordinates
    private double latitude;
    private double longitude;


    public ViolationReport() {
        // Needed for Firebase
    }

    public ViolationReport(@NonNull String userUid, int violationType, @Nullable String description, @NonNull List<String> pictures, @NonNull String licensePlate, double latitude, double longitude) {
        this.userUid = userUid;
        this.violationType = violationType;
        this.description = description;
        this.pictures = pictures;
        this.licensePlate = licensePlate;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    @NonNull
    public String getUserUid() {
        return userUid;
    }

    public int getViolationType() {
        return violationType;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @NonNull
    public List<String> getPictures() {
        return pictures;
    }

    @NonNull
    public String getLicensePlate() {
        return licensePlate;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @ServerTimestamp
    @NonNull
    public Date getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(@NonNull Date uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    @NonNull
    public ReportStatus getReportStatus() {
        return reportStatus;
    }
}
