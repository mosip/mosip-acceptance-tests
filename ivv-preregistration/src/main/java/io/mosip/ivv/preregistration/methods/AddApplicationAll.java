package io.mosip.ivv.preregistration.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;
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

public class AddApplicationAll extends Step implements StepInterface {

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        ArrayList<Person> persons = this.store.getScenarioData().getPersona().getPersons();
        for (int index = 0; index < persons.size(); index++) {

            Person person = persons.get(index);

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
            api_input.put("id", "mosip.pre-registration.demographic.create");
            api_input.put("version", System.getProperty("ivv.prereg.apiversion"));
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

            this.callRecord = new CallRecord(RestAssured.baseURI + url, "POST", api_input.toString(), api_response);
            Helpers.logCallRecord(this.callRecord);

            ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
            /* Response data parsing */
            try {
                this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationId(ctx.read("$['response']['preRegistrationId']"));
                this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationStatusCode("$['response']['statusCode']");
            } catch (PathNotFoundException e) {

            }

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

                            case API_CALL:
                                CallRecord getAppRecord = getApplication();
                                if (getAppRecord == null) {
                                    this.hasError = true;
                                    return;
                                } else {
                                    Boolean responseMatched = responseMatch(persons,getAppRecord.getResponse());
                                    if (!responseMatched) {
                                        logInfo("Assert API_CALL failed");
                                        this.hasError = true;
                                        return;
                                    }
                                }
                                break;

//                        case STATUS:
//                            if(!(Boolean) ctx.read("$['status']")){
//                                extentTest.log(Status.FAIL, "Assert via STATUS, expected ["+true+"], actual["+ctx.read("$['status']")+"]");
//                                Utils.auditLog.severe("API body status return as " + ctx.read("$['status']"));
//                                Assert.assertEquals((Boolean) ctx.read("$['status']"), (Boolean) true, "Assert via STATUS");
//                            }
//                            break;


                            //case DB_VERIFICATION:
//                            String queryString =
//                                    "SELECT * FROM prereg.applicant_demographic where prereg_id = " + ctx.read("$['response'][0]['preRegistrationId']") + " AND status_code = " + ctx.read("$['response'][0]['statusCode']");
//
//                            dbVerification(queryString, extentTest, true);
                             //   break;
                                
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

//                        case DEFAULT:
//                            if(!(Boolean) ctx.read("$['status']")){
//                                extentTest.log(Status.FAIL, "Assert via STATUS, expected ["+true+"], actual["+ctx.read("$['status']")+"]");
//                                Utils.auditLog.severe("API body status return as " + ctx.read("$['status']"));
//                                Assert.assertEquals((Boolean) ctx.read("$['status']"), (Boolean) true, "Assert via STATUS");
//                            }
//                            break;
//
                        default:
                            logInfo("Assert not found or implemented: " + pr_assert.type);
                            break;
                        }
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

    private CallRecord getApplication() {
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

        String identifier = "Sub Step: " + nstep.getName() + ", module: " + nstep.getModule() + ", variant: " + nstep.getVariant();
        if (st.hasError()) {
            logSevere(identifier + " - failed");
            return null;
        } else {
            return st.getCallRecord();
        }
    }

    private Boolean responseMatch(ArrayList<Person> persons,Response response) {
        ReadContext ctx = JsonPath.parse(response.getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']");

        for (int i = 0; i < persons.size(); i++) {
            if (!persons.get(i).getPhone().equals(app_info.get("phone"))) {
                logInfo("Response matcher: Phone does not match");
                return false;
            }
            if (!persons.get(i).getReferenceIdentityNumber().equals(app_info.get("CNIENumber"))) {
                logInfo("Response matcher: CNIENumber does not match");
                return false;
            }
            if (!persons.get(i).getPostalCode().equals(app_info.get("postalCode"))) {
                logInfo("Response matcher: postalCode does not match");
                return false;
            }


        }
        return true;
    }

}