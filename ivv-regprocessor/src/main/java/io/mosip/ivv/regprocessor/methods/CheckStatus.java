package io.mosip.ivv.regprocessor.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.regprocessor.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public class CheckStatus extends Step implements StepInterface {

    private int delay = 200;

    private String finalStatus;

    @Override
    public void run() {
        int counter = 0;
        int repeats = 10;
        String expectedStatus = "";
        try {
            repeats = Integer.parseInt(step.getParameters().get(0));
        } catch ( IndexOutOfBoundsException | NumberFormatException e  ) {

        }

        try {
            expectedStatus = step.getParameters().get(1);
        } catch ( IndexOutOfBoundsException e ) {

        }

        while(counter < repeats){
            logInfo("Checking the statusCode of Registration");
            Boolean funcStatus = getStatus(step);
            if(funcStatus){
                if (expectedStatus.isEmpty()) {
                    logInfo("Actual statusCode is [" + finalStatus + "], but can be anything");
                    return;
                } else if (expectedStatus.equals(finalStatus)) {
                    logInfo("Expected statusCode [" + expectedStatus + "] equals Actual statusCode [" + finalStatus + "]");
                    return;
                } else {
                    logInfo("Expected statusCode [" + expectedStatus + "] does not match Actual statusCode [" + finalStatus + "]");
                    this.hasError = true;
                    return;
                }
            }else{
                if(hasError){
                    return;
                }
            }
        }
    }

    private Boolean getStatus(Scenario.Step step){
        this.index = Utils.getPersonIndex(step);
        String registrationId = store.getScenarioData().getPersona().getPersons().get(index).getRegistrationId();

        JSONArray request_json = new JSONArray(){{
            add(new JSONObject(){{
                put("registrationId", registrationId);
            }});
        }};

        JSONObject api_input = new JSONObject();
        api_input.put("id", "mosip.registration.status");
        api_input.put("version", System.getProperty("ivv.global.apiversion"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        api_input.put("request", request_json);

        String url = "/registrationprocessor/" + System.getProperty("ivv.global.version") +"/registrationstatus/search";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");

        Response api_response = (Response) given()
                .contentType(ContentType.JSON).body(api_input)
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", api_input.toString(), api_response);
        Helpers.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logFail("API HTTP status return as " + api_response.getStatusCode());
            this.hasError=true;
            return false;
        }

        try {
            if(ctx.read("$['response']") == null){
                logInfo("Assert failed: Expected response not empty but found empty");
                this.hasError=true;
                return false;
            }
        } catch (PathNotFoundException e) {
            e.printStackTrace();
            logSevere("Assert failed: Expected response not empty but found empty");
            this.hasError=true;
            return false;
        }

        logInfo("Registration id: "+ctx.read("$['response'][0]['registrationId']")+", statusCode: "+ctx.read("$['response'][0]['statusCode']"));

        if(ctx.read("$['response'][0]['statusCode']") != null && !ctx.read("$['response'][0]['statusCode']").equals("PROCESSING") && !ctx.read("$['response'][0]['statusCode']").equals("RECEIVED")){
            finalStatus = ctx.read("$['response'][0]['statusCode']");
            return true;
        }

        try {
            logInfo("Retry after 10 seconds...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return false;
        }

        return false;
    }

}