package application.model.betweenness;

import java.util.HashSet;

public class Node {
	
	private long ID;
	private double centrality;
	private double sigma;
	private double delta;
	private double distance;
	private  HashSet<Node> predecessors;
	
	public Node(long ID) {
		this.ID = ID;
	}
	
	public long id() {
		return ID;
	}
	
	public void setCentrality(double centrality) {
		this.centrality = centrality;
	}
	
	public double centrality() {
		return centrality;
	}

	public double sigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public double delta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public double distance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public HashSet<Node> predecessors() {
		return predecessors;
	}

	public void setPredecessors(HashSet<Node> predecessors) {
		this.predecessors = predecessors;
	}
}
