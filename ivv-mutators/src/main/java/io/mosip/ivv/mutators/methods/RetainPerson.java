package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;

public class RetainPerson extends BaseStep implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError {

    }



    @Override
    public void run() {
        int index = -1;
        for(int i = 0; i < store.getPersona().getPersons().size(); i++){
            if(store.getCurrentPerson().getId().equals(store.getPersona().getPersons().get(i).getId())){
                index = i;
            }
        }
        if(index > -1){
            store.getPersona().getPersons().add(index, store.getCurrentPerson());
            logInfo("Retaining Person id: "+store.getCurrentPerson().getId());
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
