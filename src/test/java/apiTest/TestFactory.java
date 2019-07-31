package test.java.apiTest;

import main.java.io.mosip.ivv.orchestrator.Orchestrator;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.orchestrator.ScenarioReport;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

import java.util.ArrayList;
import java.util.List;

public class TestFactory {

    @Factory
    public Object[] createInstances() {
        Object[] result = new Object[10];
        for (int i = 0; i < 10; i++) {
            result[i] = new TestRunner();
        }
        return result;
    }

}
