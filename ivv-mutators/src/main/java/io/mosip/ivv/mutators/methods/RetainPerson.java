package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;

public class RetainPerson extends Step implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError {

    }

    @Override
    public void run() {
        int index = -1;
        for(int i = 0; i < store.getScenarioData().getPersona().getPersons().size(); i++){
            if(store.getCurrentPerson().getId().equals(store.getScenarioData().getPersona().getPersons().get(i).getId())){
                index = i;
            }
        }
        if(index > -1){
            store.getScenarioData().getPersona().getPersons().add(index, store.getCurrentPerson());
            logInfo("Retaining Person id: "+store.getCurrentPerson().getId());
        }
    }
}
