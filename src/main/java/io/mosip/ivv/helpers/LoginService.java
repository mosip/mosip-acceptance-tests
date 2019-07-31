package main.java.io.mosip.ivv.helpers;

import static io.restassured.RestAssured.given;
import static main.java.io.mosip.ivv.utils.Utils.auditLog;
import java.sql.SQLException;
import java.util.Collection;
import com.aventstack.extentreports.Status;
import org.json.simple.JSONObject;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.CallRecord;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.utils.OTPReader;
import main.java.io.mosip.ivv.utils.Utils;
import org.testng.Assert;

public class LoginService extends Controller {
    private static String otpTemp = null;

    public LoginService(Scenario.Data data) {
        super(data, extentTest);
    }

    public static void InvalidateToken(Scenario.Step step) {
        CallRecord res;
        String url = "/preregistration/" + BaseHelper.baseVersion + "/login/invalidateToken";
        RestAssured.baseURI = BaseHelper.baseUri;
        Response api_response = given().post(url);

        Utils.auditLog.info("# [method][url]: " + "["+ step.name +"]["+ url +"]");
        Utils.auditLog.info("STATUS Code: " + api_response.getStatusCode());

        Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
    }

    public static CallRecord LoginServiceEmailMobile(Scenario.Step step) throws InterruptedException, SQLException {
        switch (step.variant) {
            case "Email":

            case "Mobile":
                tableAppNameValue = "sendOtp";
                tableColName = "sendOtp";
                tableEventName = "sendOtp";
                otherUserTest = "";
                sendOTP(step);
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "AUTHENTICATION_SERVICE";
                tableEventName = "AUTHENTICATION";
                otherUserTest = "";
                validateOTP(step);
                break;

            case "InvalidPRID":
            case "InvalidOTP":
            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
        }
        return new CallRecord();
    }

    public static CallRecord LoginServiceOtherUser(Scenario.Step step) throws InterruptedException, SQLException {
        switch (step.variant) {
            case "Email":

            case "Mobile":
                tableAppNameValue = "sendOtp";
                tableColName = "sendOtp";
                tableEventName = "sendOtp";
                otherUserTest = "yes";
                sendOtpToOtherUser(step);
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "AUTHENTICATION_SERVICE";
                tableEventName = "AUTHENTICATION";
                otherUserTest = "yes";
                validateOtpFromOtherUser(step);
                break;

            case "InvalidPRID":
            case "InvalidOTP":
            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
        }
        return new CallRecord();
    }

    @SuppressWarnings("unchecked")
    public static CallRecord sendOTP(Scenario.Step step) throws InterruptedException, SQLException {
        CallRecord res;
        BaseHelper.authCookies = "";

        //Delete Old OTP emails
        OTPReader.deleteOTPEmails();
        JSONObject request_json = new JSONObject();
        request_json.put("userId", BaseHelper.otpEmail_username);

        JSONObject api_input = new JSONObject();
        api_input.put("id", prop.getProperty("sendOtpID"));
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.variant) {
            case "DEFAULT":
            case "Email":
            case "Mobile":
                break;

            case "InvalidRequestUserIdReceived":
                request_json.put("userId", "");
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

        String url = "/preregistration/" + BaseHelper.baseVersion + "/login/sendOtp";
        RestAssured.baseURI = BaseHelper.baseUri;
        Response api_response = given()
                .contentType(ContentType.JSON).body(api_input).post(url);

        String cookie;
        Collection<String> values = api_response.getCookies().values();
        for (String value : values) {
            cookie = value;
            BaseHelper.tempAuthCookies = cookie;
        }
        res = new CallRecord(url, step.name, api_input.toString(), api_response, "" + api_response.getStatusCode(), step);
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
        }
        return res;
    }


    @SuppressWarnings("unchecked")
    public static CallRecord sendOtpToOtherUser(Scenario.Step step) throws InterruptedException, SQLException {
        CallRecord res;
        BaseHelper.authCookies = "";

        //Delete Old OTP emails
        OTPReader.deleteOTPEmails();
        JSONObject request_json = new JSONObject();
        request_json.put("userId", otpOtherEmail_username);

        JSONObject api_input = new JSONObject();
        api_input.put("id", prop.getProperty("sendOtpID"));
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.variant) {
            case "DEFAULT":
            case "Mobile":
            case "Email":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }
        api_input.put("request", request_json);

        String url = "/preregistration/" + BaseHelper.baseVersion + "/login/sendOtp";
        RestAssured.baseURI = BaseHelper.baseUri;
        //RestAssured.proxy = host("127.0.0.1").withPort(8888);
        Response api_response = given()
                .contentType(ContentType.JSON).body(api_input).post(url);

        String cookie;
        Collection<String> values = api_response.getCookies().values();
        for (String value : values) {
            cookie = value;
            BaseHelper.tempAuthCookies = cookie;
        }

        res = new CallRecord(url, step.name, api_input.toString(), api_response, "" + api_response.getStatusCode(), step);
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public static CallRecord validateOTP(Scenario.Step step) throws InterruptedException, SQLException {
        //Thread.sleep(1000);
        boolean setTokenIsNotPresentInTheHeader = false;
        String otp = OTPReader.readOTP();

        if (otpTemp == null) {
            otpTemp = otp.trim();
        } else if (otpTemp.trim().equals(otp.trim())) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            otp = OTPReader.readOTP();
        }

        CallRecord res;
        JSONObject request_json = new JSONObject();
        request_json.put("otp", otp.trim());
        request_json.put("userId", BaseHelper.otpEmail_username);
        JSONObject api_input = new JSONObject();
        api_input.put("id", prop.getProperty("validateOtpID"));
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.variant) {
            case "DEFAULT":
            case "Email":
            case "Mobile":
                break;

            case "UserDetailDoesNotExist":
                request_json.put("userId", "");
                break;

            case "OtpCanNotBeEmptyOrNull":
                request_json.put("otp", "");
                break;

            case "OtpConsistsOtherThanNumerics":
                request_json.put("otp", "mosip");
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

            case "RequestDateShouldBeCurrentDate":
                api_input.put("requesttime", "2019-01-01T05:59:15.241Z");
                break;

            case "ValidationUnsuccessful":
                request_json.put("otp", "99999");
                break;

            case "TokenIsNotPresentInTheHeader":
                setTokenIsNotPresentInTheHeader = true;
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }
        api_input.put("request", request_json);

        String url = "/preregistration/" + BaseHelper.baseVersion + "/login/validateOtp";
        RestAssured.baseURI = BaseHelper.baseUri;
        Response api_response;
        if (!setTokenIsNotPresentInTheHeader) {
            api_response =
                    given()
                            .cookie("Authorization", BaseHelper.tempAuthCookies)
                            .contentType("application/json")
                            .body(api_input)
                            .post(url);
        } else {
            api_response =
                    given()
                            .cookie("Authorization", "")
                            .contentType("application/json")
                            .body(api_input)
                            .post(url);
        }

        res = new CallRecord(url, step.name, api_input.toString(), api_response, "" + api_response.getStatusCode(), step);
        String cookie;
        Collection<String> values = api_response.getCookies().values();
        for (String value : values) {
            cookie = value;
            BaseHelper.authCookies = cookie;
            otpTemp = otp;
        }

        AddCallRecord(res, api_response, extentTest);
        auditLog.info("Authorization Token: " + BaseHelper.authCookies);
        auditLog.info("-----------------------------------------------------------------------------------------------------------------");
        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    public static CallRecord validateOtpFromOtherUser(Scenario.Step step) throws InterruptedException, SQLException {
        boolean setTokenIsNotPresentInTheHeader = false;
        String otp = OTPReader.readOtpFromOtherUser();

        if (otpTemp == null) {
            otpTemp = otp.trim();
        } else if (otpTemp.trim().equals(otp.trim())) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            otp = OTPReader.readOtpFromOtherUser();
        }

        CallRecord res;
        JSONObject request_json = new JSONObject();
        request_json.put("otp", otp.trim());
        request_json.put("userId", otpOtherEmail_username);
        JSONObject api_input = new JSONObject();
        api_input.put("id", prop.getProperty("validateOtpID"));
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.variant) {
            case "DEFAULT":
            case "Email":
            case "Mobile":
                break;

            case "UserDetailDoesNotExist":
                request_json.put("userId", "");
                break;

            case "OtpCanNotBeEmptyOrNull":
                request_json.put("otp", "");
                break;

            case "OtpConsistsOtherThanNumerics":
                request_json.put("otp", "mosip");
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

            case "RequestDateShouldBeCurrentDate":
                api_input.put("requesttime", "2019-01-01T05:59:15.241Z");
                break;

            case "ValidationUnsuccessful":
                request_json.put("otp", "99999");
                break;

            case "TokenIsNotPresentInTheHeader":
                setTokenIsNotPresentInTheHeader = true;
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }
        api_input.put("request", request_json);

        String url = "/preregistration/" + BaseHelper.baseVersion + "/login/validateOtp";
        RestAssured.baseURI = BaseHelper.baseUri;
        Response api_response;
        if (!setTokenIsNotPresentInTheHeader) {
            api_response =
                    given()
                            .cookie("Authorization", BaseHelper.tempAuthCookies)
                            .contentType("application/json")
                            .body(api_input)
                            .post(url);
        } else {
            api_response =
                    given()
                            .cookie("Authorization", "")
                            .contentType("application/json")
                            .body(api_input)
                            .post(url);

            auditLog.info("Authorization: " + BaseHelper.tempAuthCookies);
        }

        res = new CallRecord(url, step.name, api_input.toString(), api_response, "" + api_response.getStatusCode(), step);
        String cookie;
        Collection<String> values = api_response.getCookies().values();
        for (String value : values) {
            cookie = value;
            BaseHelper.authCookies = cookie;
            otpTemp = otp;
        }
        tableAppNameValue = "PREREGISTRATION";
        tableColName = "AUTHENTICATION_SERVICE";
        tableEventName = "AUTHENTICATION";

        AddCallRecord(res, api_response, extentTest);
        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
        }
        return res;
    }
}