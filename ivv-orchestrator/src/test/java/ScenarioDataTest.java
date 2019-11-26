import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.dg.DataGenerator;
import org.junit.Test;

import java.util.ArrayList;

public class ScenarioDataTest {

    @Test
    public void scenarioData(){
        DataGenerator dg = new DataGenerator(System.getProperty("user.dir"), "config.properties");
        ArrayList<Scenario> scenariosToRun = dg.getScenarios();

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(scenariosToRun);
            System.out.println(jsonInString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
