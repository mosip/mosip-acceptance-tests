package io.mosip.ivv.mutators.methods;

import io.mosip.ivv.core.base.BaseStep;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.dtos.IDObjectField;
import io.mosip.ivv.core.dtos.RequestDataDTO;
import io.mosip.ivv.core.dtos.ResponseDataDTO;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.core.utils.Utils;

import static io.mosip.ivv.core.utils.Utils.regex;

public class UpdatePerson extends BaseStep implements StepInterface {

    @Override
    public void validateStep() throws RigInternalError {
        if(step.getParameters().size() < 2){
            throw new RigInternalError("DSL error: Expect key and its value");
        }

        if(step.getParameters().get(0).isEmpty()){
            throw new RigInternalError("DSL error: key should not be empty");
        }
    }



    @Override
    public void run() {
        String key = step.getParameters().get(0);
        String value = step.getParameters().get(1);
        if(value == "null"){
            value = null;
        }
        switch(key){
            case "docTypecode":
                store.getCurrentPerson().getProofOfAddress().setDocTypeCode(value);
                break;
            case "langCode":
                store.getCurrentPerson().setLangCode(value);
                break;
            case "userid":
                store.getCurrentPerson().setUserid(value);
                break;
            case "date":
                store.getCurrentPerson().getSlot().setDate(value);
                break;
            case "fromDate":
                store.getCurrentPerson().getSlot().setFrom(value);
                break;
            case "toDate":
                store.getCurrentPerson().getSlot().setTo(value);
                break;
            case "registrationId":
                store.getCurrentPerson().setRegistrationId(value);
                break;

            case "uin":
                store.getCurrentPerson().setUin(value);
                break;

            case "preRegistrationId":
                store.getCurrentPerson().setPreRegistrationId(value);
                break;

            case "centerId":
                store.getCurrentPerson().setRegistrationCenterId(value);
                break;

            default:
                if(key.isEmpty()){
                    return;
                }
                String field = regex("{\\S*}", key);
                if(field.isEmpty()){
                    return;
                }
                IDObjectField idObjectField = store.getCurrentPerson().getIdObject().get(key);
                if(idObjectField != null){
                    IDObjectField newIdObjectField = Utils.updateIDField(idObjectField, value, store.getCurrentPerson().getPrimaryLang(), store.getCurrentPerson().getSecondaryLang());
                    store.getCurrentPerson().getIdObject().put(key, newIdObjectField);
                }
                return;
        }
    }

    @Override
    public RequestDataDTO prepare() {
        return null;
    }

    @Override
    public ResponseDataDTO call(RequestDataDTO requestData) {
        return null;
    }

    @Override
    public void process(ResponseDataDTO res) {

    }
}
