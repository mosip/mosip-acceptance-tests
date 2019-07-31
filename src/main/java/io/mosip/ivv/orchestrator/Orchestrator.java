package main.java.io.mosip.ivv.orchestrator;

import main.java.io.mosip.ivv.base.BaseHelperData;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.feeder.Feeder;
import main.java.io.mosip.ivv.helpers.PreregistrationData;
import main.java.io.mosip.ivv.orchestrator.Scenario.Step;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import java.util.ArrayList;
import java.util.List;


public class Orchestrator {

	public void runScenarios(String scenarioFilter, boolean stopOnError)
	{
		List<ScenarioReport> scenarioReports = new ArrayList<ScenarioReport>();
		List<Scenario> scenariosToRun = fetchScenarios(scenarioFilter);

		for(Scenario scenario: scenariosToRun)
		{
			ScenarioReport report = executeScenario(scenario);
			scenarioReports.add(report);
			if(stopOnError && report.hasError)
			{
				break;
			}
		}
	}

	public List getScenarios(String scenarioFilter){
        ArrayList<Scenario> scenariosToRun = new ArrayList<Scenario>();
		Scenario test_scenario = new Scenario();
		test_scenario.name = "PR12";
		test_scenario.description = "Create Controller data for an user";

		// step 1
		Step step1 = new Step();
		step1.name = "addApplication";
		step1.variant = "";
		// step 2
		Step step2 = new Step();
		step1.name = "getApplication";
		step1.variant = "";
		test_scenario.steps.add(step1);
		test_scenario.steps.add(step2);

		// data setup
		PreregistrationData data = new PreregistrationData();
		// login user
		data.user = new Persona();
		data.user.userid = "av@gmail.com";
		data.user.phone = "7738710559";
		data.user.otp = "2345";
		// persona
		Persona person1 = new Persona();
		person1.date_of_birth = "1990/01/01";
		person1.address_line_1 = "2345";
		person1.address_line_2 = "2345";
		person1.address_line_3 = "2345";
		person1.region = "AP";
		person1.province = "AP";
		person1.city = "Hyderabad";
		person1.name = "Aryan";
		person1.postal_code = "123456";
		person1.phone = "7738710559";
		person1.email = "a@gmail.com";
		person1.cnie_number = "6789545678904";
		person1.pre_registration_id = "54294019209185";
		data.persons = new ArrayList<>();
		data.persons.add(person1);
		// add data to scenario
		/* Sample scenario creation ends*/
		return scenariosToRun;
	}
	
	private List<Scenario> fetchScenarios(String scenarioFilter)
	{
		Feeder feeder = new Feeder();
		return feeder.getScenarios(scenarioFilter);
	}
	
	private ScenarioReport executeScenario(Scenario scenario)
	{
		/* TODO parser engine, run test via testng factory (testng)
		*  Might have a testng factory annotation (check & confirm)
		*
		* */

		ScenarioReport scenarioReport = new ScenarioReport();
		/* TODO
		* Run the scenario and fill the report
		* */
		return scenarioReport;
	}

}
