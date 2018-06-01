package application.model.connectivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.FileCreator;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class ConnectivityThread implements Runnable{
	
	private final Driver neo4jdriver;
	
	private HashMap<Integer,NodeConn> Nodes;
	private ArrayList<RelationConn> Relations;
	
	private boolean doEdges;
	private boolean doNodes;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private double NodesCount;
	private double RelationsCount;
	
	private int insertCount;

	public ConnectivityThread(Driver neo4jdriver, boolean doNodes, boolean doEdges, ProgressBar progress, TextField progressPrompt) {
		this.neo4jdriver = neo4jdriver;
		this.doNodes = doNodes;
		this.doEdges = doEdges;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		this.NodesCount = getNodesCount();
		this.RelationsCount = getRelationsCount();
		
		Nodes = new HashMap<Integer,NodeConn>();
		Relations = new ArrayList<RelationConn>();
	}
	
	private void initNodes() {
		progressPrompt.setText("Wykonywana operacja: POBIERANIE WIERZCHO£KÓW");
		for(Record record : getNodesList()) {
			NodeConn node = new NodeConn(record.get(0).asInt());
			Nodes.put(record.get(0).asInt(), node);	
			progress.setProgress((Nodes.size()/NodesCount)/5);
		}
	}
	
	private void initRelations() {
		progressPrompt.setText("Wykonywana operacja: POBIERANIE RELACJI");
		for(Record record : getRelationsList()) {
			int id = record.get(0).asInt();
			int nodeFrom = record.get(1).asInt();
			int nodeTo = record.get(2).asInt();
			
			Nodes.get(nodeFrom).adj().add(Nodes.get(nodeTo));
			//TODO B³¹d
			Nodes.get(nodeTo).adj().add(Nodes.get(nodeFrom));
			
			if(doEdges) {				
				RelationConn relation = new RelationConn(id,nodeFrom,nodeTo);
				Relations.add(relation);
			}
			progress.setProgress(0.2 + (Relations.size()/RelationsCount)/5);
		}
	}
	
	public void algExecToFile(FileCreator algInfo) {
    	algInfo.addLine("test");
    }
	
	@Override
	public void run() {
		initNodes();
		initRelations();
		
		progressPrompt.setText("Wykonywana operacja: OBLICZANIE");
	    Connectivity connectivity = new Connectivity(Nodes, progress, NodesCount);
	    connectivity.compute(doNodes, doEdges);
	    
	    progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");
	    Transaction transaction = neo4jdriver.session().beginTransaction();
	    if(doNodes) {
	    	Nodes.forEach((id,node) -> {				    		
			    insertNodeCut(transaction,id,node.isArticulationPoint());
			    
			    progress.setProgress(0.8 + (insertCount++/NodesCount)/5);
	    	});
	    }
	    if(doEdges) {
	    	for(RelationConn relation : Relations) {
	    		System.out.println(relation.id());
	    		if(connectivity.bridges().contains(relation.nodeFrom() + " " + relation.nodeTo()))
	    			insertBridge(transaction,relation.id(),true);
	    		else
	    			insertBridge(transaction,relation.id(),false);
	    		
	    		progress.setProgress(0.8 + (insertCount++/NodesCount)/5);
	    	}
//	    	Relations.forEach((id,relation) ->{
//	    		
//	    	});
	    }
	    progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
	    transaction.success();
		transaction.close();
	}
	
	private double getNodesCount(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(ConnectivityThread::getNodesNum).get(0).asDouble();
        }
    }
	
	private double getRelationsCount() {
		try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(ConnectivityThread::getRelationsNum).get(0).asDouble();
        }
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
    
    private static Record getNodesNum(Transaction tx){
        return tx.run("MATCH (n) RETURN COUNT(n)").list().get(0);
    }
    
    private static Record getRelationsNum(Transaction tx){
        return tx.run("MATCH ()-[r]->() RETURN COUNT(r)").list().get(0);
    }
    
    private static List<Record> getNodes(Transaction tx){
        return tx.run("MATCH (n) RETURN ID(n)").list();
    }
	
	private static List<Record> getRelations(Transaction tx){
    	return tx.run("MATCH (n)-[r]->(p) RETURN ID(r),ID(n),ID(p)").list();
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
