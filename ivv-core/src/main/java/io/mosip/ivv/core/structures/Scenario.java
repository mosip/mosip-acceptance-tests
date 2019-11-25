package io.mosip.ivv.core.structures;

import io.mosip.ivv.core.policies.ErrorPolicy;
import io.mosip.ivv.core.policies.AssertionPolicy;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Scenario {
    private String id = "";
    private String name = "";
    private String description = "";
    private ArrayList<String> tags = new ArrayList();
    private String personaClass, groupName;
    private ArrayList<Step.modules> modules = new ArrayList();

    @Getter
    @Setter
    public static class Step
    {
        public enum modules {
            pr, rc, rp, ia, kr
        }
        private String name = ""; // needs to be passed
        private String variant = "DEFAULT"; // default
        private modules module;
        private ArrayList<Assert> asserts;
        private ArrayList<Error> errors;
        private int AssertionPolicy = 0; // default
        private boolean FailExpected = false; //default
        private ArrayList<String> parameters;
        private ArrayList<Integer> index;

        public static class Error{
            public ErrorPolicy type;
            public ArrayList<String> parameters = new ArrayList<>();
        }

        public static class Assert{
            public AssertionPolicy type;
            public ArrayList<String> parameters = new ArrayList<>();
        }

        public Step(){

        }

        public Step(String name, String variant, ArrayList<Assert> asserts, ArrayList<Error> errors, ArrayList<String> parameters, ArrayList<Integer> index)
        {
            this.name = name;
            this.variant = variant;
            this.asserts = asserts;
            this.errors = errors;
            this.parameters = parameters;
            this.index = index;
        }
    }

    @Getter
    @Setter
    public static class Data
    {
        private String personaClass,tag,groupName;
        private Persona persona;
        private Person operator;
        private Person supervisor;
        private Person user;
    }

    private List<Step> steps = new ArrayList<Step>();
    private Data data = null;
    private boolean continueOnFailure = false; // default
    private boolean isFailureExpected = false; // default

}
