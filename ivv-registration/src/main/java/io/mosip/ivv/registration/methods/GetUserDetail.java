package io.mosip.ivv.registration.methods;

import com.aventstack.extentreports.Status;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.dto.UserDTO;
import io.mosip.registration.entity.UserDetail;
import io.mosip.registration.service.login.LoginService;

import java.util.ArrayList;

public class GetUserDetail extends Step implements StepInterface {

    @Override
    public void run(Scenario.Step step) {
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
