package application.model.astar;

import application.model.Relation;

public class RelationAStar extends Relation{
	
	private int id;
	private double distance;
	
	public RelationAStar(int id, int nodeFrom, int nodeTo, double distance) {
		super(nodeFrom,nodeTo);
		this.id = id;
		this.distance = distance;
	}

	public int id() {
		return id;
	}
	
	public double distance() {
		return distance;
	}
}
