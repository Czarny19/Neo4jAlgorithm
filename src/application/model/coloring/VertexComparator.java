package application.model.coloring;

import java.util.Comparator;

public class VertexComparator implements Comparator<Vertex>{

	@Override
	public int compare(Vertex a, Vertex b) {
		return a.neighbors().size() < b.neighbors().size() ? 1 : a.neighbors().size() == b.neighbors().size() ? 0 : -1;
	}
	
}