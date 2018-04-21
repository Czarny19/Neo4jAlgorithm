package application.model.astar;

import java.util.*;

public class GraphAStar<T> implements Iterable<T> {

    private final Map<T, Map<NodeData<T>, Double>> graph;
    private Map<T, Map<T, Double>> heuristicMap;
    private final Map<T, NodeData<T>> nodeIdNodeData;

    public GraphAStar(Map<T, Map<T, Double>> heuristicMap) {
        if (heuristicMap == null) throw new NullPointerException("Mapa heurystyczna nie mo¿e byæ pusta");
        graph = new HashMap<>();
        nodeIdNodeData = new HashMap<>();
        this.heuristicMap = heuristicMap;
    }

    void addNode(T nodeId) {
        if (nodeId == null) throw new NullPointerException("Wierzcho³ek nie mo¿e byæ pusty");
        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("Wierzcho³ek nie nale¿y do mapy heurystycznej");

        graph.put(nodeId, new HashMap<>());
        nodeIdNodeData.put(nodeId, new NodeData<>(nodeId, heuristicMap.get(nodeId)));
    }

    void addEdge(T nodeIdFirst, T nodeIdSecond) {
        if (nodeIdFirst == null || nodeIdSecond == null) throw new NullPointerException("Pierwszy i drugi wierzcho³ek nie mo¿e byæ pusty");

        if (!heuristicMap.containsKey(nodeIdFirst) || !heuristicMap.containsKey(nodeIdSecond)) {
            throw new NoSuchElementException("Pierwszy i drugi wierzcho³ek musz¹ byæ czêœci¹ mapy heurystycznej");
        }
        if (!graph.containsKey(nodeIdFirst) || !graph.containsKey(nodeIdSecond)) {
            throw new NoSuchElementException("Pierwszy i drugi wierzcho³ek musz¹ byæ czêœci¹ grafu");
        }

        graph.get(nodeIdFirst).put(nodeIdNodeData.get(nodeIdSecond), 1.0);
        graph.get(nodeIdSecond).put(nodeIdNodeData.get(nodeIdFirst), 1.0);
    }

    Map<NodeData<T>, Double> edgesFrom(T nodeId) {
        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("Wierzcho³ek nie nale¿y do mapy heurystycznej");
        if (!graph.containsKey(nodeId)) throw new NoSuchElementException("Wierzcho³ek nie mo¿e byæ pusty.");

        return Collections.unmodifiableMap(graph.get(nodeId));
    }

    NodeData<T> getNodeData(T nodeId) {
        if (!nodeIdNodeData.containsKey(nodeId))  { throw new NoSuchElementException("Brak istniej¹cego nodeId"); }
        return nodeIdNodeData.get(nodeId);
    }

    @Override public Iterator<T> iterator() {
        return graph.keySet().iterator();
    }
}
