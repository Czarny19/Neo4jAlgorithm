package application.model.betweenness;

import java.util.HashSet;

import application.model.Node;

public class NodeBtwns extends Node{
	
	private double centrality;
	private double sigma;
	private double delta;
	private double distance;
	private HashSet<NodeBtwns> predecessors;
	
	public NodeBtwns(int id) {
		super(id);
	}
	
	public double centrality() {
		return centrality;
	}
	
	public void setCentrality(double centrality) {
		this.centrality = centrality;
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

	public HashSet<NodeBtwns> predecessors() {
		return predecessors;
	}

	public void setPredecessors(HashSet<NodeBtwns> Predecessors) {
		this.predecessors = Predecessors;
	}
}
