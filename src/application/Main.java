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
	private final boolean isDeploy = false;
	// application.resorce.LoadingScreen.fxml
	// ImageView
	// Deploy
	// Image url = /src/application/resource/WAT-logo.jpg
	// Dev
	// Image url = @../resource/WAT-logo.jpg

    @Override
    public void start(Stage mainStage) throws Exception {
    	String menuPath;
    	String loadingPath;
    	String cssPath;
    	String iconPath;
    	
    	if(isDeploy) {
    		menuPath = "/src/application/view/Menu.fxml";
    		loadingPath = "/src/application/view/LoadingScreen.fxml";
    		cssPath = "/src/application/resource/Custom.css";
    		iconPath = "/src/application/resource/Icon.png";
    	}
    	else {
    		menuPath = "view/Menu.fxml";
    		loadingPath = "view/LoadingScreen.fxml";
    		cssPath = "application/resource/Custom.css";
    		iconPath = "application/resource/Icon.png";
    	}
    	
    	final PauseTransition delay = new PauseTransition(Duration.seconds(1));	
    	
        final FXMLLoader loadingScreenLoader = new FXMLLoader(getClass().getResource(loadingPath));
        final FXMLLoader menuLoder = new FXMLLoader(getClass().getResource(menuPath));

        Parent root = loadingScreenLoader.load();
        Scene loadingScene = new Scene(root);
        
        loadingScene.getStylesheets().add(cssPath);
                   
        loading = new Stage();
        loading.getIcons().add(new Image(iconPath));
        loading.initStyle(StageStyle.TRANSPARENT);
        loading.setScene(loadingScene);         
        loading.show();
    	
    	root = menuLoder.load();
    	
    	mainStage.getIcons().add(new Image(iconPath));
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
