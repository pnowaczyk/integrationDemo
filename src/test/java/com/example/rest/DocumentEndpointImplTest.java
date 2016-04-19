package com.example.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;

import javax.ws.rs.NotFoundException;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.example.config.DataBaseConfig;
import com.example.config.ElasticClientConfig;
import com.example.db.DocumentToDBImpl;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Files;

public class DocumentEndpointImplTest {

	private File tmpDir;
	private Node node;
	private Client client;

	private DocumentEndpointImpl endpoint;
	private DocumentToDBImpl documentToDb;
	private DataBaseConfig dataBaseConfig;
	private Connection dbConnection;

	@Before
	public void setUp() {
		tmpDir = Files.createTempDir();
		node = nodeBuilder().local(true).settings(Settings.builder().put("path.home", tmpDir.getAbsolutePath()))
				.clusterName("integrationCluster").node();
		client = node.client();
		// documents for tests
		client.admin().indices().create(Requests.createIndexRequest(ElasticClientConfig.INDEX_NAME)).actionGet();
		client.admin().indices().refresh(new RefreshRequest()).actionGet();

		String databaseName = "Test_database.db";
		String driverName = "org.sqlite.JDBC";
		String databasePrefix = "jdbc:sqlite:";
		dataBaseConfig = new DataBaseConfig(driverName, databasePrefix, databaseName);
		dbConnection = dataBaseConfig.getConnection();
		documentToDb = new DocumentToDBImpl(dataBaseConfig);
		endpoint = new DocumentEndpointImpl(client, documentToDb);
		Instant created = Instant.now();
		String createTable = "CREATE TABLE IF NOT EXISTS documents (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid varchar(100), created varchar(100))";
		String insertTestData1 = "INSERT INTO documents (uuid, created) VALUES ('61f1acb2-5522-4123-a521-41f32252e803', '"
				+ created.toString() + "');";
		String insertTestData2 = "INSERT INTO documents (uuid, created) VALUES ('83772784-152c-4d3c-aa9f-bd26ca2a499f', '"
				+ created.toString() + "');";
		String insertTestData3 = "INSERT INTO documents (uuid, created) VALUES ('7481685d-d5ba-4d6d-834f-1c52e97fc892', '"
				+ created.toString() + "');";
		Statement stm = null;
		try {
			stm = dbConnection.createStatement();
			stm.execute(createTable);
			stm.executeUpdate(insertTestData1);
			stm.executeUpdate(insertTestData2);
			stm.executeUpdate(insertTestData3);
			dbConnection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				stm.close();
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		client.admin().indices().delete(Requests.deleteIndexRequest(ElasticClientConfig.INDEX_NAME)).actionGet();
		node.close();
		tmpDir.delete();
		Statement stm = null;
		try {
			stm = dbConnection.createStatement();
			stm.executeUpdate("DROP TABLE documents");
			dbConnection.commit();
			stm.close();
		} catch (SQLException e) {
			System.out.println("SQL error " + e.getMessage());
		}
		dbConnection.close();
	}

	@Test
	public void endpointCreatesDocuments() throws Exception {
		String name = endpoint.createDocument();
		assertThat(name).isNotEmpty();
		assertThat(endpoint.getDocument(UUID.fromString(name))).isNotEmpty();
		GetResponse document = client.prepareGet(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, name)
				.get();
		String foundName = (String) document.getSource().get("uuid");
		assertThat(foundName).isEqualTo(name);

	}

	@Test
	public void getCreatedDocument() throws Exception {
		UUID uuid = UUID.randomUUID();
		IndexResponse response = client
				.prepareIndex(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, uuid.toString())
				.setCreate(true).setSource(jsonBuilder().startObject().field("uuid", uuid.toString())
						.field("date", Instant.now()).endObject())
				.get();

		GetResponse document = client
				.prepareGet(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, uuid.toString()).get();
		assertThat(endpoint.getDocument(uuid)).isNotEmpty();
	}

	@Ignore
	@Test
	public void getDocumentThatDoesNotExist() throws Exception {
		UUID uuid = UUID.fromString("61f1acb2-5522-4123-a521-41f32252e803");
		assertThatThrownBy(() -> {
			endpoint.getDocument(uuid);
		}).isInstanceOf(NotFoundException.class);
	}

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

	@Test
	public void endpointGetsMockUUIDAndCreatesDocuments() throws Exception {
		stubFor(get(urlEqualTo("/uuid")).willReturn(aResponse().withStatus(200)
				.withHeader("Content-Type", "application/json").withBody("61f1acb2-5522-4123-a521-41f32252e803")));

		String name = endpoint.getUUIDAndCreateDocument();
		assertThat(name).isNotEmpty();
		assertThat(name).isEqualTo("61f1acb2-5522-4123-a521-41f32252e803");
		assertThat(endpoint.getDocument(UUID.fromString(name))).isNotEmpty();
		GetResponse document = client.prepareGet(ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME, name)
				.get();
		String foundName = (String) document.getSource().get("uuid");
		assertThat(foundName).isEqualTo(name);

	}

	@Test
	public void saveCreatedFileToDB() throws Exception {

		String initialState = endpoint.getAllDocuments();
		String[] initialDocsNbr = initialState.split(";");
		assertThat(initialDocsNbr.length).isEqualTo(3);
		String createdDocUUID = endpoint.createAndSaveDocument();
		String stateAfterDocCreation = endpoint.getAllDocuments();
		String[] stateAfterDocCreationNbr = stateAfterDocCreation.split(";");
		assertThat(stateAfterDocCreationNbr.length).isEqualTo(4);
		String foundDocWithCreatedUUID = endpoint.getDocumentFromDB(UUID.fromString(createdDocUUID));
		String foundDocUUID = foundDocWithCreatedUUID.split(", ")[0];
		assertThat(foundDocUUID).isNotEqualTo("");
		assertThat(foundDocUUID).isEqualTo(createdDocUUID);

	}

}