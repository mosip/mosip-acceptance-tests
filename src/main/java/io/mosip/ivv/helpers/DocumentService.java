/*
 *  Author: Channakeshava
 */
package main.java.io.mosip.ivv.helpers;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.base.ProofDocument;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import org.json.simple.JSONObject;
import main.java.io.mosip.ivv.utils.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.CallRecord;
import org.testng.Assert;
import static io.restassured.RestAssured.*;
import static main.java.io.mosip.ivv.utils.Utils.*;

public class DocumentService extends Controller {
    public DocumentService(Scenario.Data data) {
        super(data, extentTest);
    }

    @SuppressWarnings("unchecked")
    public static CallRecord addDocument(Scenario.Step step) throws SQLException {
        String fileLocation = null;
        String preRegistrationID = null;

        CallRecord res = null;
        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        preRegistrationID = person.pre_registration_id;

        for (int j = 0; j < person.documents.size(); j++) {
            ProofDocument proofDocument = data.persons.get(index).documents.get(j);
            fileLocation = proofDocument.path;

            JSONObject request_json = new JSONObject();
            request_json.put("docCatCode", proofDocument.doc_cat_code.toString());
            request_json.put("docTypCode", proofDocument.doc_type_code);
            request_json.put("langCode", person.lang_code);

            JSONObject api_input = new JSONObject();
            api_input.put("id", prop.getProperty("documentUploadID"));
            api_input.put("version", prop.getProperty("apiver"));
            api_input.put("requesttime", getCurrentDateAndTimeForAPI());

            switch (step.variant) {
                case "InvalidSize":

                case "ValidateDocumentExceedingPermittedSize":
                    fileLocation = ParserEngine.DOCUMENT_DATA_PATH + "Invalidsize.pdf";
                    break;

                case "InvalidPRID":

                case "ValidateInvalidPreRegID":
                    preRegistrationID = "100000000";
                    break;

                case "InvalidDocCategory":
                    request_json.put("docCatCode", data.globals.get("INVALID_DOCCATEGORY"));
                    break;

                case "InvalidDocType":
                    request_json.put("docTypCode", data.globals.get("INVALID_DOCTYPE"));
                    break;

                case "ValidateInvalidOrEmptyRequestID":
                    api_input.put("id", "");
                    break;

                case "ValidateInvalidOrEmptyRequestVersion":
                    api_input.put("version", "");
                    break;

                case "ValidateInvalidOrEmptyRequestDateTime":
                    api_input.put("requesttime", "");
                    break;

                case "ValidatePastRequestDate":
                    api_input.put("requesttime", "2019-01-01T05:59:15.241Z");
                    break;

                case "ValidateInvalidDocumentCategoryCode":
                    request_json.put("docCatCode", "");
                    break;

                case "ValidateInvalidDocumentType":
                    request_json.put("docTypCode", "");
                    break;

                case "ValidateInvalidLanguageCode":
                    request_json.put("langCode", "");
                    break;

                case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                    preRegistrationID = pre_registration_id_OtherUser;
                    break;

                case "DEFAULT":
                    break;

                default:
                    extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                    auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                    return new CallRecord();
            }
            api_input.put("request", request_json);

            // submit /documents POST service
            String url = "/preregistration/" + BaseHelper.baseVersion + "/documents/" + preRegistrationID;
            RestAssured.baseURI = baseUri;
            Response api_response = given().relaxedHTTPSValidation()
                    .cookie("Authorization", BaseHelper.authCookies)
                    .multiPart("file", new File(fileLocation))
                    .multiPart("Document request", api_input.toString())
                    .contentType("multipart/form-data")
                    .post(url);

            res = new CallRecord(url, step.name, "Pre-Reg ID: " + preRegistrationID + ", Request body: " + api_input.toString(),
                    api_response, "" + api_response.getStatusCode(), step);

            if ((!api_response.asString().toString().contains("413 Request Entity Too Large"))) {
                AddCallRecord(res, api_response, extentTest);

                /* check for api status */
                if (api_response.getStatusCode() != 200) {
                    extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
                    auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
                    Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
                }

                /* Error handling middleware */
                if (step.error != null && !step.error.isEmpty()) {
                    stepErrorMiddleware(step, res);
                } else {
                    ReadContext ctx = com.jayway.jsonpath.JsonPath.parse(api_response.getBody().asString());
                    /* Response data parsing */
                    try {
                        data.persons.get(index).documents.get(j).doc_id = ctx.read("$['response']['docId']");
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
                                    nstep.name = "getDocument";
                                    nstep.index.add(index);
                                    try {
                                        Thread.sleep(3000);
                                    } catch (InterruptedException e) {
                                        auditLog.severe(e.getMessage());
                                    }

                                    CallRecord getAppRecord = getDocuments(nstep);
                                    HashMap<String, Object> hm =
                                            ResponseParser.addDocumentResponseParser(j,
                                                    data.persons.get(index).documents.get(j), getAppRecord.response);
                                    extentTest.log((Status) hm.get("status"), hm.get("msg").toString());
                                    if (hm.get("status").equals(Status.FAIL)) {
                                        auditLog.severe(hm.get("msg").toString());
                                    } else {
                                        auditLog.info(hm.get("msg").toString());
                                    }
                                    auditLog.info("-----------------------------------------------------------------------------------------------------------------");

                                    Assert.assertEquals(hm.get("status"), Status.INFO, hm.get("msg").toString());
                                    break;

                                case DB_VERIFICATION:
                                    String queryString =
                                            "SELECT * FROM applicant_document where prereg_id = " + person.pre_registration_id
                                                    + " AND doc_cat_code = " + proofDocument.doc_cat_code.toString()
                                                    + " AND doc_type_code = " + proofDocument.doc_type_code
                                                    + " AND lang_code = " + person.lang_code;

                                    dbVerification(queryString, extentTest, true);
                                    break;

//                            default:
//                                extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                                auditLog.warning("Skipping assert " + assertion_type);
//                                break;
                            }
                        }
                    }
                }
            }
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    public static CallRecord addDocumentAll(Scenario.Step step) throws SQLException {
        CallRecord res = null;
        String fileLocation;
        String preRegistrationID;

        for (int i = 0; i < data.persons.size(); i++) {
            Persona person = data.persons.get(i);
            preRegistrationID = person.pre_registration_id;
            for (int j = 0; j < data.persons.get(i).documents.size(); j++) {
                ProofDocument proofDocument = data.persons.get(i).documents.get(j);
                fileLocation = proofDocument.path;

                JSONObject request_json = new JSONObject();
                request_json.put("docCatCode", proofDocument.doc_cat_code.toString());
                request_json.put("docTypCode", proofDocument.doc_type_code);
                request_json.put("langCode", person.lang_code);

                switch (step.variant) {
                    case "InvalidSize":
                        fileLocation = ParserEngine.DOCUMENT_DATA_PATH + "Invalidsize.pdf";
                        break;

                    case "InvalidPRID":
                        preRegistrationID = "100000000";
                        break;

                    case "InvalidDocCategory":
                        request_json.put("docCatCode", data.globals.get("INVALID_DOCCATEGORY"));
                        break;

                    case "InvalidDocType":
                        request_json.put("docTypCode", data.globals.get("INVALID_DOCTYPE"));
                        break;

                    case "DEFAULT":
                        break;

                    default:
                        extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                        auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                        return new CallRecord();
                }

                JSONObject api_input = new JSONObject();
                api_input.put("id", prop.getProperty("documentUploadID"));
                api_input.put("version", prop.getProperty("apiver"));
                api_input.put("requesttime", getCurrentDateAndTimeForAPI());
                api_input.put("request", request_json);

                // submit /documents POST service
                String url = "/preregistration/" + BaseHelper.baseVersion + "/documents/" + preRegistrationID;
                RestAssured.baseURI = baseUri;
                Response api_response = given().relaxedHTTPSValidation()
                        .cookie("Authorization", BaseHelper.authCookies)
                        .multiPart("file", new File(fileLocation))
                        .multiPart("Document request", api_input.toString())
                        .contentType("multipart/form-data")
                        .post(url);

                res = new CallRecord(url, step.name, "Pre-Reg ID: " + preRegistrationID + ", Request body: " + api_input.toString(),
                        api_response, "" + api_response.getStatusCode(), step);
                AddCallRecord(res, api_response, extentTest);

                /* check for api status */
                if (api_response.getStatusCode() != 200) {
                    extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
                    auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
                    Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
                }

                /* Error handling middleware */
                if (step.error != null && !step.error.isEmpty()) {
                    stepErrorMiddleware(step, res);
                } else {
                    ReadContext ctx = com.jayway.jsonpath.JsonPath.parse(api_response.getBody().asString());
                    /* Response data parsing */
                    try {
                        data.persons.get(i).documents.get(j).doc_id = ctx.read("$['response']['docId']");
                    } catch (PathNotFoundException e) {
                        // todo
                    }

                    /* Assertion policies execution */
                    if (step.asserts.size() > 0) {
                        for (assertion_policy assertion_type : step.asserts) {
                            switch (assertion_type) {
                                case DONT:
                                    break;

                                case API_CALL:
                                    Scenario.Step nstep = new Scenario.Step();
                                    nstep.name = "getDocument";
                                    nstep.index.add(i);
                                    CallRecord getAppRecord = getDocuments(nstep);

                                    HashMap<String, Object> hm =
                                            ResponseParser.addDocumentResponseParser(j,
                                                    data.persons.get(i).documents.get(j), getAppRecord.response);

                                    extentTest.log((Status) hm.get("status"), hm.get("msg").toString());
                                    if (hm.get("status").equals(Status.FAIL)) {
                                        auditLog.severe(hm.get("msg").toString());
                                    } else {
                                        auditLog.info(hm.get("msg").toString());
                                    }
                                    auditLog.info("-----------------------------------------------------------------------------------------------------------------");

                                    Assert.assertEquals(hm.get("status"), Status.INFO, hm.get("msg").toString());
                                    break;

                                case DB_VERIFICATION:
                                    String queryString =
                                            "SELECT * FROM applicant_document where prereg_id = " + person.pre_registration_id
                                                    + " AND doc_cat_code = " + proofDocument.doc_cat_code.toString()
                                                    + " AND doc_type_code = " + proofDocument.doc_type_code
                                                    + " AND lang_code = " + person.lang_code;

                                    dbVerification(queryString, extentTest, true);
                                    break;

//                                default:
//                                    extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                                    auditLog.warning("Skipping assert " + assertion_type);
//                                    break;
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public static CallRecord getDocuments(Scenario.Step step) throws SQLException {
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
                auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        String url = "/preregistration/" + BaseHelper.baseVersion + "/documents/preregistration/" + preRegID;
        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", BaseHelper.authCookies)
                .get(url);

        CallRecord res = new CallRecord(url, step.name, preRegID, api_response,
                "" + api_response.getStatusCode(), step);
        tableAppNameValue = null;
        AddCallRecord(res, api_response, extentTest);
        tableAppNameValue = "PREREGISTRATION";

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
            Assert.assertEquals(api_response.getStatusCode(), 200, "API HTTP status");
        }

        /* Error handling middleware */
        if (step.error != null && !step.error.isEmpty()) {
            stepErrorMiddleware(step, res);
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
//                            extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                            auditLog.warning("Skipping assert " + assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

    public static CallRecord getDocumentsByPreRegID(Scenario.Step step) throws SQLException {
        switch (step.variant) {
            case "DEFAULT":
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);

        //https://mosip.io/preregistration/v1/documents/{documentId}?preRegistrationId={preRegistrationId}
        String url =
                "/preregistration/" + BaseHelper.baseVersion + "/documents/" + person.documents.get(index).doc_id
                        + "?preRegistrationId=" + person.pre_registration_id;
        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", BaseHelper.authCookies)
                .get(url);

        CallRecord res = new CallRecord(url, step.name, "Document ID: " + person.documents.get(index).doc_id
                + ", Pre-Reg ID: " + person.pre_registration_id,
                api_response, "" + api_response.getStatusCode(), step);
        tableAppNameValue = null;
        AddCallRecord(res, api_response, extentTest);
        tableAppNameValue = "PREREGISTRATION";

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
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
//                            auditLog.warning("Skipping assert " + assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

    public static CallRecord deleteDocumentsByPreRegID(Scenario.Step step) throws SQLException {
        String url = null;
        String preRegistrationID = null;

        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        preRegistrationID = person.pre_registration_id;

        switch (step.variant) {
            case "InvalidPRID":
                preRegistrationID = "100000000";
                break;
//
//            case "DEFAULT":
//                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        //https://mosip.io/preregistration/v1/documents/preregistration/{preRegistrationId}
        url = "/preregistration/" + BaseHelper.baseVersion + "/documents/preregistration/" + preRegistrationID;
        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", BaseHelper.authCookies)
                .delete(url);

        CallRecord res = new CallRecord(url, step.name, preRegistrationID, api_response,
                "" + api_response.getStatusCode(), step);
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
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
                                    "SELECT * FROM applicant_document where prereg_id = " + data.persons.get(index).pre_registration_id;

                            dbVerification(queryString, extentTest, false);
                            break;

//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                            auditLog.warning("Skipping assert " + assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

    public static CallRecord deleteDocument(Scenario.Step step) throws SQLException {
        String url = null;
        String preRegistrationID;
        preRegistrationID = null;
        String documentID;

        int index = UtilsA.getPersonIndex(step);
        Persona person = data.persons.get(index);
        preRegistrationID = person.pre_registration_id;
        documentID = person.documents.get(index).doc_id;

        switch (step.variant) {
            case "DEFAULT":
                break;

            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                preRegistrationID = pre_registration_id_OtherUser;
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        url =
                "/preregistration/" + BaseHelper.baseVersion + "/documents/" + documentID + "?preRegistrationId=" + preRegistrationID;

        RestAssured.baseURI = baseUri;
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", BaseHelper.authCookies)
                .delete(url);

        CallRecord res = new CallRecord(url, step.name,
                "Document ID: " + documentID + ", Pre-Reg ID: " + preRegistrationID, api_response, "" + api_response.getStatusCode(), step);
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
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
                                    "SELECT * FROM applicant_document where id = " + data.persons.get(index).documents.get(0).doc_id;

                            dbVerification(queryString, extentTest, false);
                            break;

//                        default:
//                            extentTest.log(Status.WARNING, "Skipping assert " + assertion_type);
//                            auditLog.warning("Skipping assert " + assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }

    //CopyDocument_InvalidPRID
    public static CallRecord copyDocument(Scenario.Step step) throws SQLException {
        ProofDocument proofDocument = data.persons.get(0).documents.get(0);
        String source_prereg_id;
        source_prereg_id = null;
        String categoryCode = proofDocument.doc_cat_code.toString();

        if (step.index.size() != 2) {
            extentTest.log(Status.WARNING, "Skipping step as invalid step entry for step: " + step.name);
            auditLog.warning("Skipping step as invalid step entry for step: " + step.name);
            return new CallRecord();
        }

        String destination_prereg_Id;
        if (data.persons.size() <= 1) {
            //int index = UtilsA.getPersonIndex(step);
            Persona person = data.persons.get(0);
            destination_prereg_Id = person.pre_registration_id;
            source_prereg_id = person.pre_registration_id;
        } else {
            destination_prereg_Id = data.persons.get(step.index.get(0)).pre_registration_id;
            source_prereg_id = data.persons.get(step.index.get(1)).pre_registration_id;
        }

        switch (step.variant) {
            case "DEFAULT":
                break;

            case "InvalidPRID":
                source_prereg_id = "99999999999999";
                break;

            case "ValidateRequestParameterIsMissing":
                source_prereg_id = "";
                break;

            case "ValidateCatagoryCodeIsInvalid":
                categoryCode = "ERR";
                break;

            case "ValidateNoDataFoundForRequestedPreRegID":
                destination_prereg_Id = "99999999999999";
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name + " as variant " + step.variant + " not found");
                auditLog.warning("Skipping step " + step.name + " as variant " + step.variant + " not found");
                return new CallRecord();
        }

        String url =
                "/preregistration/" + BaseHelper.baseVersion + "/documents/" + destination_prereg_Id + "?catCode="
                        + categoryCode + "&sourcePreId=" + source_prereg_id;
        RestAssured.baseURI = baseUri;
        Response api_response = given()
                .cookie("Authorization", BaseHelper.authCookies)
                .put(url);

        CallRecord res = new CallRecord(url, step.name, "Destination Pre-Reg ID: " + destination_prereg_Id + ", " +
                "Source Pre-Reg ID: " + source_prereg_id, api_response, "" + api_response.getStatusCode(), step);
        AddCallRecord(res, api_response, extentTest);

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            extentTest.log(Status.FAIL, "Assert HTTP STATUS, expected [" + 200 + "], actual[" + api_response.getStatusCode() + "]");
            auditLog.severe("API HTTP status return as " + api_response.getStatusCode());
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
//                            auditLog.warning("Skipping assert " + assertion_type);
//                            break;
                    }
                }
            }
        }
        return res;
    }
}