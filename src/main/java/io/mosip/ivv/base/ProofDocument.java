package main.java.io.mosip.ivv.base;

import java.util.ArrayList;

public class ProofDocument {
	public enum DOCUMENT_CATEGORY {POA, POB, POI, POE, POR};
	public String path = "";
	public DOCUMENT_CATEGORY doc_cat_code = DOCUMENT_CATEGORY.POI;
	public String doc_type_code = "";
	public String doc_file_format = "";
	public String doc_id = "";
	public String name = "";
	public ArrayList<String> tags = new ArrayList<String>();
}
