package io.mosip.ivv.registration.methods;

import com.aventstack.extentreports.Status;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Person;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.*;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.packet.PacketHandlerService;
import io.mosip.registration.service.sync.PacketSynchService;

import java.util.ArrayList;
import java.util.List;

public class SyncPacket extends Step implements StepInterface {

    private Person person;

    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        this.person = this.store.getScenarioData().getPersona().getPersons().get(index);
        PacketSynchService packetSynchService = store.getRegApplicationContext().getBean(PacketSynchService.class);
        try {
            String response = packetSynchService.packetSync(person.getRegistrationId());
            logInfo("Response: "+response);
        } catch (RegBaseCheckedException e) {
            this.hasError = true;
            e.printStackTrace();
            logSevere("Internal error: "+e.getMessage());
            return;
        }
//        List<PacketStatusDTO> packetsToBeSynced = packetSynchService.fetchPacketsToBeSynched();
//        if(packetsToBeSynced != null && packetsToBeSynced.size() > 0){
//            for(PacketStatusDTO psdto: packetsToBeSynced){
//                if(psdto.getFileName().equals(registrationDTO.getRegistrationId())){
//                    Utils.auditLog.info("Packet to be synced: "+psdto.getFileName());
//                    reports.add(new ExtentLogger(Status.INFO, "Packet to be synced: "+psdto.getFileName()));
//                    String response = "";
//                    try {
//                        response = packetSynchService.packetSync(packetsToBeSynced);
//                    } catch (RegBaseCheckedException e) {
//                        this.hasError = true;
//                        e.printStackTrace();
//                        return;
//                    }
//                    if(response.equals("SYNC_FAILURE")){
//                        Utils.auditLog.warning("Packet sync failed with response: "+response);
//                        reports.add(new ExtentLogger(Status.INFO, "Packet sync failed with response: "+response));
//                        this.hasError=true;
//                        return;
//                    } else {
//                        Utils.auditLog.info("Packet sync passed with response: "+response);
//                        reports.add(new ExtentLogger(Status.INFO, "Packet sync passed with response: "+response));
//                        return;
//                    }
//                }
//            }
//            Utils.auditLog.warning("Packet not found in synced list");
//            reports.add(new ExtentLogger(Status.INFO, "Packet not found in synced list"));
//            this.hasError=true;
//            return;
//        }else{
//            Utils.auditLog.info("No packet to be synced");
//            reports.add(new ExtentLogger(Status.INFO, "No packet to be synced"));
//        }
    }
}
