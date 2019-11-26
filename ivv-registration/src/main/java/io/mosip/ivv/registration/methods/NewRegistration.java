package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.structures.PersonaDef;
import io.mosip.ivv.registration.config.Setup;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.RegistrationDTO;


public class NewRegistration extends Step implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError{
        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD) && store.getCurrentIntroducer() == null){
            throw new RigInternalError("Introducer is required to process this step");
        }
    }

    @Override
    public void run() {
        RegistrationDTO registrationDTO = Setup.getRegistrationDTO();
        registrationDTO.getRegistrationMetaDataDTO().setRegistrationCategory("New");

        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD)){
            SessionContext.map().put("isChild", true);
            registrationDTO.setUpdateUINChild(false);
            registrationDTO.getOsiDataDTO().setIntroducerType("Parent");
        } else {
            SessionContext.map().put("isChild", false);
            registrationDTO.setUpdateUINChild(false);
            registrationDTO.getOsiDataDTO().setIntroducerType("NONE");
        }

        this.store.setRegistrationDto(registrationDTO);
    }

}
