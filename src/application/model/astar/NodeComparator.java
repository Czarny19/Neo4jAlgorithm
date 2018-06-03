package application.model.astar;

import java.util.Comparator;

public class NodeComparator implements Comparator<NodeAStar> {
    public int compare(NodeAStar nodeFirst, NodeAStar nodeSecond) {
        return Double.compare(nodeFirst.f(), nodeSecond.f());
    }
}
