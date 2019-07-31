package main.java.io.mosip.ivv.base;

import java.util.Map;

public class apiTest {

//    public static String CONFIG_FILE_PATH = System.getProperty("user.dir") + "/src/main/resources/" + "config.properties";
    public static String TEST_CASE = System.getProperty("user.dir") + "/src/main/resources/" + "testcase.json";
    public static String CONFIG_FILE_PATH = System.getProperty("user.dir") + "/src/main/resources/" + "config.properties";


    public String getAPIInfo(String type, Object obj){
        Map<String, String> map = (Map<String, String>) obj;
        try {
            return map.get(type);
        } catch (NullPointerException e){
            e.printStackTrace();
            return "";
        }
    }

}
