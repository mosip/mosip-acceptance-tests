package io.mosip.ivv.core.structures;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Person {
    private PersonaDef personaDef = null;
    private String name = "";
    private String userid = "";
    private String password = "";
    private String center_id = "";
    private String phone = "";
    private String email = "";
    private String otp = "";
    private String registrationCenterId = "";

    /* pre-reg store */
    private String preRegistrationId = "";
    private String preRegistrationCenterId = "";
    private String preRegistrationStatusCode = "";
    private BookingSlot slot;
    private BookingSlot prevSlot;

    /* reg store */
    private String registrationId = "";

    /* system info */
    private String macAddress = "";

    // required in create pre-registration api
    private String preferredLang = "";
    private String defaultLang = "";
    private String dateOfBirth = "";
    private String addressLine1 = "";
    private String addressLine2 = "";
    private String addressLine3 = "";
    private String region = "";
    private String province = "";
    private String city = "";
    private String zone = "";
    private String idSchemaVersion = "";
    private String referenceIdentityNumber = "";
    private String postalCode = "";
    private String langCode = "fra";
    private String gender = "";
    private String residenceStatus = "";


    private ArrayList<String> docTypeList;
    private ProofDocument proofOfAddress = null;
    private ProofDocument proofOfBirth = null;
    private ProofDocument proofOfIdentity = null;
    private ProofDocument proofOfRelationship = null;
    private ProofDocument proofOfException = null;
    private ProofDocument proofOfExemption = null;

    private String uin = "";
    private List<String> vids = new ArrayList<String>();
    private boolean hasBiometricException = false;

    private ArrayList<String> bioCaptureList;
    private Biometrics thumbs = null;
    private Biometrics leftSlap = null;
    private Biometrics rightSlap = null;
    private Biometrics leftIris = null;
    private Biometrics rightIris = null;
    private Biometrics face = null;

    //@Deprecated
    public ArrayList<ProofDocument> documents;

    public Person() {
        personaDef = new PersonaDef();
    }

    public void setRole(PersonaDef.ROLE p) {
        this.personaDef.role = p;
    }

    public String getRole() {
        return personaDef.role.toString();
    }
}
