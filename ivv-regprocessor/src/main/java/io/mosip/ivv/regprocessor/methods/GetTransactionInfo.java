package io.mosip.ivv.regprocessor.methods;

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
import io.mosip.ivv.regprocessor.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class GetTransactionInfo extends Step implements StepInterface {

    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        String registrationId = store.getScenarioData().getPersona().getPersons().get(index).getRegistrationId();

        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        String url = "/registrationprocessor/" + System.getProperty("ivv.prereg.version") + "/registrationtransaction/search/"+registrationId;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response =
                (Response) given()
                        .contentType(ContentType.JSON)
                        .cookie("Authorization", this.store.getHttpData().getCookie())
                        .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", "registrationId: "+registrationId, api_response);
        Helpers.logCallRecord(this.callRecord);
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
            if(ctx.read("$['response']") != null){
                ArrayList<HashMap<String, String>> transactionInfo = ctx.read("$['response'][*]");
                for(HashMap ti: transactionInfo){
                    logInfo("id: "+ti.get("id")+", statusComment: "+ti.get("statusComment"));
                }
            }
        } catch (PathNotFoundException e) {
            e.printStackTrace();
        }
    }

}