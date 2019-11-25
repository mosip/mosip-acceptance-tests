package io.mosip.ivv.registration.config;

import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.OSIDataDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.RegistrationMetaDataDTO;
import io.mosip.registration.dto.biometric.BiometricDTO;
import io.mosip.registration.dto.demographic.ApplicantDocumentDTO;
import io.mosip.registration.dto.demographic.DemographicDTO;
import io.mosip.registration.dto.demographic.DemographicInfoDTO;
import io.mosip.registration.service.config.GlobalParamService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class Setup {

    public static class SetupResponse{
        public ApplicationContext applicationContext;
        public Object localApplicationContext;
    }

    public static SetupResponse init(){
        Setup.SetupResponse setupResponse = new SetupResponse();
//        System.setProperty("file.encoding", "UTF-8");
//        System.setProperty("spring.profiles.active", "qa");
        io.mosip.registration.context.ApplicationContext localApplicationContext = io.mosip.registration.context.ApplicationContext.getInstance();
        localApplicationContext.getApplicationMap().put(RegistrationConstants.INITIAL_SETUP, "Y");
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        SessionContext.setApplicationContext(applicationContext);
        GlobalParamService gs = applicationContext.getBean(GlobalParamService.class);
        io.mosip.registration.context.ApplicationContext.setApplicationMap(gs.getGlobalParams());
        localApplicationContext.loadResourceBundle();

        setupResponse.applicationContext = applicationContext;
        setupResponse.localApplicationContext = localApplicationContext;
        return setupResponse;
    }

    public static RegistrationDTO getRegistrationDTO(){
        RegistrationDTO registrationDTO = new RegistrationDTO();
        DemographicDTO demographicDTO = new DemographicDTO();
        demographicDTO.setApplicantDocumentDTO(new ApplicantDocumentDTO());
        demographicDTO.setDemographicInfoDTO(new DemographicInfoDTO());
        registrationDTO.setDemographicDTO(demographicDTO);
        registrationDTO.setBiometricDTO(new BiometricDTO());
        registrationDTO.setRegistrationMetaDataDTO(new RegistrationMetaDataDTO());
        registrationDTO.setOsiDataDTO(new OSIDataDTO());
        return registrationDTO;
    }
}
