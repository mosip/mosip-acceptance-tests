package io.mosip.ivv.preregistration.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
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
import java.util.Map;

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
        for (Map.Entry<String, IDObjectField> entry : person.getIdObject().entrySet()) {
            String key = entry.getKey();
            IDObjectField idField = entry.getValue();
            if(idField.getType().equals(IDObjectField.type.multilang)){
                JSONArray jvals = new JSONArray();
                if(!store.getCurrentPerson().getPrimaryLang().isEmpty()){
                    jvals.add(new JSONObject(
                            new HashMap<String, String>() {{
                                put("value", idField.getPrimaryValue());
                                put("language", store.getCurrentPerson().getPrimaryLang());
                            }}
                    ));
                }
                if(!store.getCurrentPerson().getSecondaryLang().isEmpty()){
                    jvals.add(new JSONObject(
                            new HashMap<String, String>() {{
                                put("value", idField.getSecondaryValue());
                                put("language", store.getCurrentPerson().getSecondaryLang());
                            }}
                    ));
                }
                identity_json.put(key, jvals);
            } else {
                identity_json.put(key, idField.getPrimaryValue());
            }
        }

        JSONObject request_json = new JSONObject();
        request_json.put("langCode", person.getPrimaryLang());

        JSONObject api_input = new JSONObject();
        api_input.put("id", "mosip.pre-registration.demographic.update");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        String preRegistrationID = person.getPreRegistrationId();

        switch (step.getVariant()) {

            case "DEFAULT":
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as variant " + step.getVariant() + " not found");
                return;
        }

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
