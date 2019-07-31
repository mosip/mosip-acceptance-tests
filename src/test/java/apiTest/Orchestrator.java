package test.java.apiTest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.helpers.Controller;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.sql.SQLException;

public class Orchestrator {
    private Scenario scenario;
    private ExtentTest extentTest;
    private ExtentReports extent;

    public Orchestrator(Scenario s, ExtentReports e) {
        scenario = s;
        extent = e;
    }

    @BeforeMethod
    public void beforeMethod() {
        System.out.println("Scenario name: " + scenario.name);
        System.out.println("Scenario description: " + scenario.description);
        System.out.println("No of steps: " + scenario.steps.size());
        System.out.println(BaseHelper.extentReportFile);
        extentTest = extent.createTest("Scenario: " + this.scenario.name, this.scenario.description);
    }

    @Test(sequential = true)
    private void run() throws SQLException, InterruptedException {
        Controller ctrl = new Controller(this.scenario.data, extentTest);
        for(Scenario.Step step: this.scenario.steps) {
            System.out.println(this.scenario.name+"-"+ step.name);
            extentTest.info("Test Step: " + step.name);
            ctrl.run(step);
            extentTest.log(Status.PASS, "passed");
        }
        System.out.println(ctrl.getCallRecords());
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        System.out.println("Scenario result: " + result.getStatus());
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    }
}
