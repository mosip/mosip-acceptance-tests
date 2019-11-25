package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;

public class SetIntroducer  extends Step implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError {
        if(step.getParameters().size() == 0){
            throw new RigInternalError("DSL error: Please specify the index of introducer");
        }

        if(step.getParameters().get(0).isEmpty()){
            throw new RigInternalError("DSL error: Specify the index of introducer");
        }

        try{
            Integer.parseInt(step.getParameters().get(0));
        } catch (NumberFormatException e){
            throw new RigInternalError("DSL error: Specify the index of introducer, info: "+e.getMessage());
        }
    }

    @Override
    public void run() {
        store.setCurrentIntroducer(store.getScenarioData().getPersona().getPersons().get(Integer.parseInt(step.getParameters().get(0))));
    }
}
