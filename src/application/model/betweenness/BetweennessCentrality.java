package application.model.betweenness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class BetweennessCentrality {

	protected static double INFINITY = 1000000.0; 
	private Progress progress;
	
	private int graphSize;
	
	private ArrayList<Node> Nodes;
	private ArrayList<Relation> Relations;
	
	public BetweennessCentrality(int graphSize) {
		this.graphSize = graphSize;
	} 

	public void registerProgressIndicator(Progress progress) { 
		this.progress = progress; 
	} 
	 
	public void init(ArrayList<Node> Nodes, ArrayList<Relation> Relations) { 
		this.Nodes = Nodes; 
		this.Relations = Relations;
	}
	 
	public void compute() { 
		if (!Nodes.isEmpty()) { 
			betweennessCentrality(Nodes, Relations); 
		}
	} 
	
	public void betweennessCentrality(ArrayList<Node> Nodes, ArrayList<Relation> Relations) { 
		initAllNodes(Nodes); 
	 
		float prog = 0; 
	 
		for (Node node : Nodes) {
			PriorityQueue<Node> SimpleExploreResult = null; 
	 
			SimpleExploreResult = simpleExplore(node, Nodes); 
				
			while (!SimpleExploreResult.isEmpty()) {
				Node nodeOfS = SimpleExploreResult.poll();
	
				for (Node nodeOfSPredecessor : nodeOfS.predecessors()) {
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
		} 
	} 
	
	private PriorityQueue<Node> simpleExplore(Node source, ArrayList<Node> Nodes) { 
		LinkedList<Node> Q = new LinkedList<Node>(); 
		PriorityQueue<Node> S = new PriorityQueue<Node>(graphSize, new BrandesNodeComparatorLargerFirst()); 
	 
		Nodes = setupAllNodes(Nodes); 
		source.setSigma(1.0);
		source.setDistance(0.0);
		Q.add(source); 
	 
		while (!Q.isEmpty()) { 
			Node nodeFrom = Q.removeFirst(); 	 
			S.add(nodeFrom); 
			
			ArrayList<Relation> Related = new ArrayList<Relation>();
			for(Relation relation : Relations) {
				if(relation.fromID() == nodeFrom.id())
					Related.add(relation);
			}
	   
			for(Relation relation : Related) { 
				Node nodeTo = Nodes.get(getNode(relation.toID()));
	 
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
	
	private int getNode(long ID) {
		int index = 0;
		for(Node n : Nodes) {
			if(n.id() == ID)
				return index;
			index++;
		}
		return -1;		 
	}
	  
	private void addToPredecessorsOf(Node node, Node predecessor) { 
		node.predecessors().add(predecessor);
	} 
		 
	private void clearPredecessorsOf(Node node) { 
		HashSet<Node> set = new HashSet<Node>(); 
		node.setPredecessors(set);
	} 
		 
	private void initAllNodes(ArrayList<Node> Nodes) { 
		for (Node node : Nodes) { 
			node.setCentrality(0.0);
		} 
	} 

	private ArrayList<Node> setupAllNodes(ArrayList<Node> Nodes) { 
		for (Node node : Nodes) { 
			node.setDistance(INFINITY);
			node.setDelta(0);
			node.setSigma(0);
			clearPredecessorsOf(node); 
		} 
		return Nodes;
	} 
		 
	private class BrandesNodeComparatorLargerFirst implements Comparator<Node> { 
		public int compare(Node x, Node y) { 
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