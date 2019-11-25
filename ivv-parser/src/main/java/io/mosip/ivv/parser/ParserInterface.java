package io.mosip.ivv.parser;

import io.mosip.ivv.core.structures.Persona;
import io.mosip.ivv.core.structures.ProofDocument;
import io.mosip.ivv.core.structures.Scenario;

import java.util.ArrayList;
import java.util.HashMap;

public interface ParserInterface {

    ArrayList<Persona> getPersonas();

    ArrayList<Scenario> getScenarios();

    ArrayList<ProofDocument> getDocuments();

    HashMap<String, String> getGlobals();

}
