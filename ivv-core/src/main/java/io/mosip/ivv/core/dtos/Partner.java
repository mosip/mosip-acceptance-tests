package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Partner extends PersonaDef {
    private String id = "";
    private String userId = "";
    private String password = "";
    private String partnerId = "";
    private String mispLicenceKey = "";
}
