package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dto.RegistrationDTO;

public class Sample extends Step implements StepInterface {

    @Override
    public void run() {
        RegistrationDTO registrationDTO = (RegistrationDTO) store.getRegistrationDto();
//        ApplicationContext applicationContext = store.regApplicationContext.getBean(ApplicationContext.class);

        ApplicationContext applicationContext = (ApplicationContext) store.getRegLocalContext();
        System.out.println(applicationContext.applicationLanguage());
        System.out.println(applicationContext.localLanguage());
    }
}
