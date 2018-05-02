package application.model.coloring;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

	private long node;
	private List<Long> neighbors;
	private int color;
	
	public Vertex(long node){
		this.node = node;
		this.neighbors = new ArrayList<Long>();
		this.color = 0;
	}
	
	public List<Long> neighbors() {
		return neighbors;
	}
	
	public void setNeighbors(List<Long> neighbors) {
		this.neighbors = neighbors;
	}
	
	public long node() {
		return node;
	}

	public int color() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
