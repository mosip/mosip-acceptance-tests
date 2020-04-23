package io.mosip.ivv.orchestrator;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.dg.DataGenerator;
import io.mosip.ivv.parser.Parser;
import io.mosip.ivv.registration.config.Setup;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class Orchestrator {
    private Boolean regClientSetup = false;
    private static ExtentHtmlReporter htmlReporter;
    private static ExtentReports extent;
    private ApplicationContext applicationContext;
    private Object localApplicationContext;
    private Properties properties;
    private HashMap<String, String> packages = new HashMap<String, String>(){{
        put("pr", "io.mosip.ivv.preregistration.methods");
        put("rc", "io.mosip.ivv.registration.methods");
        put("rp", "io.mosip.ivv.regprocessor.methods");
        put("kr", "io.mosip.ivv.kernel.methods");
        put("ia", "io.mosip.ivv.ida.methods");
        put("mt", "io.mosip.ivv.mutators.methods");
    }};

    @BeforeSuite
    public void beforeSuite(){
        this.properties = Utils.getProperties("config.properties");
        this.configToSystemProperties();
        Utils.setupLogger(System.getProperty("user.dir")+this.properties.getProperty("ivv._path.auditlog"));
        /* setting exentreport */
        htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir")+this.properties.getProperty("ivv._path.reports"));
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
    }

    @BeforeTest
    public static void  create_proxy_server() {

    }

    @AfterSuite
    public void afterSuite(){
        extent.flush();
    }

    @DataProvider(name="ScenarioDataProvider", parallel = false)
    public static Object[][] dataProvider() {
        String configFile = Paths.get(System.getProperty("user.dir"),"config.properties").normalize().toString();
        Properties properties = Utils.getProperties(configFile);
        try {
            Utils.validateConfigFile(configFile);
        } catch (RigInternalError rigInternalError) {
            rigInternalError.printStackTrace();
            System.exit(0);
        }
        ParserInputDTO parserInputDTO = new ParserInputDTO();
        parserInputDTO.setDocumentsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.documents.folder")).normalize().toString());
        parserInputDTO.setBiometricsFolder(Paths.get(configFile, "..", properties.getProperty("ivv.path.biometrics.folder")).normalize().toString());
        parserInputDTO.setPersonaSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.persona.sheet")).normalize().toString());
        parserInputDTO.setScenarioSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.scenario.sheet")).normalize().toString());
        parserInputDTO.setRcSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.rcpersona.sheet")).normalize().toString());
        parserInputDTO.setPartnerSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.partner.sheet")).normalize().toString());
        parserInputDTO.setIdObjectSchema(Paths.get(configFile, "..", properties.getProperty("ivv.path.idobject")).normalize().toString());
        parserInputDTO.setDocumentsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.documents.sheet")).normalize().toString());
        parserInputDTO.setBiometricsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.biometrics.sheet")).normalize().toString());
        parserInputDTO.setGlobalsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.globals.sheet")).normalize().toString());
        parserInputDTO.setConfigsSheet(Paths.get(configFile, "..", properties.getProperty("ivv.path.configs.sheet")).normalize().toString());

        Parser parser = new Parser(parserInputDTO);
        DataGenerator dg = new DataGenerator();
        ArrayList<Scenario> scenarios = new ArrayList<>();
        try {
            scenarios = dg.prepareScenarios(parser.getScenarios(), parser.getPersonas());
        } catch (RigInternalError rigInternalError) {
            rigInternalError.printStackTrace();
        }

        for(int i=0; i<scenarios.size();i++){
            scenarios.get(i).setRegistrationUsers(parser.getRCUsers());
            scenarios.get(i).setPartners(parser.getPartners());
        }
        HashMap<String, String> configs = parser.getConfigs();
        HashMap<String, String> globals = parser.getGlobals();
        ArrayList<RegistrationUser> rcUsers = parser.getRCUsers();
        Object[][] dataArray = new Object[scenarios.size()][4];
        for(int i=0; i<scenarios.size();i++){
            dataArray[i][0] = i;
            dataArray[i][1] = scenarios.get(i);
            dataArray[i][2] = configs;
            dataArray[i][3] = globals;
        }
        return dataArray;
    }

    @BeforeMethod
    public void beforeMethod(Method method) {

    }

    @Test(dataProvider="ScenarioDataProvider")
    private void run(int i, Scenario scenario, HashMap<String, String> configs, HashMap<String, String> globals) throws SQLException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String stepsAsString = mapper.writeValueAsString(scenario.getSteps());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Utils.auditLog.info("");
        Utils.auditLog.info("-- *** Scenario "+ scenario.getName() + ": " + scenario.getDescription()+
                " *** --");
        ExtentTest extentTest = extent.createTest("Scenario_" + scenario.getName() + ": " + scenario.getDescription());
        Store store = new Store();
        store.setConfigs(configs);
        store.setGlobals(globals);
        store.setPersona(scenario.getPersona());
        store.setRegistrationUsers(scenario.getRegistrationUsers());
        store.setPartners(scenario.getPartners());
        store.setProperties(this.properties);
        for(Scenario.Step step: scenario.getSteps()){
            if(step.getModule().equals(Scenario.Step.modules.rc)){
                this.regClientSetup();
                store.setRegApplicationContext(this.applicationContext);
                store.setRegLocalContext(this.localApplicationContext);
            }
            Utils.auditLog.info("" );
            String identifier =
                    "> #[Test Step: " + step.getName()+ "] [module: "+step.getModule()+"] [variant: "+step.getVariant()+
                            "]";
            Utils.auditLog.info(identifier);
            try {
                extentTest.info(identifier+" - running");
                //extentTest.info("parameters: "+step.getParameters().toString());
                StepInterface st = getInstanceOf(step);
                st.setExtentInstance(extentTest);
                st.setState(store);
                st.setStep(step);
                st.setup();
                st.validateStep();
                st.run();
                st.assertHttpStatus();
                if(st.hasError()){
                    extentTest.fail(identifier+" - failed");
                    Assert.assertFalse(st.hasError());
                }
                if(st.getErrorsForAssert().size()>0){
                    st.errorHandler();
                    if(st.hasError()){
                        extentTest.fail(identifier+" - failed");
                        Assert.assertFalse(st.hasError());
                    }
                } else {
                    st.assertStatus();
                    if(st.hasError()){
                        extentTest.fail(identifier+" - failed");
                        Assert.assertFalse(st.hasError());
                    }
                }
                store = st.getState();
                if(st.hasError()){
                    Boolean failed = false;
                    extentTest.fail(identifier+" - failed");
                    Assert.assertFalse(st.hasError());
                }else{
                    extentTest.pass(identifier+" - passed");
                }
            } catch (ClassNotFoundException e) {
                extentTest.error(identifier+" - ClassNotFoundException --> "+e.toString());
                e.printStackTrace();
                Assert.assertTrue(false);
                return;
            } catch (IllegalAccessException e) {
                extentTest.error(identifier+" - IllegalAccessException --> "+e.toString());
                e.printStackTrace();
                Assert.assertTrue(false);
                return;
            } catch (InstantiationException e) {
                extentTest.error(identifier+" - InstantiationException --> "+e.toString());
                e.printStackTrace();
                Assert.assertTrue(false);
                return;
            } catch(RigInternalError e){
                extentTest.error(identifier+" - RigInternalError --> "+e.toString());
                e.printStackTrace();
                Assert.assertTrue(false);
                return;
            } catch (RuntimeException e){
                extentTest.error(identifier+" - RuntimeException --> "+e.toString());
                e.printStackTrace();
                Assert.assertTrue(false);
                return;
            }
        }
    }

    private void regClientSetup(){
        /*  registration client related setup   */
        if(!this.regClientSetup){
            Setup.SetupResponse sr = Setup.init();
            this.applicationContext = sr.applicationContext;
            this.localApplicationContext = sr.localApplicationContext;
            this.regClientSetup = true;
        }
    }



    private String getPackage(Scenario.Step step){
        String pack = packages.get(step.getModule().toString());
        return pack;
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {

    }


    public StepInterface getInstanceOf(Scenario.Step step) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String className = getPackage(step) +"."+  step.getName().substring(0, 1).toUpperCase() + step.getName().substring(1);
        return (StepInterface) Class.forName(className).newInstance();
    }

    private void configToSystemProperties(){
        Set<String> keys = this.properties.stringPropertyNames();
        for (String key : keys) {
            System.setProperty(key, this.properties.getProperty(key));
        }
    }

}
