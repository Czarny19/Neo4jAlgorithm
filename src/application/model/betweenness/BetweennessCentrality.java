package application.model.betweenness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class BetweennessCentrality {

	protected static double INFINITY = 1000000.0; 
	
	private double graphSize;
	
	private HashMap<Integer,NodeBtwns> nodes;
	private HashMap<Integer,ArrayList<Integer>> relations;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private int computedCount;
	
	public BetweennessCentrality(double graphSize, ProgressBar progress, TextField progressPrompt) {
		this.graphSize = graphSize;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
	} 
	 
	public void init(HashMap<Integer,NodeBtwns> Nodes, HashMap<Integer,ArrayList<Integer>> Relations) { 
		this.nodes = Nodes; 
		this.relations = Relations;
	}
	 
	public void compute() { 
		if (!nodes.isEmpty()) { 
			initAllNodes(nodes); 
			
			progressPrompt.setText("Wykonywana operacja: OBLICZANIE");

			computedCount = 0;
			nodes.forEach((nodeId,node) -> {
				PriorityQueue<NodeBtwns> SimpleExploreResult = null;
				
				SimpleExploreResult = simpleExplore(node); 
				
				while (!SimpleExploreResult.isEmpty()) {
					NodeBtwns foundNode = SimpleExploreResult.poll();
		
					for (NodeBtwns nodeOfSPredecessor : foundNode.predecessors()) {
						double deltaAdd = ((nodeOfSPredecessor.sigma() / foundNode.sigma()) * (1.0 + foundNode.delta()));
						nodeOfSPredecessor.setDelta(nodeOfSPredecessor.delta() + deltaAdd);
					}
					if (foundNode != node) {
						foundNode.setCentrality(foundNode.centrality() + foundNode.delta());
					}
				}
				progress.setProgress(0.4 + (computedCount++/graphSize)/2.5);
			});
		}
	} 
	
	private PriorityQueue<NodeBtwns> simpleExplore(NodeBtwns source) { 
		LinkedList<NodeBtwns> Q = new LinkedList<NodeBtwns>(); 
		PriorityQueue<NodeBtwns> S = new PriorityQueue<NodeBtwns>((int)graphSize, new NodeComparator()); 
	 
		setupAllNodes(nodes); 
		source.setSigma(1.0);
		source.setDistance(0.0);
		Q.add(source); 
	 
		while (!Q.isEmpty()) { 
			NodeBtwns nodeFrom = Q.removeFirst(); 	 
			S.add(nodeFrom); 
			if(relations.get(nodeFrom.id()) != null) {
				for(Integer nodeRelated : relations.get(nodeFrom.id())) { 
					NodeBtwns nodeTo = nodes.get(nodeRelated);
		 
					if (nodeTo.distance() == INFINITY) { 
						nodeTo.setDistance(nodeFrom.distance()+1);
						Q.add(nodeTo); 
					} 
					if (nodeTo.distance() == (nodeFrom.distance()+1)) { 
						nodeTo.setSigma(nodeTo.sigma() + nodeFrom.sigma());
						addToPredecessorsOf(nodeTo, nodeFrom);
					} 
				} 
			}
		}
		return S; 
	}
	  
	private void addToPredecessorsOf(NodeBtwns node, NodeBtwns predecessor) { 
		node.predecessors().add(predecessor);
	} 
		 
	private void clearPredecessorsOf(NodeBtwns node) { 
		HashSet<NodeBtwns> set = new HashSet<NodeBtwns>(); 
		node.setPredecessors(set);
	} 
		 
	private void initAllNodes(HashMap<Integer,NodeBtwns> Nodes) { 
		Nodes.forEach((id,node) -> node.setCentrality(0.0)); 
	} 

	private HashMap<Integer,NodeBtwns> setupAllNodes(HashMap<Integer,NodeBtwns> Nodes) { 
		Nodes.forEach((id,node) -> {
			node.setDistance(INFINITY);
			node.setDelta(0);
			node.setSigma(0);
			clearPredecessorsOf(node);
		});
		return Nodes;
	} 
}