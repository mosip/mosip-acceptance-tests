package io.mosip.ivv.core.base;


import com.aventstack.extentreports.ExtentTest;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.dtos.Store;

public interface StepInterface {
    Boolean hasError();

    void setState(Store s);

    Store getState();

    void validateStep() throws RigInternalError;

    void setExtentInstance(ExtentTest e);

    void setStep(Scenario.Step s);

    void setup() throws RigInternalError;

    CallRecord getCallRecord();
    void run();
}
