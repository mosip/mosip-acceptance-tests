package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class AddApplication extends Step implements StepInterface {

    private Person person;

    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     * @param step
     */
    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);

        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);

        JSONObject identity_json = new JSONObject();
        identity_json.put("dateOfBirth", person.getDateOfBirth());
        identity_json.put("email", person.getEmail());
        identity_json.put("phone", person.getPhone());
        identity_json.put("referenceIdentityNumber", person.getReferenceIdentityNumber());
        identity_json.put("IDSchemaVersion", 1);
        identity_json.put("zone", "NTH");
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
        api_input.put("id", "mosip.pre-registration.demographic.create");
        api_input.put("version", "1.0");
        api_input.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

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

            case "RequestVersionIsInvalid":
                api_input.put("version", "");
                break;

            case "RequestTimestampIsInvalid":
                api_input.put("requesttime", "");
                break;

            case "RequestDateShouldBeCurrentDate":
                api_input.put("requesttime", "2019-01-01T05:59:15.241Z");
                break;

            case "LangCodeIsInvalid":
                request_json.put("langCode", this.store.getGlobals().get("INVALID_LANG_CODE"));
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
                identity_json.put("phone", "");
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
                            break;

                        case API_CALL:
                            CallRecord getAppRecord = getApplication();
                            if(getAppRecord == null){
                                this.hasError = true;
                                return;
                            }else{
                                Boolean responseMatched = responseMatch(person);
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
                            logWarning("Assert not found or implemented: " + pr_assert.type);
                            break;
                    }
                }
            }
        }

        try {
            this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationId(ctx.read("$['response']['preRegistrationId']"));
            this.store.getScenarioData().getPersona().getPersons().get(index).setPreRegistrationStatusCode("$['response']['statusCode']");
        } catch (PathNotFoundException e) {
            e.printStackTrace();
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
        st.run(nstep);
        this.store = st.getState();

        String identifier = "Sub Step: "+nstep.getName()+", module: "+nstep.getModule()+", variant: "+nstep.getVariant();
        if(st.hasError()){
            logSevere(identifier+" - failed");
            return null;
        }else{
            return st.getCallRecord();
        }
    }

    private Boolean responseMatch(Person person){
        ReadContext ctx = JsonPath.parse(this.callRecord.getResponse().getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']['demographicDetails']['identity']");

        if (!person.getPhone().equals(app_info.get("phone"))) {
            logInfo("Response matcher: Phone does not match expected["+person.getPhone()+"], found["+app_info.get("phone")+"]");
            return false;
        }
        if (!person.getReferenceIdentityNumber().equals(app_info.get("referenceIdentityNumber"))) {
            logInfo("Response matcher: referenceIdentityNumber does not match expected["+person.getReferenceIdentityNumber()+"], found["+app_info.get("referenceIdentityNumber")+"]");
            return false;
        }
        if (!person.getEmail().equals(app_info.get("email"))) {
            logInfo("Response matcher: email does not match expected["+person.getEmail()+"], found["+app_info.get("email")+"]");
            return false;
        }
        if (!person.getPostalCode().equals(app_info.get("postalCode"))) {
            logInfo("Response matcher: postalCode does not match expected["+person.getPostalCode()+"], found["+app_info.get("postalCode")+"]");
            return false;
        }

        return true;
    }

}