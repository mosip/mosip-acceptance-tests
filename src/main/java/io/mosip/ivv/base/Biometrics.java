package main.java.io.mosip.ivv.base;

public class Biometrics {

	public enum BIOMETRIC_TYPE {FACE, IRIS, FINGERPRINT};
	public enum BIOMETRIC_CAPTURE {LEFT_THUMB, RIGHT_THUMB, TWO_THUMBS, LEFT_SLAP, RIGHT_SLAP, TEN_FINGERS, LEFT_EYE, RIGHT_EYE, STILL_PHOTO, LIVE_PHOTO};
	
	public BIOMETRIC_TYPE biometric_type = BIOMETRIC_TYPE.FINGERPRINT;
	public BIOMETRIC_CAPTURE biometric_capture = BIOMETRIC_CAPTURE.RIGHT_THUMB;
	public byte[] raw_image = null;
	public String base64_encoded_image= "";
	
	
}
