package io.mosip.ivv.regprocessor.methods;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class GetTransactionInfo extends BaseStep implements StepInterface {

    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    public RequestDataDTO prepare(){
        String url = "/registrationprocessor/" + System.getProperty("ivv.prereg.version") + "/registrationtransaction/search/"+store.getCurrentPerson().getRegistrationId();
        return new RequestDataDTO(url, null);
    }

    public ResponseDataDTO call(RequestDataDTO data){
        RestAssured.baseURI = System.getProperty("ivv.mosip.host");
        RestAssured.useRelaxedHTTPSValidation();
        Response responseData =
                (Response) given()
                        .contentType(ContentType.JSON)
                        .cookie("Authorization", this.store.getHttpData().getCookie())
                        .post(data.getUrl());
        this.callRecord = new CallRecord(RestAssured.baseURI+data.getUrl(), "POST", data.getRequest(), responseData);
        return new ResponseDataDTO(responseData.getStatusCode(), responseData.getBody().asString(), responseData.getCookies());
    }

    public void process(ResponseDataDTO res){
        ReadContext ctx = JsonPath.parse(res.getBody());
        try {
            if(ctx.read("$['response']") != null){
                ArrayList<HashMap<String, String>> transactionInfo = ctx.read("$['response'][*]");
                for(HashMap ti: transactionInfo){
                    logInfo("id: "+ti.get("id")+", statusComment: "+ti.get("statusComment"));
                }
            }
        } catch (PathNotFoundException e) {
            e.printStackTrace();
        }
    }

}