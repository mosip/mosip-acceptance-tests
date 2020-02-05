package io.mosip.ivv.ida.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Partner;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public class SendAuthenticationOTP extends Step implements StepInterface {
    private Person person;
    private Partner partner;

    @Override
    public void run() {
        person = store.getCurrentPerson();
        partner = store.getCurrentPartner();

        String uin = person.getUin();

        String authPartnerID = partner.getPartnerId();
        String mispLicenseKey = partner.getMispLicenceKey();
        JSONObject otp_input = new JSONObject();
        otp_input.put("id", "mosip.identity.otp");
        otp_input.put("version", "1.0");
        otp_input.put("requestTime", Utils.getCurrentDateAndTimeForAPI());
        otp_input.put("transactionID", "1234567890");
        otp_input.put("individualId", uin);
        otp_input.put("individualIdType", "UIN");
        otp_input.put("otpChannel", new JSONArray() {
            {
                add("EMAIL");
            }
        });
        String url = "/idauthentication/" + System.getProperty("ivv.global.version") + "/otp/" + authPartnerID + "/" + mispLicenseKey;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .contentType(ContentType.JSON)
                .body(otp_input)
                .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", otp_input.toString(), api_response);
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

        try {
            logInfo(ctx.read("$['response']").toString());
        } catch (PathNotFoundException e) {
            e.printStackTrace();
            return;
        }

    }

}