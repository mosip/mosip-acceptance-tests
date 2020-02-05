package io.mosip.ivv.core.dtos;

import com.aventstack.extentreports.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtentLogger {
    private Status type;
    private String msg;
    public ExtentLogger(Status t, String m){
        this.type = t;
        this.msg = m;
    }
}
