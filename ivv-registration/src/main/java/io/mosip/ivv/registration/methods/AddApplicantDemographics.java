package io.mosip.ivv.registration.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.dtos.PersonaDef;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.demographic.CBEFFFilePropertiesDTO;
import io.mosip.registration.dto.demographic.IndividualIdentity;
import io.mosip.registration.dto.demographic.ValuesDTO;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

public class AddApplicantDemographics extends Step implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError{
        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD) && store.getCurrentIntroducer() == null){
            throw new RigInternalError("Introducer is required to process this step");
        }
    }

    @Override
    public void run() {
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();
        IndividualIdentity individualIdentity = new IndividualIdentity();
        ApplicationContext applicationContext = (ApplicationContext) store.getRegLocalContext();

        String platformLanguageCode = applicationContext.applicationLanguage();
        String localLanguageCode = applicationContext.localLanguage();

        JSONObject identity_json = new JSONObject();
        for (Map.Entry<String, ArrayList<Person.FieldValue>> entry : store.getCurrentPerson().getIdObject().entrySet()) {
            String key = entry.getKey();
            ArrayList<Person.FieldValue> vals = entry.getValue();
        }
        individualIdentity.setEmail(store.getCurrentPerson().getEmail());
        individualIdentity.setAddressLine1(createValueDTO(platformLanguageCode, store.getCurrentPerson().getAddressLine1()));
        individualIdentity.setAddressLine2(createValueDTO(platformLanguageCode, store.getCurrentPerson().getAddressLine2()));
        individualIdentity.setAddressLine3(createValueDTO(platformLanguageCode, store.getCurrentPerson().getAddressLine3()));
        individualIdentity.setFullName(createValueDTO(platformLanguageCode, store.getCurrentPerson().getName()));
        individualIdentity.setDateOfBirth(store.getCurrentPerson().getDateOfBirth());
        individualIdentity.setGender(createValueDTO(platformLanguageCode, store.getCurrentPerson().getGender()));
        individualIdentity.setResidenceStatus(createValueDTO(platformLanguageCode, store.getCurrentPerson().getResidenceStatus()));
        individualIdentity.setRegion(createValueDTO(platformLanguageCode, store.getCurrentPerson().getRegion()));
        individualIdentity.setProvince(createValueDTO(platformLanguageCode, store.getCurrentPerson().getProvince()));
        individualIdentity.setCity(createValueDTO(platformLanguageCode, store.getCurrentPerson().getCity()));
        individualIdentity.setZone(createValueDTO(platformLanguageCode, store.getCurrentPerson().getZone()));
        individualIdentity.setPostalCode(store.getCurrentPerson().getPostalCode());
        individualIdentity.setPhone(store.getCurrentPerson().getPhone());
        individualIdentity.setReferenceIdentityNumber(store.getCurrentPerson().getReferenceIdentityNumber());
        individualIdentity.setIdSchemaVersion(1.0);

        /* Only for Child */
        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD)){
            individualIdentity.setParentOrGuardianName(createValueDTO(platformLanguageCode, store.getCurrentIntroducer().getName()));
            if(store.getCurrentIntroducer().getRegistrationId() != null && store.getCurrentIntroducer().getRegistrationId().length()>0){
                individualIdentity.setParentOrGuardianRID(new BigInteger(store.getCurrentIntroducer().getRegistrationId()));
            }
            if(store.getCurrentIntroducer().getUin() != null && store.getCurrentIntroducer().getUin().length()>0){
                individualIdentity.setParentOrGuardianUIN(new BigInteger(store.getCurrentIntroducer().getUin()));
            }
            CBEFFFilePropertiesDTO cbeffDTO = new CBEFFFilePropertiesDTO();
            cbeffDTO.setFormat("cbeff");
            cbeffDTO.setVersion(1.0);
            cbeffDTO.setValue("introducer_bio_CBEFF");
            individualIdentity.setParentOrGuardianBiometrics(cbeffDTO);

            registrationDTO.getDemographicDTO().setIntroducerRID(store.getCurrentIntroducer().getRegistrationId());
            registrationDTO.getDemographicDTO().setIntroducerUIN(store.getCurrentIntroducer().getUin());
        }

        CBEFFFilePropertiesDTO cbeffDTO = new CBEFFFilePropertiesDTO();
        cbeffDTO.setFormat("cbeff");
        cbeffDTO.setVersion(1.0);
        cbeffDTO.setValue("applicant_bio_CBEFF");
        individualIdentity.setIndividualBiometrics(cbeffDTO);

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