package application.model.connectivity;

import java.util.HashMap;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.Node;

public class VCThread {
	
	private final Driver driver;
	
	private HashMap<Integer,Node> Nodes; 			//HashMap<nodeID, node>
	private HashMap<Integer,Relation> Relations; 	//HashMap<relationID, relation>

	public VCThread(Driver driver) {
		this.driver = driver;
		Nodes = new HashMap<Integer,Node>();
		Relations = new HashMap<Integer,Relation>();
	}
	
	private void initNodes() {
		for(Record record : getNodesList()) {
			Node node = new Node(Integer.parseInt(record.get(0).toString()));
			node.connectivity();
			Nodes.put(Integer.parseInt(record.get(0).toString()), node);	
		}
	}
	
	private void initRelations() {
		for(Record record : getRelationsList()) {
			int nodeFrom = Integer.parseInt(record.get(1).toString());
			int nodeTo = Integer.parseInt(record.get(2).toString());
			Nodes.get(nodeFrom).adj().add(Nodes.get(nodeTo));
		}
	}
	
	private void initRelationsdoEdges() {
		for(Record record : getRelationsList()) {
			int id = Integer.parseInt(record.get(0).toString());
			int nodeFrom = Integer.parseInt(record.get(1).toString());
			int nodeTo = Integer.parseInt(record.get(2).toString());
			Nodes.get(nodeFrom).adj().add(Nodes.get(nodeTo));
			Nodes.get(nodeTo).adj().add(Nodes.get(nodeFrom));
			Relation relation = new Relation(id,nodeFrom,nodeTo);
			Relations.put(id, relation);
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
	    VertexConnectivity bic = new VertexConnectivity(Nodes);
	    bic.compute(Nodes, doEdges);
	    int count = 0;
	    Transaction transaction = driver.session().beginTransaction();
	    if(!doEdges) {	    			    
		    for (Integer nodeId : bic.articulationPoints()) {
		    	count++;		    	
		    	insertNodeCut(transaction,nodeId);		    	
		    }	
		    System.out.println("Vertex Connectivity: " + count);
	    }
	    if(doEdges) {
	    	for(Relation relation : bic.bridges()) {
	    		List<Record> PossibleBridges;
	    		PossibleBridges = getRelationBetweenNodes(transaction,relation.nodeFrom(),relation.nodeTo());
	    		if(PossibleBridges.isEmpty())
	    			PossibleBridges = getRelationBetweenNodes(transaction,relation.nodeTo(),relation.nodeFrom());
	    		else if(!PossibleBridges.isEmpty())
	    			PossibleBridges.addAll(getRelationBetweenNodes(transaction,relation.nodeTo(),relation.nodeFrom()));
	    		if(PossibleBridges.size() == 1) {
	    			count ++;
	    			insertBridge(transaction,Integer.parseInt(PossibleBridges.get(0).get(0).toString()));
	    		}
	    	}
	    	System.out.println("Edge Connectivity: " + count);
	    }
	    transaction.success();
		transaction.close();
	}
	
	
	private List<Record> getNodesList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(VCThread::getNodes);
        }
    }

    private List<Record> getRelationsList(){
    	try ( Session session = driver.session() ) {
            return session.readTransaction(VCThread::getRelations);
        }
    }
    
    private static List<Record> getNodes(Transaction tx){
        return tx.run("MATCH (n) RETURN ID(n) LIMIT 25").list();
    }
	
	private static List<Record> getRelations(Transaction tx){
    	return tx.run("MATCH (n)-[r]->(p) RETURN distinct ID(r),ID(n),ID(p)").list();
    }
	
	private static List<Record> getRelationBetweenNodes(Transaction tx, int nodeFrom, int nodeTo){
    	return tx.run("MATCH (n)-[r]->(p) WHERE ID(n)=" + nodeFrom + " AND ID(p)=" + nodeTo + " RETURN distinct ID(r)").list();
    }
	
	private StatementResult insertNodeCut(Transaction tx, int nodeId) {
    	return tx.run("MATCH (n) WHERE ID(n) = " + nodeId + " SET n.isCut = 1");
    }
	
	private StatementResult insertBridge(Transaction tx, int relationId) {
    	return tx.run("MATCH ()-[r]->() WHERE ID(r) = " + relationId + " SET r.isBridge = 1");
    }
}
