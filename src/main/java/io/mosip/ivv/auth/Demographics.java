package main.java.io.mosip.ivv.auth;

import java.util.ArrayList;

public class Demographics {
	  ArrayList<Object> name = new ArrayList<Object>();
	  ArrayList<Object> gender = new ArrayList<Object>();
	  private String age;
	  private String dob;
	  ArrayList<Object> fullAddress = new ArrayList<Object>();


	 // Getter Methods 

	  public String getAge() {
	    return age;
	  }

	  public String getDob() {
	    return dob;
	  }

	 // Setter Methods 

	  public void setAge( String age ) {
	    this.age = age;
	  }

	  public void setDob( String dob ) {
	    this.dob = dob;
	  }
	  
	}