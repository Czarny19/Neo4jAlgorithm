package application.model.coloring;

import java.util.Comparator;

import application.model.Node;

public class VertexComparator implements Comparator<Node>{

	@Override
	public int compare(Node a, Node b) {
		return a.neighbors().size() < b.neighbors().size() ? 1 : a.neighbors().size() == b.neighbors().size() ? 0 : -1;
	}
	
}