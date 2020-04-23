package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.dtos.BiometricsDTO;
import io.mosip.ivv.core.dtos.PersonaDef;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.biometric.BiometricInfoDTO;
import io.mosip.registration.dto.biometric.FaceDetailsDTO;
import io.mosip.registration.dto.biometric.FingerprintDetailsDTO;
import io.mosip.registration.dto.biometric.IrisDetailsDTO;

import java.util.ArrayList;
import java.util.List;

public class AddIntroducerBiometrics extends BaseStep implements StepInterface {

    private int iris_threshold = 80;
    private int slap_threshold = 80;
    private int face_threshold = 80;
    private int thumbs_threshold = 80;

    private enum includes {
        face, leftEye, rightEye, leftThumb, rightThumb, leftIndex, leftMiddle, leftRing, leftLittle,
        rightIndex, rightMiddle, rightRing, rightLittle
    }

    @Override
    public void validateStep() throws RigInternalError {
        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD) && store.getCurrentIntroducer() == null){
            throw new RigInternalError("Introducer is required to process this step");
        }

        if(step.getParameters().size()>0){
            for(String par: step.getParameters()){
                try {
                    includes.valueOf(par);
                } catch (IllegalArgumentException ex) {
                    throw new RigInternalError("DSL error: no exception with this name: "+par);
                }
            }
        } else {
            step.getParameters().add(includes.face.toString());
            step.getParameters().add(includes.leftEye.toString());
            step.getParameters().add(includes.rightEye.toString());
            step.getParameters().add(includes.leftThumb.toString());
            step.getParameters().add(includes.leftIndex.toString());
            step.getParameters().add(includes.leftMiddle.toString());
            step.getParameters().add(includes.leftRing.toString());
            step.getParameters().add(includes.leftLittle.toString());
            step.getParameters().add(includes.rightThumb.toString());
            step.getParameters().add(includes.rightIndex.toString());
            step.getParameters().add(includes.rightMiddle.toString());
            step.getParameters().add(includes.rightRing.toString());
            step.getParameters().add(includes.rightLittle.toString());
        }
    }

    @Override
    public void run() {
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();
        BiometricInfoDTO biometricInfoDTO = registrationDTO.getBiometricDTO().getIntroducerBiometricDTO();
        for(String inc: step.getParameters()){
            logInfo("Adding "+inc);
            switch(includes.valueOf(inc)){

                case face:
                    biometricInfoDTO.setFace(this.getFace());
                    biometricInfoDTO.setExceptionFace(new FaceDetailsDTO());
                    break;

                case leftEye:
                    biometricInfoDTO.setIrisDetailsDTO(this.leftEye(biometricInfoDTO.getIrisDetailsDTO()));
                    break;

                case rightEye:
                    biometricInfoDTO.setIrisDetailsDTO(this.rightEye(biometricInfoDTO.getIrisDetailsDTO()));
                    break;

                case leftThumb:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.leftThumb(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case leftIndex:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.leftIndex(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case leftMiddle:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.leftMiddle(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case leftRing:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.leftRing(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case leftLittle:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.leftLittle(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case rightThumb:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.rightThumb(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case rightIndex:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.rightIndex(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case rightMiddle:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.rightMiddle(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case rightRing:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.rightRing(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;

                case rightLittle:
                    biometricInfoDTO.setFingerprintDetailsDTO(this.rightLittle(biometricInfoDTO.getFingerprintDetailsDTO()));
                    break;
            }
        }

        registrationDTO.getBiometricDTO().setIntroducerBiometricDTO(biometricInfoDTO);
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

    private FaceDetailsDTO getFace(){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getFace();
        FaceDetailsDTO face = new FaceDetailsDTO();
        face.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        face.setPhotographName(RegistrationConstants.APPLICANT_PHOTOGRAPH_NAME);
        face.setFace(Utils.readFileAsByte(bdto.getPath()));
        face.setFaceISO(Utils.readFileAsByte(bdto.getPath()));
        return face;
    }

    private List<IrisDetailsDTO> leftEye(List<IrisDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getLeftEye();
        IrisDetailsDTO leftEye = new IrisDetailsDTO();
        leftEye.setIrisType(bdto.getCapture().name());
        leftEye.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        leftEye.setIrisImageName(bdto.getName());
        leftEye.setIrisIso(Utils.readFileAsByte(bdto.getPath()));
        ls.add(leftEye);
        return ls;
    }

    private List<IrisDetailsDTO> rightEye(List<IrisDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getRightEye();
        IrisDetailsDTO rightEye = new IrisDetailsDTO();
        rightEye.setIrisType(bdto.getCapture().name());
        rightEye.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        rightEye.setIrisImageName(bdto.getName());
        rightEye.setIrisIso(Utils.readFileAsByte(bdto.getPath()));
        ls.add(rightEye);
        return ls;
    }

    private List<FingerprintDetailsDTO> leftThumb(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getLeftThumb();
        FingerprintDetailsDTO leftThumb = new FingerprintDetailsDTO();
        leftThumb.setFingerType(bdto.getCapture().name());
        leftThumb.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        leftThumb.setFingerprintImageName(bdto.getName());
        leftThumb.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        leftThumb.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(leftThumb);
        return ls;
    }

    private List<FingerprintDetailsDTO> rightThumb(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getRightThumb();
        FingerprintDetailsDTO rightThumb = new FingerprintDetailsDTO();
        rightThumb.setFingerType(bdto.getCapture().name());
        rightThumb.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        rightThumb.setFingerprintImageName(bdto.getName());
        rightThumb.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        rightThumb.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(rightThumb);
        return ls;
    }

    private List<FingerprintDetailsDTO> leftIndex(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getLeftIndex();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }

    private List<FingerprintDetailsDTO> leftMiddle(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getLeftMiddle();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }

    private List<FingerprintDetailsDTO> leftRing(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getLeftRing();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }

    private List<FingerprintDetailsDTO> leftLittle(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getLeftLittle();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }

    private List<FingerprintDetailsDTO> rightIndex(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getRightIndex();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }

    private List<FingerprintDetailsDTO> rightMiddle(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getRightMiddle();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightMiddle().getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }

    private List<FingerprintDetailsDTO> rightRing(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getRightRing();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightRing().getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }

    private List<FingerprintDetailsDTO> rightLittle(List<FingerprintDetailsDTO> ls){
        BiometricsDTO bdto = store.getCurrentIntroducer().getBiometrics().getRightLittle();
        FingerprintDetailsDTO fd = new FingerprintDetailsDTO();
        fd.setFingerType(bdto.getCapture().name());
        fd.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        fd.setFingerprintImageName(bdto.getName());
        fd.setFingerPrintISOImage(Utils.readFileAsByte(bdto.getPath()));
        fd.setSegmentedFingerprints(new ArrayList<FingerprintDetailsDTO>());
        ls.add(fd);
        return ls;
    }
}