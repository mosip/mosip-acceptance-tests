package io.mosip.ivv.dg;

import io.mosip.ivv.core.dtos.Persona;
import io.mosip.ivv.core.dtos.Scenario;

import java.util.ArrayList;
import java.util.HashMap;

public interface DataGeneratorInterface {
    ArrayList<Scenario> prepareScenarios(ArrayList<Scenario> scenarios, ArrayList<Persona> personas);
}
