package io.mosip.ivv.preregistration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import static io.restassured.RestAssured.given;

public class GenerateQRCode extends Step implements StepInterface {

    private Person person;

    @SuppressWarnings({ "unchecked", "serial" })
    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);

        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);

        JSONObject request_json = new JSONObject();
        JSONObject api_input = new JSONObject();

        request_json.put("name", person.getName());
        request_json.put("preId", person.getPreRegistrationId());
        request_json.put("appointmentDate", person.getSlot().getDate());
        request_json.put("appointmentTime", person.getSlot().getFrom());
        request_json.put("mobNum", person.getPhone());
        request_json.put("emailID", person.getEmail());
        api_input.put("id", "mosip.pre-registration.qrcode.generate");
        api_input.put("version", System.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.getVariant()) {
            case "DEFAULT":
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
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/qrCode/generate";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response = given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .contentType(ContentType.JSON)
                .body(api_input.toString())
                .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", api_input.toString(), api_response);
        Helpers.logCallRecord(this.callRecord);

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
                           logInfo("Assert not yet implemented: " + pr_assert.type);
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
