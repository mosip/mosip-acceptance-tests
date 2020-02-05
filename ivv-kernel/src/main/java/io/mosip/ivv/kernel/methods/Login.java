package io.mosip.ivv.kernel.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Partner;
import io.mosip.ivv.core.dtos.RegistrationUser;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.Collection;

import static io.restassured.RestAssured.given;

public class Login extends Step implements StepInterface{

    private enum users {
        regUser, partner
    }

    @Override
    public void validateStep() throws RigInternalError {
        if(step.getParameters().size() > 0){
            try {
                users.valueOf(step.getParameters().get(0));
            } catch (IllegalArgumentException ex) {
                throw new RigInternalError("DSL error: Key does not match a valid field");
            }
        } else {
            step.getParameters().add(0, users.regUser.name());
        }

    }

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    public void run() {
        JSONObject request_json = new JSONObject();

        switch(users.valueOf(step.getParameters().get(0))){

            case partner:
                Partner partner = this.store.getCurrentPartner();
                request_json.put("appId", "registrationprocessor");
                request_json.put("password", partner.getPassword());
                request_json.put("userName", partner.getUserId());
                break;

            default:
                RegistrationUser regUser = this.store.getCurrentRegistrationUSer();
                request_json.put("appId", "registrationprocessor");
                request_json.put("password", regUser.getPassword());
                request_json.put("userName", regUser.getUserId());
                break;
        }

        JSONObject api_input = new JSONObject();
        api_input.put("id", "");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        api_input.put("request", request_json);

        String url = "/"+System.getProperty("ivv.global.version")+"/authmanager/authenticate/useridPwd";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = (Response) given()
                .contentType(ContentType.JSON).body(api_input)
                .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", api_input.toString(), api_response);
        Utils.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        Collection<String> values = api_response.getCookies().values();
        for (String value : values) {
            this.store.getHttpData().setCookie(value);
            logInfo("Cookie auth token: " + value);
        }


        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logInfo("API HTTP status return as " + api_response.getStatusCode());
            this.hasError=true;
            return;
        }

        if (step.getErrors() != null && step.getErrors().size()>0) {
            ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step, api_response, extentInstance).inject();
            if(!emr.getStatus()){
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
                            logWarning("Assert not found or implemented: " + pr_assert.type);
                            break;
                    }
                }
            }
        }
    }

}