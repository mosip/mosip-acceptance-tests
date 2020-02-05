package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.registration.config.Setup;
import io.mosip.registration.dto.RegistrationDTO;


public class CreateRegistration extends Step implements StepInterface {
    @Override
    public void run() {
        RegistrationDTO registrationDTO = Setup.getRegistrationDTO();
        this.store.setRegistrationDto(registrationDTO);
    }
}
