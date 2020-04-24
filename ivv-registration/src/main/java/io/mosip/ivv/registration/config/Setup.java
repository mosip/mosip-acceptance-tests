package io.mosip.ivv.registration.config;

import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.OSIDataDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.RegistrationMetaDataDTO;
import io.mosip.registration.dto.biometric.*;
import io.mosip.registration.dto.demographic.ApplicantDocumentDTO;
import io.mosip.registration.dto.demographic.DemographicDTO;
import io.mosip.registration.dto.demographic.DemographicInfoDTO;
import io.mosip.registration.dto.demographic.DocumentDetailsDTO;
import io.mosip.registration.service.config.GlobalParamService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Setup {

    public static class SetupResponse{
        public ApplicationContext applicationContext;
        public Object localApplicationContext;
    }

    public static SetupResponse init(){
        Setup.SetupResponse setupResponse = new SetupResponse();
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
        ApplicantDocumentDTO appdt = new ApplicantDocumentDTO();
        appdt.setDocuments(new HashMap<String, DocumentDetailsDTO>());
        demographicDTO.setApplicantDocumentDTO(appdt);
        demographicDTO.setDemographicInfoDTO(new DemographicInfoDTO());
        registrationDTO.setDemographicDTO(demographicDTO);

        BiometricInfoDTO bidto = new BiometricInfoDTO();
        bidto.setFace(new FaceDetailsDTO());
        bidto.setFingerprintDetailsDTO(new ArrayList<FingerprintDetailsDTO>());
        bidto.setIrisDetailsDTO(new ArrayList<IrisDetailsDTO>());
        bidto.setExceptionFace(new FaceDetailsDTO());

        BiometricInfoDTO introBio = new BiometricInfoDTO();
        introBio.setFace(new FaceDetailsDTO());
        introBio.setFingerprintDetailsDTO(new ArrayList<FingerprintDetailsDTO>());
        introBio.setIrisDetailsDTO(new ArrayList<IrisDetailsDTO>());
        introBio.setExceptionFace(new FaceDetailsDTO());

        BiometricDTO bdto = new BiometricDTO();
        bdto.setApplicantBiometricDTO(bidto);
        bdto.setIntroducerBiometricDTO(introBio);
        registrationDTO.setBiometricDTO(bdto);

        registrationDTO.setRegistrationMetaDataDTO(new RegistrationMetaDataDTO());
        registrationDTO.setOsiDataDTO(new OSIDataDTO());
        return registrationDTO;
    }
}
