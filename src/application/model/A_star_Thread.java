package application.model;

import org.neo4j.driver.v1.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class A_star_Thread implements Runnable{

    private List<Record> nodes;
    private List<Record> relations;
    private Map<String, Map<String, Double>> heuristic;
    private GraphAStar<String> graph;
    private Algorithm_A_star<String> algorithm_a_star;

    private String Start;
    private String End;

    public A_star_Thread(List<Record> nodes, List<Record> relations , Map<String, Map<String, Double>> heuristic,
                         GraphAStar<String> graph, Algorithm_A_star<String> algorithm_a_star) {
        this.nodes = nodes;
        this.relations = relations;
        this.heuristic = heuristic;
        this.graph = graph;
        this.algorithm_a_star = algorithm_a_star;
    }

    public void setRoute(String Start, String End){
        this.Start = Start;
        this.End = End;
    }

    @Override
    public void run() {
        System.out.println("krok0");
        for ( final Record node : nodes ) {
            Map<String, Double> map = new HashMap<>();
            for ( final Record node_2 : nodes ) {
                if(node.get(0) == node_2.get(0))
                    map.put(node_2.get(0).toString(), 0.0);

                else
                    map.put(node_2.get(0).toString(), 1.0);
            }
            heuristic.put(node.get(0).toString(),map);
        }
        System.out.println("krok1");
        for ( final Record node : nodes ) {
            graph.addNode((node.get(0) + ""));
        }
        for ( final Record relation : relations ) {
            String from = relation.get(0).asPath().start().id() + "";
            String to = relation.get(0).asPath().end().id() + "";
            graph.addEdge(from,to);
        }
        try {
            for (String path : algorithm_a_star.astar(Start , End)) {
                System.out.println("path = " + path);
            }
        }catch (NullPointerException e){
            System.out.println("brak ścieżki");
        }

    }
}
