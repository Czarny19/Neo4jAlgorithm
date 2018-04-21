package application.model.betweenness;

public class Relation {

	private long fromID;
	private long toID;
	
	public Relation(long fromID, long toID) {
		this.fromID = fromID;
		this.toID = toID;
	}
	
	public long fromID() {
		return fromID;
	}
	
	public long toID() {
		return toID;
	}
}
