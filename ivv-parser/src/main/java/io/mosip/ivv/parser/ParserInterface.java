package io.mosip.ivv.parser;

import io.mosip.ivv.core.dtos.*;
import io.mosip.ivv.core.exceptions.RigInternalError;

import java.util.ArrayList;
import java.util.HashMap;

public interface ParserInterface {

    ArrayList<Persona> getPersonas() throws RigInternalError;

    ArrayList<Scenario> getScenarios() throws RigInternalError;

    ArrayList<RegistrationUser> getRCUsers() throws RigInternalError;

    ArrayList<Partner> getPartners() throws RigInternalError;

    HashMap<String, String> getGlobals() throws RigInternalError;

    HashMap<String, String> getConfigs() throws RigInternalError;

}
