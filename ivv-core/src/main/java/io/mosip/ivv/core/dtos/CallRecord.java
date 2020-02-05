package io.mosip.ivv.core.dtos;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallRecord {
    public enum record_types {
        HTTP, SDK
    }
    private record_types type = record_types.HTTP;
    private String url = "";
    private String method = "";
    private String inputData = "";
    private String result = "";
    private Response response = null;
    private String status = "";
    private int statusCode;

    public CallRecord(String url, String method, String input_data, Response response) {
        this.url = url;
        this.method = method;
        this.inputData = input_data;
        this.response = response;
        this.statusCode = response.getStatusCode();
    }

    public CallRecord(record_types type, String method, String input_data, String result) {
        this.type = type;
        this.method = method;
        this.inputData = input_data;
        this.result = result;
    }
}
