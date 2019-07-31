package main.java.io.mosip.ivv.helpers;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.CallRecord;
import main.java.io.mosip.ivv.base.CoreStructures;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.utils.ResponseParser;
import main.java.io.mosip.ivv.utils.Utils;
import main.java.io.mosip.ivv.utils.UtilsA;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;

import java.sql.SQLException;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static main.java.io.mosip.ivv.utils.Utils.dbVerification;
import static main.java.io.mosip.ivv.utils.Utils.theDir;

public class BookingService extends Controller {

    public BookingService(Scenario.Data dataSet, ExtentTest ex) {
        super(dataSet, ex);
    }

    @SuppressWarnings("unchecked")
    public static CallRecord reBookAppointment(Scenario.Step step) throws SQLException {
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        JSONObject api_input = new JSONObject();

        switch (step.variant) {
            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        Scenario.Step nstep = new Scenario.Step();
        nstep.name = "getAvailableSlot";
        nstep.variant = "DEFAULT";
        nstep.index.add(index);
        CallRecord slotObject = getAvailableSlots(nstep);
        CoreStructures.Slot slot = new ResponseParser().slot(slotObject.response.getBody().asString());
        if (!slot.available) {
            Assert.assertEquals(false, true, "Appointment slot");
        }

        api_input.put("id", prop.getProperty("bookingBookID"));
        api_input.put("ver", "1.0");
        api_input.put("reqTime", Utils.getCurrentDateAndTimeForAPI());
        api_input.put("request", new JSONArray() {{
            add(new JSONObject() {{
                put("preRegistrationId", person.pre_registration_id);
                put("oldBookingDetails", new JSONObject() {{
                    put("registration_center_id", person.registration_center_id);
                    put("appointment_date", person.slot.date);
                    put("time_slot_from", person.slot.from);
                    put("time_slot_to", person.slot.to);
                }});
                put("newBookingDetails", new JSONObject() {{
                    put("registration_center_id", person.registration_center_id);
                    put("appointment_date", slot.date);
                    put("time_slot_from", slot.from);
                    put("time_slot_to", slot.to);
                }});
            }});
        }});

        String url = "/pre-registration/" + BaseHelper.baseVersion + "/booking/appointment";
        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie(authCookies)
                .contentType(ContentType.JSON).body(api_input).post(url);

        CallRecord res = new CallRecord(url, step.name, api_input.toString(), api_response,
                "" + api_response.getStatusCode(), step);
        tableAppNameValue = "PREREGISTRATION";
        tableColName = "BOOKING_SERVICE";
        tableEventName = "PERSIST";
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            Utils.auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }

        /* Error handling middleware */
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
        } else {
            //            /* Response data parsing */
            //            ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

            /* Assertion policies execution */
            if (step.asserts.size() > 0) {
                for (assertion_policy assertion_type : step.asserts) {
                    switch (assertion_type) {
                        case DONT:
                            break;

                        case DB_VERIFICATION:
                            String queryString =
                                    "SELECT * FROM prereg.reg_appointment where prereg_id = " + person.pre_registration_id
                                            + " AND regcntr_id = " + person.registration_center_id
                                            + " AND appointment_date = " + slot.date
                                            + " AND slot_from_time = " + slot.from
                                            + " AND slot_to_time = " + slot.to;

                            dbVerification(queryString, extentTest, true);
                            break;

//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert "+assertion_type);
//                            Utils.auditLog.warning("Skipping assert "+assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

    @SuppressWarnings({"unchecked", "serial", "unused"})
    public static CallRecord bookAppointment(Scenario.Step step) throws SQLException {
        String preRegistrationID = null;
        int index = UtilsA.getPersonIndex(step);

        /* getting slot */
        Scenario.Step nstep = new Scenario.Step();
        nstep = step;
        nstep.name = "getAvailableSlots";
        //nstep.variant = "DEFAULT";
        nstep.index.add(index);

        CallRecord slotObject = getAvailableSlots(nstep);

        if (!slotObject.response.getBody().asString().contains("errorCode")) {
            step.name = "BookAppointment";
            CoreStructures.Slot slot = new ResponseParser().slot(slotObject.response.getBody().asString());
            data.persons.get(index).slot = slot;
            if (!slot.available) {
                extentTest.log(Status.FAIL, "Appointment slots are not available!");
                Assert.assertEquals(false, true, "Appointment slot found");
            }
            /* getting slot */
            Persona person = data.persons.get(index);
            preRegistrationID = person.pre_registration_id;

            JSONObject request_json = new JSONObject();
            request_json.put("registration_center_id", person.registration_center_id);
            request_json.put("appointment_date", slot.date);
            request_json.put("time_slot_from", slot.from);
            request_json.put("time_slot_to", slot.to);

            JSONObject api_input = new JSONObject();
            api_input.put("id", prop.getProperty("bookingBookID"));
            api_input.put("version", prop.getProperty("apiver"));
            api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

            switch (step.variant) {
                case "InvalidCenter":

                case "RegistrationCenterIdNotEntered":
                    request_json.put("registration_center_id", "");
                    break;

                case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                    preRegistrationID = pre_registration_id_OtherUser;
                    break;

                case "InvalidPRID":
                    preRegistrationID = "100000000";
                    break;

                case "NoSlots":

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
                    request_json.put("appointment_date", Utils.getEndDayOfWeek());
                    break;

                case "RequestIdIsInvalid":
                    api_input.put("id", prop.getProperty("bookingBookID") + "Invalid");
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
                    extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                    Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                    return new CallRecord();
            }

            api_input.put("request", request_json);

            String url = "/preregistration/" + BaseHelper.baseVersion + "/appointment/" + preRegistrationID;
            RestAssured.baseURI = baseUri;
            Response api_response = given().relaxedHTTPSValidation()
                    .contentType(ContentType.JSON)
                    .cookie("Authorization", BaseHelper.authCookies)
                    .body(api_input)
                    .post(url);

            CallRecord res = new CallRecord(url, step.name, "Pre-Reg ID: " + preRegistrationID + ", Request Content: "
                    + api_input.toString(), api_response, "" + api_response.getStatusCode(), step);
            tableAppNameValue = "PREREGISTRATION";
            tableColName = "BOOKING_SERVICE";
            tableEventName = "PERSIST";
            AddCallRecord(res, api_response, extentTest);

            /* check for api status */
            if (api_response.getStatusCode() != 200) {
                extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
                Utils.auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
                Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
            }

            /* Error handling middleware */
            if (step.error != null && !step.error.isEmpty()) {
                stepErrorMiddleware(step, res);
            } else {
                /* Response data parsing */
//                ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

                /* Assertion policies execution */
                if (step.asserts.size() > 0) {
                    for (assertion_policy assertion_type : step.asserts) {
                        switch (assertion_type) {
                            case DONT:
                                break;

                            case API_CALL:
                                Scenario.Step ac_step = new Scenario.Step();
                                ac_step.name = "getAppointment";
                                ac_step.index.add(index);
                                CallRecord getAppointment = getAppointment(ac_step);
                                HashMap<String, Object> hm = ResponseParser.bookAppointmentResponseParser(data.persons.get(index), getAppointment.response);
                                extentTest.log((Status) hm.get("status"), hm.get("msg").toString());
                                if (hm.get("status").equals(Status.FAIL)) {
                                    Utils.auditLog.severe(hm.get("msg").toString());
                                } else {
                                    Utils.auditLog.info(hm.get("msg").toString());
                                }
                                Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");

                                Assert.assertEquals(hm.get("status"), Status.INFO, hm.get("msg").toString());
                                break;

                            case DB_VERIFICATION:
                                String queryString =
                                        "SELECT * FROM prereg.reg_appointment where prereg_id = " + person.pre_registration_id
                                                + " AND regcntr_id = " + person.registration_center_id
                                                + " AND appointment_date = " + slot.date
                                                + " AND slot_from_time = " + slot.from
                                                + " AND slot_to_time = " + slot.to;

                                dbVerification(queryString, extentTest, true);
                                break;

//
//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert "+assertion_type);
//                            Utils.auditLog.warning("Skipping assert "+assertion_type);
//                            break;
                        }
                    }
                }
            }
            return res;
        }
        return slotObject;
    }

    @SuppressWarnings("unchecked")
    public static CallRecord bookAppointmentAll(Scenario.Step step) throws SQLException {
        switch (step.variant) {
            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        CallRecord res = null;
        JSONObject api_input = new JSONObject();
        api_input.put("id", prop.getProperty("bookingBookID"));
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.variant) {
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
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        int index = 0;
        JSONArray bookingDetails = new JSONArray();
        for (int i = 0; i < data.persons.size(); i++) {
            index = UtilsA.getPersonIndex(step);

            /* getting slot */
            Scenario.Step nstep = new Scenario.Step();
            nstep.name = "getAvailableSlots";
            nstep.variant = "DEFAULT";
            nstep.index.add(i);
            /* getting slot */
            CallRecord slotObject = getAvailableSlots(nstep);

            CoreStructures.Slot slot = new ResponseParser().slot(slotObject.response.getBody().asString());
            data.persons.get(i).slot = slot;
            if (!slot.available) {
                extentTest.log(Status.FAIL, "Appointment slots are not available!");
                Assert.assertEquals(false, true, "Appointment slot");
            }

            int finalI = i;
            bookingDetails.add(new JSONObject() {{
                put("preRegistrationId", data.persons.get(finalI).pre_registration_id);

                if (step.variant.equals("RegistrationCenterIdNotEntered")) {
                    put("registration_center_id", "");
                } else {
                    put("registration_center_id", data.persons.get(finalI).registration_center_id);
                }
                if (step.variant.equals("BookingDateTimeNotSelected")) {
                    put("appointment_date", "");
                } else if (step.variant.equals("InvalidDateTimeFormat")) {
                    put("appointment_date", "");
                } else {
                    put("appointment_date", slot.date);
                }
                put("time_slot_from", slot.from);
                put("time_slot_to", slot.to);
                if (step.variant.equals("InvalidBookingDateTimeFoundForPreregistrationId")) {
                    put("time_slot_from", "01:00:00");
                    put("time_slot_to", "01:15:00");
                }
            }});
        }
//        api_input.put("request", new JSONObject());
//        api_input.put("bookingRequest", bookingDetails);

        api_input.put("request", new JSONObject() {{
            //add(new JSONObject(){{
            put("bookingRequest", bookingDetails);
        }});


        String url = "/preregistration/" + BaseHelper.baseVersion + "/appointment";

        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .cookie("Authorization", BaseHelper.authCookies)
                .body(api_input)
                .post(url);

        res = new CallRecord(url, step.name, api_input.toString(), api_response, "" + api_response.getStatusCode(),
                step);
        tableAppNameValue = "PREREGISTRATION";
        tableColName = "BOOKING_SERVICE";
        tableEventName = "PERSIST";
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            Utils.auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }

        /* Error handling middleware */
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
        } else {
            //            /* Response data parsing */
            //            ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

            /* Assertion policies execution */
            if (step.asserts.size() > 0) {
                for (assertion_policy assertion_type : step.asserts) {
                    switch (assertion_type) {
                        case DONT:
                            break;

                        case API_CALL:
                            Scenario.Step ac_step = new Scenario.Step();
                            ac_step.name = "getAppointment";
                            ac_step.index.add(index);
                            CallRecord getAppointment = getAppointment(ac_step);
                            HashMap<String, Object> hm = ResponseParser.bookAppointmentResponseParser(data.persons.get(index), getAppointment.response);
                            extentTest.log((Status) hm.get("status"), hm.get("msg").toString());
                            if (hm.get("status").equals(Status.FAIL)) {
                                Utils.auditLog.severe(hm.get("msg").toString());
                            } else {
                                Utils.auditLog.info(hm.get("msg").toString());
                            }
                            Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");

                            Assert.assertEquals(hm.get("status"), Status.INFO, hm.get("msg").toString());
                            break;

//                        case DB_VERIFICATION:
//                            String queryString =
//                                    "SELECT * FROM prereg.reg_appointment where prereg_id = " + data.persons.get(finalI).pre_registration_id
//                                            + " AND regcntr_id = " + data.persons.get(finalI).registration_center_id
//                                            + " AND appointment_date = " + slot.date
//                                            + " AND slot_from_time = " + slot.from
//                                            + " AND slot_to_time = " + slot.to;
//
//                            dbVerification(queryString, extentTest, true);
//                            break;

//                            default:
//                                extentTest.log(Status.WARNING, "Skipping assert "+assertion_type);
//                                Utils.auditLog.warning("Skipping assert "+assertion_type);
//                                break;
                    }
                }
            }
        }
        return res;
    }

    public static CallRecord cancelAppointment(Scenario.Step step) throws SQLException {
        String preRegistrationID = null;
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        preRegistrationID = person.pre_registration_id;

        switch (step.variant) {
            case "InvalidPreReg":
                preRegistrationID = "100000000";
                break;

            case "BookingDataNotFound":
                preRegistrationID = person.pre_registration_id;
                break;

            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        String url = "/preregistration/" + BaseHelper.baseVersion + "/appointment/" + preRegistrationID;
        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", BaseHelper.authCookies)
                .put(url);

        CallRecord res = new CallRecord(url, step.name, preRegistrationID, api_response, "" + api_response.getStatusCode(),
                step);
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            Utils.auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }

        /* Error handling middleware */
        if (step.error != null && !step.error.isEmpty()) {
            CallRecord getCancelAppointment = getAppointment(step);
            stepErrorMiddleware(step, getCancelAppointment);
        } else {
            ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
            /* Response data parsing */

            /* Assertion policies execution */
            if (step.asserts.size() > 0) {
                for (assertion_policy assertion_type : step.asserts) {
                    switch (assertion_type) {
                        case DONT:
                            break;

//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert "+assertion_type);
//                            Utils.auditLog.warning("Skipping assert "+assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

    public static CallRecord getAppointment(Scenario.Step step) throws SQLException {
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        String preRegID = person.pre_registration_id;

        switch (step.variant) {
            case "DEFAULT":
                break;

            case "InvalidPreReg":
                preRegID = "100000000";
                break;

            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                preRegID = pre_registration_id_OtherUser;
                break;

            case "BookingDataNotFound":
                preRegID = person.pre_registration_id;
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        String url = "/preregistration/" + BaseHelper.baseVersion + "/appointment/" + preRegID;
        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", BaseHelper.authCookies)
                .get(url);
        CallRecord res = new CallRecord(url, "getAppointment", preRegID, api_response,
                "" + api_response.getStatusCode(), step);
        tableAppNameValue = null;
        AddCallRecord(res, api_response, extentTest);
        tableAppNameValue = "PREREGISTRATION";
        tableColName = "BOOKING_SERVICE";
        tableEventName = "PERSIST";

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            Utils.auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }

        /* Error handling middleware */
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
        } else {
            // ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

            /* Assertion policies execution */
            if (step.asserts.size() > 0) {
                for (assertion_policy assertion_type : step.asserts) {
                    switch (assertion_type) {
                        case DONT:
                            break;

//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert "+assertion_type);
//                            Utils.auditLog.warning("Skipping assert "+assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

    public static CallRecord getAvailableSlots(Scenario.Step step) throws SQLException {
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        String url = null;
        url = "preregistration/" + BaseHelper.baseVersion + "/appointment/availability/" + person.registration_center_id;

        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", BaseHelper.authCookies)
                .get(url);

        CallRecord res = new CallRecord(url, step.name, person.registration_center_id, api_response, "" + api_response.getStatusCode(),
                step);

        tableAppNameValue = "PREREGISTRATION";
        tableColName = "BOOKING_SERVICE";
        tableEventName = "RETRIEVE";
        AddCallRecord(res, api_response, extentTest);

        return res;
    }
}