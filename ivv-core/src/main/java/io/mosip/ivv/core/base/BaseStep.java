package io.mosip.ivv.core.base;

import com.aventstack.extentreports.ExtentTest;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.dtos.Store;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.ErrorMiddleware;
import io.mosip.ivv.core.utils.Utils;

import java.util.ArrayList;
import java.util.Properties;

public class BaseStep {
    public Boolean hasError = false;
    public Properties properties;
    public Store store = null;
    public int index = 0;
    public CallRecord callRecord;
    public ExtentTest extentInstance;
    public Scenario.Step step;

    public Boolean hasError() {
        return hasError;
    }

    public void setSystemProperties(Properties props){
        properties = props;
    }

    public ArrayList<Scenario.Step.Error> getErrorsForAssert(){
        return step.getErrors();
    }

    public void setStep(Scenario.Step s) {
        this.step = s;
    }

    public void setState(Store s) {
        this.store = s;
    }

    public Store getState() {
        return this.store;
    }

    public CallRecord getCallRecord() {
        return this.callRecord;
    }

    public void setExtentInstance(ExtentTest e){
        this.extentInstance = e;
    }

    public void setup() throws RigInternalError {
        try {
            if(store.getCurrentPerson() == null){
                store.setCurrentPerson(store.getPersona().getPersons().get(0));
            }
            if(store.getCurrentRegistrationUSer() == null){
                store.setCurrentRegistrationUSer(store.getRegistrationUsers().get(0));
            }
            if(store.getCurrentPartner() == null){
                store.setCurrentPartner(store.getPartners().get(0));
            }
        } catch (RuntimeException e){
            throw new RigInternalError("Error during setup "+e.getMessage());
        }
    }

    public void validateStep() throws RigInternalError {return;}

    public void logInfo(String msg){
        Utils.auditLog.info(msg);
        extentInstance.info(msg);
    }

    public void logWarning(String msg){
        Utils.auditLog.info(msg);
        extentInstance.warning(msg);
    }

    public void logFail(String msg){
        Utils.auditLog.severe(msg);
        extentInstance.fail(msg);
    }

    public void logSevere(String msg){
        Utils.auditLog.severe(msg);
        extentInstance.info(msg);
    }

    public void errorHandler(){
        if(callRecord != null){
            if (step.getErrors() != null && step.getErrors().size() > 0) {
                ErrorMiddleware.MiddlewareResponse emr = new ErrorMiddleware(step.getErrors(), callRecord.getResponse().body().asString(), extentInstance).inject();
                if (!emr.getStatus()) {
                    this.hasError = true;
                    return;
                }
            }
        }
    }

    public void assertHttpStatus(){
        if (callRecord != null && callRecord.getResponse().getStatusCode() == 200) {
            logInfo("Assert [passed]: HTTP status code assert passed - expected [200], actual [" + callRecord.getResponse().getStatusCode()+"]");
        } else if (callRecord != null && callRecord.getResponse().getStatusCode() != 200){
            logSevere("Assert [failed]: HTTP status code assert failed - expected [200], actual [" + callRecord.getResponse().getStatusCode()+"]");
            this.hasError = true;
            return;
        }
    }

    public void assertNoError() {
        if(callRecord != null){
            ReadContext ctx = JsonPath.parse(callRecord.getResponse().getBody().asString());
            try {
                if(ctx.read("$['errors']") != null){
                    logSevere("Assert [failed]: Response error object - expected [null], actual ["+ctx.read("$['errors']")+"]");
                    this.hasError=true;
                    return;
                }
                if(ctx.read("$['response']") == null){
                    logSevere("Assert [failed]: Response status - response expected [not null], actual ["+ctx.read("$['response']")+"]");
                    this.hasError=true;
                    return;
                }
            } catch (PathNotFoundException e) {
                e.printStackTrace();
                logSevere("Assert [failed]: Response status - "+e.getMessage());
                this.hasError=true;
                return;
            }
            logInfo("Assert [passed]: Response object - error object is null and response object is not null");
        }
    }

}
