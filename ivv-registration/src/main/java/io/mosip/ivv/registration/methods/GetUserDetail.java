package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.registration.dto.UserDTO;
import io.mosip.registration.service.login.LoginService;

public class GetUserDetail extends Step implements StepInterface {

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
}
