package main.java.io.mosip.ivv.auth.kyc;

import java.util.ArrayList;

public class Request {
	private String timestamp;
	private String transactionID;
	private String otp;
	ArrayList<Biometrics> biometrics = new ArrayList<Biometrics>();

	// Getter Methods

	public ArrayList<Biometrics> getBiometrics() {
		return biometrics;
	}

	public void setBiometrics(ArrayList<Biometrics> biometrics) {
		this.biometrics = biometrics;
	}

	public String getTimestamp() {
		return timestamp;
	}

	/*@Override
	public String toString() {
		return "timestamp=" + timestamp + ", transactionID=" + transactionID + ", otp=" + otp + ", biometrics="
				+ biometrics;
	}*/

	public String getTransactionID() {
		return transactionID;
	}

	public String getOtp() {
		return otp;
	}

	// Setter Methods

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}
}
