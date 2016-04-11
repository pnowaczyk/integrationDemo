package com.example.rest;

import static org.assertj.core.api.Assertions.*;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.node.Node;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.example.config.ElasticClientConfig;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Files;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DocumentEndpointImplTest {

	private File tmpDir;
	private Node node;
	private Client client;

	private DocumentEndpointImpl endpoint;


	@Before
	public void setUp() {
	   tmpDir = Files.createTempDir();
	   node = nodeBuilder()
	         .local(true)
	         .settings(
	               Settings.builder().put("path.home",
	                     tmpDir.getAbsolutePath()))
	         .clusterName("integrationCluster").node();
	   client = node.client();
	   // documents for tests
	   client.admin().indices().create(Requests.createIndexRequest(ElasticClientConfig.INDEX_NAME)).actionGet();
	   client.admin().indices().refresh(new RefreshRequest()).actionGet();

	   endpoint = new DocumentEndpointImpl(client);
	}
	
	
	

	@After
	public void tearDown() throws Exception {
		client.admin()
				.indices()
				.delete(Requests
						.deleteIndexRequest(ElasticClientConfig.INDEX_NAME))
				.actionGet();
		node.close();
		tmpDir.delete();
	}

	@Test
	public void endpointCreatesDocuments() throws Exception {
		String name = endpoint.createDocument();
		assertThat(name).isNotEmpty();
		assertThat(endpoint.getDocument(UUID.fromString(name))).isNotEmpty();
		GetResponse document = client.prepareGet(
				ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME,
				name).get();
		String foundName = (String) document.getSource().get("uuid");
		assertThat(foundName).isEqualTo(name);

	}

	// tworzê dokument przez client i sprawdze czy get go zwróci

	@Test
	public void getCreatedDocument() throws Exception {
		UUID uuid = UUID.randomUUID();
		IndexResponse response = client
				.prepareIndex(ElasticClientConfig.INDEX_NAME,
						ElasticClientConfig.TYPE_NAME, uuid.toString())
				.setCreate(true)
				.setSource(
						jsonBuilder().startObject()
								.field("uuid", uuid.toString())
								.field("date", Instant.now()).endObject())
				.get();

		GetResponse document = client.prepareGet(
				ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME,
				uuid.toString()).get();
		assertThat(endpoint.getDocument(uuid)).isNotEmpty();
	}

	// poprosiæ o dokument, którego nie ma i czy rzuci not found exception


	
	@Test
	public void getDocumentThatDoesNotExist() throws Exception {
	   UUID uuid = UUID.fromString("61f1acb2-5522-4123-a521-41f32252e803");
	   assertThatThrownBy(() -> { endpoint.getDocument(uuid);
	}).isInstanceOf(NotFoundException.class);
	}
	
//	// pobierac randomowy UUID przez wireMock
//
//	
//	  @Rule public WireMockRule wireMockRule = new WireMockRule(8090);
//	  
//	  @Test public void endpointGetsMockUUIDAndCreatesDocuments() throws
//	  Exception { stubFor(get(urlEqualTo("/uuid")).willReturn(
//	  aResponse().withStatus(200).withHeader("Content-Type",
//	  "application/json").withBody("61f1acb2-5522-4123-a521-41f32252e803")));
//	  
//	  
//	  
//	  String name = endpoint.getUUIDAndCreateDocument();
//	  assertThat(name).isNotEmpty();
//	  assertThat(name).isEqualTo("61f1acb2-5522-4123-a521-41f32252e803");
//	  assertThat(endpoint.getDocument(UUID.fromString(name))).isNotEmpty();
//	  GetResponse document = client.prepareGet(ElasticClientConfig.INDEX_NAME,
//	  ElasticClientConfig.TYPE_NAME, name).get(); String foundName = (String)
//	  document.getSource().get("uuid"); assertThat(foundName).isEqualTo(name);
//	  
//	  }
	 

//	// pobierac randomowy UUID przez inn¹ aplikacja (aplikacja GeneratorUUID)
//
//	
//	 @Test public void endpointGetsExternalUUIDAndCreatesDocuments() throws
//	 Exception { String name = endpoint.getUUIDAndCreateDocument();
//	 assertThat(name).isNotEmpty();
//	 assertThat(endpoint.getDocument(UUID.fromString(name))).isNotEmpty();
//	 GetResponse document = client.prepareGet(ElasticClientConfig.INDEX_NAME,
//	 ElasticClientConfig.TYPE_NAME, name).get(); String foundName = (String)
//	 document.getSource().get("uuid"); assertThat(foundName).isEqualTo(name);
//	 
//	 }
	


    
	// umiescic dokument w bazie danych i sprobowac i pobrac ten dokument
	// (sqlLite)
	
	@Test
	public void getSavedDocumentFromDB() throws Exception{
		// create a document
		UUID uuid = UUID.randomUUID();
		IndexResponse response = client
				.prepareIndex(ElasticClientConfig.INDEX_NAME,
						ElasticClientConfig.TYPE_NAME, uuid.toString())
				.setCreate(true)
				.setSource(
						jsonBuilder().startObject()
								.field("uuid", uuid.toString())
								.field("date", Instant.now()).endObject())
				.get();

		GetResponse document = client.prepareGet(
				ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME,
				uuid.toString()).get();
		GetResponse createdDocument = client.prepareGet(
				ElasticClientConfig.INDEX_NAME, ElasticClientConfig.TYPE_NAME,
				uuid.toString()).get();
			String creationDate = (String) document.getSource().get("date");
		
		//prepare connection to DB
		//create a db and a table
		Connection c = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:test.db");
	      c.setAutoCommit(false);
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    
	    }
	    System.out.println("Opened database successfully");
		
		
		
		
	    Statement stm = null;
	  
	    try{
	    	stm = c.createStatement();
	    
	    	String createTable = "CREATE TABLE documents (id INTEGER PRIMARY KEY, uuid varchar(100), created varchar(100))";
	    	stm.execute(createTable);
	    
	    	//insert the document uuid to DB
	    	String sql = "INSERT INTO documents (id, uuid, created) " +
	                "VALUES (1, '" + uuid.toString() + "', '" + creationDate + "');"; 
	    	System.out.println(sql);
	      stm.executeUpdate(sql);
	      c.commit();
	    	
	    }
	    catch(SQLException e){
	    	System.out.println("SQL error " + e.getMessage());
	    }
		

	
		
		//retrieve the document data from the DB
	    ResultSet rs = stm.executeQuery( "SELECT * FROM documents where id = 1;" );
	   System.out.println("uuid " + uuid.toString());
	   // assertThat(rs.getFetchSize()).isEqualTo(1);
	    String retrievedUUID = rs.getString("uuid");
	    System.out.println("retrievedUUID " + retrievedUUID);
	    String retrivedCreationDate = rs.getString("created");
	    System.out.println("retrivedCreationDate " + retrivedCreationDate);
	    rs.close();
	     assertThat(retrievedUUID).isEqualTo(uuid.toString());
	    assertThat(retrivedCreationDate).isEqualTo(creationDate);
	    assertThat(endpoint.getDocument(UUID.fromString(retrievedUUID))).isNotEmpty();
	    
		
	    //delete table after test
	    try{  stm.executeUpdate("DROP TABLE documents");
	    c.commit();
	    }
	    catch(SQLException e){
	    	System.out.println("SQL error " + e.getMessage());
	    }
		
	  
	   
	    //close db connection
	    stm.close();
	      c.commit();
	      c.close();
	}
}