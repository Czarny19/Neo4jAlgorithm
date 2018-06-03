package application.model.astar;

import java.util.*;

public class GraphAStar {

    private final HashMap<Integer, HashMap<Integer, Double>> graph;
    private HashMap<Integer, HashMap<Integer, Double>> heuristicMap;
    private final HashMap<Integer, NodeAStar> nodeData;

    public GraphAStar() {
        graph = new HashMap<Integer, HashMap<Integer, Double>>();
        nodeData = new HashMap<Integer, NodeAStar>();
    }
    
    public void setHeuristic(HashMap<Integer, HashMap<Integer, Double>> heuristicMap) {
    	this.heuristicMap = heuristicMap;
    }

    public void addNode(int nodeId) {
        graph.put(nodeId, new HashMap<Integer,Double>());
        NodeAStar node = new NodeAStar(nodeId, heuristicMap.get(nodeId));
        nodeData.put(nodeId, node);
    }

    void addEdge(int nodeIdFirst, int nodeIdSecond, double distance) {
        this.graph.get(nodeIdFirst).put(nodeIdSecond, distance);
        this.graph.get(nodeIdSecond).put(nodeIdFirst, distance);
    }

    public HashMap<Integer, Double> edgesFrom(int nodeId) {
        return this.graph.get(nodeId);
    }

    public NodeAStar getNodeData(int nodeId) {
        return nodeData.get(nodeId);
    }
}
