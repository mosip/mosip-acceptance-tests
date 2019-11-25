package io.mosip.ivv.dg;

import io.mosip.ivv.core.structures.*;
import io.mosip.ivv.core.structures.ProofDocument.DOCUMENT_CATEGORY;
import io.mosip.ivv.core.structures.Biometrics.BIOMETRIC_CAPTURE;
import io.mosip.ivv.dg.Utils.MutationEngine;
import io.mosip.ivv.dg.exceptions.PersonaNotFoundException;
import io.mosip.ivv.dg.exceptions.UserNotFoundException;
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
    private ArrayList<Person> reg_users = null;
    private ArrayList<ProofDocument> documents = null;
    private ArrayList<Biometrics> biometrics = null;

    public DataGenerator(String USER_DIR, String CONFIG_FILE) {
        this.user_dir = USER_DIR;
        this.config_file = CONFIG_FILE;
        generate();
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

    private void generate() {
        Parser parser = new Parser(this.user_dir, this.config_file);
        this.documents = parser.getDocuments();
        this.biometrics = parser.getBiometrics();
        this.globals = parser.getGlobals();
        this.configs = parser.getConfigs();
        this.personas = addDataToPersonas(parser.getPersonas());
        this.reg_users = addDataToRCUsers(parser.getRCUsers());
        ArrayList<Scenario> scenarios = parser.getScenarios();
        this.generatedScenarios = new ArrayList<Scenario>();
        for (Scenario scenario : scenarios){
            scenario.setData(new Scenario.Data());
            scenario.getData().setPersona(addPersonaData(scenario, this.personas));
            scenario.getData().setOperator(addOperatorData(this.reg_users));
            scenario.getData().setSupervisor(addSupervisorData(this.reg_users));
            this.generatedScenarios.add(scenario);
        }
    }

    private ArrayList<Persona> addDataToPersonas(ArrayList<Persona> personas){
        for(int i=0;i<personas.size();i++){
            for(int j=0;j<personas.get(i).getPersons().size();j++){
                personas.get(i).getPersons().set(j, attachDocuments(personas.get(i).getPersons().get(j)));
                personas.get(i).getPersons().set(j, attachBiometrics(personas.get(i).getPersons().get(j)));
            }
        }
        return personas;
    }

    private ArrayList<Person> addDataToRCUsers(ArrayList<Person> persons){
        for(int i=0;i<persons.size();i++){
            persons.set(i, attachBiometrics(persons.get(i)));
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

    private Person attachBiometrics(Person person){
        person.setFace(getBiometricsByCategory(BIOMETRIC_CAPTURE.STILL_PHOTO));
        person.setLeftIris(getBiometricsByCategory(BIOMETRIC_CAPTURE.LEFT_EYE));
        person.setRightIris(getBiometricsByCategory(BIOMETRIC_CAPTURE.RIGHT_EYE));
        person.setThumbs(getBiometricsByCategory(BIOMETRIC_CAPTURE.TWO_THUMBS));
        person.setLeftSlap(getBiometricsByCategory(BIOMETRIC_CAPTURE.LEFT_SLAP));
        person.setRightSlap(getBiometricsByCategory(BIOMETRIC_CAPTURE.RIGHT_SLAP));
        return person;
    }

    private ProofDocument getProofDocumentByCategory(DOCUMENT_CATEGORY cat){
        for(ProofDocument pd: this.documents){
            if(pd.getDocCatCode().equals(cat)){
                return pd;
            }
        }
        return null;
    }

    private Biometrics getBiometricsByCategory(BIOMETRIC_CAPTURE cap){
        for(Biometrics bio: this.biometrics){
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

    private Person addOperatorData(ArrayList<Person> reg_users){
        for(Person person: reg_users){
            if(person.getPersonaDef().role.equals(PersonaDef.ROLE.OPERATOR)){
                return person;
            }
        }
        throw new UserNotFoundException("Operator not found");
    }

    private Person addSupervisorData(ArrayList<Person> reg_users){
        for(Person person: reg_users){
            if(person.getPersonaDef().role.equals(PersonaDef.ROLE.SUPERVISOR)){
                return person;
            }
        }
        throw new UserNotFoundException("Supervisor not found");
    }

}
