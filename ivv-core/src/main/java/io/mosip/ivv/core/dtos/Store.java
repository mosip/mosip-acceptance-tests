package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

@Getter
@Setter
public class Store {

    @Getter
    @Setter
    public class HTTPDataObject {
        private String cookie = "";
    }

    private HashMap<String, String> globals;
    private HashMap<String, String> configs;
    private Persona persona;
    private ArrayList<RegistrationUser> registrationUsers;
    private ArrayList<Partner> partners;
    private HTTPDataObject httpData = new HTTPDataObject();
    private ApplicationContext regApplicationContext;
    private Object regLocalContext;
    private Object registrationDto;
    private Properties properties;
    private Person currentPerson;
    private Person currentIntroducer;
    private RegistrationUser currentRegistrationUSer;
    private Partner currentPartner;
}
