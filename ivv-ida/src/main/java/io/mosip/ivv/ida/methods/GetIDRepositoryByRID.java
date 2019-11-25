package io.mosip.ivv.ida.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.CallRecord;
import io.mosip.ivv.core.structures.Person;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class GetIDRepositoryByRID extends Step implements StepInterface {
    private Person person;
    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        person = store.getScenarioData().getPersona().getPersons().get(index);
        String rid = person.getRegistrationId();
        if(step.getParameters().size() > 0){
            rid = step.getParameters().get(0);
        }

        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        String url = "/idrepository/" + System.getProperty("ivv.global.version") + "/identity/rid/" + rid +"?type=all";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = (Response) given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .get(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", "rid: "+rid, api_response);
        Utils.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logFail("API HTTP status return as " + api_response.getStatusCode());
            this.hasError=true;
            return;
        }

        if (step.getErrors() != null && step.getErrors().size()>0) {
            ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step, api_response, extentInstance).inject();
            if(!emr.getStatus()){
                this.hasError = true;
                return;
            }
        }else{

            /* Assertion policies execution */
            if (step.getAsserts().size() > 0) {
                for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                    switch (pr_assert.type) {
                        case DONT:
                            break;

                        case DEFAULT:
                            try {
                                if(ctx.read("$['response']") == null){
                                    logFail("Assert failed: Expected response not empty but found empty");
                                    this.hasError=true;
                                    return;
                                }
                            } catch (PathNotFoundException e) {
                                e.printStackTrace();
                                logFail("Assert failed: Expected response not empty but found empty");
                                this.hasError=true;
                                return;
                            }
                            logInfo("Assert [DEFAULT] passed");
                            break;

                        default:
                            logWarning("Assert not found or implemented: " + pr_assert.type);
                            break;
                    }
                }
            }
        }

        try {
            logInfo(ctx.read("$['response']").toString());
        } catch (PathNotFoundException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError=true;
            return;
        }
    }

}