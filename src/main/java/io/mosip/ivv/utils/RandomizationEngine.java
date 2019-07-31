package main.java.io.mosip.ivv.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.io.mosip.ivv.base.PersonaDef;
import main.java.io.mosip.ivv.base.ProofDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class RandomizationEngine {

    public static ProofDocument pickDocumentRandomly(ArrayList<ProofDocument> list, PersonaDef.RESIDENCE_STATUS rs){
        ArrayList<ProofDocument> docs = new ArrayList();
        for (ProofDocument pd: list){
            Boolean found = false;
            for(int i=0; i< pd.tags.size(); i++){
                if(rs.equals(PersonaDef.RESIDENCE_STATUS.valueOf(pd.tags.get(i)))){
                    found = true;
                }
            }
            if(found){
                docs.add(pd);
            }
        }
        if(docs.size()>0){
            int randomNum = ThreadLocalRandom.current().nextInt(0, docs.size());
            return docs.get(randomNum);
        }else {
            return null;
        }
    }
}
