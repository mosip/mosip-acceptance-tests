package io.mosip.ivv.core.dtos;

public class AuditLogger {
    public enum types {
        ERROR, WARNING, INFO
    };
    private types type;
    private String msg;
    public AuditLogger(types t, String m){
        this.type = t;
        this.msg = m;
    }
}
