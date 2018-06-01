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
	
	private Driver neo4jdriver;
	
	private HashMap<Integer,NodeBtwns> Nodes;
	private HashMap<Integer,ArrayList<Integer>> Relations; 
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private boolean isDirected;
	
	private double NodesCount;
	private double RelationsCount;
	
	private int insertCount;
	
	private double maxBetweenness; 
	private double minBetweenness;
	private boolean isFirst;
	
	public BCThread(Driver neo4jdriver, boolean isDirected, ProgressBar progress, TextField progressPrompt) {
		this.neo4jdriver = neo4jdriver;
		this.isDirected = isDirected;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		Nodes = new HashMap<Integer,NodeBtwns>();
		Relations = new HashMap<Integer, ArrayList<Integer>>();
		NodesCount = getNodesCount();
		RelationsCount = getRelationsCount();
	}
	
	@Override
	public void run() {
		initNodes(getNodesList());
		initRelations(getRelationsList(),isDirected);
		BetweennessCentrality betweennessCentrality = new BetweennessCentrality(NodesCount, progress, progressPrompt); 
		betweennessCentrality.init(Nodes, Relations); 
		betweennessCentrality.compute();
		
		progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");
		Transaction transaction = neo4jdriver.session().beginTransaction();
		Nodes.forEach((id,node) -> {
			insertNodeCentrality(transaction, id, node, isDirected);
			progress.setProgress(0.8 + (insertCount++/NodesCount)/5);
		});
		progress.setProgress(1);
		progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
		transaction.success();
		transaction.close();
	}
	
	public void algExecToFile(FileCreator algInfo) {
		algInfo.addEmptyLine();
		if(isDirected)
			algInfo.addLine("Rodzaj relacji: Relacje skierowane");
		else
			algInfo.addLine("Rodzaj relacji: Relacje nieskierowane");
		algInfo.addEmptyLine();
		
		isFirst = true;
		Nodes.forEach((id,node) -> {
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
		
    	algInfo.addLine("Iloœæ pobranych wierzcho³ków = " + NodesCount);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ pobranych relacji      = " + RelationsCount);
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
	
	private void initNodes(List<Record> NodesList) {
		progressPrompt.setText("Wykonywana operacja: POBIERANIE WIERZCHO£KÓW");
		for(Record record : NodesList) {
			NodeBtwns node = new NodeBtwns(record.get(0).asInt());
			Nodes.put(record.get(0).asInt(),node);
			progress.setProgress((Nodes.size()/NodesCount)/5);
		}
	}
	
	private void initRelations(List<Record> RelationsList, boolean isDirected) {	
		progressPrompt.setText("Wykonywana operacja: POBIERANIE RELACJI");
		if(isDirected) {
			
			int nodeFrom;
			int nodeTo;
			
			for(Record record : RelationsList) {
				nodeFrom = record.get(0).asInt();
				nodeTo = record.get(1).asInt();
				
				if(!Relations.containsKey(nodeFrom)) {
					Relations.put(nodeFrom,new ArrayList<Integer>());
					Relations.get(nodeFrom).add(nodeTo);
				}
				else {
					Relations.get(nodeFrom).add(nodeTo);
				}
								
				progress.setProgress(0.2 + (Relations.size()/RelationsCount)/5);
			}
		}
		if(!isDirected) {
			
			int nodeFrom;
			int nodeTo;
			
			for(Record record : RelationsList) {
				nodeFrom = record.get(0).asInt();
				nodeTo = record.get(1).asInt();
				
				if(!Relations.containsKey(nodeFrom)) {
					Relations.put(nodeFrom,new ArrayList<Integer>());
					Relations.get(nodeFrom).add(nodeTo);
				}
				else if(!Relations.get(nodeFrom).contains(nodeTo)){
					Relations.get(nodeFrom).add(nodeTo);
				}
				
				progress.setProgress(0.2 + (Relations.size()/RelationsCount)/10);
			}
			for(Record record : RelationsList) {	
				
				nodeFrom = record.get(1).asInt();
				nodeTo = record.get(0).asInt();
				
				if(!Relations.containsKey(nodeFrom)) {
					Relations.put(nodeFrom,new ArrayList<Integer>());
					Relations.get(nodeFrom).add(nodeTo);
				}
				else if(!Relations.get(nodeFrom).contains(nodeTo)){
					Relations.get(nodeFrom).add(nodeTo);
				}

				progress.setProgress(0.2 + (Relations.size()/RelationsCount)/10);
			}
		}
	}
	
	private double getNodesCount(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(BCThread::getNodesNum).get(0).asDouble();
        }
    }
	
	private double getRelationsCount() {
		try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(BCThread::getRelationsNum).get(0).asDouble();
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
    
    private List<Record> getNodesBtwns(double betweenness) {
    	try ( Session session = neo4jdriver.session() ) {
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
	
    private static List<Record> getRelations(Transaction tx)
    {
    	return tx.run("MATCH (n)-[r]->(p) RETURN ID(n),ID(p),ID(r)").list();
    }

    private static List<Record> getNodes(Transaction tx){
        return tx.run("MATCH (n) RETURN ID(n)").list();
    }

    private static Record getNodesNum(Transaction tx){
        return tx.run("MATCH (n) RETURN COUNT(n)").list().get(0);
    }
    
    private static Record getRelationsNum(Transaction tx){
        return tx.run("MATCH ()-[r]->() RETURN COUNT(r)").list().get(0);
    }
    
    private static List<Record> getNodesBCUndirected(Transaction tx, double betweenness) {
    	 return tx.run("MATCH (n) WHERE n.BCUndirected=" + betweenness + " RETURN ID(n),n").list();
    }
    
    private static List<Record> getNodesBCDirected(Transaction tx, double betweenness) {
   	 return tx.run("MATCH (n) WHERE n.BCDirected=" + betweenness + " RETURN ID(n),n").list();
   }
    
    private StatementResult insertNodeCentrality(Transaction tx, int id, NodeBtwns node, boolean isDirected) {
    	if(isDirected) {
    		return tx.run("MATCH (n) WHERE ID(n) = " + id + " SET n.BCDirected = " + node.centrality());
    	}
    	return tx.run("MATCH (n) WHERE ID(n) = " + id + " SET n.BCUndirected = " + node.centrality()/2);
    }
}
