package application.model;

import java.util.*;

public class GraphAStar<T> implements Iterable<T> {

    private final Map<T, Map<NodeData<T>, Double>> graph;
    private Map<T, Map<T, Double>> heuristicMap;
    private final Map<T, NodeData<T>> nodeIdNodeData;

    public GraphAStar(Map<T, Map<T, Double>> heuristicMap) {
        if (heuristicMap == null) throw new NullPointerException("The huerisic map should not be null");
        graph = new HashMap<>();
        nodeIdNodeData = new HashMap<>();
        this.heuristicMap = heuristicMap;
    }

    void addNode(T nodeId) {
        if (nodeId == null) throw new NullPointerException("The node cannot be null");
        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");

        graph.put(nodeId, new HashMap<>());
        nodeIdNodeData.put(nodeId, new NodeData<>(nodeId, heuristicMap.get(nodeId)));
    }

    void addEdge(T nodeIdFirst, T nodeIdSecond) {
        if (nodeIdFirst == null || nodeIdSecond == null) throw new NullPointerException("The first nor second node can be null.");

        if (!heuristicMap.containsKey(nodeIdFirst) || !heuristicMap.containsKey(nodeIdSecond)) {
            throw new NoSuchElementException("Source and Destination both should be part of the part of hueristic map");
        }
        if (!graph.containsKey(nodeIdFirst) || !graph.containsKey(nodeIdSecond)) {
            throw new NoSuchElementException("Source and Destination both should be part of the part of graph");
        }

        graph.get(nodeIdFirst).put(nodeIdNodeData.get(nodeIdSecond), 1.0);
        graph.get(nodeIdSecond).put(nodeIdNodeData.get(nodeIdFirst), 1.0);
    }

    Map<NodeData<T>, Double> edgesFrom(T nodeId) {
        if (nodeId == null) throw new NullPointerException("The input node should not be null.");
        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of hueristic map");
        if (!graph.containsKey(nodeId)) throw new NoSuchElementException("The node should not be null.");

        return Collections.unmodifiableMap(graph.get(nodeId));
    }

    NodeData<T> getNodeData(T nodeId) {
        if (nodeId == null) { throw new NullPointerException("The nodeid should not be empty"); }
        if (!nodeIdNodeData.containsKey(nodeId))  { throw new NoSuchElementException("The nodeId does not exist"); }
        return nodeIdNodeData.get(nodeId);
    }

    @Override public Iterator<T> iterator() {
        return graph.keySet().iterator();
    }
}
