package io.mosip.ivv.preregistration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.MailHelper;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import static io.restassured.RestAssured.given;

public class ValidateOTP extends Step implements StepInterface {
    private Person person;
    private String otp;
    private String msg;

    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        this.person = store.getScenarioData().getPersona().getPersons().get(index);

        int counter = 0;
        int repeats = 10;
        String expectedStatus = "";
        try {
            repeats = Integer.parseInt(step.getParameters().get(0));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {

        }

        try {
            expectedStatus = step.getParameters().get(1);
        } catch (IndexOutOfBoundsException e) {

        }

        try {
            logInfo("Sleeping for 10 seconds");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return;
        }

        while (counter < repeats) {
            logInfo("Checking the User email (" + person.getUserid().getValue() + ") for OTP");
            String otp = checkForOTP();
            if (otp != null && !otp.isEmpty()) {
                logInfo("OTP retrieved: " + otp);
                validate(step, otp);
                return;
            } else {
                if (hasError) {
                    return;
                }
            }
            counter++;
        }
        logInfo("OTP not found even after " + repeats + " retries");
        this.hasError = true;

    }

    private String checkForOTP() {
        try {
            logInfo("Retrying after 10 seconds...");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return "";
        }

        String otp = "";
        ArrayList<String> subjects = new ArrayList<String>() {{
            add("Message Otp");
        }};
        String regex = "otp\\s([0-9]{6})";
        MailHelper.MailHelperResponse mailHelperResponse = MailHelper.readviaRegex(subjects, regex, person.getUserid().getValue(), 10);
        if (mailHelperResponse != null) {
            logInfo("Msg found: " + mailHelperResponse.getBody().trim());
            otp = mailHelperResponse.getRegexout();
        }

        return otp;
    }


    private void validate(Scenario.Step step, String otp) {
        Boolean setTokenIsNotPresentInTheHeader = false;

        JSONObject request_json = new JSONObject();
        request_json.put("otp", otp.trim());
        request_json.put("userId", person.getUserid().getValue());
        JSONObject api_input = new JSONObject();
        api_input.put("id", "mosip.pre-registration.login.useridotp");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            case "Email":
                break;

            case "Mobile":
                break;

            case "UserDetailDoesNotExist":
                request_json.put("userId", "");
                break;

            case "OtpCanNotBeEmptyOrNull":
                request_json.put("otp", "");
                break;

            case "OtpConsistsOtherThanNumerics":
                request_json.put("otp", "mosip");
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

            case "RequestDateShouldBeCurrentDate":
                api_input.put("requesttime", "2019-01-01T05:59:15.241Z");
                break;

            case "ValidationUnsuccessful":
                request_json.put("otp", "99999");
                break;

            case "TokenIsNotPresentInTheHeader":
                setTokenIsNotPresentInTheHeader = true;
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }
        api_input.put("request", request_json);

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/login/validateOtp";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response;
        if (!setTokenIsNotPresentInTheHeader) {
            api_response =
                    (Response) given()
                            .cookie("Authorization", this.store.getHttpData() != null ? this.store.getHttpData().getCookie() : "")
                            .contentType("application/json")
                            .body(api_input)
                            .post(url);
        } else {
            api_response =
                    (Response) given()
                            .cookie("Authorization", "")
                            .contentType("application/json")
                            .body(api_input)
                            .post(url);
        }

        this.callRecord = new CallRecord(RestAssured.baseURI + url, "POST", api_input.toString(), api_response);
        Helpers.logCallRecord(this.callRecord);

        Collection<String> values = api_response.getCookies().values();
        for (String value : values) {
            this.store.getHttpData().setCookie(value);
        }

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
                        case DB_VERIFICATION:
                        case COMM_SINK:
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