package io.mosip.ivv.preregistration.methods;

import static io.restassured.RestAssured.given;

import io.mosip.ivv.core.base.Step;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.preregistration.utils.Helpers;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class InvalidateToken extends Step implements StepInterface {
    @Override
    public void run() {
        String url = "/preregistration/" + System.getProperty("ivv.prereg.version") + "/login/invalidateToken";
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response api_response = given().cookie("Authorization", this.store.getHttpData().getCookie()).post(url);

        this.callRecord = new CallRecord(RestAssured.baseURI + url, "POST", null, api_response);
        Helpers.logCallRecord(this.callRecord);

        try {
            System.out.println("...sleeping before sending OTP......");
            Thread.sleep(30000);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
