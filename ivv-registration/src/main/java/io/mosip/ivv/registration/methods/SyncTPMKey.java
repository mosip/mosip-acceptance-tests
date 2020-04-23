package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.sync.MasterSyncService;
import io.mosip.registration.service.sync.TPMPublicKeySyncService;

public class SyncTPMKey extends BaseStep implements StepInterface {



    @Override
    public void run() {
        String tpmKey = "";
        TPMPublicKeySyncService ms = store.getRegApplicationContext().getBean(TPMPublicKeySyncService.class);
        ResponseDTO responseDTO = null;
        try {
            tpmKey = ms.syncTPMPublicKey();
            this.store.getCurrentRegistrationUSer().setKeyIndex(tpmKey);
        } catch (RegBaseCheckedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return;
        }
        if(tpmKey != null && !tpmKey.isEmpty()){
            logInfo("TPM key: "+tpmKey);
            return;
        }else{
            this.hasError = true;
            logInfo("Unable to get TPM key");
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
