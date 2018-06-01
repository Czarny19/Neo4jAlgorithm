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
	
	private GraphDatabaseService graphDb;
	private Driver driver;

	private String port;
	
	private File DBpath;
	private File DatabaseCheck;
	
	private Boolean isConnected = false;
	private Boolean ConnectionErr = false;
	
	public void initPath(String path) {
		DBpath = new File(path);
		DatabaseCheck = new File(DBpath + File.separator + "neostore.schemastore.db");
	}
	
	public Path pathToDB() {
		return DBpath.toPath();
	}
	
	public void initNewPath(String path) {
		DatabaseCheck = new File(path);
		DBpath = new File(path);
	}
	
	public void initPortNumber(String port) {	
		if(port.isEmpty())
			this.port = ":7690";
		else
			this.port = ":" + port;	
	}
	
	@SuppressWarnings("deprecation")
	public void startConnection() throws IOException{
		if(DatabaseCheck.exists() && DBpath.exists()) {
							
			GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");

			try{
				graphDb = new GraphDatabaseFactory()
						.newEmbeddedDatabaseBuilder(DBpath)
						.setConfig(bolt.type, "BOLT")
						.setConfig(bolt.enabled, "true")
						.setConfig(bolt.address, port)
						.newGraphDatabase();
						
				registerShutdownHook(graphDb);
									
				driver = GraphDatabase.driver("bolt://127.0.0.1" + port);
				
				try ( Session session = driver.session() ) {
					Transaction tx = session.beginTransaction();
		            tx.run("MATCH (n) RETURN n LIMIT 10");
		            tx.success();
		            tx.close();
		        }

				isConnected = true;
			}catch(Exception e) {
				ConnectionErr = true;
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						Alert alert = new Alert(Alert.AlertType.INFORMATION);
						alert.setTitle("B³¹d po³¹czenia!");
						alert.setHeaderText(null);
						alert.setContentText(
							"Nie mo¿na nawi¹zaæ po³¹czenia z baz¹, nale¿y zakoñczyæ wszystkie inne po³¹czenia z baz¹, " +
							"lub zwolniæ port " + port.replaceAll(":", ""));
						alert.showAndWait();
					}
				});
			}	
		}
		if(!DBpath.exists()) {
			ConnectionErr = true;
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
		if(!DatabaseCheck.exists() && DBpath.exists()){
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					try {
						final FXMLLoader fxmlLoader = new FXMLLoader();
					    fxmlLoader.setLocation(getClass().getResource("/application/view/NewDatabase.fxml"));
			        
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
	
	public void shutdownConnection() throws Exception {
		graphDb.shutdown();
		graphDb = null;
		driver.close();
		isConnected = false;
	}
	
	public void run(){	
		try {
			if(!isConnected()) {
				startConnection();
			}
			else if(isConnected()) {
				shutdownConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	
	public Driver driver() {
		return driver;
	}
	
	public GraphDatabaseService graphDb() {
		return graphDb;
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb ){
	    Runtime.getRuntime().addShutdownHook(new Thread(graphDb::shutdown));
	}
}
