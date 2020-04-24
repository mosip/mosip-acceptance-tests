package io.mosip.ivv.dg.Utils;

import com.google.gson.Gson;
import io.mosip.ivv.core.dtos.IDObjectField;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.dtos.Persona;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static io.mosip.ivv.core.utils.Utils.regex;

public class MutationEngine {

    private String key = "";

    public MutationEngine(){
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        this.key = RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public Persona mutatePersona(Persona persona){
        Gson gson = new Gson();
        Persona per = gson.fromJson(gson.toJson(persona), Persona.class);
        per.setGroupName("");
        for(int i=0; i<per.getPersons().size();i++){
            per.getPersons().get(i).setUserid(mutateEmail(per.getPersons().get(i).getUserid()));
            per.getPersons().get(i).setPhone(generatePhone());
            per.getPersons().get(i).setIdObject(mutateIDFields(per.getPersons().get(i).getIdObject()));
        }
        return per;
    }

    private HashMap<String, IDObjectField> mutateIDFields(HashMap<String, IDObjectField> idFields){
        Gson gson = new Gson();
        HashMap<String, IDObjectField> newIdFields = new HashMap<String, IDObjectField>();
        for (Map.Entry<String, IDObjectField> entry : idFields.entrySet()) {
            String key = entry.getKey();
            IDObjectField idField = entry.getValue();
            IDObjectField newIdField = gson.fromJson(gson.toJson(idField), IDObjectField.class);
            if(idField.getMutate()) {
                if(!regex("(\\S+@\\S+\\.\\S+)", idField.getPrimaryValue()).isEmpty()){
                    if(!idField.getPrimaryValue().isEmpty()){
                        newIdField.setPrimaryValue(mutateEmail(idField.getPrimaryValue()));
                    }
                    if(!idField.getSecondaryValue().isEmpty()){
                        newIdField.setSecondaryValue(mutateEmail(idField.getSecondaryValue()));
                    }
                } else if(!regex("^(\\d+$)", idField.getPrimaryValue()).isEmpty()) {
                    if(!idField.getPrimaryValue().isEmpty()){
                        newIdField.setPrimaryValue(generatePhone());
                    }
                    if(!idField.getSecondaryValue().isEmpty()){
                        newIdField.setSecondaryValue(generatePhone());
                    }
                } else {
                    if(!idField.getPrimaryValue().isEmpty()){
                        newIdField.setPrimaryValue(mutateCommon(idField.getPrimaryValue()));
                    }
                    if(!idField.getSecondaryValue().isEmpty()){
                        newIdField.setSecondaryValue(mutateCommon(idField.getSecondaryValue()));
                    }
                }
            }
            newIdFields.put(key, newIdField);
        }
        return newIdFields;
    }

    public Person mutateDocumentsOnly(Person person){
        person.getProofOfAddress().setName(mutateDocumentName(person.getProofOfAddress().getName()));
        person.getProofOfBirth().setName(mutateDocumentName(person.getProofOfBirth().getName()));
        person.getProofOfIdentity().setName(mutateDocumentName(person.getProofOfIdentity().getName()));
        person.getProofOfRelationship().setName(mutateDocumentName(person.getProofOfRelationship().getName()));
        person.getProofOfException().setName(mutateDocumentName(person.getProofOfException().getName()));
        person.getProofOfExemption().setName(mutateDocumentName(person.getProofOfExemption().getName()));
        return person;
    }

    private String mutateCommon(String par){
        if(par.isEmpty()){
            return par;
        }
        return par+" "+this.key;
    }

    private String mutateDocumentName(String par){
        if(par == null){
            return null;
        }
        return this.key+"_"+par;
    }

    private String mutateEmail(String par){
        return par.replace("@", this.key+"@").toLowerCase();
    }

    private String generatePhone(){
        Random generator = new Random();
        int first_digit = generator.nextInt((9 - 6) + 1) + 6; //add 1 so there is no 0 to begin
        int second_digit = generator.nextInt(8); //randomize to 8 becuase 0 counts as a number in the generator
        int third_digit = generator.nextInt(8);

        // Sequence two of phone number
        // the plus 100 is so there will always be a 3 digit number
        // randomize to 643 because 0 starts the first placement so if i randomized up to 642 it would only go up yo 641 plus 100
        // and i used 643 so when it adds 100 it will not succeed 742
        int set1 = generator.nextInt(643) + 100;

        //Sequence 3 of numebr
        // add 1000 so there will always be 4 numbers
        //8999 so it wont succed 9999 when the 1000 is added
        int set2 = generator.nextInt(8999) + 1000;

        return first_digit+""+second_digit+""+third_digit+""+set1+""+set2;
    }

    private String mutateAddress(String par){
        if(par.isEmpty()){
            return par;
        }
        return par+" "+this.key;
    }

    private String generatePostalCode(){
        Random generator = new Random();
        int first_digit = generator.nextInt(7) + 1;
        int set1 = generator.nextInt(8999) + 1000;
        return first_digit+""+set1;
    }
}
