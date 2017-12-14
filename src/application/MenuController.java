package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import application.model.Algorithm_A_star;
import application.model.Neo4jConnection;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

public class MenuController implements Initializable{
	
	@FXML
	private Button BaseConnectionButton;
	@FXML
	private Button BaseDisconnectButton;
	@FXML
	private ChoiceBox<String> AlgorithmChooser;
	@FXML
	private TextField AlgorithmText;
	@FXML
	private TextField ConnectionStatus;
	@FXML
	public Circle ConnectionStatusIndicator;
	@FXML
	private ProgressBar AlgorithmStatusBar;
	@FXML
	private TextField AlgorithmStatusText;
	@FXML
	public TextField PathTextField;
	@FXML
	private Button BrowseButton;
	
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
		
		PathTextField.setText("");
		
		ConnectionStatusStage = 0;
		ConnectionStatus.setText("Status po��czenia: Brak po��czenia z baz�");
		
		ObservableList<String> ChoiceBoxItems = FXCollections.observableArrayList("A*","test");
		AlgorithmChooser.setItems(ChoiceBoxItems);
	}
	
	public void initData(Neo4jConnection N4jC) {
		this.setN4jC(N4jC);
	}
	
	@FXML
	private void BrowseButtonAction(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		
		File selectedDirectory = chooser.showDialog(null);
		
		PathTextField.setText(selectedDirectory.toString());
	}
	
	@FXML
    private void MenuActionConnect(ActionEvent event) {
		
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
		            case 0:  ConnectionStatus.setText("Status po��czenia: Nawi�zywanie po��czenia");
		                     break;
		            case 1:  ConnectionStatus.setText(ConnectionStatus.getText() + ".");
		                     break;
		            case 2:  ConnectionStatus.setText(ConnectionStatus.getText() + "..");
		                     break;
		            case 3:  ConnectionStatus.setText(ConnectionStatus.getText() + "...");
		                     break;
		            case 4:  ConnectionStatus.setText("Status po��czenia: Nawi�zywanie po��czenia");
		                     break;
		            default: ConnectionStatus.setText("Status po��czenia: Nawi�zywanie po��czenia");;
		                     break;
					}
					ConnectionStatusStage = (ConnectionStatusStage+1)%4;
					StatusDelay.play();
				}
				else if(QuestionCtr.getAnswer() == 1) {
					N4jC.initNewPath(PathTextField.getText());
					
					QuestionCtr.setAnswer(0);
					
					Thread StartNewDatabase = new Thread(N4jC); 
					StartNewDatabase.start();
					
					StatusDelay.play();
				}
				else if(QuestionCtr.getAnswer() == 2) {
					ConnectionStatus.setText("Status po��czenia: Brak po��czenia z baz�");
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
				ConnectionStatus.setText("Status po��czenia: B��d - podana �cie�ka nie istnieje");
				ConnectionStatusIndicator.setFill(Color.RED);

				PathTextField.setEditable(true);
				PathTextField.setFocusTraversable(true);
				
				BaseDisconnectButton.setVisible(false);
				BaseConnectionButton.setDisable(false);
				
				ConnectionStatusStage = 0;	
			}
			else {
				ConnectionStatus.setText("Status po��czenia: Po��czono");
				ConnectionStatusIndicator.setFill(Color.GREEN);
				
				ConnectionStatusStage = 5;
				
				BaseDisconnectButton.setVisible(true);
			}
        });		
		StatusDelay.play();  
    }
	
	@FXML
	private void DisconnectButtonAction(ActionEvent event) {
		
		Thread StopConnection =new Thread(N4jC); 
		StopConnection.start();
		
		ConnectionStatusIndicator.setFill(Color.YELLOW);
		
		StatusDelay.setOnFinished(delayEvent -> {
			if(N4jC.isConnected()) {
				switch (ConnectionStatusStage) {
	            case 5:  ConnectionStatus.setText("Status po��czenia: Roz��czanie");
	                     break;
	            case 6:  ConnectionStatus.setText(ConnectionStatus.getText() + ".");
	                     break;
	            case 7:  ConnectionStatus.setText(ConnectionStatus.getText() + "..");
	                     break;
	            case 8:  ConnectionStatus.setText(ConnectionStatus.getText() + "...");
	                     break;
	            case 9:  ConnectionStatus.setText("Status po��czenia: Roz��czanie");
	                     break;
	            default: ConnectionStatus.setText("Status po��czenia: Roz��czanie");;
	                     break;
				}
				ConnectionStatusStage++;
				if(ConnectionStatusStage == 10)
					ConnectionStatusStage = 5;
				
				StatusDelay.play(); 
			}
			else {
				ConnectionStatus.setText("Status po��czenia: Brak po��czenia z baz�");
				ConnectionStatusIndicator.setFill(Color.RED);
				
				PathTextField.setEditable(true);
				PathTextField.setFocusTraversable(true);
				
				BaseDisconnectButton.setVisible(false);
				BaseConnectionButton.setDisable(false);
				
				ConnectionStatusStage = 0;	
			}
        });
		StatusDelay.play();		
	}
	
	@FXML
	private void StartAction() {
		
		switch (AlgorithmChooser.getSelectionModel().getSelectedItem()) {
        	case "A*":  	Algorithm_A_star A_star = new Algorithm_A_star(N4jC.getDriver());
							Thread Start_A_star = new Thread(A_star); 
							Start_A_star.start();
							break;
					
        	case "test":  	System.out.println("test");
        					break;
		}		
	}
	
	public Neo4jConnection getN4jC() {
		return N4jC;
	}

	public void setN4jC(Neo4jConnection n4jC) {
		N4jC = n4jC;
	}
}
