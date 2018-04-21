package application.model.astar;

import org.neo4j.driver.v1.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AStarThread implements Runnable{

    private List<Record> nodes;
    private List<Record> relations;
    private Map<String, Map<String, Double>> heuristic;
    private GraphAStar<String> graph;
    private AStar<String> algorithm_a_star;

    private String Start;
    private String End;

    public AStarThread(
    		List<Record> nodes, 
    		List<Record> relations, 
    		Map<String,Map<String, Double>> heuristic,
            GraphAStar<String> graph, 
            AStar<String> algorithm_a_star) 
    {
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
        for ( final Record nodeFrom : nodes ) {
            Map<String, Double> map = new HashMap<>();
            for ( final Record nodeTo : nodes ) {
                if(nodeFrom.get(0) == nodeTo.get(0))
                    map.put(nodeTo.get(0).toString(), 0.0);
                else
                    map.put(nodeTo.get(0).toString(), 1.0);
            }
            heuristic.put(nodeFrom.get(0).toString(),map);
        }
        for ( final Record node : nodes ) {
            graph.addNode((node.get(0) + ""));
        }
        for ( final Record relation : relations ) {
            String from = relation.get(0) + "";
            String to = relation.get(2) + "";
            graph.addEdge(from,to);
        }
        try {
            for (String path : algorithm_a_star.astar(Start , End)) {
                System.out.println("path = " + path);
            }
        }catch (NullPointerException e){
            System.out.println("Brak œcie¿ki");
        }
    }
}
