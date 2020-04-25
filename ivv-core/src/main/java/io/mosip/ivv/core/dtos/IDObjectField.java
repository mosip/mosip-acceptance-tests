package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class IDObjectField {

    public enum type {
        string, integer, simpleType
    }

    private Boolean prereg = false;
    private Boolean regclient = false;
    private Boolean ida = false;
    private Boolean mutate = false;
    private IDObjectField.type type;
    private String primaryValue = "";
    private String secondaryValue = "";
}
