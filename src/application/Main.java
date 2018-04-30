package application;

import application.controller.Menu;
import application.model.Neo4jConnection;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Main extends Application {
	
	private PauseTransition delay = new PauseTransition(Duration.seconds(1));	
	private Stage Loading;

    @Override
    public void start(Stage MainStage) throws Exception {

        final FXMLLoader LoadingScreen = new FXMLLoader(getClass().getResource("view/LoadingScreen.fxml"));
        final FXMLLoader Menu = new FXMLLoader(getClass().getResource("view/Menu.fxml"));

        Parent root = LoadingScreen.load();
        Scene loading = new Scene(root);
        loading.getStylesheets().add("/application/resource/Custom.css");
                
        Loading = new Stage();
        Loading.initStyle(StageStyle.TRANSPARENT);
        Loading.setScene(loading);       
        Loading.show();
    	
    	root = Menu.load();
    	
    	MainStage.setTitle("Neo4j Algorithm");
    	MainStage.getIcons().add(new Image("application/resource/Icon.png"));
    	MainStage.setScene(new Scene(root));
    	MainStage.initStyle(StageStyle.UNIFIED);
    	MainStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                MainStage.setMaximized(false);
        });
    	
    	Neo4jConnection Neo4jConnection = new Neo4jConnection();
    	
    	Menu MenuCtr = new Menu();
    	MenuCtr.initData(Neo4jConnection);
    	  	
        delay.setOnFinished(event -> {
        	Loading.hide();
        	MainStage.show();
        });
        delay.play();        
    }
      
	public static void main(String[] args) {
		launch(args);
	} 
}
