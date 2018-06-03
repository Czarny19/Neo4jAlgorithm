package application.model.coloring;

import java.util.ArrayList;
import java.util.Collections;
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

public class GraphColoringThread implements Runnable{

	private Driver neo4jDriver;
	private HashMap<Integer, NodeGC> nodesNoRelations;
	private ArrayList<NodeGC> nodes;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private double nodesCount;
	private double relationsCount;
	
	private int insertCount;
	private int computedCount;
	
	public GraphColoringThread(Driver neo4jDriver, ProgressBar progress, TextField progressPrompt){
		this.neo4jDriver = neo4jDriver;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		this.nodesCount = getNodesCount();
		this.relationsCount = getRelationsCount();
	}
	
	private void getGraph() {
		nodesNoRelations = new HashMap<Integer, NodeGC>();
		nodes = new ArrayList<NodeGC>();
		
		progressPrompt.setText("Wykonywana operacja: POBIERANIE WIERZCHO£KÓW");		
		for(Record record : getNodesList()) {
			NodeGC node = new NodeGC(record.get(0).asInt());
			nodesNoRelations.put(node.id(),node);
			progress.setProgress((nodesNoRelations.size()/nodesCount)/5);
		}	
		
		progressPrompt.setText("Wykonywana operacja: POBIERANIE RELACJI");
		for(Record relationRecord : getRelationsList()) {
			int nodeFrom = relationRecord.get(0).asInt();
			int nodeTo = relationRecord.get(1).asInt();
			
			nodesNoRelations.get(nodeFrom).neighbors().add(nodeTo);
			
			progress.setProgress(0.2 + (computedCount++/relationsCount)/5);
		}
		nodesNoRelations.forEach((id,node) -> {
			nodes.add(node);
		});
		nodesNoRelations = null;
	}
	
	@Override
	public void run(){
		getGraph();
		
		progressPrompt.setText("Wykonywana operacja: OBLICZANIE");		
		computedCount = 0;
		
		Collections.sort(nodes, new NodeComparator());
		for(int i = 0; i < nodes.size(); i++) {
			for(int j = 0; j < nodes.size();) {
				if(nodes.get(i).neighbors().contains(nodes.get(j).id())){
					if(nodes.get(j).color() == nodes.get(i).color())
						nodes.get(j).setColor(nodes.get(j).color()+1);	
					else
						j+=1;
				}
				else
					j+=1;
			}
			progress.setProgress(0.4 + ((computedCount++/nodesCount)/2.5));
		}
		
		progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");		
		Transaction tx = neo4jDriver.session().beginTransaction();		
		for(NodeGC node : nodes) {
			insertNodeColor(tx,node.id(),node.color());
			progress.setProgress(0.8 + (insertCount++/nodesCount)/5);
		}

		progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
		tx.success();
		tx.close();				
	}
	
	public void algExecToFile(FileCreator algInfo) {
		int maxColor = getMaxColor() + 1;
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ pobranych wierzcho³ków = " + nodesCount);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ pobranych relacji      = " + relationsCount);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Liczba chromatyczna dla grafu = " + maxColor);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ wierzcho³ków podzielna kolorami : ");
    	algInfo.addEmptyLine();

    	for(int i = 0; i < maxColor; i++) {
    		algInfo.addLine("Kolor : " + i +" | Iloœæ wierzcho³ków = " + getNodesOfColorCount(i));
    	}
    	
    }
	
	private double getNodesCount(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(GraphColoringThread::getNodesNum).get(0).asDouble();
        }
    }
	
	private double getRelationsCount(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(GraphColoringThread::getRelationsNum).get(0).asDouble();
        }
    }

    private List<Record> getNodesList(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(GraphColoringThread::getNodes);
        }
    }
    
    private List<Record> getRelationsList(){
    	try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(GraphColoringThread::getRelations);
        }
    }
    
    private int getMaxColor(){
    	try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(GraphColoringThread::getMaxColor).get(0).asInt();
        }
    }
    
    private int getNodesOfColorCount(int color){
    	try ( Session session = neo4jDriver.session() ) {
    		Transaction tx = session.beginTransaction();
 	        return getNodesOfColorCount(tx,color).get(0).asInt();
        }
    }

    private static List<Record> getNodes(Transaction tx){
        return tx.run(	"MATCH (n) " + 
        				"RETURN ID(n)").list();
    }
    
    private static List<Record> getRelations(Transaction tx){
        return tx.run(	"MATCH (n)-[]->(p) " + 
        				"RETURN ID(n),ID(p)").list();
    }

    private static Record getNodesNum(Transaction tx){
        return tx.run(	"MATCH (n) " + 
        				"RETURN COUNT(n)").list().get(0);
    }
    
    private static Record getRelationsNum(Transaction tx){
        return tx.run(	"MATCH ()-[r]->()" + 
        				"RETURN COUNT(r)").list().get(0);
    }
    
    private StatementResult insertNodeColor(Transaction tx, long node, int color) {
    	return tx.run(	"MATCH (n) " +
    					"WHERE ID(n) = " + node + " " +
    					"SET n.Color = " + color);
    }
    
    private static Record getMaxColor(Transaction tx) {
    	return tx.run(	"MATCH (n) " +
    					"RETURN max(n.Color)").list().get(0);
    }
    
    private static Record getNodesOfColorCount(Transaction tx, int color) {
    	return tx.run(	"MATCH (n) " +
    					"WHERE n.Color=" + color + " " +
    					"RETURN count(n)").list().get(0);
    }
}
