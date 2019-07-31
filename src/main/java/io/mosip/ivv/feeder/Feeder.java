package main.java.io.mosip.ivv.feeder;


import main.java.io.mosip.ivv.orchestrator.Scenario;

import java.util.ArrayList;
import java.util.List;

public class Feeder {

    public List<Scenario> getScenarios(String filter)
    {
        List<String> filterParams = getFilterParameters(filter);
        /* TODO
        * query the scenario repository and get scenarios
        * */
        List<String> scenarioDefs = null;
        scenarioDefs = getScenariosFromRepository(filterParams);
        List<Scenario> scenarios = new ArrayList<Scenario>();
        for(String scenarioDef : scenarioDefs)
        {
            Scenario scenario = new Scenario();
            /* TODO
            * Set Scenarios Parameters
            * Get data for the scenario and set it
            * */
            scenarios.add(scenario);
        }
        return scenarios;
    }

    private List<String> getScenariosFromRepository(List<String> filterParams)
    {
        /* TODO
        * Fetch scenarios from repository using filter parameters
        * */
        return null;
    }

    private List<String> getFilterParameters(String filter)
    {
        /* TODO
        * parse the filter string into a list of parameters that are used to control the execution
        * */
        return new ArrayList<String>();
    }

}
