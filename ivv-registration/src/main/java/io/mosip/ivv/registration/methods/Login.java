package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.dto.LoginUserDTO;
import io.mosip.registration.dto.UserDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.login.LoginService;
import io.mosip.registration.service.operator.UserOnboardService;

import java.io.IOException;
import java.util.Map;

public class Login extends Step implements StepInterface {
    
    @Override
    public void run() {
        String jsonInString = "";
        LoginService loginService = store.getRegApplicationContext().getBean(LoginService.class);
        UserOnboardService userOnboardService = store.getRegApplicationContext().getBean(UserOnboardService.class);
        Map<String, String> centerAndMachineId = userOnboardService.getMachineCenterId();
        ApplicationContext.map().put(RegistrationConstants.USER_CENTER_ID, centerAndMachineId.get(RegistrationConstants.USER_CENTER_ID));
        ApplicationContext.map().put(RegistrationConstants.USER_STATION_ID, centerAndMachineId.get(RegistrationConstants.USER_STATION_ID));

        UserDTO userDTO = loginService.getUserDetail(store.getCurrentRegistrationUSer().getUserId());
        LoginUserDTO ldto = new LoginUserDTO();
        ldto.setUserId(store.getCurrentRegistrationUSer().getUserId());
        ldto.setPassword(store.getCurrentRegistrationUSer().getPassword());
        ApplicationContext.map().put("userDTO", ldto);
        AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();
        authenticationValidatorDTO.setUserId(store.getCurrentRegistrationUSer().getUserId());
        authenticationValidatorDTO.setPassword(store.getCurrentRegistrationUSer().getPassword());
        authenticationValidatorDTO.setAuthValidationType("PWD");
        Boolean isInitialSetup = true;
        Boolean isUserNewToMachine = true;
        if(userDTO != null){
            System.out.println("user found");
            Boolean scResponse = null;
            try {
                scResponse = SessionContext.create(userDTO, "PWD", isInitialSetup, isUserNewToMachine, authenticationValidatorDTO);
            } catch (RegBaseCheckedException e) {
                e.printStackTrace();
                logSevere(e.getMessage());
                this.hasError = true;
                return;
            } catch (IOException e) {
                e.printStackTrace();
                logSevere(e.getMessage());
                this.hasError = true;
                return;
            }
            if(scResponse){
                logInfo("SessionContext successfully created");
            }else{
                logInfo("SessionContext not created");
                this.hasError = true;
            }
        }else{
            logInfo("user not found in local db");
            this.hasError = true;
        }
    }
}
