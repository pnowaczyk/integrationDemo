package com.example.db;

public class DocumentModel {

	@Override
	public String toString() {
		return documentUUID + ", " + creationDate;
	}

	private String documentUUID;
	private String creationDate;

	public DocumentModel(String documentUUID, String creationDate) {
		this.documentUUID = documentUUID;
		this.creationDate = creationDate;
	}

	public String getDocumentUUID() {
		return documentUUID;
	}

	public void setDocumentUUID(String documentUUID) {
		this.documentUUID = documentUUID;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
}
