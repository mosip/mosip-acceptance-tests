package io.mosip.ivv.core.base;


import com.aventstack.extentreports.ExtentTest;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.structures.CallRecord;
import io.mosip.ivv.core.structures.ExtentLogger;
import io.mosip.ivv.core.structures.Scenario;
import io.mosip.ivv.core.structures.Store;

import java.util.ArrayList;

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
