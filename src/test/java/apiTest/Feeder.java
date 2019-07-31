package test.java.apiTest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.helpers.Controller;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.utils.ParserEngine;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import main.java.io.mosip.ivv.utils.*;

public class Feeder {
    private static ExtentReports extent;
    public ExtentTest extentTest;
    public static ArrayList<String> scenariosFilter;

    static {
        scenariosFilter = new ArrayList<>();
    }

    private static String statusEmailSubject;
    private static String statusEmailBodyContent;

    @BeforeSuite
    public void beforeSuite() {
        //Extent Report settings
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(BaseHelper.extentReportFile);
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);

        // Initialize audit logger
        Utils.setupLogger();

        //Scenario's filter for test rig execution
        scenariosFilter.add("Smoke_Test");
        scenariosFilter.add("Positive_Test");
        scenariosFilter.add("Negative_Test");
        scenariosFilter.add("ErrorCodes_Test");

        //Add SendEmail Recipients
        BaseHelper.email_recipients.add("channakeshava.p@technoforte.co.in");
//        BaseHelper.email_recipients.add("krish@mosip.io");
//        BaseHelper.email_recipients.add("yogesh@technoforte.co.in");
//        BaseHelper.email_recipients.add("hemant.vadher@technoforte.co.in");
//        BaseHelper.email_recipients.add("ankit.vaishnav@technoforte.co.in");

        //Run Status email details
        statusEmailSubject = "<Imp> Test Rig: Daily execution status - Dt: " + Utils.getCurrentDateAndTime();
        statusEmailBodyContent = "Hello \n \n Please find the today's Test Rig run status... \n Result files are attached! " +
                "\n \n Thanks & Regards \n Test Rig :) (Auto generated email)";

//        statusEmailSubject =
//                "<Imp> Test Rig: Re-run failure scripts - Daily execution status - Dt: " + Utils.getCurrentDateAndTime();
//        statusEmailBodyContent = "Hello \n \n Please find the today's Test Rig re-run status... \n Result files are " +
//                "attached! " +
//                "\n \n Thanks & Regards \n Test Rig :) (Auto generated email)";
    }

    @AfterSuite
    public void afterSuite() {
        extent.flush();

        //Run Report and log files as a status email attachments
        ArrayList<String> fileAttachments;
        fileAttachments = new ArrayList<>();
        fileAttachments.add(BaseHelper.auditLogFile);
        fileAttachments.add(BaseHelper.extentReportFile);

        //DSL file used for test rig execution - for status email attachment
        String CONFIG_FILE = System.getProperty("user.dir") + "/config.properties";
        Properties properties = UtilsA.getProperties(CONFIG_FILE);
        fileAttachments.add(System.getProperty("user.dir") + properties.getProperty("SCENARIO_SHEET"));
        //Send status email to all recipients with attachments
        SendEmail.sendEmailTestRigStatus(BaseHelper.otpEmail_hostname, BaseHelper.otpEmail_username,
                BaseHelper.otpEmail_password, BaseHelper.email_recipients, statusEmailSubject, statusEmailBodyContent, extentTest,
                fileAttachments);
    }

    @DataProvider(name = "ScenarioDataProvider")
    public static Object[][] dataProvider(ITestContext context) {
    //    String scenarioFilter = context.getCurrentXmlTest().getParameter("scenarioFilter");
    //    boolean stopOnError = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("stopOnError"));
        ParserEngine parser = new ParserEngine();
        ArrayList<Scenario> scenariosToRun = parser.fetch();
        Object[][] dataArray = new Object[scenariosToRun.size()][2];
        for (int i = 0; i < scenariosToRun.size(); i++) {
            dataArray[i][0] = i;
            dataArray[i][1] = scenariosToRun.get(i);
        }
        return dataArray;
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
    }

    @Test(dataProvider = "ScenarioDataProvider")
    public void run(int i, Scenario s) throws SQLException, InterruptedException {
        if (scenariosFilter.contains(s.flags.get(0).trim())) {
            extentTest = extent.createTest("Scenario_" + s.name + ": " + s.description);
            Utils.auditLog.info("");
            Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");
            Utils.auditLog.info(">>> TEST Scenario: " + s.name + "." + s.description);
            Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");

            Controller ctrl = new Controller(s.data, extentTest);
            for (Scenario.Step step : s.steps) {
                extentTest.info("Test Step: " + step.name);
                Utils.auditLog.info("Test Step: " + step.name);
                ctrl.run(step);
            }
        }
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.FAILURE:
                extentTest.fail("Scenario failed");
                break;

            case ITestResult.SKIP:
                extentTest.fail("Scenario skipped");
                break;

            case ITestResult.SUCCESS:
                break;
        }
    }
}