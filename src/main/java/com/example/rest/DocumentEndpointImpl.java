package com.example.rest;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.NotFoundException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.config.ElasticClientConfig;
import com.example.db.DocumentModel;
import com.example.db.DocumentToDBImpl;

@EndpointImplementation
public class DocumentEndpointImpl implements DocumentEndpoint {

	private Client elasticClient;
	private DocumentToDBImpl documentToDb;

	@Autowired
	public DocumentEndpointImpl(Client elasticClient, DocumentToDBImpl documentToDB) {
		this.elasticClient = elasticClient;
		this.documentToDb = documentToDB;
	}

	@Override
	public String createDocument() throws Exception {
		UUID uuid = UUID.randomUUID();
		IndexResponse response = elasticClient
				.prepareIndex(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, uuid.toString())
				.setCreate(true).setSource(jsonBuilder().startObject().field("uuid", uuid.toString())
						.field("date", Instant.now()).endObject())
				.get();
		response.isCreated();
		return uuid.toString();
	}

	@Override
	public String getDocument(UUID uuid) throws Exception {
		GetResponse getResponse = elasticClient
				.prepareGet(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, uuid.toString()).get();

		if (getResponse.isExists()) {
			return getResponse.getSourceAsString();
		} else {
			throw new NotFoundException();
		}
	}
	
	@Override
	public String getUUIDAndCreateDocument() throws Exception {
		String uuid = new UUIDGenerator().getExternalUUID();
		System.out.println("uuid!!!! " + uuid);
		IndexResponse response = elasticClient.prepareIndex(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, uuid)
                .setCreate(true)
                .setSource(jsonBuilder().startObject().field("uuid", uuid).field("date", Instant.now()).endObject())
                .get();
       
		return uuid;
		
	}

	@Override
	public String getDocumentFromDB(UUID uuid) throws Exception {
		documentToDb.createDocumentsTable();
		DocumentModel foundDoc = documentToDb.getDocumentFromDB(uuid);
		return foundDoc != null ? foundDoc.toString() : "";
	}

	@Override
	public String createAndSaveDocument() throws Exception {
		UUID uuid = UUID.randomUUID();
		Instant created = Instant.now();
		IndexResponse response = elasticClient
				.prepareIndex(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, uuid.toString())
				.setCreate(true)
				.setSource(
						jsonBuilder().startObject().field("uuid", uuid.toString()).field("date", created).endObject())
				.get();

		if (response.isCreated() == true) {
			documentToDb.createDocumentsTable();
			documentToDb.saveDocument(new DocumentModel(uuid.toString(), created.toString()));
		}

		return uuid.toString();

	}

	@Override
	public String getAllDocuments() {
		documentToDb.createDocumentsTable();
		List<DocumentModel> allDocuments = documentToDb.getAllDocuments();
		String result = "";
		for (DocumentModel document : allDocuments) {
			result += document.getDocumentUUID() + ", " + document.getCreationDate() + ";";
		}
		return result;
	}
}
