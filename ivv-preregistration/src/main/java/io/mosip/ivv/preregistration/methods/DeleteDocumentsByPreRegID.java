package io.mosip.ivv.preregistration.methods;

import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.*;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.base.PRStepInterface;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class DeleteDocumentsByPreRegID extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        String url = null;
        String preRegistrationID = null;


        this.index = Utils.getPersonIndex(step);
        Person person = this.store.getScenarioData().getPersona().getPersons().get(index);
        preRegistrationID = person.getPreRegistrationId();

        switch (step.getVariant()) {
            case "InvalidPRID":
                preRegistrationID = "100000000";
                break;

            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/documents/preregistration/" + preRegistrationID;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();

        Response api_response =
                (Response) given()
                        .contentType(ContentType.JSON)
                        .cookie("Authorization", this.store.getHttpData().getCookie())
                        .delete(url);

        this.callRecord = new CallRecord(RestAssured.baseURI + url, "DELETE", "preRegistrationID : " + preRegistrationID, api_response);
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

            /* Assertion policies execution */
            if (step.getAsserts().size() > 0) {
                for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                    switch (pr_assert.type) {
                        case DONT:
                            break;
                        case API_CALL:
                            CallRecord getDocumentsRecord = getDocuments();
                            if(getDocumentsRecord == null){
                                this.hasError = true;
                                return;
                            }else{
                                Boolean responseMatched = responseMatch(person,getDocumentsRecord.getResponse());
                                if(!responseMatched){
                                    logInfo("Assert API_CALL failed");
                                    this.hasError = true;
                                    return;
                                }
                            }
                            break;

//                        case STATUS:
//                            if (!(Boolean) ctx.read("$['status']")) {
//                                extentTest.log(Status.FAIL, "Assert via STATUS, expected [" + true + "], actual[" + ctx.read("$['status']") + "]");
//                                auditLog.severe("API body status return as " + ctx.read("$['status']"));
//                                Assert.assertEquals((Boolean) ctx.read("$['status']"), (Boolean) true, "Assert via STATUS");
//                            }
//                            break;

					/*
					 * case DB_VERIFICATION: String queryString =
					 * "SELECT * FROM applicant_document where prereg_id = " + preRegistrationID;
					 * 
					 * 
					 * try { Helpers.dbVerification(queryString, "",""); } catch (SQLException e) {
					 * Utils.auditLog.severe("Failed while querying from DB " + e.getMessage()); }
					 * break;
					 */
                            
                        case DB_VERIFICATION:
                            String queryString =
                            		createSQLQuery(person);


                            try {
                                Helpers.dbVerification(queryString, "","");
                            } catch (SQLException e) {
                                Utils.auditLog.severe("Failed while querying from DB " + e.getMessage());
                            }
                            break;

//                        case DEFAULT:
//                            if (!(Boolean) ctx.read("$['status']")) {
//                                extentTest.log(Status.FAIL, "Assert via STATUS, expected [" + true + "], actual[" + ctx.read("$['status']") + "]");
//                                auditLog.severe("API body status return as " + ctx.read("$['status']"));
//                                Assert.assertEquals((Boolean) ctx.read("$['status']"), (Boolean) true, "Assert via STATUS");
//                            }
//                            break;
//
                        default:
                            logInfo("API HTTP status return as " + pr_assert.type);
                            break;
                    }
                }
            }
        }
    }

    private CallRecord getDocuments(){
        Scenario.Step nstep = new Scenario.Step();
        nstep.setName("getDocuments");
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

    private Boolean responseMatch(Person person,Response response) {
        ReadContext ctx = JsonPath.parse(response.getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']");
        if (person != null && app_info != null) {
            if (person.getDocuments().get(index).getDocId().equals(app_info.get("doc_id"))) {
                logInfo("Response matcher: Document  not deleted");
                return false;
            }
        }else{
            logInfo("Document deleted successFully for Pre-reg-id : "+person.getPreRegistrationId());
        }
        return true;
    }
    
    private String createSQLQuery(Person person) {
    	StringBuilder sqlQuery= new StringBuilder();
    	sqlQuery.append("SELECT * FROM applicant_document where prereg_id =")
    	.append("'").append(person.getPreRegistrationId()).append("'");
    	return sqlQuery.toString();
    }
}
