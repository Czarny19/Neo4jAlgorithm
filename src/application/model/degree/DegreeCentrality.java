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

public class DegreeCentrality implements Runnable{

	private boolean isIndegree;
	private boolean isOutdegree;
	private boolean isBoth;
	private Driver neo4jdriver;
	
	private ArrayList<NodeDegree> Nodes;
	private ArrayList<String> SaveParameters;
	
	private ProgressBar progress;
	private TextField progressPrompt;
	
	private double nodesCount;
	private int count;
	
	public DegreeCentrality(Driver neo4jdriver, boolean isIndegree, boolean isOutdegree, boolean isBoth, ProgressBar progress, TextField progressPrompt) {
		this.neo4jdriver = neo4jdriver;
		this.isIndegree = isIndegree;
		this.isOutdegree = isOutdegree;
		this.isBoth = isBoth;
		this.progress = progress;
		this.progressPrompt = progressPrompt;
		this.nodesCount = getNodesCount();
		
		Nodes = new ArrayList<NodeDegree>();
		SaveParameters = new ArrayList<String>();
	}
	
	@Override
	public void run() {
		progressPrompt.setText("Wykonywana operacja: POBIERANIE GRAFU");
		for(Record record : getNodesList()) {
			NodeDegree node = new NodeDegree(record.get(0).asInt());
			Nodes.add(node);
			progress.setProgress((Nodes.size()/nodesCount)/5);
		}
		
		progressPrompt.setText("Wykonywana operacja: OBLICZANIE");
		Runnable indegree = () -> {
			for(NodeDegree node : Nodes) {
				node.setIndegree(getRelationsInCount(node.id()));
				progress.setProgress(0.2 + (count++/nodesCount)/2.5);
			}
		};	
		Runnable outdegree = () -> {
			for(NodeDegree node : Nodes) {
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
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}		
		
		if(isIndegree)
			SaveParameters.add("Indegree");			
		
		if(isOutdegree)
			SaveParameters.add("Outdegree");		

		if(isBoth)
			SaveParameters.add("Degree");
		
		progressPrompt.setText("Wykonywana operacja: ZAPISYWANIE WYNIKÓW");
		count = 0;
		Transaction transaction = neo4jdriver.session().beginTransaction();
		for(NodeDegree node : Nodes) {
			Runnable save = () -> {
				for(String saveParameter : SaveParameters) {
					insertNodeCentrality(transaction, node, saveParameter);
				}
			};	
			progress.setProgress(0.6 + (count++/nodesCount)/2.5);
			Thread saveThread = new Thread(save);
			saveThread.start();	
			try {
				saveThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		progress.setProgress(1);
		progressPrompt.setText("Wykonywana operacja: FINALIZOWANIE TRANSAKCJI");
		
		transaction.success();
		transaction.close();
	}
	
	public void algExecToFile(FileCreator algInfo) {
		algInfo.addLine("Iloœæ pobranych wierzcho³ków = " + nodesCount);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Iloœæ pobranych relacji      = " + getRelationsCount());
    	algInfo.addEmptyLine();
    	
    	algInfo.addLine("Obliczane parametry :");
    	for(int i = 1; i <= SaveParameters.size() ; i++) {
    		algInfo.addLine("	" + i + ". " + SaveParameters.get(i-1));
    	}
    	algInfo.addEmptyLine();
    	for(int i = 0; i < SaveParameters.size() ; i++) {
    		int maxDegree = 0;
    		int minDegree = Integer.MAX_VALUE;
    		
    		for(NodeDegree node : Nodes) {
    			if(SaveParameters.get(i).equals("Indegree")) {
    				if(node.indegree() > maxDegree) {
    					maxDegree = node.indegree();
    				}
    				if(node.indegree() < minDegree) {
    					minDegree = node.indegree();
    				}
    			}
    			if(SaveParameters.get(i).equals("Outdegree")) {
    				if(node.outdegree() > maxDegree) {
    					maxDegree = node.indegree();
    				}
    				if(node.outdegree() < minDegree) {
    					minDegree = node.indegree();
    				}
    			}
    			if(SaveParameters.get(i).equals("Degree")) {
    				if(node.indegree() + node.outdegree() > maxDegree) {
    					maxDegree = node.indegree();
    				}
    				if(node.indegree() + node.outdegree() < minDegree) {
    					minDegree = node.indegree();
    				}
    			}
    		}
    		
    		algInfo.addLine("Parametr : " + SaveParameters.get(i));
    		algInfo.addEmptyLine();
    		algInfo.addLine("Maksymalna wartoœæ = " + maxDegree);
			for(Record record : getNodesByDegree(maxDegree,SaveParameters.get(i))) {
				algInfo.addLine("ID: " + record.get(0).asInt() + "	Wierzcho³ek : " + record.get(1).asMap());
			}	
			algInfo.addEmptyLine();
			algInfo.addLine("Minimalna wartoœæ  = " + minDegree);
			for(Record record : getNodesByDegree(minDegree,SaveParameters.get(i))) {
				algInfo.addLine("ID: " + record.get(0).asInt() +"	Wierzcho³ek : " + record.get(1).asMap());
			}
			algInfo.addEmptyLine();
			algInfo.addEmptyLine();
    	}
    }
	
	private double getNodesCount(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(DegreeCentrality::getNodesNum).get(0).asDouble();
        }
    }
	
	private int getRelationsCount(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(DegreeCentrality::getRelationsNum).get(0).asInt();
        }
    }
	
	private List<Record> getNodesList(){
		try ( Session session = neo4jdriver.session() ) {
			return session.readTransaction(DegreeCentrality::getNodes);
	    }
	}

	private int getRelationsOutCount(long node){
		try ( Session session = neo4jdriver.session() ) {
	       Transaction tx = session.beginTransaction();
	       return Integer.parseInt(getRelationsOut(tx,node).get(0).toString());
	    }
	}
	
	private int getRelationsInCount(long node){
		try ( Session session = neo4jdriver.session() ) {
	       Transaction tx = session.beginTransaction();
	       return Integer.parseInt(getRelationsIn(tx,node).get(0).toString());
	    }
	}
	
	private List<Record> getNodesByDegree(int degree, String parameter) {
    	try ( Session session = neo4jdriver.session() ) {
    		Transaction tx = session.beginTransaction();
    		List<Record> records;
    		records = getNodesByDegree(tx, degree, parameter);
            tx.success();
            tx.close();
            return records;
        }
    }
	
	private static Record getNodesNum(Transaction tx){
        return tx.run("MATCH (n) RETURN COUNT(n)").list().get(0);
    }
	
	private static Record getRelationsNum(Transaction tx){
        return tx.run("MATCH ()-[r]->() RETURN COUNT(r)").list().get(0);
    }
		
	private static Record getRelationsOut(Transaction tx, long node){
		return tx.run("MATCH (n)-[r]->(p) where ID(n)=" + node + " RETURN count(p)").list().get(0);
	}
	    
	private static Record getRelationsIn(Transaction tx, long node){
		return tx.run("MATCH (p)-[r]->(n) where ID(n)=" + node + " RETURN count(p)").list().get(0);
	}

	private static List<Record> getNodes(Transaction tx){
		return tx.run("MATCH (n) RETURN ID(n)").list();
	}
	
	private StatementResult insertNodeCentrality(Transaction tx, NodeDegree node, String saveParameter) {
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
	
	private static List<Record> getNodesByDegree(Transaction tx, int degree, String parameter) {
   	 return tx.run("MATCH (n) WHERE n." + parameter + "=" + degree + " RETURN ID(n),n").list();
   }
}
