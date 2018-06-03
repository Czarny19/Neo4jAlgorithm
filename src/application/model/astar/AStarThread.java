package application.model.astar;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import application.model.FileCreator;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStarThread implements Runnable{

    private Driver neo4jdriver;
    private String distanceKey;
    
    private HashMap<Integer,NodeAStar> nodes;
    private HashMap<String,RelationAStar> relations;

    private int startNodeId;
    private int endNodeId;
    
    private ProgressBar progress;
    private TextField progressPrompt;
    
    private AStar aStar;

    public AStarThread(Driver neo4jdriver, int StartId, int EndId, String distanceKey, ProgressBar progress, TextField progressPrompt) {
    	this.neo4jdriver = neo4jdriver;
        this.distanceKey = distanceKey;
        this.startNodeId = StartId;
        this.endNodeId = EndId;
        this.progress = progress;
        this.progressPrompt = progressPrompt;
    }
    
    public boolean startNodeExists() {
    	if(!getNodeById(startNodeId).isEmpty()) {
    		return true;
    	}
    	return false;
    }
    
    public boolean endNodeExists() {
    	if(!getNodeById(endNodeId).isEmpty()) {
    		return true;
    	}
    	return false;
    }
    
    private HashMap<Integer,NodeAStar> initNodes() {    
    	nodes = new HashMap<Integer,NodeAStar>();
    	for(Record record : getNodesList()) {
    		NodeAStar node = new NodeAStar(record.get(0).asInt(), new HashMap<>());
    		nodes.put(record.get(0).asInt(),node);
    	}
    	return nodes;
    }
    
    private HashMap<String, RelationAStar> initRelations() {
    	relations = new HashMap<String,RelationAStar>();
    	for(Record record : getRelationsList()) {
    		RelationAStar relation;
    		if(record.get(3).toString() == "NULL" || distanceKey.equals("Brak klucza")) {
    			relation = new RelationAStar(
        				record.get(1).asInt(),
        				record.get(0).asInt(),
        				record.get(2).asInt(),
        				1.0);	
    			if(!relations.containsKey(relation.nodeFrom() + " " + relation.nodeTo()))
    				relations.put(relation.nodeFrom() + " " + relation.nodeTo(),relation);
    		}
    		else {
    			relation = new RelationAStar(
        				record.get(1).asInt(),
        				record.get(0).asInt(),
        				record.get(2).asInt(),
        				record.get(3).asDouble());
    			if(relations.containsKey(relation.nodeFrom() + " " + relation.nodeTo())) {
        			if(relation.distance() < relations.get(relation.nodeFrom() + " " + relation.nodeTo()).distance()) {
        				relations.put(relation.nodeFrom() + " " + relation.nodeTo(),relation);   
        			}
        		}
        		else {
        			relations.put(relation.nodeFrom() + " " + relation.nodeTo(),relation);
        		}
    		}
    	}
    	return relations;
    }

    @Override
    public void run() {
    	HashMap<Integer, HashMap<Integer, Double>> heuristic = new HashMap<>();	
		
		progressPrompt.setText("Wykonywana operacja: POBIERANIE WIERZCHO£KÓW");
		initNodes();
		progress.setProgress(0.2);
		
		progressPrompt.setText("Wykonywana operacja: POBIERANIE RELACJI");		
		initRelations();
		progress.setProgress(0.4);
			
		GraphAStar graph = new GraphAStar();	
		
		double relationsCount = relations.size();
		double nodesCount = nodes.size();
		double localProgress = 0;
				
		progressPrompt.setText("Wykonywana operacja: WYSZUKIWANIE ŒCIE¯KI");
		for (Integer nodeFromid : nodes.keySet()) {
			HashMap<Integer, Double> map = new HashMap<Integer, Double>();
			for (Integer nodeToid : nodes.keySet()) {
				if(nodeFromid == nodeToid)
	                map.put(nodeToid, 0.0);
				else 
					map.put(nodeToid, 1.0);
			}
			progress.setProgress(0.4 + (localProgress++/nodesCount)/5);
			heuristic.put(nodeFromid, map);
		}
		
		graph.setHeuristic(heuristic);
		
		localProgress = 0;
		for (Integer nodeId : nodes.keySet()) {
			graph.addNode(nodeId);
			progress.setProgress(0.6+(localProgress++/nodesCount)/5);
		}
		
		localProgress = 0;		
		for (RelationAStar relation : relations.values()) {
			int nodeFromId = -1;
			int nodeToId = -1;
			
			for (int nodeId : nodes.keySet()) {
				if(nodeId == relation.nodeFrom())
					nodeFromId = nodeId;
				
				if(nodeId == relation.nodeTo())
					nodeToId = nodeId;
				
				if(nodeFromId != -1 && nodeToId != -1)
					graph.addEdge(nodeFromId, nodeToId, relation.distance());
			}
			
			progress.setProgress(0.8+(localProgress++/relationsCount)/5);
		}

		aStar = new AStar(graph, progress);
    }
    
    public void algExecToFile(FileCreator algInfo) {
    	ArrayList<String> path = new ArrayList<String>();
    	
    	algInfo.addEmptyLine();
    	algInfo.addLine("ID Wierzcho³ka pocz¹tkowego = " + startNodeId);
    	algInfo.addLine("ID wierzcho³ka koñcowego    = " + endNodeId);
    	algInfo.addEmptyLine();
    	algInfo.addLine("Œcie¿ka :");
    	try {
    		double distanceSum = 0;
    		
			for (Integer pathElement : aStar.compute(startNodeId , endNodeId)) {	
				path.add(pathElement.toString());
			}
			
			for(int i=0 ; i < path.size()-1 ; i++) {
				distanceSum += relations.get(path.get(i)+" "+path.get(i+1)).distance();
			}
			
			algInfo.addLine("--- Klucz odleg³oœci    = " + distanceKey);
			algInfo.addLine("--- Odleg³oœæ ca³kowita = " + distanceSum);
			algInfo.addEmptyLine();
			
			Transaction tx = neo4jdriver.session().beginTransaction();			
			for(int i=0 ; i < path.size() ; i++) {
				algInfo.addLine("ID: " + path.get(i) + "	Wierzcho³ek : " + getNodeById(Integer.parseInt(path.get(i))));
				if(i != (path.size()-1)) {
					int relationId = relations.get(path.get(i)+" "+path.get(i+1)).id();
					
					algInfo.addLine("		ID: " + relationId +
									" | " + path.get(i) + " --> " + path.get(i+1) + " | " +
									"Relacja : " + getRelationById(relationId));
				}
			}
			tx.success();
			tx.close();
		}catch (NullPointerException exc){
			algInfo.addLine("Brak œcie¿ki");
		}
    }
    
    public List<Record> getNodeById(int nodeId){
    	try ( Session session = neo4jdriver.session() ) {
 	       Transaction tx = session.beginTransaction();
 	       return getNode(tx, nodeId);
 	    }
    }
    
    public Map<String, Object> getRelationById(int relationId) {
    	try ( Session session = neo4jdriver.session() ) {
   	       Transaction tx = session.beginTransaction();
   	       return getRelation(tx, relationId);
   	    }
    }

    public List<Record> getNodesList(){
    	try ( Session session = neo4jdriver.session() ) {
 	       Transaction tx = session.beginTransaction();
 	       return getNodes(tx, startNodeId, endNodeId);
 	    }
    }

    public List<Record> getRelationsList(){
    	try ( Session session = neo4jdriver.session() ) {
  	       Transaction tx = session.beginTransaction();
  	       return getRelations(tx, startNodeId, distanceKey);
  	    }
    }
    
    private static List<Record> getNode(Transaction tx, int nodeId) {
    	return tx.run(	"MATCH (n) " +
    					"WHERE ID(n)=" + nodeId + " " +
    					"RETURN n").list();
    }
    
    private static Map<String, Object> getRelation(Transaction tx, int relationId) {
    	return tx.run(	"MATCH ()-[r]->() " +
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
}
