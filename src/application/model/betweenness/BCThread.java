package application.model.betweenness;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.Node;

public class BCThread {
	
	private Driver driver;
	private ArrayList<Node> Nodes;
	private ArrayList<Relation> Relations; 
	
	private boolean isDirected;
	
	public BCThread(Driver driver, boolean isDirected) {
		this.isDirected = isDirected;
		this.driver = driver;
		Nodes = new ArrayList<Node>();
		Relations = new ArrayList<Relation>();		
	}
	
	public void start() {
		initNodes(getNodesList());
		initRelations(getRelationsList(),isDirected());
		BetweennessCentrality betweennessCentrality = new BetweennessCentrality(Integer.parseInt(getGraphSize().get(0).toString())); 
		betweennessCentrality.init(Nodes, Relations); 
		betweennessCentrality.compute();
		
		Transaction transaction = driver.session().beginTransaction();
		for(Node node : Nodes) {
			if(!isDirected)
				System.out.println(node.id() + " = " + node.centrality()/2); 
			else
				System.out.println(node.id() + " = " + node.centrality()); 
			insertNodeCentrality(transaction, node, isDirected());
		}
		transaction.success();
		transaction.close();
	}
	
	private void initNodes(List<Record> NodesList) {
		for(Record record : NodesList) {
			Node node = new Node(Integer.parseInt(record.get(0).toString()));
			Nodes.add(node);
		}
	}
	
	private void initRelations(List<Record> RelationsList, boolean isDirected) {
		for(Record record : RelationsList) {
			Relation relation = new Relation(
				Long.parseLong(record.get(0).toString()),
				Long.parseLong(record.get(1).toString()));
			Relations.add(relation);
		}
		if(!isDirected) {
			for(Record record : RelationsList) {	
				Relation relation = new Relation(
					Long.parseLong(record.get(1).toString()),
					Long.parseLong(record.get(0).toString()));
				if(!findTheSame(relation))
					Relations.add(relation);
			}
		}
	}
	
	private boolean findTheSame(Relation relation) {
		for(Relation r : Relations) {
			if(r.fromID() == relation.fromID() && r.toID() == relation.toID()) {
				return true;
			}
		}
		return false;
	}
	
	private Record getGraphSize(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BCThread::getSize);
        }
    }

    private List<Record> getNodesList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BCThread::getNodes);
        }
    }

    private List<Record> getRelationsList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(BCThread::getRelations);
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
    
    private StatementResult insertNodeCentrality(Transaction tx, Node node, boolean isDirected) {
    	if(isDirected) {
    		return tx.run("MATCH (n) WHERE ID(n) = " + node.id() + " SET n.BCDirected = " + node.centrality());
    	}
    	return tx.run("MATCH (n) WHERE ID(n) = " + node.id() + " SET n.BCUndirected = " + node.centrality()/2);
    }
    
    private boolean isDirected() {
    	return isDirected;
    }
}
