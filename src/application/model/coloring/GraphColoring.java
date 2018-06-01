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

public class GraphColoring implements Runnable{

	private Driver neo4jdriver;
	private HashMap<Integer, NodeGC> NodesNoRelations;
	private ArrayList<NodeGC> Nodes;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private double nodesCount;
	private double relationsCount;
	
	private int insertCount;
	private int computedCount;
	
	public GraphColoring(Driver neo4jdriver, ProgressBar progress, TextField progressPrompt){
		this.neo4jdriver = neo4jdriver;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		this.nodesCount = getNodesCount();
		this.relationsCount = getRelationsCount();
	}
	
	private void getGraph() {
		NodesNoRelations = new HashMap<Integer, NodeGC>();
		Nodes = new ArrayList<NodeGC>();
		
		progressPrompt.setText("Wykonywana operacja: POBIERANIE WIERZCHO£KÓW");		
		for(Record record : getNodesList()) {
			NodeGC node = new NodeGC(record.get(0).asInt());
			NodesNoRelations.put(node.id(),node);
			progress.setProgress((NodesNoRelations.size()/nodesCount)/5);
		}	
		
		progressPrompt.setText("Wykonywana operacja: POBIERANIE RELACJI");
		for(Record relationRecord : getRelationsList()) {
			int nodeFrom = relationRecord.get(0).asInt();
			int nodeTo = relationRecord.get(1).asInt();
			
			NodesNoRelations.get(nodeFrom).neighbors().add(nodeTo);
			
			progress.setProgress(0.2 + (computedCount++/relationsCount)/5);
		}
		NodesNoRelations.forEach((id,node) -> {
			Nodes.add(node);
		});
		NodesNoRelations = null;
	}
	
	@Override
	public void run(){
		getGraph();
		
		progressPrompt.setText("Wykonywana operacja: OBLICZANIE");		
		computedCount = 0;
		
		Collections.sort(Nodes, new NodeComparator());
		for(int i = 0; i < Nodes.size(); i++) {
			for(int j = 0; j < Nodes.size();) {
				if(Nodes.get(i).neighbors().contains(Nodes.get(j).id())){
					if(Nodes.get(j).color() == Nodes.get(i).color())
						Nodes.get(j).setColor(Nodes.get(j).color()+1);	
					else
						j+=1;
				}
				else
					j+=1;
			}
			progress.setProgress(0.4 + ((computedCount++/nodesCount)/2.5));
		}
		
		progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");
		
		Transaction transaction = neo4jdriver.session().beginTransaction();		
		for(NodeGC vertex : Nodes) {
			insertNodeColor(transaction,vertex.id(),vertex.color());
			progress.setProgress(0.8 + (insertCount++/nodesCount)/5);
		}

		progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
		transaction.success();
		transaction.close();				
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
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(GraphColoring::getNodesNum).get(0).asDouble();
        }
    }
	
	private double getRelationsCount(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(GraphColoring::getRelationsNum).get(0).asDouble();
        }
    }

    private List<Record> getNodesList(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(GraphColoring::getNodes);
        }
    }
    
    private List<Record> getRelationsList(){
    	try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(GraphColoring::getRelations);
        }
    }
    
    private int getMaxColor(){
    	try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(GraphColoring::getMaxColor).get(0).asInt();
        }
    }
    
    private int getNodesOfColorCount(int color){
    	try ( Session session = neo4jdriver.session() ) {
    		Transaction tx = session.beginTransaction();
 	        return getNodesOfColorCount(tx,color).get(0).asInt();
        }
    }

    private static List<Record> getNodes(Transaction tx){
        return tx.run("MATCH (n) RETURN ID(n)").list();
    }
    
    private static List<Record> getRelations(Transaction tx){
        return tx.run("MATCH (n)-[]->(p) RETURN ID(n),ID(p)").list();
    }

    private static Record getNodesNum(Transaction tx){
        return tx.run("MATCH (n) RETURN COUNT(n)").list().get(0);
    }
    
    private static Record getRelationsNum(Transaction tx){
        return tx.run("MATCH ()-[r]->() RETURN COUNT(r)").list().get(0);
    }
    
    private StatementResult insertNodeColor(Transaction tx, long node, int color) {
    	return tx.run("MATCH (n) WHERE ID(n) = " + node + " SET n.Color = " + color);
    }
    
    private static Record getMaxColor(Transaction tx) {
    	return tx.run("MATCH (n) RETURN max(n.Color)").list().get(0);
    }
    
    private static Record getNodesOfColorCount(Transaction tx, int color) {
    	return tx.run("MATCH (n) WHERE n.Color=" + color + " RETURN count(n)").list().get(0);
    }
}
