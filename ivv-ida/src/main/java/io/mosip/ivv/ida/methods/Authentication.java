package io.mosip.ivv.ida.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class Authentication extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        String authPartnerID = store.getCurrentPartner().getPartnerId();
        String mispLicenseKey = store.getCurrentPartner().getMispLicenceKey();
        String encryptedAuthRequest = createAuthRequest();
        String url = "/idauthentication/" + System.getProperty("ivv.global.version") + "/auth/" + authPartnerID + "/" + mispLicenseKey;
        return new RequestDataDTO(url, encryptedAuthRequest);
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .contentType(ContentType.JSON)
                .body(data.getRequest())
                .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }



    private String createAuthRequest(){
        store.getCurrentPerson().getAuthenticationJSON().put("timestamp", Utils.getCurrentDateAndTimeForAPI());

        String url = "/v1/identity/createAuthRequest";
        RestAssured.baseURI = System.getProperty("ivv.swagger.host");
        Response api_response = given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .queryParam("Authtype", String.join(",", store.getCurrentPerson().getAuthParams()))
                .queryParam("id", store.getCurrentPerson().getUin())
                .queryParam("idType", "UIN")
                .queryParam("isInternal", false)
                .queryParam("transactionId", "1234567890")
                .contentType(ContentType.JSON)
                .body(store.getCurrentPerson().getAuthenticationJSON())
                .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", store.getCurrentPerson().getAuthenticationJSON().toString(), api_response);
        Utils.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
        String responseBody = api_response.getBody().asString();

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logFail("API HTTP status return as " + api_response.getStatusCode());
            this.hasError=true;
            return null;
        }

        if(responseBody != null && !responseBody.isEmpty()){
            return responseBody;
        } else {
            logInfo("CreateAuthRequest body is null or empty");
            this.hasError=true;
            return null;
        }
    }

}