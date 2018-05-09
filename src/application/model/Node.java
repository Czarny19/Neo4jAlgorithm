package application.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import application.model.connectivity.VCBag;

public class Node {
	
	private int id;

	// Betweenness Centrality
	private double centrality;
	private double sigma;
	private double delta;
	private double distance;
	private HashSet<Node> predecessors;
	private HashSet<Node> successors;
	
	// Kolorowanie grafu
	private List<Integer> neighbors;
	private int color;
	
	// Connectivity
	private int pre;
	private int low;	
	private boolean isArticulationPoint;
	private VCBag<Node> adj;
	
	// Degree Centrality
	private int indegree;
	private int outdegree;
	
	public Node(int ID) {
		this.id = ID;
	}
	
	public void coloring() {
		this.neighbors = new ArrayList<Integer>();
		this.color = 0;
	}
	
	public void connectivity() {
		this.setArticulationPoint(false);
		this.adj = new VCBag<Node>();
	}
	
	public void degreeCentrality() {
		this.setIndegree(0);
		this.setOutdegree(0);
	}
	
	public int id() {
		return id;
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

	public HashSet<Node> predecessors() {
		return predecessors;
	}

	public void setPredecessors(HashSet<Node> predecessors) {
		this.predecessors = predecessors;
	}

	public HashSet<Node> successors() {
		return successors;
	}

	public void setSuccessors(HashSet<Node> succesors) {
		this.successors = succesors;
	}
	
	public List<Integer> neighbors() {
		return neighbors;
	}
	
	public void setNeighbors(List<Integer> neighbors) {
		this.neighbors = neighbors;
	}

	public int color() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
	
	public int pre() {
		return pre;
	}

	public void setPre(int pre) {
		this.pre = pre;
	}

	public int low() {
		return low;
	}

	public void setLow(int low) {
		this.low = low;
	}

	public boolean isArticulationPoint() {
		return isArticulationPoint;
	}

	public void setArticulationPoint(boolean isArticulationPoint) {
		this.isArticulationPoint = isArticulationPoint;
	}

	public VCBag<Node> adj() {
		return adj;
	}
	
	public int indegree() {
		return indegree;
	}

	public void setIndegree(int indegree) {
		this.indegree = indegree;
	}

	public int outdegree() {
		return outdegree;
	}

	public void setOutdegree(int outdegree) {
		this.outdegree = outdegree;
	}
}
