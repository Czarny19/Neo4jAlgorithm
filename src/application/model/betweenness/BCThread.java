package application.model.betweenness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.FileCreator;
import application.model.Relation;

public class BCThread implements Runnable{
	
	private Driver neo4jdriver;
	private HashMap<Integer,NodeBtwns> Nodes;
	private ArrayList<Relation> Relations; 
	
	private boolean isDirected;
	
	public BCThread(Driver neo4jdriver, boolean isDirected) {
		this.isDirected = isDirected;
		this.neo4jdriver = neo4jdriver;
		Nodes = new HashMap<Integer,NodeBtwns>();
		Relations = new ArrayList<Relation>();		
	}
	
	@Override
	public void run() {
		initNodes(getNodesList());
		initRelations(getRelationsList(),isDirected());
		BetweennessCentrality betweennessCentrality = new BetweennessCentrality(getGraphSize().get(0).asInt()); 
		betweennessCentrality.init(Nodes, Relations); 
		betweennessCentrality.compute();
		
		Transaction transaction = neo4jdriver.session().beginTransaction();
		Nodes.forEach((id,node) -> {
			insertNodeCentrality(transaction, id, node, isDirected());
		});
		transaction.success();
		transaction.close();
	}
	
	public void algExecToFile(FileCreator algInfo) {
    	algInfo.addLine("test");
    }
	
	private void initNodes(List<Record> NodesList) {
		for(Record record : NodesList) {
			NodeBtwns node = new NodeBtwns(record.get(0).asInt());
			Nodes.put(record.get(0).asInt(),node);
		}
	}
	
	private void initRelations(List<Record> RelationsList, boolean isDirected) {
		for(Record record : RelationsList) {
			Relation relation = new Relation(
				record.get(0).asInt(),
				record.get(1).asInt());
			Relations.add(relation);
		}
		if(!isDirected) {
			for(Record record : RelationsList) {	
				Relation relation = new Relation(
					record.get(1).asInt(),
					record.get(0).asInt());
				if(!findTheSame(relation))
					Relations.add(relation);
			}
		}
	}
	
	private boolean findTheSame(Relation relation) {
		for(Relation r : Relations) {
			if(r.nodeFrom() == relation.nodeFrom() && r.nodeTo() == relation.nodeTo()) {
				return true;
			}
		}
		return false;
	}
	
	private Record getGraphSize(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(BCThread::getSize);
        }
    }

    private List<Record> getNodesList(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(BCThread::getNodes);
        }
    }

    private List<Record> getRelationsList(){
        try ( Session session = neo4jdriver.session() ) {
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
    
    private StatementResult insertNodeCentrality(Transaction tx, int id, NodeBtwns node, boolean isDirected) {
    	if(isDirected) {
    		return tx.run("MATCH (n) WHERE ID(n) = " + id + " SET n.BCDirected = " + node.centrality());
    	}
    	return tx.run("MATCH (n) WHERE ID(n) = " + id + " SET n.BCUndirected = " + node.centrality()/2);
    }
    
    private boolean isDirected() {
    	return isDirected;
    }
}
