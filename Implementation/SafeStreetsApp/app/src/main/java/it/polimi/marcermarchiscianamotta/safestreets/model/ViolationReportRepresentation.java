package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.marcermarchiscianamotta.safestreets.util.ViolationEnum;

public class ViolationReportRepresentation {
	private static final String TAG = "ViolationReportRep";

	private String userUid;
	private String licensePlate;
	private String description;
	private Double latitude;
	private Double longitude;
	private String municipality;
	private Date uploadTimestamp;
	private ViolationEnum typeOfViolation;
	private List<String> pictures;
	private ReportStatus reportStatus = ReportStatus.SUBMITTED;

	public ViolationReportRepresentation(ViolationReport report) {
		this.userUid = report.getUserUid();
		this.licensePlate = report.getlicensePlate();
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
	public String getlicensePlate() {
		return licensePlate;
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
	public Date getUploadTimestamp() {
		return uploadTimestamp;
	}

	@NonNull
	public ViolationEnum getTypeOfViolation() {
		return typeOfViolation;
	}

	@NonNull
	public List<String> getPictures() {
		return pictures;
	}
}
