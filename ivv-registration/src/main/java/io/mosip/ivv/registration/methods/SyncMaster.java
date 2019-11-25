package io.mosip.ivv.registration.methods;

import com.aventstack.extentreports.Status;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.sync.MasterSyncService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SyncMaster extends Step implements StepInterface {

    @Override
    public void run(Scenario.Step step) {
        MasterSyncService ms = store.getRegApplicationContext().getBean(MasterSyncService.class);

        //TODO variants needs to be added
        String masterSyncDetails = "MDS_J00001";
        String triggerPoint = "User";
        ResponseDTO responseDTO = null;
        try {
            responseDTO = ms.getMasterSync(masterSyncDetails, triggerPoint);
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
}
