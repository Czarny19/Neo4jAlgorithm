package application.model;

import java.util.Map;

class NodeData<T> {
    private final T NodeID;
    private final Map<T, Double> heuristic;

    private double g;
    private double f;

    NodeData(T nodeId, Map<T, Double> heuristic) {
        this.NodeID = nodeId;
        this.g = Double.MAX_VALUE;
        this.heuristic = heuristic;
    }

    T getNodeId() {
        return NodeID;
    }

    double getG() {
        return g;
    }

    void setG(double g) {
        this.g = g;
    }

    void calcF(T destination) {
        double h = heuristic.get(destination);
        this.f = g + h;
    }

    double getF() {
        return f;
    }
}
