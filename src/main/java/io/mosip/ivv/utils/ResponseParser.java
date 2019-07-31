package main.java.io.mosip.ivv.utils;

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.CoreStructures;
import main.java.io.mosip.ivv.base.Persona;
import main.java.io.mosip.ivv.base.ProofDocument;
import main.java.io.mosip.ivv.orchestrator.Scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseParser {
    public CoreStructures.Slot slot(String rawData) {
        Gson gsn = new Gson();
        Map<String, Object> resMap = gsn.fromJson(rawData, Map.class);
        try {
            List center_details = (List) ((Map) resMap.get("response")).get("centerDetails");
            if (center_details.size() == 0) {
                return new CoreStructures.Slot();
            }
            for (Object center_info : center_details) {
                String date = (String) ((Map) center_info).get("date");
                List timeSlots = (List) ((Map) center_info).get("timeSlots");
                if (timeSlots.size() > 0) {
                    String from = (String) ((Map) timeSlots.get(0)).get("fromTime");
                    String to = (String) ((Map) timeSlots.get(0)).get("toTime");
                    return new CoreStructures.Slot(date, from, to);
                }
            }
        } catch (NullPointerException ne) {
            // TO Do
        }
        return new CoreStructures.Slot();
    }

    public static HashMap<String, Object> addDocumentResponseParser(int docIndex, ProofDocument proofDocument, Response api_response) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("status", Status.FAIL);
        hm.put("msg", "No Documents uploaded");

        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());

        HashMap<String, String> documents = ctx.read("$['response']['documentsMetaData'][" + docIndex + "]");
        for (int i = 0; i < documents.size(); i++) {
            if (proofDocument.doc_id.equals(documents.get("documentId"))) {
                hm.put("status", Status.INFO);
                hm.put("msg", "Document '" + proofDocument.name.toString().trim() + "' uploaded");
            }
        }
        return hm;
    }


    public static HashMap<String, Object> addApplicationResponceParser(Persona person, Response api_response) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("status", Status.FAIL);
        hm.put("msg", "No Application Create");

        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']");
        if (!person.phone.equals(app_info.get("phone"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Application Created");
            return hm;
        }
        if (!person.cnie_number.equals(app_info.get("CNIENumber"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Expected: " + person.cnie_number + ", Actual: " + app_info.get("CNIENumber") + " applicatoin response matching");
            return hm;
        }
        if (!person.email.equals(app_info.get("email"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Expected: " + person.email + ", Actual: " + app_info.get("email") + " applicatoin response matching");
            return hm;
        }
        if (!person.postal_code.equals(app_info.get("postalCode"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Expected: " + person.postal_code + ", Actual: " + app_info.get("postalCode") + " applicatoin response matching");
            return hm;
        }
        return hm;
    }

    public static HashMap<String, Object> addApplicationAllResponceParser(List<Persona> person, Response api_response) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("status", Status.FAIL);
        hm.put("msg", "No Application Create");

        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
        HashMap<String, String> app_info = ctx.read("$['response']");
        for (int i = 0; i < person.size(); i++) {
            if (!person.get(i).phone.equals(app_info.get("phone"))) {
                hm.put("status", Status.INFO);
                hm.put("msg", "Application Created");
                return hm;
            }
            if (!person.get(i).cnie_number.equals(app_info.get("CNIENumber"))) {
                hm.put("status", Status.INFO);
                hm.put("msg", "Expected: " + person.get(i).cnie_number + ", Actual: " + app_info.get("CNIENumber") + " applicatoin response matching");
                return hm;
            }
            if (!person.get(i).email.equals(app_info.get("email"))) {
                hm.put("status", Status.INFO);
                hm.put("msg", "Expected: " + person.get(i).email + ", Actual: " + app_info.get("email") + " applicatoin response matching");
                return hm;
            }
            if (!person.get(i).postal_code.equals(app_info.get("postalCode"))) {
                hm.put("status", Status.INFO);
                hm.put("msg", "Expected: " + person.get(i).postal_code + ", Actual: " + app_info.get("postalCode") + " applicatoin response matching");
                return hm;
            }
        }
        return hm;
    }

    public static HashMap<String, Object> updateApplicationResponceParser(Persona person, Response api_response) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("status", Status.FAIL);
        hm.put("msg", "No Application Updated");

        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
        HashMap<String, String> udpate_info = ctx.read("$['response']");
        if (!person.phone.equals(udpate_info.get("phone"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Application Updated");
            return hm;
        }
        if (!person.date_of_birth.equals(udpate_info.get("dateOfBirth"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Expected: " + person.date_of_birth + ", Actual: " + udpate_info.get("dateOfBirth") + " applicatoin response matching");
        }
        if (!person.name.equals(udpate_info.get("name"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Expected: " + person.name + ", Actual: " + udpate_info.get("fullName") + " applicatoin response matching");
        }
        if (!person.email.equals(udpate_info.get("email"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Expected: " + person.email + ", Actual: " + udpate_info.get("email") + " applicatoin response matching");
        }
        if (!person.postal_code.equals(udpate_info.get("postalCode"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Expected: " + person.postal_code + ", Actual: " + udpate_info.get("postalCode") + " applicatoin response matching");
        }
        return hm;
    }


    public static HashMap<String, Object> deleteApplicationResponceParser(Persona person, Response api_response) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("status", Status.FAIL);
        hm.put("msg", "No Application Deleted");

        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
        HashMap<String, String> delete_info = ctx.read("$['response']");
        if (person.pre_registration_id.equals(delete_info.get("preRegistrationId"))) {
            hm.put("status", Status.INFO);
            hm.put("msg", "Application Deleted");
            return hm;
        }
        return hm;
    }


    public static HashMap<String, Object> bookAppointmentResponseParser(Persona person, Response api_response) {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("status", Status.INFO);
        hm.put("msg", "book appointment asserted successfully");

        ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
        HashMap<String, String> appointment_info = ctx.read("$['response']");
        if (!person.registration_center_id.equals(appointment_info.get("registration_center_id"))) {
            hm.put("status", Status.FAIL);
            hm.put("msg", "Expected: " + person.registration_center_id + ", Actual: " + appointment_info.get("registration_center_id") + ", registration center id");
            return hm;
        }
        if (!person.slot.date.equals(appointment_info.get("appointment_date"))) {
            hm.put("status", Status.FAIL);
            hm.put("msg", "Expected: " + person.slot.date + ", Actual: " + appointment_info.get("appointment_date") + ", appointment date");
            return hm;
        }
        if (!person.slot.from.contains(appointment_info.get("time_slot_from"))) {
            hm.put("status", Status.FAIL);
            hm.put("msg", "Expected: " + person.slot.from + ", Actual: " + appointment_info.get("time_slot_from") + ", appointment time from");
            return hm;
        }
        if (!person.slot.to.contains(appointment_info.get("time_slot_to"))) {
            hm.put("status", Status.FAIL);
            hm.put("msg", "Expected: " + person.slot.to + ", Actual: " + appointment_info.get("time_slot_to") + ", appointment time to");
            return hm;
        }
        return hm;
    }

}
