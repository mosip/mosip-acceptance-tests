package io.mosip.ivv.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.dtos.ParserInputDTO;
import io.mosip.ivv.core.dtos.Persona;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

public class ParserTest {
    @Test
    public void getPersonas(){
        String configFile = Paths.get(System.getProperty("user.dir"), "..", "ivv-orchestrator","config.properties").normalize().toString();
        Properties properties = Utils.getProperties(configFile);
        ParserInputDTO parserInputDTO = new ParserInputDTO();
        parserInputDTO.setConfigProperties(properties);
        parserInputDTO.setDocumentsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.documents.folder")).normalize().toString());
        parserInputDTO.setBiometricsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.biometrics.folder")).normalize().toString());
        parserInputDTO.setPersonaSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.persona.sheet")).normalize().toString());
        parserInputDTO.setRcSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.rcpersona.sheet")).normalize().toString());
        parserInputDTO.setPartnerSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.partner.sheet")).normalize().toString());
        parserInputDTO.setIdObjectSchema(Paths.get(configFile, "..", properties.getProperty("ivv.path.idobject")).normalize().toString());
        parserInputDTO.setDocumentsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.documents.sheet")).normalize().toString());
        parserInputDTO.setBiometricsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.biometrics.sheet")).normalize().toString());
        parserInputDTO.setGlobalsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.globals.sheet")).normalize().toString());
        parserInputDTO.setConfigsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.configs.sheet")).normalize().toString());
        Parser pr = new Parser(parserInputDTO);
        try {
            ArrayList<Persona> psn = pr.getPersonas();
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writerWithDefaultPrettyPrinter().writeValueAsString(psn);
            System.out.println(jsonStr);
        } catch (RigInternalError rigInternalError) {
            rigInternalError.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getScenarios(){
        String configFile = Paths.get(System.getProperty("user.dir"), "..", "ivv-orchestrator","config.properties").normalize().toString();
        Properties properties = Utils.getProperties(configFile);
        ParserInputDTO parserInputDTO = new ParserInputDTO();
        parserInputDTO.setConfigProperties(properties);
        parserInputDTO.setDocumentsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.documents.folder")).normalize().toString());
        parserInputDTO.setBiometricsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.biometrics.folder")).normalize().toString());
        parserInputDTO.setPersonaSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.persona.sheet")).normalize().toString());
        parserInputDTO.setRcSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.rcpersona.sheet")).normalize().toString());
        parserInputDTO.setPartnerSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.partner.sheet")).normalize().toString());
        parserInputDTO.setIdObjectSchema(Paths.get(configFile, "..", properties.getProperty("ivv.path.idobject")).normalize().toString());
        parserInputDTO.setDocumentsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.documents.sheet")).normalize().toString());
        parserInputDTO.setBiometricsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.biometrics.sheet")).normalize().toString());
        parserInputDTO.setGlobalsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.globals.sheet")).normalize().toString());
        parserInputDTO.setScenarioSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.scenario.sheet")).normalize().toString());
        Parser pr = new Parser(parserInputDTO);
        try {
            ArrayList<Scenario> psn = pr.getScenarios();
            ObjectMapper Obj = new ObjectMapper();
            String jsonStr = Obj.writerWithDefaultPrettyPrinter().writeValueAsString(psn);
            System.out.println(jsonStr);
        } catch (RigInternalError rigInternalError) {
            rigInternalError.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
