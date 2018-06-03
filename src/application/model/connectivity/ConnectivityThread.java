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
	
	private final Driver neo4jDriver;
	
	private HashMap<Integer,NodeConn> nodes;
	private ArrayList<RelationConn> relations;
	
	private boolean doEdges;
	private boolean doNodes;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private double nodesCount;
	private double relationsCount;
	
	private int insertCount;

	public ConnectivityThread(Driver neo4jDriver, boolean doNodes, boolean doEdges, ProgressBar progress, TextField progressPrompt) {
		this.neo4jDriver = neo4jDriver;
		this.doNodes = doNodes;
		this.doEdges = doEdges;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		this.nodesCount = getNodesCount();
		this.relationsCount = getRelationsCount();
		
		nodes = new HashMap<Integer,NodeConn>();
		relations = new ArrayList<RelationConn>();
	}
	
	private void initNodes() {
		progressPrompt.setText("Wykonywana operacja: POBIERANIE WIERZCHO£KÓW");
		for(Record record : getNodesList()) {
			NodeConn node = new NodeConn(record.get(0).asInt());
			nodes.put(record.get(0).asInt(), node);	
			progress.setProgress((nodes.size()/nodesCount)/5);
		}
	}
	
	private void initRelations() {
		int id;
		int nodeFrom;
		int nodeTo;
		
		progressPrompt.setText("Wykonywana operacja: POBIERANIE RELACJI");
		for(Record record : getRelationsList()) {
			id = record.get(0).asInt();
			nodeFrom = record.get(1).asInt();
			nodeTo = record.get(2).asInt();
			nodes.get(nodeFrom).adj().add(nodes.get(nodeTo));
			
			if(doEdges) {	
				nodes.get(nodeTo).adj().add(nodes.get(nodeFrom));
				RelationConn relation = new RelationConn(id,nodeFrom,nodeTo);
				relations.add(relation);
			}
			progress.setProgress(0.2 + (relations.size()/relationsCount)/5);
		}
	}
	
	public void algExecToFile(FileCreator algInfo) {
		algInfo.addEmptyLine();
		if(doNodes)
			algInfo.addLine("Wyszukiwane wartoœci : Punkty artykulacji (przeciêcia)");
		if(doEdges)
			algInfo.addLine("Wyszukiwane wartoœci : Mosty");
		algInfo.addEmptyLine();
		algInfo.addLine("Iloœæ pobranych wierzcho³ków = " + nodesCount);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ pobranych relacji      = " + relationsCount);
    	algInfo.addEmptyLine();
    	
    	if(doNodes) {
    		algInfo.addLine("Iloœæ punktów artykulacji = " + getCutsCount());
    		algInfo.addEmptyLine();
			algInfo.addLine("Wyznaczone punkty artykulacji");
			for(Record record : getCuts()) {
				algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
			}
		}
		if(doEdges) {
			algInfo.addLine("Iloœæ mostów= " + getBridgesCount());
			algInfo.addLine("Wyznaczone mosty");
			for(Record record : getBridges()) {
				algInfo.addLine("ID: " + record.get(0).asInt() +"	Relacja : " + record.get(1).asMap());
			}
		}
    }
	
	@Override
	public void run() {
		initNodes();
		initRelations();
		
		progressPrompt.setText("Wykonywana operacja: OBLICZANIE");
	    Connectivity connectivity = new Connectivity(nodes, progress, nodesCount);
	    connectivity.compute(doNodes, doEdges);
	    
	    progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");
	    Transaction tx = neo4jDriver.session().beginTransaction();
	    if(doNodes) {
	    	nodes.forEach((id,node) -> {				    		
			    insertNodeCut(tx,id,node.isArticulationPoint());		    
			    progress.setProgress(0.8 + (insertCount++/nodesCount)/5);
	    	});
	    }
	    if(doEdges) {
	    	for(RelationConn relation : relations) {
	    		if(connectivity.bridges().contains(relation.nodeFrom() + " " + relation.nodeTo()))
	    			insertBridge(tx,relation.id(),true);
	    		else
	    			insertBridge(tx,relation.id(),false);	    		
	    		progress.setProgress(0.8 + (insertCount++/nodesCount)/5);
	    	}
	    }
	    progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
	    tx.success();
		tx.close();
	}
	
	private double getNodesCount(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getNodesNum).get(0).asDouble();
        }
    }
	
	private double getRelationsCount() {
		try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getRelationsNum).get(0).asDouble();
        }
	}
	
	private List<Record> getNodesList(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getNodes);
        }
    }

    private List<Record> getRelationsList(){
    	try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getRelations);
        }
    }
    
    private List<Record> getCuts(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getCuts);
        }
    }

    private List<Record> getBridges(){
    	try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getBridges);
        }
    }
    
    private int getCutsCount(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getCutsNum).get(0).asInt();
        }
    }
	
	private int getBridgesCount() {
		try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(ConnectivityThread::getBridgesNum).get(0).asInt();
        }
	}
    
    private static Record getNodesNum(Transaction tx){
        return tx.run(	"MATCH (n) " +
        				"RETURN COUNT(n)").list().get(0);
    }
    
    private static Record getRelationsNum(Transaction tx){
        return tx.run(	"MATCH ()-[r]->() " +
        				"RETURN COUNT(r)").list().get(0);
    }
    
    private static List<Record> getNodes(Transaction tx){
        return tx.run(	"MATCH (n) " +
        				"RETURN ID(n)").list();
    }
	
	private static List<Record> getRelations(Transaction tx){
    	return tx.run(	"MATCH (n)-[r]->(p) " +
    					"RETURN ID(r),ID(n),ID(p)").list();
    }

	private StatementResult insertNodeCut(Transaction tx, int nodeId, boolean isCut) {
		String query = 	"MATCH (n) " +
						"WHERE ID(n) = " + nodeId + " " +
						"SET n.isCut = ";
		if(isCut)
			return tx.run(query + "1");		
		return tx.run(query + "0");
    }
	
	private StatementResult insertBridge(Transaction tx, int relationId, boolean isBridge) {
		String query = 	"MATCH ()-[r]->() " +
						"WHERE ID(r) = " + relationId + " " +
						"SET r.isBridge = ";
		if(isBridge)
			return tx.run(query + "1");		
		return tx.run(query + "0");
    }
	
	private static List<Record> getCuts(Transaction tx){
    	return tx.run(	"MATCH (n) " +
    					"WHERE n.isCut=1 " +
    					"RETURN ID(n),n").list();
    }
	
	private static List<Record> getBridges(Transaction tx){
    	return tx.run(	"MATCH ()-[r]->() " +
    					"WHERE r.isBridge=1 " +
    					"RETURN ID(r),r").list();
    }
	
	private static Record getCutsNum(Transaction tx){
    	return tx.run(	"MATCH (n) " +
    					"WHERE n.isCut=1 "+
    					"RETURN count(n)").list().get(0);
    }
	
	private static Record getBridgesNum(Transaction tx){
    	return tx.run(	"MATCH ()-[r]->() " +
    					"WHERE r.isBridge=1 " +
    					"RETURN count(r)").list().get(0);
    }
}
