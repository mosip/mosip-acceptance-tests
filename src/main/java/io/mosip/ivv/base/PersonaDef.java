package main.java.io.mosip.ivv.base;

public class PersonaDef {
    public enum AGE_GROUP {
        ADULT, CHILD, INFANT
    }
    public enum GENDER {
        MALE, FEMALE, OTHER
    }
    public enum RESIDENCE_STATUS {
        CITIZEN, FOREIGNER, OVERSEAS_RESIDENT, InvalidPRID, InvalidSize
    }
    public enum ROLE {
        APPLICANT, OPERATOR, SUPERVISOR, ADMINISTRATOR, ADJUDICATOR
    }

    public AGE_GROUP age_group = AGE_GROUP.ADULT;
    public GENDER gender = GENDER.MALE;
    public RESIDENCE_STATUS residence_status = RESIDENCE_STATUS.CITIZEN;
    public ROLE role = ROLE.APPLICANT;
    public String name = "";

}
