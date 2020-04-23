package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.dtos.PersonaDef;

public class UpdateRegistrationUser extends BaseStep implements StepInterface {

    private enum fields {
        userId, password, centerId, type
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

        if(fields.valueOf(step.getParameters().get(0)).equals(fields.type)){
            try {
                PersonaDef.ROLE.valueOf(step.getParameters().get(1));
            } catch (IllegalArgumentException ex) {
                throw new RigInternalError("DSL error: value is invalid");
            }
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
            case userId:
                store.getCurrentRegistrationUSer().setUserId(value);
                break;

            case password:
                store.getCurrentRegistrationUSer().setPassword(value);
                break;

            case type:
                store.getCurrentRegistrationUSer().setRole(PersonaDef.ROLE.valueOf(value));
                break;

            case centerId:
                store.getCurrentRegistrationUSer().setCenterId(value);
                break;

            default:
                logWarning("Skipping step " + step.getName() + " as key: " + key + " not found");
                return;
        }
    }

    @Override
    public RequestDataDTO prepare() {
        return null;
    }

    @Override
    public ResponseDataDTO call(RequestDataDTO requestData) {
        return null;
    }

    @Override
    public void process(ResponseDataDTO res) {

    }
}
