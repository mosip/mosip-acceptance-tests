package io.mosip.ivv.registration.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.service.packet.PacketHandlerService;

public class CreatePacket extends BaseStep implements StepInterface {



    @Override
    public void run() {
        this.index = Utils.getPersonIndex(step);
        //TODO create registrationDTO method
        String center_id = (String) ApplicationContext.map().get(RegistrationConstants.USER_CENTER_ID);
        String machine_id = (String) ApplicationContext.map().get(RegistrationConstants.USER_STATION_ID);
        logInfo("center_id: "+center_id+", machine_id: "+machine_id);
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();
        /* operator osi data */
        registrationDTO.getOsiDataDTO().setOperatorAuthenticatedByPassword(true);
        registrationDTO.getOsiDataDTO().setOperatorID(store.getCurrentRegistrationUSer().getUserId());

        String registrationId = Utils.generateRID(center_id, machine_id);
        store.getCurrentPerson().setRegistrationId(registrationId);
        registrationDTO.setRegistrationId(registrationId);

        /* Setting metadata */
        registrationDTO.getRegistrationMetaDataDTO().setConsentOfApplicant("Y");
        registrationDTO.getRegistrationMetaDataDTO().setCenterId(center_id);
        registrationDTO.getRegistrationMetaDataDTO().setMachineId(machine_id);

        ObjectMapper mapper = new ObjectMapper();
        try {
            logInfo("OSI data: "+mapper.writeValueAsString(registrationDTO.getOsiDataDTO()));
            logInfo("Identity data: "+mapper.writeValueAsString(registrationDTO.getDemographicDTO().getDemographicInfoDTO()));
            logInfo("Documents data: "+registrationDTO.getDemographicDTO().getApplicantDocumentDTO().getDocuments().toString());
            logInfo("Biometrics data: "+registrationDTO.getBiometricDTO().getApplicantBiometricDTO().toString());
            logInfo("Meta data: "+mapper.writeValueAsString(registrationDTO.getRegistrationMetaDataDTO()));
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
