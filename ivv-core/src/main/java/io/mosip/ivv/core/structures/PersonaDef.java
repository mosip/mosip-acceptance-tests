package io.mosip.ivv.core.structures;

public class PersonaDef {
    public enum AGE_GROUP {
        ADULT, CHILD, INFANT
    }
    public enum ROLE {
        APPLICANT, OPERATOR, SUPERVISOR, ADMINISTRATOR, ADJUDICATOR
    }

    public AGE_GROUP ageGroup = AGE_GROUP.ADULT;
    public ROLE role = ROLE.APPLICANT;
}
