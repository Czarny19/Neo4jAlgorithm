package application.model.coloring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import application.model.FileCreator;

public class GraphColoring implements Runnable{

	private Driver noe4jdriver;
	private ArrayList<NodeGC> Nodes;
	
	public GraphColoring(Driver neo4jdriver){
		this.noe4jdriver = neo4jdriver;
		Nodes = new ArrayList<NodeGC>();
		for(Record record : getNodesList()) {
			NodeGC node = new NodeGC(record.get(0).asInt());
			for(Record relationRecord : getRelationsList(node.id())) {
				node.neighbors().add(relationRecord.get(0).asInt());
			}
			Nodes.add(node);
		}	
	}
	
	@Override
	public void run(){
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
		}
		
		Transaction transaction = noe4jdriver.session().beginTransaction();		
		for(NodeGC vertex : Nodes) {
			insertNodeColor(transaction,vertex.id(),vertex.color());
		}
		transaction.success();
		transaction.close();				
	}
	
	public void algExecToFile(FileCreator algInfo) {
    	algInfo.addLine("test");
    }
	
//	private Record getGraphSize(){
//        try ( Session session = driver.session() ) {
//            return session.readTransaction(GraphColoring::getSize);
//        }
//    }

    private List<Record> getNodesList(){
        try ( Session session = noe4jdriver.session() ) {
            return session.readTransaction(GraphColoring::getNodes);
        }
    }

    private ArrayList<Record> getRelationsList(long node){
        try ( Session session = noe4jdriver.session() ) {
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
