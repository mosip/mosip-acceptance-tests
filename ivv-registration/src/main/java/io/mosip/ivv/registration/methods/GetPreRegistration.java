package io.mosip.ivv.registration.methods;

import com.aventstack.extentreports.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Person;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.registration.config.Setup;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.service.config.GlobalParamService;
import io.mosip.registration.service.sync.PreRegistrationDataSyncService;

import java.util.ArrayList;

public class GetPreRegistration extends Step implements StepInterface {

    private Person person;

    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);
        SessionContext.map().put("registrationDTOContent", Setup.getRegistrationDTO());

        PreRegistrationDataSyncService serv = store.getRegApplicationContext().getBean(PreRegistrationDataSyncService.class);
        ResponseDTO responseDTO = serv.getPreRegistration("38142157162067");
        if(responseDTO.getErrorResponseDTOs() != null && responseDTO.getErrorResponseDTOs().size() > 0){
            for(ErrorResponseDTO es: responseDTO.getErrorResponseDTOs()){
                logSevere("Message: "+es.getMessage()+", code: "+es.getCode()+", info: "+es.getInfoType());
            }
            this.hasError = true;
            return;
        }else{
            SuccessResponseDTO es = responseDTO.getSuccessResponseDTO();
            RegistrationDTO registrationDTO = (RegistrationDTO) SessionContext.map().get("registrationDTOContent");
            this.store.setRegistrationDto(registrationDTO);
            logInfo("Message: "+es.getMessage()+", code: "+es.getCode()+", infoType: "+es.getInfoType());
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(SessionContext.map().get("registrationDTOContent"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logSevere("Jackson ObjectMapper: "+e.getMessage());
        }
    }
}