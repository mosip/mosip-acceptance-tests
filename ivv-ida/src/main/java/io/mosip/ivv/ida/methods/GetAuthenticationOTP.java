package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.utils.MailHelper;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class GetAuthenticationOTP extends BaseStep implements StepInterface {



    @Override
    public void run() {
        int counter = 0;
        int repeats = 10;
        String expectedStatus = "";
        try {
            repeats = Integer.parseInt(step.getParameters().get(0));
        } catch ( IndexOutOfBoundsException | NumberFormatException e  ) {

        }

        while(counter < repeats){
            logInfo("Checking Authentication OTP User email ("+store.getCurrentPerson().getUserid()+") for UIN mail");
            String otp = getOTP();
            if(otp != null && !otp.isEmpty()){
                logInfo("OTP: "+otp);
                store.getCurrentPerson().setAuthenticationOTP(otp);
                return;
            }else{
                if(hasError){
                    return;
                }
            }
            counter++;
        }
        logInfo("OTP not found");
        this.hasError = true;
    }

    private String getOTP(){
        try {
            logInfo("Retrying after 10 seconds...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return "";
        }

        String otp = "";
        ArrayList<String> subjects = new ArrayList<String>(){{
            add("UIN XXXXXXXX"+store.getCurrentPerson().getUin().substring(store.getCurrentPerson().getUin().length() - 2)+": Requête OTP");
            add("UIN XXXXXXXX"+store.getCurrentPerson().getUin().substring(store.getCurrentPerson().getUin().length() - 2)+": OTP Request");
        }};
        String regex = "([0-9]{6})";
        MailHelper.MailHelperResponse mailHelperResponse = MailHelper.readviaRegex(subjects, regex, store.getCurrentPerson().getUserid(), 50);
        if(mailHelperResponse != null){
            logInfo("Msg found: "+mailHelperResponse.getBody());
            otp = mailHelperResponse.getRegexout();
        }
        return otp;
    }

    public RequestDataDTO prepare(){
        return null;
    }

    public ResponseDataDTO call(RequestDataDTO data){
        return null;
    }

    public void process(ResponseDataDTO res){

    }

}
