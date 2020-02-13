package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import org.json.simple.JSONObject;

public class AddDemographicInfo extends Step implements StepInterface {

    @Override
    public void run() {
        JSONObject demographics_json = new JSONObject();
        demographics_json.put("phoneNumber", store.getCurrentPerson().getPhone());
        demographics_json.put("email", store.getCurrentPerson().getUserid());
        demographics_json.put("fullName", store.getCurrentPerson().getUserid());
        store.getCurrentPerson().getAuthenticationJSON().put("demographics", demographics_json);
        store.getCurrentPerson().getAuthParams().add("demo");
    }

}