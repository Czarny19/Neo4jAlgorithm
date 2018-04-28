package application.model.betweenness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class BCDirected {
	
	protected static double INFINITY = 1000000.0; 
	private Progress progress;
	
	private int graphSize;
	
	private ArrayList<Node> Nodes;
	private ArrayList<Relation> Relations;
	
	public BCDirected(int graphSize) {
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
				LinkedList<Node> SuccessorsOfS = new LinkedList<Node>();
				LinkedList<Node> PredecessorsOfS = new LinkedList<Node>();
				
				if(nodeOfS.id() != node.id() && node.delta() != 0) {
					if(!nodeOfS.successors().isEmpty()) {
						for(Node node1 : nodeOfS.predecessors()) {
							if(node1.id() != node.id())
								PredecessorsOfS.add(node1);		
						}
						while(!PredecessorsOfS.isEmpty()) {
							nodeOfS.setDelta(nodeOfS.delta()+1);
							Node takeOf = PredecessorsOfS.removeFirst();
							for(Node n : takeOf.successors()) {
								PredecessorsOfS.addLast(n);
							}
						}
					}
					if(!nodeOfS.predecessors().isEmpty()) {
						for(Node node1 : nodeOfS.successors()) {
							SuccessorsOfS.add(node1);
						}
						while(!SuccessorsOfS.isEmpty()) {
							nodeOfS.setDelta(nodeOfS.delta()+1);
							Node takeOf = SuccessorsOfS.removeFirst();
							for(Node n : takeOf.successors()) {
								SuccessorsOfS.addLast(n);
							}
						}
					}
					if(nodeOfS.delta() > 0) {
						nodeOfS.setCentrality(nodeOfS.centrality()+(nodeOfS.delta()/(node.delta()-1)));
					}
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
					source.setDelta(source.delta()+1);
					Q.add(nodeTo); 
				} 
				if (nodeTo.distance() == (nodeFrom.distance()+1)) { 
					addToPredecessorsOf(nodeTo, nodeFrom); 
					addToSuccessorsOf(nodeFrom, nodeTo);
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
	
	private void addToSuccessorsOf(Node node, Node successor) { 
		node.successors().add(successor);
	} 
		 
	private void clearSuccessorsOf(Node node) { 
		HashSet<Node> set = new HashSet<Node>(); 
		node.setSuccessors(set);
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
			clearPredecessorsOf(node); 
			clearSuccessorsOf(node);
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
