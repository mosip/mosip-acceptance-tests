package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.packet.RegistrationApprovalService;

public class ApproveRegistration extends BaseStep implements StepInterface {

    @Override
    public void run() {
        String registrationId =  store.getCurrentPerson().getRegistrationId();
        String comment = "";
        String statusCode = "APPROVED";

        RegistrationApprovalService registrationApprovalService = store.getRegApplicationContext().getBean(RegistrationApprovalService.class);
        Registration registration = null;
        try {
            registration = registrationApprovalService.updateRegistration(registrationId, comment, statusCode);
        } catch (RegBaseCheckedException e) {
            logSevere(e.getMessage());
        } catch (Exception e) {
            logSevere(e.getMessage());
            this.hasError = true;
            e.printStackTrace();
            return;
        }
        if(registration.getId().equals(registrationId) && registration.getClientStatusCode().equals("APPROVED")){
            logInfo("Registration id: "+registration.getId()+", status: "+registration.getClientStatusCode());
        }else{
            logInfo("Registration id: "+registration.getId()+", status: "+registration.getClientStatusCode());
            this.hasError = true;
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