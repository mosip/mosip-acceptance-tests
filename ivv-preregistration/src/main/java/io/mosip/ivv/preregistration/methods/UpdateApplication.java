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
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class UpdateApplication extends Step implements StepInterface {

    private Person person;

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @SuppressWarnings({ "unchecked", "serial" })
	@Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        /* getting active user from persons */
        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);

        JSONObject identity_json = new JSONObject();
        identity_json.put("dateOfBirth", person.getDateOfBirth());
        identity_json.put("email", person.getEmail());
        identity_json.put("phone", person.getPhone());
        identity_json.put("referenceIdentityNumber", person.getReferenceIdentityNumber());
        identity_json.put("IDSchemaVersion", 1);
        identity_json.put("postalCode", person.getPostalCode());

        HashMap<String, String> demographic = new HashMap<>();
        demographic.put("fullName", person.getName());
        demographic.put("gender", person.getGender());
        demographic.put("addressLine1", person.getAddressLine1());
        demographic.put("addressLine2", person.getAddressLine2());
        demographic.put("addressLine3", person.getAddressLine3());
        demographic.put("region", person.getRegion());
        demographic.put("province", person.getProvince());
        demographic.put("city", person.getCity());
        demographic.put("zone", person.getZone());
        demographic.put("residenceStatus", person.getResidenceStatus());

        JSONObject request_json = new JSONObject();
        request_json.put("langCode", person.getDefaultLang());

        JSONObject api_input = new JSONObject();
        api_input.put("id", "mosip.pre-registration.demographic.update");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        String preRegistrationID = person.getPreRegistrationId();

        switch (step.getVariant()) {
            case "invalidGender":
                demographic.put("gender", this.store.getGlobals().get("INVALID_GENDER"));
                break;

            case "invalidEmail":
                identity_json.put("email", this.store.getGlobals().get("INVALID_EMAIL"));
                break;

            case "invalidPhone":
                identity_json.put("phone", this.store.getGlobals().get("INVALID_PHONE"));
                break;

            case "InvalidPRID":
                preRegistrationID = this.store.getGlobals().get("INVALID_PREREGISTRATION_ID");
                break;

            case "RequestIdIsInvalid":
                api_input.put("id", "");
                break;

            case "RequestVersionIsInvalid":
                api_input.put("version", "");
                break;

            case "RequestTimestampIsInvalid":
                api_input.put("requesttime", "");
                break;

            case "RequestDateShouldBeCurrentDate":
                api_input.put("requesttime", "2019-01-01T05:59:15.241Z");
                break;

            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
                preRegistrationID = this.store.getGlobals().get("OTHER_PERSON_PREREGISTRATION_ID");
                break;

            case "MissingInputParameterIdentityFullname":
                demographic.remove("fullName");
                break;

            case "MissingInputParameterIdentityDateofbirthIdentityAge":
                identity_json.remove("dateOfBirth");
                break;

            case "MissingInputParameterIdentityGender":
                demographic.remove("gender");
                break;

            case "MissingInputParameterIdentityResidencestatus":
                demographic.remove("residenceStatus");
                break;

            case "MissingInputParameterIdentityAddressline":
                demographic.remove("addressLine1");
                demographic.remove("addressLine2");
                demographic.remove("addressLine3");
                break;

            case "MissingInputParameterIdentityRegion":
                demographic.remove("region");
                break;

            case "MissingInputParameterIdentityProvince":
                demographic.remove("province");
                break;

            case "MissingInputParameterIdentityCity":
                demographic.remove("city");
                break;

            case "MissingInputParameterIdentityLocaladministrativeauthority":
                demographic.remove("localAdministrativeAuthority");
                break;

            case "MissingInputParameterIdentityPostalcode":
                identity_json.remove("postalCode");
                break;

            case "MissingInputParameterIdentityCnienumber":
                identity_json.remove("CNIENumber");
                break;

            case "InvalidInputParameterIdentityPhone":
                identity_json.put("Phone", "");
                break;

            case "InvalidInputParameterIdentityEmail":
                identity_json.put("email", "");
                break;


            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

        demographic.forEach((k, v) -> identity_json.put(k, new JSONArray() {{
                    add(new JSONObject(
                                    new HashMap<String, String>() {{
                                        put("value", v);
                                        put("language", person.getDefaultLang());

                                    }}
                            )
                    );
                }}
                )
        );

        JSONObject demographic_json = new JSONObject();
        demographic_json.put("identity", identity_json);
        request_json.put("demographicDetails", demographic_json);
        api_input.put("request", request_json);

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/applications/"+preRegistrationID;
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = given().relaxedHTTPSValidation()
                .contentType(ContentType.JSON).body(api_input)
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .put(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "PUT", api_input.toString(), api_response);
        Helpers.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

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
                                logSevere("Assert failed: Expected response not empty but found empty");
                                this.hasError=true;
                                return;
                            }
                            logInfo("Assert [DEFAULT] passed");
                            break;
                            
                        case DB_VERIFICATION:
    						String queryString =createSQLQuery(ctx);

    						try {
    							Helpers.dbVerification(queryString,"prereg_id", "status_code");
    						} catch (SQLException e) {
    							e.printStackTrace();
    							logSevere("Assert failed: No data Found for preRegistrationId :"
    									+ ctx.read("$['response']['preRegistrationId']") + "and status_code :"
    									+ ctx.read("$['response']['statusCode']"));
    							this.hasError = true;
    							return;
    						}
    						break;

                        default:
                            logInfo("API HTTP status return as " + pr_assert.type);
                            break;
                    }
                }
            }
        }
    }
    
    private String createSQLQuery( ReadContext ctx) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(" prereg.applicant_demographic ").append(" where prereg_id =").append("'")
				.append(ctx.read("$['response']['preRegistrationId']").toString()).append("'")
				.append("AND status_code =").append("'").append(ctx.read("$['response']['statusCode']").toString())
				.append("'");
		return query.toString();
    }

}
