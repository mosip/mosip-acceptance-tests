package io.mosip.ivv.preregistration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class SampleMethod extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        /* getting active user from persons */
        Person person = this.store.getScenarioData().getPersona().getPersons().get(index);

        String url = "<url of the api>";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = (Response) given()
                .contentType(ContentType.JSON).post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", "<api input>", api_response);
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
        }
    }

}