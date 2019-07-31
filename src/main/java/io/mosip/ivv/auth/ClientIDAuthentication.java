package main.java.io.mosip.ivv.auth;

import static io.restassured.RestAssured.given;

import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.auth.kyc.Application;
import main.java.io.mosip.ivv.auth.otp.OtpApplication;
import main.java.io.mosip.ivv.auth.staticpin.StaticPinApplication;
import main.java.io.mosip.ivv.auth.staticpin.StaticPinRequest;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.CallRecord;
import main.java.io.mosip.ivv.helpers.Controller;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.orchestrator.Scenario.Data;

public class ClientIDAuthentication extends Controller{
		public static JsonPath jsonFormatPath = null;
	
		public ClientIDAuthentication(Data data) {
                super(data,extentTest);
                // TODO Auto-generated constructor stub
        }
	
		@SuppressWarnings("unchecked")
		public static CallRecord authenticationService(Scenario.Step step) throws SQLException {
			
			CallRecord res=null;
	        /*int index = UtilsA.getPersonIndex(step);
	    	Persona person = data.persons.get(index);*/
	    	
	        JSONObject auth_json_request = new JSONObject();
	        
	        auth_json_request.put("id", "person.getId()");
	        auth_json_request.put("version", "person.getVersion()");
	        auth_json_request.put("requestTime", "person.getRequestTime()");
	        auth_json_request.put("transactionID", "person.getTransactionID()");
	        auth_json_request.put("otp", "person.otp");
	        auth_json_request.put("name", "person.name");
	        auth_json_request.put("dob", "person.date_of_birth");
	        auth_json_request.put("requestedAuth", "person.getRequestedAuthObject()");
	        auth_json_request.put("consentObtained", "person.isConsentObtained()");
	        auth_json_request.put("individualId", "person.getIndividualId()");
	        auth_json_request.put("individualIdType", "person.getIndividualIdType()");
	        auth_json_request.put("keyIndex", "person.getKeyIndex()");
	        auth_json_request.put("requestSessionKey", "person.getRequestSessionKey()");
			auth_json_request.put("requestHMAC", "person.getRequestHMAC()");
	        auth_json_request.put("timestamp", "person.getRequestObject().getTimestamp()");
	        auth_json_request.put("gender", "person.getRequestObject().getDemographicsObject().gender");
	        auth_json_request.put("age", "person.getRequestObject().getDemographicsObject().getAge()");
	        auth_json_request.put("fullAddress", "person.getRequestObject().getDemographicsObject().fullAddress");
	        
	        
	        String url = BaseHelper.baseAuthVersion + "/identity/auth/";
	        RestAssured.baseURI = BaseHelper.baseUri;
	        RestAssured.useRelaxedHTTPSValidation();

	        Response api_response =
	                (Response) given()
	                .queryParam("Auth-Partner-ID", "123")
	                .queryParam("MISP-LicenseKey", "1234")
	                .contentType(ContentType.JSON).body(auth_json_request).post(url);
	        System.out.println(api_response.getBody().asString());
	        res = new CallRecord(url, "", auth_json_request.toString(), api_response,
					"" + api_response.getStatusCode(), step);
	        AddCallRecord(res, api_response, extentTest);
	        
	    	return res;
			
		}
		
		@SuppressWarnings("unchecked")
		public static CallRecord eKYCService(Scenario.Step step) throws SQLException {
			CallRecord res=null;
	        /*int index = UtilsA.getPersonIndex(step);
	    	Persona person = data.persons.get(index);*/
			
			Application application=Application.getdApplication();
			
	        JSONObject eky_json_request = new JSONObject();
	        
	        eky_json_request.put("id",application.getId());
	        eky_json_request.put("version",application.getVersion());
	        eky_json_request.put("requestTime",application.getRequestTime());
	        eky_json_request.put("transactionID",application.getTransactionID());
	        
	        
	        JSONObject object_request_auth = new JSONObject();
	        object_request_auth.put("otp",String.valueOf(application.getRequestedAuth().getOtp()));
	        object_request_auth.put("demo",String.valueOf(application.getRequestedAuth().getDemo()));
	        object_request_auth.put("bio",String.valueOf(application.getRequestedAuth().getBio()));
	        
	        eky_json_request.put("requestedAuth",object_request_auth);
	        eky_json_request.put("consentObtained",application.getConsentObtained());
	        eky_json_request.put("secondaryLangCode",application.getSecondaryLangCode());
	        eky_json_request.put("individualId",application.getIndividualId());
	        eky_json_request.put("individualIdType",application.getIndividualIdType());
	        eky_json_request.put("keyIndex",application.getKeyIndex());
	        eky_json_request.put("requestSessionKey",application.getRequestSessionKey());
	        eky_json_request.put("requestHMAC",application.getRequestHMAC());
	        
	        JSONObject data_object= new JSONObject();
	        data_object.put("mosipProcess", "");
	        data_object.put("environment", "");
	        data_object.put("version", "");
	        data_object.put("deviceCode", "");
	        data_object.put("deviceProviderID", "");
	        data_object.put("deviceServiceID", "");
	        data_object.put("deviceServiceVersion", "");
	        data_object.put("bioType", "");
	        data_object.put("bioSubType", "");
	        data_object.put("bioValue", "");
	        data_object.put("transactionID", "");
	        data_object.put("timestamp", "");
	        data_object.put("requestedScore", "");
	        data_object.put("qualityScore", "");
	        
	        
	        JSONObject bio_object= new JSONObject();
	        bio_object.put("data", data_object);
	        bio_object.put("hash", "");
	        bio_object.put("sessionKey","");
	        bio_object.put("signature", "");
	        
	        ArrayList<JSONObject> array= new ArrayList<>();
	        array.add(bio_object);
	        
	        JSONObject object_request = new JSONObject();
	        object_request.put("timestamp", application.getRequest().getTimestamp());
	        object_request.put("transactionID", application.getRequest().getTransactionID());
	        object_request.put("otp", application.getRequest().getOtp());
	        object_request.put("biometrics", array);
	        
	       
	        
	        eky_json_request.put("request",object_request);
	        
	        
	        String url = BaseHelper.baseAuthVersion + "/identity/kyc/";
	        RestAssured.baseURI = BaseHelper.baseUri;
	        RestAssured.useRelaxedHTTPSValidation();

	        Response api_response =
	                (Response) given()
	                .queryParam("eKYC-Partner-ID", "123")
	                .queryParam("MISP-LicenseKey", "1234")
	                .contentType(ContentType.JSON).body(eky_json_request).post(url);
	        System.out.println(api_response.getBody().asString());
	        res = new CallRecord(url, "", eky_json_request.toString(), api_response,
					"" + api_response.getStatusCode(), step);
	        AddCallRecord(res, api_response, extentTest);
			
			return res;
		}
		
	@SuppressWarnings("unchecked")
	public static CallRecord oTPRequestService(Scenario.Step step) throws SQLException {
		CallRecord res = null;
		/*
		 * int index = UtilsA.getPersonIndex(step); Persona person =
		 * data.persons.get(index);
		 */

		OtpApplication application = OtpApplication.getOtpApplication();

		ArrayList<Object> otpList = new ArrayList<>();
		otpList.add("EMAIL");
		otpList.add("PHONE");

		JSONObject otp_json_request = new JSONObject();
		otp_json_request.put("id", application.getId());
		otp_json_request.put("version", application.getVersion());
		otp_json_request.put("requestTime", application.getRequestTime());
		otp_json_request.put("transactionID", application.getTransactionID());
		otp_json_request.put("individualId", application.getIndividualId());
		otp_json_request.put("individualIdType", application.getIndividualIdType());
		otp_json_request.put("otpChannel", otpList);

		String url = BaseHelper.baseAuthVersion + "/identity/otp/123/1234";
		RestAssured.baseURI = BaseHelper.baseUri;
		RestAssured.useRelaxedHTTPSValidation();

		Response api_response = (Response) given()
				// .queryParam("Partner-ID", "123")
				// .queryParam("MISP-LicenseKey", "1234")
				.contentType(ContentType.JSON).body(otp_json_request).post(url);
		System.out.println(api_response.getBody().asString());
		res = new CallRecord(url, "", otp_json_request.toString(), api_response, "" + api_response.getStatusCode(),
				step);
		AddCallRecord(res, api_response, extentTest);

		return null;
	}
		
		
	@SuppressWarnings("unchecked")
	public static CallRecord staticPinService(Scenario.Step step) throws SQLException {

		CallRecord res = null;
		/*
		 * int index = UtilsA.getPersonIndex(step); Persona person =
		 * data.persons.get(index);
		 */

		StaticPinApplication application = StaticPinApplication.getStaticPinApplication();
		StaticPinRequest request=StaticPinApplication.createStaticPinRequest();
		
		JSONObject request_json= new JSONObject();
		request_json.put("staticPin", request.getStaticPin());
		
		JSONObject pin_json_request = new JSONObject();
		pin_json_request.put("id", application.getId());
		pin_json_request.put("version", application.getVersion());
		pin_json_request.put("requestTime", application.getRequestTime());
		pin_json_request.put("individualId", application.getIndividualId());
		pin_json_request.put("individualIdType", application.getIndividualIdType());
		pin_json_request.put("request", request_json);

		String url = BaseHelper.baseAuthVersion + "/identity/staticpin";
		RestAssured.baseURI = BaseHelper.baseUri;
		RestAssured.useRelaxedHTTPSValidation();

		Response api_response = (Response) given().contentType(ContentType.JSON).body(pin_json_request).post(url);
		System.out.println(api_response.getBody().asString());
		res = new CallRecord(url, "", pin_json_request.toString(), api_response, "" + api_response.getStatusCode(),
				step);
		AddCallRecord(res, api_response, extentTest);

		return null;
	}
		
		public static void main(String[] args) throws SQLException {
			System.out.println("===================================");
			ClientIDAuthentication.staticPinService(null);
		}
		
}
