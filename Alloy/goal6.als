sig ViolationReport {
	state: one ViolationReportState
}

// Enums
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

// Facts

// All connections that use a modern encryption protocol can not be manipulated.
fact domainAssumption6 {
	all n : AT_NETWORK | n.encryptedConnection = FALSE <=> n.canBeAltered = TRUE
	all n : AT_NETWORK | n.encryptedConnection = TRUE <=> n.canBeAltered = FALSE
}

// The application will allow using pictures in a violation report only if the picture was taken by the application itself, preventing it to be manipulated on the device.
fact requirement17 {
	all d : AT_DEVICE | d.canBeAltered = FALSE
}

// All connections used by the system use modern encryption protocols.
fact requirement18 {
	all n : AT_NETWORK | n.encryptedConnection = TRUE
}

// Data saved in the server can not be manipulated.
fact requirement19 {
	all s : AT_SERVER | s.canBeAltered = FALSE
}

// Assertions

// The integrity of the violation report is guaranteed.
assert goal6 {
	all v : ViolationReport | v.state.canBeAltered = FALSE
}
check goal6 for 5
