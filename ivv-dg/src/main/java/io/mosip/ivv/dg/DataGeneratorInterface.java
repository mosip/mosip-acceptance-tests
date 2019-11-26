package io.mosip.ivv.dg;

import io.mosip.ivv.core.structures.Scenario;

import java.util.ArrayList;
import java.util.HashMap;

public interface DataGeneratorInterface {
    void saveScenariosToDb();
    ArrayList<Scenario> getScenarios();
    HashMap<String, String> getConfigs();
    HashMap<String, String> getGlobals();
}
