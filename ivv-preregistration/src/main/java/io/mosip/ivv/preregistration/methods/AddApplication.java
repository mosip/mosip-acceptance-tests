package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class AddApplication extends Step implements StepInterface {

    private Person person;

    @SuppressWarnings({ "unchecked", "serial" })
	@Override
    public void run() {
        this.index = Utils.getPersonIndex(step);

        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);

        JSONObject identity_json = new JSONObject();
        for (Map.Entry<String, ArrayList<Person.FieldValue>> entry : person.getIdObject().entrySet()) {
            String key = entry.getKey();
            ArrayList<Person.FieldValue> vals = entry.getValue();
            if(vals.size() == 2){
                identity_json.put(key, new JSONArray() {{
                    add(new JSONObject(
                                    new HashMap<String, String>() {{
                                        put("value", vals.get(0).getValue());
                                        put("language", vals.get(0).getLang());
                                    }}
                            )
                    );
                    add(new JSONObject(
                                    new HashMap<String, String>() {{
                                        put("value", vals.get(1).getValue());
                                        put("language", vals.get(1).getLang());
                                    }}
                            )
                    );
                }});
            } else {
                identity_json.put(key, new JSONArray() {{
                    add(new JSONObject(
                                    new HashMap<String, String>() {{
                                        put("value", vals.get(0).getValue());
                                        put("language", vals.get(0).getLang());
                                    }}
                            )
                    );
                }});
            }
        }

        JSONObject request_json = new JSONObject();
        request_json.put("langCode", person.getPrimaryLang().getValue());

        JSONObject api_input = new JSONObject();
        api_input.put("id", "mosip.pre-registration.demographic.create");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

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

        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/applications";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response api_response =
                (Response) given()
                        .contentType(ContentType.JSON).body(api_input)
                        .cookie("Authorization", this.store.getHttpData().getCookie())
                        .post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI+url, "POST", api_input.toString(), api_response);
        Helpers.logCallRecord(this.callRecord);
        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        try {
            // this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationId(ctx.read("$['response']['preRegistrationId']"));
            if(ctx.read("$['response']")!=null) {
                this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationId(ctx.read("$['response']['preRegistrationId']"));
                this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationStatusCode("$['response']['statusCode']");
            }
        } catch (PathNotFoundException e) {
            e.printStackTrace();
        }

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
                        case STATUS:
                            logInfo("Assert not yet implemented: " + pr_assert.type);
                            break;

                        case API_CALL:
                            CallRecord getAppRecord = getApplication();
                            if(getAppRecord == null){
                                this.hasError = true;
                                return;
                            }else{
                                Boolean responseMatched = responseMatch(person,getAppRecord.getResponse());
                                if(!responseMatched){
                                    logInfo("Assert API_CALL failed");
                                    this.hasError = true;
                                    return;
                                }
                            }
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
                            logInfo("Assert not found or implemented: " + pr_assert.type);
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

    private CallRecord getApplication(){
        Scenario.Step nstep = new Scenario.Step();
        nstep.setName("getApplication");
        nstep.setVariant("DEFAULT");
        nstep.setModule(Scenario.Step.modules.pr);
        nstep.setIndex(new ArrayList<Integer>());
        nstep.getIndex().add(this.index);
        GetApplication st = new GetApplication();
        st.setExtentInstance(extentInstance);
        st.setState(this.store);
        st.setStep(nstep);
        st.run();
        this.store = st.getState();

        String identifier = "Sub Step: "+nstep.getName()+", module: "+nstep.getModule()+", variant: "+nstep.getVariant();
        if(st.hasError()){
            logSevere(identifier+" - failed");
            return null;
        }else{
            return st.getCallRecord();
        }
    }

    private Boolean responseMatch(Person person,Response response){
        ReadContext ctx = JsonPath.parse(response.getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']['demographicDetails']['identity']");

        if (!person.getPhone().equals(app_info.get("phone"))) {
            logInfo("Response matcher: Phone does not match expected["+person.getPhone()+"], found["+app_info.get("phone")+"]");
            return false;
        }
        if (!person.getReferenceIdentityNumber().equals(app_info.get("referenceIdentityNumber"))) {
            logInfo("Response matcher: referenceIdentityNumber does not match expected["+person.getReferenceIdentityNumber()+"], found["+app_info.get("referenceIdentityNumber")+"]");
            return false;
        }
        if (!person.getPostalCode().equals(app_info.get("postalCode"))) {
            logInfo("Response matcher: postalCode does not match expected["+person.getPostalCode()+"], found["+app_info.get("postalCode")+"]");
            return false;
        }

        return true;
    }

}