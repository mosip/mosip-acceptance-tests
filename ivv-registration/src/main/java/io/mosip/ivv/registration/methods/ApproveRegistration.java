package io.mosip.ivv.registration.methods;

import com.aventstack.extentreports.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.dto.*;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.packet.PacketHandlerService;
import io.mosip.registration.service.packet.RegistrationApprovalService;

import java.util.ArrayList;

public class ApproveRegistration extends Step implements StepInterface {

    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);

        String registrationId =  store.getScenarioData().getPersona().getPersons().get(this.index).getRegistrationId();
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
}