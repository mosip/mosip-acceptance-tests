package io.mosip.ivv.preregistration.methods;

import com.google.gson.Gson;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class GetBookingSlots extends BaseStep implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/appointment/availability/"+ store.getCurrentPerson().getRegistrationCenterId();
        return new RequestDataDTO(url, null);
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = given().relaxedHTTPSValidation()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .get(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        Helpers.logCallRecord(this.callRecord);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){
        Gson gsn = new Gson();
        Map<String, Object> resMap = gsn.fromJson(res.getBody(), Map.class);
        try {
            List center_details = (List)((Map)resMap.get("response")).get("centerDetails");
            if(center_details.size()==0){
                return ;
            }
            for(Object center_info: center_details) {
                String date = (String)((Map)center_info).get("date");
                List timeSlots = (List)((Map)center_info).get("timeSlots");
                if(timeSlots.size()>0){
                    String from = (String)((Map)timeSlots.get(0)).get("fromTime");
                    String to = (String)((Map)timeSlots.get(0)).get("toTime");
                    store.getCurrentPerson().setPrevSlot(store.getCurrentPerson().getSlot());
                    store.getCurrentPerson().setSlot(new BookingSlot(date, from, to));
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            logSevere("Error while running Booking slot: "+e.getMessage());
            this.hasError = true;
            return ;
        }
    }

}
