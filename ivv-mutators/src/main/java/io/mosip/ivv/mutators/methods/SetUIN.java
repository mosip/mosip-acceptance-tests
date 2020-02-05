package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.utils.Utils;

public class SetUIN extends Step implements StepInterface {

    private Person person;

    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        if(step.getParameters().size() > 0){
            String val = step.getParameters().get(0);
            store.getScenarioData().getPersona().getPersons().get(index).setUin(val);
            if(val.equals("EMPTY")){
                store.getScenarioData().getPersona().getPersons().get(index).setUin("");
            }
        }
    }

}