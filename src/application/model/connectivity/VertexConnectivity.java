package application.model.connectivity;

import java.util.ArrayList;
import java.util.HashMap;

import application.model.Node;

public class VertexConnectivity {

	private int cnt;
	
	private ArrayList<Integer> ArticulationPoints;
	private ArrayList<Relation> Bridges;
	 
	public VertexConnectivity(HashMap<Integer,Node> Nodes){
		ArticulationPoints = new ArrayList<Integer>();
		Bridges = new ArrayList<Relation>();
	    for (Node node : Nodes.values()) {
	    	node.setPre(-1);
	    	node.setLow(-1);
	    }	    
	}
	
	public void compute(HashMap<Integer,Node> Nodes, boolean doEdges) {
		if(!doEdges)
			for(Node node : Nodes.values())
		    	if(node.pre() == -1)
		    		dfsNodes(node, node);
		if(doEdges)
			for(Node node : Nodes.values())
		    	if(node.pre() == -1)
		    		dfsEdges(node, node);
	}
	
	private void dfsEdges(Node u, Node v) {
		v.setPre(cnt++);
		v.setLow(v.pre());
	    for (Node w : v.adj())
	    {
	    	if (w.pre() == -1)
	        {
	    		dfsEdges(v, w);
	            v.setLow(Math.min(v.low(), w.low()));
	            if (w.low() == w.pre())
                {
	            	Relation relation = new Relation(v.id(),w.id());
                    setBridge(relation);
                }
	        }
	    	else if (w != u)
	    		v.setLow(Math.min(v.low(), w.low()));
	    }
	}
	 
	private void dfsNodes(Node u, Node v){
		int children = 0;
		v.setPre(cnt++);
		v.setLow(v.pre());
	    for (Node w : v.adj())
	    {
	    	if (w.pre() == -1)
	        {
	    		children++;
	            dfsNodes(v, w);
	            v.setLow(Math.min(v.low(), w.low()));
	            if (w.low() >= v.low() && u != v)
	            	setArticulationPoint(v);
	            
	        }
	        else if (w != u)
	        	v.setLow(Math.min(v.low(), w.low()));
	    }
	    if (u == v && children > 1)
	    	setArticulationPoint(v);
	}

	private void setArticulationPoint(Node v) {
		ArticulationPoints.add(v.id());
	}
	
	public ArrayList<Integer> articulationPoints(){
		return ArticulationPoints;
	}

	public void setBridge(Relation r) {
		Bridges.add(r);
	}

	public ArrayList<Relation> bridges() {
		return Bridges;
	}
}
