package io.mosip.ivv.orchestrator;

import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;

import java.util.ArrayList;

public class ScenarioRunner {

    private Scenario scenario;
    private ArrayList<ExtentLogger> reports = new ArrayList<ExtentLogger>();

    public ScenarioRunner(Scenario s){
        this.scenario = s;
    }

    public void run(){
        this.before();
        this.execute();
        this.after();
    }

    public ArrayList<ExtentLogger> getScenarioReport(){
        return this.reports;
    }

    private void execute(){
        for(Scenario.Step step: this.scenario.getSteps()){
            switch(step.getModule()+step.getName()){
                case "Login":
                    break;

                default:
                    System.out.println("step "+step.getName()+" not found");
                    break;
            }
        }
    }

    private void before(){

    }

    private void after(){

    }
}
