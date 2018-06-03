package application.model.degree;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.FileCreator;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class DegreeCentralityThread implements Runnable{

	private boolean isIndegree;
	private boolean isOutdegree;
	private boolean isBoth;
	private Driver neo4jDriver;
	
	private ArrayList<NodeDegree> nodes;
	private ArrayList<String> saveParameters;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private double nodesCount;
	private int computedCount;
	
	public DegreeCentralityThread(
			Driver neo4jDriver,
			boolean isIndegree, 
			boolean isOutdegree, 
			boolean isBoth, 
			ProgressBar progress, 
			TextField progressPrompt) 
	{
		this.neo4jDriver = neo4jDriver;
		this.isIndegree = isIndegree;
		this.isOutdegree = isOutdegree;
		this.isBoth = isBoth;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		this.nodesCount = getNodesCount();
		
		nodes = new ArrayList<NodeDegree>();
		saveParameters = new ArrayList<String>();
	}
	
	@Override
	public void run() {
		progressPrompt.setText("Wykonywana operacja: POBIERANIE GRAFU");
		for(Record record : getNodesList()) {
			NodeDegree node = new NodeDegree(record.get(0).asInt());
			nodes.add(node);
			progress.setProgress((nodes.size()/nodesCount)/5);
		}
		
		progressPrompt.setText("Wykonywana operacja: OBLICZANIE");
		Runnable indegree = () -> {
			for(NodeDegree node : nodes) {
				node.setIndegree(getRelationsInCount(node.id()));
				progress.setProgress(0.2 + (computedCount++/nodesCount)/2.5);
			}
		};	
		Runnable outdegree = () -> {
			for(NodeDegree node : nodes) {
				node.setOutdegree(getRelationsOutCount(node.id()));
			}
		};
		Thread inThread = new Thread(indegree);
		Thread outThread = new Thread(outdegree);
		inThread.start();
		outThread.start();
		try {
			inThread.join();
			outThread.join();
		} catch (InterruptedException exc) {
			exc.printStackTrace();
		}		
		
		if(isIndegree)
			saveParameters.add("Indegree");			
		
		if(isOutdegree)
			saveParameters.add("Outdegree");		

		if(isBoth)
			saveParameters.add("Degree");
		
		progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");
		computedCount = 0;
		Transaction tx = neo4jDriver.session().beginTransaction();
		for(NodeDegree node : nodes) {
			Runnable save = () -> {
				for(String saveParameter : saveParameters) {
					insertNodeCentrality(tx, node, saveParameter);
				}
			};	
			progress.setProgress(0.6 + (computedCount++/nodesCount)/2.5);
			Thread saveThread = new Thread(save);
			saveThread.start();	
			try {
				saveThread.join();
			} catch (InterruptedException exc) {
				exc.printStackTrace();
			}
		}
		progress.setProgress(1);
		progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
		
		tx.success();
		tx.close();
	}
	
	public void algExecToFile(FileCreator algInfo) {
		algInfo.addLine("Iloœæ pobranych wierzcho³ków = " + nodesCount);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ pobranych relacji      = " + getRelationsCount());
    	algInfo.addEmptyLine();
    	
    	algInfo.addLine("Obliczane parametry :");
    	for(int i = 1; i <= saveParameters.size() ; i++) {
    		algInfo.addLine("	" + i + ". " + saveParameters.get(i-1));
    	}
    	algInfo.addEmptyLine();
    	for(int i = 0; i < saveParameters.size() ; i++) {
    		int maxDegree = 0;
    		int minDegree = Integer.MAX_VALUE;
    		
    		for(NodeDegree node : nodes) {
    			if(saveParameters.get(i).equals("Indegree")) {
    				if(node.indegree() > maxDegree) {
    					maxDegree = node.indegree();
    				}
    				if(node.indegree() < minDegree) {
    					minDegree = node.indegree();
    				}
    			}
    			if(saveParameters.get(i).equals("Outdegree")) {
    				if(node.outdegree() > maxDegree) {
    					maxDegree = node.indegree();
    				}
    				if(node.outdegree() < minDegree) {
    					minDegree = node.indegree();
    				}
    			}
    			if(saveParameters.get(i).equals("Degree")) {
    				if(node.indegree() + node.outdegree() > maxDegree) {
    					maxDegree = node.indegree();
    				}
    				if(node.indegree() + node.outdegree() < minDegree) {
    					minDegree = node.indegree();
    				}
    			}
    		}
    		
    		algInfo.addLine("Parametr : " + saveParameters.get(i));
    		algInfo.addEmptyLine();
    		algInfo.addLine("Maksymalna wartoœæ = " + maxDegree);
			for(Record record : getNodesByDegree(maxDegree,saveParameters.get(i))) {
				algInfo.addLine("ID: " + record.get(0).asInt() + "	Wierzcho³ek : " + record.get(1).asMap());
			}	
			algInfo.addEmptyLine();
			algInfo.addLine("Minimalna wartoœæ  = " + minDegree);
			for(Record record : getNodesByDegree(minDegree,saveParameters.get(i))) {
				algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
			}
			algInfo.addEmptyLine();
			algInfo.addEmptyLine();
    	}
    }
	
	private double getNodesCount(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(DegreeCentralityThread::getNodesNum).get(0).asDouble();
        }
    }
	
	private int getRelationsCount(){
        try ( Session session = neo4jDriver.session() ) {
            return session.readTransaction(DegreeCentralityThread::getRelationsNum).get(0).asInt();
        }
    }
	
	private List<Record> getNodesList(){
		try ( Session session = neo4jDriver.session() ) {
			return session.readTransaction(DegreeCentralityThread::getNodes);
	    }
	}

	private int getRelationsOutCount(long node){
		try ( Session session = neo4jDriver.session() ) {
	       Transaction tx = session.beginTransaction();
	       return Integer.parseInt(getRelationsOut(tx,node).get(0).toString());
	    }
	}
	
	private int getRelationsInCount(long node){
		try ( Session session = neo4jDriver.session() ) {
	       Transaction tx = session.beginTransaction();
	       return Integer.parseInt(getRelationsIn(tx,node).get(0).toString());
	    }
	}
	
	private List<Record> getNodesByDegree(int degree, String parameter) {
    	try ( Session session = neo4jDriver.session() ) {
    		Transaction tx = session.beginTransaction();
    		List<Record> records;
    		records = getNodesByDegree(tx, degree, parameter);
            tx.success();
            tx.close();
            return records;
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
		
	private static Record getRelationsOut(Transaction tx, long node){
		return tx.run(	"MATCH (n)-[r]->(p) " +
						"WHERE ID(n)=" + node + " " +
						"RETURN count(p)").list().get(0);
	}
	    
	private static Record getRelationsIn(Transaction tx, long node){
		return tx.run(	"MATCH (p)-[r]->(n) " +
						"WHERE ID(n)=" + node + " " +
						"RETURN count(p)").list().get(0);
	}

	private static List<Record> getNodes(Transaction tx){
		return tx.run(	"MATCH (n) " +
						"RETURN ID(n)").list();
	}
	
	private StatementResult insertNodeCentrality(Transaction tx, NodeDegree node, String saveParameter) {
		String query = "MATCH (n) " +
				"WHERE ID(n) = " + node.id() + " " +
				"SET n." + saveParameter + " = ";
		if(saveParameter.equals("Indegree"))
			return tx.run(query	+ node.indegree());
		if(saveParameter.equals("Outdegree"))
			return tx.run(query + node.outdegree());
		if(saveParameter.equals("Degree")) {
			int degree = node.indegree() + node.outdegree();
			return tx.run(query + degree);
		}
		return null;
    }
	
	private static List<Record> getNodesByDegree(Transaction tx, int degree, String parameter) {
   	 	return tx.run(	"MATCH (n) " +
   	 					"WHERE n." + parameter + "=" + degree + " " +
   	 					"RETURN ID(n),n").list();
   }
}
