package application.model;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import application.model.astar.AStar;
import application.model.astar.AStarThread;
import application.model.astar.GraphAStar;
import application.model.betweenness.BCThread;
import application.model.coloring.GraphColoring;
import application.model.connectivity.VCThread;
import application.model.degree.DegreeCentrality;
import javafx.scene.control.Alert;

/// B��d przy wpisywaniu z�ego startu i ko�ca !!!!
public class Algorithms {
	
	private Driver Neo4jDriver;
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
				aStarMessage(startExists, endExists, false);
		}
		else {
			aStarMessage(true, true, true);
		}
	}
	
	public void betweennessCentrality(Driver Neo4jDriver, boolean isDirected) {
		BCThread BCDirectedThread = new BCThread(Neo4jDriver,isDirected);
		BCDirectedThread.start();
	}
	
	public void graphColoring(Driver Neo4jDriver) {
		GraphColoring graphColoring = new GraphColoring(Neo4jDriver);
		graphColoring.colourVertices();
	}
	
	public void degreeCentrality(Driver Neo4jDriver, boolean isIndegree, boolean isOutdegree, boolean isBoth) {
		if(!isIndegree && !isOutdegree && !isBoth) {
			degreeCentralityMessage();
		}
		if(isIndegree || isOutdegree || isBoth){
			DegreeCentrality degreeCentrality = new DegreeCentrality(Neo4jDriver, isIndegree, isOutdegree, isBoth);
			degreeCentrality.compute();
		}
	}
	
	public void vertexConnectivity(Driver Neo4jDriver, boolean doEdges) {
		VCThread VCThread = new VCThread(Neo4jDriver);
		VCThread.compute(doEdges);
	}
	
	private void aStarMessage(boolean startExists, boolean endExists, boolean badFormat) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("B��d!");
		alert.setHeaderText(null);
		
		if(!startExists && !endExists) {
			alert.setContentText("Podane warto�ci w�z�a pocz�tkowego oraz ko�cowego nie wyst�puj� w bazie.");
		}
		if(!startExists && endExists) {
			alert.setContentText("Podana warto�� w�z�a pocz�tkowego nie wyst�puje w bazie.");
		}
		if(startExists && !endExists) {
			alert.setContentText("Podana warto�� w�z�a ko�cowego nie wyst�puje w bazie.");
		}
		if(badFormat) {
			alert.setContentText(
					"Nale�y wprowadzi� poprawne warto�ci w�z�a pocz�tkowego i ko�cowego " +
					"(Warto�ci musz� by� liczbami ca�kowitymi).");
		}
		alert.showAndWait();
	}
	
	private void degreeCentralityMessage() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("B��d!");
		alert.setHeaderText(null);
		alert.setContentText("Nale�y zaznaczy� przynajmniej jeden z warunk�w.");
		alert.showAndWait();
	}
}
