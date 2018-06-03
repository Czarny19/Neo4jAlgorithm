package application.model.connectivity;

import java.util.ArrayList;

import application.model.Node;

public class NodeConn extends Node{

	private int pre;
	private int low;	
	private boolean isArticulationPoint;
	private ArrayList<NodeConn> adjecentNodes;
	
	public NodeConn(int id) {
		super(id);
		this.isArticulationPoint = false;
		this.adjecentNodes = new ArrayList<NodeConn>();
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

	public ArrayList<NodeConn> adj() {
		return adjecentNodes;
	}
}
