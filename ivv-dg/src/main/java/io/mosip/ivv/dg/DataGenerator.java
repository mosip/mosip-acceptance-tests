package io.mosip.ivv.dg;

import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.dtos.BiometricsDTO.BIOMETRIC_CAPTURE;
import io.mosip.ivv.core.dtos.ProofDocument.DOCUMENT_CATEGORY;
import io.mosip.ivv.dg.Utils.MutationEngine;
import io.mosip.ivv.dg.exceptions.PersonaNotFoundException;
import io.mosip.ivv.parser.Parser;

import java.util.ArrayList;
import java.util.HashMap;

public class DataGenerator implements DataGeneratorInterface {

    private String user_dir = "";
    private String config_file = "";
    private ArrayList<Scenario> generatedScenarios= null;
    private HashMap<String, String> globals = null;
    private HashMap<String, String> configs = null;
    private ArrayList<Persona> personas = null;
    private ArrayList<RegistrationUser> reg_users = null;
    private ArrayList<Partner> partners = null;
    private ArrayList<ProofDocument> documents = null;
    private ArrayList<BiometricsDTO> biometrics = null;

    public DataGenerator(String USER_DIR, String CONFIG_FILE) {
        this.user_dir = USER_DIR;
        this.config_file = CONFIG_FILE;
        generate();
    }

    public DataGenerator(String USER_DIR, String CONFIG_FILE, Boolean manually) {
        this.user_dir = USER_DIR;
        this.config_file = CONFIG_FILE;
    }


    @Override
    public void saveScenariosToDb() {

    }

    @Override
    public ArrayList<Scenario> getScenarios() {
        return this.generatedScenarios;
    }

    @Override
    public HashMap<String, String> getConfigs() {
        return this.configs;
    }

    @Override
    public HashMap<String, String> getGlobals() {
        return this.globals;
    }

    public Person getPerson() {
        Parser parser = new Parser(this.user_dir, this.config_file);
        this.documents = parser.getDocuments();
        this.biometrics = parser.getBiometrics();
        ArrayList<Persona> ps = addDataToPersonas(parser.getPersonas());
        if(ps.size()>0 && ps.get(0).getPersons().size()>0){
            return ps.get(0).getPersons().get(0);
        }
        return null;
    }

    public RegistrationUser getRegistrationUser() {
        Parser parser = new Parser(this.user_dir, this.config_file);
        this.documents = parser.getDocuments();
        this.biometrics = parser.getBiometrics();
        ArrayList<RegistrationUser> ps = addDataToRCUsers(parser.getRCUsers());
        if(ps.size()>0){
            return ps.get(0);
        }
        return null;
    }

    public Partner getPartner() {
        Parser parser = new Parser(this.user_dir, this.config_file);
        ArrayList<Partner> ps = addDataToPartners(parser.getPartners());
        if(ps.size()>0){
            return ps.get(0);
        }
        return null;
    }

    private void generate() {
        Parser parser = new Parser(this.user_dir, this.config_file);
        this.documents = parser.getDocuments();
        this.biometrics = parser.getBiometrics();
        this.globals = parser.getGlobals();
        this.configs = parser.getConfigs();
        this.personas = addDataToPersonas(parser.getPersonas());
        this.reg_users = addDataToRCUsers(parser.getRCUsers());
        this.partners = addDataToPartners(parser.getPartners());
        ArrayList<Scenario> scenarios = parser.getScenarios();
        this.generatedScenarios = new ArrayList<Scenario>();
        for (Scenario scenario : scenarios){
            scenario.setData(new Scenario.Data());
            scenario.getData().setPersona(addPersonaData(scenario, this.personas));
            scenario.getData().setRegistrationUsers(this.reg_users);
            scenario.getData().setPartners(partners);
            this.generatedScenarios.add(scenario);
        }
    }

    private ArrayList<Persona> addDataToPersonas(ArrayList<Persona> personas){
        for(int i=0;i<personas.size();i++){
            for(int j=0;j<personas.get(i).getPersons().size();j++){
                personas.get(i).getPersons().set(j, attachDocuments(personas.get(i).getPersons().get(j)));
                personas.get(i).getPersons().get(j).setBiometrics(attachBiometrics());
            }
        }
        return personas;
    }

    private ArrayList<RegistrationUser> addDataToRCUsers(ArrayList<RegistrationUser> persons){
        for(int i=0;i<persons.size();i++){
            persons.get(i).setBiometrics(attachBiometrics());
        }
        return persons;
    }

    private ArrayList<Partner> addDataToPartners(ArrayList<Partner> persons){
        for(int i=0;i<persons.size();i++){
            persons.get(i).setBiometrics(attachBiometrics());
        }
        return persons;
    }



    private Person attachDocuments(Person person){
        person.setProofOfAddress(getProofDocumentByCategory(DOCUMENT_CATEGORY.POA));
        person.setProofOfBirth(getProofDocumentByCategory(DOCUMENT_CATEGORY.POB));
        person.setProofOfIdentity(getProofDocumentByCategory(DOCUMENT_CATEGORY.POI));
        person.setProofOfRelationship(getProofDocumentByCategory(DOCUMENT_CATEGORY.POR));
        person.setProofOfException(getProofDocumentByCategory(DOCUMENT_CATEGORY.POEX));
        person.setProofOfExemption(getProofDocumentByCategory(DOCUMENT_CATEGORY.POEM));
        return person;
    }

    private PersonaDef.Biometrics attachBiometrics(){
        PersonaDef.Biometrics biometrics = new PersonaDef.Biometrics();
        biometrics.setExceptionPhoto(getBiometricsByCategory(BIOMETRIC_CAPTURE.exceptionPhoto));
        biometrics.setFace(getBiometricsByCategory(BIOMETRIC_CAPTURE.face));
        biometrics.setLeftEye(getBiometricsByCategory(BIOMETRIC_CAPTURE.leftEye));
        biometrics.setRightEye(getBiometricsByCategory(BIOMETRIC_CAPTURE.rightEye));
        biometrics.setLeftThumb(getBiometricsByCategory(BIOMETRIC_CAPTURE.leftThumb));
        biometrics.setRightThumb(getBiometricsByCategory(BIOMETRIC_CAPTURE.rightThumb));
        biometrics.setLeftIndex(getBiometricsByCategory(BIOMETRIC_CAPTURE.leftIndex));
        biometrics.setLeftMiddle(getBiometricsByCategory(BIOMETRIC_CAPTURE.leftMiddle));
        biometrics.setLeftRing(getBiometricsByCategory(BIOMETRIC_CAPTURE.leftRing));
        biometrics.setLeftLittle(getBiometricsByCategory(BIOMETRIC_CAPTURE.leftLittle));
        biometrics.setRightIndex(getBiometricsByCategory(BIOMETRIC_CAPTURE.rightIndex));
        biometrics.setRightMiddle(getBiometricsByCategory(BIOMETRIC_CAPTURE.rightMiddle));
        biometrics.setRightRing(getBiometricsByCategory(BIOMETRIC_CAPTURE.rightRing));
        biometrics.setRightLittle(getBiometricsByCategory(BIOMETRIC_CAPTURE.rightLittle));
        return biometrics;
    }

    private ProofDocument getProofDocumentByCategory(DOCUMENT_CATEGORY cat){
        for(ProofDocument pd: this.documents){
            if(pd.getDocCatCode().equals(cat)){
                return pd;
            }
        }
        return null;
    }

    private BiometricsDTO getBiometricsByCategory(BIOMETRIC_CAPTURE cap){
        for(BiometricsDTO bio: this.biometrics){
            if(bio.getCapture().equals(cap)){
                return bio;
            }
        }
        return null;
    }

    private Persona addPersonaData(Scenario scenario, ArrayList<Persona> personas){
        Persona per = new Persona();
        for (Persona persona : personas) {
            if(scenario.getGroupName() != null && !scenario.getGroupName().isEmpty()
                    && persona.getPersonaClass().equals(scenario.getPersonaClass())
                    && persona.getGroupName().equals(scenario.getGroupName())){
                return persona;
            }
        }
        for (Persona persona : personas) {
            if(persona.getPersonaClass().equals(scenario.getPersonaClass())){
                return new MutationEngine().mutatePersona(persona);
            }
        }
        throw new PersonaNotFoundException("Persona not found");
    }

}
