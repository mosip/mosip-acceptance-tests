package io.mosip.ivv.preregistration.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.aventstack.extentreports.Status;

import io.mosip.ivv.core.dtos.Store;
import io.mosip.ivv.core.policies.AssertionPolicy;
import io.mosip.ivv.core.dtos.CallRecord;
import io.mosip.ivv.core.dtos.ExtentLogger;
import io.mosip.ivv.core.dtos.Scenario;
import io.mosip.ivv.core.utils.Utils;

public abstract class Helpers {
	  private static ArrayList<ExtentLogger> reports = new ArrayList<>();

    public static void logCallRecord(CallRecord c){
        Utils.auditLog.info("EndPoint : "+c.getUrl());
        Utils.auditLog.info("Request PayLoad :"+c.getInputData());
        Utils.auditLog.info("Response :"+c.getResponse().asString());
    }

	public static void dbVerification(String sqlStatement, String colName1, String colName2, String... columns)
			throws SQLException {
		try (   Connection dbConnection = dbConnect();
				Statement statement = dbConnection.createStatement();
				ResultSet resultSet = statement.executeQuery(sqlStatement);
		) {

			if (resultSet != null) {
				while (resultSet.next()) {
					Utils.auditLog.info("Data verified successfully in DB for " + colName1
							+ " := "+resultSet.getString(colName1) + " and " + colName2 + " :=" + resultSet.getString(colName2));
					reports.add(new ExtentLogger(Status.PASS,
							"Data verified successfully in DB for " + colName1 +" "+ resultSet.getString(colName1) + "  and "
									+ colName2 + " " + " "+resultSet.getString(colName2)));
					if (columns.length > 0) {
						for (int i = 0; i < columns.length; i++) {
							Utils.auditLog
									.info("Data verified successfully in DB for : " + resultSet.getString(columns[i]));
							reports.add(new ExtentLogger(Status.PASS,
									"Data verified successfully in DB for : " + resultSet.getString(columns[i])));
						}
					}
				}

			} else {
				Utils.auditLog.info(" DB-Audit Logs:  No record found in DB ");
				reports.add(new ExtentLogger(Status.FAIL, "No record found in DB"));
			}

		} catch (SQLException e) {
			Utils.auditLog.severe(e.getMessage());
		}

	}

    public static Connection dbConnect() {
    	   String dbUrl = "jdbc:postgresql://psql-mosip.southeastasia.cloudapp.azure.com:5432/mosip_prereg";
    	  String dbUser = "TF-user";
    	    String dbPassword = "Techno@123";
        Connection dbConnectionObject = null;
        try {
            dbConnectionObject = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            Utils.auditLog.severe(e.getMessage());
        }
        return dbConnectionObject;
    }

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

	public static String getRequestJson(String name) throws IOException {
		String path = System.getProperty("user.dir")+"/src/test/resources/requests/"+name;
		return Utils.readFileAsString(path);
	}

	public static String getResponseJson(String name) throws FileNotFoundException {
		String path = System.getProperty("user.dir")+"/src/test/resources/responses/"+name;
		return Utils.readFileAsString(path);
	}


}
