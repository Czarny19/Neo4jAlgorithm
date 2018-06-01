package application.model.connectivity;

import application.model.Relation;

public class RelationConn extends Relation{
	
	private int id;
	private boolean isBridge;
	
	public RelationConn(int id, int nodeFrom, int nodeTo) {
		super(nodeFrom, nodeTo);
		this.id = id;
		this.isBridge = false;
	}

	public int id() {
		return id;
	}

	public boolean isBridge() {
		return isBridge;
	}

	public void setBridge(boolean isBridge) {
		this.isBridge = isBridge;
	}
}
