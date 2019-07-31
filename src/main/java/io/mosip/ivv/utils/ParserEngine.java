package main.java.io.mosip.ivv.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.io.mosip.ivv.base.BaseHelper;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.base.PersonaDef;
import main.java.io.mosip.ivv.base.ProofDocument;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import org.apache.commons.lang3.builder.ToStringBuilder;
import test.java.apiTest.Feeder;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class ParserEngine {
    private String CONFIG_FILE = System.getProperty("user.dir")+"/config.properties";
    private String PERSONA_SHEET = "";
    private String SCENARIO_SHEET = "";
    private String MACROS_SHEET = "";
    private String DOCUMENTS_SHEET = "";
    public static String DOCUMENT_DATA_PATH = "";
    private ArrayList<Scenario.Data> input_data_list;
    private ArrayList<Scenario> scenario_list;
    private HashMap<String, ArrayList<ProofDocument>> document_list;
    private HashMap<String, String> globals_map;
    Properties properties = null;

    public ParserEngine(){
        properties = UtilsA.getProperties(CONFIG_FILE);
        this.PERSONA_SHEET = System.getProperty("user.dir")+properties.getProperty("PERSONA_SHEET");
        this.SCENARIO_SHEET = System.getProperty("user.dir")+properties.getProperty("SCENARIO_SHEET");
        this.MACROS_SHEET = System.getProperty("user.dir")+properties.getProperty("MACROS_SHEET");
        this.DOCUMENTS_SHEET = System.getProperty("user.dir")+properties.getProperty("DOCUMENTS_SHEET");
        this.DOCUMENT_DATA_PATH = System.getProperty("user.dir")+properties.getProperty("DOCUMENT_DATA_PATH");

        this.document_list = formatDocuments(fetchDocuments());
        this.input_data_list = formatData(fetchData());
        this.scenario_list = formatScenarios(fetchScenarios());
        this.globals_map = formatMacros(fetchMacros());
    }

    public ArrayList<Scenario.Data> getInputData(){
        return input_data_list;
    }

    public ArrayList<Scenario> getScenarioList(){
        return scenario_list;
    }

    public String getInputDataString(){
        String listString = "";
        for (Scenario.Data d : input_data_list)
        {
            listString += "Persona class "+d.persona_class+""+"\n";
            listString += ToStringBuilder.reflectionToString(d) + "\n";
            listString += "Persons: "+ "\n";
            for (Persona p : d.persons) {
                listString += ToStringBuilder.reflectionToString(p) + "\t";
            }
            listString += "\n";
        }
        return listString;
    }

    public String getScenarioString(){
        return scenario_list.toString();
    }

    public String getString(ArrayList<Scenario> scenarios){
        String listString = "";
        for (Scenario s : scenarios)
        {
            listString += "Scenario "+s.name+ "\n";
            listString += ToStringBuilder.reflectionToString(s) + "\n";
            listString += "Steps: "+ "\n";
            for (Scenario.Step st : s.steps) {
                listString += ToStringBuilder.reflectionToString(st) + "\t";
            }
            listString += "\n";
            listString += "Data: "+ "\n";
            listString += ToStringBuilder.reflectionToString(s.data) + "\n";
            listString += "Persona: "+ "\n";
            for (Persona p : s.data.persons) {
                listString += ToStringBuilder.reflectionToString(p) + "\n";
            }
        }
        return listString;
    }

    public ArrayList<Scenario> fetch(){
        ArrayList<Scenario> scenarios = new ArrayList<>();
        for (Scenario scenario : scenario_list){
            Scenario.Data input_data = addInputData(scenario);
            scenario.data = input_data;
            scenario.data.globals = this.globals_map;
            scenarios.add(scenario);
        }
        return scenarios;
    }

    private Scenario.Data addInputData(Scenario scenario){
        Scenario.Data input_data = new Scenario.Data();
        for (Scenario.Data data : input_data_list) {
            if(data.persona_class.equals(scenario.persona_class) && data.group_name.equals(scenario.group_name)){
                input_data.group_name = data.group_name;
                input_data.globals = data.globals;
                input_data.persona_class = data.persona_class;
                input_data.user = data.user;
                if(!scenario.group_name.isEmpty() && data.group_name.equals(scenario.group_name)){
                    input_data.persons = data.persons;
                }else{
                    for(Persona person: data.persons){
                        input_data.persons.add(MutationEngine.mutatePersona(person));
                    }
                }
                return input_data;
            }
        }
        for (Scenario.Data data : input_data_list) {
            if(data.persona_class.equals(scenario.persona_class)){
                input_data.group_name = data.group_name;
                input_data.globals = data.globals;
                input_data.persona_class = data.persona_class;
                input_data.user = data.user;
                if(!scenario.group_name.isEmpty() && data.group_name.equals(scenario.group_name)){
                    input_data.persons = data.persons;
                }else{
                    for(Persona person: data.persons){
                        input_data.persons.add(MutationEngine.mutatePersona(person));
                    }
                }
                return input_data;
            }
        }
        return input_data;
    }

    private ArrayList formatData(ArrayList data){
        //System.out.println("total persons found: "+data.size());
        ArrayList<Scenario.Data> scenario_data_list = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            //System.out.println("Parsing Persona: "+data_map.get("persona_class"));
            Scenario.Data input_data = new Scenario.Data();
            Persona user = new Persona();
            user.userid = data_map.get("email");
            user.otp = data_map.get("otp");
            Persona iam = new Persona();
            /* persona definition */
            iam.personaDef.gender = PersonaDef.GENDER.valueOf(data_map.get("gender"));
            iam.personaDef.name = data_map.get("persona_class");
            iam.personaDef.residence_status = PersonaDef.RESIDENCE_STATUS.valueOf(data_map.get("residence_status"));
            iam.personaDef.age_group = PersonaDef.AGE_GROUP.valueOf("ADULT");
            iam.personaDef.role = PersonaDef.ROLE.valueOf("APPLICANT");
            /* persona */
            iam.name = data_map.get("name");
            iam.preffered_lang = data_map.get("preffered_lang");
            iam.default_lang = data_map.get("default_lang");
            iam.address_line_1 = data_map.get("address_line1");
            iam.address_line_2 = data_map.get("address_line2");
            iam.address_line_3 = data_map.get("address_line3");
            iam.region = data_map.get("region");
            iam.province = data_map.get("province");
            iam.city = data_map.get("city");
            iam.postal_code = data_map.get("postal_code");
            iam.email = data_map.get("email");
            iam.phone = data_map.get("mobile");
            iam.date_of_birth = data_map.get("dob");
            iam.cnie_number = data_map.get("cnie");
            iam.registration_center_id = data_map.get("registration_center_id");
            iam.residence_status=data_map.get("residence_status");
            /* attaching documents */
            iam.documents = attachDocuments(iam.personaDef.residence_status);
            input_data.tag = data_map.get("tag");
            input_data.user = user;
            if(data_map.get("group_name") == null || data_map.get("group_name").isEmpty()){
                input_data.group_name = data_map.get("group_name");
                input_data.persona_class = data_map.get("persona_class");
                input_data.addPerson(iam);
                scenario_data_list.add(input_data);
            }else{
                Boolean group_exist = false;
                for(int i=0; i<scenario_data_list.size();i++) {
                    if(scenario_data_list.get(i).group_name.equals(data_map.get("group_name"))){
                        group_exist = true;
                        scenario_data_list.get(i).addPerson(iam);
                    }
                }
                if(!group_exist){
                    input_data.group_name = data_map.get("group_name");
                    input_data.persona_class = data_map.get("persona_class");
                    input_data.addPerson(iam);
                    scenario_data_list.add(input_data);
                }
            }
        }
        //System.out.println("total persons parsed: "+scenario_data_list.size());
        return scenario_data_list;
    }

    private ArrayList attachDocuments(PersonaDef.RESIDENCE_STATUS rs){
        ArrayList<ProofDocument> documents = new ArrayList<>();
        for (Map.Entry<String, ArrayList<ProofDocument>> entry : this.document_list.entrySet()) {
            ProofDocument pf = RandomizationEngine.pickDocumentRandomly(entry.getValue(), rs);
            if(pf != null){
                documents.add(pf);
            }
        }
        return documents;
    }

    private ArrayList formatScenarios(ArrayList data){
        System.out.println("total scenarios found: "+data.size());
        ArrayList<Scenario> scenario_array = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            Scenario scenario = new Scenario();
            scenario.name = data_map.get("tc_no");
            scenario.description = data_map.get("description");
            scenario.persona_class = data_map.get("persona_class");
            scenario.persona = data_map.get("persona");
            scenario.group_name = data_map.get("group_name");
            scenario.flags = parseTags(data_map.get("tags"));
            scenario.steps = formatSteps(data_map);
            if (Feeder.scenariosFilter.contains(scenario.flags.get(0).toString().trim())) {
                //System.out.println("Parsing Scenario: "+data_map.get("tc_no"));
                scenario_array.add(scenario);
            }
        }
        //System.out.println("total scenarios parsed: "+scenario_array.size());
        return scenario_array;
    }

    private HashMap<String, String> formatMacros(ArrayList data){
        System.out.println("total macros found: "+data.size());
        HashMap<String, String> macros_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            macros_map.put(data_map.get("key"), data_map.get("value"));
        }
        //System.out.println("total macros parsed: "+macros_map.size());
        return macros_map;
    }

    private HashMap<String, ArrayList<ProofDocument>> formatDocuments(ArrayList data){
        //System.out.println("total documents found: "+data.size());
        HashMap<String, ArrayList<ProofDocument>> document_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            ProofDocument pdoc = new ProofDocument();
            if(document_map.get(data_map.get("doc_cat_code")) == null){
                document_map.put(data_map.get("doc_cat_code"), new ArrayList<>());
            }
            pdoc.doc_cat_code = ProofDocument.DOCUMENT_CATEGORY.valueOf(data_map.get("doc_cat_code"));
            pdoc.doc_type_code = data_map.get("doc_typ_code");
            pdoc.doc_file_format = data_map.get("doc_file_format");
            pdoc.tags = parseTags(data_map.get("tags"));
            pdoc.name = data_map.get("file");
            pdoc.path = DOCUMENT_DATA_PATH+data_map.get("file");
            File f = new File(pdoc.path);
            if(f.exists() && !f.isDirectory()) {
                document_map.get(data_map.get("doc_cat_code")).add(pdoc);
            }
        }
        //System.out.println("total documents parsed: "+document_map.size());
        return document_map;
    }

    private ArrayList fetchData(){
        return UtilsA.csvToList(PERSONA_SHEET);
    }

    private ArrayList fetchScenarios(){
        return UtilsA.csvToList(SCENARIO_SHEET);
    }

    private ArrayList fetchDocuments(){
        return UtilsA.csvToList(DOCUMENTS_SHEET);
    }

    private ArrayList fetchMacros(){
        return UtilsA.csvToList(MACROS_SHEET);
    }

    private ArrayList<Scenario.Step> formatSteps(HashMap<String, String> data_map){
        ArrayList<Scenario.Step> steps = new ArrayList<Scenario.Step>();
        for (HashMap.Entry<String, String> entry : data_map.entrySet())
        {
            boolean isMatching = entry.getKey().contains("field");
            if(isMatching && entry.getValue() != null && !entry.getValue().isEmpty()){
                if(entry.getValue() != null && !entry.getValue().equals("")) {
                    steps.add(parseStep(entry.getValue()));
                }
            }
        }
        return steps;
    }

    private Scenario.Step parseStep(String data){
        String[] assertKeys = new String[]{"Assert", "assert", "ASSERT"};
        String[] errorKeys = new String[]{"Error", "error", "ERROR"};
        ArrayList<BaseHelper.assertion_policy> asserts = new ArrayList<>();
        ArrayList<String> parameters = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        Scenario.Step step = new Scenario.Step();
        Pattern pattern = Pattern.compile("\\.");
        String[] str_split = pattern.split(data);
        for( int i = 0; i < str_split.length; i++) {
            if(i==0){
                String name_variant = UtilsA.regex("(\\w*)\\(", str_split[i]);
                String[] nv_split = name_variant.split("\\_");
                step.name = nv_split[0];
                if(nv_split.length>1){
                    step.variant = nv_split[1];
                }else{
                    step.variant = "DEFAULT";
                }
                String[] param_array = Pattern.compile("," ).split(UtilsA.regex("\\((.*?)\\)", str_split[i]).replaceAll("\\s+",""));
                for(int z=0; z<param_array.length;z++){
                    parameters.add(param_array[z]);
                }
                String[] index_array = Pattern.compile("," ).split(UtilsA.regex("\\[(.*?)\\]", str_split[i]).replaceAll("\\s+",""));
                for(int z=0; z<index_array.length;z++){
                    try {
                        indexes.add(Integer.parseInt(index_array[z]));
                    }catch (NumberFormatException e){

                    }

                }
            }
            for( int j = 0; j < assertKeys.length; j++) {
                if(str_split[i].contains(assertKeys[j])){
                    asserts.add(BaseHelper.assertion_policy.valueOf(UtilsA.regex("\\((.*?)\\)", str_split[i])));
                }
            }

            for( int j = 0; j < errorKeys.length; j++) {
                if(str_split[i].contains(errorKeys[j]) && !UtilsA.regex("\\((.*?)\\)", str_split[i]).isEmpty()){
                    step.error = UtilsA.regex("\\((.*?)\\)", str_split[i]);
                }
            }
        }
        if(asserts.size() == 0){
            asserts.add(BaseHelper.assertion_policy.valueOf("DEFAULT"));
        }
        step.parameters = parameters;
        step.asserts = asserts;
        step.index = indexes;
        return step;
    }

    private ArrayList<String> parseTags(String tags){
        ArrayList<String> tag = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\," );
        String[] split = pattern.split(tags);
        for( int i = 0; i < split.length; i++) {
            tag.add(split[i].trim());
        }
        return tag;
    }

    public void debug(){
        String str = "loginwithemail(1, 2)";
        Pattern pattern = Pattern.compile("\\.");
        String[] str_split = pattern.split(str);
//        System.out.println(str_split[0]);
//        System.out.println(UtilsA.regex("(\\w*)\\(", str));
    }
}
