package application.model.connectivity;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.ProgressBar;

public class Connectivity {
	
	private ArrayList<String> Bridges;
	
	private HashMap<Integer,NodeConn> Nodes;
	
	private ProgressBar progress;

	private double nodesCount;
	private int count;
	
	public Connectivity(HashMap<Integer,NodeConn> Nodes, ProgressBar progress, double nodesCount){
		this.Bridges = new ArrayList<String>();
		this.Nodes = Nodes;
		this.progress = progress;   
		this.nodesCount = nodesCount;
	}
	
	public void compute(boolean doNodes, boolean doEdges) {
		for (NodeConn node : Nodes.values()) {
	    	node.setPre(-1);
	    	node.setLow(-1);
	    }	
		if(doNodes) {
			for(NodeConn node : Nodes.values()) {
		    	if(node.pre() == -1) {
					dfsNodes(node, node);
		    	}			
		    	progress.setProgress(0.4 + (count++/nodesCount)/2.5);
			}
		}
		if(doEdges) {
			for(NodeConn node : Nodes.values()) {
		    	if(node.pre() == -1) {
		    		dfsEdges(node, node);
		    	}	
		    	progress.setProgress(0.4 + (count++/nodesCount)/2.5);
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
	    	System.out.println(nodeAdj.id());
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
