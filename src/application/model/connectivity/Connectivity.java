package application.model.connectivity;

import java.util.ArrayList;
import java.util.HashMap;

public class Connectivity {
	
	private ArrayList<String> Bridges;

	private int count;
	 
	public Connectivity(HashMap<Integer,NodeConn> Nodes){
		Bridges = new ArrayList<String>();
	    for (NodeConn node : Nodes.values()) {
	    	node.setPre(-1);
	    	node.setLow(-1);
	    }	    
	}
	
	public void compute(HashMap<Integer,NodeConn> Nodes, boolean doNodes, boolean doEdges) {
		if(doNodes)
			for(NodeConn node : Nodes.values())
		    	if(node.pre() == -1)
		    		dfsNodes(node, node);
		if(doEdges)
			for(NodeConn node : Nodes.values())
		    	if(node.pre() == -1)
		    		dfsEdges(node, node);
	}
	
	// TODO po³¹czyæ w jedno
	private void dfsEdges(NodeConn nodeFrom, NodeConn nodeTo) {
		nodeTo.setPre(count++);
		nodeTo.setLow(nodeTo.pre());
	    for (NodeConn nodeAdj : nodeTo.adj()){
	    	if (nodeAdj.pre() == -1){
	    		dfsEdges(nodeTo, nodeAdj);
	            nodeTo.setLow(Math.min(nodeTo.low(), nodeAdj.low()));
	            if (nodeAdj.low() == nodeAdj.pre()) {
                    Bridges.add(nodeTo.id() + " " + nodeAdj.id());
                    Bridges.add(nodeAdj.id() + " " + nodeTo.id());
	            }
	        }
	    	else if (nodeAdj != nodeFrom)
	    		nodeTo.setLow(Math.min(nodeTo.low(), nodeAdj.low()));
	    }
	}
	 
	private void dfsNodes(NodeConn nodeFrom, NodeConn nodeTo){
		int children = 0;
		nodeTo.setPre(count++);
		nodeTo.setLow(nodeTo.pre());
	    for (NodeConn nodeAdj : nodeTo.adj()){
	    	if (nodeAdj.pre() == -1){
	    		children++;
	            dfsNodes(nodeTo, nodeAdj);
	            nodeTo.setLow(Math.min(nodeTo.low(), nodeAdj.low()));
	            if (nodeAdj.low() >= nodeTo.low() && nodeFrom != nodeTo)
	            	nodeTo.setArticulationPoint();
	        }
	        else if (nodeAdj != nodeFrom)
	        	nodeTo.setLow(Math.min(nodeTo.low(), nodeAdj.low()));
	    }
	    if (nodeFrom.id() == nodeTo.id() && children > 1) {
	    	nodeTo.setArticulationPoint();
	    }
	}
	
	public ArrayList<String> bridges(){
		return Bridges;
	}
}
