//—————— ENTITIES —————--
abstract sig Entity {
	username: one Username
}
sig User extends Entity {}
sig Municipality extends Entity {}

sig Username{}

//—————— ENTITIES —————--
sig Device {
	hasGPS: one Bool,
	hasInternet: one Bool,
	hasCamera: one Bool
}

//—————— VIOLATION REPORTS —————--
sig ViolationReport {
	pictures: set Picture,
	location: lone Location,
	timestamp: lone Timestamp,
	typeOfViolation: lone TypeOfViolation,
	licensePlate: lone LicensePlate,
	state: one ViolationReportLocation,
	createdBy: one Device
}

sig Picture {}
sig Location {}
sig Timestamp {}
sig TypeOfViolation {}
sig LicensePlate {}

//—————— QUERIES —————--
sig ViolationReportsQuery {
	askingEntity: one Entity,
	violationReportsData: set ViolationReportDataForQuery
}

abstract sig ViolationReportDataForQuery {
	pictures: set Picture,
	location: lone Location,
	timestamp: lone Timestamp,
	typeOfViolation: lone TypeOfViolation,
	licensePlate: lone LicensePlate,
}

sig ViolationReportDataForQueryWithPrivacy extends ViolationReportDataForQuery {
} {
	#pictures = 0
	#licensePlate = 0
}

sig ViolationReportDataForQueryWithoutPrivacy extends ViolationReportDataForQuery {
} {
	#pictures >= 1
	#licensePlate = 1
}

//—————— ENUMS —————--
abstract sig Bool {}
one sig TRUE extends Bool {}
one sig FALSE extends Bool {}

abstract sig ViolationReportLocation {
	canBeAltered: one Bool
}
one sig ON_DEVICE extends ViolationReportLocation {}
one sig ON_NETWORK extends ViolationReportLocation {
	encryptedConnection: one Bool
}
one sig ON_SERVER extends ViolationReportLocation {}

// ########## GENERAL FACTS ##########

/* There could not exist two reports in the same state that share the same picture. 
 *A picture is related to only one real violation report.
*/
fact uniqueReport{
	all r1, r2: ViolationReport | (r1 != r2 and r1.state = r2.state) implies #(r1.pictures & r2.pictures) = 0
}

/*There could not exist any data that is not associated with a report
*/
fact allDataAreReferencedByAReport{
	all p:Picture | some r:ViolationReport | p in r.pictures
	all li:LicensePlate | some r:ViolationReport | li = r.licensePlate
	all t:Timestamp | some r:ViolationReport | t = r.timestamp
	all lo:Location | some r:ViolationReport | lo = r.location
	all tv:TypeOfViolation | some r:ViolationReport | tv = r.typeOfViolation
}

fact networkModelling{
	// Ever report that is in the server has been created by a device. Furthermore it is granted that there exists a corresponding report in the network
	all vS : ViolationReport | vS.state = ON_SERVER implies 
		(some vD : ViolationReport | vD.state = ON_DEVICE and sendReportToServerFromDevice[vD, vS])
	// Ever report that is in the network has been created by a device
	all vN : ViolationReport | vN.state = ON_NETWORK implies 
		(some vD : ViolationReport | vD.state = ON_DEVICE and sendReportToNetwork[vD, vN])
	// Every report created by a device has at most one corresponding report in the network
	all vD : ViolationReport | vD.state = ON_DEVICE implies
		(lone vN : ViolationReport | vN.state = ON_NETWORK and sendReportToNetwork[vD, vN])
}

/* Users have access only to anonymized data
*/
fact useAnonymizedDataWhenUser {
	all query : ViolationReportsQuery | query.askingEntity in User => query.violationReportsData in ViolationReportDataForQueryWithPrivacy
}


/* Municipality have access only to all the data
*/
fact useNotAnonymizedDataWhenMunicipality {
	all query : ViolationReportsQuery | query.askingEntity in Municipality => query.violationReportsData in ViolationReportDataForQueryWithoutPrivacy
}

/* There could not exist data not referenced by a query
*/
fact allDataAreReferencedByAQuery{
	all data : ViolationReportDataForQuery | some v:ViolationReportsQuery | v.violationReportsData = data
}

/* The username of every entity must be unique
*/
fact uniqueUsername {
	no disj e1, e2 : Entity | e1.username = e2.username
}

/* There could not exist a username not referenced by an entity
*/
fact allUsernamesAreAssociatedWithAnEtity{
	all u:Username | some e : Entity | e.username = u
}

// ########## NETWORK PREDICATES ##########

/* Represent the act of sending a violation report through the network.
* @param vDevice the representation of the violation report on the device.
* @param vNetwork the representation of the violation report in the network.
*/
pred sendReportToNetwork [vDevice : ViolationReport, vNetwork : ViolationReport] {
	vDevice.state = ON_DEVICE
	vNetwork.state = ON_NETWORK
	vDevice.pictures = vNetwork.pictures
	vDevice.location = vNetwork.location
	vDevice.timestamp = vNetwork.timestamp
	vDevice.typeOfViolation = vNetwork.typeOfViolation
	vDevice.licensePlate = vNetwork.licensePlate
	vDevice.createdBy = vNetwork.createdBy
}

/* Represent the act of receiving a violation report from the network.
* @param vNetwork the representation of the violation report in the network.
* @param vServer the representation of the violation report on the server.
*/
pred receiveReportFromNetwork [vNetwork : ViolationReport, vServer : ViolationReport] {
	vNetwork.state = ON_NETWORK
	vServer.state = ON_SERVER
	vNetwork.pictures = vServer.pictures
	vNetwork.location = vServer.location
	vNetwork.timestamp = vServer.timestamp
	vNetwork.typeOfViolation = vServer.typeOfViolation
	vNetwork.licensePlate = vServer.licensePlate
	vNetwork.createdBy = vServer.createdBy
}

/* Represent the act of sending a violation report to the server from the device.
* This basically says that there exists a violation report sent in the network such that:
* it has the same content of the violation report sent by the device,
* and the same content of the violation report received by the server.
* @param vDevice the representation of the violation report on the device.
* @param vServer the representation of the violation report on the server.
*/
pred sendReportToServerFromDevice [vDevice: ViolationReport, vServer : ViolationReport] {
	vDevice.state = ON_DEVICE
	vServer.state = ON_SERVER
	some vNetwork : ViolationReport | vNetwork.state = ON_NETWORK and sendReportToNetwork[vDevice, vNetwork] and receiveReportFromNetwork[vNetwork, vServer]
}

// ########## GOAL 2 ##########

/*[R4] The application must allow reporting of violations only from devices equipped 
 *	with a GPS receiver which are in the conditions to obtain a GPS fix.
 *[R5] The application must allow reporting of violations only from devices equipped 
 *	with a camera.
 *[R6] The application must allow reporting of violations only from devices with an active 
 *	internet connection. This requirement basically says that the violation report 
 *	created by the device will be correctly received by the server.
 *[R7] A user has the possibility to specify the type of the reported violation choosing from a list.
 *[R8] The application creates a violation report with at least one picture, exactly one timestamp,
 *	exactly one location, exactly one type of violation and the license plate of the vehicle.
*/
fact requirement4_5_6_7_8 {
	//Only devisces with GPS camera and internet can create reports
	#{v:ViolationReport | v.state=ON_DEVICE and 
						(v.createdBy.hasGPS = FALSE or
						v.createdBy.hasInternet = FALSE or
						v.createdBy.hasCamera = FALSE)} = 0
	
	//A valid report is created only if the device has the GPS, a camera and internet
	all v : ViolationReport | (v.state = ON_DEVICE and 
						v.createdBy.hasGPS = TRUE and
						v.createdBy.hasInternet = TRUE and
						v.createdBy.hasCamera = TRUE)
					 implies 
	(#v.pictures >= 1 and 
 	#v.location = 1 and 
 	#v.timestamp = 1 and 
 	#v.typeOfViolation = 1 and 
 	#v.licensePlate = 1)
}




/*[G2] A violation report received by the system must have enough information to be valid,
 * i.e. has at least one picture of the violation, exactly one GPS position, exactly one 
 * timestamp, exactly one type of violation and the license plate of the vehicle.
*/
assert goal2 {
	all v : ViolationReport | v.state = ON_SERVER implies  
(#v.pictures >= 1 and
 #v.location = 1 and
 #v.timestamp = 1 and
 #v.typeOfViolation = 1 and
 #v.licensePlate = 1)
}
check goal2 for 5


// ########## GOAL6 ##########

/*[D6] Data transferred through connections that use modern encryption protocols 
 *cannot be manipulated.
*/
fact domainAssumption6 {
	all n : ON_NETWORK | n.encryptedConnection = FALSE <=> n.canBeAltered = TRUE
	all n : ON_NETWORK | n.encryptedConnection = TRUE <=> n.canBeAltered = FALSE
}

/*[R17] The application will allow using pictures in a violation report only if the picture
 * was taken by the application itself, preventing it to be manipulated on the device.
*/
fact requirement17 {
	all d : ON_DEVICE | d.canBeAltered = FALSE
}

/*[R18] All connections used by the system use modern encryption protocols.
*/
fact requirement18 {
	all n : ON_NETWORK | n.encryptedConnection = TRUE
}

/*[R19] Data saved in the server can not be manipulated.
*/
fact requirement19 {
	all s : ON_SERVER | s.canBeAltered = FALSE
}

/*[G6] The integrity of the violation report is guaranteed.
*/
assert goal6 {
	all v : ViolationReport | v.state.canBeAltered = FALSE
}
check goal6 for 5



// ########## PRIVACY MUST BE RESPECTED FOR QUERIES ##########
assert privacyRespected {
	all query : ViolationReportsQuery | all data : ViolationReportDataForQuery | (data in query.violationReportsData and query.askingEntity in User) => (#data.pictures = 0 and #data.licensePlate = 0)
	all query : ViolationReportsQuery | all data : ViolationReportDataForQuery | (data in query.violationReportsData and query.askingEntity in Municipality) => (#data.pictures >= 1 and #data.licensePlate = 1)
}
check privacyRespected for 5



// ########## No entities with the same username ##########
assert checkNoSameUsername {
	all e1 : Entity | all e2 : Entity | e1 != e2 => e1.username != e2. username
}
check checkNoSameUsername for 5
