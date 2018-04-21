package application.model.astar;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import java.util.*;

public class AStar<T>{

	private Driver driver;
    private GraphAStar<T> graph;
    private static int StartId;
    private static int EndId;
	
	public AStar(Driver driver, GraphAStar<T> graph, int StartId, int EndId) {
		this.driver = driver;
		this.graph = graph;
		AStar.StartId = StartId;
		AStar.EndId = EndId;
	}

    public class NodeComparator implements Comparator<NodeData<T>> {
        public int compare(NodeData<T> nodeFirst, NodeData<T> nodeSecond) {
            return Double.compare(nodeFirst.F(), nodeSecond.F());
        }
    }

    public Record getGraphSize(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(AStar::getSize);
        }
    }

    public List<Record> getNodesList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(AStar::getNodes);
        }
    }

    public List<Record> getRelationsList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(AStar::getRelations);
        }
    }

    List<T> astar(T start, T end) {
    	//Java docs
    	//Creates a PriorityQueue with the default initial capacity (11),
    	//that orders its elements according to their natural ordering
        final Queue<NodeData<T>> openQueue = new PriorityQueue<>(11, new NodeComparator());

        NodeData<T> sourceNodeData = graph.getNodeData(start);
        sourceNodeData.setG(0);
        sourceNodeData.calcF(end);
        openQueue.add(sourceNodeData);

        final Map<T, T> path = new HashMap<>();
        final Set<NodeData<T>> closedList = new HashSet<>();

        while (!openQueue.isEmpty()) {
            final NodeData<T> nodeData = openQueue.poll();

            if (nodeData.getNodeId().equals(end)) {
                return path(path, end);
            }

            closedList.add(nodeData);

            for (Map.Entry<NodeData<T>, Double> neighborEntry : graph.edgesFrom(nodeData.getNodeId()).entrySet()) {
                NodeData<T> neighbor = neighborEntry.getKey();

                if (closedList.contains(neighbor)) continue;

                double distanceBetweenTwoNodes = neighborEntry.getValue();
                double tentativeG = distanceBetweenTwoNodes + nodeData.G();

                if (tentativeG < neighbor.G()) {
                    neighbor.setG(tentativeG);
                    neighbor.calcF(end);

                    path.put(neighbor.getNodeId(), nodeData.getNodeId());
                    if (!openQueue.contains(neighbor)) {
                        openQueue.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

    private List<T> path(Map<T, T> path, T destination) {
        assert path != null;
        assert destination != null;

        final List<T> pathList = new ArrayList<>();
        pathList.add(destination);
        while (path.containsKey(destination)) {
            destination = path.get(destination);
            pathList.add(destination);
        }
        Collections.reverse(pathList);
        return pathList;
    }

    private static List<Record> getRelations(Transaction tx)
    {
        List<Record> result;
        List<Record> finalResult;
        int index = 1;
        int prevSize = 0;

        String matchExpand = "(n0)-[r0]->(n)-[r]->(p)";

        finalResult = tx.run("MATCH (n)-[r]->(p) WHERE ID(n)="
                + StartId + " RETURN distinct ID(n),ID(r),ID(p)").list();

        while (finalResult.size() != prevSize){
            prevSize = finalResult.size();

            result = tx.run("MATCH" + matchExpand + " WHERE ID(n0)="
                    + StartId + " RETURN distinct ID(n),ID(r),ID(p)").list();

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

    private static List<Record> getNodes(Transaction tx){
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
