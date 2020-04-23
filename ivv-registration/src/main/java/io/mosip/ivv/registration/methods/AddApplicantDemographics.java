package io.mosip.ivv.registration.methods;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.IDObjectField;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
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

public class AddApplicantDemographics extends BaseStep implements StepInterface {

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
        for (Map.Entry<String, IDObjectField> entry : store.getCurrentPerson().getIdObject().entrySet()) {
            String key = entry.getKey();
            IDObjectField vals = entry.getValue();
        }
        individualIdentity.setEmail(store.getCurrentPerson().getIdObject().get("email").getPrimaryValue());
        individualIdentity.setAddressLine1(createValueDTO(store.getCurrentPerson().getIdObject().get("addressLine1")));
        individualIdentity.setAddressLine2(createValueDTO(store.getCurrentPerson().getIdObject().get("addressLine1")));
        individualIdentity.setAddressLine3(createValueDTO(store.getCurrentPerson().getIdObject().get("addressLine1")));
        individualIdentity.setFullName(createValueDTO(store.getCurrentPerson().getIdObject().get("addressLine1")));
        individualIdentity.setDateOfBirth(store.getCurrentPerson().getIdObject().get("dateOfBirth").getPrimaryValue());
        individualIdentity.setGender(createValueDTO(store.getCurrentPerson().getIdObject().get("gender")));
        individualIdentity.setResidenceStatus(createValueDTO(store.getCurrentPerson().getIdObject().get("residenceStatus")));
        individualIdentity.setRegion(createValueDTO(store.getCurrentPerson().getIdObject().get("region")));
        individualIdentity.setProvince(createValueDTO(store.getCurrentPerson().getIdObject().get("province")));
        individualIdentity.setCity(createValueDTO(store.getCurrentPerson().getIdObject().get("city")));
        individualIdentity.setZone(createValueDTO(store.getCurrentPerson().getIdObject().get("zone")));
        individualIdentity.setPostalCode(store.getCurrentPerson().getPostalCode());
        individualIdentity.setPhone(store.getCurrentPerson().getIdObject().get("phone").getPrimaryValue());
        individualIdentity.setReferenceIdentityNumber(store.getCurrentPerson().getReferenceIdentityNumber());
        individualIdentity.setIdSchemaVersion(1.0);

        /* Only for Child */
        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD)){
            individualIdentity.setParentOrGuardianName(createValueDTO(store.getCurrentIntroducer().getIdObject().get("fullName")));
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

    private ArrayList<ValuesDTO> createValueDTO(IDObjectField idObjectField){
        if(idObjectField == null){
            return new ArrayList<>();
        }
        ArrayList<ValuesDTO> values = new ArrayList<ValuesDTO>();
        ValuesDTO valuesDTO1 = new ValuesDTO();
        valuesDTO1.setLanguage(idObjectField.getPrimaryValue());
        valuesDTO1.setValue(store.getCurrentPerson().getPrimaryLang());
        if(!store.getCurrentPerson().getSecondaryLang().isEmpty()){
            ValuesDTO valuesDTO2 = new ValuesDTO();
            valuesDTO2.setLanguage(idObjectField.getPrimaryValue());
            valuesDTO2.setValue(store.getCurrentPerson().getSecondaryLang());
        }
        values.add(valuesDTO1);
        return values;
    }
}