package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.demographic.*;

import java.util.ArrayList;
import java.util.HashMap;

public class AddApplicantDocuments extends Step implements StepInterface {

    @Override
    public void run(Scenario.Step step) {
        this.index = Utils.getPersonIndex(step);
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();

        /* Add documents in identity object */
        IndividualIdentity individualIdentity = (IndividualIdentity) registrationDTO.getDemographicDTO().getDemographicInfoDTO().getIdentity();
        individualIdentity.setProofOfAddress(this.getPOA());
        individualIdentity.setProofOfDateOfBirth(this.getPOB());
        individualIdentity.setProofOfIdentity(this.getPOI());
        individualIdentity.setProofOfRelationship(this.getPOR());

        registrationDTO.getDemographicDTO().getDemographicInfoDTO().setIdentity(individualIdentity);

        ApplicantDocumentDTO applicantDocumentDTO = new ApplicantDocumentDTO();
        applicantDocumentDTO.setDocuments(new HashMap<>());
        //TODO add applicant's documents to registration dto
        applicantDocumentDTO.getDocuments().put("POA", this.getPOA());
        applicantDocumentDTO.getDocuments().put("POI", this.getPOI());
        applicantDocumentDTO.getDocuments().put("POR", this.getPOR());
        applicantDocumentDTO.getDocuments().put("POB", this.getPOB());
        registrationDTO.getDemographicDTO().setApplicantDocumentDTO(applicantDocumentDTO);
        this.store.setRegistrationDto(registrationDTO);
    }

    private HashMap<String, DocumentDetailsDTO> getDocuments(){
        HashMap<String, DocumentDetailsDTO> documents = new HashMap<>();
        return documents;
    }

    private DocumentDetailsDTO getPOA(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfAddress().getPath()));
        doc.setFormat(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfAddress().getDocFileFormat());
        doc.setType(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfAddress().getDocTypeCode());
        doc.setValue(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfAddress().getPath());
        return doc;
    }

    private DocumentDetailsDTO getPOI(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfIdentity().getPath()));
        doc.setFormat(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfIdentity().getDocFileFormat());
        doc.setType(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfIdentity().getDocTypeCode());
        doc.setValue(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfIdentity().getPath());
        return doc;
    }

    private DocumentDetailsDTO getPOR(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfRelationship().getPath()));
        doc.setFormat(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfRelationship().getDocFileFormat());
        doc.setType(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfRelationship().getDocTypeCode());
        doc.setValue(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfRelationship().getPath());
        return doc;
    }

    private DocumentDetailsDTO getPOB(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfBirth().getPath()));
        doc.setFormat(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfBirth().getDocFileFormat());
        doc.setType(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfBirth().getDocTypeCode());
        doc.setValue(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfBirth().getPath());
        return doc;
    }

    private DocumentDetailsDTO getPOEX(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfException().getPath()));
        doc.setFormat(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfException().getDocFileFormat());
        doc.setType(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfException().getDocTypeCode());
        doc.setValue(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfException().getPath());
        return doc;
    }

    private DocumentDetailsDTO getPOEM(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfExemption().getPath()));
        doc.setFormat(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfExemption().getDocFileFormat());
        doc.setType(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfExemption().getDocTypeCode());
        doc.setValue(store.getScenarioData().getPersona().getPersons().get(this.index).getProofOfExemption().getPath());
        return doc;
    }
}
