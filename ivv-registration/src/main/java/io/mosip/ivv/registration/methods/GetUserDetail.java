package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.registration.dto.UserDTO;
import io.mosip.registration.service.login.LoginService;

public class GetUserDetail extends BaseStep implements StepInterface {

    @Override
    public void run() {
        String jsonInString = "";
        LoginService ls = store.getRegApplicationContext().getBean(LoginService.class);
        UserDTO user =  ls.getUserDetail("13131313");
        if(user != null){
            System.out.println(user.getId());
        }else{
            System.out.println("user is null");
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
