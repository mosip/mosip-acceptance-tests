package io.mosip.ivv.preregistration.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.MailHelper;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class ValidateOTP extends BaseStep implements StepInterface {
    private String otp;

    @Override
    public void run() throws RigInternalError {
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
            logInfo("Checking the User email (" + store.getCurrentPerson().getUserid() + ") for OTP");
            otp = checkForOTP();
            if (otp != null && !otp.isEmpty()) {
                logInfo("OTP retrieved: " + otp);
                validate(step);
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

    private String checkForOTP() throws RigInternalError {
        try {
            logInfo("Retrying after 10 seconds...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return "";
        }

        String otp = "";
        ArrayList<String> subjects = new ArrayList<String>() {{
            if(properties.getProperty("ivv.prereg.otp.subject") != null && !properties.getProperty("ivv.prereg.otp.subject").isEmpty()){
                add(properties.getProperty("ivv.prereg.otp.subject"));
            } else {
                throw new RigInternalError("ivv.prereg.otp.subject property cannot be empty. Its is used to identify the OTP message");
            }
        }};
        String regex = "otp\\s([0-9]{6})";
        MailHelper.MailHelperResponse mailHelperResponse = MailHelper.readviaRegex(subjects, regex, store.getCurrentPerson().getUserid(), 10);
        if (mailHelperResponse != null) {
            logInfo("Msg found: " + mailHelperResponse.getBody().trim());
            otp = mailHelperResponse.getRegexout();
        }

        return otp;
    }


    private void validate(Scenario.Step step) {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        JSONObject request_json = new JSONObject();
        request_json.put("otp", otp.trim());
        request_json.put("userId", store.getCurrentPerson().getUserid());
        JSONObject requestData = new JSONObject();
        requestData.put("id", "mosip.pre-registration.login.useridotp");
        requestData.put("version", "1.0");
        requestData.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("request", request_json);
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/login/validateOtp";
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = (Response) given()
                        .cookie("Authorization", this.store.getHttpData() != null ? this.store.getHttpData().getCookie() : "")
                        .contentType("application/json")
                        .body(data.getRequest())
                        .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        Helpers.logCallRecord(this.callRecord);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){
        ReadContext ctx = JsonPath.parse(res.getBody());

        for (String value : res.getCookies().values()) {
            this.store.getHttpData().setCookie(value);
        }
    }

}