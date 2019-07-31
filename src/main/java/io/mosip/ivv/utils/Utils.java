/*
 * author: Channakeshava
 */

package main.java.io.mosip.ivv.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.restassured.response.Response;
import main.java.io.mosip.ivv.base.BaseHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static main.java.io.mosip.ivv.base.BaseHelper.*;

public class Utils {
    public static Boolean dbSearchStatus;
    public static Properties prop;
    public static String folderPath;
    public static File theDir;
    private static List<String> fileList;
    public static Logger auditLog = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static Logger requestAndresponseLog = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static Connection dbConnect(String dbUrl, String dbUser, String dbpassword) {
        Connection dbConnectionObject = null;
        try {
            dbConnectionObject = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            Utils.auditLog.severe(e.getMessage());
        }
        return dbConnectionObject;
    }

    public static void dbVerification(String sqlStatement, ExtentTest extentTest, Boolean shouldExists) throws SQLException {
        dbSearchStatus = false;
        Connection dbConnection = dbConnect(dbAuditLogDBPath, dbUser, dbPassword);
        Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet resultSet = statement.executeQuery(sqlStatement);
        resultSet.beforeFirst();
        resultSet.last();

        if (shouldExists) {
            if (resultSet.getRow() > 0) {
                extentTest.log(Status.PASS, "DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
                Utils.auditLog.info("DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
            } else {
                extentTest.log(Status.FAIL, "DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
                Utils.auditLog.severe("DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
            }

        } else {
            if (resultSet.getRow() <= 0) {
                extentTest.log(Status.PASS, "DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
                Utils.auditLog.info("DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
            } else {
                extentTest.log(Status.FAIL, "DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
                Utils.auditLog.severe("DB-Audit Logs: " + resultSet.getString("even_name") +
                        resultSet.getString("log_desc"));
            }
        }

        resultSet.close();
        statement.close();
        dbConnection.close();
    }

    public static void dbAuditLogVerification(Response response, ExtentTest extentTest) throws SQLException {
        String responsetimeStamp = null;
        String[] responsetimeStampFrom = null;
        String responsetimeStampFromTime = null;
        String responsetimeStampToTime = null;
        String[] responsetimeStampTemp1 = null;
        String[] responsetimeStampTemp2 = null;

        if (tableAppNameValue != null) {
            ReadContext ctx = JsonPath.parse(response.getBody().asString());
            responsetimeStamp = ctx.read("responsetime").toString().replace("T", " ");
            responsetimeStampFrom = responsetimeStamp.split("\\.");
            responsetimeStampTemp1 = responsetimeStampFrom[0].split(":");
            responsetimeStampTemp2 = responsetimeStampTemp1[0].split(" ");

            if (tableColName.equals("NoFilter") & tableEventName.equals("DELETE")) {
                if (Integer.parseInt(responsetimeStampTemp1[1]) == 0) {
                    if (Integer.parseInt(responsetimeStampTemp2[1]) == 0) {
                        responsetimeStampFromTime = responsetimeStampTemp2[0].trim() + " 23:59" + responsetimeStampTemp1[2];
                    } else {
                        responsetimeStampFromTime = responsetimeStampTemp1[0] + ":"
                                + (Integer.parseInt(responsetimeStampTemp1[1]) - 1) + ":" + responsetimeStampTemp1[2];
                    }
                } else {
                    responsetimeStampFromTime = responsetimeStampTemp1[0] + ":"
                            + (Integer.parseInt(responsetimeStampTemp1[1]) - 1) + ":" + responsetimeStampTemp1[2];
                }
            } else {
                responsetimeStampFromTime = responsetimeStampFrom[0];
            }

            if (Integer.parseInt(responsetimeStampTemp1[1]) >= 59) {
                responsetimeStampTemp2 = responsetimeStampTemp1[0].split(" ");
                responsetimeStampToTime = responsetimeStampTemp2[0].trim() + " "
                        + (Integer.parseInt(responsetimeStampTemp2[1]) + 1) + ":00:"
                        + responsetimeStampTemp1[2] + ".999";
            } else {
                responsetimeStampToTime = responsetimeStampTemp1[0] + ":"
                        + (Integer.parseInt(responsetimeStampTemp1[1]) + 1) + ":" + responsetimeStampTemp1[2] + ".999";
            }

            String queryString = "";
            if (otherUserTest.equals("yes")) {
                if (tableAppNameValue.equals("sendOtp")) {
                    responsetimeStampTemp1 = responsetimeStampToTime.split("\\.");
                    queryString = "select * from kernel.otp_transaction where id = '" + otpOtherEmail_username
                            + "' and status_code = 'OTP_UNUSED' " + "order by generated_dtimes";
                } else {
                    queryString = "select * from audit.app_audit_log where session_user_id = '" + otpOtherEmail_username
                            + "' and app_name = '" + tableAppNameValue + "' "
                            + (tableColName.equals("NoFilter") ? "" : " and module_name = '" + tableColName + "'")
                            + " and event_name in('" + tableEventName + "','EXCEPTION')"
                            + " and log_dtimes BETWEEN '" + responsetimeStampFromTime + ".000" + "' AND '"
                            + responsetimeStampToTime + "' order by log_dtimes desc ";
                }
            } else {
                if (tableAppNameValue.equals("sendOtp")) {
                    responsetimeStampTemp1 = responsetimeStampToTime.split("\\.");
                    queryString = "select * from kernel.otp_transaction where id = '" + otpEmail_username
                            + "' and status_code = 'OTP_UNUSED' " + "order by generated_dtimes";
                } else {
                    queryString = "select * from audit.app_audit_log where session_user_id = '" + otpEmail_username
                            + "' and app_name = '" + tableAppNameValue + "' "
                            + (tableColName.equals("NoFilter") ? "" : " and module_name = '" + tableColName + "'")
                            + " and event_name in('" + tableEventName + "','EXCEPTION')"
                            + " and log_dtimes BETWEEN '" + responsetimeStampFromTime + ".000" + "' AND '"
                            + responsetimeStampToTime + "' order by log_dtimes desc ";
                }
            }

            //+ "' and module_name = '" + tableColName
            dbSearchStatus = false;
            Connection dbConnection;
            if (tableAppNameValue.equals("sendOtp")) {
                dbConnection = dbConnect(dbKernelDBPath, dbUser, dbPassword);
            } else {
                dbConnection = dbConnect(dbAuditLogDBPath, dbUser, dbPassword);
            }

            Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = statement.executeQuery(queryString);
            while (resultSet.next()) {
                if (tableAppNameValue.equals("sendOtp")) {
                    extentTest.log(Status.INFO, "OTP sent as [" + resultSet.getString("otp") + "] @"
                            + resultSet.getString("generated_dtimes") + " with status as '"
                            + resultSet.getString("status_code") + "'");
                    Utils.auditLog.info("OTP sent as [" + resultSet.getString("otp") + "] @"
                            + resultSet.getString("generated_dtimes") + " with status as '"
                            + resultSet.getString("status_code") + "'");
                } else {
                    if (!resultSet.getString("event_name").equals("EXCEPTION")) {
                        extentTest.log(Status.INFO, "DB-Audit Logs: [" + resultSet.getString("event_name") +
                                "] " + resultSet.getString("log_desc"));
                        Utils.auditLog.info("DB-Audit Logs: [" + resultSet.getString("event_name") +
                                "] " + resultSet.getString("log_desc"));
                    } else {
                        extentTest.log(Status.INFO, "DB-Audit Logs: [" + resultSet.getString("event_name") +
                                "] " + resultSet.getString("log_desc"));
                        Utils.auditLog.info("DB-Audit Logs: [" + resultSet.getString("event_name") +
                                "] " + resultSet.getString("log_desc"));
                    }
                }
            }
            Utils.auditLog.info("[SQL]" + queryString);
            Utils.auditLog.info("-----------------------------------------------------------------------------------------------------------------");

            resultSet.close();
            statement.close();
            dbConnection.close();
        }
    }

    public static void deleteDirectoryPath(String path) {
        File file = new File(path);
        if (file.exists()) {
            do {
                deleteIt(file);
            } while (file.exists());
        } else {
        }
    }

    public static void copyFileFromTo(String sourceFile, String destinationFolder) throws IOException {
        System.err.println("Source File: " + sourceFile);
        System.err.println("Destination Folder: " + destinationFolder);
        Path sourcePath = Paths.get(sourceFile);
        Path destinationPath = Paths.get(destinationFolder);
        System.err.println("S. Path: " + sourcePath);
        System.err.println("D. Path: " + destinationPath);
        Files.copy(sourcePath, destinationPath);
    }

    public static void deleteFile(String destinationFile) throws IOException {
        Path destinationPath = Paths.get(destinationFile);
        Files.delete(destinationPath);
    }

    private static void deleteIt(File file) {
        if (file.isDirectory()) {
            String fileList[] = file.list();
            if (fileList.length == 0) {
                file.delete();
            } else {
                int size = fileList.length;
                for (int i = 0; i < size; i++) {
                    String fileName = fileList[i];
                    String fullPath = file.getPath() + "/" + fileName;
                    File fileOrFolder = new File(fullPath);
                    deleteIt(fileOrFolder);
                }
            }
        } else {
            file.delete();
        }
    }

    public static String createDirectory(String directoryName, String folderName) {
        folderPath = directoryName + folderName;
        theDir = new File(folderPath);
        if (!theDir.exists()) {
            try {
                theDir.mkdirs();
            } catch (SecurityException se) {
            }
        }
        return folderPath;
    }

    public static String getCurrentDate() {
        DateFormat format = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        return format.format(date).toString();
    }

    public static String getCurrentDateAndTime() {
        DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        Date date = new Date();
        return format.format(date).toString();
    }

    public static String getCurrentDateAndTimeForAPI() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date date = new Date();
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    public static String getInvalidCurrentDateAndTimeForAPI() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date date = new Date();
        return format.format(date).toString();
    }

    public static void zipTheFolder(String SOURCE_FOLDER, String OUTPUT_ZIP_FILE) {
        fileList = new ArrayList<String>();
        byte[] buffer = new byte[1024];
        generateFileList(new File(SOURCE_FOLDER), SOURCE_FOLDER);

        String source = new File(SOURCE_FOLDER).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(OUTPUT_ZIP_FILE);
            zos = new ZipOutputStream(fos);
            FileInputStream in = null;

            for (String file : fileList) {
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }
            zos.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void generateFileList(File node, String SOURCE_FOLDER) {
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(), SOURCE_FOLDER));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename), SOURCE_FOLDER);
            }
        }
    }

    private static String generateZipEntry(String file, String SOURCE_FOLDER) {
        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
    }

    public static void setupLogger() {
        LogManager.getLogManager().reset();
        auditLog.setLevel(Level.ALL);

        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new CustomizedLogFormatter());
        ch.setLevel(Level.ALL);
        auditLog.addHandler(ch);
        try {
            FileHandler fh = new FileHandler(BaseHelper.auditLogFile, true);
            fh.setFormatter(new CustomizedLogFormatter());
            fh.setLevel(Level.ALL);
            auditLog.addHandler(fh);
        } catch (IOException e) {
            auditLog.log(Level.SEVERE, "File logger not working", e);
        }
    }

    public static void logRequestAndResponseContent(String requestResponseLogFile) {
        LogManager.getLogManager().reset();
        requestAndresponseLog.setLevel(Level.ALL);
        try {
            FileHandler fh = new FileHandler(requestResponseLogFile, true);
            fh.setFormatter(new CustomizedLogFormatter());
            fh.setLevel(Level.ALL);
            requestAndresponseLog.addHandler(fh);
        } catch (IOException e) {
            requestAndresponseLog.log(Level.SEVERE, "File logger not working", e);
        }
    }

    public static void writeRequestAndResponseToFile(String personaDirectory, String fileName, StringWriter requestAndresponseWriter) {
        BufferedWriter bufferedWriter = null;
        try {
            File myFile = new File(personaDirectory + fileName.replace(":", "_"));
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
            Writer writer = new FileWriter(myFile);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(requestAndresponseWriter.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null)
                    bufferedWriter.close();
            } catch (Exception ex) {

            }
        }
    }

    public static String getEndDayOfWeek() {
        Calendar c = GregorianCalendar.getInstance();

        switch (weekStartDay.toUpperCase()) {
            case "SUNDAY":
                c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case "MONDAY":
                c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case "TUESDAY":
                c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                break;
            case "WEDNESDAY":
                c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                break;
            case "THURSDAY":
                c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                break;
            case "FRIDAY":
                c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                break;
            default:
                c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                break;
        }
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = "", endDate = "";
        startDate = df.format(c.getTime());
        c.add(Calendar.DATE, 6);
        endDate = df.format(c.getTime());
        return endDate;
    }

    public static String assertResponseStatus(String statusCode) {
        String returnString = null;

        switch (Integer.parseInt(statusCode)) {
            case 200: // Successful or OK
                returnString = "OK, The request has succeeded";
                break;
            case 201: // Created
                returnString = "Created! The request has been fulfilled and resulted in a new resource being created";
                break;
            case 204: // Created
                returnString = "No Content! The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation";
                break;
            case 304: // Created
                returnString = "Not Modified!";
                break;
            case 400: // Bad Request
                returnString = "Bad Request! The request could not be understood by the server due to malformed syntax";
                break;
            case 401: // Unauthorized
                returnString = "Unauthorized! The request requires user authentication";
                break;
            case 403: // Forbidden
                returnString = "Forbidden! The server understood the request, but is refusing to fulfill it";
                break;
            case 404: // Not found
                returnString = "Not Found! The server has not found anything matching the Request-URI";
                break;
            case 405: // Method not allowed
                returnString = "Method not allowed! The method specified in the Request-Line is not allowed for the resource identified by the Request-URI.";
                break;
            case 409: // Conflict
                returnString = "Conflict! The request could not be completed due to a conflict with the current state of the resource.";
                break;
            case 413:
                returnString = "MOS-27850: 413 Request Entity Too Large";
                break;
            case 500: // Internal Server Error
                returnString = "Internal Server Error! The server encountered an unexpected condition which prevented it from fulfilling the request";
                break;
            case 503: // Service Unavailable
                returnString = "Service Unavailable! The server is currently unable to handle the request due to a temporary overloading or maintenance of the server";
                break;
        }
        return returnString;
    }
}