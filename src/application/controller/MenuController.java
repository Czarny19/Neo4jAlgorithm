package application.controller;

import application.model.A_star_Thread;
import application.model.Algorithm_A_star;
import application.model.GraphAStar;
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
import org.neo4j.driver.v1.Record;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class MenuController implements Initializable{

	@FXML
	private TextField AlgorithmTextField;
	@FXML
	private Button BaseConnectionButton;
	@FXML
	private Button BaseDisconnectButton;
	@FXML
	private Button StartButton;
	@FXML
	private ChoiceBox<String> AlgorithmChooser;
	@FXML
	private TextField ConnectionStatus;
	@FXML
	public Circle ConnectionStatusIndicator;
	@FXML
	private ProgressBar AlgorithmStatusBar;
	@FXML
	private TextField AlgorithmStatusText;
	@FXML
	private TextField PathTextField;
	@FXML
	private TextField StartNodeTF;
	@FXML
	private TextField EndNodeTF;

	private int ConnectionStatusStage;
	
	private static Neo4jConnection N4jC;
	
	private PauseTransition StatusDelay = new PauseTransition(Duration.seconds(0.2));
	
	public MenuController() {
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {	
		AlgorithmStatusBar.setVisible(false);
		AlgorithmStatusText.setVisible(false);
		BaseDisconnectButton.setVisible(false);
		StartNodeTF.setVisible(false);
		EndNodeTF.setVisible(false);
		StartButton.setVisible(false);
		AlgorithmTextField.setVisible(false);
		AlgorithmChooser.setVisible(false);
		
		PathTextField.setText("");
		
		ConnectionStatusStage = 0;
		ConnectionStatus.setText("Status połączenia: Brak połączenia z bazą");

		ObservableList<String> ChoiceBoxItems = FXCollections.observableArrayList("A*","test");
		AlgorithmChooser.setItems(ChoiceBoxItems);
	}

	public void initData(Neo4jConnection N4jC) {
		this.setN4jC(N4jC);
	}

	@FXML
	private void BrowseButtonAction() {
		DirectoryChooser chooser = new DirectoryChooser();

		File selectedDirectory = chooser.showDialog(null);

		PathTextField.setText(selectedDirectory.toString());
	}

	@FXML
    private void MenuActionConnect() {

		ConnectionStatusIndicator.setFill(Color.YELLOW);

		NewDbQuestionBoxController QuestionCtr = new NewDbQuestionBoxController();

		BaseConnectionButton.setDisable(true);

		PathTextField.setEditable(false);
		PathTextField.setFocusTraversable(false);

		N4jC.initPath(PathTextField.getText());
		Thread StartConnection = new Thread(N4jC);
		StartConnection.start();

		StatusDelay.setOnFinished(delayEvent -> {
			if(!N4jC.isConnected() && !N4jC.isConnectionErr()) {
				if(QuestionCtr.getAnswer() == 0) {
					switch (ConnectionStatusStage) {
		            case 0:  ConnectionStatus.setText("Status połączenia: Nawiązywanie połączenia");
		                     break;
		            case 1:  ConnectionStatus.setText(ConnectionStatus.getText() + ".");
		                     break;
		            case 2:  ConnectionStatus.setText(ConnectionStatus.getText() + "..");
		                     break;
		            case 3:  ConnectionStatus.setText(ConnectionStatus.getText() + "...");
		                     break;
		            case 4:  ConnectionStatus.setText("Status połączenia: Nawiązywanie połączenia");
		                     break;
		            default: ConnectionStatus.setText("Status połączenia: Nawiązywanie połączenia");
		                     break;
					}
					ConnectionStatusStage = (ConnectionStatusStage+1)%4;
					StatusDelay.play();
				}
				else if(QuestionCtr.getAnswer() == 1) {
					N4jC.initNewPath(PathTextField.getText());

					QuestionCtr.setAnswer();

					Thread StartNewDatabase = new Thread(N4jC);
					StartNewDatabase.start();

					StatusDelay.play();
				}
				else if(QuestionCtr.getAnswer() == 2) {
					ConnectionStatus.setText("Status połaczenia: Brak połączenia z bazą");
					ConnectionStatusIndicator.setFill(Color.RED);

					PathTextField.setEditable(true);
					PathTextField.setFocusTraversable(true);

					BaseDisconnectButton.setVisible(false);
					BaseConnectionButton.setDisable(false);

					ConnectionStatusStage = 0;
				}
			}
			else if(N4jC.isConnectionErr()) {
				N4jC.setConnectionErr(false);
				ConnectionStatus.setText("Status połączenia: Błąd - podana ścieżka nie istnieje");
				ConnectionStatusIndicator.setFill(Color.RED);

				PathTextField.setEditable(true);
				PathTextField.setFocusTraversable(true);

				BaseDisconnectButton.setVisible(false);
				BaseConnectionButton.setDisable(false);

				ConnectionStatusStage = 0;
			}
			else {
				ConnectionStatus.setText("Status połączenia: Połączono");
				ConnectionStatusIndicator.setFill(Color.GREEN);

				ConnectionStatusStage = 5;

				BaseDisconnectButton.setVisible(true);
				AlgorithmChooser.setVisible(true);
				AlgorithmTextField.setVisible(true);
			}
        });
		StatusDelay.play();
    }

	@FXML
	private void DisconnectButtonAction() {

		Thread StopConnection =new Thread(N4jC);
		StopConnection.start();

		ConnectionStatusIndicator.setFill(Color.YELLOW);

		StatusDelay.setOnFinished(delayEvent -> {
			if(N4jC.isConnected()) {
				switch (ConnectionStatusStage) {
	            case 5:  ConnectionStatus.setText("Status połączenia: Rozłączanie");
	                     break;
	            case 6:  ConnectionStatus.setText(ConnectionStatus.getText() + ".");
	                     break;
	            case 7:  ConnectionStatus.setText(ConnectionStatus.getText() + "..");
	                     break;
	            case 8:  ConnectionStatus.setText(ConnectionStatus.getText() + "...");
	                     break;
	            case 9:  ConnectionStatus.setText("Status połączenia: Rozłączanie");
	                     break;
	            default: ConnectionStatus.setText("Status połączenia: Rozłączanie");
	                     break;
				}
				ConnectionStatusStage++;
				if(ConnectionStatusStage == 10)
					ConnectionStatusStage = 5;

				StatusDelay.play();
			}
			else {
				ConnectionStatus.setText("Status połączenia: Brak połączenia z bazą");
				ConnectionStatusIndicator.setFill(Color.RED);
				
				PathTextField.setEditable(true);
				PathTextField.setFocusTraversable(true);
				
				BaseDisconnectButton.setVisible(false);
				BaseConnectionButton.setDisable(false);
				
				ConnectionStatusStage = 0;	
			}
        });
		StatusDelay.play();
		AlgorithmChooser.setVisible(false);
		AlgorithmTextField.setVisible(false);
	}

	@FXML
	private void AlgorithmChooserAction(){
		AlgorithmChooser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if(!Objects.equals(newValue, "")){
				StartButton.setVisible(true);
			}
			else{
				StartButton.setVisible(false);
			}

			if(Objects.equals(newValue, "A*")){
			   StartNodeTF.setVisible(true);
			   EndNodeTF.setVisible(true);

			}
			else {
			   StartNodeTF.setVisible(false);
			   EndNodeTF.setVisible(false);
			}
        });
	}

	@FXML
	private void StartAction() {

		switch (AlgorithmChooser.getSelectionModel().getSelectedItem()) {

        	case "A*":  	Alg_A_Star();
							break;

        	case "test":  	System.out.println("test");
        					break;
		}
	}

	private void setN4jC(Neo4jConnection n4jC) {
		N4jC = n4jC;
	}

	private void Alg_A_Star(){
		if(StartNodeTF.getText().matches("[0-9]+") && EndNodeTF.getText().matches("[0-9]+")) {
			Map<String, Map<String, Double>> heuristic = new HashMap<>();
			GraphAStar<String> graph = new GraphAStar<>(heuristic);
			Algorithm_A_star<String> A_star = new Algorithm_A_star<>(N4jC.getDriver(), graph);
			A_star_Thread A_star_thread = new A_star_Thread(A_star.getNodesList(), A_star.getRelationsList(), heuristic,
					graph, A_star);
			A_star_thread.setRoute(StartNodeTF.getText(),EndNodeTF.getText());

			Boolean startExists = false;
			Boolean endExists = false;

			for ( Record node : A_star.getNodesList() ){
				if(StartNodeTF.getText().equals(node.get(0).toString()))
					startExists = true;
				if(EndNodeTF.getText().equals(node.get(0).toString()))
					endExists = true;
			}

			if(startExists && endExists){
				Thread Start_A_star = new Thread(A_star_thread);
				Start_A_star.start();
			}
			if(!startExists && !endExists){
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Błąd!");
				alert.setHeaderText(null);
				alert.setContentText("Podane wartości węzła początkowego oraz końcowego nie występują w bazie.");

				alert.showAndWait();
			}
			else if(!startExists){
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Błąd!");
				alert.setHeaderText(null);
				alert.setContentText("Podana wartość węzła początkowego nie występuje w bazie.");

				alert.showAndWait();
			}
			else if(!endExists){
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Błąd!");
				alert.setHeaderText(null);
				alert.setContentText("Podana wartość węzła końcowego nie występuje w bazie.");

				alert.showAndWait();
			}
		}
		else {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Błąd!");
			alert.setHeaderText(null);
			alert.setContentText("Należy wprowadzić poprawne wartości węzła początkowego i końcowego " +
					"(Wartości muszą być liczbami całkowitymi).");

			alert.showAndWait();
		}
	}
}
