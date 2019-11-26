package io.mosip.ivv.core.structures;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class PersonaDef {
    public enum AGE_GROUP {
        ADULT, CHILD, INFANT
    }
    public enum ROLE {
        APPLICANT, OPERATOR, SUPERVISOR, ADMINISTRATOR, ADJUDICATOR, PARTNER
    }

    private AGE_GROUP ageGroup = AGE_GROUP.ADULT;
    private ROLE role = ROLE.APPLICANT;

    private boolean hasBiometricException = false;
    private ArrayList<String> bioCaptureList;

    private Biometrics biometrics;

    @Getter
    @Setter
    public static class Biometrics {
        private BiometricsDTO leftThumb = null;
        private BiometricsDTO rightThumb = null;
        private BiometricsDTO leftEye = null;
        private BiometricsDTO rightEye = null;
        private BiometricsDTO face = null;
        private BiometricsDTO exceptionPhoto = null;
        private BiometricsDTO leftIndex = null;
        private BiometricsDTO leftMiddle = null;
        private BiometricsDTO leftRing = null;
        private BiometricsDTO leftLittle = null;
        private BiometricsDTO rightIndex = null;
        private BiometricsDTO rightMiddle = null;
        private BiometricsDTO rightRing = null;
        private BiometricsDTO rightLittle = null;
    }
}
