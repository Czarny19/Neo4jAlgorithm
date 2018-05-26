package application.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.lang.time.StopWatch;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Transaction;

import application.model.Algorithms;
import application.model.Neo4jConnection;
import javafx.animation.PauseTransition;
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

public class Menu implements Initializable{

	@FXML
	private TextField AlgorithmText;
	@FXML
	private Button DBConnect;
	@FXML
	private Button DBDisconnect;
	@FXML
	private Button Start;
	@FXML
	private Button BrowseForDB;
	@FXML
	private ChoiceBox<String> AlgorithmChooser;
	@FXML
	private TextField ConnectionStatus;
	@FXML
	public Circle ConnectionStatusIndicator;
	@FXML
	private ProgressBar AlgorithmProgress;
	@FXML
	private TextField AlgorithmStatus;
	@FXML
	private TextField Path;
	@FXML
	private TextField StartNode;
	@FXML
	private TextField EndNode;
	@FXML
	private CheckBox isOption1;
	@FXML
	private CheckBox isOption2;
	@FXML
	private CheckBox isOption3;
	@FXML
	private TextField PortInput;
	@FXML
	private TextField PortPrompt;
	@FXML
	private TextField DistanceKeyPrompt;
	@FXML
	private ChoiceBox<String> DistanceKey;
	
	private static Neo4jConnection Neo4jConnection;
	
	private PauseTransition statusDelay = new PauseTransition(Duration.seconds(0.2));	
	private ArrayList<String> statusText = new ArrayList<String>();	
	private String dot = ".";
	private int dotIndex = 1;
	
	private StopWatch stopWatch = new StopWatch();

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {	
		DistanceKeyPrompt.setVisible(false);
		AlgorithmProgress.setVisible(false);
		AlgorithmChooser.setVisible(false);
		AlgorithmStatus.setVisible(false);		
		AlgorithmText.setVisible(false);
		DBDisconnect.setVisible(false);
		DistanceKey.setVisible(false);
		isOption3.setVisible(false);
		isOption2.setVisible(false);
		isOption1.setVisible(false);
		StartNode.setVisible(false);		
		EndNode.setVisible(false);		
		Start.setVisible(false);		
			
		Path.setText("");
		statusTextInit();

		ConnectionStatus.setText(statusText.get(0));

		ObservableList<String> ChoiceBoxItems = FXCollections.observableArrayList(
				"A*",
				"Betweenness centrality (Relacje skierowane)",
				"Betweenness centrality (Relacje nieskierowane)",
				"Kolorowanie grafu",
				"Degree centrality",
				"Vertex connectivity");
		AlgorithmChooser.setItems(ChoiceBoxItems);
		
		AlgorithmText.setBackground(Background.EMPTY);
		PortPrompt.setBackground(Background.EMPTY);
		DistanceKeyPrompt.setBackground(Background.EMPTY);

		ConnectionStatus.setEditable(false);
		PortPrompt.setEditable(false);
	}

	public void initData(Neo4jConnection N4jC) {
		this.setN4jC(N4jC);
	}
	
	private void setN4jC(Neo4jConnection n4jC) {
		Neo4jConnection = n4jC;
	}
	
	private void statusTextInit() {
		statusText.add("Status po³¹czenia: Brak po³¹czenia z baz¹");
		statusText.add("Status po³¹czenia: Nawi¹zywanie po³¹czenia");
		statusText.add("Status po³¹czenia: B³¹d po³¹czenia");
		statusText.add("Status po³¹czenia: Po³¹czono z baz¹");
		statusText.add("Status po³¹czenia: Roz³¹czanie");
	}

	@FXML
	private void BrowseButtonAction() {
		BrowseForDB.setDisable(true);
		PortInput.setDisable(true);
		DBConnect.setDisable(true);
		Path.setDisable(true);
		
		DirectoryChooser browse = new DirectoryChooser();
		File selectedDirectory = browse.showDialog(null);
		if(selectedDirectory != null)
			Path.setText(selectedDirectory.toString());
		
		BrowseForDB.setDisable(false);
		PortInput.setDisable(false);
		DBConnect.setDisable(false);
		Path.setDisable(false);
	}

	@FXML
    private void Connect() throws IOException {		
		DBConnect.setDisable(true);
		BrowseForDB.setDisable(true);
		pathOptions(false);
		portOptions(false);

	    Neo4jConnection.initPath(Path.getText());  
	    Neo4jConnection.initPortNumber(PortInput.getText());
	    Thread StartConnection = new Thread(Neo4jConnection);
		StartConnection.start();

		connectionStatusChange();
    }

	@FXML
	private void Disconnect() {

		Thread StopConnection = new Thread(Neo4jConnection);
		StopConnection.start();

		ConnectionStatusIndicator.setFill(Color.YELLOW);

		statusDelay.setOnFinished(delayEvent -> {
			if(Neo4jConnection.isConnected()) {
				statusAnimation(4);
				statusDelay.play();
			}
			if(!Neo4jConnection.isConnected()){
				ConnectionStatus.setText(statusText.get(0));
				ConnectionStatusIndicator.setFill(Color.RED);				
				pathOptions(true);	
				portOptions(true);
				dbButtonsOptions(false,false);
			}
        });
		statusDelay.play();
		
		BrowseForDB.setDisable(false);
		
		AlgorithmChooser.setVisible(false);
		AlgorithmText.setVisible(false);
		StartNode.setVisible(false);
		EndNode.setVisible(false);
		Start.setVisible(false);	
	}
	
	private void statusAnimation(int statusNumber) {
		ConnectionStatus.setText(statusText.get(statusNumber) + dot);
		dot += "..";
		dot = dot.substring(0,dotIndex);
		dotIndex++;
		dotIndex = dotIndex%7;
	}
	
	private void connectionStatusChange() {
		NewDatabaseController QuestionCtr = new NewDatabaseController();
		ConnectionStatusIndicator.setFill(Color.YELLOW);
		
		statusDelay.setOnFinished(delayEvent -> {
			if(!Neo4jConnection.isConnected() && !Neo4jConnection.isConnectionErr()) {
				if(QuestionCtr.answer() == 0) {
					statusAnimation(1);
					statusDelay.play();
				}
				if(QuestionCtr.answer() == 1) {
					Neo4jConnection.initNewPath(Path.getText());
					QuestionCtr.resetAnswer();
					Thread StartNewDatabase = new Thread(Neo4jConnection);
					StartNewDatabase.start();
					
					statusDelay.play();
				}
				if(QuestionCtr.answer() == 2) {
					ConnectionStatus.setText(statusText.get(0));
					ConnectionStatusIndicator.setFill(Color.RED);
					pathOptions(true);
					portOptions(true);
					dbButtonsOptions(false,false);
					DBConnect.setDisable(false);
					BrowseForDB.setDisable(false);
					QuestionCtr.resetAnswer();
				}
			}
			if(Neo4jConnection.isConnectionErr()) {
				Neo4jConnection.setConnectionErr(false);
				ConnectionStatus.setText(statusText.get(2));
				ConnectionStatusIndicator.setFill(Color.RED);
				pathOptions(true);
				portOptions(true);
				dbButtonsOptions(false,false);
				DBConnect.setDisable(false);
				BrowseForDB.setDisable(false);
			}
			if(Neo4jConnection.isConnected()) {
				ConnectionStatus.setText(statusText.get(3));
				ConnectionStatusIndicator.setFill(Color.GREEN);
				DBDisconnect.setVisible(true);
				AlgorithmChooser.setVisible(true);
				AlgorithmText.setVisible(true);
			}
		});
		statusDelay.play();
	}
	
	private void pathOptions(boolean on) {
		Path.setEditable(on);
		Path.setFocusTraversable(on);
	}
	
	private void portOptions(boolean on) {
		PortInput.setEditable(on);
		PortInput.setFocusTraversable(on);
		if(PortInput.getText().isEmpty() && on == false)
			PortInput.setText("7690");
		if(PortInput.getText().equals("7690") && on == true)
			PortInput.setText("");
	}
	
	private void dbButtonsOptions(boolean DisconnectVisible, boolean ConnectDisabled) {
		DBDisconnect.setVisible(DisconnectVisible);
		DBConnect.setDisable(ConnectDisabled);
		
	}

	@FXML
	private void algorithmChosen(){
		AlgorithmChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Start.setVisible(false);
			
			StartNode.setVisible(false);
			StartNode.setText("");
			DistanceKeyPrompt.setVisible(false);
			DistanceKey.setVisible(false);
			
			EndNode.setVisible(false);
			EndNode.setText("");
			
			isOption1.setVisible(false);
			isOption1.setSelected(false);
			
			isOption2.setVisible(false);
			isOption2.setSelected(false);
			
			isOption3.setVisible(false);
			isOption3.setSelected(false);
			
			if(!Objects.equals(newValue, "")){
				Start.setVisible(true);
			}
			if(Objects.equals(newValue, "A*")){
				StartNode.setVisible(true);
				EndNode.setVisible(true);
				DistanceKeyPrompt.setVisible(true);
				ObservableList<String> obsItems = FXCollections.observableArrayList();
				obsItems.addAll(getRelationKeys(Neo4jConnection.getDriver().session().beginTransaction()));
				DistanceKey.setItems(obsItems);
				DistanceKey.setVisible(true);
			}
			if(Objects.equals(newValue, "Degree centrality")) {
				isOption1.setText("Relacje wchodz¹ce");
				isOption2.setText("Relacje wychodz¹ce");
				isOption3.setText("Wszystkie relacje");
				isOption1.setVisible(true);
				isOption2.setVisible(true);
				isOption3.setVisible(true);
			}
			if(Objects.equals(newValue, "Vertex connectivity")) {
				isOption1.setText("Wyszukiwanie przeciêæ");
				isOption2.setText("Wyszukiwanie mostów");
				isOption1.setVisible(true);
				isOption2.setVisible(true);
			}
        });
	}

	@FXML
	private void startAlgorithm() throws InterruptedException {	
		AlgorithmProgress.setVisible(true);
		Algorithms algorithms = new Algorithms(AlgorithmProgress);
		stopWatch.reset();
		stopWatch.start();
		algorithms.setStopWatch(stopWatch);
		if(AlgorithmChooser.getSelectionModel().getSelectedItem().equals("A*")) {
			algorithms.AStar(
					Neo4jConnection.getDriver(),
					StartNode.getText(),
					EndNode.getText(),
					DistanceKey.getSelectionModel().getSelectedItem());
		}
		if(AlgorithmChooser.getSelectionModel().getSelectedItem().equals("Betweenness centrality (Relacje skierowane)"))
			algorithms.betweennessCentrality(Neo4jConnection.getDriver(),true);
		if(AlgorithmChooser.getSelectionModel().getSelectedItem().equals("Betweenness centrality (Relacje nieskierowane)"))
			algorithms.betweennessCentrality(Neo4jConnection.getDriver(),false);
		if(AlgorithmChooser.getSelectionModel().getSelectedItem().equals("Kolorowanie grafu"))
			algorithms.graphColoring(Neo4jConnection.getDriver());
		if(AlgorithmChooser.getSelectionModel().getSelectedItem().equals("Degree centrality")) {
			algorithms.degreeCentrality(
					Neo4jConnection.getDriver(),
					isOption1.isSelected(),
					isOption2.isSelected(),
					isOption3.isSelected());
		}
		if(AlgorithmChooser.getSelectionModel().getSelectedItem().equals("Vertex connectivity")) {
			if(isOption1.isSelected())
				algorithms.vertexConnectivity(Neo4jConnection.getDriver(),false);
			if(isOption2.isSelected())
				algorithms.vertexConnectivity(Neo4jConnection.getDriver(),true);
		}
	}
	
	private static ArrayList<String> getRelationKeys(Transaction tx){
		ArrayList<String> RelationKeys = new ArrayList<String>();
		int index;
		for(Record records : tx.run("match (a)-[b]->(c) return distinct keys(b)").list()) {
			index = 0;
			while(records.get(0).get(index).asString() != "null") {			
				if(!RelationKeys.contains(records.get(0).get(index).asString())) {
					RelationKeys.add(records.get(0).get(index).asString());
				}
				index++;
			}
		}
		return RelationKeys;
	}
}
