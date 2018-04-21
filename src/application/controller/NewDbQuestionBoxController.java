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

public class NewDbQuestionBoxController implements Initializable{
	
	@FXML
	private Pane NewDBQuestion;
	@FXML
	private TextArea InfoBox;
	
	private static int Answer;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		NewDBQuestion.setBorder(new Border(new BorderStroke(
				Color.BLACK, 
	            BorderStrokeStyle.SOLID, 
	            CornerRadii.EMPTY, 
	            BorderWidths.DEFAULT)));
		
		InfoBox.setEditable(false);
		InfoBox.setFocusTraversable(false);		
		Answer = 0;
	}
	
	@FXML
	private void yesButtonAction(ActionEvent event) {
		buttonAction(event);
		Answer = 1;
	}
	
	@FXML
	private void noButtonAction(ActionEvent event) {
		buttonAction(event);
	    Answer = 2;
	}
	
	private void buttonAction(ActionEvent event) {
		final Node source = (Node) event.getSource();
	    final Stage stage = (Stage) source.getScene().getWindow();
	    stage.close();
	}
	
	int answer() {
		return Answer;
	}

	void resetAnswer() {
		Answer = 0;
	}

}
