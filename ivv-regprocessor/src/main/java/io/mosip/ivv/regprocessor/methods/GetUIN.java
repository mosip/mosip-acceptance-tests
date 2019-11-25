package io.mosip.ivv.regprocessor.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.CallRecord;
import io.mosip.ivv.regprocessor.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class GetUIN extends Step implements StepInterface {

    @Override
    public void run() {
        String registrationId = store.getCurrentPerson().getRegistrationId();
        if(step.getParameters().size() > 0){
            registrationId = step.getParameters().get(0);
        }

        String url = "/idrepository/" + System.getProperty("ivv.global.version") +"/identity/rid/"+registrationId+"?type=demo";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");

        Response api_response = (Response) given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .get(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", "registrationId: "+registrationId, api_response);
        Helpers.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logFail("API HTTP status return as " + api_response.getStatusCode());
            this.hasError=true;
            return;
        }

        try {
            if(ctx.read("$['response']") == null){
                logInfo("Assert failed: Expected response not empty but found empty");
                this.hasError=true;
                return;
            }
            String uin = ctx.read("$['response']['identity']['UIN']").toString();
            if(uin != null && !uin.isEmpty()){
                logInfo("UIN generated: "+uin);
                store.getScenarioData().getPersona().getPersons().get(index).setUin(uin);
            } else {
                logInfo("UIN not found in response");
                this.hasError=true;
                return;
            }
        } catch (PathNotFoundException e) {
            e.printStackTrace();
            logSevere("Assert failed: Expected response not empty but found empty");
            this.hasError=true;
            return;
        }

    }

}