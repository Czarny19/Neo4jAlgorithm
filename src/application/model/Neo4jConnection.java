package application.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Neo4jConnection implements Runnable{
	
	private GraphDatabaseService graphDBService;
	private Driver neo4jDriver;

	private String portNumber;
	
	private File pathToDB;
	private File databaseCheck;
	
	private Boolean isConnected = false;
	private Boolean connectionErr = false;
	
	public void initPath(String pathToDB) {
		this.pathToDB = new File(pathToDB.replace("\\", File.separator));
		this.databaseCheck = new File(pathToDB + File.separator + "neostore.schemastore.db");
	}
	
	public Path pathToDB() {
		return pathToDB.toPath();
	}
	
	public void initNewPath(String pathToDB) {
		this.databaseCheck = new File(pathToDB);
		this.pathToDB = new File(pathToDB);
	}
	
	public void initPortNumber(String portNumber) {	
		if(portNumber.isEmpty())
			this.portNumber = ":7690";
		else
			this.portNumber = ":" + portNumber;	
	}
	
	@SuppressWarnings("deprecation")
	public void startConnection() throws IOException{
		if(databaseCheck.exists() && pathToDB.exists()) {						
			GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");		
			try{
				graphDBService = new GraphDatabaseFactory()
						.newEmbeddedDatabaseBuilder(pathToDB)
						.setConfig(bolt.type, "BOLT")
						.setConfig(bolt.enabled, "true")
						.setConfig(bolt.address, portNumber)
						.newGraphDatabase();
						
				registerShutdownHook(graphDBService);
									
				neo4jDriver = GraphDatabase.driver("bolt://127.0.0.1" + portNumber);
				
				try ( Session session = neo4jDriver.session() ) {
					Transaction tx = session.beginTransaction();
		            tx.run("MATCH (n) RETURN n LIMIT 10");
		            tx.success();
		            tx.close();
		        }

				isConnected = true;
			}catch(Exception e) {
				e.printStackTrace();
				connectionErr = true;
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("B³¹d po³¹czenia!");
						alert.setHeaderText(null);
						alert.setContentText(
							"Nie mo¿na nawi¹zaæ po³¹czenia z baz¹, nale¿y zakoñczyæ wszystkie inne po³¹czenia z baz¹, " +
							"lub zwolniæ port " + portNumber.replaceAll(":", ""));
						alert.showAndWait();
					}
				});
			}	
		}
		if(!pathToDB.exists()) {
			connectionErr = true;
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("B³¹d po³¹czenia!");
					alert.setHeaderText(null);
					alert.setContentText("Podana œcie¿ka nie jest prawid³owa lub nie istnieje!");
					alert.showAndWait();
				}
			});
		}
		if(!databaseCheck.exists() && pathToDB.exists()){
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					try {
						final FXMLLoader loader = new FXMLLoader();
					    loader.setLocation(getClass().getResource("/src/application/view/NewDatabase.fxml"));
			        
					    Scene newDBScene = new Scene(loader.load(), 300, 150);
					    Stage newDBStage = new Stage();
					    
					    newDBScene.getStylesheets().add("/src/application/resource/Custom.css");
					    newDBStage.initStyle(StageStyle.TRANSPARENT);
					    newDBStage.setScene(newDBScene);
					    newDBStage.show();					 
					} catch (IOException e) {
				        Logger logger = Logger.getLogger(getClass().getName());
				        logger.log(Level.SEVERE, "Failed to create new Window.", e);
				    }
				}
		    });  		    		
		}
	}
	
	public void shutdownConnection() throws Exception {
		graphDBService.shutdown();
		graphDBService = null;
		neo4jDriver.close();
		isConnected = false;
	}
	
	public void run(){	
		try {
			if(!isConnected())
				startConnection();
			else if(isConnected())
				shutdownConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isConnectionErr() {
		return connectionErr;
	}
	
	public void setConnectionErr(boolean ConnectionErr) {
		this.connectionErr = ConnectionErr;
	}
	
	public Driver driver() {
		return neo4jDriver;
	}
	
	public GraphDatabaseService graphDBService() {
		return graphDBService;
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDBService ){
	    Runtime.getRuntime().addShutdownHook(new Thread(graphDBService::shutdown));
	}
}
