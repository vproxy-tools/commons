package io.vproxy.commons.graph;

import java.util.*;

public class Graph<N extends GraphNode<N>> {
    private final Set<N> nodes;

    public Graph(Collection<N> nodes) {
        this.nodes = new HashSet<>(nodes);
    }

    public Map<N, GraphPath<N>> shortestPaths(N from) {
        return shortestPaths(from, Collections.emptySet());
    }

    public Map<N, GraphPath<N>> shortestPaths(N from, Set<N> skipNodes) {
        if (!nodes.contains(from))
            throw new IllegalArgumentException("`from`=" + from + " is not contained in `nodes`");
        return Dijkstra.dijkstra(from, skipNodes);
    }

    public boolean containsNode(N node) {
        return nodes.contains(node);
    }
}
