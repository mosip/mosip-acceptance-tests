package io.mosip.ivv.registration.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.registration.config.Setup;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.demographic.IndividualIdentity;
import io.mosip.registration.dto.demographic.ValuesDTO;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AddApplicantDemographics extends Step implements StepInterface {

    private int index;
    /**
     * Method to create RegistrationDTO if not created and adding only demographic details to it.
     * @param step
     */
    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();
        IndividualIdentity individualIdentity = new IndividualIdentity();
        ApplicationContext applicationContext = (ApplicationContext) store.getRegLocalContext();

        SessionContext.map().put("isChild", false);
        registrationDTO.setUpdateUINChild(false);

        String platformLanguageCode = applicationContext.applicationLanguage();
        String localLanguageCode = applicationContext.localLanguage();

        //TODO complete the identity refer the fields from individualIdentity.class
        individualIdentity.setEmail(store.getScenarioData().getPersona().getPersons().get(this.index).getEmail());
        individualIdentity.setAddressLine1(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(index).getAddressLine1()));
        individualIdentity.setAddressLine2(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(index).getAddressLine2()));
        individualIdentity.setAddressLine3(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(index).getAddressLine3()));
        individualIdentity.setFullName(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(this.index).getName()));
        individualIdentity.setDateOfBirth(store.getScenarioData().getPersona().getPersons().get(this.index).getDateOfBirth());
//      individualIdentity.setAge(user.age);
        individualIdentity.setGender(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(this.index).getGender()));
        individualIdentity.setResidenceStatus(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(this.index).getResidenceStatus()));
        individualIdentity.setRegion(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(this.index).getRegion()));
        individualIdentity.setProvince(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(this.index).getProvince()));
        individualIdentity.setCity(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(this.index).getCity()));
        individualIdentity.setZone(createValueDTO(platformLanguageCode, store.getScenarioData().getPersona().getPersons().get(this.index).getZone()));
        individualIdentity.setPostalCode(store.getScenarioData().getPersona().getPersons().get(this.index).getPostalCode());
        individualIdentity.setPhone(store.getScenarioData().getPersona().getPersons().get(this.index).getPhone());
        individualIdentity.setReferenceIdentityNumber(store.getScenarioData().getPersona().getPersons().get(this.index).getReferenceIdentityNumber());
        individualIdentity.setIdSchemaVersion(1.0);
//        individualIdentity.setUin(new BigInteger(store.personaData.getPersona().getPersons().get(0).getUin()));
        //TODO Add proof documents in identity, add individual biometrics in identity, add postalcode accordingly, idschemaversion fin identity (1.0)
//        individualIdentity.setIndividualBiometrics();

        registrationDTO.getDemographicDTO().getDemographicInfoDTO().setIdentity(individualIdentity);
        this.store.setRegistrationDto(registrationDTO);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Utils.auditLog.info(mapper.writeValueAsString(registrationDTO));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ValuesDTO> createValueDTO(String platformLanguageCode, String value){
        ArrayList<ValuesDTO> values = new ArrayList<ValuesDTO>();
        ValuesDTO valuesDTO = new ValuesDTO();
        valuesDTO.setLanguage(platformLanguageCode);
        valuesDTO.setValue(value);
        values.add(valuesDTO);
        return values;
    }
}