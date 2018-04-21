package application.model.betweenness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

public class BetweennessCentrality {
	
	protected static double INFINITY = 1000000.0; 
	//private String centralityAttribute = "centrality";
	private Progress progress;
	
	private int graphSize;
	
	private ArrayList<Node> Nodes;
	private ArrayList<Relation> Relations;
	
	public BetweennessCentrality(int graphSize) {
		this.graphSize = graphSize;
		//this.driver = driver;
	} 
	
	 /**
	  * Specify an interface to call in order to indicate the algorithm progress. 
	  * Pass null to remove the progress indicator. The progress indicator will 
	  * be called regularly to indicate the computation progress. 
	  */ 
	 public void registerProgressIndicator(Progress progress) { 
		 this.progress = progress; 
	 } 
	 
	 /**
	  * Setup the algorithm to work on the given graph. 
	  */ 
	 public void init(ArrayList<Node> Nodes, ArrayList<Relation> Relations) { 
		 this.Nodes = Nodes; 
		 this.Relations = Relations;
	 }
	 
	 public void compute() { 
		 if (!Nodes.isEmpty()) { 
			 betweennessCentrality(Nodes, Relations); 
		 } 
	 } 
	 
	 /**
	  * Compute the betweenness centrality on the given graph for each node and 
	  * eventually edges. This method is equivalent to a call in sequence to the 
	  * two methods {@link #init(Graph)} then {@link #compute()}. 
	  */ 
	 public void betweennessCentrality(ArrayList<Node> Nodes, ArrayList<Relation> Relations) { 
	  init(Nodes, Relations); 
	  initAllNodes(Nodes); 
	 
	  float n = graphSize; 
	  float i = 0; 
	 
	  for (Node s : Nodes) { 
	   PriorityQueue<Node> S = null; 
	 
	   S = simpleExplore(s, Nodes); 
	
	   // The really new things in the Brandes algorithm are here: 
	   // Accumulation phase: 
	 
	   while (!S.isEmpty()) { 
	    Node w = S.poll(); 
	 
	    for (Node v : predecessorsOf(w)) { 
	     double c = ((sigma(v) / sigma(w)) * (1.0 + delta(w))); 
	     setDelta(v, delta(v) + c); 
	    } 
	    if (w != s) { 
	     setCentrality(w, centrality(w) + delta(w)); 
	    } 
	   } 
	 
	   if (progress != null) 
	    progress.progress(i / n); 
	 
	   i++; 
	  } 
	 } 
	 
	 /**
	  * Compute single-source multiple-targets shortest paths on an unweighted 
	  * graph. 
	  *  
	  * @param source 
	  *            The source node. 
	  * @param graph 
	  *            The graph. 
	  * @return A priority queue of explored nodes with sigma values usable to 
	  *         compute the centrality. 
	  */ 
	 protected PriorityQueue<Node> simpleExplore(Node source, ArrayList<Node> Nodes) { 
	  LinkedList<Node> Q = new LinkedList<Node>(); 
	  PriorityQueue<Node> S = new PriorityQueue<Node>(graphSize, new BrandesNodeComparatorLargerFirst()); 
	 
	  Nodes = setupAllNodes(Nodes); 
	  Q.add(source); 
	  setSigma(source, 1.0); 
	  setDistance(source, 0.0); 
	 
	  while (!Q.isEmpty()) { 
	   Node v = Q.removeFirst(); 
	 
	   S.add(v); 
	   ArrayList<Relation> related = new ArrayList<Relation>();
	   for(Relation r : Relations) {
		   if(r.fromID() == v.id())
			   related.add(r);
	   }
	   
	   for(Relation r : related) { 
	    Node w = Nodes.get(getNode(r.toID()));
	 
		    if (distance(w) == INFINITY) { 
		     setDistance(w, distance(v) + 1); 
		     Q.add(w); 
		    } 
		 
		    if (distance(w) == (distance(v) + 1.0)) { 
		     setSigma(w, sigma(w) + sigma(v)); 
		     addToPredecessorsOf(w, v); 
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
	 
	  /**
	   * The sigma value of the given node. 
	   *  
	   * @param node 
	   *            Extract the sigma value of this node. 
	   * @return The sigma value. 
	   */ 
	  protected double sigma(Node node) { 
		  return node.sigma(); 
	  } 
	  
	  /**
	   * The distance value of the given node. 
	   *  
	   * @param node 
	   *            Extract the distance value of this node. 
	   * @return The distance value. 
	   */ 
	  protected double distance(Node node) { 
		  return node.distance(); 
	  } 
	  
	  /**
	   * The delta value of the given node. 
	   *  
	   * @param node 
	   *            Extract the delta value of this node. 
	   * @return The delta value. 
	   */ 
	  protected double delta(Node node) { 
		  return node.delta(); 
	  } 
	  
	  /**
	   * The centrality value of the given node or edge. 
	   *  
	   * @param elt 
	   *            Extract the centrality of this node or edge. 
	   * @return The centrality value. 
	   */ 
	  public double centrality(Node node) { 
		  return node.centrality(); 
	  } 
	   
	  /**
	   * List of predecessors of the given node. 
	   *  
	   * @param node 
	   *            Extract the predecessors of this node. 
	   * @return The list of predecessors. 
	   */ 

	  protected Set<Node> predecessorsOf(Node node) { 
	   return (HashSet<Node>) node.predecessors(); 
	  } 
	  
	  /**
	   * Set the sigma value of the given node. 
	   *  
	   * @param node 
	   *            The node to modify. 
	   * @param sigma 
	   *            The sigma value to store on the node. 
	   */ 
	  protected void setSigma(Node node, double sigma) { 
	   node.setSigma(sigma);
	  } 
	  
	  /**
	   * Set the distance value of the given node. 
	   *  
	   * @param node 
	   *            The node to modify. 
	   * @param distance 
	   *            The delta value to store on the node. 
	   */ 
	  protected void setDistance(Node node, double distance) { 
	   node.setDistance(distance); 
	  } 
	  
	  /**
	   * Set the delta value of the given node. 
	   *  
	   * @param node 
	   *            The node to modify. 
	   * @param delta 
	   *            The delta value to store on the node. 
	   */ 
	  protected void setDelta(Node node, double delta) { 
	   node.setDelta(delta);
	  } 
	  
	  /**
	   * Set the centrality of the given node or edge. 
	   *  
	   * @param elt 
	   *            The node or edge to modify. 
	   * @param centrality 
	   *            The centrality to store on the node. 
	   */ 
	  public void setCentrality(Node node, double centrality) { 
	   node.setCentrality(centrality);
	  } 
	  
	  protected void replacePredecessorsOf(Node node, Node predecessor) { 
		  HashSet<Node> set = new HashSet<Node>(); 
		 
		  set.add(predecessor); 
		  node.setPredecessors(set);
		 } 
		 
		 /**
		  * Add a node to the predecessors of another. 
		  *  
		  * @param node 
		  *            Modify the predecessors of this node. 
		  * @param predecessor 
		  *            The predecessor to add. 
		  */ 
		 @SuppressWarnings("all") 
		 protected void addToPredecessorsOf(Node node, Node predecessor) { 
			 node.predecessors().add(predecessor);
		 } 
		 
		 /**
		  * Remove all predecessors of the given node. 
		  *  
		  * @param node 
		  *            Remove all predecessors of this node. 
		  */ 
		 protected void clearPredecessorsOf(Node node) { 
		  HashSet<Node> set = new HashSet<Node>(); 
		  node.setPredecessors(set);
		 } 
		 
		 /**
		  * Set a default centrality of 0 to all nodes. 
		  *  
		  * @param graph 
		  *            The graph to modify. 
		  */ 
		 protected void initAllNodes(ArrayList<Node> Nodes) { 
		  for (Node node : Nodes) { 
		   node.setCentrality(0.0);
		  } 
		 } 
		 
		 /**
		  * Add a default value for attributes used during computation. 
		  *  
		  * @param graph 
		  *            The graph to modify. 
		  */ 
		 protected ArrayList<Node> setupAllNodes(ArrayList<Node> Nodes) { 
		  for (Node node : Nodes) { 
		   clearPredecessorsOf(node); 
		   setSigma(node, 0.0); 
		   setDistance(node, INFINITY); 
		   setDelta(node, 0.0); 
		  } 
		  return Nodes;
		 } 
		 
		 /**
		  * Delete attributes used by this algorithm in nodes of the graph 
		  */ 
		 public void cleanNodes(){ 
			 for(Node node : Nodes) {
				 clearPredecessorsOf(node); 
				   setSigma(node, 0.0); 
				   setDistance(node, INFINITY); 
				   setDelta(node, 0.0); 
			 }
		 } 

		 /**
		  * Increasing comparator used for priority queues. 
		  */ 
		  protected class BrandesNodeComparatorLargerFirst implements Comparator<Node> { 
			  public int compare(Node x, Node y) { 
				 // return (int) ( (distance(y)*1000.0) - (distance(x)*1000.0) ); 
				 double yy = distance(y); 
				 double xx = distance(x); 
		 
				 if (xx > yy) 
					 return -1; 
				 else if (xx < yy) 
					 return 1; 
		 
				 return 0; 
			  } 
		 } 
		 
		 /**
		  * Decreasing comparator used for priority queues. 
		  */ 
		 protected class BrandesNodeComparatorSmallerFirst implements Comparator<Node> { 
			 public int compare(Node x, Node y) { 
				 // return (int) ( (distance(x)*1000.0) - (distance(y)*1000.0) ); 
				 double yy = distance(y); 
				 double xx = distance(x); 
		 
				 if (xx > yy) 
					 return 1; 
				 else if (xx < yy) 
					 return -1; 
		 
				 return 0; 
			 } 
		 } 
		 
	public interface Progress { 
		void progress(float percent); 
	} 	
}
