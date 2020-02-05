package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.BookingSlot;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ReBookAppointment extends Step implements StepInterface {
	private Person person;

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	public void run() {
		this.index = Utils.getPersonIndex(step);
		/* getting active user from persons */
		this.person = this.store.getScenarioData().getPersona().getPersons().get(index);
		JSONObject api_input = new JSONObject();

        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            default:
    			logInfo("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
    			return;
        }
        
        BookingSlot slot = getBookingSlot();
        if(slot == null){
            logInfo("No Booking slot available");
            this.hasError = true;
            return;
        }
        
        api_input.put("id", System.getProperty("bookingBookID"));
        api_input.put("ver", "1.0");
        api_input.put("reqTime", Utils.getCurrentDateAndTimeForAPI());
        api_input.put("request", new JSONArray() {{
            add(new JSONObject() {{
                put("preRegistrationId", person.getPreRegistrationId());
                put("oldBookingDetails", new JSONObject() {{
                    put("registration_center_id", person.getRegistrationCenterId());
                    put("appointment_date", slot.getDate());
                    put("time_slot_from", slot.getFrom());
                    put("time_slot_to", slot.getTo());
                }});
                put("newBookingDetails", new JSONObject() {{
                    put("registration_center_id", person.getRegistrationCenterId());
                    put("appointment_date", slot.getDate());
                    put("time_slot_from", slot.getFrom());
                    put("time_slot_to", slot.getTo());
                }});
            }});
        }});
       
        
        
        String url = "/pre-registration/" + System.getProperty("ivv.prereg.version") + "/booking/appointment";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response =
                (Response) given()
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
            return;
        }
        
        
        if (step.getErrors() != null && step.getErrors().size()>0) {
            ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step, api_response, extentInstance).inject();
            if(!emr.getStatus()){
                this.hasError = true;
                return;
            }
        }else{


            /* Assertion policies execution */
            if (step.getAsserts().size() > 0) {
                for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                    switch (pr_assert.type) {
                    case DONT:
                        break;

                    case DB_VERIFICATION:
                        String queryString =
                                "SELECT * FROM prereg.reg_appointment where prereg_id = " + person.getPreRegistrationId()
                                        + " AND regcntr_id = " + person.getRegistrationCenterId()
                                        + " AND appointment_date = " + slot.getDate()
                                        + " AND slot_from_time = " + slot.getFrom()
                                        + " AND slot_to_time = " + slot.getTo();

						try {
							Helpers.dbVerification(queryString,"prereg_id", "status_code");
						} catch (SQLException e) {
							e.printStackTrace();
							logSevere("Assert failed: No data Found for preRegistrationId :"
									+ ctx.read("$['response']['preRegistrationId']") + "and status_code :"
									+ ctx.read("$['response']['statusCode']"));
							this.hasError = true;
							return;
						}
						break;
					default:
						break;

                    }
                }
            }
        }
        
      
	}
	
	
	
	private BookingSlot getBookingSlot(){
        Scenario.Step nstep = Helpers.generateStep("getBookingSlots", this.index);
        GetBookingSlots st = new GetBookingSlots();
        setExtentInstance(extentInstance);
        st.setState(this.store);
        st.setStep(nstep);
        st.run();
        this.store = st.getState();

        String identifier = "Sub Step: "+nstep.getName()+", module: "+nstep.getModule()+", variant: "+nstep.getVariant();
        if(st.hasError()){
            logSevere(identifier+" - failed");
            return null;
        }else{
            return parseSlots(st.getCallRecord().getResponse().getBody().asString());
        }
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private BookingSlot parseSlots(String rawData){
        Gson gsn = new Gson();
		Map<String, Object> resMap = gsn.fromJson(rawData, Map.class);
        try {
            List center_details = (List)((Map)resMap.get("response")).get("centerDetails");
            if(center_details.size()==0){
                return null;
            }
            for(Object center_info: center_details) {
                String date = (String)((Map)center_info).get("date");
                List timeSlots = (List)((Map)center_info).get("timeSlots");
                if(timeSlots.size()>0){
                    String from = (String)((Map)timeSlots.get(0)).get("fromTime");
                    String to = (String)((Map)timeSlots.get(0)).get("toTime");
                    return new BookingSlot(date, from, to);
                }
            }
        } catch (NullPointerException ne) {
            logSevere("Error while running Booking slot: "+ne.getMessage());
            return null;
        }
        return null;
    }

}
