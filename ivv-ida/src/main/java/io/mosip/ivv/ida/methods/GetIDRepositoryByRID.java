package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class GetIDRepositoryByRID extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        String rid = store.getCurrentPerson().getRegistrationId();
        if(step.getParameters().size() > 0){
            rid = step.getParameters().get(0);
        }
        String url = "/idrepository/" + System.getProperty("ivv.global.version") + "/identity/rid/" + rid +"?type=all";
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

    }



}