package application.model.connectivity;

import application.model.Node;

public class NodeConn extends Node{

	private int pre;
	private int low;	
	private boolean isArticulationPoint;
	private NodeConnBag<NodeConn> adj;
	
	public NodeConn(int id) {
		super(id);
		this.isArticulationPoint = false;
		this.adj = new NodeConnBag<NodeConn>();
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

	public void setArticulationPoint() {
		this.isArticulationPoint = true;
	}

	public NodeConnBag<NodeConn> adj() {
		return adj;
	}
}
