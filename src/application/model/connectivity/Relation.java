package application.model.connectivity;

public class Relation {
	
	private int id;
	private int nodeFrom;
	private int nodeTo;
	
	public Relation(int id, int nodeFrom, int nodeTo) {
		this.id = id;
		this.nodeFrom = nodeFrom;
		this.nodeTo = nodeTo;
	}
	
	public Relation(int nodeFrom, int nodeTo) {
		this.nodeFrom = nodeFrom;
		this.nodeTo = nodeTo;
	}
	
	public int id() {
		return id;
	}
	
	public int nodeFrom() {
		return nodeFrom;
	}

	public int nodeTo() {
		return nodeTo;
	}
}
