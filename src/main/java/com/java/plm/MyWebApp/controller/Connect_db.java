package com.java.plm.MyWebApp.controller;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class Connect_db {

	final static Logger logger = Logger. getLogger(Connect_db.class);

	public Connection getPLM2Connection() {
		Connection con=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			//con = DriverManager.getConnection("jdbc:oracle:thin:@10.62.134.26:1521/DPLM.WORLD", "PLM2_DBO", "PLM2_DBO"); // DEV
			//con = DriverManager.getConnection("jdbc:oracle:thin:@10.62.134.36:1521/QPLM.WORLD", "CEC_DBO", "CEC_DBO_2018"); // QA
			con = DriverManager.getConnection("jdbc:oracle:thin:@catl0plor00007.corp.cox.com:1521/PPLM.WORLD", "b45752", "B45752#pass"); // PLM2 Prod
		} catch(Exception e) {
			//PLMUtility.errorsLst.add(new String[] {"Connection to PLM2 database failed!! " , e.getMessage()});
			logger.error("Connection to PLM2 database failed!! " + e.getMessage());
		}
		return con;        
	}

	public Connection getPLM1Connection() {
		Connection con=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@catl0plor00007.corp.cox.com:1521/PPLM_APP.WORLD", "b45752", "B45752#pass"); // PLM1 Prod
		} catch(Exception e) {
			//PLMUtility.errorsLst.add(new String[] {"Connection to PLM1 database failed!! ", e.getMessage()});
			logger.error("Connection to PLM1 database failed!! " + e.getMessage());
		}
		return con;        
	}

	public Connection getPStageConnection() {
		Connection con=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@dukedmps10-scan.corp.cox.com:1521/PSTAGE_ALL_USERS.WORLD", "b45752", "Capjan20"); // P-Stage
			logger.info("Connection to P-Stage database (dukedmps10) successful.");
		} catch(Exception e) {
			logger.info("Connection to P-Stage database (dukedmps10) failed!! " + e.getMessage());
		}
		try {
			logger.info("Trying to connect to P-Stage database (dukedmps11)... ");
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@dukedmps11-scan.corp.cox.com:1521/PSTAGE_ALL_USERS.WORLD", "b45752", "Capjan20"); // P-Stage
			logger.info("Connection to P-Stage database (dukedmps11) successful.");
		} catch(Exception e) {
			logger.info("Connection to P-Stage database (dukedmps11) failed!! " + e.getMessage());
		}
		try {
			logger.info("Trying to connect to P-Stage database (dukedmps12)... ");
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@dukedmps12-scan.corp.cox.com:1521/PSTAGE_ALL_USERS.WORLD", "b45752", "Capjan20"); // P-Stage
			logger.info("Connection to P-Stage database (dukedmps12) successful.");
		} 
		catch(Exception e) {
			//PLMUtility.errorsLst.add(new String[] {"Connection to P-Stage database (dukedmps10/dukedmps11/dukedmps12) failed!! ", e.getMessage()});
			logger.error("Connection to P-Stage database (dukedmps12) failed!! " + e.getMessage());
		}
		return con;        
	}

	public Connection getPinpointConnection() {
		Connection con=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection("jdbc:oracle:thin:@oscar-duke2.corp.cox.com:1521/POSCAR_APP.WORLD", "b45752", "oscar_b45752"); // Pinpoint
		} catch(Exception e) {
			//PLMUtility.errorsLst.add(new String[] {"Connection to Pinpoint database failed!! ", e.getMessage()});
			logger.error("Connection to Pinpoint database failed!! " + e.getMessage());
		}
		return con;        
	}
}