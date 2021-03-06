package aero.champ.projectpera.repository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import aero.champ.projectpera.util.PropertyFile;

public class MongoDbConnector implements DatabaseConnector {
	
	public static final String CONFIG_FILE = "mongodb.properties";
	public static final String KEY_HOST = "host";
	public static final String KEY_PORT = "port";
	public static final String KEY_DB = "database";
	
	private MongoDatabase database;

	@Override
	public void startConnection() throws Exception {
		PropertyFile propertyFile = new PropertyFile(CONFIG_FILE);
		String host = propertyFile.getValue(KEY_HOST);
		Integer port = Integer.parseInt(propertyFile.getValue(KEY_PORT));
		String databaseName = propertyFile.getValue(KEY_DB);
		
		MongoClient mongoClient = new MongoClient(host, port);
		database = mongoClient.getDatabase(databaseName);
	}

	@Override
	public void endConnection() {
		// TODO Auto-generated method stub
		
	}	
	
}
