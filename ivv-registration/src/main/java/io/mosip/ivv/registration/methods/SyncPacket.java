package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.sync.PacketSynchService;

public class SyncPacket extends BaseStep implements StepInterface {

    @Override
    public void assertAPI() {

    }

    @Override
    public void run() {
        PacketSynchService packetSynchService = store.getRegApplicationContext().getBean(PacketSynchService.class);
        try {
            String response = packetSynchService.packetSync(store.getCurrentPerson().getRegistrationId());
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
