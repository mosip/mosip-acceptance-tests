package io.mosip.ivv.orchestrator;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.dg.DataGenerator;
import io.mosip.ivv.registration.config.Setup;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
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
        Utils.setupLogger(System.getProperty("user.dir")+this.properties.getProperty("ivv.path.auditlog"));
        /* setting exentreport */
        htmlReporter = new ExtentHtmlReporter(System.getProperty("user.dir")+this.properties.getProperty("ivv.path.reports"));
        extent = new ExtentReports();
        extent.attachReporter(htmlReporter);
        this.regClientSetup();
    }

    @BeforeTest
    public static void  create_proxy_server() {

    }

    @AfterSuite
    public void afterSuite(){
        extent.flush();
    }

    @DataProvider(name="ScenarioDataProvider", parallel = false)
    public static Object[][] dataProvider(ITestContext context) {
        DataGenerator dg = new DataGenerator(System.getProperty("user.dir"), "config.properties");
        ArrayList<Scenario> scenariosToRun = dg.getScenarios();
        HashMap<String, String> configs = dg.getConfigs();
        HashMap<String, String> globals = dg.getGlobals();
        Object[][] dataArray = new Object[scenariosToRun.size()][4];
        for(int i=0; i<scenariosToRun.size();i++){
            dataArray[i][0] = i;
            dataArray[i][1] = scenariosToRun.get(i);
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
        store.setScenarioData(scenario.getData());
        store.setProperties(this.properties);
        store.setRegApplicationContext(this.applicationContext);
        store.setRegLocalContext(this.localApplicationContext);

        for(Scenario.Step step: scenario.getSteps()){
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
                store = st.getState();
                if(st.hasError()){
                    extentTest.fail(identifier+" - failed");
                    if(System.getProperty("ivv.scenario.continueOnFailure") == null || System.getProperty("ivv.scenario.continueOnFailure").equals("N")){
                        Assert.assertEquals(java.util.Optional.of(true), st.hasError());
                        return;
                    }
                }else{
                    extentTest.pass(identifier+" - passed");
                }
            } catch (ClassNotFoundException e) {
                extentTest.error(identifier+" - ClassNotFoundException --> "+e.toString());
                e.printStackTrace();
                Assert.assertEquals(java.util.Optional.of(true), false);
                return;
            } catch (IllegalAccessException e) {
                extentTest.error(identifier+" - IllegalAccessException --> "+e.toString());
                e.printStackTrace();
                Assert.assertEquals(java.util.Optional.of(true), false);
                return;
            } catch (InstantiationException e) {
                extentTest.error(identifier+" - InstantiationException --> "+e.toString());
                e.printStackTrace();
                Assert.assertEquals(java.util.Optional.of(true), false);
                return;
            } catch(RigInternalError e){
                extentTest.error(identifier+" - RigInternalError --> "+e.toString());
                e.printStackTrace();
                Assert.assertEquals(java.util.Optional.of(true), false);
                return;
            } catch (RuntimeException e){
                extentTest.error(identifier+" - RuntimeException --> "+e.toString());
                e.printStackTrace();
                Assert.assertEquals(java.util.Optional.of(true), false);
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
