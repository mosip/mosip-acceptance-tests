package io.mosip.ivv.core.base;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.mosip.ivv.core.structures.CallRecord;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Store;
import io.mosip.ivv.core.utils.Utils;

import java.util.ArrayList;

public class Step {
    public Boolean hasError = false;
    public Store store = null;
    public int index = 0;
    public CallRecord callRecord;
    public ExtentTest extentInstance;

    public Boolean hasError() {
        return hasError;
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
}
