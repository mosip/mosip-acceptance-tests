/*
 *  Author: Sanjeev
 */
package main.java.io.mosip.ivv.helpers;

import static io.restassured.RestAssured.given;
import static main.java.io.mosip.ivv.utils.Utils.dbVerification;

import java.sql.SQLException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.Assert;
import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.CallRecord;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.orchestrator.Scenario.Data;
import main.java.io.mosip.ivv.utils.ResponseParser;
import main.java.io.mosip.ivv.utils.Utils;
import main.java.io.mosip.ivv.utils.UtilsA;

public class DemographicService extends Controller {
    public static String preRegistrationId = null;

    public DemographicService(Data data) {
        super(data, extentTest);
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings({"unchecked", "serial", "unused"})
    public static CallRecord addApplication(Scenario.Step step) throws SQLException {
        CallRecord res = null;
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);

        JSONObject identity_json = new JSONObject();
        identity_json.put("dateOfBirth", person.date_of_birth);
        identity_json.put("email", "mosip-test@technoforte.co.in");
        identity_json.put("phone", person.phone);
        identity_json.put("CNIENumber", person.cnie_number);
        identity_json.put("IDSchemaVersion", 1);
        identity_json.put("postalCode", person.postal_code);

        HashMap<String, String> demographic = new HashMap<>();
        demographic.put("fullName", person.name);
        demographic.put("gender", "FLE");
        demographic.put("addressLine1", person.address_line_1);
        demographic.put("addressLine2", person.address_line_2);
        demographic.put("addressLine3", person.address_line_3);
        demographic.put("region", person.region);
        demographic.put("province", person.province);
        demographic.put("city", person.city);
        demographic.put("localAdministrativeAuthority", "SATZ");
        demographic.put("residenceStatus", person.residence_status);

        JSONObject request_json = new JSONObject();
        request_json.put("langCode", demographicLangcode);

        JSONObject api_input = new JSONObject();
        api_input.put("id", prop.getProperty("demographicCreateID"));
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.variant) {
            case "invalidGender":
                demographic.put("gender", data.globals.get("INVALID_GENDER"));
                break;

            case "invalidEmail":
                identity_json.put("email", data.globals.get("INVALID_EMAIL"));
                break;

            case "invalidPhone":
                identity_json.put("phone", data.globals.get("INVALID_PHONE"));
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

            case "LangCodeIsInvalid":
                request_json.put("langCode", "AAA");
                break;

            case "MissingInputParameterIdentityFullname":
                demographic.remove("fullName");
                break;

            case "MissingInputParameterIdentityDateofbirthIdentityAge":
                identity_json.remove("dateOfBirth");
                break;

            case "MissingInputParameterIdentityGender":
                demographic.remove("gender");
                break;

            case "MissingInputParameterIdentityResidencestatus":
                demographic.remove("residenceStatus");
                break;

            case "MissingInputParameterIdentityAddressline":
                demographic.remove("addressLine1");
                break;

            case "MissingInputParameterIdentityRegion":
                demographic.remove("region");
                break;

            case "MissingInputParameterIdentityProvince":
                demographic.remove("province");
                break;

            case "MissingInputParameterIdentityCity":
                demographic.remove("city");
                break;

            case "MissingInputParameterIdentityLocaladministrativeauthority":
                demographic.remove("localAdministrativeAuthority");
                break;

            case "MissingInputParameterIdentityPostalcode":
                identity_json.remove("postalCode");
                break;

            case "MissingInputParameterIdentityCnienumber":
                identity_json.remove("CNIENumber");
                break;

            case "InvalidInputParameterIdentityPhone":
                identity_json.put("phone", "");
                break;

            case "InvalidInputParameterIdentityEmail":
                identity_json.put("email", "");
                break;

            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        demographic.forEach((k, v) -> identity_json.put(k, new JSONArray() {{
                    add(new JSONObject(
                                    new HashMap<String, String>() {{
                                        put("value", v);
                                        put("language", demographicLangcode);

                                    }}
                            )
                    );
                }}
                )
        );

        JSONObject demographic_json = new JSONObject();
        demographic_json.put("identity", identity_json);
        request_json.put("demographicDetails", demographic_json);
        api_input.put("request", request_json);

        String url = "/preregistration/" + BaseHelper.baseVersion + "/applications";
        RestAssured.baseURI = BaseHelper.baseUri;
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response =
                (Response) given()
                        .contentType(ContentType.JSON).body(api_input)
                        .cookie("Authorization", BaseHelper.authCookies)
                        .post(url);
        res = new CallRecord(url, step.name, api_input.toString(), api_response, "" + api_response.getStatusCode(),
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
            stepErrorMiddleware(step, res);
        } else {
            ReadContext ctx = com.jayway.jsonpath.JsonPath.parse(api_response.getBody().asString());
            /* Response data parsing */
            try {
                data.persons.get(index).pre_registration_id = ctx.read("$['response']['preRegistrationId']");
                if (otherUserTest.equals("yes")) {
                    pre_registration_id_OtherUser = ctx.read("$['response']['preRegistrationId']");
                }
                data.persons.get(index).pre_registration_status_code = ctx.read("$['response']['statusCode']");
            } catch (PathNotFoundException e) {
                //ToDo
            }

            /* Assertion policies execution */
            if (step.asserts.size() > 0) {
                for (assertion_policy assertion_type : step.asserts) {
                    switch (assertion_type) {
                        case DONT:
                            break;

                        case API_CALL:
                            Scenario.Step nstep = new Scenario.Step();
                            nstep.name = "getApplication";
                            nstep.index.add(index);
                            CallRecord getAppRecord = getApplication(nstep);
                            HashMap<String, Object> hm = ResponseParser.addApplicationResponceParser(data.persons.get(index), getAppRecord.response);
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
                                    "SELECT * FROM prereg.applicant_demographic where prereg_id = " + ctx.read("$['response'][0]['preRegistrationId']") + " AND status_code = " + ctx.read("$['response'][0]['statusCode']");

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
    public static CallRecord addApplicationAll(Scenario.Step step) throws SQLException {
        CallRecord res = null;
        for (int i = 0; i < data.persons.size(); i++) {
            Persona person = data.persons.get(i);

            JSONObject identity_json = new JSONObject();
            identity_json.put("dateOfBirth", person.date_of_birth);
            identity_json.put("email", "mosip-test@technoforte.co.in");
            identity_json.put("phone", person.phone);
            identity_json.put("CNIENumber", person.cnie_number);
            identity_json.put("IDSchemaVersion", 1.0);
            identity_json.put("postalCode", person.postal_code);

            HashMap<String, String> demographic = new HashMap<>();
            demographic.put("fullName", person.name);
            //demographic.put("gender", person.getGender());
            demographic.put("gender", "FLE");
            demographic.put("addressLine1", person.address_line_1);
            demographic.put("addressLine2", person.address_line_2);
            demographic.put("addressLine3", person.address_line_3);
            demographic.put("region", person.region);
            demographic.put("province", person.province);
            demographic.put("city", person.city);
            //demographic.put("localAdministrativeAuthority", person.local_administrative_authority);
            demographic.put("localAdministrativeAuthority", "SATZ");
            demographic.put("residenceStatus", person.residence_status);

            switch (step.variant) {
                case "invalidGender":
                    demographic.put("gender", data.globals.get("INVALID_GENDER"));
                    break;

                case "invalidEmail":
                    identity_json.put("gender", data.globals.get("INVALID_EMAIL"));
                    break;

                case "invalidPhone":
                    identity_json.put("gender", data.globals.get("INVALID_PHONE"));
                    break;

                case "DEFAULT":
                    break;

                default:
                    extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                    Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                    return new CallRecord();
            }

            demographic.forEach((k, v) -> identity_json.put(k, new JSONArray() {{
                        add(new JSONObject(
                                        new HashMap<String, String>() {{
                                            put("language", demographicLangcode);
                                            put("value", v);
                                        }}
                                )
                        );
                    }}
                    )
            );


            JSONObject demographic_json = new JSONObject();
            demographic_json.put("identity", identity_json);

            JSONObject request_json = new JSONObject();
            request_json.put("langCode", "fra");
            request_json.put("demographicDetails", demographic_json);

            JSONObject api_input = new JSONObject();
            api_input.put("id", prop.getProperty("demographicCreateID"));
            api_input.put("version", prop.getProperty("apiver"));
            api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
            api_input.put("request", request_json);

            String url = "/preregistration/" + BaseHelper.baseVersion + "/applications";
            RestAssured.baseURI = BaseHelper.baseUri;
            RestAssured.useRelaxedHTTPSValidation();
            Response api_response =
                    (Response) given()
                            .contentType(ContentType.JSON).body(api_input).cookie("Authorization", BaseHelper.authCookies)
                            .post(url);
            res = new CallRecord(url, step.name, api_input.toString(), api_response,
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
                ReadContext ctx = com.jayway.jsonpath.JsonPath.parse(api_response.getBody().asString());
                /* Response data parsing */
                try {
                    data.persons.get(i).pre_registration_id = ctx.read("$['response']['preRegistrationId']");
                    if (otherUserTest.equals("yes")) {
                        pre_registration_id_OtherUser = ctx.read("$['response']['preRegistrationId']");
                    }
                    data.persons.get(i).pre_registration_status_code = ctx.read("$['response']['statusCode']");
                } catch (PathNotFoundException e) {
                    //ToDo
                }

                /* Assertion policies execution */
                if (step.asserts.size() > 0) {
                    for (assertion_policy assertion_type : step.asserts) {
                        switch (assertion_type) {
                            case DONT:
                                break;

                            case API_CALL:
                                Scenario.Step nstep = new Scenario.Step();
                                nstep.name = "getApplication";
                                nstep.index.add(i);
                                CallRecord getAppRecord = getApplication(nstep);
                                HashMap<String, Object> hm = ResponseParser.addApplicationAllResponceParser(data.persons, getAppRecord.response);
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
                                        "SELECT * FROM prereg.applicant_demographic where prereg_id = " + ctx.read("$['response'][0]['preRegistrationId']") + " AND status_code = " + ctx.read("$['response'][0]['statusCode']");

                                dbVerification(queryString, extentTest, true);
                                break;

//                            default:
//                                extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                                Utils.auditLog.warning("Skipping assert " + assertion_type);
//                                break;
                        }
                    }
                }
            }
        }

        return res;
    }

    @SuppressWarnings({"unchecked", "serial", "unused"})
    public static CallRecord updateApplication(Scenario.Step step) throws SQLException {
        String preRegistrationID = null;

        CallRecord res = null;
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        preRegistrationID = person.pre_registration_id;

        JSONObject identity_json = new JSONObject();
        //identity_json.put("dateOfBirth", UtilsA.updateDOB(person));
        identity_json.put("dateOfBirth", "1993/12/12");
        identity_json.put("email", "mosip-test@technoforte.co.in");
        identity_json.put("phone", person.phone);
        identity_json.put("CNIENumber", person.cnie_number);
        identity_json.put("IDSchemaVersion", 1.0);
        identity_json.put("postalCode", person.postal_code);

        HashMap<String, String> demographic = new HashMap<>();
        demographic.put("fullName", person.name);
        demographic.put("gender", "FLE");
        demographic.put("addressLine1", person.address_line_1);
        demographic.put("addressLine2", person.address_line_2);
        demographic.put("addressLine3", person.address_line_3);
        demographic.put("region", person.region);
        demographic.put("province", person.province);
        demographic.put("city", person.city);
        demographic.put("localAdministrativeAuthority", "SATZ");
        demographic.put("residenceStatus", person.residence_status);

        JSONObject request_json = new JSONObject();
        request_json.put("langCode", "fra");

        JSONObject api_input = new JSONObject();
        api_input.put("id", prop.getProperty("demographicUpdateID"));
        api_input.put("version", prop.getProperty("apiver"));
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        switch (step.variant) {
            case "invalidGender":
                demographic.put("gender", data.globals.get("INVALID_GENDER"));
                break;

            case "invalidEmail":
                identity_json.put("email", data.globals.get("INVALID_EMAIL"));
                break;

            case "invalidPhone":
                identity_json.put("phone", data.globals.get("INVALID_PHONE"));
                break;

            case "InvalidPRID":
                preRegistrationID = "100000000";
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

            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                preRegistrationID = pre_registration_id_OtherUser;
                //preRegistrationID = "64925836982146";
                break;

            case "MissingInputParameterIdentityFullname":
                demographic.remove("fullName");
                break;

            case "MissingInputParameterIdentityDateofbirthIdentityAge":
                identity_json.remove("dateOfBirth");
                break;

            case "MissingInputParameterIdentityGender":
                demographic.remove("gender");
                break;

            case "MissingInputParameterIdentityResidencestatus":
                demographic.remove("residenceStatus");
                break;

            case "MissingInputParameterIdentityAddressline":
                demographic.remove("addressLine1");
                demographic.remove("addressLine2");
                demographic.remove("addressLine3");
                break;

            case "MissingInputParameterIdentityRegion":
                demographic.remove("region");
                break;

            case "MissingInputParameterIdentityProvince":
                demographic.remove("province");
                break;

            case "MissingInputParameterIdentityCity":
                demographic.remove("city");
                break;

            case "MissingInputParameterIdentityLocaladministrativeauthority":
                demographic.remove("localAdministrativeAuthority");
                break;

            case "MissingInputParameterIdentityPostalcode":
                identity_json.remove("postalCode");
                break;

            case "MissingInputParameterIdentityCnienumber":
                identity_json.remove("CNIENumber");
                break;

            case "InvalidInputParameterIdentityPhone":
                identity_json.put("Phone", "");
                break;

            case "InvalidInputParameterIdentityEmail":
                identity_json.put("email", "");
                break;

            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        demographic.forEach((k, v) -> identity_json.put(k, new JSONArray() {{
                    add(new JSONObject(
                                    new HashMap<String, String>() {{
                                        put("language", demographicLangcode);
                                        put("value", v);
                                    }}
                            )
                    );
                }}
                )
        );
        JSONObject demographic_json = new JSONObject();
        demographic_json.put("identity", identity_json);
        request_json.put("demographicDetails", demographic_json);
        api_input.put("request", request_json);

        String url = "/preregistration/" + BaseHelper.baseVersion + "/applications/" + preRegistrationID;
        RestAssured.baseURI = BaseHelper.baseUri;
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response =
                (Response) given()
                        .contentType(ContentType.JSON).body(api_input)
                        .cookie("Authorization", BaseHelper.authCookies)
                        .put(url);

        res = new CallRecord(url, step.name,
                "Pre-Reg ID: " + preRegistrationID
                        + ", Request Body: " + api_input.toString(),
                api_response, "" + api_response.getStatusCode(), step);
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
            ReadContext ctx = com.jayway.jsonpath.JsonPath.parse(api_response.getBody().asString());
            /* Response data parsing */
            try {
                data.persons.get(index).pre_registration_id = ctx.read("$['response'][0]['preRegistrationId']");
                if (otherUserTest.equals("yes")) {
                    pre_registration_id_OtherUser = ctx.read("$['response'][0]['preRegistrationId']");
                }
                data.persons.get(index).pre_registration_status_code = ctx.read("$['response'][0]['statusCode']");
            } catch (PathNotFoundException e) {
                //ToDo
            }

            /* Assertion policies execution */
            if (step.asserts.size() > 0) {
                for (assertion_policy assertion_type : step.asserts) {
                    switch (assertion_type) {
                        case DONT:
                            break;

                        case API_CALL:
                            Scenario.Step nstep = new Scenario.Step();
                            nstep.name = "updateApplication";
                            nstep.index.add(index);
                            CallRecord getAppRecord = getApplication(nstep);
                            HashMap<String, Object> hm = ResponseParser.updateApplicationResponceParser(data.persons.get(index), getAppRecord.response);
                            extentTest.log((Status) hm.get("status"), hm.get("msg").toString());
                            if (hm.get("status").equals(Status.FAIL)) {
                                Utils.auditLog.severe(hm.get("msg").toString());
                            } else {
                                Utils.auditLog.info(hm.get("msg").toString());
                            }
                            Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
                            Assert.assertEquals(hm.get("status"), Status.INFO, hm.get("msg").toString());
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

    @SuppressWarnings("unused")
    public static CallRecord deleteApplication(Scenario.Step step) throws SQLException {
        String preRegistrationID = data.persons.get(0).pre_registration_id;
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);

        switch (step.variant) {
            case "InvalidPRID":
                preRegistrationID = "100000000";
                break;

            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                preRegistrationID = pre_registration_id_OtherUser;
                break;

            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        String url = "/preregistration/" + BaseHelper.baseVersion + "/applications/" + preRegistrationID;
        RestAssured.baseURI = baseUri;
        RestAssured.useRelaxedHTTPSValidation();
        api_response = RestAssured.given()
                .cookie("Authorization", BaseHelper.authCookies)
                .delete(url);
        CallRecord res = new CallRecord(url, step.name, preRegistrationID, api_response,
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
            ReadContext ctx = stepErrorMiddleware(step, res);
        } else {
            ReadContext ctx = com.jayway.jsonpath.JsonPath.parse(api_response.getBody().asString());
            /* Response data parsing */

            /* Assertion policies execution */
            if (step.asserts.size() > 0) {
                for (assertion_policy assertion_type : step.asserts) {
                    switch (assertion_type) {
                        case DONT:
                            break;

                        case API_CALL:
                            Scenario.Step nstep = new Scenario.Step();
                            nstep.name = "deleteApplication";
                            nstep.index.add(index);
                            //CallRecord getAppRecord = getApplication(nstep);
                            CallRecord getAppRecord = res;
                            HashMap<String, Object> hm = ResponseParser.deleteApplicationResponceParser(data.persons.get(index), getAppRecord.response);
                            extentTest.log((Status) hm.get("status"), hm.get("msg").toString());
                            if (hm.get("status").equals(Status.FAIL)) {
                                Utils.auditLog.severe(hm.get("msg").toString());
                            } else {
                                Utils.auditLog.info(hm.get("msg").toString());
                            }
                            Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
                            Assert.assertEquals(hm.get("status"), Status.INFO, hm.get("msg").toString());
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

    @SuppressWarnings({"unused"})
    public static CallRecord getApplication(Scenario.Step step) throws SQLException {
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        String preRegID = person.pre_registration_id;

        switch (step.variant) {
            case "DEFAULT":
                break;

            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                preRegID = pre_registration_id_OtherUser;
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                Utils.auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        String url = "/preregistration/" + BaseHelper.baseVersion + "/applications/" + preRegID;
        RestAssured.baseURI = baseUri;
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response = (Response) given()
                .cookie("Authorization", BaseHelper.authCookies)
                .get(url);
        CallRecord res = new CallRecord(url, step.name, person.pre_registration_id, api_response,
                "" + api_response.getStatusCode(), step);
        tableAppNameValue = null;
        AddCallRecord(res, api_response, extentTest);
        tableAppNameValue = "PREREGISTRATION";

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
            ReadContext ctx = com.jayway.jsonpath.JsonPath.parse(api_response.getBody().asString());

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
}