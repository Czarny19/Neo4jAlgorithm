package application.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.driver.v1.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Neo4jConnection implements Runnable{
	
	private GraphDatabaseService graphDb;
	private Driver driver;
	
	private File DBpath;
	private File DatabaseCheck;
	
	private Boolean isConnected = false;
	private Boolean ConnectionErr = false;

	public Neo4jConnection() {
	
	}	
	
	public void initPath(String path) {
		DBpath = new File(path);
		DatabaseCheck = new File(DBpath + "\\neostore.schemastore.db");
	}
	
	public void initNewPath(String path) {
		DatabaseCheck = new File(path);
		DBpath = new File(path);
	}
	
	private void shutdownConnection() throws Exception {
		graphDb.shutdown();
		graphDb = null;
		driver.close();
		isConnected = false;
	}
	
	@SuppressWarnings("deprecation")
	private void startConnection() throws Exception {
		if(DatabaseCheck.exists() && DBpath.exists()) {

			GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");

			graphDb = new GraphDatabaseFactory()
					.newEmbeddedDatabaseBuilder(DBpath)
					.setConfig(bolt.type, "BOLT")
					.setConfig(bolt.enabled, "true")
					.setConfig(bolt.address, ":7688")
					.newGraphDatabase();

			registerShutdownHook(graphDb);

			String uri = "bolt://127.0.0.1:7688";
			driver = GraphDatabase.driver(uri, AuthTokens.basic("neo4j", "neo4jadmin"));

			try (Session session = driver.session()) {
				session.beginTransaction().run("MATCH (n) RETURN n LIMIT 1");
			}

			isConnected = true;
		}
		else if(!DBpath.exists()) {
			ConnectionErr = true;
		}
		else{
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					try {
						final FXMLLoader fxmlLoader = new FXMLLoader();
					    fxmlLoader.setLocation(getClass().getResource("/application/view/NewDatabaseQuestionBox.fxml"));
			        
					    Scene QuestionScene = new Scene(fxmlLoader.load(), 300, 150);
					    Stage QuestionStage = new Stage();
					    
					    QuestionScene.getStylesheets().add("/application/resource/Custom.css");
					    QuestionStage.initStyle(StageStyle.TRANSPARENT);
					    QuestionStage.setScene(QuestionScene);
					    QuestionStage.show();					 
					} catch (IOException e) {
				        Logger logger = Logger.getLogger(getClass().getName());
				        logger.log(Level.SEVERE, "Failed to create new Window.", e);
				    }
				}
		    });  		    		
		}
	}
	
	public void run(){
		if(isConnected()) {
			try {
				shutdownConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				startConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isConnectionErr() {
		return ConnectionErr;
	}
	
	public void setConnectionErr(boolean ConnectionErr) {
		this.ConnectionErr = ConnectionErr;
	}
	
	public Driver getDriver() {
		return driver;
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb ){
	    Runtime.getRuntime().addShutdownHook(new Thread(graphDb::shutdown));
	}
}
