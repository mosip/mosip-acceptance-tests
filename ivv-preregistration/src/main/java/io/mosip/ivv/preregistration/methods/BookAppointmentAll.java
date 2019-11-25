package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.ivv.core.structures.BookingSlot;
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
     */
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        int index = 0;
        JSONArray bookingDetails = new JSONArray();
        ArrayList<Person> persons = this.store.getScenarioData().getPersona().getPersons();
        for (int i = 0; i < persons.size(); i++) {
            index = Utils.getPersonIndex(step);

            /* getting slot */
            BookingSlot slot = getBookingSlot();
            if (slot == null) {
                logInfo("No Booking slot available");
                this.hasError = true;
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
                    put("appointment_date", slot.getDate());
                }
                put("time_slot_from", slot.getFrom());
                put("time_slot_to", slot.getTo());
                if (step.getVariant().equals("InvalidBookingDateTimeFoundForPreregistrationId")) {
                    put("time_slot_from", "01:00:00");
                    put("time_slot_to", "01:15:00");
                }
            }});

            api_input.put("request", new JSONObject() {{
                //add(new JSONObject(){{
                put("bookingRequest", bookingDetails);
            }});

            String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/appointment";
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
                                if (getAppointment == null) {
                                    this.hasError = true;
                                    return;
                                } else {
                                    Boolean responseMatched = responseMatch(persons.get(index),getAppointment.getResponse());
                                    if (!responseMatched) {
                                        logInfo("Assert API_CALL failed");
                                        this.hasError = true;
                                        return;
                                    }
                                }
                                break;

                            case DB_VERIFICATION:
                                String queryString = createSQLQuery(persons, finalI, slot);
                                try {
                                    Helpers.dbVerification(queryString, "", "");
                                } catch (SQLException e) {
                                    Utils.auditLog.severe("Failed while querying from DB " + e.getMessage());
                                }

                            default:
                                logInfo("Assert not found or implemented: " + pr_assert.type);
                                break;
                        }

                    }
                }
            }
        }


    }

    private String createSQLQuery(ArrayList<Person> persons, int index, BookingSlot slot) {
        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("SELECT * FROM prereg.reg_appointment where prereg_id = ").append("'")
                .append(persons.get(index).getPreRegistrationId()).append("'").append(" AND regcntr_id = ").append("'")
                .append(persons.get(index).getRegistrationCenterId()).append("'").append("AND appointment_date =").append("'")
                .append(slot.getDate()).append("'").append("AND slot_from_time =").append("'").append(slot.getFrom())
                .append("'").append("AND slot_to_time =").append("'").append(slot.getTo()).append("'");
        return sqlQuery.toString();
    }


    private CallRecord getAppointment() {
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

        String identifier = "Sub Step: " + nstep.getName() + ", module: " + nstep.getModule() + ", variant: " + nstep.getVariant();
        if (st.hasError()) {
            logSevere(identifier + " - failed");
            return null;
        } else {
            return st.getCallRecord();
        }
    }

    private Boolean responseMatch(Person person,Response response) {
        ReadContext ctx = JsonPath.parse(response.getBody().asString());
        HashMap<String, String> appointment_info = ctx.read("$['response']");
        if (person != null && appointment_info != null) {
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

        }
        return true;
    }

    private BookingSlot getBookingSlot() {
        Scenario.Step nstep = Helpers.generateStep("getBookingSlots", this.index);
        GetBookingSlots st = new GetBookingSlots();
        setExtentInstance(extentInstance);
        st.setState(this.store);
        st.setStep(nstep);
        st.run();
        this.store = st.getState();

        String identifier = "Sub Step: " + nstep.getName() + ", module: " + nstep.getModule() + ", variant: " + nstep.getVariant();
        if (st.hasError()) {
            logSevere(identifier + " - failed");
            return null;
        } else {
            return parseSlots(st.getCallRecord().getResponse().getBody().asString());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private BookingSlot parseSlots(String rawData) {
        Gson gsn = new Gson();
        Map<String, Object> resMap = gsn.fromJson(rawData, Map.class);
        try {
            List center_details = (List) ((Map) resMap.get("response")).get("centerDetails");
            if (center_details.size() == 0) {
                return null;
            }
            for (Object center_info : center_details) {
                String date = (String) ((Map) center_info).get("date");
                List timeSlots = (List) ((Map) center_info).get("timeSlots");
                if (timeSlots.size() > 0) {
                    String from = (String) ((Map) timeSlots.get(0)).get("fromTime");
                    String to = (String) ((Map) timeSlots.get(0)).get("toTime");
                    return new BookingSlot(date, from, to);
                }
            }
        } catch (NullPointerException ne) {
            logSevere("Error while running Booking slot: " + ne.getMessage());
            return null;
        }
        return null;
    }
}
