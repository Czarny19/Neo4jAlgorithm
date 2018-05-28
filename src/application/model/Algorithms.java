package application.model;

import java.io.IOException;
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
import javafx.scene.control.TextField;

public class Algorithms {
	
	private StopWatch execTime;
	private ProgressBar progress;
	private FileCreator algInfo;
	private TextField progressPrompt;
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH;mm;ss");
	Date date = new Date();
	
	public Algorithms(ProgressBar progress, TextField progressPrompt, FileCreator algInfo) {
		this.progress = progress;
		this.algInfo = algInfo;
		this.progressPrompt = progressPrompt;
	}
	
	public void setStopWatch(StopWatch execTime) {
		this.execTime = execTime;
	}
	
	private void saveExecTimeToFile() {
		algInfo.addLine("Czas wykonania algorytmu : ");
		algInfo.addLine(execTime.getTime()/3600000 + " h; " +
				execTime.getTime()/60000 + " m; " +
				execTime.getTime()/1000 + " s; " +
				execTime.getTime()%1000 + " ms;");
	}

	public void AStar(Driver neo4jDriver, String startNode, String endNode, String distanceKey) throws InterruptedException{
		if(startNode.matches("[0-9]+") && endNode.matches("[0-9]+") && distanceKey != null) {
			final int startId = Integer.parseInt(startNode);
			final int endId = Integer.parseInt(endNode);
			
			AStarThread aStarThread = new AStarThread(neo4jDriver, startId, endId, distanceKey, progress);
			
			if(aStarThread.startExists() && aStarThread.endExists()) {
				progress.setVisible(true);
				progressPrompt.setVisible(true);
				Thread Start_A_star = new Thread(aStarThread);
				Start_A_star.start();
				Start_A_star.join();
				
				algInfo.setFileName("Astar" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
				saveExecTimeToFile();
				aStarThread.algExecToFile(algInfo);
				try {
					algInfo.create();
					afterAlg(execTime,"Astar");
				} catch (IOException e) {
					afterAlgFailedToSave(execTime,"Astar");
				}
				
				progress.setVisible(false);
				progressPrompt.setVisible(false);
			}
			else
				aStarMessage(aStarThread.startExists(), aStarThread.endExists(), false);
		}		
		else
			aStarMessage(true, true, true);
	}
	
	public void betweennessCentrality(Driver neo4jDriver, boolean isDirected) throws InterruptedException {
		progress.setVisible(true);
		progressPrompt.setVisible(true);
		BCThread BCThread = new BCThread(neo4jDriver,isDirected);
		Thread Start_BC = new Thread(BCThread);
		Start_BC.start();
		Start_BC.join();
		
		algInfo.setFileName("Betweenness" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
		saveExecTimeToFile();
		BCThread.algExecToFile(algInfo);
		try {
			algInfo.create();
			afterAlg(execTime,"Betweenness");
		} catch (IOException e) {
			afterAlgFailedToSave(execTime,"Betweenness");
		}
	}
	
	public void graphColoring(Driver neo4jDriver) throws InterruptedException {
		progress.setVisible(true);
		progressPrompt.setVisible(true);
		GraphColoring graphColoring = new GraphColoring(neo4jDriver);
		Thread Start_Coloring = new Thread(graphColoring);
		Start_Coloring.start();
		Start_Coloring.join();
		
		algInfo.setFileName("Kolorowanie grafu" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
		saveExecTimeToFile();
		
		graphColoring.algExecToFile(algInfo);
		try {
			algInfo.create();
			afterAlg(execTime,"Kolorowanie grafu");
		} catch (IOException e) {
			afterAlgFailedToSave(execTime,"Kolorowanie grafu");
		}
	}
	
	public void degreeCentrality(Driver neo4jDriver, boolean isIndegree, boolean isOutdegree, boolean isBoth) throws InterruptedException {
		if(!isIndegree && !isOutdegree && !isBoth) {
			optionsNotSelectedMessage();
		}
		if(isIndegree || isOutdegree || isBoth){
			progress.setVisible(true);
			progressPrompt.setVisible(true);
			DegreeCentrality degreeCentrality = new DegreeCentrality(neo4jDriver, isIndegree, isOutdegree, isBoth);
			Thread Start_Degree = new Thread(degreeCentrality);
			Start_Degree.start();
			Start_Degree.join();
			
			algInfo.setFileName("Degree" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
			saveExecTimeToFile();
			
			degreeCentrality.algExecToFile(algInfo);
			try {
				algInfo.create();
				afterAlg(execTime,"Degree");
			} catch (IOException e) {
				afterAlgFailedToSave(execTime,"Degree");
			}
		}
	}
	
	public void vertexConnectivity(Driver neo4jDriver, boolean doNodes, boolean doEdges) throws InterruptedException {
		if(!doNodes && !doEdges) {
			optionsNotSelectedMessage();
		}
		else {
			progress.setVisible(true);
			progressPrompt.setVisible(true);
			ConnectivityThread ConnectivityThread = new ConnectivityThread(neo4jDriver, doNodes, doEdges);
			Thread Start_Conn = new Thread(ConnectivityThread);
			Start_Conn.start();
			Start_Conn.join();
			
			algInfo.setFileName("Connectivity" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
			saveExecTimeToFile();
			
			ConnectivityThread.algExecToFile(algInfo);
			try {
				algInfo.create();
				afterAlg(execTime,"Connectivity");
			} catch (IOException e) {
				afterAlgFailedToSave(execTime,"Connectivity");
			}
		}
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
					"(Wartoœci musz¹ byæ liczbami ca³kowitymi) oraz wybraæ klucz odleg³oœci.");
		}
		alert.showAndWait();
	}
	
	private void optionsNotSelectedMessage() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("B³¹d!");
		alert.setHeaderText(null);
		alert.setContentText("Nale¿y zaznaczyæ przynajmniej jeden z warunków.");
		alert.showAndWait();
	}
	
	private void afterAlg(StopWatch execTime, String algorithmName) {
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
						"(" + dateFormat.format(date).replaceAll(" ", "_") + ")" + ".txt");
		alert.showAndWait();
	}
	
	private void afterAlgFailedToSave(StopWatch execTime, String algorithmName) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(algorithmName);
		alert.setHeaderText("Zakoñczono pracê algorytmu");
		alert.setContentText(
						"Algorytm zakoñczy³ dzia³anie po " + 
						execTime.getTime()/3600000 + " h; " +
						execTime.getTime()/60000 + " m; " +
						execTime.getTime()/1000 + " s; " +
						execTime.getTime()%1000 + " ms;" );
		alert.showAndWait();
	}
}
