package application.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.StopWatch;
import org.neo4j.driver.v1.Driver;

import application.model.astar.AStarThread;
import application.model.betweenness.BCThread;
import application.model.coloring.GraphColoring;
import application.model.connectivity.ConnectivityThread;
import application.model.degree.DegreeCentrality;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;

public class Algorithms {
	
	private StopWatch execTime;
	private ProgressBar progress;
	
	public Algorithms(ProgressBar progress) {
		this.progress = progress;
	}
	
	public void setStopWatch(StopWatch execTime) {
		this.execTime = execTime;
	}

	public void AStar(Driver neo4jDriver, String startNode, String endNode, String distanceKey) throws InterruptedException{
		if(startNode.matches("[0-9]+") && endNode.matches("[0-9]+")) {
			final int startId = Integer.parseInt(startNode);
			final int endId = Integer.parseInt(endNode);
			AStarThread aStarThread = new AStarThread(neo4jDriver, startId, endId, distanceKey, progress);
			if(aStarThread.startExists() && aStarThread.endExists()) {
				Thread Start_A_star = new Thread(aStarThread);
				Start_A_star.start();
				Start_A_star.join();
				afterAlg(execTime,"A*");
			}
			else
				aStarMessage(aStarThread.startExists(), aStarThread.endExists(), false);
		}		
		else
			aStarMessage(true, true, true);
	}
	
	public void betweennessCentrality(Driver neo4jDriver, boolean isDirected) {
		BCThread BCDirectedThread = new BCThread(neo4jDriver,isDirected);
		BCDirectedThread.start();
	}
	
	public void graphColoring(Driver neo4jDriver) {
		GraphColoring graphColoring = new GraphColoring(neo4jDriver);
		graphColoring.colourVertices();
	}
	
	public void degreeCentrality(Driver neo4jDriver, boolean isIndegree, boolean isOutdegree, boolean isBoth) {
		if(!isIndegree && !isOutdegree && !isBoth) {
			degreeCentralityMessage();
		}
		if(isIndegree || isOutdegree || isBoth){
			DegreeCentrality degreeCentrality = new DegreeCentrality(neo4jDriver, isIndegree, isOutdegree, isBoth);
			degreeCentrality.compute();
		}
	}
	
	public void vertexConnectivity(Driver neo4jDriver, boolean doEdges) {
		ConnectivityThread VCThread = new ConnectivityThread(neo4jDriver);
		VCThread.compute(doEdges);
	}
	
	private void aStarMessage(boolean startExists, boolean endExists, boolean badFormat) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("B³¹d!");
		alert.setHeaderText(null);
		
		if(!startExists && !endExists) {
			alert.setContentText("Podane wartoœci wêz³a pocz¹tkowego oraz koñcowego nie wystêpuj¹ w bazie.");
		}
		if(!startExists && endExists) {
			alert.setContentText("Podana wartoœæ wêz³a pocz¹tkowego nie wystêpuje w bazie.");
		}
		if(startExists && !endExists) {
			alert.setContentText("Podana wartoœæ wêz³a koñcowego nie wystêpuje w bazie.");
		}
		if(badFormat) {
			alert.setContentText(
					"Nale¿y wprowadziæ poprawne wartoœci wêz³a pocz¹tkowego i koñcowego " +
					"(Wartoœci musz¹ byæ liczbami ca³kowitymi).");
		}
		alert.showAndWait();
	}
	
	private void degreeCentralityMessage() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("B³¹d!");
		alert.setHeaderText(null);
		alert.setContentText("Nale¿y zaznaczyæ przynajmniej jeden z warunków.");
		alert.showAndWait();
	}
	
	private void afterAlg(StopWatch execTime, String algorithmName) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(algorithmName);
		alert.setHeaderText("Zakoñczono pracê algorytmu");
		alert.setContentText(
						"Algorytm zakoñczy³ dzia³anie po " + 
						execTime.getTime()/3600000 + " h; " +
						execTime.getTime()/60000 + " m; " +
						execTime.getTime()/1000 + " s; " +
						execTime.getTime()%1000 + " ms;" + "\n\n" +
						"Plik z informacjami na temat wyników algorytmu " +
						"zosta³ zapisany w folderze AlgResults wewn¹trz folderu " +
						"bazy danych.\n\n" +
						"Nazwa pliku : " + algorithmName +
						"(" + dateFormat.format(date) + ")" + ".txt");
		alert.showAndWait();
	}
}
