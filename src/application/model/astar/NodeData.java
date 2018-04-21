package application.model.astar;

import java.util.Map;

class NodeData<T> {
    private final T NodeID;
    private final Map<T, Double> heuristic;

    private double g;  // g = distance from the source
    private double f;  // f = g + h 

    NodeData(T nodeId, Map<T, Double> heuristic) {
        this.NodeID = nodeId;
        this.g = Double.MAX_VALUE;
        this.heuristic = heuristic;
    }

    T getNodeId() {
        return NodeID;
    }

    double G() {
        return g;
    }

    void setG(double g) {
        this.g = g;
    }

    void calcF(T destination) {
    	// h = heuristic of destination.
        double h = heuristic.get(destination);
        this.f = g + h;
    }

    double F() {
        return f;
    }
}
