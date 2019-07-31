package test.java.apiTest;

import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.orchestrator.ScenarioReport;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ScenarioRunner {

    private Scenario scenario;
    public ScenarioRunner(Scenario s) {
        this.scenario = s;
        System.out.println(s.name);
    }

    @Test(sequential = true)
    public void run() {
        //TODO write a code to tun a scenario
        System.out.println("run scenario");
        ScenarioReport sc = new ScenarioReport();
        sc.hasError = false;
        sc.scenarioResult = true;
        System.out.println("run scenario end");
    }
}
