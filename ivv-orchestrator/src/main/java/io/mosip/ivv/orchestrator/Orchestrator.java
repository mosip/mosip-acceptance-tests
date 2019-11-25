package io.mosip.ivv.orchestrator;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;
import io.mosip.ivv.dg.DataGenerator;
import io.mosip.ivv.registration.config.Setup;
import org.springframework.context.ApplicationContext;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

@Test
public class Orchestrator {
    private Boolean regClientSetup = false;
    private static ExtentHtmlReporter htmlReporter;
    private static ExtentReports extent;
    private ExtentTest extentTest;
    private ApplicationContext applicationContext;
    private Object localApplicationContext;
    private Properties properties;
    private HashMap<String, String> packages = new HashMap<String, String>(){{
        put("pr", "io.mosip.ivv.preregistration.methods");
        put("rc", "io.mosip.ivv.registration.methods");
        put("rp", "io.mosip.ivv.regprocessor.methods");
        put("kr", "io.mosip.ivv.kernel.methods");
        put("ia", "io.mosip.ivv.idauthentication.methods");
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
    }

    @BeforeTest
    public static void  create_proxy_server() {

    }

    @AfterSuite
    public void afterSuite(){
        extent.flush();
    }

    @DataProvider(name="ScenarioDataProvider")
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
            System.out.println(stepsAsString);
            System.out.println(mapper.writeValueAsString(scenario.getData()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        this.extentTest = extent.createTest("Scenario_" + scenario.getName() + ": " + scenario.getDescription());
        Store store = new Store();
        store.setConfigs(configs);
        store.setGlobals(globals);
        store.setScenarioData(scenario.getData());
        store.setProperties(this.properties);

        if(scenario.getModules().contains(Scenario.Step.modules.rc)){
            this.regClientSetup();
            store.setRegApplicationContext(this.applicationContext);
            store.setRegLocalContext(this.localApplicationContext);
        }

        for(Scenario.Step step: scenario.getSteps()){
            String identifier = "Step: "+step.getName()+", module: "+step.getModule()+", variant: "+step.getVariant();
            try {
                this.extentTest.info(identifier+" - running");
                StepInterface st = getInstanceOf(step);
                st.setExtentInstance(this.extentTest);
                st.setState(store);
                st.run(step);
                store = st.getState();
                if(st.hasError()){
                    this.extentTest.fail(identifier+" - failed");
                    if(System.getProperty("ivv.scenario.continueOnFailure") == null || System.getProperty("ivv.scenario.continueOnFailure").equals("N")){
                        return;
                    }
                }else{
                    this.extentTest.pass(identifier+" - passed");
                }
            } catch (ClassNotFoundException e) {
                this.extentTest.error(identifier+" - error");
                e.printStackTrace();
                return;
            } catch (IllegalAccessException e) {
                this.extentTest.error(identifier+" - error");
                e.printStackTrace();
                return;
            } catch (InstantiationException e) {
                this.extentTest.error(identifier+" - error");
                e.printStackTrace();
                return;
            } catch (RuntimeException e){
                this.extentTest.error(identifier+" - error");
                e.printStackTrace();
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
        System.out.println(step.getModule());
        System.out.println(pack);
        return pack;
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {

    }

    public StepInterface getInstanceOf(Scenario.Step step) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String className = getPackage(step) +"."+  step.getName().substring(0, 1).toUpperCase() + step.getName().substring(1);
        System.out.println(className);
        return (StepInterface) Class.forName(className).newInstance();
    }

    private void printReport(ArrayList<ExtentLogger> reportList){
        for (ExtentLogger el : reportList){
            this.extentTest.log(el.getType(), el.getMsg());
        }
    }

    private void configToSystemProperties(){
        Set<String> keys = this.properties.stringPropertyNames();
        for (String key : keys) {
            System.setProperty(key, this.properties.getProperty(key));
        }
    }

}
