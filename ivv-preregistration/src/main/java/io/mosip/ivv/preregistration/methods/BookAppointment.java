package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.ivv.core.structures.*;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class BookAppointment extends Step implements StepInterface {

    private Person person;

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @SuppressWarnings("unchecked")
	@Override
    public void run() {
        this.index = Utils.getPersonIndex(step);

        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);

        /* getting slot */
        BookingSlot slot = getBookingSlot();
        if(slot == null){
            logInfo("No Booking slot available");
            this.hasError = true;
            return;
        }
        this.store.getScenarioData().getPersona().getPersons().get(index).setSlot(slot);

        String preRegistrationID = person.getPreRegistrationId();

        JSONObject request_json = new JSONObject();
        request_json.put("registration_center_id", person.getRegistrationCenterId());
        request_json.put("appointment_date", slot.getDate());
        request_json.put("time_slot_from", slot.getFrom());
        request_json.put("time_slot_to", slot.getTo());

        JSONObject api_input = new JSONObject();
        api_input.put("id", "mosip.pre-registration.booking.book");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.getVariant()) {
            case "InvalidCenter":

            case "RegistrationCenterIdNotEntered":
                request_json.replace("registration_center_id", person.getRegistrationCenterId(),"");
                break;

            case "InvalidPRID":
                preRegistrationID = this.store.getGlobals().get("INVALID_PRID");
                break;

            case "UserHasNotSelectedTimeSlot":
                request_json.put("time_slot_from", "");
                request_json.put("time_slot_to", "");
                break;

            case "sameSlots":
                request_json.put("time_slot_from", "23:30:00");
                request_json.put("time_slot_to", "23:45:00");
                break;

            case "PASTDATE":

            case "InvalidBookingDateTimeFoundForPreregistrationId":
                request_json.put("appointment_date", "2000-01-01");
                break;

            case "Holiday":
                request_json.put("appointment_date", this.store.getGlobals().get("HOLIDAY_DATE"));
                break;

            case "RequestIdIsInvalid":
                api_input.put("id", "mosip.pre-registration.booking.book.Invalid");
                break;

            case "RequestVersionIsInvalid":
                api_input.put("version", "");
                break;

            case "RequestTimestampIsInvalid":
                api_input.put("requesttime", "");
                break;

            case "RequestDateShouldBeCurrentDate":
                api_input.put("requesttime", "2019-01-01T05:59:15.241Z");
                break;

            case "BookingDateTimeNotSelected":

            case "InvalidDateTimeFormat":
                request_json.put("appointment_date", "");
                break;

            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        api_input.put("request", request_json);

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/appointment/"+preRegistrationID;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = given().relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .body(api_input)
                .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", api_input.toString(), api_response);
        Helpers.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logSevere("API HTTP status return as " + api_response.getStatusCode());
            this.hasError = true;
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
                        case ALL:
                            logInfo("Assert not yet implemented: " + pr_assert.type);
                            break;

                        case API_CALL:
                            CallRecord getAppointmentRecord = getAppointment();
                            System.out.println(getAppointmentRecord.getResponse().getBody().asString());
                            if(getAppointmentRecord == null){
                                this.hasError = true;
                                return;
                            }else{
                                Boolean responseMatched = responseMatch(this.person,getAppointmentRecord.getResponse());
                                if(!responseMatched){
                                    logInfo("Assert API_CALL failed");
                                    this.hasError = true;
                                    return;
                                }
                            }
                            break;

                        case DEFAULT:
                            try {
                                if(ctx.read("$['response']") == null){
                                    logInfo("Assert failed: Expected response not empty but found empty");
                                    this.hasError=true;
                                    return;
                                }
                            } catch (PathNotFoundException e) {
                                e.printStackTrace();
                                logInfo("Assert failed: Expected response not empty but found empty");
                                this.hasError=true;
                                return;
                            }
                           // logInfo("Assert [DEFAULT] passed");
                            break;
                        case DB_VERIFICATION:
                        	String queryString=createSQLQuery(person,slot);
                        	try {
                                Helpers.dbVerification(queryString, "","");
                            }catch (SQLException e){
                                Utils.auditLog.severe("Failed while querying from DB "+ e.getMessage());
                            }

                        default:
                            logInfo("Assert not found or implemented: " + pr_assert.type);
                            break;
                    }
                }
            }
        }

        this.store.getScenarioData().getPersona().getPersons().get(index).setSlot(slot);

    }

    private CallRecord getAppointment(){
        Scenario.Step nstep = new Scenario.Step();
        nstep.setName("getAppointment");
        nstep.setVariant("DEFAULT");
        nstep.setModule(Scenario.Step.modules.pr);
        nstep.setIndex(new ArrayList<Integer>());
        nstep.getIndex().add(this.index);
        GetAppointment st = new GetAppointment();
        st.setExtentInstance(extentInstance);
        st.setState(this.store);
        st.setStep(nstep);
        st.run();
        this.store = st.getState();

        String identifier = "Sub Step: "+nstep.getName()+", module: "+nstep.getModule()+", variant: "+nstep.getVariant();
        if(st.hasError()){
            logSevere(identifier+" - failed");
            return null;
        }else{
            return st.getCallRecord();
        }
    }

    private Boolean responseMatch(Person person,Response response) {
        ReadContext ctx = JsonPath.parse(response.getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']");
        if (person != null && app_info != null) {
            if (!person.getRegistrationCenterId().equals(app_info.get("registration_center_id"))) {
                logInfo("Response matcher: Registration_center_id does not match");
                return false;
            }
            if (!person.getSlot().getDate().equals(app_info.get("appointment_date"))) {
                logInfo("Response matcher: Appointment_date does not match");
                return false;
            }

            if (!person.getSlot().getFrom().contains(app_info.get("time_slot_from"))) {
                logInfo("Response matcher: Time_slot_from does not match");
                return false;
            }
            if (!person.getSlot(). getTo().contains(app_info.get("time_slot_to"))) {
                logInfo("Response matcher: Time_slot_to does not match");
                return false;
            }
        }
        return true;
    }







    
    private String createSQLQuery(Person person,BookingSlot slot) {
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("SELECT * FROM prereg.reg_appointment where prereg_id = ").append("'")
				.append(person.getPreRegistrationId()).append("'").append(" AND regcntr_id = ").append("'")
				.append(person.getRegistrationCenterId()).append("'").append("AND appointment_date =").append("'")
				.append(slot.getDate()).append("'").append("AND slot_from_time =").append("'").append(slot.getFrom())
				.append("'").append("AND slot_to_time =").append("'").append(slot.getTo()).append("'");
		return sqlQuery.toString();
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