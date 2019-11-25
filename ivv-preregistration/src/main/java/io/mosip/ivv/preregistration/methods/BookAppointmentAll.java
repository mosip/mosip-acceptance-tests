package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.CallRecord;
import io.mosip.ivv.core.structures.Person;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.utils.CoreStructures;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class BookAppointmentAll extends Step implements StepInterface {
    public static String tableColName = null;
    public static String tableAppNameValue = null;
    public static String tableEventName = null;
    public static String pre_registration_id_OtherUser = "";

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     * @param step
     */
    @SuppressWarnings("unchecked")
	@Override
    public void run(Scenario.Step step) {
        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }
       // CallRecord res = null;
        JSONObject api_input = new JSONObject();
        api_input.put("id", System.getProperty("bookingBookID"));
        api_input.put("version", System.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            case "RequestIdIsInvalid":
                api_input.put("id", "");
                break;

            case "RequestVersionIsInvalid":
                api_input.put("version", "");
                break;

            case "RequestTimestampIsInvalid":
                api_input.put("requesttime", "");
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        int index = 0;
        JSONArray bookingDetails = new JSONArray();
        ArrayList<Person> persons = this.store.getScenarioData().getPersona().getPersons();
        for (int i = 0; i < persons.size(); i++) {
            index = Utils.getPersonIndex(step);

            /* getting slot */
            Scenario.Step nstep = new Scenario.Step();
            nstep.setName("getAvailableSlots");
            nstep.setVariant("DEFAULT");
            nstep.getIndex().add(i);
            /* getting slot */
            CallRecord slotObject = getAvailableSlots(nstep);

            CoreStructures.Slot slot = slot(slotObject.getResponse().getBody().asString());
            if (!slot.available) {
                logInfo("Appointment slots are not available!");
                //Assert.assertEquals(false, true, "Appointment slot");
            }
            int finalI = i;
            bookingDetails.add(new JSONObject() {{
                put("preRegistrationId", persons.get(finalI).getPreRegistrationId());

                if (step.getVariant().equals("RegistrationCenterIdNotEntered")) {
                    put("registration_center_id", "");
                } else {
                    put("registration_center_id", persons.get(finalI).getRegistrationCenterId());
                }
                if (step.getVariant().equals("BookingDateTimeNotSelected")) {
                    put("appointment_date", "");
                } else if (step.getVariant().equals("InvalidDateTimeFormat")) {
                    put("appointment_date", "");
                } else {
                    put("appointment_date", slot.date);
                }
                put("time_slot_from", slot.from);
                put("time_slot_to", slot.to);
                if (step.getVariant().equals("InvalidBookingDateTimeFoundForPreregistrationId")) {
                    put("time_slot_from", "01:00:00");
                    put("time_slot_to", "01:15:00");
                }
            }});

            api_input.put("request", new JSONObject() {{
                //add(new JSONObject(){{
                put("bookingRequest", bookingDetails);
            }});

            String url = "/preregistration/" + System.getProperty("ivv.prereg.version")  + "/appointment";
            RestAssured.baseURI = System.getProperty("ivv.mosip.host");
            RestAssured.useRelaxedHTTPSValidation();
            Response api_response =
                    (Response) given()
                            .contentType(ContentType.JSON).body(api_input)
                            .cookie("Authorization", this.store.getHttpData().getCookie())
                            .post(url);
            this.callRecord = new CallRecord(RestAssured.baseURI + url, "POST", "api_input: " + api_input, api_response);
            tableAppNameValue = "PREREGISTRATION";
            tableColName = "BOOKING_SERVICE";
            tableEventName = "PERSIST";
            Helpers.logCallRecord(this.callRecord);

            /* check for api status */
            if (api_response.getStatusCode() != 200) {
                logSevere("API HTTP status return as " + api_response.getStatusCode());
                this.hasError = true;
                return;
            }

            /* Error handling middleware */
            if (step.getErrors() != null && step.getErrors().size() > 0) {
                ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step, api_response, extentInstance).inject();
                if (!emr.getStatus()) {
                    this.hasError = true;
                    return;
                }
            } else {
                // ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

                /* Assertion policies execution */
                if (step.getAsserts().size() > 0) {
                    for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                        switch (pr_assert.type) {
                            case DONT:
                                break;
                            case API_CALL:
                                CallRecord getAppointment = getAppointment();
                                if(getAppointment == null){
                                    this.hasError = true;
                                    return;
                                }else{
                                    Boolean responseMatched = responseMatch(persons.get(index));
                                    if(!responseMatched){
                                        logInfo("Assert API_CALL failed");
                                        this.hasError = true;
                                        return;
                                    }
                                }
                                break;
							/*
							 * case DB_VERIFICATION: String queryString =
							 * "SELECT * FROM prereg.reg_appointment where prereg_id = " +
							 * persons.get(finalI).getPreRegistrationId() + " AND regcntr_id = " +
							 * persons.get(finalI).getRegistrationCenterId() + " AND appointment_date = " +
							 * slot.date + " AND slot_from_time = " + slot.from + " AND slot_to_time = " +
							 * slot.to;
							 */

							/*
							 * dbVerification(queryString, extentTest, true); break;
							 * 
							 * default: extentTest.log(Status.WARNING, "Skipping assert "+assertion_type);
							 * Utils.auditLog.warning("Skipping assert "+assertion_type);
							 */
                                                   //break;
                                                   
                               case DB_VERIFICATION:
                               	String queryString=createSQLQuery(persons,finalI,slot);
                               	try {
                                       Helpers.dbVerification(queryString, "","");
                                   }catch (SQLException e){
                                       Utils.auditLog.severe("Failed while querying from DB "+ e.getMessage());
                                   }

                               default:
                                   logWarning("Assert not found or implemented: " + pr_assert.type);
                                   break;
                           }

                        }
                    }
                }
            }


        }
        
        private String createSQLQuery(ArrayList<Person> persons,int index,CoreStructures.Slot slot) {
    		StringBuilder sqlQuery = new StringBuilder();
    		sqlQuery.append("SELECT * FROM prereg.reg_appointment where prereg_id = ").append("'")
    				.append(persons.get(index).getPreRegistrationId()).append("'").append(" AND regcntr_id = ").append("'")
    				.append(persons.get(index).getRegistrationCenterId()).append("'").append("AND appointment_date =").append("'")
    				.append(slot.date).append("'").append("AND slot_from_time =").append("'").append(slot.from)
    				.append("'").append("AND slot_to_time =").append("'").append(slot.to).append("'");
    		return sqlQuery.toString();
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
        st.run(nstep);
        this.store = st.getState();

        String identifier = "Sub Step: "+nstep.getName()+", module: "+nstep.getModule()+", variant: "+nstep.getVariant();
        if(st.hasError()){
            logSevere(identifier+" - failed");
            return null;
        }else{
            return st.getCallRecord();
        }
    }

    private Boolean responseMatch(Person person){
        ReadContext ctx = JsonPath.parse(this.callRecord.getResponse().getBody().asString());
        HashMap<String, String> appointment_info = ctx.read("$['response']");

        if (!person.getRegistrationCenterId().equals(appointment_info.get("registration_center_id"))) {
            logInfo("Response matcher: RegistrationCenterId does not match");
            return false;
        }
        if (!person.getSlot().getDate().equals(appointment_info.get("appointment_date"))) {
            logInfo("Response matcher: Appointment Date does not match");
            return false;
        }
        if (!person.getSlot().getFrom().contains(appointment_info.get("time_slot_from"))) {
            logInfo("Response matcher: TimeSlot_From does not match");
            return false;
        }
        if (!person.getSlot().getTo().contains(appointment_info.get("time_slot_to"))) {
            logInfo("Response matcher: TimeSlot_To does not match");
            return false;
        }
        return true;
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private CoreStructures.Slot slot(String rawData) {
        Gson gsn = new Gson();
        Map<String, Object> resMap = gsn.fromJson(rawData, Map.class);
        try {
            List center_details = (List) ((Map) resMap.get("response")).get("centerDetails");
            if (center_details.size() == 0) {
                return new CoreStructures.Slot();
            }
            for (Object center_info : center_details) {
                String date = (String) ((Map) center_info).get("date");
                List timeSlots = (List) ((Map) center_info).get("timeSlots");
                if (timeSlots.size() > 0) {
                    String from = (String) ((Map) timeSlots.get(0)).get("fromTime");
                    String to = (String) ((Map) timeSlots.get(0)).get("toTime");
                    return new CoreStructures.Slot(date, from, to);
                }
            }
        } catch (NullPointerException ne) {
            // TO Do
        }
        return new CoreStructures.Slot();
    }

    private CallRecord getAvailableSlots(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        Person person = this.store.getScenarioData().getPersona().getPersons().get(index);

        String url = "preregistration/" + System.getProperty("ivv.prereg.version") + "/appointment/availability/" + person.getRegistrationCenterId();
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response =
                (Response) given()
                        .contentType(ContentType.JSON)
                        .cookie("Authorization", this.store.getHttpData().getCookie())
                        .get(url);
        this.callRecord = new CallRecord(RestAssured.baseURI+url, "GET", "registration_center_id: "+person.getRegistrationCenterId(), api_response);
        Helpers.logCallRecord(this.callRecord);
        tableAppNameValue = "PREREGISTRATION";
        tableColName = "BOOKING_SERVICE";
        tableEventName = "RETRIEVE";
        return callRecord;
    }
}
