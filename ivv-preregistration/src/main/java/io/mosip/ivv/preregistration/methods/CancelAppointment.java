package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CancelAppointment extends Step implements StepInterface {
	private Person person;

	@Override
	public void run() {
		String preRegistrationID = null;
		this.index = Utils.getPersonIndex(step);
		/* getting active user from persons */
		this.person = this.store.getScenarioData().getPersona().getPersons().get(index);

		preRegistrationID = person.getPreRegistrationId();

		switch (step.getVariant()) {
		case "InvalidPreReg":
			preRegistrationID = "100000000";
			break;

		case "BookingDataNotFound":
			preRegistrationID = person.getPreRegistrationId();
			break;

		case "DEFAULT":
			break;

		default:
			logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
			return;
		}

		String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/appointment/"
				+ preRegistrationID;
		RestAssured.useRelaxedHTTPSValidation();
		Response api_response = (Response) given().cookie("Authorization", this.store.getHttpData().getCookie())
				.put(url);

		this.callRecord = new CallRecord(RestAssured.baseURI + url, "PUT", "pre_reg_id: " + preRegistrationID,
				api_response);
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
			if (step.getAsserts().size() > 0) {
				for (Scenario.Step.Assert pr_assert : step.getAsserts()) {
					switch (pr_assert.type) {
					case DONT:
						break;

//                         default:
//                             extentTest.log(Status.WARNING, "Skipping assert "+assertion_type);
//                             Utils.auditLog.warning("Skipping assert "+assertion_type);
//                             break;
					}
				}
			}
		}
	}
}
