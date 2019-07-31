package main.java.io.mosip.ivv.auth;

import java.util.ArrayList;

public class Request {
	  private String timestamp;
	  private String transactionID;
	  private String otp;
	  Demographics demographicsObject;
	  ArrayList<Object> biometrics = new ArrayList<Object>();

	 // Getter Methods 

	  public String getTimestamp() {
	    return timestamp;
	  }

	  public String getTransactionID() {
	    return transactionID;
	  }

	  public String getOtp() {
	    return otp;
	  }

	  public Demographics getDemographicsObject() {
	    return demographicsObject;
	  }

	 // Setter Methods 

	  public void setTimestamp( String timestamp ) {
	    this.timestamp = timestamp;
	  }

	  public void setTransactionID( String transactionID ) {
	    this.transactionID = transactionID;
	  }

	  public void setOtp( String otp ) {
	    this.otp = otp;
	  }

	  public void setDemographicsObject( Demographics demographicsObject ) {
	    this.demographicsObject = demographicsObject;
	  }
	}