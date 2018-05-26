package application.model.connectivity;

import java.util.HashMap;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public class ConnectivityThread {
	
	private final Driver neo4jdriver;
	
	private HashMap<Integer,NodeConn> Nodes;
	private HashMap<String,RelationConn> Relations;

	public ConnectivityThread(Driver neo4jdriver) {
		this.neo4jdriver = neo4jdriver;
		Nodes = new HashMap<Integer,NodeConn>();
		Relations = new HashMap<String,RelationConn>();
	}
	
	private void initNodes() {
		for(Record record : getNodesList()) {
			NodeConn node = new NodeConn(record.get(0).asInt());
			Nodes.put(record.get(0).asInt(), node);	
		}
	}
	
	private void initRelations() {
		for(Record record : getRelationsList()) {
			int nodeFrom = record.get(1).asInt();
			int nodeTo = record.get(2).asInt();
			Nodes.get(nodeFrom).adj().add(Nodes.get(nodeTo));
		}
	}
	
	private void initRelationsdoEdges() {
		for(Record record : getRelationsList()) {
			int id = record.get(0).asInt();
			int nodeFrom = record.get(1).asInt();
			int nodeTo = record.get(2).asInt();
			Nodes.get(nodeFrom).adj().add(Nodes.get(nodeTo));
			Nodes.get(nodeTo).adj().add(Nodes.get(nodeFrom));
			RelationConn relation = new RelationConn(id,nodeFrom,nodeTo);
			Relations.put(nodeFrom + " " + nodeTo, relation);
		}
	}
	
	public void compute(boolean doEdges) {
		initNodes();
		if(doEdges) {
			initRelationsdoEdges();
		}
		if(!doEdges) {
			initRelations();
		}		
	    Connectivity connectivity = new Connectivity(Nodes);
	    connectivity.compute(Nodes, doEdges);
	    Transaction transaction = neo4jdriver.session().beginTransaction();
	    if(!doEdges) {	
	    	Nodes.forEach((id,node) -> {				    		
			    insertNodeCut(transaction,id,node.isArticulationPoint());
	    	});
	    }
	    if(doEdges) {
	    	Relations.forEach((id,relation) ->{
	    		if(connectivity.bridges().contains(id))
	    			insertBridge(transaction,relation.id(),true);
	    		else
	    			insertBridge(transaction,relation.id(),false);
	    	});
	    }
	    transaction.success();
		transaction.close();
	}
	
	
	private List<Record> getNodesList(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(ConnectivityThread::getNodes);
        }
    }

    private List<Record> getRelationsList(){
    	try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(ConnectivityThread::getRelations);
        }
    }
    
    private static List<Record> getNodes(Transaction tx){
        return tx.run("MATCH (n) RETURN ID(n) LIMIT 25").list();
    }
	
	private static List<Record> getRelations(Transaction tx){
    	return tx.run("MATCH (n)-[r]->(p) RETURN distinct ID(r),ID(n),ID(p)").list();
    }

	private StatementResult insertNodeCut(Transaction tx, int nodeId, boolean isCut) {
		if(isCut)
			return tx.run("MATCH (n) WHERE ID(n) = " + nodeId + " SET n.isCut = 1");
		
		return tx.run("MATCH (n) WHERE ID(n) = " + nodeId + " SET n.isCut = 0");
    }
	
	private StatementResult insertBridge(Transaction tx, int relationId, boolean isBridge) {
		if(isBridge)
			return tx.run("MATCH ()-[r]->() WHERE ID(r) = " + relationId + " SET r.isBridge = 1");
		
		return tx.run("MATCH ()-[r]->() WHERE ID(r) = " + relationId + " SET r.isBridge = 0");
    }
}
