package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.Person;

public class AddOTPInfo extends Step implements StepInterface {
    private Person person;

    @Override
    public void run() {
        store.getCurrentPerson().getAuthenticationJSON().put("otp", store.getCurrentPerson().getAuthenticationOTP());
        store.getCurrentPerson().getAuthParams().add("otp");
    }

}