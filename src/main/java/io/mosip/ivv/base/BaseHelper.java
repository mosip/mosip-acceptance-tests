package main.java.io.mosip.ivv.base;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.utils.Utils;
import org.testng.Assert;

import java.sql.SQLException;
import java.util.ArrayList;

import static main.java.io.mosip.ivv.utils.Utils.dbAuditLogVerification;

public abstract class BaseHelper {
    //public static String baseUri = "https://tf-proxy.southeastasia.cloudapp.azure.com";
    public static String baseUri = "https://nginxtf.southeastasia.cloudapp.azure.com";
    public static String baseVersion = "v1";
    public static String weekStartDay = "SATURDAY";
    public static String demographicLangcode = "eng";
    protected static ExtentTest extentTest;
    private static String extentReportFileName = "mosip_reports" + Utils.getCurrentDateAndTime().replace(":", "_") + ".html";
    private static String auditLogFileName = "mosip_auditLogs" + Utils.getCurrentDateAndTime().replace(":", "_") + ".log";
    public static String extentReportFile = System.getProperty("user.dir") + "\\testRun\\reports\\" + extentReportFileName;
    public static String auditLogFile = System.getProperty("user.dir") + "\\testRun\\logs\\" + auditLogFileName;

    //DB Connection - Parameters
    public static String dbAuditLogDBPath = "jdbc:postgresql://psql-mosip.southeastasia.cloudapp.azure.com:5432/mosip_audit";
    public static String dbKernelDBPath = "jdbc:postgresql://psql-mosip.southeastasia.cloudapp.azure.com" +
            ":5432/mosip_kernel";
    public static String dbUser = "TF-user";
    public static String dbPassword = "Techno@123";
    public static String tableColName = null;
    public static String tableAppNameValue = null;
    public static String tableEventName = null;

    //Authorization variables
    public static String authCookies = "";
    public static String tempAuthCookies = "";

    //Email Catch-All Account details - Parameters
    public static String otpEmail_hostname = "outlook.office365.com";
    public static String otpEmail_username = "mosip-test@technoforte.co.in";
    public static String otpEmail_password = "vmfWuq2b1";

    //SendEmail to Other User- Parameters
    public static String otherUserTest = "";
    public static String pre_registration_id_OtherUser = "";
    public static String otpOtherEmail_username = "mosipInvalidEmailAccount@technoforte.co.in";

    //Send Email Recipients
    public static ArrayList<String> email_recipients = new ArrayList<String>();

    public enum login_variations {
        EMAIL, PHONE, POB, POI, POA;
    }

    public enum assertion_policy {
        DONT, DEFAULT, STATUS, API_CALL, AUDIT_LOG, DB_VERIFICATION, COMMUNICATION_SINK, NotSupported, COMM_SINK, BLOCKED, ALL
    }

    public abstract void SetData(String json);

    public static ArrayList<CallRecord> calls = new ArrayList<CallRecord>();

    public static void AddCallRecord(CallRecord record, Response response, ExtentTest extentTest) throws SQLException {
        calls.add(record);

        //DB Audit log verification
        dbAuditLogVerification(response, extentTest);
    }

    public static ReadContext stepErrorMiddleware(Scenario.Step st, CallRecord cr) {
        Boolean api_body_status = false;
        String api_body_status_code = "";
        ReadContext ctx = JsonPath.parse(cr.response.getBody().asString());
        if (ctx.read("$['errors']") != null) {
            try {
                if (!ctx.read("$['errors'][0]['errorCode']").equals(st.error)) {
                    extentTest.log(Status.FAIL, "Error code expected " + st.error + " but found "
                            + ctx.read("$['errors'][0]['errorCode']"));
                    Utils.auditLog.severe("Error code expected " + st.error + " but found "
                            + ctx.read("$['errors'][0]['errorCode']"));
                    defectsReference(st);
                    Assert.assertEquals(ctx.read("$['errors'][0]['errorCode']"), st.error, "error code");
                } else {
                    extentTest.log(Status.PASS, "Expected: " + st.error + ", Actual: " + ctx.read("$['errors" +
                            "'][0]['errorCode']"));
                    Utils.auditLog.info("Expected: " + st.error + ",  Actual: " + ctx.read("$['errors'][0]['errorCode']").toString());
                }
                Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
            } catch (PathNotFoundException pathNotFoundException) {
                if (!ctx.read("$['errors']['errorCode']").equals(st.error)) {
                    extentTest.log(Status.FAIL, "Error code expected " + st.error + " but found " + ctx.read("$['errors']['errorCode']"));
                    Utils.auditLog.severe("Error code expected " + st.error + " but found " + ctx.read("$['errors']['errorCode']"));
                    defectsReference(st);
                    Assert.assertEquals(ctx.read("$['errors']['errorCode']"), st.error, "error code");
                } else {
                    extentTest.log(Status.PASS, "Expected: " + st.error + ",  Actual: " + ctx.read("$['errors']['errorCode']"));
                    Utils.auditLog.fine("Expected: " + st.error + ",  Actual: " + ctx.read("$['errors']['errorCode']"));
                    Assert.assertEquals(ctx.read("$['errors']['errorCode']"), st.error, "error code");
                }
                Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
            }
        } else {
            extentTest.log(Status.FAIL, "Error code expected " + st.error + " but found " + ctx.read("$['errors']"));
            Utils.auditLog.severe("Error code expected " + st.error + " but found " + ctx.read("$['errors']"));
            defectsReference(st);
            Assert.assertEquals(ctx.read("$['errors']"), st.error, "No error code returned");
        }
        return ctx;
    }

    public static void defectsReference(Scenario.Step st) {
        String errorCodeReference = null;

        switch (st.variant) {
            case "RequestedPreRegistrationIdDoesNotBelongToTheUser":
            case "ValidateDocumentExceedingPermittedSize":
                errorCodeReference = "MOS-23891";
                break;
        }

        switch (st.name) {
            case "AddDocuments":
                errorCodeReference = "MOS-27850";
                break;

            case "getDocuments":
                errorCodeReference = "MOS-27851";
                break;

            case "DeleteDocumentsByPreRegID":
                errorCodeReference = "MOS-27882";
                break;
        }

        extentTest.log(Status.FAIL, "Please refer the defect 'https://mosipid.atlassian" +
                ".net/browse/'" + errorCodeReference + " for latest updates");
        Utils.auditLog.severe("Please refer the defect 'https://mosipid.atlassian.net/browse/" + errorCodeReference +
                "for latest updates");
    }
}