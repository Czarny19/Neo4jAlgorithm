package application.model;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import java.util.*;

public class Algorithm_A_star<T>{

	private Driver driver;
    private GraphAStar<T> graph;
	
	public Algorithm_A_star(Driver driver, GraphAStar<T> graph) {

		this.driver = driver;
		this.graph = graph;
	}

    public class NodeComparator implements Comparator<NodeData<T>> {
        public int compare(NodeData<T> nodeFirst, NodeData<T> nodeSecond) {
            return Double.compare(nodeFirst.getF(), nodeSecond.getF());
        }
    }

    public List<Record> getNodesList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(Algorithm_A_star::getNodes);
        }
    }

    public List<Record> getRelationsList(){
        try ( Session session = driver.session() ) {
            return session.readTransaction(Algorithm_A_star::getRelations);
        }
    }

    List<T> astar(T source, T destination) {
        final Queue<NodeData<T>> openQueue = new PriorityQueue<>(11, new NodeComparator());

        NodeData<T> sourceNodeData = graph.getNodeData(source);
        sourceNodeData.setG(0);
        sourceNodeData.calcF(destination);
        openQueue.add(sourceNodeData);

        final Map<T, T> path = new HashMap<>();
        final Set<NodeData<T>> closedList = new HashSet<>();

        while (!openQueue.isEmpty()) {
            final NodeData<T> nodeData = openQueue.poll();

            if (nodeData.getNodeId().equals(destination)) {
                return path(path, destination);
            }

            closedList.add(nodeData);

            for (Map.Entry<NodeData<T>, Double> neighborEntry : graph.edgesFrom(nodeData.getNodeId()).entrySet()) {
                NodeData<T> neighbor = neighborEntry.getKey();

                if (closedList.contains(neighbor)) continue;

                double distanceBetweenTwoNodes = neighborEntry.getValue();
                double tentativeG = distanceBetweenTwoNodes + nodeData.getG();

                if (tentativeG < neighbor.getG()) {
                    neighbor.setG(tentativeG);
                    neighbor.calcF(destination);

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
    //Zmienić pobieranie dla dużej bazy tak żeby dało się wykonać
    private static List<Record> getRelations(Transaction tx )
    {
        return tx.run( "MATCH p=()-->() RETURN p LIMIT 2000" ).list();
    }

    private static List<Record> getNodes(Transaction tx){
        return tx.run( "MATCH (n) RETURN ID(n) LIMIT 2000" ).list();
    }
}
