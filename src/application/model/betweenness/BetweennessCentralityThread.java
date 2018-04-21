package application.model.betweenness;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

public class BetweennessCentralityThread {
	
	private Driver driver;
	private ArrayList<Node> Nodes;
	private ArrayList<Relation> Relations; 
	
	public BetweennessCentralityThread(Driver driver) {
		
		this.driver = driver;
		Nodes = new ArrayList<Node>();
		Relations = new ArrayList<Relation>();		
	}
	
	public void start() {
		BetweennessCentrality bcb = new BetweennessCentrality(Integer.parseInt(getGraphSize().get(0).toString())); 
		initNodes(getNodesList());
		initRelations(getRelationsList());
		System.out.println(Nodes.size());
		System.out.println(Relations.size());
		bcb.init(Nodes, Relations); 
		bcb.compute();
		
		for(Node node : Nodes) {
			System.out.println(node.id() + " = " + node.centrality()/2); 
		}
		
		System.out.println(getGraphSize().get(0));
	}
	
	public void initNodes(List<Record> NodesList) {
		for(Record record : NodesList) {
			Node node = new Node(Long.parseLong(record.get(0).toString()));
			Nodes.add(node);
		}
	}
	
	public void initRelations(List<Record> RelationsList) {
		for(Record record : RelationsList) {
			Relation relation = new Relation(
					Long.parseLong(record.get(0).toString()),
					Long.parseLong(record.get(1).toString()));
			Relation relation2 = new Relation(
					Long.parseLong(record.get(1).toString()),
					Long.parseLong(record.get(0).toString()));
			Relations.add(relation);
//			Relations.add(relation2);
		}
	}
	
	public Record getGraphSize(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BetweennessCentralityThread::getSize);
        }
    }

    public List<Record> getNodesList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BetweennessCentralityThread::getNodes);
        }
    }

    public List<Record> getRelationsList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BetweennessCentralityThread::getRelations);
        }
    }
	
    private static List<Record> getRelations(Transaction tx)
    {
    	return tx.run("MATCH (n)-[r]->(p) RETURN distinct ID(n),ID(p)").list();
    }

    private static List<Record> getNodes(Transaction tx){
        return tx.run("MATCH (n) RETURN ID(n) LIMIT 25").list();
    }

    private static Record getSize(Transaction tx){
        return tx.run("start n=node(*) match (n) return count(n)").list().get(0);
    }
	
}
