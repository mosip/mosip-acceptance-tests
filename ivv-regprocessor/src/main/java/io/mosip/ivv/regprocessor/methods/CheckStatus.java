package io.mosip.ivv.regprocessor.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public class CheckStatus extends BaseStep implements StepInterface {

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
            RequestDataDTO requestData = prepare();
            ResponseDataDTO responseData = call(requestData);
            process(responseData);
            if(!hasError){
                if (expectedStatus.isEmpty()) {
                    logInfo("Actual statusCode is [" + finalStatus + "], but can be anything");
                    return;
                } else if (expectedStatus.equals(finalStatus)) {
                    logInfo("Expected statusCode [" + expectedStatus + "] equals Actual statusCode [" + finalStatus + "]");
                    return;
                } else if(finalStatus.isEmpty()){

                }else {
                    logInfo("Expected statusCode [" + expectedStatus + "] does not match Actual statusCode [" + finalStatus + "]");
                    this.hasError = true;
                    return;
                }
            }else{
                return;
            }

            try {
                logInfo("Retrying after 10 seconds...");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logSevere(e.getMessage());
                this.hasError = true;
                return;
            }
        }
    }

    public RequestDataDTO prepare(){
        JSONArray request_json = new JSONArray(){{
            add(new JSONObject(){{
                put("registrationId", store.getCurrentPerson().getRegistrationId());
            }});
        }};

        JSONObject requestData = new JSONObject();
        requestData.put("id", "mosip.registration.status");
        requestData.put("version", System.getProperty("ivv.global.apiversion"));
        requestData.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("request", request_json);

        String url = "/registrationprocessor/" + System.getProperty("ivv.global.version") +"/registrationstatus/search";
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = (Response) given()
                .contentType(ContentType.JSON).body(data.getRequest())
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){
        ReadContext ctx = JsonPath.parse(res.getBody());

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

        logInfo("Registration id: "+ctx.read("$['response'][0]['registrationId']")+", statusCode: "+ctx.read("$['response'][0]['statusCode']"));

        if(ctx.read("$['response'][0]['statusCode']") != null && !ctx.read("$['response'][0]['statusCode']").equals("PROCESSING") && !ctx.read("$['response'][0]['statusCode']").equals("RECEIVED")){
            finalStatus = ctx.read("$['response'][0]['statusCode']");
        }
    }

}