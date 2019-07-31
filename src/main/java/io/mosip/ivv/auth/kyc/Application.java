package main.java.io.mosip.ivv.auth.kyc;

import java.util.ArrayList;

public class Application {
	private String id;
	private String version;
	private String requestTime;
	private String transactionID;
	RequestedAuth RequestedAuthObject;
	private boolean consentObtained;
	private String secondaryLangCode;
	private String individualId;
	private String individualIdType;
	private String keyIndex;
	private String requestSessionKey;
	private String requestHMAC;
	Request RequestObject;

	
	public static Application getdApplication() {
		Application application = new Application();
		application.setId("mosip.identity.kyc");
		application.setVersion("1.0");
		application.setRequestTime("2019-02-15T10:01:57.086+05:30");
		application.setTransactionID("");
		application.setRequestedAuth(createRequestedAuth());
		application.setConsentObtained(true);
		application.setSecondaryLangCode("eng");
		application.setIndividualId("9830872690593682");
		application.setIndividualIdType("VID");
		application.setKeyIndex("key-index");
		application.setRequestSessionKey("session-Key");
		application.setRequestHMAC("HMAC");
		application.setRequest(createRequest());
		return application;
	}
	
	private static RequestedAuth  createRequestedAuth(){
		RequestedAuth requestedAuth = new RequestedAuth();
		requestedAuth.setOtp(true);
		requestedAuth.setDemo(false);
		requestedAuth.setBio(false);
		return requestedAuth;
	}

	
	private static Request  createRequest(){
		Request request = new Request();
		request.setOtp("123456");
		request.setTimestamp("2019-02-15T10:01:56.086+05:30 - ISO format timestamp");
		request.setTransactionID("1234567890");
		
		return request;
	}
	
	// Getter Methods
	public Application() {
	}

	public Application(String id, String version, String requestTime, String transactionID,
			RequestedAuth requestedAuthObject, boolean consentObtained, String secondaryLangCode, String individualId,
			String individualIdType, String keyIndex, String requestSessionKey, String requestHMAC,
			Request requestObject) {

		this.id = id;
		this.version = version;
		this.requestTime = requestTime;
		this.transactionID = transactionID;
		RequestedAuthObject = requestedAuthObject;
		this.consentObtained = consentObtained;
		this.secondaryLangCode = secondaryLangCode;
		this.individualId = individualId;
		this.individualIdType = individualIdType;
		this.keyIndex = keyIndex;
		this.requestSessionKey = requestSessionKey;
		this.requestHMAC = requestHMAC;
		RequestObject = requestObject;
	}

	
	public String getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public String getTransactionID() {
		return transactionID;
	}

	public RequestedAuth getRequestedAuth() {
		return RequestedAuthObject;
	}

	public boolean getConsentObtained() {
		return consentObtained;
	}

	public String getSecondaryLangCode() {
		return secondaryLangCode;
	}

	public String getIndividualId() {
		return individualId;
	}

	public String getIndividualIdType() {
		return individualIdType;
	}

	public String getKeyIndex() {
		return keyIndex;
	}

	public String getRequestSessionKey() {
		return requestSessionKey;
	}

	public String getRequestHMAC() {
		return requestHMAC;
	}

	public Request getRequest() {
		return RequestObject;
	}

	// Setter Methods

	public void setId(String id) {
		this.id = id;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public void setRequestedAuth(RequestedAuth requestedAuthObject) {
		this.RequestedAuthObject = requestedAuthObject;
	}

	public void setConsentObtained(boolean consentObtained) {
		this.consentObtained = consentObtained;
	}

	public void setSecondaryLangCode(String secondaryLangCode) {
		this.secondaryLangCode = secondaryLangCode;
	}

	public void setIndividualId(String individualId) {
		this.individualId = individualId;
	}

	public void setIndividualIdType(String individualIdType) {
		this.individualIdType = individualIdType;
	}

	public void setKeyIndex(String keyIndex) {
		this.keyIndex = keyIndex;
	}

	public void setRequestSessionKey(String requestSessionKey) {
		this.requestSessionKey = requestSessionKey;
	}

	public void setRequestHMAC(String requestHMAC) {
		this.requestHMAC = requestHMAC;
	}

	public void setRequest(Request requestObject) {
		this.RequestObject = requestObject;
	}
}
