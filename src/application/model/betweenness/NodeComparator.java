package application.model.betweenness;

import java.util.Comparator;

public class NodeComparator implements Comparator<NodeBtwns> { 
	public int compare(NodeBtwns x, NodeBtwns y) { 
		if (x.distance() > y.distance()) 
			return -1; 
		else if (x.distance() < y.distance()) 
			return 1; 		 
		return 0; 
	} 
}