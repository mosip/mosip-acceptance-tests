package io.mosip.ivv.preregistration.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.Collection;

import static io.restassured.RestAssured.given;

public class SendOTP extends Step implements StepInterface {
    private Person person;
    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        this.person = store.getScenarioData().getPersona().getPersons().get(index);
        //MailHelper.deleteOTPEmails(System.getProperty("ivv.email.server.user"), System.getProperty("ivv.email.server.pass"));
        JSONObject request_json = new JSONObject();
        request_json.put("userId", person.getUserid());

        JSONObject api_input = new JSONObject();
        api_input.put("id", "mosip.pre-registration.login.sendotp");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            case "Email":
                break;

            case "Mobile":
                break;

            case "InvalidRequestUserIdReceived":
                request_json.put("userId", "");
                break;

            case "RequestIdIsInvalid":
                api_input.put("id", "");
                break;

            case "RequestVersionIsInvalid":
                api_input.put("version", "");
                break;

            case "RequestTimestampIsInvalid":
                api_input.put("requesttime", "");
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }
        api_input.put("request", request_json);

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/login/sendOtp";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = (Response) given()
                .contentType(ContentType.JSON).body(api_input).post(url);

        Collection<String> values = api_response.getCookies().values();
        for (String value : values) {
            this.store.getHttpData().setCookie(value);
        }


        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", api_input.toString(), api_response);
       // Helpers.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logSevere("API HTTP status return as " + api_response.getStatusCode());
            this.hasError = true;
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
                                if(ctx.read("$['response']") == null){
                                    logInfo("Assert failed: Expected response not empty but found empty");
                                    this.hasError=true;
                                    return;
                                }
                            } catch (PathNotFoundException e) {
                                e.printStackTrace();
                                logSevere("Assert failed: Expected response not empty but found empty");
                                this.hasError=true;
                                return;
                            }
                            logInfo("Assert [DEFAULT] passed");
                            break;

                        default:
                            logInfo("API HTTP status return as " + pr_assert.type);
                            break;
                    }
                }
            }
        }

    }

}