package io.mosip.ivv.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.parser.Utils.StepParser;
import io.mosip.ivv.core.structures.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;

public class Parser implements ParserInterface {

    private String PERSONA_SHEET = "";
    private String RCUSER_SHEET = "";
    private String SCENARIO_SHEET = "";
    private String CONFIGS_SHEET = "";
    private String GLOBALS_SHEET = "";
    private String DOCUMENTS_SHEET = "";
    private String BIOMETRICS_SHEET = "";
    private static String  DOCUMENT_DATA_PATH = "";
    private String BIOMETRICS_DATA_PATH = "";
    Properties properties = null;

    public Parser(String USER_DIR, String CONFIG_FILE){
        properties = Utils.getProperties(CONFIG_FILE);
        this.PERSONA_SHEET = USER_DIR+properties.getProperty("ivv.sheet.persona");
        this.RCUSER_SHEET = USER_DIR+properties.getProperty("ivv.sheet.rcpersona");
        this.SCENARIO_SHEET = USER_DIR+properties.getProperty("ivv.sheet.scenario");
        this.CONFIGS_SHEET = USER_DIR+properties.getProperty("ivv.sheet.configs");
        this.GLOBALS_SHEET = USER_DIR+properties.getProperty("ivv.sheet.globals");
        this.DOCUMENTS_SHEET = USER_DIR+properties.getProperty("ivv.sheet.documents");
        this.BIOMETRICS_SHEET = USER_DIR+properties.getProperty("ivv.sheet.biometrics");
        this.DOCUMENT_DATA_PATH = USER_DIR+properties.getProperty("ivv.path.documents");
        this.BIOMETRICS_DATA_PATH = USER_DIR+properties.getProperty("ivv.path.biometrics");
    }

    public ArrayList<Persona> getPersonas(){
        ArrayList data = fetchData();
        ArrayList<Persona> persona_list = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            System.out.println("Parsing Persona: "+data_map.get("persona_class"));
            Persona main = new Persona();

            Person iam = new Person();
            /* persona definition */
            iam.setGender(data_map.get("gender"));
            iam.setResidenceStatus(data_map.get("residence_status"));
            iam.setRole(PersonaDef.ROLE.valueOf("APPLICANT"));

            /* persona */
            iam.setUserid(data_map.get("userid"));
            iam.setName(data_map.get("name"));
            iam.setPreferredLang(data_map.get("preffered_lang"));
            iam.setDefaultLang(data_map.get("default_lang"));
            iam.setAddressLine1(data_map.get("address_line1"));
            iam.setAddressLine2(data_map.get("address_line2"));
            iam.setAddressLine3(data_map.get("address_line3"));
            iam.setRegion(data_map.get("region"));
            iam.setProvince(data_map.get("province"));
            iam.setCity(data_map.get("city"));
            iam.setZone(data_map.get("zone"));
            iam.setPostalCode(data_map.get("postal_code"));
            iam.setEmail(data_map.get("email"));
            iam.setPhone(data_map.get("mobile"));
            iam.setDateOfBirth(data_map.get("dob"));
            iam.setReferenceIdentityNumber(data_map.get("reference_identity_number"));
            iam.setRegistrationCenterId(data_map.get("registration_center_id"));
            iam.setDocuments(getDocuments());

            if(data_map.get("group_name") == null || data_map.get("group_name").isEmpty()){
                main.setGroupName(data_map.get("group_name"));
                main.setPersonaClass(data_map.get("persona_class"));
                main.addPerson(iam);
                persona_list.add(main);
            }else{
                Boolean group_exist = false;
                for(int i=0; i<persona_list.size();i++) {
                    if(persona_list.get(i).getGroupName().equals(data_map.get("group_name"))){
                        group_exist = true;
                        persona_list.get(i).addPerson(iam);
                    }
                }
                if(!group_exist){
                    main.setGroupName(data_map.get("group_name"));
                    main.setPersonaClass(data_map.get("persona_class"));
                    main.addPerson(iam);
                    persona_list.add(main);
                }
            }
        }
        return persona_list;
    }

    public ArrayList<Person> getRCUsers(){
        ArrayList data = fetchRCUsers();
        ArrayList<Person> person_list = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            Person iam = new Person();
            /* persona definition */
            iam.setRole(PersonaDef.ROLE.valueOf(data_map.get("user_type")));

            /* persona */
            iam.setName(data_map.get("name"));
            iam.setUserid(data_map.get("user_id"));
            iam.setPassword(data_map.get("password"));
            iam.setCenter_id(data_map.get("center_id"));
            iam.setMacAddress(data_map.get("mac_address"));
            person_list.add(iam);
        }
        return person_list;
    }

    public ArrayList<Scenario> getScenarios(){
        ArrayList data = fetchScenarios();
        ArrayList<Scenario> scenario_array = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            Scenario scenario = new Scenario();
            scenario.setName(data_map.get("tc_no"));
            scenario.setDescription(data_map.get("description"));
            scenario.setPersonaClass(data_map.get("persona_class"));
            scenario.setGroupName(data_map.get("group_name"));
            scenario.setTags(parseTags(data_map.get("tags")));
            scenario.setSteps(formatSteps(data_map));
            for(Scenario.Step stp: scenario.getSteps()){
                if(!scenario.getModules().contains(stp.getModule())){
                    scenario.getModules().add(stp.getModule());
                }
            }
            scenario_array.add(scenario);
        }
        System.out.println("total scenarios parsed: "+scenario_array.size());
        return scenario_array;
    }

    public ArrayList<ProofDocument> getDocuments(){
        ArrayList data = fetchDocuments();
        ArrayList<ProofDocument> documents = new ArrayList<>();
        System.out.println("total documents found: "+data.size());
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            ProofDocument pdoc = new ProofDocument();
            pdoc.setDocCatCode(ProofDocument.DOCUMENT_CATEGORY.valueOf(data_map.get("doc_cat_code")));
            pdoc.setDocTypeCode(data_map.get("doc_typ_code"));
            pdoc.setDocFileFormat(data_map.get("doc_file_format"));
            pdoc.setTags(parseTags(data_map.get("tags")));
            pdoc.setName(data_map.get("name"));
            pdoc.setPath(DOCUMENT_DATA_PATH+data_map.get("name"));
            documents.add(pdoc);
        }
        System.out.println("total documents parsed: "+documents.size());
        return documents;
    }

    public ArrayList<Biometrics> getBiometrics(){
        ArrayList data = fetchBiometrics();
        ArrayList<Biometrics> biometrics = new ArrayList<>();
        System.out.println("total biometrics found: "+data.size());
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            Biometrics biom = new Biometrics();
            biom.setType(Biometrics.BIOMETRIC_TYPE.valueOf(data_map.get("type")));
            biom.setCapture(Biometrics.BIOMETRIC_CAPTURE.valueOf(data_map.get("capture")));
            biom.setName(data_map.get("name"));
            biom.setPath(BIOMETRICS_DATA_PATH+data_map.get("name"));
            biometrics.add(biom);
        }
        System.out.println("total biometrics parsed: "+biometrics.size());
        return biometrics;
    }

    public HashMap<String, String> getGlobals(){
        ArrayList data = fetchGlobals();
        HashMap<String, String> globals_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            globals_map.put(data_map.get("key"), data_map.get("value"));
        }
        System.out.println("total global entries parsed: "+globals_map.size());
        return globals_map;
    }

    public HashMap<String, String> getConfigs(){
        ArrayList data = fetchConfigs();
        HashMap<String, String> configs_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            configs_map.put(data_map.get("key"), data_map.get("value"));
        }
        System.out.println("total config entries parsed: "+configs_map.size());
        return configs_map;
    }

    private ArrayList fetchData(){
        return Utils.csvToList(PERSONA_SHEET);
    }

    private ArrayList fetchScenarios(){
        return Utils.csvToList(SCENARIO_SHEET);
    }

    private ArrayList fetchDocuments(){
        return Utils.csvToList(DOCUMENTS_SHEET);
    }

    private ArrayList fetchBiometrics(){
        return Utils.csvToList(BIOMETRICS_SHEET);
    }

    private ArrayList fetchConfigs(){
        return Utils.csvToList(CONFIGS_SHEET);
    }

    private ArrayList fetchGlobals(){
        return Utils.csvToList(GLOBALS_SHEET);
    }

    private ArrayList fetchRCUsers(){
        return Utils.csvToList(RCUSER_SHEET);
    }

    private ArrayList<Scenario.Step> formatSteps(HashMap<String, String> data_map){
        ArrayList<Scenario.Step> steps = new ArrayList<Scenario.Step>();
        for (HashMap.Entry<String, String> entry : data_map.entrySet())
        {
            boolean isMatching = entry.getKey().contains("field");
            if(isMatching && entry.getValue() != null && !entry.getValue().isEmpty()){
                if(entry.getValue() != null && !entry.getValue().equals("")) {
                    steps.add(StepParser.parse(entry.getValue()));
                }
            }
        }
        return steps;
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

}
