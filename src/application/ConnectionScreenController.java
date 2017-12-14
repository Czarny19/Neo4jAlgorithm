package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import application.model.Neo4jConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ConnectionScreenController implements Initializable{
	
	@FXML
	private Pane ConnectionPane;
	@FXML
	private TextField PathText;
	@FXML
	private TextField PathInput;
	@FXML
	private Button ConnectButton;
	@FXML
	private Button CancelButton;
	@FXML
	private Button BrowseButton;

	private static Stage ConnectionStage;
	
	private static boolean isOpen = false;
	
	private Neo4jConnection N4jC;
	
	public ConnectionScreenController(){
		
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		PathText.setBackground(Background.EMPTY);
	}
	
	public void initData(Neo4jConnection N4jC, Stage ConnectionStage) {
		this.setN4jC(N4jC);
		ConnectionScreenController.ConnectionStage = ConnectionStage;
		isOpen = true;	
	}
	
	@FXML
    private void CancelButtonAction(ActionEvent event) {
		isOpen = false;
    	ConnectionStage.close();  
    }
	
	@FXML
	private void BrowseButtonAction(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File selectedDirectory = chooser.showDialog(null);
		if(!selectedDirectory.getPath().isEmpty())
			PathInput.setText(selectedDirectory.toString());
	}
	
	@FXML
    private void ConnectButtonAction(ActionEvent event) throws Exception {
    	if(!PathInput.getText().isEmpty()) {	
	
    		ConnectionStage.close();
    		
    		//N4jC.init(ConnectionStage);		
    		//N4jC.start();    			    	
    		isOpen = false;	 
    	}
    }
	
	public boolean isOpen() {
		return isOpen;
	}

	public Neo4jConnection getN4jC() {
		return N4jC;
	}

	public void setN4jC(Neo4jConnection n4jC) {
		N4jC = n4jC;
	}

}
