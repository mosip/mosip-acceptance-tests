package io.mosip.ivv.parser;

import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.exceptions.RigInternalError;

import java.util.ArrayList;
import java.util.HashMap;

public interface ParserInterface {

    ArrayList<Persona> getPersonas() throws RigInternalError;

    ArrayList<Scenario> getScenarios();

    ArrayList<RegistrationUser> getRCUsers();

    ArrayList<Partner> getPartners();

    ArrayList<ProofDocument> getDocuments();

    HashMap<String, String> getGlobals();

    HashMap<String, String> getConfigs();

}
