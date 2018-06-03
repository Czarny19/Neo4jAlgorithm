package application;

import application.controller.MenuController;
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
	
	private Stage loading;

    @Override
    public void start(Stage mainStage) throws Exception {
    	final PauseTransition delay = new PauseTransition(Duration.seconds(1));	
    	
        final FXMLLoader loadingScreenLoader = new FXMLLoader(getClass().getResource("view/LoadingScreen.fxml"));
        final FXMLLoader menuLoder = new FXMLLoader(getClass().getResource("view/Menu.fxml"));

        Parent root = loadingScreenLoader.load();
        Scene loadingScene = new Scene(root);
        
        loadingScene.getStylesheets().add("/application/resource/Custom.css");
                   
        loading = new Stage();
        loading.getIcons().add(new Image("application/resource/Icon.png"));
        loading.initStyle(StageStyle.TRANSPARENT);
        loading.setScene(loadingScene);         
        loading.show();
    	
    	root = menuLoder.load();
    	
    	mainStage.getIcons().add(new Image("application/resource/Icon.png"));
    	mainStage.setTitle("Neo4j Algorithm");    	
    	mainStage.setScene(new Scene(root));
    	mainStage.initStyle(StageStyle.UNIFIED);
    	mainStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                mainStage.setMaximized(false);
            }
        });
    	
    	Neo4jConnection neo4jConnection = new Neo4jConnection();
    	
    	MenuController menuController = menuLoder.getController();
    	menuController.initNeo4jConnection(neo4jConnection);
    	  	
        delay.setOnFinished(event -> {
        	loading.hide();
        	mainStage.show();
        });
        delay.play();        
    }
      
	public static void main(String[] args) {
		launch(args);
	} 
}
