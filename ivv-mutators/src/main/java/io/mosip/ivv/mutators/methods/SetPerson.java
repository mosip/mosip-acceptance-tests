package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;

public class SetPerson extends BaseStep implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError {
        if(step.getParameters().size() == 0){
            throw new RigInternalError("DSL error: Please specify the index of person from Persona");
        }

        if(step.getParameters().get(0).isEmpty()){
            throw new RigInternalError("DSL error: Specify the index of person from Persona");
        }

        try{
            Integer.parseInt(step.getParameters().get(0));
        } catch (NumberFormatException e){
            throw new RigInternalError("DSL error: Specify the index of person from Persona, info: "+e.getMessage());
        }
    }



    @Override
    public void run() {
        store.setCurrentPerson(store.getPersona().getPersons().get(Integer.parseInt(step.getParameters().get(0))));
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
