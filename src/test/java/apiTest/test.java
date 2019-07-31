package test.java.apiTest;

import main.java.io.mosip.ivv.orchestrator.Scenario;
import main.java.io.mosip.ivv.utils.ParserEngine;
import main.java.io.mosip.ivv.utils.UtilsA;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertEquals;

public class test {
    @Test
    public void parserTest(){
        ParserEngine parser = new ParserEngine();
        ArrayList<Scenario> scenarios= parser.fetch();
        System.out.println(parser.getInputDataString());
        System.out.println(parser.getString(scenarios));
    }


    public void testJsonValue(){
        String json = "{\"name\":[{\"language\":\"en\",\"value\":\"Ankit\"},{\"language\":\"eu\",\"value\":\"el Ankit\"}],\"attr\":{\"height\":\"6feet\",\"weight\":\"65kg\"}}";
        String path = "attr.height";
        String value = "10f\"eet";
        String str = UtilsA.JsonReplace(json, path, value);
        System.out.println(str);
    }

}
