package application.model.astar;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import application.model.FileCreator;
import javafx.scene.control.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStarThread implements Runnable{

    private Driver neo4jdriver;
    private String distanceKey;
    
    private HashMap<Integer,NodeAStar> Nodes;
    private HashMap<String,RelationAStar> Relations;

    private int StartId;
    private int EndId;
    
    private ProgressBar progress;
    
    private AStar aStar;

    public AStarThread(Driver neo4jdriver, int StartId, int EndId, String distanceKey, ProgressBar progress) {
    	this.neo4jdriver = neo4jdriver;
        this.distanceKey = distanceKey;
        this.StartId = StartId;
        this.EndId = EndId;
        this.progress = progress;
    }
    
    public boolean startExists() {
    	if(getNodeById(StartId).size() == 1) {
    		return true;
    	}
    	return false;
    }
    
    public boolean endExists() {
    	if(getNodeById(EndId).size() == 1) {
    		return true;
    	}
    	return false;
    }
    
    private HashMap<Integer,NodeAStar> initNodes() {    
    	Nodes = new HashMap<Integer,NodeAStar>();
    	for(Record record : getNodesList()) {
    		NodeAStar node = new NodeAStar(record.get(0).asInt(), new HashMap<>());
    		Nodes.put(record.get(0).asInt(),node);
    	}
    	return Nodes;
    }
    
    private HashMap<String, RelationAStar> initRelations() {
    	Relations = new HashMap<String,RelationAStar>();
    	for(Record record : getRelationsList()) {
    		RelationAStar relation;
    		if(record.get(3).toString() == "NULL" || distanceKey.equals("Brak klucza")) {
    			relation = new RelationAStar(
        				record.get(1).asInt(),
        				record.get(0).asInt(),
        				record.get(2).asInt(),
        				1.0);	
    			if(!Relations.containsKey(relation.nodeFrom()+" "+relation.nodeTo()))
    				Relations.put(relation.nodeFrom()+" "+relation.nodeTo(),relation);
    		}
    		else {
    			relation = new RelationAStar(
        				record.get(1).asInt(),
        				record.get(0).asInt(),
        				record.get(2).asInt(),
        				record.get(3).asDouble());
    			if(Relations.containsKey(relation.nodeFrom()+" "+relation.nodeTo())) {
        			if(relation.distance() < Relations.get(relation.nodeFrom()+" "+relation.nodeTo()).distance()) {
        				Relations.put(relation.nodeFrom()+" "+relation.nodeTo(),relation);   
        			}
        		}
        		else {
        			Relations.put(relation.nodeFrom()+" "+relation.nodeTo(),relation);
        		}
    		}
    	}

    	return Relations;
    }

    @Override
    public void run() {
    	HashMap<Integer, HashMap<Integer, Double>> heuristic = new HashMap<>();	
		Nodes = initNodes();
		progress.setProgress(0.2);
		Relations = initRelations();
		progress.setProgress(0.4);
			
		GraphAStar graph = new GraphAStar();	
		
		double localProg = 0;
		double arraySize = Nodes.size();
		for (Integer idFrom : Nodes.keySet()) {
			HashMap<Integer, Double> map = new HashMap<Integer, Double>();
			for (Integer idTo : Nodes.keySet()) {
				if(idFrom == idTo)
	                map.put(idTo, 0.0);
				else 
					map.put(idTo, 1.0);
			}
			localProg += 1;
			progress.setProgress(progress.getProgress()+(localProg/(arraySize*500)));
			heuristic.put(idFrom, map);
		}
		
		graph.setHeuristic(heuristic);
		
		localProg = 0;
		for (Integer id : Nodes.keySet()) {
			graph.addNode(id);
			localProg += 1;
			progress.setProgress(progress.getProgress()+(localProg/(arraySize*500)));
		}
		
		localProg = 0;
		arraySize = Relations.size();
		for (RelationAStar relation : Relations.values()) {
			int nodeFromId = -1;
			int nodeToId = -1;
			
			for (int id : Nodes.keySet()) {
				if(id == relation.nodeFrom())
					nodeFromId = id;
				
				if(id == relation.nodeTo())
					nodeToId = id;
				
				if(nodeFromId != -1 && nodeToId != -1)
					graph.addEdge(nodeFromId, nodeToId, relation.distance());
			}
			
			localProg += 1;
			progress.setProgress(progress.getProgress()+(localProg/(arraySize*500)));
		}

		aStar = new AStar(graph, progress);
    }
    
    public void algExecToFile(FileCreator algInfo) {
    	ArrayList<String> Path = new ArrayList<String>();
    	
    	algInfo.addLine("");
    	algInfo.addLine("ID Wierzcho³ka pocz¹tkowego = " + StartId);
    	algInfo.addLine("ID wierzcho³ka koñcowego    = " + EndId);
    	algInfo.addLine("");
    	algInfo.addLine("Œcie¿ka :");
    	try {
    		double distanceSum = 0;
			for (Integer pathElement : aStar.astar(StartId , EndId)) {	
				Path.add(pathElement.toString());
			}
			for(int i=0 ; i < Path.size()-1 ; i++) {
				distanceSum += Relations.get(Path.get(i)+" "+Path.get(i+1)).distance();
			}
			algInfo.addLine("--- Klucz odleg³oœci    = " + distanceKey);
			algInfo.addLine("--- Odleg³oœæ ca³kowita = " + distanceSum);
			Transaction tx = neo4jdriver.session().beginTransaction();
			
			for(int i=0 ; i < Path.size() ; i++) {
				algInfo.addLine("ID: " + Path.get(i) + 
						"  Wierzcho³ek Info: " + getNodeData(tx,Path.get(i)));
				if(i != (Path.size()-1)) {
					int relationId = Relations.get(Path.get(i)+" "+Path.get(i+1)).id();
					algInfo.addLine("		ID: " + relationId + " Wierzcho³ki relacji: " +  
							Path.get(i)+" --> "+Path.get(i+1) + 
							" Relacja Info: " +
							getRelationData(relationId));
				}
			}
			tx.success();
			tx.close();
		}catch (NullPointerException e){
			algInfo.addLine("Brak œcie¿ki");
		}
    }

    public Record getGraphSize(){
        try ( Session session = neo4jdriver.session() ) {
            return session.readTransaction(AStarThread::getSize);
        }
    }
    
    public List<Record> getNodeById(int nodeId){
    	try ( Session session = neo4jdriver.session() ) {
 	       Transaction tx = session.beginTransaction();
 	       return getNode(tx, nodeId);
 	    }
    }
    
    public Map<String, Object> getNodeData(String nodeId) {
    	try ( Session session = neo4jdriver.session() ) {
  	       Transaction tx = session.beginTransaction();
  	       return getNodeData(tx, nodeId);
  	    }
    }
    
    public Map<String, Object> getRelationData(int relationId) {
    	try ( Session session = neo4jdriver.session() ) {
   	       Transaction tx = session.beginTransaction();
   	       return getRelationData(tx, relationId);
   	    }
    }

    public List<Record> getNodesList(){
    	try ( Session session = neo4jdriver.session() ) {
 	       Transaction tx = session.beginTransaction();
 	       return getNodes(tx, StartId, EndId);
 	    }
    }

    public List<Record> getRelationsList(){
    	try ( Session session = neo4jdriver.session() ) {
  	       Transaction tx = session.beginTransaction();
  	       return getRelations(tx, StartId, distanceKey);
  	    }
    }
    
    private static List<Record> getNode(Transaction tx, int nodeId) {
    	return tx.run("MATCH (node) " +
    			"WHERE ID(node)=" + nodeId + " " +
    			"RETURN node").list();
    }
    
    private static Map<String, Object> getNodeData(Transaction tx, String nodeId) {
    	return tx.run("MATCH (n) " +
    			"WHERE ID(n)=" + nodeId + " " +
    			"RETURN n").single().get(0).asMap();
    }
    
    private static Map<String, Object> getRelationData(Transaction tx, int relationId) {
    	return tx.run("MATCH ()-[r]->() " +
    			"WHERE ID(r)=" + relationId + " " +
    			"RETURN r").single().get(0).asMap();
    }
    
    private static List<Record> getRelations(Transaction tx, int StartId, String distanceKey)
    {
        List<Record> result;
        List<Record> finalResult;
        int index = 1;
        int prevSize = 0;

        String matchExpand = "(n0)-[r0]->(n)-[r]->(p)";

        if(distanceKey.equals("Brak klucza")) {
        	finalResult = tx.run("MATCH (n)-[r]->(p) WHERE ID(n)="
                + StartId + " RETURN distinct ID(n),ID(r),ID(p)").list();
        }
        else {
        	finalResult = tx.run("MATCH (n)-[r]->(p) WHERE ID(n)="
                    + StartId + " RETURN distinct ID(n),ID(r),ID(p),r." + distanceKey).list();
        }

        while (finalResult.size() != prevSize){
            prevSize = finalResult.size();

            if(distanceKey.equals("Brak klucza")) {
            	result = tx.run("MATCH" + matchExpand + " WHERE ID(n0)="
                    + StartId + " RETURN distinct ID(n),ID(r),ID(p)").list();
            }
            else {
            	result = tx.run("MATCH" + matchExpand + " WHERE ID(n0)="
                        + StartId + " RETURN distinct ID(n),ID(r),ID(p),r." + distanceKey).list();
            }

            for (Record aResult1 : result) {
                if (!finalResult.contains(aResult1)) {
                    finalResult.add(aResult1);
                }
            }

            matchExpand = matchExpand.substring(0,matchExpand.length()-12);
            matchExpand += "(p" + index + ")-[r" + index + "]->(n)-[r]->(p)";
            index++;
        }

        return finalResult;
    }

    private static List<Record> getNodes(Transaction tx, int StartId, int EndId){
        List<Record> result;
        List<Record> finalResult;

        int prevSize = 0;
        String matchExpand = "-[]->(n)";
        int index = 1;
        finalResult = tx.run("MATCH (n) WHERE ID(n)=" + StartId + " OR ID(n)=" + EndId + " RETURN ID(n)").list();
        while(finalResult.size() != prevSize) {
            prevSize = finalResult.size();

            result = tx.run("MATCH (a0)" + matchExpand + "WHERE ID(a0)=" + StartId +
                    " RETURN distinct ID(n)").list();

            for (Record aResult1 : result) {
                if (!finalResult.contains(aResult1)) {
                    finalResult.add(aResult1);

                }
            }
            index++;
            matchExpand = matchExpand.substring(0, matchExpand.length() - 8);
            matchExpand += "-[]->(a" + index + ")" + "-[]->(n)";
        }
        return finalResult;
    }

    private static Record getSize(Transaction tx){
        return tx.run("start n=node(*) match (n) return count(n)").list().get(0);
    }
}
