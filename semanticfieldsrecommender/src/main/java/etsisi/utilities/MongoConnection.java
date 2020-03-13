package etsisi.utilities;

import java.util.ArrayList;
import java.util.Arrays;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public class MongoConnection {
	private static MongoConnection mongoConnection;
	
	private MongoCredential credential;
	private MongoClient client;
	private String address;
	private int port;
	private String userName;
	private String dbName;
	
	public static MongoConnection getMongoConnection(String address, int port, String userName, String dbName,
			char[] password, boolean authentication) {
		if(mongoConnection == null ) {
			mongoConnection = new MongoConnection(address, port, userName, dbName, password, authentication);
		}
		else {
			if(userName != mongoConnection.getUserName() || dbName != mongoConnection.getDbName() ||
					address != mongoConnection.getAddress() || port != mongoConnection.getPort()) {
				if(authentication)
					mongoConnection.setCredential(MongoCredential.createCredential(userName, dbName, password));
				mongoConnection.setUserName(userName);
				mongoConnection.setDbName(dbName);
				mongoConnection.setAddress(address);
				mongoConnection.setPort(port);
				mongoConnection.createConnection(authentication);
			}
		}
		return mongoConnection;
	}
	
	private MongoConnection(String address, int port, String userName, String dbName,
			char[] password, boolean authentication){
		this.address = address;
		this.port = port;
		this.userName = userName;
		this.dbName = dbName;
		if(authentication) {
			this.credential = MongoCredential.createCredential(userName, dbName, password);
		}
		this.createConnection(authentication);
	}
	
	private void createConnection(boolean authentication) {
		if(authentication) {
			this.client = MongoClients.create(MongoClientSettings.builder()
	                .applyToClusterSettings(builder ->
                    builder.hosts(Arrays.asList(new ServerAddress(this.address, this.port))))
	                .credential(this.credential)
            .build());
		}
		else {
			this.client = MongoClients.create(MongoClientSettings.builder()
	                .applyToClusterSettings(builder ->
                    builder.hosts(Arrays.asList(new ServerAddress(this.address, this.port))))
            .build());
		}
	}

	public MongoCredential getCredential() {
		return credential;
	}

	public void setCredential(MongoCredential credential) {
		this.credential = credential;
	}

	public MongoClient getClient() {
		return client;
	}

	public void setClient(MongoClient client) {
		this.client = client;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public ArrayList<JSONObject> getCollection(String collectionName){
		ArrayList<JSONObject> jsonCollection = new ArrayList<JSONObject>();
		for(Document document : this.getMongoCollection(collectionName).find()) {
			jsonCollection.add(this.documentToJson(document));
		}
		return jsonCollection;
	}
	
	public JSONObject getFirstDocument(String collectionName) {
		return documentToJson(this.getClient().getDatabase(
				this.getDbName()).getCollection(collectionName).find().first());
	}
	
	public JSONObject getLastDocument(String collectionName) {
		return documentToJson(this.getClient().getDatabase(
				this.getDbName()).getCollection(collectionName).find().sort(new Document("_id", -1)).first());
	}
	
	public boolean checkExistenceFilter(String collectionName, Bson filter) {
		MongoCursor<Document> iterator = 
				this.getClient().getDatabase(
						this.getDbName()).getCollection(collectionName).find(filter).iterator();
		if(iterator.hasNext())
			return true;
		return false;
	}
	
	public ArrayList<JSONObject> getDocumentWithFilter(String collectionName, Bson filter) {
		ArrayList<JSONObject> documents = new ArrayList<JSONObject>();
		MongoCursor<Document> iterator = 
				this.getClient().getDatabase(
						this.getDbName()).getCollection(collectionName).find(filter).iterator();
		while(iterator.hasNext())
			documents.add(this.documentToJson(iterator.next()));
		return documents;
	}
	
	public void insertDocument(String collectionName, JSONObject jsonDocument) {
		Document document = Document.parse(jsonDocument.toString());
		this.getMongoCollection(collectionName).insertOne(document);
	}
	
	private JSONObject documentToJson(Document document) {
		if(document != null)
			return new JSONObject(document.toJson());
		return new JSONObject();
	}
	
	private MongoCollection<Document> getMongoCollection(String collectionName){
		return this.getClient().getDatabase(this.getDbName()).getCollection(collectionName);
	}
}
