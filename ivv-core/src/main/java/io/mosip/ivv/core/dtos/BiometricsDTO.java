package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BiometricsDTO {

    public enum BIOMETRIC_TYPE {PHOTO, IRIS, FINGER};
    public enum BIOMETRIC_CAPTURE {face, exceptionPhoto, leftThumb, rightThumb, leftEye, rightEye, leftIndex, leftMiddle, leftRing, leftLittle,
        rightIndex, rightMiddle, rightRing, rightLittle};

    private BIOMETRIC_TYPE type = null;
    private BIOMETRIC_CAPTURE capture = BIOMETRIC_CAPTURE.leftThumb;
    private Object object = null;
    private String threshold = "";
    private String name = "";
    private String path = "";
    private byte[] rawImage = null;
    private String base64EncodedImage= "";

}
