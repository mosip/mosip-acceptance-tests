package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class Logout extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/login/invalidateToken";
        return new RequestDataDTO(url, null);
    }

    public ResponseDataDTO call(RequestDataDTO data){
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/login/invalidateToken";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = given().cookie("Authorization", this.store.getHttpData().getCookie()).post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        Helpers.logCallRecord(this.callRecord);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }



}
