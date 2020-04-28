package io.mosip.ivv.mutators.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;


public class PrintPerson extends BaseStep implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError {

    }

    @Override
    public void run() throws RigInternalError {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(store.getCurrentPerson());
            logInfo(jsonInString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RigInternalError("Error in PrintPerson: "+e.getMessage());
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
