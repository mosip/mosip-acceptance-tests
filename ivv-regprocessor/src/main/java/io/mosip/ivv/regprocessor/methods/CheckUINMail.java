package io.mosip.ivv.regprocessor.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.MailHelper;

import java.util.ArrayList;

import static io.restassured.RestAssured.given;

public class CheckUINMail extends BaseStep implements StepInterface {

    private int delay = 200;
    private String finalStatus;

    @Override
    public void run() {
        int counter = 0;
        int repeats = 20;
        String expectedStatus = "";
        try {
            repeats = Integer.parseInt(step.getParameters().get(0));
        } catch ( IndexOutOfBoundsException | NumberFormatException e  ) {

        }

        try {
            expectedStatus = step.getParameters().get(1);
        } catch ( IndexOutOfBoundsException e ) {

        }

        while(counter < repeats){
            logInfo("Checking the User email ("+store.getCurrentPerson().getUserid()+") for UIN mail");
            RequestDataDTO requestData = prepare();
            ResponseDataDTO responseData = call(requestData);
            process(responseData);
            String emailBody = getStatus(step);
            if(emailBody != null && !emailBody.isEmpty()){
                logInfo("UIN mail has been found");
                logInfo(emailBody);
                return;
            }else{
                if(hasError){
                    return;
                }
            }
            counter++;
        }
        logInfo("UIN mail not found");
        this.hasError = true;
    }

    private String getStatus(Scenario.Step step){
        try {
            logInfo("Retrying after 10 seconds...");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logSevere(e.getMessage());
            this.hasError = true;
            return "";
        }

        String mailReceived = "";
        ArrayList<String> subjects = new ArrayList<String>(){{
            add("UIN Generated");
        }};
        String regex = "";
        MailHelper.MailHelperResponse mailHelperResponse = MailHelper.readviaRegex(subjects, regex, store.getCurrentPerson().getUserid(), 50);
        if(mailHelperResponse != null){
            logInfo("Msg found: "+mailHelperResponse.getBody());
            mailReceived = mailHelperResponse.getBody();
        }
        return mailReceived;
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