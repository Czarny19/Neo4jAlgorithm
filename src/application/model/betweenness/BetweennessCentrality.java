package application.model.betweenness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class BetweennessCentrality {

	protected static double INFINITY = 1000000.0; 
	
	private double graphSize;
	
	private HashMap<Integer,NodeBtwns> Nodes;
	private HashMap<Integer,ArrayList<Integer>> Relations;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private int computedCount;
	
	public BetweennessCentrality(double graphSize, ProgressBar progress, TextField progressPrompt) {
		this.graphSize = graphSize;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
	} 
	 
	public void init(HashMap<Integer,NodeBtwns> Nodes, HashMap<Integer,ArrayList<Integer>> Relations) { 
		this.Nodes = Nodes; 
		this.Relations = Relations;
	}
	 
	public void compute() { 
		if (!Nodes.isEmpty()) { 
			betweennessCentrality(); 
		}
	} 
	
	public void betweennessCentrality() { 
		initAllNodes(Nodes); 
		
		progressPrompt.setText("Wykonywana operacja: OBLICZANIE");

		computedCount = 0;
		Nodes.forEach((id,node) -> {
			PriorityQueue<NodeBtwns> SimpleExploreResult = null;
			
			SimpleExploreResult = simpleExplore(node); 
			
			while (!SimpleExploreResult.isEmpty()) {
				NodeBtwns nodeOfS = SimpleExploreResult.poll();
	
				for (NodeBtwns nodeOfSPredecessor : nodeOfS.predecessors()) {
					double deltaAdd = ((nodeOfSPredecessor.sigma() / nodeOfS.sigma()) * (1.0 + nodeOfS.delta()));
					nodeOfSPredecessor.setDelta(nodeOfSPredecessor.delta() + deltaAdd);
				}
				if (nodeOfS != node) {
					nodeOfS.setCentrality(nodeOfS.centrality() + nodeOfS.delta());
				}
			}
			progress.setProgress(0.4 + (computedCount++/graphSize)/2.5);
		});
	} 
	
	private PriorityQueue<NodeBtwns> simpleExplore(NodeBtwns source) { 
		LinkedList<NodeBtwns> Q = new LinkedList<NodeBtwns>(); 
		PriorityQueue<NodeBtwns> S = new PriorityQueue<NodeBtwns>((int)graphSize, new BrandesNodeComparatorLargerFirst()); 
	 
		setupAllNodes(Nodes); 
		source.setSigma(1.0);
		source.setDistance(0.0);
		Q.add(source); 
	 
		while (!Q.isEmpty()) { 
			NodeBtwns nodeFrom = Q.removeFirst(); 	 
			S.add(nodeFrom); 
			if(Relations.get(nodeFrom.id()) != null) {
				for(Integer nodeRelated : Relations.get(nodeFrom.id())) { 
					NodeBtwns nodeTo = Nodes.get(nodeRelated);
		 
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
		 
	private class BrandesNodeComparatorLargerFirst implements Comparator<NodeBtwns> { 
		public int compare(NodeBtwns x, NodeBtwns y) { 
			if (x.distance() > y.distance()) 
				return -1; 
			else if (x.distance() < y.distance()) 
				return 1; 
		 
			return 0; 
		} 
	} 
		 
	public interface Progress { 
		void progress(float percent); 
	} 	

}