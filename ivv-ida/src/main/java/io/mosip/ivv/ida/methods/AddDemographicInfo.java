package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public class AddDemographicInfo extends BaseStep implements StepInterface {



    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        JSONObject demographics_json = new JSONObject();
        demographics_json.put("phoneNumber", store.getCurrentPerson().getPhone());
        demographics_json.put("email", store.getCurrentPerson().getUserid());
        demographics_json.put("fullName", store.getCurrentPerson().getUserid());
        store.getCurrentPerson().getAuthenticationJSON().put("demographics", demographics_json);
        store.getCurrentPerson().getAuthParams().add("demo");
        return new RequestDataDTO(null, demographics_json.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
       return null;
    }

    public void process(ResponseDataDTO res){

    }

}