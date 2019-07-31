package main.java.io.mosip.ivv.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.orchestrator.Scenario;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.Random;

public class MutationEngine {

    public static Persona mutatePersona(Persona person){
        Gson gson = new Gson();
        Persona per = gson.fromJson(gson.toJson(person), Persona.class);
        per.name = mutateName(person.name);
        per.email = mutateEmail(person.email);
        per.phone = generatePhone();
        per.address_line_1 = mutateAddress(person.address_line_1);
        per.address_line_2 = mutateAddress(person.address_line_2);
        per.address_line_3 = mutateAddress(person.address_line_3);
        per.postal_code = generatePostalCode(person.postal_code);
        return per;
    }

    private static String mutateName(String par){
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        String rs = RandomStringUtils.random(length, useLetters, useNumbers);
        System.out.println(rs);
        return par+" "+rs;
    }

    private static String mutateEmail(String par){
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        String rs = RandomStringUtils.random(length, useLetters, useNumbers);
        return par.replace("@", rs+"@");
    }

    private static String generatePhone(){
        Random generator = new Random();
        int first_digit = generator.nextInt((9 - 6) + 1) + 6;
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

    private static String mutateAddress(String par){
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        String rs = RandomStringUtils.random(length, useLetters, useNumbers);
        return par+" "+rs;
    }

    private static String generatePostalCode(String par){
        Random generator = new Random();
        int first_digit = generator.nextInt(7) + 1;
        int set1 = generator.nextInt(8999) + 1000;
//        return first_digit+""+set1;
        return par;
    }
}