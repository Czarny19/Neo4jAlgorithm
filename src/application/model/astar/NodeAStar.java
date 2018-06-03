package application.model.astar;

import java.util.HashMap;

import application.model.Node;

public class NodeAStar extends Node{

    private final HashMap<Integer, Double> heuristic;

    private double g;  // g = distance from the source
    private double f;  // f = g + h 

    public NodeAStar(int id, HashMap<Integer, Double> heuristic) {
        super(id);
        this.g = Double.MAX_VALUE;
        this.heuristic = heuristic;
    }

    public double g() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public void calcF(int destination) {
        double h = heuristic.get(destination);
        this.f = g + h;
    }

    public double f() {
        return f;
    }
}
