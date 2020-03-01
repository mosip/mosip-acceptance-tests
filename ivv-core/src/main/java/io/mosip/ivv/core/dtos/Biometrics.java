package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Biometrics {

    public enum BIOMETRIC_TYPE {FACE, IRIS, FINGER};
    public enum BIOMETRIC_CAPTURE {LEFT_THUMB, RIGHT_THUMB, TWO_THUMBS, LEFT_SLAP, RIGHT_SLAP, TEN_FINGERS, LEFT_EYE, RIGHT_EYE, STILL_PHOTO, LIVE_PHOTO};

    private BIOMETRIC_TYPE type = null;
    private BIOMETRIC_CAPTURE capture = BIOMETRIC_CAPTURE.TWO_THUMBS;
    private Object object = null;
    private String name = "";
    private String path = "";
    private byte[] rawImage = null;
    private String base64EncodedImage= "";

}
