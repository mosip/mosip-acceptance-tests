package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public class SendAuthenticationOTP extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        String uin = store.getCurrentPerson().getUin();

        String authPartnerID = store.getCurrentPartner().getPartnerId();
        String mispLicenseKey = store.getCurrentPartner().getMispLicenceKey();
        JSONObject requestData = new JSONObject();
        requestData.put("id", "mosip.identity.otp");
        requestData.put("version", "1.0");
        requestData.put("requestTime", Utils.getCurrentDateAndTimeForAPI());
        requestData.put("transactionID", "1234567890");
        requestData.put("individualId", uin);
        requestData.put("individualIdType", "UIN");
        requestData.put("otpChannel", new JSONArray() {
            {
                add("EMAIL");
            }
        });

        String url = "/idauthentication/" + System.getProperty("ivv.global.version") + "/otp/" + authPartnerID + "/" + mispLicenseKey;
        return new RequestDataDTO(url, requestData.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        Response responseData = given().relaxedHTTPSValidation()
                .cookie("Authorization", this.store.getHttpData().getCookie())
                .contentType(ContentType.JSON)
                .body(data.getRequest())
                .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){

    }



}