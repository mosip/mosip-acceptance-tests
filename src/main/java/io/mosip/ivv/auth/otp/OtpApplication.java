package main.java.io.mosip.ivv.auth.otp;

import java.util.ArrayList;

import main.java.io.mosip.ivv.auth.kyc.Application;

public class OtpApplication {
	private String id;
	private String version;
	private String requestTime;
	private String transactionID;
	private String individualId;
	private String individualIdType;
	ArrayList<Object> otpChannel = new ArrayList<Object>();

	public static OtpApplication getOtpApplication() {
		OtpApplication application = new OtpApplication();

		application.setId("mosip.identity.otp");
		application.setVersion("1.0");
		application.setRequestTime("2019-02-15T07:22:57.086+05:30");
		application.setTransactionID("txn12345");
		application.setIndividualId("9830872690593682");
		application.setIndividualIdType("VID");
		return application;
	}

	// Getter Methods

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

	public String getIndividualId() {
		return individualId;
	}

	public String getIndividualIdType() {
		return individualIdType;
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

	public void setIndividualId(String individualId) {
		this.individualId = individualId;
	}

	public void setIndividualIdType(String individualIdType) {
		this.individualIdType = individualIdType;
	}
}
