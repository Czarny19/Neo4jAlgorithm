package application.model;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import application.model.astar.AStar;
import application.model.astar.AStarThread;
import application.model.astar.GraphAStar;
import application.model.betweenness.BetweennessCentralityThread;
import javafx.scene.control.Alert;

/// B³¹d przy wpisywaniu z³ego startu i koñca !!!!
public class Algorithms {
	
	private Driver Neo4jDriver;
	// A* algorithm variables
	private String StartNode;
	private String EndNode;
	
	public void initAStar(String StartNode, String EndNode, Driver Neo4jDriver) {
		this.StartNode = StartNode;
		this.EndNode = EndNode;
		this.Neo4jDriver = Neo4jDriver;
	}

	public void AStar(){
		if(StartNode.matches("[0-9]+") && EndNode.matches("[0-9]+")) {

			Boolean startExists = false;
			Boolean endExists = false;
			
			Map<String, Map<String, Double>> heuristic = new HashMap<>();
			GraphAStar<String> graph = new GraphAStar<>(heuristic);
			
			AStar<String> AStar = new AStar<>(
					Neo4jDriver, 
					graph, 
					Integer.parseInt(StartNode), 
					Integer.parseInt(EndNode));

			for ( Record node : AStar.getNodesList() ){
				if(StartNode.equals(node.get(0).toString()))
					startExists = true;
				if(EndNode.equals(node.get(0).toString()))
					endExists = true;
			}

			if(startExists && endExists){
				
				AStarThread AStarThread = new AStarThread(
						AStar.getNodesList(), 
						AStar.getRelationsList(), 
						heuristic, 
						graph, 
						AStar);
				
				AStarThread.setRoute(StartNode,EndNode);
				
				Thread Start_A_star = new Thread(AStarThread);
				Start_A_star.start();
			}
			else
				AStarMessage(startExists, endExists, false);
		}
		else {
			AStarMessage(true, true, true);
		}
	}
	
	private void AStarMessage(boolean startExists, boolean endExists, boolean badFormat) {
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
	
	public void betweennessCentrality(Driver Neo4jDriver) {
		BetweennessCentralityThread BCThread = new BetweennessCentralityThread(Neo4jDriver);
		BCThread.start();
	}
}
