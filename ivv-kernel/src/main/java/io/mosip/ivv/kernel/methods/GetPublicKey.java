package io.mosip.ivv.kernel.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class GetPublicKey extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    public void run() {
        String url = "/"+System.getProperty("ivv.global.version")+"/syncdata/publickey/KERNEL?timeStamp="+Utils.getCurrentDateAndTimeForAPI()+"&referenceId=SIGN";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = (Response) given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .get(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "GET", "", api_response);
        Utils.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        /* check for api status */
        if (api_response.getStatusCode() != 200) {
            logInfo("API HTTP status return as " + api_response.getStatusCode());
            this.hasError=true;
            return;
        }

        /* check for Publickey  */
        if (ctx.read("$['response'][''publicKey]") == null) {
            logInfo("Publickey not found");
            this.hasError=true;
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

                        case DEFAULT:
                            try {
                                if(ctx.read("$['response']") == null){
                                    logInfo("Assert failed: Expected response not empty but found empty");
                                    this.hasError=true;
                                    return;
                                }
                            } catch (PathNotFoundException e) {
                                e.printStackTrace();
                                logInfo("Assert failed: Expected response not empty but found empty");
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
    }

}