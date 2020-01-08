package it.polimi.marcermarchiscianamotta.safestreets.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a representation of a user document in the database.
 * This class contains only the relevant attributes that need to be sent or retrieved from the database.
 *
 * @author Desno365
 */
public class UserRepresentation {

    private List<String> violationReportIds;

    //region Constructors
    //================================================================================
    public UserRepresentation() {
        violationReportIds = new ArrayList<>();
    }
    //endregion

    //region Getter methods
    //================================================================================
    @NonNull
    public List<String> getViolationReportIds() {
        return violationReportIds;
    }
    //endregion

    //region Public methods
    //================================================================================
    /**
     * Add a violation report id to the list of violation reports made by this user.
     * @param reportId the id of the report to add.
     */
    public void addReport(String reportId) {
        violationReportIds.add(reportId);
    }
    //endregion
}
