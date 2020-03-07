import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.dg.DataGenerator;
import org.junit.Test;

import java.util.ArrayList;

public class DataGeneratorTest {

    @Test
    public void dataGeneratorTest(){
        DataGenerator dg = new DataGenerator(System.getProperty("user.dir"), "config.properties");
        ArrayList<Scenario> scenarios = dg.getScenarios();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(scenarios);
            System.out.println(jsonInString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
