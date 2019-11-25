package io.mosip.ivv.registration.methods;

import com.aventstack.extentreports.Status;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Person;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.PacketStatusDTO;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.operator.UserSaltDetailsService;
import io.mosip.registration.service.packet.PacketUploadService;
import io.mosip.registration.service.sync.PacketSynchService;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class UploadPacket extends Step implements StepInterface {

    private Person person;

    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);
        PacketSynchService syncserv = store.getRegApplicationContext().getBean(PacketSynchService.class);
        PacketUploadService serv = store.getRegApplicationContext().getBean(PacketUploadService.class);
        ResponseDTO responseDTO = null;

        PacketStatusDTO packetToBeUploaded = null;
        /* Get packet by rid */
        List<PacketStatusDTO> fetchPacketsToBeSynched = syncserv.fetchPacketsToBeSynched();
        for(PacketStatusDTO psdto: fetchPacketsToBeSynched){
            logInfo("Finding synced packet: ["+psdto.getFileName()+"] equals ["+person.getRegistrationId()+"]");
            if(psdto.getFileName().equals(person.getRegistrationId())){
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
}