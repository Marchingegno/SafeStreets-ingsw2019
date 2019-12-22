package it.polimi.marcermarchiscianamotta.safestreets.model;

import android.util.Log;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents a representation of a violation report. This class contains only the relevant attributes
 * that need to be sent or retrieved from the database.
 */
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
	private ReportStatusEnum reportStatus = ReportStatusEnum.SUBMITTED;
	private String statusMotivation = null;

	public ViolationReportRepresentation() {
		// Needed for deserializing the object
	}

	public ViolationReportRepresentation(ViolationReport report) {
		if (report.isReadyToSend()) {
			this.userUid = report.getUserUid();
			this.licensePlate = report.getLicensePlate();
			this.description = report.getDescription();
			this.latitude = report.getLatitude();
			this.longitude = report.getLongitude();
			this.municipality = report.getMunicipality();
			this.typeOfViolation = report.getTypeOfViolation();

			List<String> pictureIDs = report.getPicturesIDOnServer();
			if (pictureIDs == null || pictureIDs.size() == 0) {
				Log.e(TAG, "Report should have at least one picture in order to be uploaded");
				throw new NullPointerException("Report should have at least one picture in order to be uploaded");
			} else
				pictures = new ArrayList<>(pictureIDs);
		} else
			throw new RuntimeException("Not all mandatory fields are specified");//TODO create a new type of exception
	}

	//region Getter methods
	//================================================================================
	@NonNull
	public String getUserUid() {
		return userUid;
	}

	@NonNull
	public String getLicensePlate() {
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

	@NonNull
	public ReportStatusEnum getReportStatus() {
		return reportStatus;
	}

	@Nullable
	public String getStatusMotivation() {
		return statusMotivation;
	}
	//endregion
}
