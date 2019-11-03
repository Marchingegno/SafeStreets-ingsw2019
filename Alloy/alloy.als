// ########## Signatures ##########
sig ViolationReport {
	pictures: set Picture,
	location: lone Location,
	timestamp: lone Timestamp,
	typeOfViolation: lone TypeOfViolation,
	state: one ViolationReportState
}

sig Picture {}
sig Location {}
sig Timestamp {}
sig TypeOfViolation {}



// ########## Enums ##########
abstract sig Bool {}
one sig TRUE extends Bool {}
one sig FALSE extends Bool {}

abstract sig ViolationReportState {
	canBeAltered: one Bool
}
one sig AT_DEVICE extends ViolationReportState {}
one sig AT_NETWORK extends ViolationReportState {
	encryptedConnection: one Bool
}
one sig AT_SERVER extends ViolationReportState {}



// ########## Goal 2 ##########

/* Represent the act of sending a violation report through the network.
* @param vDevice the representation of the violation report on the device.
* @param vNetwork the representation of the violation report in the network.
*/
pred sendReportToNetwork [vDevice : ViolationReport, vNetwork : ViolationReport] {
	vDevice.state = AT_DEVICE
	vNetwork.state = AT_NETWORK
	vDevice.pictures = vNetwork.pictures
	vDevice.location = vNetwork.location
	vDevice.timestamp = vNetwork.timestamp
	vDevice.typeOfViolation = vNetwork.typeOfViolation
}

/* Represent the act of receiving a violation report from the network.
* @param vNetwork the representation of the violation report in the network.
* @param vServer the representation of the violation report on the server.
*/
pred receiveReportFromNetwork [vNetwork : ViolationReport, vServer : ViolationReport] {
	vNetwork.state = AT_NETWORK
	vServer.state = AT_SERVER
	vNetwork.pictures = vServer.pictures
	vNetwork.location = vServer.location
	vNetwork.timestamp = vServer.timestamp
	vNetwork.typeOfViolation = vServer.typeOfViolation
}

/* Represent the act of sending a violation report to the server from the device.
* This basically says that there exist a violation report sent in the network such that:
* it has the same content of the violation report sent by the device,
* and the same content of the violation report received by the server.
* @param vDevice the representation of the violation report on the device.
* @param vServer the representation of the violation report on the server.
*/
pred sendReportToServerFromDevice [vDevice: ViolationReport, vServer : ViolationReport] {
	vDevice.state = AT_DEVICE
	vServer.state = AT_SERVER
	some vNetwork : ViolationReport | vNetwork.state = AT_NETWORK and sendReportToNetwork[vDevice, vNetwork] and receiveReportFromNetwork[vNetwork, vServer]
}

// [R4] The application must allow reporting of violations only from devices equipped with a GPS receiver which are in the conditions to obtain a GPS fix.
// [R5] The application must allow reporting of violations only from devices equipped with a camera.
// [R7] A user has the possibility to specify the type of the reported violation choosing from a list.
// [R8] The application creates a violation report with at least one picture, one timestamp, one location and one type of violation.
fact requirement4_5_7_8 {
	all v : ViolationReport | v.state = AT_DEVICE implies (#v.pictures >= 1 and #v.location = 1 and #v.timestamp = 1 and #v.typeOfViolation = 1)
}


// [R6] The application must allow reporting of violations only from devices with an active internet connection.
// This requirement basically says that the violation report created by the device will be correclty received by the server.
fact requirement6 {
	all vS : ViolationReport | vS.state = AT_SERVER implies 
		(some vD : ViolationReport | vD.state = AT_DEVICE and sendReportToServerFromDevice[vD, vS])
}

// [G2] A violation report received by the system must have enough information to be valid, i.e. has at least one picture of the violation, one GPS position, one timestamp and one type of violation.
assert goal2 {
	all v : ViolationReport | v.state = AT_SERVER implies  (#v.pictures >= 1 and #v.location = 1 and #v.timestamp = 1 and #v.typeOfViolation = 1)
}
check goal2 for 5



// ########## Goal 6 ##########

// [D6] All connections that use a modern encryption protocol can not be manipulated.
fact domainAssumption6 {
	all n : AT_NETWORK | n.encryptedConnection = FALSE <=> n.canBeAltered = TRUE
	all n : AT_NETWORK | n.encryptedConnection = TRUE <=> n.canBeAltered = FALSE
}

// [R17] The application will allow using pictures in a violation report only if the picture was taken by the application itself, preventing it to be manipulated on the device.
fact requirement17 {
	all d : AT_DEVICE | d.canBeAltered = FALSE
}

// [R18] All connections used by the system use modern encryption protocols.
fact requirement18 {
	all n : AT_NETWORK | n.encryptedConnection = TRUE
}

// [R19] Data saved in the server can not be manipulated.
fact requirement19 {
	all s : AT_SERVER | s.canBeAltered = FALSE
}

// [G6] The integrity of the violation report is guaranteed.
assert goal6 {
	all v : ViolationReport | v.state.canBeAltered = FALSE
}
check goal6 for 5
