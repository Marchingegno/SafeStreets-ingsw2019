package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViolationReportRepresentation {
	private static final String TAG = "ViolationReportRep";

	private String userUid;
	private String licencePlate;
	private String description;
	private Double latitude;
	private Double longitude;
	private String municipality;
	private Date timestamp;
	private String typeOfViolation;
	private List<String> pictures;
	private ReportStatus reportStatus = ReportStatus.SUBMITTED;

	public ViolationReportRepresentation(ViolationReport report) {
		this.userUid = report.getUserUid();
		this.licencePlate = report.getLicencePlate();
		this.description = report.getDescription();
		this.latitude = report.getLatitude();
		this.longitude = report.getLongitude();
		this.municipality = report.getMunicipality();
		this.typeOfViolation = report.getTypeOfViolation();

		List<String> pictureIDs = report.getPicturesIDOnServer();
		if (pictureIDs == null || pictureIDs.size() == 0)
			Log.e(TAG, "Report should have at least one picture in server");
		else {
			pictures = new ArrayList<>(pictureIDs);
		}
	}

	@NonNull
	public String getUserUid() {
		return userUid;
	}

	@NonNull
	public String getLicencePlate() {
		return licencePlate;
	}

	@Nullable
	public String getDescription() {
		return description;
	}

	@NonNull
	public Double getLatitude() {
		return latitude;
	}

	@NonNull
	public Double getLongitude() {
		return longitude;
	}

	@NonNull
	public String getMunicipality() {
		return municipality;
	}

	@ServerTimestamp
	@NonNull
	public Date getTimestamp() {
		return timestamp;
	}

	@NonNull
	public String getTypeOfViolation() {
		return typeOfViolation;
	}

	@NonNull
	public List<String> getPictures() {
		return pictures;
	}
}
