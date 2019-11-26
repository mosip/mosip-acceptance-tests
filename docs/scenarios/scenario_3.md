# Scenario: new application of child (walkin)

**Type:** HappyPath

**Persona required:** HOF (with at least one introducer & child)

#### [mt_setPerson(0)](../DSL.md#mt_setperson)
System sets the 0th index person as active person

_for this case index 0 is father_

#### [rc_login()](../DSL.md#rc_login)
Registration user logs in via userid & password

#### [rc_deletePackets()](../DSL.md#rc_deletepackets)
Older packets will be deleted from local machine. This will be based on the parameter set in global param

#### [rc_newRegistration()](../DSL.md#rc_newregistration)
Registration user selects new registration for application

#### [rc_addApplicantDemographics()](../DSL.md#rc_addapplicantdemographics)
Registration user adds demographic info of applicant

#### [rc_addApplicantDocuments()](../DSL.md#rc_addapplicantdocuments)
Registration user adds applicant's documents

#### [rc_addApplicantBiometrics()](../DSL.md#rc_addapplicantbiometrics)
Registration user adds all applicant's biometrics:
* face
* leftEye
* rightEye
* leftThumb
* rightThumb
* leftIndex
* leftMiddle
* leftRing
* leftLittle
* rightIndex
* rightMiddle
* rightRing
* rightLittle

#### [rc_createPacket()](../DSL.md#rc_createpacket)
Registration user create a packet based on above info

#### [rc_approveRegistration()](../DSL.md#rc_approveregistration)
Registration user approves registration, which means its ready to sync

#### [rc_syncPacket()](../DSL.md#rc_syncpacket)
Registration user synces the meta data of the packet like id, hash, etc

#### [rc_uploadPacket()](../DSL.md#rc_uploadpacket)
Registration user uploads the packet zip to the server

#### [kr_login()](../DSL.md#kr_login)
System logs in to mosip via Registration user's userid & password

#### [rp_getRegistrationStatus(5, PROCESSED)](../DSL.md#rp_getregistrationstatus)
System will fetch the status of registration with a total of 5 attempts and asserts for final status to be "PROCESSED"

#### [rp_getUIN()](../DSL.md#rp_getuin)
System will fetch the UIN from server via registration id

#### [mt_retainPerson()](../DSL.md#mt_retainperson)
Retains the data of active person and now it is save to switch person

#### [mt_setPerson(1)](../DSL.md#mt_setperson)
System sets the 1th index person as active person (child)

_for this case index 1 is child_

#### [mt_setIntroducer(0)](../DSL.md#mt_setIntroducer)
System sets the 0th index person as active introducer (father)

#### [rc_newRegistration()](../DSL.md#rc_newregistration)
Registration user selects new registration for application

#### [rc_addApplicantDemographics()](../DSL.md#rc_addapplicantdemographics)
Registration user adds demographic info of applicant

#### [rc_addApplicantDocuments()](../DSL.md#rc_addapplicantdocuments)
Registration user adds applicant's documents

#### [rc_addApplicantBiometrics(face)](../DSL.md#rc_addapplicantbiometrics)
Registration user adds all applicant's biometrics:
* face

#### [rc_addIntroducerBiometrics()](../DSL.md#rc_addIntroducerBiometrics)
Registration user adds all introducer's biometrics:
* face
* leftEye
* rightEye
* leftThumb
* rightThumb
* leftIndex
* leftMiddle
* leftRing
* leftLittle
* rightIndex
* rightMiddle
* rightRing
* rightLittle

#### [rc_createPacket()](../DSL.md#rc_createpacket)
Registration user create a packet based on above info

#### [rc_approveRegistration()](../DSL.md#rc_approveregistration)
Registration user approves registration, which means its ready to sync

#### [rc_syncPacket()](../DSL.md#rc_syncpacket)
Registration user synces the meta data of the packet like id, hash, etc

#### [rc_uploadPacket()](../DSL.md#rc_uploadpacket)
Registration user uploads the packet zip to the server

#### [kr_login()](../DSL.md#kr_login)
System logs in to mosip via Registration user's userid & password

#### [rp_getRegistrationStatus(5, PROCESSED)](../DSL.md#rp_getregistrationstatus)
System will fetch the status of registration with a total of 5 attempts and asserts for final status to be "PROCESSED"

#### [rp_getUIN()](../DSL.md#rp_getuin)
System will fetch the UIN from server via registration id