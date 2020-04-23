package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.dto.*;
import io.mosip.registration.service.packet.RegPacketStatusService;

public class DeletePackets extends BaseStep implements StepInterface {

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
