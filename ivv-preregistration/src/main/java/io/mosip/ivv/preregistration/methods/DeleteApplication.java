package io.mosip.ivv.preregistration.methods;

import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.base.PRStepInterface;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.sql.SQLException;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class DeleteApplication extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     * @param step
     */
    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        /* getting active user from persons */
        Person person = this.store.getScenarioData().getPersona().getPersons().get(index);
        String preRegistrationID = person.getPreRegistrationId();

        switch (step.getVariant()) {
            case "InvalidPRID":
                preRegistrationID = "100000000";
                break;

            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                preRegistrationID = this.store.getGlobals().get("OTHER_PERSON_PREREGISTRATION_ID");
                break;

            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/applications/"+preRegistrationID;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = given().relaxedHTTPSValidation()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .delete(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "DELETE", "preRegistrationID: "+preRegistrationID, api_response);
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
        } else {
            /* Assertion policies execution */
            if (step.getAsserts().size() > 0) {
                for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                    switch (pr_assert.type) {
                        case DONT:
                            break;
                            
                            case DB_VERIFICATION:
                                String queryString =
                                        "SELECT * FROM applicant_demographic where prereg_id = " + preRegistrationID;



                                try {
                                    Helpers.dbVerification(queryString, "","");
                                } catch (SQLException e) {
                                    Utils.auditLog.severe("Failed while querying from DB " + e.getMessage());
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
                                logSevere("Assert failed: Expected response not empty but found empty");
                                this.hasError=true;
                                return;
                            }
                            Utils.auditLog.info("Assert [DEFAULT] passed");
                            break;

                        default:
                            logWarning("API HTTP status return as " + pr_assert.type);
                            break;
                    }
                }
            }
        }
    }

}
