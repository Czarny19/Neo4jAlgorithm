package application.model.connectivity;

import application.model.Relation;

public class RelationConn extends Relation{
	
	private int id;
	
	public RelationConn(int id, int nodeFrom, int nodeTo) {
		super(nodeFrom, nodeTo);
		this.id = id;
	}

	public int id() {
		return id;
	}
}
