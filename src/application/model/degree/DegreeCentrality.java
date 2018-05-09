package application.model.degree;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.Node;

public class DegreeCentrality {

	private boolean isIndegree;
	private boolean isOutdegree;
	private boolean isBoth;
	private Driver driver;
	
	private ArrayList<Node> Nodes;
	private ArrayList<String> SaveParameters;
	
	public DegreeCentrality(Driver driver, boolean isIndegree, boolean isOutdegree, boolean isBoth) {
		this.driver = driver;
		this.isIndegree = isIndegree;
		this.isOutdegree = isOutdegree;
		this.isBoth = isBoth;
		Nodes = new ArrayList<Node>();
		SaveParameters = new ArrayList<String>();
	}
	
	public void compute() {
		for(Record record : getNodesList()) {
			Node node = new Node(Integer.parseInt(record.get(0).toString()));
			node.degreeCentrality();
			Nodes.add(node);
		}
		for(Node node : Nodes) {
			node.setIndegree(getRelationsInCount(node.id()));
			node.setOutdegree(getRelationsOutCount(node.id()));
		}
		if(isIndegree)
			SaveParameters.add("Indegree");			
		
		if(isOutdegree)
			SaveParameters.add("Outdegree");		

		if(isBoth)
			SaveParameters.add("Degree");
		
		Transaction transaction = driver.session().beginTransaction();
		for(Node node : Nodes) {
			for(String saveParameter : SaveParameters) {
				insertNodeCentrality(transaction, node, saveParameter);
			}
		}
		transaction.success();
		transaction.close();
	}
	
	private List<Record> getNodesList(){
		try ( Session session = driver.session() ) {
			return session.readTransaction(DegreeCentrality::getNodes);
	    }
	}

	private int getRelationsOutCount(long node){
		try ( Session session = driver.session() ) {
	       Transaction tx = session.beginTransaction();
	       return Integer.parseInt(getRelationsOut(tx,node).get(0).toString());
	    }
	}
	
	private int getRelationsInCount(long node){
		try ( Session session = driver.session() ) {
	       Transaction tx = session.beginTransaction();
	       return Integer.parseInt(getRelationsIn(tx,node).get(0).toString());
	    }
	}
		
	private static Record getRelationsOut(Transaction tx, long node){
		return tx.run("MATCH (n)-[r]->(p) where ID(n)=" + node + " RETURN count(p)").list().get(0);
	}
	    
	private static Record getRelationsIn(Transaction tx, long node){
		return tx.run("MATCH (p)-[r]->(n) where ID(n)=" + node + " RETURN count(p)").list().get(0);
	}

	private static List<Record> getNodes(Transaction tx){
		return tx.run("MATCH (n) RETURN ID(n) LIMIT 25").list();
	}
	
	private StatementResult insertNodeCentrality(Transaction tx, Node node, String saveParameter) {
		if(saveParameter.equals("Indegree"))
			return tx.run("MATCH (n) WHERE ID(n) = " + node.id() + " SET n." + saveParameter + " = " + node.indegree());
		if(saveParameter.equals("Outdegree"))
			return tx.run("MATCH (n) WHERE ID(n) = " + node.id() + " SET n." + saveParameter + " = " + node.outdegree());
		if(saveParameter.equals("Degree")) {
			int degree = node.indegree() + node.outdegree();
			return tx.run("MATCH (n) WHERE ID(n) = " + node.id() + " SET n." + saveParameter + " = " + degree);
		}
		return null;
    }
}
