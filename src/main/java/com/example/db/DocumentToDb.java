package com.example.db;

import java.util.List;
import java.util.UUID;


public interface DocumentToDb {

	Boolean createDocumentsTable();
	
	Boolean saveDocument(DocumentModel document);
	
	List<DocumentModel> getAllDocuments();
	
	DocumentModel getDocumentFromDB(UUID uuid);
}
