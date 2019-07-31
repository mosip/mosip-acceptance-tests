package main.java.io.mosip.ivv.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.orchestrator.Scenario;

public class UtilsA {

    public static Properties getProperties(String path) {
        Properties prop = new Properties();
        try {
            FileInputStream file = new FileInputStream(path);
            prop.load(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    public static HashMap getMap(String path) {
        Object obj = new Object();
        JSONParser jsonParser = new JSONParser();
        try {
            FileReader file = new FileReader(path);
            obj = jsonParser.parse(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return (HashMap) obj;
    }

    public static String readFileAsString(String path){
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String csvToJson(String path){
        String json = "";
        File input = new File(path);
        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
        CsvMapper csvMapper = new CsvMapper();
        try {
            // Read data from CSV file
            List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readAll);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static ArrayList csvToList(String path){
        ArrayList list = new ArrayList();
        File input = new File(path);
        CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
        CsvMapper csvMapper = new CsvMapper();
        try {
            // Read data from CSV file
            List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input).readAll();
            list = (ArrayList) readAll;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String regex(String exp, String str){
        String parsed = new String();
        Pattern pattern = Pattern.compile(exp);
        Matcher m = pattern.matcher(str);
        while(m.find()) {
            parsed = m.group(1);
        }
        return parsed;
    }

    public static String JsonReplace(String input_json, String path, String value){
        String json = "";
        String funct = "";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        final ScriptContext ctx = new SimpleScriptContext();
        ctx.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        if(value.contains("\"")){
            ctx.setAttribute("INPUT_VALUE", value, ScriptContext.ENGINE_SCOPE);
            funct = "function set(s,t,e){var r=Array.isArray(t)?t:t.split(\".\");return 1<r.length?(s && s.hasOwnProperty(r[0])&&\"object\"==typeof s[r[0]]||(s[r[0]]={}),set(s[r[0]],r.slice(1),e)):(s[r[0]]=e,!0)}json="+input_json+", v=INPUT_VALUE, p=\""+path+"\" , set(json,p,v), json = JSON.stringify(json), json;";
        }else{
            funct = "function set(s,t,e){var r=Array.isArray(t)?t:t.split(\".\");return 1<r.length?(s && s.hasOwnProperty(r[0])&&\"object\"==typeof s[r[0]]||(s[r[0]]={}),set(s[r[0]],r.slice(1),e)):(s[r[0]]=e,!0)}json="+input_json+", v=\""+value+"\", p=\""+path+"\" , set(json,p,v), json = JSON.stringify(json), json;";
        }
        try {
            json = (String) engine.eval(funct, ctx);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static int getPersonIndex(Scenario.Step step){
        int index=0;
        if(step.index.size() > 0){
            index=step.index.get(0);
        }
        return index;
    }
    
    public static String updateDOB(Persona person) {
    	
        String updatedDate = person.date_of_birth.replaceAll("-","/");
        Date date = new Date(updatedDate);  
	    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");  
	    String strDate = formatter.format(date);  
	    Calendar c = Calendar.getInstance();
        c.setTime(date); // Now use today date.
        c.add(Calendar.DATE, 5); // Adding 5 days
        c.add(Calendar.MONTH, 5); // Adding 5 Month
        String output = formatter.format(c.getTime());
        
        return output;
	}

}
