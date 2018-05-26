package application.model;

public class Relation {
	
	private int nodeFrom;
	private int nodeTo;
	
	public Relation(int nodeFrom, int nodeTo) {
		this.nodeFrom = nodeFrom;
		this.nodeTo = nodeTo;
	}
	
	public int nodeFrom() {
		return nodeFrom;
	}

	public int nodeTo() {
		return nodeTo;
	}
}
