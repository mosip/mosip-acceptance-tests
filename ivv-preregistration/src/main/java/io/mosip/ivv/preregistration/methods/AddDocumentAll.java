package io.mosip.ivv.preregistration.methods;

import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.*;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.base.PRStepInterface;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class AddDocumentAll extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        String fileLocation = null;
        String preRegistrationID = null;
        String pre_registration_id_OtherUser = "";

        this.index = Utils.getPersonIndex(step);

        ArrayList<Person> persons = this.store.getScenarioData().getPersona().getPersons();

        for (int i = 0; i <persons.size(); i++) {
            Person person=persons.get(i);
            preRegistrationID = person.getPreRegistrationId();

            for (int j = 0; j < persons.get(i).getDocuments().size(); j++) {
                ProofDocument proofDocument = this.store.getScenarioData().getPersona().getPersons().get(i).getDocuments().get(j);
                fileLocation = proofDocument.getPath();

                JSONObject request_json = new JSONObject();
                request_json.put("docCatCode", proofDocument.getDocCatCode().toString().toUpperCase());
                request_json.put("docTypCode", proofDocument.getDocTypeCode().toUpperCase());
                request_json.put("langCode", person.getLangCode());

                JSONObject api_input = new JSONObject();
                api_input.put("id", "mosip.pre-registration.document.upload");
                api_input.put("version", System.getProperty("ivv.prereg.apiversion"));
                api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

                switch (step.getVariant()) {
                    case "InvalidSize":
                        fileLocation = System.getProperty("user.dir") + System.getProperty("ivv.path.documents") + "Invalidsize.pdf";
                        break;

                    case "InvalidPRID":
                        preRegistrationID = "100000000";
                        break;

                    case "InvalidDocCategory":
                        request_json.put("docCatCode", this.store.getGlobals().get("INVALID_DOCCATEGORY"));
                        break;

                    case "InvalidDocType":
                        request_json.put("docTypCode", this.store.getGlobals().get("INVALID_DOCTYPE"));
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

                    case "ValidateDocumentExceedingPermittedSize":
                        fileLocation = System.getProperty("user.dir") + System.getProperty("ivv.path.documents") + "Invalidsize.pdf";
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

                    case "ValidateInvalidPreRegID":
                        preRegistrationID = "100000000";
                        break;

                    case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                        preRegistrationID = pre_registration_id_OtherUser;
                        break;

                    case "DEFAULT":
                        break;

                    default:
                        logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                        return;

                }
                api_input.put("request", request_json);
                String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/documents/" + preRegistrationID;
                RestAssured.baseURI = System.getProperty("ivv.mosip.host");
                RestAssured.useRelaxedHTTPSValidation();


                Response api_response =
                        (Response) given()
                                .cookie("Authorization", this.store.getHttpData().getCookie())
                                .multiPart("file", new File(fileLocation))
                                .multiPart("Document request", api_input.toString())
                                .contentType("multipart/form-data")
                                .post(url);

                this.callRecord = new CallRecord(RestAssured.baseURI + url, "POST", api_input.toString(), api_response);
                Helpers.logCallRecord(this.callRecord);

                /* check for api status */
                if (api_response.getStatusCode() != 200) {
                    logSevere("API HTTP status return as " + api_response.getStatusCode());
                    this.hasError = true;
                    return;
                }


                if (step.getErrors() != null && step.getErrors().size() > 0) {
                    ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step, api_response, extentInstance).inject();
                    if (!emr.getStatus()) {
                        this.hasError = true;
                        return;
                    }
                } else {
                    ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
                    /* Response data parsing */
                    try {
                        this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationId(ctx.read("$['response']['preRegistrationId']"));
                        //this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationStatusCode("$['response']['statusCode']");
                        this.store.getScenarioData().getPersona().getPersons().get(index).getDocuments().get(j).setDocId(ctx.read("$['response']['docId']"));
                    } catch (PathNotFoundException e) {

                    }

                }

                /* Assertion policies execution */
                if (step.getAsserts().size() > 0) {
                    for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                        switch (pr_assert.type) {
                            case DONT:
                                break;

                            case API_CALL:
                                CallRecord getAppRecord = getDocuments();
                                if (getAppRecord == null) {
                                    this.hasError = true;
                                    return;
                                } else {
                                    Boolean responseMatched = responseMatch(proofDocument,getAppRecord.getResponse());
                                    if (!responseMatched) {
                                        logInfo("Assert API_CALL failed");
                                        this.hasError = true;
                                        return;
                                    }
                                }
                                break;

                            case DB_VERIFICATION:
                                String queryString =
                                		createSQLQuery(person,proofDocument);

                                try {
                                    Helpers.dbVerification(queryString, "doc_cat_code","doc_typ_code");
                                }catch (SQLException e){
                                    Utils.auditLog.severe("Failed while querying from DB "+ e.getMessage());
                                }
                                break;
						/*
						 * case DB_VERIFICATION: String queryString =
						 * "SELECT * FROM applicant_document where prereg_id = " +
						 * person.getPreRegistrationId() + " AND doc_cat_code = " +
						 * proofDocument.getDocCatCode().toString() + " AND doc_type_code = " +
						 * proofDocument.getDocTypeCode() + " AND lang_code = " + person.getLangCode();
						 * 
						 * try { Helpers.dbVerification(queryString, "",""); } catch (SQLException e) {
						 * Utils.auditLog.severe("Failed while querying from DB " + e.getMessage()); }
						 * break;
						 */

//                            case DEFAULT:
//                                if (!(Boolean) ctx.read("$['status']")) {
//                                    extentTest.log(Status.FAIL, "Assert via STATUS, expected [" + true + "], actual[" + ctx.read("$['status']") + "]");
//                                    auditLog.severe("API body status return as " + ctx.read("$['status']"));
//                                    Assert.assertEquals((Boolean) ctx.read("$['status']"), (Boolean) true, "Assert via STATUS");
//                                }
//                                break;
//
                            default:
                                logInfo("Assert not found or implemented: " + pr_assert.type);
                                break;
                        }
                    }
                }
            }
        }

    }
    
    
    private String createSQLQuery(Person person,ProofDocument proofDocument) {
    	StringBuilder sqlQuery= new StringBuilder();
    	sqlQuery.append("SELECT * FROM ").append("applicant_document where prereg_id =").append("'")
    	.append(person.getPreRegistrationId()).append("'").append("AND doc_cat_code =").append("'").append(proofDocument.getDocCatCode().toString())
    	.append("'").append("AND lang_code = ").append("'").append(person.getLangCode()).append("'");
    	return sqlQuery.toString();
    	
    }


    private CallRecord getDocuments(){
        Scenario.Step nstep = new Scenario.Step();
        nstep.setName("getDocument");
        nstep.setVariant("DEFAULT");
        nstep.setModule(Scenario.Step.modules.pr);
        nstep.setIndex(new ArrayList<Integer>());
        nstep.getIndex().add(this.index);
        GetDocuments st = new GetDocuments();
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

    private Boolean responseMatch(ProofDocument proofDocument,Response response){
        ReadContext ctx = JsonPath.parse(response.getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']");

        if (!proofDocument.getDocTypeCode().equals(app_info.get("docTypCode"))) {
            logInfo("Response matcher: docTypCode does not match");
            return false;
        }
        if (!proofDocument.getDocCatCode().equals(app_info.get("docCatCode"))) {
            logInfo("Response matcher: docCatCode does not match");
            return false;
        }
        return true;
    }
}
