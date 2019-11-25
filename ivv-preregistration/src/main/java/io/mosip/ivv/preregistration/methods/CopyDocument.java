package io.mosip.ivv.preregistration.methods;

import com.aventstack.extentreports.Status;
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

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class CopyDocument extends Step implements StepInterface {
    public static String pre_registration_id_OtherUser = "";

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        ArrayList<Person> persons = this.store.getScenarioData().getPersona().getPersons();
        ProofDocument proofDocument = persons.get(0).getDocuments().get(0);
        //ProofDocument proofDocument = this.store.getScenarioData().getPersona().getPersons().get(0).getDocuments().get(0);
        String source_prereg_id = null;
        String categoryCode = proofDocument.getDocCatCode().toString();
        if (step.getIndex().size() != 2) {
            logWarning("Skipping step " + step.getName() + " as parameters expected are not provided");
            return;
        }

        String destination_prereg_Id;
        if (persons.size() <= 1) {
            //int index = UtilsA.getPersonIndex(step);
            Person person = persons.get(0);
            destination_prereg_Id = person.getPreRegistrationId();
            source_prereg_id = person.getPreRegistrationId();
        } else {
            destination_prereg_Id = persons.get(step.getIndex().get(0)).getPreRegistrationId();
            source_prereg_id = persons.get(step.getIndex().get(1)).getPreRegistrationId();
        }

        switch (step.getVariant()) {
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
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }
        String url =
                "/preregistration/" + System.getProperty("ivv.prereg.version") + "/documents/" + destination_prereg_Id + "?catCode="
                        + categoryCode + "&sourcePreId=" + source_prereg_id;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();

        Response api_response =
                (Response) given()
                        .contentType(ContentType.JSON)
                        .cookie("Authorization", this.store.getHttpData().getCookie())
                        .put(url);

        this.callRecord = new CallRecord(RestAssured.baseURI + url, "PUT", "Destination Pre-Reg ID: " + destination_prereg_Id + ", " +
                "Source Pre-Reg ID: " + source_prereg_id, api_response);
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
           // ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

            /* Response data parsing */

            /* Assertion policies execution */
            if (step.getAsserts().size() > 0) {
                for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
                    switch (pr_assert.type) {
                        case DONT:
                            break;
                    }
                }
            }


        }
    }
}
