package io.mosip.ivv.preregistration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public class GenerateQRCode extends BaseStep implements StepInterface {
    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        JSONObject request_json = new JSONObject();
        JSONObject requestData = new JSONObject();

        /* TODO create a mapping for dynamic naming */
        request_json.put("name", "");
        request_json.put("preId", store.getCurrentPerson().getPreRegistrationId());
        request_json.put("appointmentDate", store.getCurrentPerson().getSlot().getDate());
        request_json.put("appointmentTime", store.getCurrentPerson().getSlot().getFrom());
        request_json.put("mobNum", store.getCurrentPerson().getPhone());
        request_json.put("emailID", "");
        requestData.put("id", "mosip.pre-registration.qrcode.generate");
        requestData.put("version", System.getProperty("apiver"));
        requestData.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("request", request_json);
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/applications/" + store.getCurrentPerson().getPreRegistrationId();
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response responseData = (Response) given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .get(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        Helpers.logCallRecord(this.callRecord);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }

}
