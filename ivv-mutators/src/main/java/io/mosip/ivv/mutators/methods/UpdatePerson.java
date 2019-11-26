package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;

public class UpdatePerson extends Step implements StepInterface {

    private enum fields {
        name, email, registrationId,uin, preRegistrationId, dob, gender, residenceStatus, zone, centerId
    }

    @Override
    public void validateStep() throws RigInternalError {
        if(step.getParameters().size() < 2){
            throw new RigInternalError("DSL error: Expect key and its value");
        }

        if(step.getParameters().get(0).isEmpty()){
            throw new RigInternalError("DSL error: key should not be empty");
        }
        try {
            fields.valueOf(step.getParameters().get(0));
        } catch (IllegalArgumentException ex) {
            throw new RigInternalError("DSL error: Key does not match a valid field");
        }
    }

    @Override
    public void run() {
        String key = step.getParameters().get(0);
        String value = step.getParameters().get(1);
        if(value == "null"){
            value = null;
        }
        switch(fields.valueOf(key)){
            case name:
                store.getCurrentPerson().setName(value);
                break;

            case email:
                store.getCurrentPerson().setEmail(value);
                break;

            case registrationId:
                store.getCurrentPerson().setRegistrationId(value);
                break;

            case uin:
                store.getCurrentPerson().setUin(value);
                break;

            case preRegistrationId:
                store.getCurrentPerson().setPreRegistrationId(value);
                break;

            case dob:
                store.getCurrentPerson().setDateOfBirth(value);
                break;

            case gender:
                store.getCurrentPerson().setGender(value);
                break;

            case residenceStatus:
                store.getCurrentPerson().setResidenceStatus(value);
                break;

            case zone:
                store.getCurrentPerson().setZone(value);
                break;

            case centerId:
                store.getCurrentPerson().setCenter_id(value);
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as key: " + key + " not found");
                return;
        }
    }
}
