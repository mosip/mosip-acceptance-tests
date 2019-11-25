package io.mosip.ivv.preregistration.methods;

import com.aventstack.extentreports.Status;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.base.PRStepInterface;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.mosip.ivv.core.utils.OTPReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import static io.restassured.RestAssured.given;

public class ValidateOTP extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     * @param step
     */
    @SuppressWarnings("unchecked")
	@Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        Person person = this.store.getScenarioData().getPersona().getPersons().get(index);

        Boolean setTokenIsNotPresentInTheHeader = false;
       // String otp = OTPReader.readOTP(person.getUserid());
        String otp = OTPReader.readOTP("mosip-test@technoforte.co.in");

        JSONObject request_json = new JSONObject();
        request_json.put("otp", otp.trim());
      //  request_json.put("userId", person.getUserid());
        request_json.put("userId", "mosip-test@technoforte.co.in");
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
                            .cookie("Authorization", this.store.getHttpData()!=null ? this.store.getHttpData().getCookie() : "")
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

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", api_input.toString(), api_response);
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
        }

    }

}