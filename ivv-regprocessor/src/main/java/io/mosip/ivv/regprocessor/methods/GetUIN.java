package io.mosip.ivv.regprocessor.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class GetUIN extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        String registrationId = store.getCurrentPerson().getRegistrationId();
        if(step.getParameters().size() > 0){
            registrationId = step.getParameters().get(0);
        }
        String url = "/idrepository/" + System.getProperty("ivv.global.version") +"/identity/rid/"+registrationId+"?type=demo";
        return new RequestDataDTO(url, null);
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = (Response) given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .get(data.getUrl());
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
            String uin = ctx.read("$['response']['identity']['UIN']").toString();
            if(uin != null && !uin.isEmpty()){
                logInfo("UIN generated: "+uin);
                store.getPersona().getPersons().get(index).setUin(uin);
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