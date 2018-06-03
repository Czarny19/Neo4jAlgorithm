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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class BCThread implements Runnable{
	
	private Driver neo4jDriver;
	
	private HashMap<Integer,NodeBtwns> nodes;
	private HashMap<Integer,ArrayList<Integer>> relations; 
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private boolean isDirected;
	
	private double nodesCount;
	private double relationsCount;
	
	private int insertCount;
	
	private double maxBetweenness; 
	private double minBetweenness;
	private boolean isFirst;
	
	public BCThread(Driver neo4jDriver, boolean isDirected, ProgressBar progress, TextField progressPrompt) {
		this.neo4jDriver = neo4jDriver;
		this.isDirected = isDirected;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		
		nodes = new HashMap<Integer,NodeBtwns>();
		relations = new HashMap<Integer, ArrayList<Integer>>();
		nodesCount = getNodesCount();
		relationsCount = getRelationsCount();
	}
	
	@Override
	public void run() {
		progressPrompt.setText("Wykonywana operacja: POBIERANIE WIERZCHO£KÓW");
		initNodes();
		progressPrompt.setText("Wykonywana operacja: POBIERANIE RELACJI");
		initRelations(getRelationsList(),isDirected);
		
		BetweennessCentrality betweennessCentrality = new BetweennessCentrality(nodesCount, progress, progressPrompt); 
		betweennessCentrality.init(nodes, relations); 
		betweennessCentrality.compute();
		
		progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");
		Transaction tx = neo4jDriver.session().beginTransaction();
		nodes.forEach((id,node) -> {
			insertNodeCentrality(tx, id, node, isDirected);
			progress.setProgress(0.8 + (insertCount++/nodesCount)/5);
		});
		progress.setProgress(1);
		progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
		tx.success();
		tx.close();
	}
	
	private void initNodes() {		
		for(Record record : getNodesList()) {
			NodeBtwns node = new NodeBtwns(record.get(0).asInt());
			nodes.put(record.get(0).asInt(),node);
			progress.setProgress((nodes.size()/nodesCount)/5);
		}
	}
	
	private void initRelations(List<Record> RelationsList, boolean isDirected) {			
		if(isDirected) {			
			int nodeFrom;
			int nodeTo;
			
			for(Record record : getRelationsList()) {
				nodeFrom = record.get(0).asInt();
				nodeTo = record.get(1).asInt();
				
				if(!relations.containsKey(nodeFrom)) {
					relations.put(nodeFrom,new ArrayList<Integer>());
					relations.get(nodeFrom).add(nodeTo);
				}
				else {
					relations.get(nodeFrom).add(nodeTo);
				}
								
				progress.setProgress(0.2 + (relations.size()/relationsCount)/5);
			}
		}
		if(!isDirected) {			
			int nodeFrom;
			int nodeTo;
			
			for(Record record : RelationsList) {
				nodeFrom = record.get(0).asInt();
				nodeTo = record.get(1).asInt();
				
				if(!relations.containsKey(nodeFrom)) {
					relations.put(nodeFrom,new ArrayList<Integer>());
					relations.get(nodeFrom).add(nodeTo);
				}
				else if(!relations.get(nodeFrom).contains(nodeTo)){
					relations.get(nodeFrom).add(nodeTo);
				}
				
				progress.setProgress(0.2 + (relations.size()/relationsCount)/10);
			}
			for(Record record : RelationsList) {					
				nodeFrom = record.get(1).asInt();
				nodeTo = record.get(0).asInt();
				
				if(!relations.containsKey(nodeFrom)) {
					relations.put(nodeFrom,new ArrayList<Integer>());
					relations.get(nodeFrom).add(nodeTo);
				}
				else if(!relations.get(nodeFrom).contains(nodeTo)){
					relations.get(nodeFrom).add(nodeTo);
				}

				progress.setProgress(0.2 + (relations.size()/relationsCount)/10);
			}
		}
	}
	
	public void algExecToFile(FileCreator algInfo) {
		algInfo.addEmptyLine();
		if(isDirected)
			algInfo.addLine("Rodzaj relacji: Relacje skierowane");
		else
			algInfo.addLine("Rodzaj relacji: Relacje nieskierowane");
		algInfo.addEmptyLine();
		
		isFirst = true;
		nodes.forEach((id,node) -> {
			if(isFirst) {
				maxBetweenness = node.centrality();
				minBetweenness = node.centrality();
				isFirst = false;
			}
			else {
				if(node.centrality() > maxBetweenness) {
					maxBetweenness = node.centrality();
				}
				if(node.centrality() < minBetweenness) {
					minBetweenness = node.centrality();
				}
			}
		});
		
    	algInfo.addLine("Iloœæ pobranych wierzcho³ków = " + nodesCount);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ pobranych relacji      = " + relationsCount);
    	algInfo.addEmptyLine();
    	
		if(isDirected) {
			algInfo.addLine("Maksymalna wartoœæ = " + maxBetweenness);
			for(Record record : getNodesBtwns(maxBetweenness)) {
				algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
			}	
			algInfo.addEmptyLine();
			algInfo.addLine("Minimalna wartoœæ  = " + minBetweenness);
			for(Record record : getNodesBtwns(minBetweenness)) {
				algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
			}
		}
		else {
			algInfo.addLine("Maksymalna wartoœæ = " + maxBetweenness/2);
			for(Record record : getNodesBtwns(maxBetweenness/2)) {
				algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
			}	
			algInfo.addEmptyLine();
			if( minBetweenness != 0) {
				algInfo.addLine("Minimalna wartoœæ  = " + minBetweenness/2);
				for(Record record : getNodesBtwns(minBetweenness/2)) {
					algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
				}
			}
			else {
				algInfo.addLine("Minimalna wartoœæ  = 0");
				for(Record record : getNodesBtwns(0)) {
					algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
				}
			}
		}   	
    }
	
	private double getNodesCount(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(BCThread::getNodesNum).get(0).asDouble();
        }
    }
	
	private double getRelationsCount() {
		try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(BCThread::getRelationsNum).get(0).asDouble();
        }
	}

    private List<Record> getNodesList(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(BCThread::getNodes);
        }
    }

    private List<Record> getRelationsList(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(BCThread::getRelations);
        }
    }
    
    private List<Record> getNodesBtwns(double betweenness) {
    	try ( Session session = neo4jDriver.session() ) {
    		Transaction tx = session.beginTransaction();
    		List<Record> records;
    		if(isDirected) {
    			records = getNodesBCDirected(tx, betweenness);
    		}
    		else {
    			records = getNodesBCUndirected(tx, betweenness);
    		}
            tx.success();
            tx.close();
            return records;
        }
    }
	
    private static List<Record> getRelations(Transaction tx){
    	return tx.run(	"MATCH (n)-[r]->(p) " + 
    					"RETURN ID(n),ID(p),ID(r)").list();
    }

    private static List<Record> getNodes(Transaction tx){
        return tx.run(	"MATCH (n) " +
        				"RETURN ID(n)").list();
    }

    private static Record getNodesNum(Transaction tx){
        return tx.run(	"MATCH (n) " +
        				"RETURN COUNT(n)").list().get(0);
    }
    
    private static Record getRelationsNum(Transaction tx){
        return tx.run(	"MATCH ()-[r]->() " + 
        				"RETURN COUNT(r)").list().get(0);
    }
    
    private static List<Record> getNodesBCUndirected(Transaction tx, double betweenness) {
    	 return tx.run(	"MATCH (n) " + 
    			 		"WHERE n.BCUndirected=" + betweenness + " " +
    			 		"RETURN ID(n),n").list();
    }
    
    private static List<Record> getNodesBCDirected(Transaction tx, double betweenness) {
   	 	return tx.run(	"MATCH (n) " + 
   			 			"WHERE n.BCDirected=" + betweenness + " " +
   	 					"RETURN ID(n),n").list();
   }
    
    private StatementResult insertNodeCentrality(Transaction tx, int id, NodeBtwns node, boolean isDirected) {
    	if(isDirected) {
    		return tx.run(	"MATCH (n) " + 
    						"WHERE ID(n) = " + id + " " + 
    						"SET n.BCDirected = " + node.centrality());
    	}
    	return tx.run(		"MATCH (n) " + 
    						"WHERE ID(n) = " + id + " " +
    						"SET n.BCUndirected = " + node.centrality()/2);
    }
}
