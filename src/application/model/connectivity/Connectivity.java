package application.model.connectivity;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.ProgressBar;

public class Connectivity {
	
	private ArrayList<String> bridges;	
	private HashMap<Integer,NodeConn> nodes;
	
	private ProgressBar progress;

	private double nodesCount;
	
	private int computedCount;
	private int count;
	
	public Connectivity(HashMap<Integer,NodeConn> Nodes, ProgressBar progress, double nodesCount){		
		this.nodes = Nodes;
		this.progress = progress;   
		this.nodesCount = nodesCount;
		this.bridges = new ArrayList<String>();
	}
	
	public void compute(boolean doNodes, boolean doEdges) {
		for (NodeConn node : nodes.values()) {
	    	node.setPre(-1);
	    	node.setLow(-1);
	    }	
		if(doNodes) {
			for(NodeConn node : nodes.values()) {
		    	if(node.pre() == -1)
					dfsNodes(node, node);		
		    	progress.setProgress(0.4 + (computedCount++/nodesCount)/2.5);
			}
		}
		if(doEdges) {
			for(NodeConn node : nodes.values()) {
		    	if(node.pre() == -1) {
		    		dfsEdges(node,node);
		    		for(NodeConn node2 : nodes.values()) {
		    			if(node.pre() != -1) {
		    				node2.adj().removeAll(node2.adj());
		    			}
		    		}
		    	}
		    	progress.setProgress(0.4 + (computedCount++/nodesCount)/2.5);
			}
		}
	}
	
	private void dfsEdges(NodeConn nodeFrom, NodeConn nodeTo) {
		nodeTo.setPre(count++);
		nodeTo.setLow(nodeTo.pre());
	    for (NodeConn nodeAdj : nodeTo.adj()){
	    	if (nodeAdj.pre() == -1){
	    		dfsEdges(nodeTo, nodeAdj);	    		
	            nodeTo.setLow(Math.min(nodeTo.low(), nodeAdj.low()));
	            if (nodeAdj.low() == nodeAdj.pre()) {
                    bridges.add(nodeTo.id() + " " + nodeAdj.id());
                    bridges.add(nodeAdj.id() + " " + nodeTo.id());
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
		return bridges;
	}
}
