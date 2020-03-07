package io.mosip.ivv.parser.Utils;

import io.mosip.ivv.core.dtos.IDObjectField;
import io.mosip.ivv.core.dtos.Person;
import org.apache.commons.lang3.EnumUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static io.mosip.ivv.core.utils.Utils.regex;

public class Helper {

    public static String readFileAsString(String path, Charset encoding){
        byte[] encoded = new byte[0];
        File f = new File(path);
        if(!f.exists()) {
            return null;
        }
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, encoding);
    }

    public static IDObjectField parseField(String key, String val, String primaryLang, String secondaryLang){
        String field = "";
        String fieldType;
        IDObjectField.type fieldTypeEnum = IDObjectField.type.string;
        IDObjectField idObjectField = new IDObjectField();
        field = regex("\\{(\\S*)\\}", key);
        fieldType = regex("\\((\\S*)\\)", key);
        if(EnumUtils.isValidEnum(IDObjectField.type.class, fieldType)){
            idObjectField.setType(IDObjectField.type.valueOf(fieldType));
        } else {
            idObjectField.setType(IDObjectField.type.string);
        }
        if(idObjectField.getType().equals(IDObjectField.type.multilang)){
            String[] vals = val.split("%%");
            switch(vals.length){
                case 0:
                    idObjectField.setPrimaryValue("");
                    idObjectField.setSecondaryValue("");
                    break;
                case 1:
                    idObjectField.setPrimaryValue(vals[0].trim());
                    idObjectField.setSecondaryValue(vals[0].trim());
                    break;
                default:
                    idObjectField.setPrimaryValue(vals[0].trim());
                    idObjectField.setSecondaryValue(vals[1].trim());
                    break;
            }
        } else {
            idObjectField.setPrimaryValue(val);
        }

        if(!regex("([\\^])", key).isEmpty()){
            idObjectField.setMutate(true);
        }
        return idObjectField;
    }

}
