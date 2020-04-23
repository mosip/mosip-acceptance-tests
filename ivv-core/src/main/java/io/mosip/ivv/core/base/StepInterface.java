package io.mosip.ivv.core.base;


import com.aventstack.extentreports.ExtentTest;
import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.exceptions.RigInternalError;

import java.util.ArrayList;

public interface StepInterface {
    Boolean hasError();

    void setState(Store s);

    Store getState();

    ArrayList<Scenario.Step.Error> getErrorsForAssert();

    void errorHandler();

    void validateStep() throws RigInternalError;

    void assertStatus();

    void assertHttpStatus();

    void setExtentInstance(ExtentTest e);

    void setStep(Scenario.Step s);

    void setup() throws RigInternalError;

    CallRecord getCallRecord();
    void run();

    RequestDataDTO prepare();
    ResponseDataDTO call(RequestDataDTO requestData);
    void process(ResponseDataDTO res);
}
