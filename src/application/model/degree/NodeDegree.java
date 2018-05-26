package application.model.degree;

import application.model.Node;

public class NodeDegree extends Node {
	
	private int indegree;
	private int outdegree;
	
	public NodeDegree(int id) {
		super(id);
		this.setIndegree(0);
		this.setOutdegree(0);
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
