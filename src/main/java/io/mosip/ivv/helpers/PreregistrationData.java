package main.java.io.mosip.ivv.helpers;

import java.util.ArrayList;
import main.java.io.mosip.ivv.base.BaseHelperData;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.base.ProofDocument;

public class PreregistrationData extends BaseHelperData{
	public ArrayList<Persona> persons;
	public Persona user;
	public ArrayList<ProofDocument> documents;

	@Override
	protected boolean setData(String json) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getData() {
		// TODO Auto-generated method stub
		return null;
	}
}
