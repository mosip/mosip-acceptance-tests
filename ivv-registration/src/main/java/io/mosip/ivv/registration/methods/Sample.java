package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dto.RegistrationDTO;

public class Sample extends BaseStep implements StepInterface {



    @Override
    public void run() {
        RegistrationDTO registrationDTO = (RegistrationDTO) store.getRegistrationDto();
//        ApplicationContext applicationContext = store.regApplicationContext.getBean(ApplicationContext.class);

        ApplicationContext applicationContext = (ApplicationContext) store.getRegLocalContext();
        System.out.println(applicationContext.applicationLanguage());
        System.out.println(applicationContext.localLanguage());
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
