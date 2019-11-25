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
import io.mosip.kernel.core.idgenerator.spi.RidGenerator;
import io.mosip.kernel.idgenerator.rid.impl.RidGeneratorImpl;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.*;
import io.mosip.registration.service.packet.PacketHandlerService;

import java.util.ArrayList;

public class CreatePacket extends Step implements StepInterface {

    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        //TODO create registrationDTO method
        String center_id = (String) ApplicationContext.map().get(RegistrationConstants.USER_CENTER_ID);
        String machine_id = (String) ApplicationContext.map().get(RegistrationConstants.USER_STATION_ID);
        logInfo("center_id: "+center_id+", machine_id: "+machine_id);
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();
        RidGenerator ridGenerator = new RidGeneratorImpl();
//        String registrationId = (String) ridGenerator.generateId(center_id, machine_id, 5, 5, 5, 14);
        String registrationId = Utils.generateRID(center_id, machine_id);
//        String registrationId = "12345678912345678912345678912";
        store.getScenarioData().getPersona().getPersons().get(this.index).setRegistrationId(registrationId);
        ArrayList<AuditDTO> ls = new ArrayList<AuditDTO>();
        registrationDTO.setRegistrationId(registrationId);
        registrationDTO.getOsiDataDTO().setIntroducerType("NONE");
        registrationDTO.getOsiDataDTO().setOperatorID(store.getScenarioData().getOperator().getUserid());

        /* Setting metadata */
        registrationDTO.getRegistrationMetaDataDTO().setConsentOfApplicant("Y");
        registrationDTO.getRegistrationMetaDataDTO().setRegistrationCategory("New");
        registrationDTO.getRegistrationMetaDataDTO().setCenterId(center_id);
        registrationDTO.getRegistrationMetaDataDTO().setMachineId(machine_id);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String registrationDTOString = mapper.writeValueAsString(registrationDTO);
            Utils.auditLog.info(registrationDTOString);
            extentInstance.info("Registration id: "+registrationDTO.getRegistrationId());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        PacketHandlerService packetHandlerService = store.getRegApplicationContext().getBean(PacketHandlerService.class);

        /* logging parameters */

        ResponseDTO responseDTO = packetHandlerService.handle(registrationDTO);

        if(responseDTO.getErrorResponseDTOs() != null && responseDTO.getErrorResponseDTOs().size() > 0){
            for(ErrorResponseDTO es: responseDTO.getErrorResponseDTOs()){
                logInfo("Message: "+es.getMessage()+", code: "+es.getCode()+", infoType: "+es.getInfoType());
            }
            this.hasError = true;
        }else{
            SuccessResponseDTO es = responseDTO.getSuccessResponseDTO();
            logInfo("Message: "+es.getMessage()+", code: "+es.getCode()+", infoType: "+es.getInfoType());
        }

        this.store.setRegistrationDto(registrationDTO);
    }
}
