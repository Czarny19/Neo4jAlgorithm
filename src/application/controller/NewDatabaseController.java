package application.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class NewDatabaseController implements Initializable{
	
	@FXML
	private Pane newDatabasePane;
	@FXML
	private TextArea newDatabasePrompt;
	
	private static int answer;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		newDatabasePane.setBorder(new Border(new BorderStroke(
				Color.BLACK, 
	            BorderStrokeStyle.SOLID, 
	            CornerRadii.EMPTY, 
	            BorderWidths.DEFAULT)));
		
		newDatabasePrompt.setFocusTraversable(false);	
		newDatabasePrompt.setEditable(false);
			
		answer = 0;
	}
	
	@FXML
	private void yes(ActionEvent event) {
		buttonAction(event);
		answer = 1;
	}
	
	@FXML
	private void no(ActionEvent event) {
		buttonAction(event);
	    answer = 2;
	}
	
	private void buttonAction(ActionEvent event) {
		final Node source = (Node) event.getSource();
	    final Stage stage = (Stage) source.getScene().getWindow();
	    stage.close();
	}
	
	public int answer() {
		return answer;
	}

	void resetAnswer() {
		answer = 0;
	}
}
