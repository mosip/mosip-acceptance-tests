package io.mosip.ivv.regprocessor.utils;

import io.mosip.ivv.core.policies.AssertionPolicy;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.Utils;

import java.sql.SQLException;
import java.util.ArrayList;

public abstract class Helpers {

    public static void logCallRecord(CallRecord c){
        Utils.auditLog.info(c.getUrl());
        Utils.auditLog.info(c.getInputData());
        Utils.auditLog.info(c.getResponse().asString());
    }

    public static void dbVerification(String sqlStatement, Boolean shouldExists) throws SQLException {
//        dbSearchStatus = false;
//        Connection dbConnection = dbConnect();
//        Statement statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//        ResultSet resultSet = statement.executeQuery(sqlStatement);
//        resultSet.beforeFirst();
//        resultSet.last();
//
//        if (shouldExists) {
//            if (resultSet.getRow() > 0) {
//                extentTest.log(Status.PASS, "DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//                Utils.auditLog.info("DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//            } else {
//                extentTest.log(Status.FAIL, "DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//                Utils.auditLog.severe("DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//            }
//
//        } else {
//            if (resultSet.getRow() <= 0) {
//                extentTest.log(Status.PASS, "DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//                Utils.auditLog.info("DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//            } else {
//                extentTest.log(Status.FAIL, "DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//                Utils.auditLog.severe("DB-Audit Logs: " + resultSet.getString("even_name") +
//                        resultSet.getString("log_desc"));
//            }
//        }
//
//        resultSet.close();
//        statement.close();
//        dbConnection.close();
    }

//    public static Connection dbConnect() {
//        Connection dbConnectionObject = null;
//        try {
//            dbConnectionObject = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
//            //Utils.auditLog.info("** Successfully connected to the PostgresSQL server **");
//        } catch (SQLException e) {
//            Utils.auditLog.severe(e.getMessage());
//        }
//        return dbConnectionObject;
//    }

    public static Scenario.Step generateStep(String name, int index){
        Scenario.Step nstep = new Scenario.Step();
        nstep.setName(name);
        nstep.setVariant("DEFAULT");
        nstep.setModule(Scenario.Step.modules.pr);
        nstep.setIndex(new ArrayList<Integer>());
        nstep.getIndex().add(index);
        ArrayList<Scenario.Step.Assert> asserts = new ArrayList<>();
        Scenario.Step.Assert as = new Scenario.Step.Assert();
        as.type = AssertionPolicy.valueOf("DEFAULT");
        asserts.add(as);

        nstep.setAsserts(asserts);
        return nstep;
    }


}
