package io.mosip.ivv.preregistration;

import io.mosip.ivv.core.base.StepAPITestInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.dg.DataGenerator;
import io.mosip.ivv.parser.Parser;
import io.mosip.ivv.preregistration.methods.SendOTP;
import io.mosip.ivv.preregistration.utils.Helpers;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import static io.restassured.RestAssured.given;

public class SendOTPTest implements StepAPITestInterface {

    private Parser parser;

    @Before
    public void init() {
        String configFile = Paths.get(System.getProperty("user.dir"),"..", "ivv-orchestrator","config.properties").normalize().toString();
        Properties properties = Utils.getProperties(configFile);
        ParserInputDTO parserInputDTO = new ParserInputDTO();
        parserInputDTO.setDocumentsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.documents.folder")).normalize().toString());
        parserInputDTO.setBiometricsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.biometrics.folder")).normalize().toString());
        parserInputDTO.setPersonaSheet(Paths.get(configFile, "..", properties.getProperty("ivv.sheet.persona")).normalize().toString());
        parserInputDTO.setIdObjectSchema(Paths.get(configFile, "..", properties.getProperty("ivv.path.idobject")).normalize().toString());
        parserInputDTO.setDocumentsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.sheet.documents")).normalize().toString());
        parserInputDTO.setBiometricsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.sheet.biometrics")).normalize().toString());
        parserInputDTO.setGlobalsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.sheet.globals")).normalize().toString());
        parserInputDTO.setConfigsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.sheet.configs")).normalize().toString());
        parser = new Parser(parserInputDTO);
    }

    @Test
    public void prepareRequest() throws RigInternalError {
        Store store = new Store();
        store.setConfigs(parser.getConfigs());
        store.setGlobals(parser.getGlobals());
        store.setCurrentPerson(parser.getPersonas().get(0).getPersons().get(0));
        String expectedRequest = "";
        try {
            /* Change the json filename with your request file */
            expectedRequest = (String) Helpers.getRequestJson("SendOTPRequest.json");
            /* Change the json filename with your request file */
        } catch (IOException e) {
            e.printStackTrace();
            throw new RigInternalError(e.getMessage());
        }

        /* Change this class according to the Step to test*/
        SendOTP sc = new SendOTP();
        /* Change this class according to the Step to test */

        sc.setState(store);
        RequestDataDTO request = sc.prepare();
        String expectedRequestKeysOnly = Utils.removeValuesFromJson(expectedRequest);
        String actualRequestKeysOnly = Utils.removeValuesFromJson(request.getRequest());
        System.out.println(expectedRequestKeysOnly+"----"+actualRequestKeysOnly);
        try {
            JSONAssert.assertEquals(expectedRequestKeysOnly, actualRequestKeysOnly, true);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RigInternalError(e.getMessage());
        }
    }

    @Test
    public void processResponse() throws RigInternalError {
        Store store = new Store();
        store.setConfigs(parser.getConfigs());
        store.setGlobals(parser.getGlobals());
        store.setCurrentPerson(parser.getPersonas().get(0).getPersons().get(0));
        String response = "";
        try {
            /* Change the json filename with your response template file */
            response = (String) Helpers.getResponseJson("SendOTPResponse.json");
            /* Change the json filename with your response template file */
        } catch (IOException e) {
            e.printStackTrace();
            throw new RigInternalError(e.getMessage());
        }
        /* Change this class according to the Step to test*/
        SendOTP sc = new SendOTP();
        /* Change this class according to the Step to test*/
        sc.setState(store);
        sc.process(new ResponseDataDTO(200, response, new HashMap<>()));
        Assert.assertFalse(sc.hasError);
    }
}
