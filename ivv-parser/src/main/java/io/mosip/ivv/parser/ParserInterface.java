package io.mosip.ivv.parser;

import io.mosip.ivv.core.dtos.Persona;
import io.mosip.ivv.core.dtos.ProofDocument;
import io.mosip.ivv.core.dtos.Scenario;

import java.util.ArrayList;
import java.util.HashMap;

public interface ParserInterface {

    ArrayList<Persona> getPersonas();

    ArrayList<Scenario> getScenarios();

    ArrayList<ProofDocument> getDocuments();

    HashMap<String, String> getGlobals();

}
