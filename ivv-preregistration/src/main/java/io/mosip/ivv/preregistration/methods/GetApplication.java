package io.mosip.ivv.preregistration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class GetApplication extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);

        Person person = this.store.getScenarioData().getPersona().getPersons().get(index);
        String preRegID = person.getPreRegistrationId();

        switch (step.getVariant()) {
            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/applications/" + preRegID;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response = (Response) given()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .get(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "GET", "pre_reg_id: "+preRegID, api_response);
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