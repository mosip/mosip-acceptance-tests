package io.mosip.ivv.preregistration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class UpdateApplication extends BaseStep implements StepInterface {
    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     *
     *
     */
    @SuppressWarnings({ "unchecked", "serial" })
	@Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        JSONObject identity_json = new JSONObject();
        for (Map.Entry<String, IDObjectField> entry : store.getCurrentPerson().getIdObject().entrySet()) {
            String key = entry.getKey();
            IDObjectField idField = entry.getValue();
            if(idField.getType().equals(IDObjectField.type.simpleType)){
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
        request_json.put("langCode", store.getCurrentPerson().getPrimaryLang());

        JSONObject requestData = new JSONObject();
        requestData.put("id", "mosip.pre-registration.demographic.update");
        requestData.put("version", "1.0");
        requestData.put("requesttime", Utils.getCurrentDateAndTimeForAPI());

        JSONObject demographic_json = new JSONObject();
        demographic_json.put("identity", identity_json);
        request_json.put("demographicDetails", demographic_json);
        requestData.put("request", request_json);
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/applications/"+store.getCurrentPerson().getPreRegistrationId();
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = given().relaxedHTTPSValidation()
                .contentType(ContentType.JSON).body(data.getRequest())
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .put(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        Helpers.logCallRecord(this.callRecord);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }

}
