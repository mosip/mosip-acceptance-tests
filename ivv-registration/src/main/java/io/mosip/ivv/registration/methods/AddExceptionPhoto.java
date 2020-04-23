package io.mosip.ivv.registration.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.BiometricsDTO;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.biometric.BiometricInfoDTO;
import io.mosip.registration.dto.biometric.FaceDetailsDTO;

public class AddExceptionPhoto extends BaseStep implements StepInterface {

    private int face_threshold = 80;



    @Override
    public void run() {
        RegistrationDTO registrationDTO = (RegistrationDTO) this.store.getRegistrationDto();
        BiometricInfoDTO biometricInfoDTO = registrationDTO.getBiometricDTO().getApplicantBiometricDTO();
        biometricInfoDTO.setExceptionFace(getExceptionPhoto());
        registrationDTO.getBiometricDTO().setApplicantBiometricDTO(biometricInfoDTO);
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

    private FaceDetailsDTO getExceptionPhoto(){
        BiometricsDTO bdto = store.getCurrentPerson().getBiometrics().getFace();
        FaceDetailsDTO face = new FaceDetailsDTO();
        face.setQualityScore(Double.parseDouble(bdto.getThreshold()));
        face.setPhotographName(RegistrationConstants.EXCEPTION_PHOTOGRAPH_NAME);
        face.setFace(Utils.readFileAsByte(bdto.getPath()));
        face.setFaceISO(Utils.readFileAsByte(bdto.getPath()));
        return face;
    }
}
