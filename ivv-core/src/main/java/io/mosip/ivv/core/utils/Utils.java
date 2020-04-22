package io.mosip.ivv.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.IDObjectField;
import io.mosip.ivv.core.dtos.Person;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.exceptions.RigInternalError;
import org.apache.commons.lang3.EnumUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utils {
    public static Logger auditLog = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static Logger requestAndresponseLog = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    /*
     * public static void deleteDirectoryPath(String directoryName, String folderName) { folderPath = directoryName; theDir = new File(folderPath);
     * theDir.delete(); }
     *
     */
    public static void deleteDirectoryPath(String path) {
        File file = new File(path);
        if (file.exists()) {
            do {
                deleteIt(file);
            } while (file.exists());
        } else {
        }
    }

    public static void setupLogger(String path) {
        LogManager.getLogManager().reset();
        auditLog.setLevel(Level.ALL);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new CustomizedLogFormatter());
        ch.setLevel(Level.ALL);
        auditLog.addHandler(ch);
        try {
            FileHandler fh = new FileHandler(path, true);
            fh.setFormatter(new CustomizedLogFormatter());
            fh.setLevel(Level.ALL);
            auditLog.addHandler(fh);
        } catch (IOException e) {
            auditLog.log(Level.SEVERE, "File logger not working", e);
        }
    }


    private static void deleteIt(File file) {
        if (file.isDirectory()) {
            String fileList[] = file.list();
            if (fileList.length == 0) {
                file.delete();
            } else {
                int size = fileList.length;
                for (int i = 0; i < size; i++) {
                    String fileName = fileList[i];
                    String fullPath = file.getPath() + "/" + fileName;
                    File fileOrFolder = new File(fullPath);
                    deleteIt(fileOrFolder);
                }
            }
        } else {
            file.delete();
        }
    }

    public static String createDirectory(String path, String name) {
        String folderPath = path + name;
        File theDir = new File(folderPath);
        if (!theDir.exists()) {
            try {
                theDir.mkdirs();
            } catch (SecurityException se) {
            }
        }
        return folderPath;
    }

    public static String getCurrentDate() {
        DateFormat format = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        return format.format(date).toString();
    }

    public static String getCurrentDateAndTime() {
        DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        Date date = new Date();
        return format.format(date).toString();
    }

    public static String assertResponseStatus(String statusCode) {
        String returnString = null;

        switch (Integer.parseInt(statusCode)) {
            case 200: // Successful or OK
                returnString = "OK, The request has succeeded";
                break;
            case 201: // Created
                returnString = "Created! The request has been fulfilled and resulted in a new resource being created";
                break;
            case 204: // Created
                returnString = "No Content! The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation";
                break;
            case 304: // Created
                returnString = "Not Modified!";
                break;
            case 400: // Bad Request
                returnString = "Bad Request! The request could not be understood by the server due to malformed syntax";
                break;
            case 401: // Unauthorized
                returnString = "Unauthorized! The request requires user authentication";
                break;
            case 403: // Forbidden
                returnString = "Forbidden! The server understood the request, but is refusing to fulfill it";
                break;
            case 404: // Not found
                returnString = "Not Found! The server has not found anything matching the Request-URI";
                break;
            case 405: // Method not allowed
                returnString = "Method not allowed! The method specified in the Request-Line is not allowed for the resource identified by the Request-URI.";
                break;
            case 409: // Conflict
                returnString = "Conflict! The request could not be completed due to a conflict with the current state of the resource.";
                break;
            case 500: // Internal Server ErrorPolicy
                returnString = "Internal Server ErrorPolicy! The server encountered an unexpected condition which prevented it from fulfilling the request";
                break;
            case 503: // Service Unavailable
                returnString = "Service Unavailable! The server is currently unable to handle the request due to a temporary overloading or maintenance of the server";
                break;
        }

        return returnString;
    }

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

    public static byte[] readFileAsByte(String path){
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encoded;
    }

    public static String byteToBase64(byte[] b){
        return Base64.getEncoder().encodeToString(b);
    }

    public static byte[] base64ToByte(String b){
        return Base64.getDecoder().decode(b);
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
            if(m.group().length() > 0){
                parsed = m.group(1);
            }
        }
        return parsed;
    }

    public static boolean isNumeric(String inputStr) {
        try {
            double d = Double.parseDouble(inputStr);
        } catch (NumberFormatException nfe) {
            return false;
        } catch (NullPointerException npe){
            return false;
        }
        return true;
    }

    public static int getPersonIndex(Scenario.Step step){
        int index=0;
        if(step.getIndex().size() > 0){
            index=step.getIndex().get(0);
        }
        return index;
    }

    public static boolean hasPersonIndex(ArrayList<Integer> indexes, int i){
        boolean hasIndex = false;
        if(indexes.size() > i){
            hasIndex = true;
        }
        return hasIndex;
    }

    public static String getUUID() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid;
    }

    public static String getCurrentDateAndTimeForAPI() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date date = new Date();
//                ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT )
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    public static void logCallRecord(CallRecord c){
        Utils.auditLog.info(c.getUrl());
        Utils.auditLog.info(c.getInputData());
        Utils.auditLog.info(c.getResponse().asString());
    }

    public static String getRandom(int min, int max){
        Random generator = new Random();
        return (generator.nextInt((max - min) + 1) + min)+"";
    }

    public static String generateRID(String center_id, String machine_id) {
        String rid = center_id+machine_id+getRandom(10000, 99999);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String date_part = ZonedDateTime.now(ZoneOffset.UTC).format(format);
        return rid+date_part;
    }

    public static String prettyJson(String uglyJson){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(uglyJson);
        return gson.toJson(je);
    }

    public static String removeValuesFromJson(String json){
        return prettyJson(json).replaceAll("[:].+\".*\"",":\"\"").replaceAll("\\s+","");
    }

    public static Boolean matchJsonOnlyKeys(String a, String b){
        String prettya = removeValuesFromJson(prettyJson(a)).replaceAll("\\s+","");
        String prettyb = removeValuesFromJson(prettyJson(b)).replaceAll("\\s+","");
        System.out.println("JSON A:");
        System.out.println(prettya);
        System.out.println("JSON B:");
        System.out.println(prettyb);
        if(prettya.equals(prettyb)){
            return true;
        } else {
            return false;
        }
    }

    public static IDObjectField updateIDField(IDObjectField idObjectField, String value, String primaryLang, String secondaryLang){
        IDObjectField newIDObjectField = new IDObjectField();
        newIDObjectField.setType(idObjectField.getType());
        newIDObjectField.setMutate(idObjectField.getMutate());
        newIDObjectField.setPrimaryValue(idObjectField.getPrimaryValue());
        newIDObjectField.setSecondaryValue(idObjectField.getSecondaryValue());
        if(idObjectField.getType().equals(IDObjectField.type.simpleType)){
            String[] vals = value.split("%%");
            switch(vals.length){
                case 0:
                    newIDObjectField.setPrimaryValue("");
                    newIDObjectField.setSecondaryValue("");
                    break;
                case 1:
                    newIDObjectField.setPrimaryValue(vals[0].trim());
                    newIDObjectField.setSecondaryValue(vals[0].trim());
                    break;
                default:
                    newIDObjectField.setPrimaryValue(vals[0].trim());
                    newIDObjectField.setSecondaryValue(vals[1].trim());
                    break;
            }
        } else {
            newIDObjectField.setPrimaryValue(value);
        }
        return newIDObjectField;
    }

    public static void validateConfigFile(String configPath) throws RigInternalError {
        Path config_path = Paths.get(configPath).normalize();
        if(!Files.exists(config_path)){
            throw new RigInternalError("validateConfigFile -- Invalid config file path");
        }
        ArrayList<String> paths = new ArrayList<>();
        Properties properties = Utils.getProperties(config_path.toString());

        ArrayList<String> props = new ArrayList<>();
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if(key.startsWith("ivv.path")){
                if(value == null){
                    throw new RigInternalError("validateConfigFile -- "+key+" value is null");
                }
                if(!Files.exists(Paths.get(config_path.toString(), "..", value).normalize())){
                    throw new RigInternalError("validateConfigFile -- "+key+" path:"+value);
                }
            }
        }
    }

}
