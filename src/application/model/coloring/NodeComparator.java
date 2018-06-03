package application.model.coloring;

import java.util.Comparator;

public class NodeComparator implements Comparator<NodeGC>{

	@Override
	public int compare(NodeGC nodeOne, NodeGC nodeTwo) {
		return nodeOne.neighbors().size() < nodeTwo.neighbors().size() ? 1 
			: nodeOne.neighbors().size() == nodeTwo.neighbors().size() ? 0 
			: -1;
	}	
}