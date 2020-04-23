package io.mosip.ivv.kernel.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.Collection;

import static io.restassured.RestAssured.given;

public class Login extends BaseStep implements StepInterface {

    private enum users {
        regUser, partner
    }

    @Override
    public void validateStep() throws RigInternalError {
        if(step.getParameters().size() > 0){
            try {
                users.valueOf(step.getParameters().get(0));
            } catch (IllegalArgumentException ex) {
                throw new RigInternalError("DSL error: Key does not match a valid field");
            }
        } else {
            step.getParameters().add(0, users.regUser.name());
        }

    }

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        JSONObject request_json = new JSONObject();
        switch(users.valueOf(step.getParameters().get(0))){

            case partner:
                Partner partner = this.store.getCurrentPartner();
                request_json.put("appId", "registrationprocessor");
                request_json.put("password", partner.getPassword());
                request_json.put("userName", partner.getUserId());
                break;

            default:
                RegistrationUser regUser = this.store.getCurrentRegistrationUSer();
                request_json.put("appId", "registrationprocessor");
                request_json.put("password", regUser.getPassword());
                request_json.put("userName", regUser.getUserId());
                break;
        }

        JSONObject requestData = new JSONObject();
        requestData.put("id", "");
        requestData.put("version", "1.0");
        requestData.put("requesttime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("request", request_json);

        String url = "/"+System.getProperty("ivv.global.version")+"/authmanager/authenticate/useridPwd";
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = (Response) given()
                .contentType(ContentType.JSON).body(data.getRequest())
                .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){
        ReadContext ctx = JsonPath.parse(res.getBody());
        Collection<String> values = res.getCookies().values();
        for (String value : values) {
            store.getHttpData().setCookie(value);
            logInfo("Cookie auth token: " + value);
        }
    }



}