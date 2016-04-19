package com.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import org.springframework.stereotype.Component;

@Component
public class DataBaseConfig {

	public DataBaseConfig(String driverName, String databasePrefix, String databaseName) {
		this.driverName = driverName;
		this.databasePrefix = databasePrefix;
		this.databaseName = databaseName;
	}

	private String databaseName = "Prod_database.db";
	private String driverName = "org.sqlite.JDBC";
	private String databasePrefix = "jdbc:sqlite:";
	private Connection dbConnection = null;

	public Connection getConnection() {

		if (dbConnection == null) {
			try {
				Class.forName(driverName);
				this.dbConnection = DriverManager.getConnection(databasePrefix + databaseName);
				this.dbConnection.setAutoCommit(false);
			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage());

			}
			System.out.println("Opened database successfully");
		}

		return dbConnection;
	}
}
