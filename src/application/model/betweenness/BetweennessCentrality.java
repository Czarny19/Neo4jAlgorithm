package application.model.betweenness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import application.model.Relation;

public class BetweennessCentrality {

	protected static double INFINITY = 1000000.0; 
	private Progress progress;
	private float prog = 0; 
	
	private int graphSize;
	
	private HashMap<Integer,NodeBtwns> Nodes;
	private ArrayList<Relation> Relations;
	
	public BetweennessCentrality(int graphSize) {
		this.graphSize = graphSize;
	} 

	public void registerProgressIndicator(Progress progress) { 
		this.progress = progress; 
	} 
	 
	public void init(HashMap<Integer,NodeBtwns> Nodes, ArrayList<Relation> Relations) { 
		this.Nodes = Nodes; 
		this.Relations = Relations;
	}
	 
	public void compute() { 
		if (!Nodes.isEmpty()) { 
			betweennessCentrality(Nodes, Relations); 
		}
	} 
	
	public void betweennessCentrality(HashMap<Integer,NodeBtwns> Nodes, ArrayList<Relation> Relations) { 
		initAllNodes(Nodes); 

		Nodes.forEach((id,node) -> {
			PriorityQueue<NodeBtwns> SimpleExploreResult = null;
			
			SimpleExploreResult = simpleExplore(node, Nodes); 
			
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
			
			if (progress != null) 
				progress.progress(prog / graphSize); 
			prog++; 
		});
	} 
	
	private PriorityQueue<NodeBtwns> simpleExplore(NodeBtwns source, HashMap<Integer,NodeBtwns> Nodes) { 
		LinkedList<NodeBtwns> Q = new LinkedList<NodeBtwns>(); 
		PriorityQueue<NodeBtwns> S = new PriorityQueue<NodeBtwns>(graphSize, new BrandesNodeComparatorLargerFirst()); 
	 
		Nodes = setupAllNodes(Nodes); 
		source.setSigma(1.0);
		source.setDistance(0.0);
		Q.add(source); 
	 
		while (!Q.isEmpty()) { 
			NodeBtwns nodeFrom = Q.removeFirst(); 	 
			S.add(nodeFrom); 
			
			ArrayList<Relation> Related = new ArrayList<Relation>();
			for(Relation relation : Relations) {
				if(relation.nodeFrom() == nodeFrom.id())
					Related.add(relation);
			}
	   
			for(Relation relation : Related) { 
				NodeBtwns nodeTo = Nodes.get(relation.nodeTo());
	 
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