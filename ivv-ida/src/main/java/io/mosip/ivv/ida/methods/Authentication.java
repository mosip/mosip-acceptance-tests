package io.mosip.ivv.ida.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.CallRecord;
import io.mosip.ivv.core.structures.Partner;
import io.mosip.ivv.core.structures.Person;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class Authentication extends Step implements StepInterface {
    private Person person;
    private Partner partner;
    private String uin;

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     */
    @Override
    public void run() {
        uin = store.getCurrentPerson().getUin();
        person = store.getCurrentPerson();
        partner = store.getCurrentPartner();

        String authPartnerID = partner.getPartnerId();
        String mispLicenseKey = partner.getMispLicenceKey();

        String encryptedAuthRequest = createAuthRequest();
        String url = "/idauthentication/" + System.getProperty("ivv.global.version") + "/auth/" + authPartnerID + "/" + mispLicenseKey;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .contentType(ContentType.JSON)
                .body(encryptedAuthRequest)
                .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", encryptedAuthRequest, api_response);
        Utils.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logFail("API HTTP status return as " + api_response.getStatusCode());
            this.hasError=true;
            return;
        }

        if (step.getErrors() != null && step.getErrors().size() > 0) {
            ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step, api_response, extentInstance).inject();
            if (!emr.getStatus()) {
                this.hasError = true;
                return;
            }
        } else {
            /* Assertion policies execution */
            if (step.getAsserts().size() > 0) {
                for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                    switch (pr_assert.type) {
                        case DONT:
                            break;

                        case DEFAULT:
                            try {
                                if (ctx.read("$['response']") == null) {
                                    logInfo("Assert failed: Expected response not empty but found empty");
                                    logInfo("Error Code: "+ctx.read("$['errors'][0]['errorCode']"));
                                    logInfo("Error Message: "+ctx.read("$['errors'][0]['errorMessage']"));
                                    this.hasError = true;
                                    return;
                                }
                            } catch (PathNotFoundException e) {
                                e.printStackTrace();
                                logSevere(e.getMessage());
                                this.hasError = true;
                                return;
                            }
                            logInfo("Assert [DEFAULT] passed");
                            break;

                        default:
                            logWarning("API HTTP status return as " + pr_assert.type);
                            break;
                    }
                }
            }
        }
    }

    private String createAuthRequest(){
        store.getCurrentPerson().getAuthenticationJSON().put("timestamp", Utils.getCurrentDateAndTimeForAPI());

        String url = "/v1/identity/createAuthRequest";
        RestAssured.baseURI = System.getProperty("ivv.swagger.host");
        Response api_response = given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .queryParam("Authtype", String.join(",", person.getAuthParams()))
                .queryParam("id", uin)
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