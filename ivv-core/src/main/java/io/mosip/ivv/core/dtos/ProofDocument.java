package io.mosip.ivv.core.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class ProofDocument {

    public enum DOCUMENT_CATEGORY {POA, POB, POI, POR, POEX, POEM};
    private DOCUMENT_CATEGORY docCatCode = DOCUMENT_CATEGORY.POI;
    private String docTypeCode = "";
    private String docFileFormat = "";
    private String docId = "";
    private String name = "";
    private String path = "";
    private ArrayList<String> tags = new ArrayList();
}
