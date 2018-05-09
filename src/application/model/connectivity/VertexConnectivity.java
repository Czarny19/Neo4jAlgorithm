package application.model.connectivity;

import java.util.HashMap;

import application.model.Node;

public class VertexConnectivity {

	private int cnt;
	 
	public VertexConnectivity(HashMap<Integer,Node> Nodes){
	    for (Node node : Nodes.values()) {
	    	node.setPre(-1);
	    	node.setLow(-1);
	    }	    
	}
	
	public void compute(HashMap<Integer,Node> Nodes) {
		for(Node node : Nodes.values()) {
	    	if(node.pre() == -1) {
	    		dfs(node, node);
	    	}
	    }
	}
	 
	private void dfs(Node u, Node v)
	{
		int children = 0;
		v.setPre(cnt++);
		v.setLow(v.pre());
	    for (Node w : v.adj())
	    {
	    	if (w.pre() == -1)
	        {
	    		children++;
	            dfs(v, w);
	            // update low number
	            v.setLow(Math.min(v.low(), w.low()));
	            // non-root of DFS is an articulation point if low[w] >= pre[v]
	            if (w.low() >= v.low() && u != v)
	            	v.setArticulationPoint(true);
	        }
	        // update low number - ignore reverse of edge leading to v
	        else if (w != u)
	        	v.setLow(Math.min(v.low(), w.low()));
	    }
	    // root of DFS is an articulation point if it has more than 1 child
	    if (u == v && children > 1)
	    	v.setArticulationPoint(true);
	}
}
