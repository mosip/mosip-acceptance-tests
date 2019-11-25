package io.mosip.ivv.core.structures;

import io.mosip.ivv.core.structures.Scenario;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

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
    private Scenario.Data scenarioData;
    private HTTPDataObject httpData = new HTTPDataObject();
    private ApplicationContext regApplicationContext;
    private Object regLocalContext;
    private Object registrationDto;
    private Properties properties;
}
