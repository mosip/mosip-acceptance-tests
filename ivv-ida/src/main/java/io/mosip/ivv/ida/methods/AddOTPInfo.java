package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;

public class AddOTPInfo extends BaseStep implements StepInterface {



    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        store.getCurrentPerson().getAuthenticationJSON().put("otp", store.getCurrentPerson().getAuthenticationOTP());
        store.getCurrentPerson().getAuthParams().add("otp");
        return null;
    }

    public ResponseDataDTO call(RequestDataDTO data){
        return null;
    }

    public void process(ResponseDataDTO res){

    }

}