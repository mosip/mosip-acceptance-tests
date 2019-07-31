package main.java.io.mosip.ivv.base;

import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.utils.Utils;
import org.testng.Assert;

public class CallRecord {
    public String method = "";
    public String url = "";
    public String input_data = "";
    public Response response = null;
    public String status = "";
    public int statusCode;

    public CallRecord(String url, String method, String input_data, Response response, String status, Scenario.Step step) {
        this.url = url;
        this.method = method;
        this.input_data = input_data;
        this.response = response;
        this.status = status;
        this.statusCode = response.getStatusCode();

        Utils.auditLog.info("# [method][url]: " + "["+method+"]["+url+"]");
        Utils.auditLog.info("# INPUT Data: " + input_data);
        Utils.auditLog.info("# RETURN Response: " + response.asString().toString());
        Utils.auditLog.info("STATUS Code: " + status);


        if (Integer.parseInt(status) == 200 && Integer.parseInt(status) < 300 ) {
            BaseHelper.extentTest.log(Status.PASS, "Method[" + method + "] executed with the status " + response.getStatusCode() + "(" + Utils.assertResponseStatus(status) + ")");
            Utils.auditLog.fine("HTTP Status: " + Utils.assertResponseStatus(status));
        } else if (Integer.parseInt(status) >= 300 && Integer.parseInt(status) < 400 ) {
            BaseHelper.extentTest.log(Status.WARNING, "Method[" + method + "] executed with the status " + response.getStatusCode() + "(" + Utils.assertResponseStatus(status) + ");");
            Utils.auditLog.warning( "HTTP Status: " + Utils.assertResponseStatus(status));
        } else if (Integer.parseInt(status) >= 400 && Integer.parseInt(status) < 599 ) {
            if (Integer.parseInt(status) == 413) {
                BaseHelper.extentTest.log(Status.INFO,
                        "Method[" + method + "] executed with the status " + response.getStatusCode() + "(" + Utils.assertResponseStatus(status) + ")");
                Utils.auditLog.info("HTTP Status: " + Utils.assertResponseStatus(status));
            } else {
                BaseHelper.extentTest.log(Status.ERROR, "Method[" + method + "] executed with the status " + response.getStatusCode() + "(" + Utils.assertResponseStatus(status) + ")");
                Utils.auditLog.severe("HTTP Status: " + Utils.assertResponseStatus(status));
            }
        }
        Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");

        if (step.name.contains("AddDocuments") && (response.asString().toString().contains("413 Request Entity Too Large")))  {
            BaseHelper.defectsReference(step);
            Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
        }

        if (step.error != null && !step.error.isEmpty()) {
        } else {
            ReadContext ctx = null;
            ctx = JsonPath.parse(response.getBody().asString());
            if (ctx.read("$['errors']") != null) {
                try {
                    BaseHelper.extentTest.log(Status.INFO, "Error Code: '" + ctx.read("$['errors'][0]['errorCode']")
                            + "', message: '" + ctx.read("$['errors'][0]['message']") + "'");
                    Utils.auditLog.info("Error Code: '" + ctx.read("$['errors'][0]['errorCode']")
                            + "', message: '" + ctx.read("$['errors'][0]['message']") + "'");
                    Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
//            Assert.assertEquals((Boolean) (ctx.read("$['errors']") != null), (Boolean) true, "Return response " +
//                    "errorCode");
                } catch (PathNotFoundException pathNotFoundException) {
                    BaseHelper.extentTest.log(Status.INFO, "Error Code: '" + ctx.read("$['errors']['errorCode']")
                            + "', message: '" + ctx.read("$['errors']['message']") + "'");
                    Utils.auditLog.info("Error Code: '" + ctx.read("$['errors']['errorCode']")
                            + "', message: '" + ctx.read("$['errors']['message']") + "'");
                    Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
//            Assert.assertEquals((Boolean) (ctx.read("$['errors']") != null), (Boolean) true, "Return response " +
//                    "errorCode");
                }
            }
        }

    }

    public CallRecord(){

    }

    public String getMethod(){
        return method;
    }

    public String getInputData(){
        return input_data;
    }

    public Response getResponse(){
        return response;
    }

    public String getStatus(){
        return status;
    }

    public int getStatusCode() {
            return statusCode;
    }
}
