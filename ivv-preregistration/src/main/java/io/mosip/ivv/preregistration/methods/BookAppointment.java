package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.preregistration.utils.Helpers;
import org.json.simple.JSONObject;

import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class BookAppointment extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        JSONObject request_json = new JSONObject();
        request_json.put("registration_center_id", store.getCurrentPerson().getRegistrationCenterId());
        request_json.put("appointment_date", store.getCurrentPerson().getSlot().getDate());
        request_json.put("time_slot_from", store.getCurrentPerson().getSlot().getFrom());
        request_json.put("time_slot_to", store.getCurrentPerson().getSlot().getTo());

        JSONObject requestData = new JSONObject();
        requestData.put("id", "mosip.pre-registration.booking.book");
        requestData.put("version", "1.0");
        requestData.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("request", request_json);

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/appointment/"+store.getCurrentPerson().getPreRegistrationId();
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = given().relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .body(data.getRequest())
                .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        Helpers.logCallRecord(this.callRecord);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }

}