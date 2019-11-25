# Pre-registration

#### pr_sendOTP()
Person enters an email and OTP is sent on his email

**Asserts**: 
* status

#### pr_validateOTP()
Person copies his OTP from his email and gets logged in after validating the OTP

**Asserts**: 
* status

#### pr_addApplication()
Person fills his demographic data and creates a new application

**Asserts**: 
* status
* API_CALL (getApplication will be used to assert)

#### pr_getApplication()
Person gets his application

**Asserts**: 
* status

#### pr_updateApplication()
Person updates his demographic data

**Asserts**: 
* status
* API_CALL (getApplication will be used to assert)

#### pr_deleteApplication()
Person deletes his application

**Asserts**:
* status
* API_CALL (getApplication will be used to assert)

#### pr_addDocument()
Person uploads proof documents

**Asserts**:
* status
* API_CALL (getDocument will be used to assert)

#### pr_getDocument()
Person gets his document data via document id

**Asserts**:
* status

#### pr_getDocumentByPreRegID()
Person gets all his documents via the pre-registration id

**Asserts**:
* status

#### pr_bookAppointment()
Person books an appointment

**Asserts**:
* status
* API_CALL (getAppointment will be used to assert)

#### pr_getAppointment()
Person gets his appointment details

**Asserts**:
* status

#### pr_reBookAppointment()
Person re-books his appointment with new details

**Asserts**:
* status
* API_CALL (getAppointment will be used to assert)

#### pr_getBookingSlots()
Person gets available booking slots. It will be used in bookAppointment & reBookAppointment

**Asserts**:
* status

# Registration

#### rc_syncPublicKey()
Fetches the public key from server and save it in local db. Public key is used to encrypt the data

**Asserts**:
* Boolean (success/ failure)

#### rc_syncGlobalParam()
Fetches the global parameters (required to control application's behavior) from server and save it in local db

**Asserts**:
* Boolean (success/ failure)

#### rc_syncUserDetail()
Fetches the users data (based on center) from the server and saves it in local db

**Asserts**:
* Boolean (success/ failure)

#### rc_syncUserSalt()
Fetches the users salt (based on center) from the server and saves it in local db

**Asserts**:
* Boolean (success/ failure)

#### rc_syncMaster()
Fetches the master data (based on center) from the server and saves it in local db. It is required for running the application with support data like holidays, meta data related to gender, location, doc. category, etc

**Asserts**:
* Boolean (success/ failure)

#### rc_login()
Registration user logs in via userid & password

**Asserts**:
* Boolean (success/ failure)

#### rc_newRegistration()
Registration user selects new registration

**Asserts**:
* Boolean (success/ failure)

#### rc_fetchPreRegistrationData()
Registration user fetches the pre-registration data of applicant's via his pre-registration id

**Asserts**:
* Boolean (success/ failure)

#### rc_addApplicantDemographics()
Registration user adds demographic info of applicant

#### rc_addApplicantDocuments()
Registration user adds applicant's proof documents

#### rc_addApplicantBiometrics()
Registration user adds applicant's biometrics

**Parameters**
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

_* If no parameter is provided then it will add all biometrics_

#### rc_addExceptionPhoto()
Registration user adds applicant's exception photo in case the applicant is bio-exception

#### rc_addIntroducerBiometrics()
Registration user adds introducer's biometrics

**Parameters**
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

_* If no parameter is provided then it will add all biometrics_

#### rc_createPacket()
Registration user creates a packet, which contains all necessary data required to generate an UIN

**Asserts**:
* Boolean (success/ failure)

#### rc_approveRegistration()
Registration user marks the packet as approved (ready for sync)

**Asserts**:
* Boolean (success/ failure)

#### rc_syncPacket()
Registration user syncs the packet info (like registration id, hash etc) with the server

**Asserts**:
* Boolean (success/ failure)

#### rc_uploadPacket()
Registration user uploads the packet zip file

**Asserts**:
* Boolean (success/ failure)

#### rc_deletePackets()
Older packets will be deleted from local machine. This will be based on the parameter set in global param

**Asserts**:
* Boolean (success/ failure)

#### rc_updateUIN()
Registration user selects update UIN

**Asserts**:
* Boolean (success/ failure)

#### rc_updateApplicantDemographics()
Registration user updates the demographic info of applicant

#### rc_updateApplicantBiometrics()
Registration user updates the biometric info of applicant

# Registration processor

#### rp_getRegistrationStatus()
Repetitively checks the status of a registration until its (PROCESSED/ REJECTED/ REREGISTER)

**Parameters**
* first parameter -> max. no. of attempts (default 5)
* second parameter -> final status (PROCESSED/ REREGISTER/ REJECTED)

**Asserts**:
* status

#### rp_checkUINMail()
Repetitively checks the email of person for UIN generation mail

**Parameters**
* first parameter -> max. no. of attempts (default 5)

**Asserts**:
* status

#### rp_getUIN()
fetches the UIN via registration id of person

**Asserts**:
* status

# Kernel

#### kr_login()
User (regUser/ partner) logs in via userid & password

**Parameters**
* first parameter -> regUser/ partner (default regUser)

**Asserts**:
* status

#### kr_getPublicKey()
User gets the public key from server

**Asserts**:
* status

#### kr_getGlobalConfig()
User gets the global config from server

**Asserts**:
* status

#### kr_getUserDetails()
User gets the user details from server

**Asserts**:
* status

#### kr_getMasterData()
User gets the master data from server

**Asserts**:
* status

# ID Authentication

#### ia_sendAuthenticationOTP()
User (regUser/ partner) sends authentication OTP to the person for eKYC

**Asserts**:
* status

#### ia_getAuthenticationOTP()
Fetches authentication OTP from person's email

**Asserts**:
* status

#### ia_addOTPInfo()
User (regUser/ partner) adds OTP in the authentication request

#### ia_addDemographicInfo()
User (regUser/ partner) adds demographic info in the authentication request

#### ia_addBiometricInfo()
User (regUser/ partner) adds biometric info in the authentication request

#### ia_authentication()
User (regUser/ partner) sends authentication request

**Asserts**:
* status

# Mutation

#### mt_setPerson()
Set a person from scenario data to active person. Active person's data will be used in the executing steps

#### mt_setRegistrationUser()
Set a registration user from scenario data to active registration user. Active registration user's data will be used in the executing steps

#### mt_setIntroducer()
Set a person from scenario data to active introducer. Active introducer's data will be used in the executing steps which uses introducer data

#### mt_setPartner()
Set a partner from scenario data to active partner. Active partner's data will be used in the executing steps which uses partner data

#### mt_retianPerson()
Retains a person data in scenario data. It is used while switching a person and if previous person's data is needed later

#### mt_updatePerson()
Updates person's information

**Parameters**
* first parameter -> name/ email/ registrationId/ uin/ preRegistrationId/ dob/ gender/ residenceStatus/ zone/ centerId (any one of them)
* second parameter -> new value

#### mt_updatePartner()
Updates partner's information

**Parameters**
* first parameter -> partnerId/ mispLicenseKey/ type (any one of them)
* second parameter -> new value

#### mt_updateRegistrationUser()
Updates registration user's information

**Parameters**
* first parameter -> userId/ password/ centerId/ type (any one of them)
* second parameter -> new value

