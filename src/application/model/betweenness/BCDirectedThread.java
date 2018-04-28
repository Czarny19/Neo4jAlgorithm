package application.model.betweenness;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public class BCDirectedThread {
	
	private Driver driver;
	private ArrayList<Node> Nodes;
	private ArrayList<Relation> Relations; 
	
	public BCDirectedThread(Driver driver) {
		
		this.driver = driver;
		Nodes = new ArrayList<Node>();
		Relations = new ArrayList<Relation>();		
	}
	
	public void start() {
		BCDirected bcb = new BCDirected(Integer.parseInt(getGraphSize().get(0).toString())); 
		initNodes(getNodesList());
		initRelations(getRelationsList());
		System.out.println(Nodes.size());
		System.out.println(Relations.size());
		bcb.init(Nodes, Relations); 
		bcb.compute();
		
		Transaction transaction = driver.session().beginTransaction();
		for(Node node : Nodes) {
			System.out.println(node.id() + " = " + node.centrality()); 
			insertNodeCentrality(transaction, node);
		}
		transaction.success();
		transaction.close();
	}
	
	private void initNodes(List<Record> NodesList) {
		for(Record record : NodesList) {
			Node node = new Node(Long.parseLong(record.get(0).toString()));
			Nodes.add(node);
		}
	}
	
	private void initRelations(List<Record> RelationsList) {
		for(Record record : RelationsList) {
			Relation relation = new Relation(
				Long.parseLong(record.get(0).toString()),
				Long.parseLong(record.get(1).toString()));
			Relations.add(relation);
		}
	}
	
	private Record getGraphSize(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BCDirectedThread::getSize);
        }
    }

    private List<Record> getNodesList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BCDirectedThread::getNodes);
        }
    }

    private List<Record> getRelationsList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BCDirectedThread::getRelations);
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
    
    private StatementResult insertNodeCentrality(Transaction tx, Node node) {
    	return tx.run("MATCH (n) WHERE ID(n) = " + node.id() + " SET n.BCUndirected = " + node.centrality());
    }
}
