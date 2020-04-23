package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.dtos.PersonaDef;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.demographic.ApplicantDocumentDTO;
import io.mosip.registration.dto.demographic.DocumentDetailsDTO;

import java.util.HashMap;

public class AddApplicantDocuments extends BaseStep implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError {
        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD) && store.getCurrentIntroducer() == null){
            throw new RigInternalError("Introducer is required to process this step");
        }
    }



    @Override
    public void run() {
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();

        ApplicantDocumentDTO applicantDocumentDTO = new ApplicantDocumentDTO();
        applicantDocumentDTO.setDocuments(new HashMap<>());

        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD)){
            applicantDocumentDTO.getDocuments().put("POA", this.getPOA());
            applicantDocumentDTO.getDocuments().put("POI", this.getPOI());
            applicantDocumentDTO.getDocuments().put("POR", this.getPOR());
        } else {
            applicantDocumentDTO.getDocuments().put("POA", this.getPOA());
            applicantDocumentDTO.getDocuments().put("POI", this.getPOI());
        }

        registrationDTO.getDemographicDTO().setApplicantDocumentDTO(applicantDocumentDTO);
        this.store.setRegistrationDto(registrationDTO);
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

    private HashMap<String, DocumentDetailsDTO> getDocuments(){
        HashMap<String, DocumentDetailsDTO> documents = new HashMap<>();
        return documents;
    }

    private DocumentDetailsDTO getPOA(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getCurrentPerson().getProofOfAddress().getPath()));
        doc.setFormat(store.getCurrentPerson().getProofOfAddress().getDocFileFormat());
        doc.setType(store.getCurrentPerson().getProofOfAddress().getDocTypeCode());
        doc.setValue(store.getCurrentPerson().getProofOfAddress().getDocCatCode()+"_"+store.getCurrentPerson().getProofOfAddress().getDocTypeCode());
        return doc;
    }

    private DocumentDetailsDTO getPOI(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getCurrentPerson().getProofOfIdentity().getPath()));
        doc.setFormat(store.getCurrentPerson().getProofOfIdentity().getDocFileFormat());
        doc.setType(store.getCurrentPerson().getProofOfIdentity().getDocTypeCode());
        doc.setValue(store.getCurrentPerson().getProofOfIdentity().getDocCatCode()+"_"+store.getCurrentPerson().getProofOfIdentity().getDocTypeCode());
        return doc;
    }

    private DocumentDetailsDTO getPOR(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getCurrentPerson().getProofOfRelationship().getPath()));
        doc.setFormat(store.getCurrentPerson().getProofOfRelationship().getDocFileFormat());
        doc.setType(store.getCurrentPerson().getProofOfRelationship().getDocTypeCode());
        doc.setValue(store.getCurrentPerson().getProofOfRelationship().getDocCatCode()+"_"+store.getCurrentPerson().getProofOfRelationship().getDocTypeCode());
        return doc;
    }

    private DocumentDetailsDTO getPOB(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getCurrentPerson().getProofOfBirth().getPath()));
        doc.setFormat(store.getCurrentPerson().getProofOfBirth().getDocFileFormat());
        doc.setType(store.getCurrentPerson().getProofOfBirth().getDocTypeCode());
        doc.setValue(store.getCurrentPerson().getProofOfBirth().getDocCatCode()+"_"+store.getCurrentPerson().getProofOfBirth().getDocTypeCode());
        return doc;
    }

    private DocumentDetailsDTO getPOEX(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getCurrentPerson().getProofOfException().getPath()));
        doc.setFormat(store.getCurrentPerson().getProofOfException().getDocFileFormat());
        doc.setType(store.getCurrentPerson().getProofOfException().getDocTypeCode());
        doc.setValue(store.getCurrentPerson().getProofOfException().getDocCatCode()+"_"+store.getCurrentPerson().getProofOfException().getDocTypeCode());
        return doc;
    }

    private DocumentDetailsDTO getPOEM(){
        DocumentDetailsDTO doc = new DocumentDetailsDTO();
        doc.setDocument(Utils.readFileAsByte(store.getCurrentPerson().getProofOfExemption().getPath()));
        doc.setFormat(store.getCurrentPerson().getProofOfExemption().getDocFileFormat());
        doc.setType(store.getCurrentPerson().getProofOfExemption().getDocTypeCode());
        doc.setValue(store.getCurrentPerson().getProofOfExemption().getPath());
        return doc;
    }
}
