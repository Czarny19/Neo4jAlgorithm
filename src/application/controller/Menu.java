package application.controller;

import application.model.Algorithms;
import application.model.Neo4jConnection;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

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
		StartNode.setVisible(false);
		EndNode.setVisible(false);
		Start.setVisible(false);
			
		Path.setText("");
		statusTextInit();

		ConnectionStatus.setText(StatusText.get(0));

		ObservableList<String> ChoiceBoxItems = FXCollections.observableArrayList(
				"A*",
				"Betweenness centrality (Relacje skierowane)",
				"Betweenness centrality (Relacje nieskierowane)");
		AlgorithmChooser.setItems(ChoiceBoxItems);
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
		StatusText.add("Status po³¹czenia: B³¹d - podana œcie¿ka nie istnieje");
		StatusText.add("Status po³¹czenia: Po³¹czono z baz¹");
		StatusText.add("Status po³¹czenia: Roz³¹czanie");
	}

	@FXML
	private void BrowseButtonAction() {
		DirectoryChooser browse = new DirectoryChooser();
		File selectedDirectory = browse.showDialog(null);
		Path.setText(selectedDirectory.toString());
	}

	@FXML
    private void Connect() {		
		DBConnect.setDisable(true);
		pathOptions(false);

		Neo4jConnection.initPath(Path.getText());
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
				dbButtonsOptions(false,false);
			}
        });
		StatusDelay.play();
		
		AlgorithmChooser.setVisible(false);
		AlgorithmText.setVisible(false);
		Start.setVisible(false);
		StartNode.setVisible(false);
		EndNode.setVisible(false);
	}
	
	private void statusAnimation(int statusNumber) {
		ConnectionStatus.setText(StatusText.get(statusNumber) + dot);
		dot += "..";
		dot = dot.substring(0,dotIndex);
		dotIndex++;
		dotIndex = dotIndex%7;
	}
	
	private void connectionStatusChange() {
		NewDbQuestionBoxController QuestionCtr = new NewDbQuestionBoxController();
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
					dbButtonsOptions(false,false);
				}
			}
			if(Neo4jConnection.isConnectionErr()) {
				Neo4jConnection.setConnectionErr(false);
				ConnectionStatus.setText(StatusText.get(2));
				ConnectionStatusIndicator.setFill(Color.RED);
				pathOptions(true);
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
	
	private void dbButtonsOptions(boolean DisconnectVisible, boolean ConnectDisabled) {
		DBDisconnect.setVisible(DisconnectVisible);
		DBConnect.setDisable(ConnectDisabled);
	}

	@FXML
	private void algorithmChosen(){
		AlgorithmChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Start.setVisible(false);
			StartNode.setVisible(false);
			EndNode.setVisible(false);
			if(!Objects.equals(newValue, "")){
				Start.setVisible(true);
			}
			if(Objects.equals(newValue, "A*")){
			   StartNode.setVisible(true);
			   EndNode.setVisible(true);
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
	}
}
