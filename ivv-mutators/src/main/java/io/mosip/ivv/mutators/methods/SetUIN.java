package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.utils.Utils;

public class SetUIN extends BaseStep implements StepInterface {

    private Person person;



    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        if(step.getParameters().size() > 0){
            String val = step.getParameters().get(0);
            store.getPersona().getPersons().get(index).setUin(val);
            if(val.equals("EMPTY")){
                store.getPersona().getPersons().get(index).setUin("");
            }
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