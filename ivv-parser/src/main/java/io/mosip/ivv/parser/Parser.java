package io.mosip.ivv.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.parser.Utils.Helper;
import io.mosip.ivv.parser.Utils.StepParser;
import io.mosip.ivv.core.dtos.*;
import org.apache.commons.lang3.EnumUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static io.mosip.ivv.core.utils.Utils.regex;

public class Parser implements ParserInterface {
    private ParserInputDTO inputDTO;
    private static String  DOCUMENT_DATA_PATH = "";
    private String BIOMETRICS_DATA_PATH = "";
    Properties properties = null;

    public Parser(ParserInputDTO input){
        inputDTO = input;
    }

    public ArrayList<Persona> getPersonas() throws RigInternalError {
        JSONParser parser = new JSONParser();
        ArrayList personaData = Utils.csvToList(inputDTO.getPersonaSheet());
        ArrayList documentData = getDocuments();
        ArrayList biometricData = getBiometrics();
        String idObjectSchema = Utils.readFileAsString(inputDTO.getIdObjectSchema());
        ArrayList<Persona> persona_list = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = personaData.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            System.out.println("Parsing Persona: "+data_map.get("personaClass"));
            Persona main = new Persona();

            Person iam = new Person();

            /* persona */
            iam.setId(data_map.get("id"));
            iam.setUserid(data_map.get("userid"));
            iam.setPrimaryLang(data_map.get("primaryLang"));
            iam.setSecondaryLang(data_map.get("secondaryLang"));
            iam.setRegistrationCenterId(data_map.get("registrationCenterId"));

            /* Adding documents */
            iam.setProofOfAddress(getProofDocumentByCategory(ProofDocument.DOCUMENT_CATEGORY.POA, documentData));
            iam.setProofOfBirth(getProofDocumentByCategory(ProofDocument.DOCUMENT_CATEGORY.POB, documentData));
            iam.setProofOfIdentity(getProofDocumentByCategory(ProofDocument.DOCUMENT_CATEGORY.POI, documentData));
            iam.setProofOfRelationship(getProofDocumentByCategory(ProofDocument.DOCUMENT_CATEGORY.POR, documentData));
            iam.setProofOfException(getProofDocumentByCategory(ProofDocument.DOCUMENT_CATEGORY.POEX, documentData));
            iam.setProofOfExemption(getProofDocumentByCategory(ProofDocument.DOCUMENT_CATEGORY.POEM, documentData));

            /* Adding biometrics */
            iam.setFace(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.face, biometricData));
            iam.setLeftEye(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.leftEye, biometricData));
            iam.setRightEye(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.rightEye, biometricData));
            iam.setLeftThumb(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.leftThumb, biometricData));
            iam.setRightThumb(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.rightThumb, biometricData));
            iam.setLeftIndexFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.leftIndex, biometricData));
            iam.setLeftMiddleFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.leftMiddle, biometricData));
            iam.setLeftRingFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.leftRing, biometricData));
            iam.setLeftLittleFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.leftLittle, biometricData));
            iam.setRightIndexFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.rightIndex, biometricData));
            iam.setRightMiddleFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.rightMiddle, biometricData));
            iam.setRightRingFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.rightRing, biometricData));
            iam.setRightLittleFinger(getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE.rightLittle, biometricData));

            JsonNode rootNode = null;
            try {
                rootNode = oMapper.readTree(idObjectSchema);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RigInternalError("Parser - error in id object schema: "+e.getMessage());
            }
            Map<String, Object> idObjectMap = oMapper.convertValue(rootNode.path("properties").path("identity").path("properties"), new TypeReference<Map<String, Object>>(){});
            for (Map.Entry<String, Object> entry : idObjectMap.entrySet()) {
                IDObjectField iof = new IDObjectField();
                String key = entry.getKey();
                JSONObject typeDef = new JSONObject((HashMap)entry.getValue());
                if(key.isEmpty()){
                    continue;
                }
                if(typeDef != null &&
                        (typeDef.get("type") != null && typeDef.get("type").equals("integer")) ||
                        (typeDef.get("type") != null && typeDef.get("type").equals("string")) ||
                        (typeDef.get("$ref") != null && typeDef.get("$ref").equals("#/definitions/simpleType"))){
                    String sheetVal = "";
                    for (Map.Entry<String, String> ent: data_map.entrySet()){
                        if(ent.getKey().contains(key)){
                            if(ent.getKey().contains("^")){
                                iof.setMutate(true);
                            }
                            sheetVal = ent.getValue();
                            break;
                        }
                    }
                    if(typeDef.get("type") != null && typeDef.get("type").equals("integer")){
                        iof.setType(IDObjectField.type.integer);
                        iof.setPrimaryValue(sheetVal);
                    } else if(typeDef.get("type") != null && typeDef.get("type").equals("string")){
                        iof.setType(IDObjectField.type.string);
                        iof.setPrimaryValue(sheetVal);
                    } else if(typeDef.get("$ref") != null && typeDef.get("$ref").equals("#/definitions/simpleType")){
                        iof.setType(IDObjectField.type.simpleType);
                        String[] sheetArr = sheetVal.split("%%");
                        iof.setPrimaryValue(sheetArr[0]);
                        if(sheetArr.length>1 && !iam.getSecondaryLang().isEmpty()){
                            iof.setSecondaryValue(sheetArr[0]);
                        }
                    }
                    iam.getIdObject().put(key, iof);
                }
            }

            if(data_map.get("groupName") == null || data_map.get("groupName").isEmpty()){
                main.setGroupName(data_map.get("groupName"));
                main.setPersonaClass(data_map.get("personaClass"));
                main.addPerson(iam);
                persona_list.add(main);
            }else{
                Boolean group_exist = false;
                for(int i=0; i<persona_list.size();i++) {
                    if(persona_list.get(i).getGroupName().equals(data_map.get("groupName"))){
                        group_exist = true;
                        persona_list.get(i).addPerson(iam);
                    }
                }
                if(!group_exist){
                    main.setGroupName(data_map.get("groupName"));
                    main.setPersonaClass(data_map.get("personaClass"));
                    main.addPerson(iam);
                    persona_list.add(main);
                }
            }
        }
        System.out.println("total personas parsed: "+persona_list.size());
        return persona_list;
    }

    private static ProofDocument getProofDocumentByCategory(ProofDocument.DOCUMENT_CATEGORY cat, ArrayList<ProofDocument> documents){
        for(ProofDocument pd: documents){
            if(pd.getDocCatCode().equals(cat)){
                return pd;
            }
        }
        return null;
    }

    private static BiometricsDTO getBiometricsByCategory(BiometricsDTO.BIOMETRIC_CAPTURE cap, ArrayList<BiometricsDTO> biometrics){
        for(BiometricsDTO bio: biometrics){
            if(bio.getCapture().equals(cap)){
                return bio;
            }
        }
        return null;
    }

    private String getMappedField(JSONObject mapping, String key){
        if(mapping.containsKey(key)){
            return mapping.get(key).toString();
        }
        return null;
    }

    public ArrayList<RegistrationUser> getRCUsers(){
        ArrayList data = Utils.csvToList(inputDTO.getRcSheet());
        ArrayList<RegistrationUser> person_list = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            RegistrationUser iam = new RegistrationUser();
            /* persona definition */
            iam.setRole(PersonaDef.ROLE.valueOf(data_map.get("user_type")));

            /* persona */
            iam.setId(data_map.get("id"));
            iam.setUserId(data_map.get("user_id"));
            iam.setPassword(data_map.get("password"));
            iam.setCenterId(data_map.get("center_id"));
            iam.setMacAddress(data_map.get("mac_address"));
            iam.setNo_Of_User(data_map.get("no_of_user"));
            person_list.add(iam);
        }
        System.out.println("total registration users parsed: "+person_list.size());
        return person_list;
    }

    public ArrayList<Partner> getPartners(){
        ArrayList data = Utils.csvToList(inputDTO.getPartnerSheet());
        ArrayList<Partner> person_list = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            Partner iam = new Partner();
            /* persona definition */
            iam.setRole(PersonaDef.ROLE.valueOf(data_map.get("user_type")));

            /* partner */
            iam.setId(data_map.get("id"));
            iam.setUserId(data_map.get("user_id"));
            iam.setPassword(data_map.get("password"));
            iam.setPartnerId(data_map.get("partner_id"));
            iam.setMispLicenceKey(data_map.get("misp_license_key"));
            person_list.add(iam);
        }
        System.out.println("total partners parsed: "+person_list.size());
        return person_list;
    }

    public ArrayList<Scenario> getScenarios(){
        ArrayList data = Utils.csvToList(inputDTO.getScenarioSheet());
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
        ArrayList data = Utils.csvToList(inputDTO.getDocumentsSheet());
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
            pdoc.setPath(Paths.get(DOCUMENT_DATA_PATH, data_map.get("name")).normalize().toString());
            documents.add(pdoc);
        }
        System.out.println("total documents parsed: "+documents.size());
        return documents;
    }

    public ArrayList<BiometricsDTO> getBiometrics(){
        ArrayList data = Utils.csvToList(inputDTO.getBiometricsSheet());
        ArrayList<BiometricsDTO> biometrics = new ArrayList<>();
        System.out.println("total biometrics found: "+data.size());
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            BiometricsDTO biom = new BiometricsDTO();
            biom.setType(BiometricsDTO.BIOMETRIC_TYPE.valueOf(data_map.get("type")));
            biom.setCapture(BiometricsDTO.BIOMETRIC_CAPTURE.valueOf(data_map.get("capture")));
            biom.setName(data_map.get("name"));
            biom.setThreshold(data_map.get("threshold"));
            biom.setPath(Paths.get(BIOMETRICS_DATA_PATH, data_map.get("name")).normalize().toString());
            biometrics.add(biom);
        }
        System.out.println("total biometrics parsed: "+biometrics.size());
        return biometrics;
    }

    public HashMap<String, String> getGlobals(){
        ArrayList data = Utils.csvToList(inputDTO.getGlobalsSheet());
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
        ArrayList data = Utils.csvToList(inputDTO.getConfigsSheet());
        HashMap<String, String> configs_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            configs_map.put(data_map.get("key"), data_map.get("value"));
        }
        System.out.println("total utils entries parsed: "+configs_map.size());
        return configs_map;
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
