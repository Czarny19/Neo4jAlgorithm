package application;
	
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
	
	private PauseTransition delayLoading = new PauseTransition(Duration.seconds(1));
	
	private Stage LoadingStage;
	
	private Parent root;

    @Override
    public void start(Stage MainStage) throws Exception {   
    	
        final FXMLLoader loaderLoadingScreen = new FXMLLoader(getClass().getResource("view/LoadingScreen.fxml"));
        final FXMLLoader loaderMenu = new FXMLLoader(getClass().getResource("view/Menu2.fxml"));
        
        root = (Parent) loaderLoadingScreen.load();
        Scene loadingScene = new Scene(root);
        loadingScene.getStylesheets().add("/application/Custom.css");
                
        LoadingStage = new Stage();
        LoadingStage.initStyle(StageStyle.TRANSPARENT);
        LoadingStage.setScene(loadingScene);       
        LoadingStage.show();
    	
    	root = (Parent) loaderMenu.load();
    	
    	MainStage.setTitle("Neo4j Algorithm");
    	MainStage.getIcons().add(new Image("Icon.png"));
    	MainStage.setScene(new Scene(root));
    	
    	Neo4jConnection N4jC = new Neo4jConnection();
    	
    	MenuController MenuCtr = new MenuController();
    	MenuCtr.initData(N4jC);
    	  	
        delayLoading.setOnFinished(event -> {
        	LoadingStage.hide();
        	MainStage.show();
        });
        delayLoading.play();        
    }
      
	public static void main(String[] args) {
		launch(args);
	} 
}