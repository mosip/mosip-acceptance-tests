package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.PacketStatusDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.packet.PacketUploadService;
import io.mosip.registration.service.sync.PacketSynchService;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

public class UploadPacket extends BaseStep implements StepInterface {

    @Override
    public void run() {
        PacketSynchService syncserv = store.getRegApplicationContext().getBean(PacketSynchService.class);
        PacketUploadService serv = store.getRegApplicationContext().getBean(PacketUploadService.class);
        ResponseDTO responseDTO = null;

        PacketStatusDTO packetToBeUploaded = null;
        /* Get packet by rid */
        List<PacketStatusDTO> fetchPacketsToBeSynched = syncserv.fetchPacketsToBeSynched();
        for(PacketStatusDTO psdto: fetchPacketsToBeSynched){
            logInfo("Finding synced packet: ["+psdto.getFileName()+"] equals ["+store.getCurrentPerson().getRegistrationId()+"]");
            if(psdto.getFileName().equals(store.getCurrentPerson().getRegistrationId())){
                packetToBeUploaded = psdto;
                break;
            }
        }

        if(packetToBeUploaded == null){
            logInfo("RID not found in synced packets list: exiting the step");
            return ;
        }

        try {
            String ackFileName = packetToBeUploaded.getPacketPath();
            int lastIndex = ackFileName.indexOf(RegistrationConstants.ACKNOWLEDGEMENT_FILE);
            String packetPath = ackFileName.substring(0, lastIndex);
            logInfo("Packet path: "+packetPath+RegistrationConstants.ZIP_FILE_EXTENSION);
            responseDTO = serv.pushPacket(new File(packetPath+RegistrationConstants.ZIP_FILE_EXTENSION));
        } catch (URISyntaxException e) {
            logSevere(e.getMessage());
            this.hasError = true;
            e.printStackTrace();
            return;
        } catch (RegBaseCheckedException e) {
            logSevere(e.getMessage());
            this.hasError = true;
            e.printStackTrace();
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