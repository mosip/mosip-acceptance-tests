package main.java.io.mosip.ivv.helpers;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.CallRecord;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.utils.Utils;
import main.java.io.mosip.ivv.utils.UtilsA;
import org.json.simple.JSONObject;
import org.testng.Assert;
import java.sql.SQLException;
import static io.restassured.RestAssured.given;
import static main.java.io.mosip.ivv.utils.Utils.getCurrentDateAndTimeForAPI;

public class NotificationService extends Controller {

    public NotificationService(Scenario.Data dataSet, ExtentTest ex) {
        super(dataSet, ex);
    }

    @SuppressWarnings("unchecked")
    public static CallRecord generateQRCode(Scenario.Step step) throws SQLException {
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        JSONObject request_json = new JSONObject();
        JSONObject api_input = new JSONObject();

        request_json.put("name", person.name);
        request_json.put("preId", person.pre_registration_id);
        request_json.put("appointmentDate", data.persons.get(index).slot.date);
        request_json.put("appointmentTime", data.persons.get(index).slot.from);
        request_json.put("mobNum", person.phone);
        request_json.put("emailID", person.email);
        api_input.put("id", "mosip.pre-registration.qrcode.generate");
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", getCurrentDateAndTimeForAPI());

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
        api_input.put("request", request_json);

        String url = "/preregistration/" + BaseHelper.baseVersion + "/qrCode/generate";
        RestAssured.baseURI = baseUri;
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response = given()
                .cookie("Authorization", BaseHelper.authCookies)
                .contentType(ContentType.JSON)
                .body(api_input.toString())
                .post(url);

        CallRecord res = new CallRecord(url, step.name, api_input.toString(), api_response,
                "" + api_response.getStatusCode(), step);
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

//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                            Utils.auditLog.warning("Skipping assert " + assertion_type);
//                            break;
                    }
                }
            }
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    public static CallRecord notify(Scenario.Step step) throws SQLException {
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);

        switch (step.variant) {
            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        JSONObject api_input = new JSONObject();
        api_input.put("name", person.name);
        api_input.put("preId", person.pre_registration_id);
        api_input.put("appointmentDate", "");
        api_input.put("appointmentTime", "");
        api_input.put("mobNum", person.phone);
        api_input.put("emailID", person.email);

        String url = "/pre-registration/" + BaseHelper.baseVersion + "/notification/notify";
        RestAssured.baseURI = baseUri;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response = given()
                .contentType(ContentType.JSON)
                .body(api_input)
                .post("/pre-registration/" + BaseHelper.baseVersion + "/notification/notify");
        CallRecord res = new CallRecord(url, step.name, api_input.toString(), api_response,
                "" + api_response.getStatusCode(), step);
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

//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                            Utils.auditLog.warning("Skipping assert " + assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

}
