package main.java.io.mosip.ivv.helpers;

import com.aventstack.extentreports.Status;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.CallRecord;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import com.aventstack.extentreports.ExtentTest;
import main.java.io.mosip.ivv.utils.Utils;

public class Controller extends BaseHelper {
    public static Properties prop;
    public static String CONFIG_FILE_PATH = System.getProperty("user.dir") + "/config.properties";
    public static Response api_response;
    public static Scenario.Data data;
    public static String baseUri = "";
    public static String baseVersion = "";

    public Controller(Scenario.Data dataSet, ExtentTest ex) {
        baseUri = BaseHelper.baseUri;
        baseVersion = BaseHelper.baseVersion;
        data = dataSet;
        calls = new ArrayList<CallRecord>();
        extentTest = ex;

        try {
            prop = new Properties();
            FileInputStream ip = new FileInputStream(CONFIG_FILE_PATH);
            prop.load(ip);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Override
    public void SetData(String json) {
        // initialize data
    }

    public String getCallRecords() {
        StringBuilder listString = new StringBuilder();
        for (CallRecord call : calls) {
            listString.append("Method ").append(call.method).append("\n");
            listString.append("Inputdata ").append(call.input_data).append("\n");
            listString.append("Response ").append(call.response.getBody().print()).append("\n");
            listString.append("Status ").append(call.status).append("\n");
            listString.append("------------------------------------------" + "\n");
        }
        return listString.toString();
    }

    public Controller run(Scenario.Step step) throws SQLException, InterruptedException {
        CallRecord record = null;
        tableAppNameValue = null;
        tableColName = null;

        switch (step.name) {

            /* Logon */
            case "Login":
                otherUserTest = "";
                LoginService.LoginServiceEmailMobile(step);
                break;

            /* Logon to other user account*/
            case "LoginOtherUser":
                otherUserTest = "yes";
                LoginService.LoginServiceOtherUser(step);
                break;

            /* Invalidate Token */
            case "InvalidateToken":
                LoginService.InvalidateToken(step);
                break;

            /* SendOTP */
            case "SendOTP":
                LoginService.sendOTP(step);
                break;

            /* AddApplication */
            case "ValidateOTP":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "AUTHENTICATION_SERVICE";
                tableEventName = "AUTHENTICATION";
                LoginService.validateOTP(step);
                break;

            /* AddApplication */
            case "AddApplication":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DEMOGRAPHY_SERVICE";
                tableEventName = "PERSIST";
                DemographicService.addApplication(step);
                break;

            /* AddApplicationAll */
            case "AddApplicationAll":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DEMOGRAPHY_SERVICE";
                tableEventName = "PERSIST";
                DemographicService.addApplicationAll(step);
                break;

            /* GetApplication */
            case "GetApplication":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DEMOGRAPHY_SERVICE";
                DemographicService.getApplication(step);
                break;

            /* DiscardApplication */
            case "DiscardApplication":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "NoFilter";
                tableEventName = "DELETE";
                DemographicService.deleteApplication(step);
                break;

            /* UpdateDemographics */
            case "UpdateDemographics":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DEMOGRAPHY_SERVICE";
                tableEventName = "UPDATE";
                DemographicService.updateApplication(step);
                break;

            /* AddDocuments */
            case "AddDocuments":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DOCUMENT_SERVICE";
                tableEventName = "UPLOAD";
                DocumentService.addDocument(step);
                break;

            /* AddDocumentsAll */
            case "AddDocumentAll":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DOCUMENT_SERVICE";
                tableEventName = "UPLOAD";
                DocumentService.addDocumentAll(step);
                break;

            /* GetDocument */
            case "GetDocument":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DOCUMENT_SERVICE";
                tableEventName = "RETRIEVE";
                DocumentService.getDocuments(step);
                break;

            /* DeleteDocument */
            case "DeleteDocument":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DOCUMENT_SERVICE";
                tableEventName = "DELETE";
                DocumentService.deleteDocument(step);
                break;

            /* DeleteDocumentsByPreRegID */
            case "DeleteDocumentsByPreRegID":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DOCUMENT_SERVICE";
                tableEventName = "DELETE";
                DocumentService.deleteDocumentsByPreRegID(step);
                break;

            /* CopyDocument */
            case "CopyDocument":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "DOCUMENT_SERVICE";
                tableEventName = "COPY";
                DocumentService.copyDocument(step);
                break;

            /* BookAppointment */
            case "BookAppointment":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "BOOKING_SERVICE";
                tableEventName = "PERSIST";
                BookingService.bookAppointment(step);
                break;

            /* BookAppointmentAll */
            case "BookAppointmentAll":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "BOOKING_SERVICE";
                tableEventName = "PERSIST";
                BookingService.bookAppointmentAll(step);
                break;

            /* ReBookAppointment */
            case "ReBookAppointment":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "BOOKING_SERVICE";
                tableEventName = "PERSIST";
                BookingService.reBookAppointment(step);
                break;

            /* GetAppointment */
            case "GetAppointment":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "BOOKING_SERVICE";
                BookingService.getAppointment(step);
                break;

            /* GetAvailableSlots */
            case "GetAvailableSlots":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "BOOKING_SERVICE";
                tableEventName = "RETRIEVE";
                BookingService.getAvailableSlots(step);
                break;

            /* CancelAppointment */
            case "CancelAppointment":
                tableAppNameValue = "PREREGISTRATION";
                tableColName = "BOOKING_SERVICE";
                tableEventName = "UPDATE";
                BookingService.cancelAppointment(step);
                break;

            case "GenerateAcknowledgement":

                /* GenerateQRCode */
            case "GenerateQRCode":
                NotificationService.generateQRCode(step);
                break;

            case "OtpBasedAuthentication":
                AuthenticationService.otpAuthenticationService(step);
                break;

            case "DemographicBasedAuthentication":
                AuthenticationService.demographicAuthenticationService(step);
                break;

            case "BiometricBasedAuthentication":
                AuthenticationService.biometricAuthenticationService(step);
                break;

            /* Notify */
            case "Notify":
                NotificationService.notify(step);
                break;

            default:
                extentTest.log(Status.WARNING, "Skipping step " + step.name);
                Utils.auditLog.warning("Skipping step " + step.name);
                Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
                break;
        }
        return this;
    }

}