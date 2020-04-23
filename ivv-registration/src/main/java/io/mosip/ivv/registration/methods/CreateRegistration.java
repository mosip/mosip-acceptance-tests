package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.registration.config.Setup;
import io.mosip.registration.dto.RegistrationDTO;


public class CreateRegistration extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RegistrationDTO registrationDTO = Setup.getRegistrationDTO();
        this.store.setRegistrationDto(registrationDTO);
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
