package io.mosip.ivv.core.logger;

public class Logger {

    public enum types {
        DEBUG, WARN, ERROR, INFO
    }

    public void log(String log_level, String msg){

    }

    public void info(String msg){

    }

    public void error(String msg){

    }

    public void warn(String msg){

    }

    public void debug(String msg){

    }
    public static java.util.logging.Logger auditLog = java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
}
