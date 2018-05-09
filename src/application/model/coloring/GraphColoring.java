package application.model.coloring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.Node;

public class GraphColoring {

	private Driver driver;
	private List<Node> Vertexes;
	
	public GraphColoring(Driver driver){
		this.driver = driver;
		Vertexes = new ArrayList<Node>();
		for(Record record : getNodesList()) {
			Node node = new Node(Integer.parseInt(record.get(0).toString()));
			node.coloring();
			for(Record relationRecord : getRelationsList(node.id())) {
				node.neighbors().add(Integer.parseInt(relationRecord.get(0).toString()));
			}
			Vertexes.add(node);
		}	
	}
	
	public void colourVertices(){
		Collections.sort(Vertexes, new VertexComparator());
		for(int i = 0; i < Vertexes.size(); i++) {
			for(int j = 0; j < Vertexes.size();) {
				if(Vertexes.get(i).neighbors().contains(Vertexes.get(j).id())){
					if(Vertexes.get(j).color() == Vertexes.get(i).color())
						Vertexes.get(j).setColor(Vertexes.get(j).color()+1);	
					else
						j+=1;
				}
				else
					j+=1;
			}
		}
		
		Transaction transaction = driver.session().beginTransaction();		
		for(Node vertex : Vertexes) {
			insertNodeColor(transaction,vertex.id(),vertex.color());
		}
		transaction.success();
		transaction.close();				
	}
	
//	private Record getGraphSize(){
//        try ( Session session = driver.session() ) {
//            return session.readTransaction(GraphColoring::getSize);
//        }
//    }

    private List<Record> getNodesList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(GraphColoring::getNodes);
        }
    }

    private ArrayList<Record> getRelationsList(long node){
        try ( Session session = driver.session() ) {
        	Transaction tx = session.beginTransaction();
        	ArrayList<Record> RelationsList = new ArrayList<Record>(); 
        	RelationsList.addAll(getRelationsTo(tx,node));
        	RelationsList.addAll(getRelationsFrom(tx,node));
            return RelationsList;
        }
    }
	
    private static List<Record> getRelationsTo(Transaction tx, long node)
    {
    	return tx.run("MATCH (n)-[r]->(p) where ID(n)=" + node + " RETURN distinct ID(p)").list();
    }
    
    private static List<Record> getRelationsFrom(Transaction tx, long node)
    {
    	return tx.run("MATCH (p)-[r]->(n) where ID(n)=" + node + " RETURN distinct ID(p)").list();
    }

    private static List<Record> getNodes(Transaction tx){
        return tx.run("MATCH (n) RETURN ID(n) LIMIT 25").list();
    }

//    private static Record getSize(Transaction tx){
//        return tx.run("start n=node(*) match (n) return count(n)").list().get(0);
//    }
    
    private StatementResult insertNodeColor(Transaction tx, long node, int color) {
    	return tx.run("MATCH (n) WHERE ID(n) = " + node + " SET n.Color = " + color);
    }
}
