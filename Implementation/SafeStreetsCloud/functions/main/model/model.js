exports.newViolationReport = function(description, latitude, licensePlate, longitude, municipality, pictures, reportStatus, statusMotivation, typeOfViolation, uploadTimestamp, userUid) {
    return {
        description: description,
        latitude: latitude,
        licensePlate: licensePlate,
        longitude: longitude,
        municipality: municipality,
        pictures: pictures,
        reportStatus: reportStatus,
        statusMotivation: statusMotivation,
        typeOfViolation: typeOfViolation,
        uploadTimestamp: uploadTimestamp,
        userUid: userUid
    }
};

exports.newGroup = function(firstTimestamp, groupStatus, lastTimestamp, latitude, licensePlate, longitude, reports, typeOfViolation) {
    return {
        firstTimestamp: firstTimestamp,
        groupStatus: groupStatus,
        lastTimestamp: lastTimestamp,
        latitude: latitude,
        licensePlate: licensePlate,
        longitude: longitude,
        reports: reports,
        typeOfViolation: typeOfViolation
    }
};

exports.newCluster = function(groups, latitude, longitude, typeOfViolation) {
    return {
        groups: groups,
        latitude: latitude,
        longitude: longitude,
        typeOfViolation: typeOfViolation
    }
};

exports.ReportStatusEnum = {
    SUBMITTED: "SUBMITTED",
    APPROVED: "APPROVED",
    REJECTED: "REJECTED",
    CORRECT: "CORRECT"
};