package io.mosip.ivv.core.utils;

import com.aventstack.extentreports.ExtentTest;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.policies.ErrorPolicy;
import io.mosip.ivv.core.dtos.ExtentLogger;
import io.mosip.ivv.core.dtos.Scenario;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class ErrorMiddleware {

    private ArrayList<Scenario.Step.Error> ers;
    private String rs;
    private ExtentTest extentInstance;
    private ArrayList<ExtentLogger> reports = new ArrayList<>();

    @Getter
    @Setter
    public class MiddlewareResponse {
        /* status: false - failure */
        private Boolean status = true;
        private ArrayList<ExtentLogger> reports = new ArrayList<>();
    }

    public ErrorMiddleware(ArrayList<Scenario.Step.Error> ers, String rs, ExtentTest extentInstance){
        this.ers = ers;
        this.rs = rs;
        this.extentInstance = extentInstance;
    }

    public MiddlewareResponse inject(){
        MiddlewareResponse mr = new MiddlewareResponse();
        for(Scenario.Step.Error er: this.ers){
            Boolean resp = httpErrorCodeMiddleware(er.code);
            if(!resp){
                mr.setStatus(resp);
                return mr;
            }
        }
        return mr;
    }

    public Boolean httpErrorCodeMiddleware(String errorCode) {
        ReadContext ctx = JsonPath.parse(rs);
        if (ctx.read("$['errors']") != null) {
            try {
                if (!ctx.read("$['errors'][0]['errorCode']").equals(errorCode)) {
                    Utils.auditLog.info("Error code expected "+errorCode+" but found "+ctx.read("$['errors'][0]['errorCode']"));
                    extentInstance.info("Error code expected "+errorCode+" but found "+ctx.read("$['errors'][0]['errorCode']"));
                    return false;
                } else {
                    Utils.auditLog.info("Expected: "+errorCode+",  Actual: "+ctx.read("$['errors'][0]['errorCode']").toString());
                    extentInstance.info("Expected: "+errorCode+",  Actual: "+ctx.read("$['errors'][0]['errorCode']").toString());
                    return true;
                }
            } catch (PathNotFoundException pathNotFoundException) {
                if (!ctx.read("$['errors']['errorCode']").equals(errorCode)) {
                    Utils.auditLog.info("Error code expected "+errorCode+" but found "+ctx.read("$['errors']['errorCode']"));
                    extentInstance.info("Error code expected "+errorCode+" but found "+ctx.read("$['errors']['errorCode']"));
                    return false;
                } else {
                    Utils.auditLog.info("Expected: "+errorCode+",  Actual: "+ctx.read("$['errors'['errorCode']").toString());
                    extentInstance.info("Expected: "+errorCode+",  Actual: "+ctx.read("$['errors']['errorCode']").toString());
                    return true;
                }
            }
        } else {
            Utils.auditLog.info("Error code expected "+errorCode+" but no error code returned");
            extentInstance.info( "Error code expected "+errorCode+" but no error code returned");
            return false;
        }
    }

}
