package application.model.astar;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import javafx.scene.control.ProgressBar;

import java.util.HashMap;
import java.util.List;

public class AStarThread implements Runnable{

    private Driver neo4jdriver;
    private String distanceKey;
    
    private HashMap<Integer,NodeAStar> Nodes;
    private HashMap<String,RelationAStar> Relations;

    private int StartId;
    private int EndId;
    
    private ProgressBar progress;

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
    		if(record.get(3).toString() == "NULL") {
    			relation = new RelationAStar(
        				record.get(1).asInt(),
        				record.get(0).asInt(),
        				record.get(2).asInt(),
        				1.0);		
    		}
    		else {
    			relation = new RelationAStar(
        				record.get(1).asInt(),
        				record.get(0).asInt(),
        				record.get(2).asInt(),
        				record.get(3).asDouble());
    		}
    		Relations.put(relation.nodeFrom()+" "+relation.nodeTo(),relation);   	
    	}

    	return Relations;
    }

    @Override
    public void run() {
    	HashMap<Integer, HashMap<Integer, Double>> heuristic = new HashMap<>();	
		Nodes = initNodes();
		Relations = initRelations();
			
		GraphAStar graph = new GraphAStar();	
		
		Nodes.forEach((idFrom,nodeFrom) -> {
			HashMap<Integer, Double> map = new HashMap<Integer, Double>();
			Nodes.forEach((idTo,nodeTo) -> {
				if(idFrom == idTo)
	                map.put(idTo, 0.0);
				else 
					map.put(idTo, 1.0);
			});
			heuristic.put(idFrom, map);
		});
		graph.setHeuristic(heuristic);
		
		Nodes.forEach((id,node) -> {
			graph.addNode(id);
		});	
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
		}

		AStar aStar = new AStar(graph);
		try {
			for (Integer path : aStar.astar(StartId , EndId)) {	
				System.out.println("path = " + path);
			}
		}catch (NullPointerException e){
	        System.out.println("Brak œcie¿ki");
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
    
    private static List<Record> getRelations(Transaction tx, int StartId, String distanceKey)
    {
        List<Record> result;
        List<Record> finalResult;
        int index = 1;
        int prevSize = 0;

        String matchExpand = "(n0)-[r0]->(n)-[r]->(p)";

        finalResult = tx.run("MATCH (n)-[r]->(p) WHERE ID(n)="
                + StartId + " RETURN distinct ID(n),ID(r),ID(p),r." + distanceKey).list();

        while (finalResult.size() != prevSize){
            prevSize = finalResult.size();

            result = tx.run("MATCH" + matchExpand + " WHERE ID(n0)="
                    + StartId + " RETURN distinct ID(n),ID(r),ID(p),r." + distanceKey).list();

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
