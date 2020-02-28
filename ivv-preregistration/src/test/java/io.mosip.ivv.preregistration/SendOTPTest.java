package io.mosip.ivv.preregistration;

import io.mosip.ivv.core.base.StepAPITestInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.Store;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.dg.DataGenerator;
import io.mosip.ivv.preregistration.methods.SendOTP;
import io.mosip.ivv.preregistration.utils.Helpers;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class SendOTPTest implements StepAPITestInterface {

    @Test
    public void prepareRequest() throws RigInternalError {
        DataGenerator dg = new DataGenerator(System.getProperty("user.dir")+"/../ivv-orchestrator", "config.properties", true);
        Store store = new Store();
        store.setConfigs(dg.getConfigs());
        store.setGlobals(dg.getGlobals());
        store.setCurrentPerson(dg.getPerson());
        String expectedRequest = "";
        try {
            /* Changed the json filename with your request file */
            expectedRequest = (String) Helpers.getRequestJson("SendOTPRequest.json");
            /* Changed the json filename with your request file */
        } catch (IOException e) {
            e.printStackTrace();
            throw new RigInternalError(e.getMessage());
        }

        /* Changed this class according to the Step to test*/
        SendOTP sc = new SendOTP();
        /* Changed this class according to the Step to test */

//        sc.setState(store);
//        RequestDataDTO request = sc.prepare();
//        String expectedRequestKeysOnly = Utils.removeValuesFromJson(expectedRequest);
//        String actualRequestKeysOnly = Utils.removeValuesFromJson(request);
//        System.out.println(expectedRequestKeysOnly+"----"+actualRequestKeysOnly);
//        try {
//            JSONAssert.assertEquals(expectedRequestKeysOnly, actualRequestKeysOnly, true);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            throw new RigInternalError(e.getMessage());
//        }
    }

    @Test
    public void processResponse() throws RigInternalError {
        DataGenerator dg = new DataGenerator(System.getProperty("user.dir")+"/../ivv-orchestrator", "config.properties", true);
        Store store = new Store();
        store.setConfigs(dg.getConfigs());
        store.setGlobals(dg.getGlobals());
        store.setCurrentPerson(dg.getPerson());
        String expectedResponse = "";
        try {
            expectedResponse = (String) Helpers.getResponseJson("SendOTPResponse.json");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RigInternalError(e.getMessage());
        }
        SendOTP sc = new SendOTP();
        sc.setState(store);
//        sc.process(200, expectedResponse);
        Assert.assertFalse(sc.hasError);
    }
}
