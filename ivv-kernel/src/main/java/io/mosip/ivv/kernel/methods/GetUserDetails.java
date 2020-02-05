package io.mosip.ivv.kernel.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.RegistrationUser;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.minidev.json.JSONArray;

import static io.restassured.RestAssured.given;

public class GetUserDetails extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    public void run() {
        /* getting active user from persons */
        RegistrationUser person = this.store.getCurrentRegistrationUSer();

        String url = "/"+System.getProperty("ivv.global.version")+"/syncdata/userdetails/"+person.getCenterId();
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
                        case USERCOUNT:
                            Object idValues = ctx.read("$['response']['userDetails']");
                            int size=0;
                            if(idValues!=null){
                                size=((JSONArray)idValues).size();
                                if(size !=Integer.valueOf(person.getNo_Of_User())){
                                    logInfo("No of User expected "+Integer.valueOf(person.getNo_Of_User())+" but found  "+size);
                                    this.hasError=true;
                                    return;
                                }
                            }else{
                                logInfo("No user details found for the  CenterId :"+ person.getCenterId());
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