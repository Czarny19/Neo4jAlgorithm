package application.model.coloring;

import java.util.ArrayList;
import java.util.List;

import application.model.Node;

public class NodeGC extends Node{
	
	private List<Integer> Neighbors;
	private int color;
	
	public NodeGC(int id) {
		super(id);
		this.Neighbors = new ArrayList<Integer>();
		this.color = 0;
	}
	
	public List<Integer> neighbors() {
		return Neighbors;
	}
	
	public void setNeighbors(List<Integer> Neighbors) {
		this.Neighbors = Neighbors;
	}

	public int color() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
}
