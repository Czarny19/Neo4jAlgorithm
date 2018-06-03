package application.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.lang.time.StopWatch;

import application.model.Algorithm;
import application.model.FileCreator;
import application.model.Neo4jConnection;
import application.model.PropertyHelper;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

public class MenuController implements Initializable{

	@FXML
	private TextField algorithmPrompt;
	@FXML
	private TextField connectionStatus;
	@FXML
	private TextField algorithmStatus;
	@FXML
	private TextField pathToDB;
	@FXML
	private TextField startNodeInput;
	@FXML
	private TextField endNodeInput;
	@FXML
	private TextField portInput;
	@FXML
	private TextField portPrompt;
	@FXML
	private TextField distanceKeyPrompt;
	@FXML
	private TextField keysLoadingPrompt;
	
	@FXML
	private Button connectToDB;
	@FXML
	private Button disconnectFromDB;
	@FXML
	private Button startAlgorithm;
	@FXML
	private Button browseForDB;
	
	@FXML
	private CheckBox isIndegree;
	@FXML
	private CheckBox isOutdegree;
	@FXML
	private CheckBox isDegree;
	@FXML
	private CheckBox isFindCuts;
	@FXML
	private CheckBox isFindBridges;
	
	@FXML
	private ChoiceBox<String> algorithmChooser;
	@FXML
	private ChoiceBox<String> distanceKeyChooser;
	
	@FXML
	private ProgressBar algorithmProgress;
		
	@FXML
	private Circle connectionStatusIndicator;

	private Neo4jConnection neo4jConnection;
	
	private PauseTransition statusDelay = new PauseTransition(Duration.seconds(0.2));	
	private ArrayList<String> statusText;
	
	private String dot = ".";
	private int dotIndex = 1;
	
	private StopWatch algExecTime = new StopWatch();
	
	private ObservableList<String> fxObservable;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {	
		distanceKeyChooser.setVisible(false);		
		distanceKeyPrompt.setVisible(false);
		keysLoadingPrompt.setVisible(false);
		algorithmProgress.setVisible(false);
		algorithmChooser.setVisible(false);
		disconnectFromDB.setVisible(false);
		algorithmStatus.setVisible(false);		
		algorithmPrompt.setVisible(false);
		startAlgorithm.setVisible(false);	
		startNodeInput.setVisible(false);
		isFindBridges.setVisible(false);
		endNodeInput.setVisible(false);	
		isOutdegree.setVisible(false);	
		isIndegree.setVisible(false);
		isFindCuts.setVisible(false);		
		isDegree.setVisible(false);
	
		statusTextInit();

		connectionStatus.setText(statusText.get(0));
		algorithmProgress.setStyle("-fx-accent: forestgreen;\r\n");

		ObservableList<String> choiceBoxItems = FXCollections.observableArrayList(
				"A*",
				"Betweenness centrality (Relacje skierowane)",
				"Betweenness centrality (Relacje nieskierowane)",
				"Kolorowanie grafu",
				"Degree centrality",
				"Connectivity");
		algorithmChooser.setItems(choiceBoxItems);
				
		distanceKeyPrompt.setBackground(Background.EMPTY);		
		keysLoadingPrompt.setBackground(Background.EMPTY);
		algorithmStatus.setBackground(Background.EMPTY);
		algorithmPrompt.setBackground(Background.EMPTY);
		portPrompt.setBackground(Background.EMPTY);

		connectionStatus.setEditable(false);
		portPrompt.setEditable(false);
		
		isFindCuts.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	isFindBridges.setSelected(!newValue);
		    }
		});
		isFindBridges.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	isFindCuts.setSelected(!newValue);
		    }
		});
	}

	public void initNeo4jConnection(Neo4jConnection neo4jConnection) {
		this.neo4jConnection = neo4jConnection;
	}

	private void statusTextInit() {
		statusText = new ArrayList<String>();	
		
		statusText.add("Status po³¹czenia: Brak po³¹czenia z baz¹");
		statusText.add("Status po³¹czenia: Nawi¹zywanie po³¹czenia");
		statusText.add("Status po³¹czenia: B³¹d po³¹czenia");
		statusText.add("Status po³¹czenia: Po³¹czono z baz¹");
		statusText.add("Status po³¹czenia: Roz³¹czanie");
	}

	@FXML
	private void browseForDB() {
		browseForDB.setDisable(true);
		connectToDB.setDisable(true);
		portInput.setDisable(true);		
		pathToDB.setDisable(true);
		
		DirectoryChooser browseForDirectory = new DirectoryChooser();
		File selectedDirectory = browseForDirectory.showDialog(null);
		if(selectedDirectory != null)
			pathToDB.setText(selectedDirectory.toString());
		
		browseForDB.setDisable(false);
		connectToDB.setDisable(false);
		portInput.setDisable(false);		
		pathToDB.setDisable(false);
	}

	@FXML
    private void connect() throws IOException {		
		connectToDB.setDisable(true);
		browseForDB.setDisable(true);
		pathOptions(false);
		portOptions(false);

	    neo4jConnection.initPath(pathToDB.getText());  
	    neo4jConnection.initPortNumber(portInput.getText());
	    Thread connection = new Thread(neo4jConnection);
		connection.start();

		connectionStatusChange();
    }

	@FXML
	private void disconnect() {

		Thread stopConnection = new Thread(neo4jConnection);
		stopConnection.start();

		connectionStatusIndicator.setFill(Color.YELLOW);

		statusDelay.setOnFinished(delayEvent -> {
			if(neo4jConnection.isConnected()) {
				statusAnimation(4);
				statusDelay.play();
			}
			if(!neo4jConnection.isConnected()){
				connectionStatus.setText(statusText.get(0));
				connectionStatusIndicator.setFill(Color.RED);				
				pathOptions(true);	
				portOptions(true);
				dbButtonsOptions(false,false);
			}
        });
		statusDelay.play();
		
		fxObservable = FXCollections.observableArrayList();
		distanceKeyChooser.setItems(fxObservable);
		fxObservable = null;		
		algorithmChooser.getSelectionModel().clearSelection();
		
		browseForDB.setDisable(false);
		
		algorithmChooser.setVisible(false);
		algorithmPrompt.setVisible(false);
		startAlgorithm.setVisible(false);				
		
		distanceKeyChooser.setVisible(false);
		keysLoadingPrompt.setVisible(false);
		distanceKeyPrompt.setVisible(false);
		distanceKeyPrompt.setVisible(false);
		startNodeInput.setVisible(false);
		endNodeInput.setVisible(false);
		
		isOutdegree.setVisible(false);		
		isIndegree.setVisible(false);		
		isDegree.setVisible(false);
		
		isFindBridges.setVisible(false);
		isFindCuts.setVisible(false);	
	}
	
	private void statusAnimation(int statusNumber) {
		connectionStatus.setText(statusText.get(statusNumber) + dot);
		dot += "..";
		dot = dot.substring(0,dotIndex);
		dotIndex++;
		dotIndex = dotIndex%7;
	}
	
	private void connectionStatusChange() {
		NewDatabaseController newDatabaseController = new NewDatabaseController();
		connectionStatusIndicator.setFill(Color.YELLOW);
		
		statusDelay.setOnFinished(delayEvent -> {
			if(!neo4jConnection.isConnected() && !neo4jConnection.isConnectionErr()) {
				if(newDatabaseController.answer() == 0) {
					statusAnimation(1);
					statusDelay.play();
				}
				if(newDatabaseController.answer() == 1) {
					neo4jConnection.initNewPath(pathToDB.getText());
					newDatabaseController.resetAnswer();
					Thread startNewDatabase = new Thread(neo4jConnection);
					startNewDatabase.start();
					
					statusDelay.play();
				}
				if(newDatabaseController.answer() == 2) {
					connectionStatus.setText(statusText.get(0));
					connectionStatusIndicator.setFill(Color.RED);
					pathOptions(true);
					portOptions(true);
					dbButtonsOptions(false,false);
					connectToDB.setDisable(false);
					browseForDB.setDisable(false);
					newDatabaseController.resetAnswer();
				}
			}
			if(neo4jConnection.isConnectionErr()) {
				neo4jConnection.setConnectionErr(false);
				connectionStatus.setText(statusText.get(2));
				connectionStatusIndicator.setFill(Color.RED);
				pathOptions(true);
				portOptions(true);
				dbButtonsOptions(false,false);
				connectToDB.setDisable(false);
				browseForDB.setDisable(false);
			}
			if(neo4jConnection.isConnected()) {
				connectionStatus.setText(statusText.get(3));
				connectionStatusIndicator.setFill(Color.GREEN);
				disconnectFromDB.setVisible(true);
				algorithmChooser.setVisible(true);
				algorithmPrompt.setVisible(true);
			}
		});
		statusDelay.play();
	}
	
	private void pathOptions(boolean isOn) {
		pathToDB.setEditable(isOn);
		pathToDB.setFocusTraversable(isOn);
	}
	
	private void portOptions(boolean isOn) {
		portInput.setEditable(isOn);
		portInput.setFocusTraversable(isOn);
		if(portInput.getText().isEmpty() && !isOn)
			portInput.setText("7690");
		if(portInput.getText().equals("7690") && isOn)
			portInput.setText("");
	}
	
	private void dbButtonsOptions(boolean disconnectVisible, boolean connectDisabled) {
		disconnectFromDB.setVisible(disconnectVisible);
		connectToDB.setDisable(connectDisabled);		
	}

	@FXML
	private void algorithmChosen(){
		algorithmChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			startAlgorithm.setVisible(false);
			
			startNodeInput.setVisible(false);
			startNodeInput.setText("");
			endNodeInput.setVisible(false);
			endNodeInput.setText("");
			distanceKeyPrompt.setVisible(false);
			distanceKeyChooser.setVisible(false);						
			
			isIndegree.setVisible(false);
			isIndegree.setSelected(false);			
			isOutdegree.setVisible(false);
			isOutdegree.setSelected(false);			
			isDegree.setVisible(false);
			isDegree.setSelected(false);
			
			isFindCuts.setVisible(false);			
			isFindBridges.setVisible(false);
			
			if(!Objects.equals(newValue, "")){
				startAlgorithm.setVisible(true);
			}
			if(Objects.equals(newValue, "A*")){
				startNodeInput.setVisible(true);
				endNodeInput.setVisible(true);
				distanceKeyPrompt.setVisible(true);
				distanceKeyChooser.setVisible(true);
								
				if(fxObservable == null) {					
					keysLoadingPrompt.setVisible(true);
								
					fxObservable = FXCollections.observableArrayList();
					fxObservable.add("Brak klucza");
					
					Runnable setAvailableKeys = () -> {
						PropertyHelper propertiesHelper;
						Thread findProperty;
						
						try(org.neo4j.graphdb.Transaction tx = neo4jConnection.graphDBService().beginTx()) {
							for(String property : neo4jConnection.graphDBService().getAllPropertyKeys()) {
								
								propertiesHelper = new PropertyHelper(neo4jConnection.driver().session());
								propertiesHelper.setKey(property);
								
								findProperty = new Thread(propertiesHelper);
								findProperty.start();
								findProperty.join(150);
								
								if(propertiesHelper.isFound()) {
									fxObservable.add(property);
								}
								else if (!propertiesHelper.isFound()){
									propertiesHelper.closeTransaction();
								}
							}
							distanceKeyChooser.setItems(fxObservable);
							
							keysLoadingPrompt.setVisible(false);
							distanceKeyChooser.setVisible(true);
							tx.success();
							tx.close();
					    } catch (Exception exc) {
							exc.printStackTrace();
					    }	
					};
					Thread setKeysThread = new Thread(setAvailableKeys);
					setKeysThread.start();				
				}
			}
			if(Objects.equals(newValue, "Degree centrality")) {
				isOutdegree.setVisible(true);
				isIndegree.setVisible(true);				
				isDegree.setVisible(true);
			}
			if(Objects.equals(newValue, "Connectivity")) {
				isFindBridges.setVisible(true);
				isFindCuts.setVisible(true);				
			}
        });
	}

	@FXML
	private void startAlgorithm() throws InterruptedException {			
		FileCreator algInfo = new FileCreator(neo4jConnection.pathToDB());
		
		startAlgorithm.setDisable(true);
		algorithmChooser.setDisable(true);
				
		algExecTime.reset();
		algExecTime.start();
		Algorithm algorithm = new Algorithm(
				neo4jConnection.driver(),
				algorithmProgress, 
				algorithmStatus, 
				algInfo, 
				algExecTime);
				
		Runnable start = () -> {
			try {
				if(algorithmChooser.getSelectionModel().getSelectedItem().equals("A*")) {	
					distanceKeyChooser.setDisable(true);
					startNodeInput.setDisable(true);
					endNodeInput.setDisable(true);		
					
					String selectedKey = distanceKeyChooser.getSelectionModel().getSelectedItem();
					
					algorithm.aStar(startNodeInput.getText(), endNodeInput.getText(), selectedKey);
					
					distanceKeyChooser.setDisable(false);
					startNodeInput.setDisable(false);
					endNodeInput.setDisable(false);					
				}
				if(algorithmChooser.getSelectionModel().getSelectedItem().equals("Betweenness centrality (Relacje skierowane)")) {
					algorithm.betweennessCentrality(true);
				}
				if(algorithmChooser.getSelectionModel().getSelectedItem().equals("Betweenness centrality (Relacje nieskierowane)")) {
					algorithm.betweennessCentrality(false);
				}
				if(algorithmChooser.getSelectionModel().getSelectedItem().equals("Kolorowanie grafu")) {
					algorithm.graphColoring();
				}
				if(algorithmChooser.getSelectionModel().getSelectedItem().equals("Degree centrality")) {
					isIndegree.setDisable(true);
					isOutdegree.setDisable(true);
					isDegree.setDisable(true);
					
					algorithm.degreeCentrality(isIndegree.isSelected(), isOutdegree.isSelected(), isDegree.isSelected());
					
					isIndegree.setDisable(false);
					isOutdegree.setDisable(false);
					isDegree.setDisable(false);
				}
				if(algorithmChooser.getSelectionModel().getSelectedItem().equals("Connectivity")) {
					isFindCuts.setDisable(true);
					isFindBridges.setDisable(true);
					
					algorithm.connectivity(isFindCuts.isSelected(), isFindBridges.isSelected());
					
					isFindCuts.setDisable(false);
					isFindBridges.setDisable(false);
				}
				algorithmChooser.setDisable(false);				
				startAlgorithm.setDisable(false);
			}catch(Exception exc) {
				algorithmChooser.setDisable(false);
				startAlgorithm.setDisable(false);				
			}
		};
		Thread startAlgorithm = new Thread(start);
		startAlgorithm.start();
	}
}
