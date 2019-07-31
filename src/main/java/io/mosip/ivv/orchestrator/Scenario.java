package main.java.io.mosip.ivv.orchestrator;

import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.BaseHelperData;
import main.java.io.mosip.ivv.base.Persona;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scenario {
    public String name = "";
    public String description = "";
    public ArrayList<String> flags = new ArrayList<String>();
    public String persona_class,persona,tag,group_name;

    public static class Step
    {
        public String name = ""; // needs to be passed
        public String variant = "DEFAULT"; // default
        public ArrayList<BaseHelper.assertion_policy> asserts = new ArrayList<BaseHelper.assertion_policy>();
        public String error;
        public int AssertionPolicy = 0; // default
        public boolean FailExpected = false; //default
        public ArrayList<String> parameters = new ArrayList<String>();
        public ArrayList<Integer> index = new ArrayList<Integer>();

        public static class Error{
            public String type = "";
            public ArrayList<String> parameters = new ArrayList<String>();
        }

        public static class Assert{
            public String type = "";
            public ArrayList<String> parameters = new ArrayList<String>();
        }

        public Step()
        {

        }

        public Step(String name, String variant, ArrayList<BaseHelper.assertion_policy> asserts, String error, ArrayList<String> parameters, ArrayList<Integer> index)
        {
            this.name = name;
            this.variant = variant;
            this.asserts = asserts;
            this.error = error;
            this.parameters = parameters;
            this.index = index;
        }
    }

    public static class Data
    {
        public String persona_class,tag,group_name;
        public ArrayList<Persona> persons = new ArrayList<Persona>();
        public Persona user;
        public HashMap<String, String> globals;
        public void addPerson(Persona person){
            this.persons.add(person);
        }
    }

    public List<Step> steps = new ArrayList<Step>();
    public Data data = null;
    public boolean continueOnFailure = false; // default
    public boolean isFailureExpected = false; // default
}
