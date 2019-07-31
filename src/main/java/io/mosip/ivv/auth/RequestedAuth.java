package main.java.io.mosip.ivv.auth;

public class RequestedAuth {
	  private boolean otp;
	  private boolean demo;
	  private boolean bio;


	 // Getter Methods 

	  public boolean getOtp() {
	    return otp;
	  }

	  public boolean getDemo() {
	    return demo;
	  }

	  public boolean getBio() {
	    return bio;
	  }

	 // Setter Methods 

	  public void setOtp( boolean otp ) {
	    this.otp = otp;
	  }

	  public void setDemo( boolean demo ) {
	    this.demo = demo;
	  }

	  public void setBio( boolean bio ) {
	    this.bio = bio;
	  }
	  
	}
