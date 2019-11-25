package io.mosip.ivv.registration.methods;

import com.aventstack.extentreports.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.registration.config.Setup;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.*;
import io.mosip.registration.entity.UserDetail;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.login.LoginService;
import io.mosip.registration.service.operator.UserDetailService;
import io.mosip.registration.service.operator.UserOnboardService;

import java.io.IOException;
import java.util.*;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

public class Login extends Step implements StepInterface {

    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        String jsonInString = "";
        LoginService loginService = store.getRegApplicationContext().getBean(LoginService.class);
        UserOnboardService userOnboardService = store.getRegApplicationContext().getBean(UserOnboardService.class);
        Map<String, String> centerAndMachineId = userOnboardService.getMachineCenterId();
        ApplicationContext.map().put(RegistrationConstants.USER_CENTER_ID, centerAndMachineId.get(RegistrationConstants.USER_CENTER_ID));
        ApplicationContext.map().put(RegistrationConstants.USER_STATION_ID, centerAndMachineId.get(RegistrationConstants.USER_STATION_ID));

        UserDTO userDTO = loginService.getUserDetail(store.getScenarioData().getOperator().getUserid());
        LoginUserDTO ldto = new LoginUserDTO();
        ldto.setUserId(store.getScenarioData().getOperator().getUserid());
        ldto.setPassword(store.getScenarioData().getOperator().getPassword());
        ApplicationContext.map().put("userDTO", ldto);
        AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();
        authenticationValidatorDTO.setUserId(store.getScenarioData().getOperator().getUserid());
        authenticationValidatorDTO.setPassword(store.getScenarioData().getOperator().getPassword());
        authenticationValidatorDTO.setAuthValidationType("PWD");
        Boolean isInitialSetup = true;
        Boolean isUserNewToMachine = false;
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
