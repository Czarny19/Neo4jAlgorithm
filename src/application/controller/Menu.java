package application.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

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
	
	private static Neo4jConnection Neo4jConnection;
	
	private PauseTransition StatusDelay = new PauseTransition(Duration.seconds(0.2));	
	private ArrayList<String> StatusText = new ArrayList<String>();	
	private String dot = ".";
	private int dotIndex = 1;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {	
		AlgorithmProgress.setVisible(false);
		AlgorithmChooser.setVisible(false);
		AlgorithmStatus.setVisible(false);
		AlgorithmText.setVisible(false);
		DBDisconnect.setVisible(false);
		isOption2.setVisible(false);
		isOption1.setVisible(false);
		StartNode.setVisible(false);		
		EndNode.setVisible(false);
		isOption3.setVisible(false);
		Start.setVisible(false);
			
		Path.setText("");
		statusTextInit();

		ConnectionStatus.setText(StatusText.get(0));

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
		StatusText.add("Status po³¹czenia: Brak po³¹czenia z baz¹");
		StatusText.add("Status po³¹czenia: Nawi¹zywanie po³¹czenia");
		StatusText.add("Status po³¹czenia: B³¹d po³¹czenia");
		StatusText.add("Status po³¹czenia: Po³¹czono z baz¹");
		StatusText.add("Status po³¹czenia: Roz³¹czanie");
	}

	@FXML
	private void BrowseButtonAction() {
		DirectoryChooser browse = new DirectoryChooser();
		File selectedDirectory = browse.showDialog(null);
		if(selectedDirectory != null)
			Path.setText(selectedDirectory.toString());
	}

	@FXML
    private void Connect() throws IOException {		
		DBConnect.setDisable(true);
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

		StatusDelay.setOnFinished(delayEvent -> {
			if(Neo4jConnection.isConnected()) {
				statusAnimation(4);
				StatusDelay.play();
			}
			if(!Neo4jConnection.isConnected()){
				ConnectionStatus.setText(StatusText.get(0));
				ConnectionStatusIndicator.setFill(Color.RED);				
				pathOptions(true);	
				portOptions(true);
				dbButtonsOptions(false,false);
			}
        });
		StatusDelay.play();
		
		AlgorithmChooser.setVisible(false);
		AlgorithmText.setVisible(false);
		StartNode.setVisible(false);
		EndNode.setVisible(false);
		Start.setVisible(false);	
	}
	
	private void statusAnimation(int statusNumber) {
		ConnectionStatus.setText(StatusText.get(statusNumber) + dot);
		dot += "..";
		dot = dot.substring(0,dotIndex);
		dotIndex++;
		dotIndex = dotIndex%7;
	}
	
	private void connectionStatusChange() {
		NewDatabaseController QuestionCtr = new NewDatabaseController();
		ConnectionStatusIndicator.setFill(Color.YELLOW);
		
		StatusDelay.setOnFinished(delayEvent -> {
			if(!Neo4jConnection.isConnected() && !Neo4jConnection.isConnectionErr()) {
				if(QuestionCtr.answer() == 0) {
					statusAnimation(1);
					StatusDelay.play();
				}
				if(QuestionCtr.answer() == 1) {
					Neo4jConnection.initNewPath(Path.getText());
					QuestionCtr.resetAnswer();
					Thread StartNewDatabase = new Thread(Neo4jConnection);
					StartNewDatabase.start();
					
					StatusDelay.play();
				}
				if(QuestionCtr.answer() == 2) {
					ConnectionStatus.setText(StatusText.get(0));
					ConnectionStatusIndicator.setFill(Color.RED);
					pathOptions(true);
					portOptions(true);
					dbButtonsOptions(false,false);
				}
			}
			if(Neo4jConnection.isConnectionErr()) {
				Neo4jConnection.setConnectionErr(false);
				ConnectionStatus.setText(StatusText.get(2));
				ConnectionStatusIndicator.setFill(Color.RED);
				pathOptions(true);
				portOptions(true);
				dbButtonsOptions(false,false);
			}
			if(Neo4jConnection.isConnected()) {
				ConnectionStatus.setText(StatusText.get(3));
				ConnectionStatusIndicator.setFill(Color.GREEN);
				DBDisconnect.setVisible(true);
				AlgorithmChooser.setVisible(true);
				AlgorithmText.setVisible(true);
			}
		});
		StatusDelay.play();
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
	private void startAlgorithm() {	
		Algorithms algorithms = new Algorithms();
		if(AlgorithmChooser.getSelectionModel().getSelectedItem() == "A*") {
			algorithms.initAStar(StartNode.getText(), EndNode.getText(), Neo4jConnection.getDriver());
			algorithms.AStar();
		}
		if(AlgorithmChooser.getSelectionModel().getSelectedItem() == "Betweenness centrality (Relacje skierowane)")
			algorithms.betweennessCentrality(Neo4jConnection.getDriver(),true);
		if(AlgorithmChooser.getSelectionModel().getSelectedItem() == "Betweenness centrality (Relacje nieskierowane)")
			algorithms.betweennessCentrality(Neo4jConnection.getDriver(),false);
		if(AlgorithmChooser.getSelectionModel().getSelectedItem() == "Kolorowanie grafu")
			algorithms.graphColoring(Neo4jConnection.getDriver());
		if(AlgorithmChooser.getSelectionModel().getSelectedItem() == "Degree centrality") {
			algorithms.degreeCentrality(
					Neo4jConnection.getDriver(),
					isOption1.isSelected(),
					isOption2.isSelected(),
					isOption3.isSelected());
		}
		if(AlgorithmChooser.getSelectionModel().getSelectedItem() == "Vertex connectivity") {
			if(isOption1.isSelected())
				algorithms.vertexConnectivity(Neo4jConnection.getDriver(),false);
			if(isOption2.isSelected())
				algorithms.vertexConnectivity(Neo4jConnection.getDriver(),true);
		}
			
	}
}
