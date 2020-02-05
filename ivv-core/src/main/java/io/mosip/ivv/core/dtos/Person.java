package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class Person extends PersonaDef {
    /* required */
    private HashMap<String, ArrayList<FieldValue>> idObject = new HashMap<String, ArrayList<FieldValue>>();
    private FieldValue id;
    private FieldValue userid;
    private FieldValue password;
    private FieldValue phone;
    private FieldValue otp;
    private FieldValue registrationCenterId;
    private FieldValue primaryLang;
    private FieldValue secondaryLang;

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

    /* IDA fields */
    private String staticToken = "";
    private String authenticationOTP = "";
    private JSONObject authenticationJSON = new JSONObject();
    private ArrayList<String> authParams = new ArrayList<String>();

    // required in create pre-registration api
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

    //@Deprecated
    public ArrayList<ProofDocument> documents;

    @Getter
    @Setter
    public static class FieldValue {
        private Boolean mutate = false;
        private String value;
        private String lang = "en";
    }
}
