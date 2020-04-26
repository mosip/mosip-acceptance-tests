package io.mosip.ivv.parser.Utils;

import io.mosip.ivv.core.policies.AssertionPolicy;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.parser.exceptions.StepParsingException;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class StepParser {

    public static Scenario.Step parse(String cell){
        String[] errorKeys = new String[]{"Error", "error", "ERROR"};
        ArrayList<Scenario.Step.Assert> asserts = new ArrayList<>();
        ArrayList<Scenario.Step.Error> errors = new ArrayList<>();
        ArrayList<String> parameters = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        Scenario.Step step = new Scenario.Step();
        Pattern pattern = Pattern.compile("\\.");
        String[] str_split = pattern.split(cell);
        for( int i = 0; i < str_split.length; i++) {
            String func = str_split[i];
            if(i==0){
                System.out.println(func);
                String name_variant = Utils.regex("(\\w*)\\(", func);
                String[] nv_split = name_variant.split("\\_");
                if(nv_split.length < 2){
                    throw new StepParsingException("invalid step format, it should be (module_stepName): "+func);
                }
                step.setModule(Scenario.Step.modules.valueOf(nv_split[0]));
                step.setName(nv_split[1]);
                if(nv_split.length>2){
                    step.setVariant(nv_split[2]);
                }else{
                    step.setVariant("DEFAULT");
                }
                String[] param_array = Pattern.compile("," ).split(Utils.regex("\\((.*?)\\)", str_split[i]).replaceAll("\\s+",""));
                for(int z=0; z<param_array.length;z++){
                    if(param_array[z] != null && !param_array[z].isEmpty()){
                        parameters.add(param_array[z]);
                    }
                }
                String[] index_array = Pattern.compile("," ).split(Utils.regex("\\[(.*?)\\]", str_split[i]).replaceAll("\\s+",""));
                for(int z=0; z<index_array.length;z++){
                    try {
                        indexes.add(Integer.parseInt(index_array[z]));
                    }catch (NumberFormatException e){

                    }
                }
            }
            for( int j = 0; j < errorKeys.length; j++) {
                if(func.contains(errorKeys[j])){
                    Scenario.Step.Error er = new Scenario.Step.Error();
                    String ertype = Utils.regex("\\((.*?)\\)", str_split[i]);
                    er.code = ertype;
                    errors.add(er);
                }
            }
        }
        if(asserts.size() == 0){
            Scenario.Step.Assert as = new Scenario.Step.Assert();
            as.type = AssertionPolicy.valueOf("DEFAULT");
            asserts.add(as);
        }
        step.setParameters(parameters);
        step.setAsserts(asserts);
        step.setErrors(errors);
        step.setIndex(indexes);
        return step;
    }


}
