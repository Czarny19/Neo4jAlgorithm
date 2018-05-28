package application.model.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import javafx.scene.control.ProgressBar;

public class AStar{

    private GraphAStar graph;
    
    private ProgressBar progress;
	
	public AStar(GraphAStar graph, ProgressBar progress) {
		this.graph = graph;
		this.progress = progress;
	} 

    List<Integer> astar(int start, int end) {
    	//Java docs
    	//Creates a PriorityQueue with the default initial capacity (11),
    	//that orders its elements according to their natural ordering
        final Queue<NodeAStar> openQueue = new PriorityQueue<>(11, new NodeComparator());

        NodeAStar sourceNodeData = graph.getNodeData(start);
        sourceNodeData.setG(0);
        sourceNodeData.calcF(end);
        openQueue.add(sourceNodeData);

        final Map<Integer, Integer> path = new HashMap<>();
        final Set<NodeAStar> closedList = new HashSet<>();
        while (!openQueue.isEmpty()) {
            final NodeAStar nodeData = openQueue.poll();
            if (nodeData.id() == end) {
            	progress.setProgress(1);
                return path(path, end);
            }
            
            closedList.add(nodeData);
            graph.edgesFrom(nodeData.id()).forEach((node,distance) -> {
            	NodeAStar neighbor = graph.getNodeData(node);

                double distanceBetweenTwoNodes = distance;
                double tentativeG = distanceBetweenTwoNodes + nodeData.G();

                if (tentativeG < neighbor.G()) {
                    neighbor.setG(tentativeG);
                    neighbor.calcF(end);
 
                    path.put(neighbor.id(), nodeData.id());
                    if (!openQueue.contains(neighbor)) {
                        openQueue.add(neighbor);
                    }
                }
    		});
        }

        return null;
    }

    private List<Integer> path(Map<Integer, Integer> path, int destination) {
        assert path != null;

        final List<Integer> pathList = new ArrayList<>();
        pathList.add(destination);
        while (path.containsKey(destination)) {
            destination = path.get(destination);
            pathList.add(destination);
        }
        Collections.reverse(pathList);
        return pathList;
    }
}
