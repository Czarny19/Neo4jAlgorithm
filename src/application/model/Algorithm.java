package application.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.StopWatch;
import org.neo4j.driver.v1.Driver;

import application.model.astar.AStarThread;
import application.model.betweenness.BCThread;
import application.model.coloring.GraphColoringThread;
import application.model.connectivity.ConnectivityThread;
import application.model.degree.DegreeCentralityThread;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class Algorithm {
	
	private Driver neo4jDriver;
	private ProgressBar progress;
	private TextField progressPrompt;
	private FileCreator algInfo;
	private StopWatch execTime;
	
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH;mm;ss");
	private final Date date = new Date();
	
	public Algorithm(Driver neo4jDriver, ProgressBar progress, TextField progressPrompt, FileCreator algInfo, StopWatch execTime) {
		this.neo4jDriver = neo4jDriver;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		this.algInfo = algInfo;	
		this.execTime = execTime;
	}

	public void aStar(String startNode, String endNode, String distanceKey) throws InterruptedException{		
		final int startId = Integer.parseInt(startNode);
		final int endId = Integer.parseInt(endNode);
		
		if(startNode.matches("[0-9]+") && endNode.matches("[0-9]+") && distanceKey != null) {
			AStarThread aStarThread = new AStarThread(neo4jDriver, startId, endId, distanceKey, progress, progressPrompt);
			
			if(aStarThread.startNodeExists() && aStarThread.endNodeExists()) {			
				progress.setVisible(true);
				progressPrompt.setVisible(true);
				
				Thread aStar = new Thread(aStarThread);
				aStar.start();
				aStar.join();
				
				algInfo.setFileName("Astar" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
				saveExecTimeToFile();
				aStarThread.algExecToFile(algInfo);
				try {
					algInfo.create();
					algSucces(execTime,"Astar");
				}catch(IOException e) {
					algFail(execTime,"Astar");
				}
				
				progress.setProgress(0);
				progressPrompt.setText("");				
				progress.setVisible(false);
				progressPrompt.setVisible(false);	
			}
			else {
				aStarMessage(aStarThread.startNodeExists(), aStarThread.endNodeExists(), false);
			}
		}		
		else {
			aStarMessage(true, true, true);
		}
	}
	
	public void betweennessCentrality(boolean isDirected) throws InterruptedException {		
		progress.setVisible(true);
		progressPrompt.setVisible(true);
		
		BCThread betweennessThread = new BCThread(neo4jDriver,isDirected, progress, progressPrompt);
		Thread betweennessCentrality = new Thread(betweennessThread);
		betweennessCentrality.start();
		betweennessCentrality.join();
		
		algInfo.setFileName("Betweenness" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
		saveExecTimeToFile();
		betweennessThread.algExecToFile(algInfo);
		try {
			algInfo.create();
			algSucces(execTime,"Betweenness");
		} catch (IOException e) {
			algFail(execTime,"Betweenness");
		}
		
		progress.setProgress(0);
		progressPrompt.setText("");		
		progress.setVisible(false);
		progressPrompt.setVisible(false);	
	}
	
	public void graphColoring() throws InterruptedException {		
		progress.setVisible(true);
		progressPrompt.setVisible(true);
		
		GraphColoringThread graphColoringThread = new GraphColoringThread(neo4jDriver, progress, progressPrompt);
		Thread graphColoring = new Thread(graphColoringThread);
		graphColoring.start();
		graphColoring.join();
		
		algInfo.setFileName("Coloring" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
		saveExecTimeToFile();		
		graphColoringThread.algExecToFile(algInfo);
		try {
			algInfo.create();
			algSucces(execTime,"Coloring");
		} catch (IOException e) {
			algFail(execTime,"Coloring");
		}
		
		progress.setProgress(0);
		progressPrompt.setText("");		
		progress.setVisible(false);
		progressPrompt.setVisible(false);	
	}
	
	public void degreeCentrality(boolean isIndegree, boolean isOutdegree, boolean isDegree) throws InterruptedException {
		if(!isIndegree && !isOutdegree && !isDegree) {
			optionsNotSelectedMessage();
		}
		if(isIndegree || isOutdegree || isDegree){			
			progress.setVisible(true);
			progressPrompt.setVisible(true);
			
			DegreeCentralityThread degreeCentralityThread = new DegreeCentralityThread(
					neo4jDriver, 
					isIndegree, 
					isOutdegree, 
					isDegree,
					progress, 
					progressPrompt);
			Thread degreeCentrality = new Thread(degreeCentralityThread);
			degreeCentrality.start();
			degreeCentrality.join();
			
			algInfo.setFileName("Degree" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
			saveExecTimeToFile();			
			degreeCentralityThread.algExecToFile(algInfo);
			try {
				algInfo.create();
				algSucces(execTime,"Degree");
			} catch (IOException e) {
				algFail(execTime,"Degree");
			}
			
			progress.setProgress(0);
			progressPrompt.setText("");			
			progress.setVisible(false);
			progressPrompt.setVisible(false);			
		}
	}
	
	public void connectivity(boolean doNodes, boolean doEdges) throws InterruptedException {
		if(!doNodes && !doEdges) {
			optionsNotSelectedMessage();
		}
		else {			
			progress.setVisible(true);
			progressPrompt.setVisible(true);
			
			ConnectivityThread ConnectivityThread = new ConnectivityThread(neo4jDriver, doNodes, doEdges, progress, progressPrompt);
			Thread connectivity = new Thread(ConnectivityThread);
			connectivity.start();
			connectivity.join();
			
			algInfo.setFileName("Connectivity" + "(" + dateFormat.format(date).replaceAll(" ", "_") + ")");
			saveExecTimeToFile();			
			ConnectivityThread.algExecToFile(algInfo);
			try {
				algInfo.create();
				algSucces(execTime,"Connectivity");
			} catch (IOException e) {
				algFail(execTime,"Connectivity");
			}
			
			progress.setProgress(0);
			progressPrompt.setText("");			
			progress.setVisible(false);
			progressPrompt.setVisible(false);	
		}
	}
	
	private void aStarMessage(boolean startExists, boolean endExists, boolean badFormat) {
		Platform.runLater(new Runnable() {
			@Override public void run() {
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
	    });		
	}
	
	private void optionsNotSelectedMessage() {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        	alert.setTitle("B³¹d!");
	        	alert.setHeaderText(null);
	        	alert.setContentText("Nale¿y zaznaczyæ przynajmniej jeden z warunków.");
	        	alert.showAndWait();
	        }
		});		
	}
	
	private void saveExecTimeToFile() {
		algInfo.addLine("Czas wykonania algorytmu : ");
		algInfo.addLine((execTime.getTime()/3600000)%60 + " h; " +
				(execTime.getTime()/60000)%60 + " m; " +
				(execTime.getTime()/1000)%60 + " s; " +
				execTime.getTime()%1000 + " ms;");
	}
	
	private void algSucces(StopWatch execTime, String algorithmName) {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        	alert.setTitle(algorithmName);
	        	alert.setHeaderText("Zakoñczono pracê algorytmu");
	        	alert.setContentText(
	        		"Algorytm zakoñczy³ dzia³anie po " + 
	        		(execTime.getTime()/3600000)%60 + " h; " +
	        		(execTime.getTime()/60000)%60 + " m; " +
	        		(execTime.getTime()/1000)%60 + " s; " +
	        		(execTime.getTime()%1000) + " ms;" + "\n\n" +
	        		"Plik z informacjami na temat wyników algorytmu " +
	        		"zosta³ zapisany w folderze AlgResults wewn¹trz folderu " +
	        		"bazy danych.\n\n" +
	        		"Nazwa pliku : " + algorithmName +
	        		"(" + dateFormat.format(date).replaceAll(" ", "_") + ")" + ".txt");
	        	alert.showAndWait();
	        }
		});
	}
	
	private void algFail(StopWatch execTime, String algorithmName) {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
        		alert.setTitle(algorithmName);
        		alert.setHeaderText("Zakoñczono pracê algorytmu");
        		alert.setContentText(
        			"Algorytm zakoñczy³ dzia³anie po " + 
        			(execTime.getTime()/3600000)%60 + " h; " +
        			(execTime.getTime()/60000)%60 + " m; " +
        			(execTime.getTime()/1000)%60 + " s; " +
        			(execTime.getTime()%1000) + " ms;" );
        		alert.showAndWait();
			}
		});		
	}
}
