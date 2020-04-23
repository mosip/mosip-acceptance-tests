package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.operator.UserSaltDetailsService;

public class SyncUserSalt extends BaseStep implements StepInterface {

    @Override
    public void run() {
        UserSaltDetailsService serv = store.getRegApplicationContext().getBean(UserSaltDetailsService.class);
        ResponseDTO responseDTO = null;
        try {
            responseDTO = serv.getUserSaltDetails("system");
        } catch (RegBaseCheckedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return;
        }
        if(responseDTO.getErrorResponseDTOs() != null && responseDTO.getErrorResponseDTOs().size() > 0){
            for(ErrorResponseDTO es: responseDTO.getErrorResponseDTOs()){
                logInfo("Message: "+es.getMessage()+", code: "+es.getCode()+", infoType: "+es.getInfoType());
            }
            this.hasError = true;
            return;
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
