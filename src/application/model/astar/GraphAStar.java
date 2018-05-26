package application.model.astar;

import java.util.*;

public class GraphAStar {

    private final HashMap<Integer, HashMap<Integer, Double>> Graph;
    private HashMap<Integer, HashMap<Integer, Double>> HeuristicMap;
    private final HashMap<Integer, NodeAStar> NodeIdNodeData;

    public GraphAStar() {
        Graph = new HashMap<Integer, HashMap<Integer, Double>>();
        NodeIdNodeData = new HashMap<Integer, NodeAStar>();
    }
    
    public void setHeuristic(HashMap<Integer, HashMap<Integer, Double>> HeuristicMap) {
    	this.HeuristicMap = HeuristicMap;
    }

    public void addNode(int nodeId) {
        Graph.put(nodeId, new HashMap<Integer,Double>());
        NodeAStar node = new NodeAStar(nodeId, HeuristicMap.get(nodeId));
        NodeIdNodeData.put(nodeId, node);
    }

    void addEdge(int nodeIdFirst, int nodeIdSecond, double distance) {
        this.Graph.get(nodeIdFirst).put(nodeIdSecond, distance);
        this.Graph.get(nodeIdSecond).put(nodeIdFirst, distance);
    }

    public HashMap<Integer, Double> edgesFrom(int nodeId) {
        return this.Graph.get(nodeId);
    }

    public NodeAStar getNodeData(int nodeId) {
        return NodeIdNodeData.get(nodeId);
    }
}
