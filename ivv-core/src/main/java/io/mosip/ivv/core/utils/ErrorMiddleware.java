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

    private Scenario.Step st;
    private Response rs;
    private ExtentTest extentInstance;
    private ArrayList<ExtentLogger> reports = new ArrayList<>();

    @Getter
    @Setter
    public class MiddlewareResponse {
        /* status: false - failure */
        private Boolean status = true;
        private ArrayList<ExtentLogger> reports = new ArrayList<>();
    }

    public ErrorMiddleware(Scenario.Step st, Response rs, ExtentTest extentInstance){
        this.st = st;
        this.rs = rs;
        this.extentInstance = extentInstance;
    }

    public MiddlewareResponse inject(){
        MiddlewareResponse mr = new MiddlewareResponse();
        for(Scenario.Step.Error er: this.st.getErrors()){
            if(er.equals(ErrorPolicy.HTTP_ERROR_CODE)){
                Boolean resp = httpErrorCodeMiddleware(er);
                if(!resp){
                    mr.setStatus(resp);
                    return mr;
                }
            }
        }
        return mr;
    }

    public Boolean httpErrorCodeMiddleware(Scenario.Step.Error er) {
        String errorCode = er.parameters.get(0);
        ReadContext ctx = JsonPath.parse(rs.getBody().asString());
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
