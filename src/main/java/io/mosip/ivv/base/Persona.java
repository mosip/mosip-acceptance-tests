package main.java.io.mosip.ivv.base;

import java.util.ArrayList;

public class Persona {
    private String id;
    private String version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PersonaDef personaDef = null;
    public String name = "";
    public String userid = "";
    public String phone = "";
    public String email = "";
    public String otp = "";
    public String registration_center_id = "";
    public String status_code = "";
    public String err = "";
    public String errorCode = "";
    public String status = "";
    public String message = "";
    public String deletedBy = "";
    public String deletedDateTime = "";
    public String response = "";

    /* pre-reg store */
    public String pre_registration_id = "";
    public String pre_registration_status_code = "";
    public CoreStructures.Slot slot = new CoreStructures.Slot();

    // required in create pre-registration api
    public String preffered_lang = "";
    public String default_lang = "";
    public String date_of_birth = "";
    public String address_line_1 = "";
    public String address_line_2 = "";
    public String address_line_3 = "";
    public String region = "";
    public String province = "";
    public String city = "";
    public String cnie_number = "";
    public String postal_code = "";
    public String lang_code = "fra";
    public String residence_status = "";

    public Persona() {
        personaDef = new PersonaDef();
    }

    public String getGender() {
        return personaDef.gender.toString();
    }

    public String getResidenceStatus() {
        return personaDef.residence_status.toString();
    }

    public ArrayList<ProofDocument> documents;
}
