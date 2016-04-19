package com.example.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.config.DataBaseConfig;

@Component
public class DocumentToDBImpl implements DocumentToDb {

	private DataBaseConfig dbConfig;

	@Autowired
	public DocumentToDBImpl(DataBaseConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	@Override
	public Boolean saveDocument(DocumentModel document) {
		Connection connection = dbConfig.getConnection();
		String saveQuery = "INSERT INTO documents (uuid, created) VALUES ('" + document.getDocumentUUID() + "', '"
				+ document.getCreationDate() + "');";
		Statement stm = null;
		try {
			stm = connection.createStatement();
			stm.executeUpdate(saveQuery);
			connection.commit();
		} catch (SQLException e) {
			System.out.println("SQL error while saving the document to db" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				stm.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public List<DocumentModel> getAllDocuments() {
		Connection connection = dbConfig.getConnection();
		String selectQuery = "SELECT * FROM documents";
		List<DocumentModel> retrievedDocs = new ArrayList<>();
		Statement stm = null;
		try {
			stm = connection.createStatement();
			ResultSet rs = stm.executeQuery(selectQuery);
			while (rs.next()) {
				retrievedDocs.add(new DocumentModel(rs.getString("uuid"), rs.getString("created")));
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("SQL error while getting all documents from db" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				stm.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retrievedDocs;
	}

	@Override
	public DocumentModel getDocumentFromDB(UUID uuid) {
		Connection connection = dbConfig.getConnection();
		String selectQuery = "SELECT * FROM documents where uuid = '" + uuid + "'";
		Statement stm = null;
		DocumentModel foundDoc = null;
		try {
			stm = connection.createStatement();
			ResultSet rs = stm.executeQuery(selectQuery);
			if (rs.next()) {
				foundDoc = new DocumentModel(rs.getString("uuid"), rs.getString("created"));
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println("SQL error while getting the document from db" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				stm.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return foundDoc;
	}

	@Override
	public Boolean createDocumentsTable() {
		Connection connection = dbConfig.getConnection();
		String createTable = "CREATE TABLE IF NOT EXISTS documents (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid varchar(100), created varchar(100))";
		Statement stm = null;
		try {
			stm = connection.createStatement();
			stm.execute(createTable);
			connection.commit();

		} catch (SQLException e) {
			System.out.println("SQL error while creating the datatable in db" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				stm.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;

	}

}
