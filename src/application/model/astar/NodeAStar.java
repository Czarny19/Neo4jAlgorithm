package application.model.astar;

import java.util.HashMap;

import application.model.Node;

public class NodeAStar extends Node{

    private final HashMap<Integer, Double> Heuristic;

    private double g;  // g = distance from the source
    private double f;  // f = g + h 

    public NodeAStar(int id, HashMap<Integer, Double> Heuristic) {
        super(id);
        this.g = Double.MAX_VALUE;
        this.Heuristic = Heuristic;
    }

    public double G() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public void calcF(int destination) {
        double h = Heuristic.get(destination);
        this.f = g + h;
    }

    public double F() {
        return f;
    }
}
