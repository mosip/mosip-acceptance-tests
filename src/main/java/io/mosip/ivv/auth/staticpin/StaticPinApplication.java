package main.java.io.mosip.ivv.auth.staticpin;

public class StaticPinApplication {
	private String id;
	private String version;
	private String requestTime;
	private String individualId;
	private String individualIdType;
	StaticPinRequest requestObject;
	
	
	
	public static StaticPinApplication getStaticPinApplication() {
		StaticPinApplication application = new StaticPinApplication();

		application.setId("mosip.identity.staticpin");
		application.setVersion("1.0");
		application.setRequestTime("2019-01-21T07:22:57.086+05:30");
		application.setIndividualId("4974892859");
		application.setIndividualIdType("UIN");
		return application;
	}
	
	public static StaticPinRequest createStaticPinRequest(){
		StaticPinRequest request = new StaticPinRequest();
		request.setStaticPin("123456");
		return request;
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

	public String getIndividualId() {
		return individualId;
	}

	public String getIndividualIdType() {
		return individualIdType;
	}

	public StaticPinRequest getStaticPinRequest() {
		return requestObject;
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

	public void setIndividualId(String individualId) {
		this.individualId = individualId;
	}

	public void setIndividualIdType(String individualIdType) {
		this.individualIdType = individualIdType;
	}

	public void setStaticPinRequest(StaticPinRequest requestObject) {
		this.requestObject = requestObject;
	}
}
