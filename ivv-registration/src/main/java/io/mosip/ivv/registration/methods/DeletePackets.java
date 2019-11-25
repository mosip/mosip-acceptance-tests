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
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.*;
import io.mosip.registration.service.packet.PacketHandlerService;
import io.mosip.registration.service.packet.RegPacketStatusService;

import java.util.ArrayList;

public class DeletePackets extends Step implements StepInterface {

    @Override
    public void run() {
        /*  packets with status PROCESSED will be deleted */
        this.index = Utils.getPersonIndex(step);
        RegPacketStatusService serv = store.getRegApplicationContext().getBean(RegPacketStatusService.class);

        /* logging parameters */
        ResponseDTO responseDTO = serv.deleteRegistrationPackets();

        if(responseDTO.getErrorResponseDTOs() != null && responseDTO.getErrorResponseDTOs().size() > 0){
            for(ErrorResponseDTO es: responseDTO.getErrorResponseDTOs()){
                logInfo("Message: "+es.getMessage()+", code: "+es.getCode()+", infoType: "+es.getInfoType());
            }
            this.hasError = true;
        }else{
            SuccessResponseDTO es = responseDTO.getSuccessResponseDTO();
            logInfo("Message: "+es.getMessage()+", code: "+es.getCode()+", infoType: "+es.getInfoType());
        }
    }
}
