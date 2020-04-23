package io.mosip.ivv.ida.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.dtos.PersonaDef;
import io.mosip.ivv.core.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public class AddBiometricInfo extends BaseStep implements StepInterface {
    private enum includes {
        leftEye, rightEye, leftThumb, rightThumb, leftIndex, leftMiddle, leftRing, leftLittle,
        rightIndex, rightMiddle, rightRing, rightLittle
    }

    @Override
    public void validateStep() throws RigInternalError {
        if(store.getCurrentPerson().getAgeGroup().equals(PersonaDef.AGE_GROUP.CHILD) && store.getCurrentIntroducer() == null){
            throw new RigInternalError("Introducer is required to process this step");
        }

        if(step.getParameters().size()>0){
            for(String par: step.getParameters()){
                try {
                    includes.valueOf(par);
                } catch (IllegalArgumentException ex) {
                    throw new RigInternalError("DSL error: no exception with this name: "+par);
                }
            }
        } else {
            step.getParameters().add(includes.leftEye.toString());
            step.getParameters().add(includes.rightEye.toString());
            step.getParameters().add(includes.leftThumb.toString());
            step.getParameters().add(includes.leftIndex.toString());
            step.getParameters().add(includes.leftMiddle.toString());
            step.getParameters().add(includes.leftRing.toString());
            step.getParameters().add(includes.leftLittle.toString());
            step.getParameters().add(includes.rightThumb.toString());
            step.getParameters().add(includes.rightIndex.toString());
            step.getParameters().add(includes.rightMiddle.toString());
            step.getParameters().add(includes.rightRing.toString());
            step.getParameters().add(includes.rightLittle.toString());
        }
    }



    @Override
    public void run() {
        RequestDataDTO requestData = prepare();
        ResponseDataDTO responseData = call(requestData);
        process(responseData);
    }

    @Override
    public RequestDataDTO prepare() {
        store.getCurrentPerson().getAuthenticationJSON().put("biometrics", new JSONArray());
        JSONArray encodedBiometrics = new JSONArray();
        JSONObject biometricsJSON = new JSONObject();
        for(String inc: step.getParameters()){
            logInfo("Adding "+inc);
            switch(includes.valueOf(inc)){

                case leftEye:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getLeftEye().getPath()));
                    biometricsJSON.put("bioType", "IIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case rightEye:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightEye().getPath()));
                    biometricsJSON.put("bioType", "IIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case leftThumb:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getLeftThumb().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case rightThumb:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightThumb().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case leftIndex:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getLeftIndex().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case leftMiddle:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getLeftMiddle().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case leftRing:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getLeftRing().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case leftLittle:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getLeftLittle().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case rightIndex:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightIndex().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case rightMiddle:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightMiddle().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case rightRing:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightRing().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;

                case rightLittle:
                    biometricsJSON.put("bioValue", Utils.readFileAsByte(store.getCurrentPerson().getBiometrics().getRightLittle().getPath()));
                    biometricsJSON.put("bioType", "FIR");
                    biometricsJSON.put("bioSubType", "UNKNOWN");
                    break;
            }
            biometricsJSON.put("deviceCode", "cogent");
            biometricsJSON.put("deviceProviderID", "cogent");
            biometricsJSON.put("transactionID", "1234567890");
            biometricsJSON.put("timestamp", Utils.getCurrentDateAndTimeForAPI());
            JSONObject biometricsData = new JSONObject(){{
                put("data", biometricsJSON);
            }};
            encodedBiometrics.add(biometricsData);
        }

        store.getCurrentPerson().getAuthenticationJSON().put("biometrics", encodedBiometrics);
        store.getCurrentPerson().getAuthParams().add("bio");
        return new RequestDataDTO(null, encodedBiometrics.toJSONString());
    }

    public ResponseDataDTO call(RequestDataDTO data){
        return null;
    }

    @Override
    public void process(ResponseDataDTO res) {

    }

}