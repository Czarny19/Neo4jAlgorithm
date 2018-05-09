package application.model.connectivity;

import java.util.HashMap;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import application.model.Node;

public class VCThread {
	
	private final Driver driver;
	
	private HashMap<Integer,Node> Nodes; // HashMap<nodeID, node>

	public VCThread(Driver driver) {
		this.driver = driver;
		Nodes = new HashMap<Integer,Node>();
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
			int nodeFrom = Integer.parseInt(record.get(0).toString());
			int nodeTo = Integer.parseInt(record.get(1).toString());
			Nodes.get(nodeFrom).adj().add(Nodes.get(nodeTo));
		}
	}
	
	public void compute() {
		initNodes();
		initRelations();
	    VertexConnectivity bic = new VertexConnectivity(Nodes);
	    bic.compute(Nodes);
	    int count = 0;
	    for (Node node : Nodes.values()) {
	    	if(node.isArticulationPoint()) {
	    		count++;
	    		System.out.println(node.id());
	    	}
	    }
	    System.out.println("Vertex Connectivity: " + count);
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
    	return tx.run("MATCH (n)-[r]->(p) RETURN distinct ID(n),ID(p)").list();
    }

}
