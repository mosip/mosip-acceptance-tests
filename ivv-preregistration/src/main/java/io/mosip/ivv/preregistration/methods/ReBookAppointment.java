package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ReBookAppointment extends BaseStep implements StepInterface {
    private JSONObject requestData;
    private Response responseData;

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        JSONObject requestData = new JSONObject();
        BookingSlot slot = store.getCurrentPerson().getSlot();

        requestData.put("id", System.getProperty("bookingBookID"));
        requestData.put("ver", "1.0");
        requestData.put("reqTime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("request", new JSONArray() {{
            add(new JSONObject() {{
                put("preRegistrationId", store.getCurrentPerson().getPreRegistrationId());
                put("oldBookingDetails", new JSONObject() {{
                    put("registration_center_id", store.getCurrentPerson().getRegistrationCenterId());
                    put("appointment_date", store.getCurrentPerson().getPrevSlot().getDate());
                    put("time_slot_from", store.getCurrentPerson().getPrevSlot().getFrom());
                    put("time_slot_to", store.getCurrentPerson().getPrevSlot().getTo());
                }});
                put("newBookingDetails", new JSONObject() {{
                    put("registration_center_id", store.getCurrentPerson().getRegistrationCenterId());
                    put("appointment_date", store.getCurrentPerson().getSlot().getDate());
                    put("time_slot_from", store.getCurrentPerson().getSlot().getFrom());
                    put("time_slot_to", store.getCurrentPerson().getSlot().getTo());
                }});
            }});
        }});
        String url = "/pre-registration/" + System.getProperty("ivv.prereg.version") + "/booking/appointment";
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response responseData =
                (Response) given()
                        .contentType(ContentType.JSON).body(requestData)
                        .cookie("Authorization", this.store.getHttpData().getCookie())
                        .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }

    @Override
    public void assertAPI() {

    }

}
