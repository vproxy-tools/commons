package io.vproxy.commons.graph;

import io.vproxy.base.util.coll.Tuple;

import java.util.*;

public class Dijkstra {
    private Dijkstra() {
    }

    public static <N extends GraphNode<N>> Map<N, GraphPath<N>> dijkstra(N from) {
        return dijkstra(from, Collections.emptySet());
    }

    public static <N extends GraphNode<N>> Map<N, GraphPath<N>> dijkstra(N from, Collection<N> skipNodes) {
        if (skipNodes.contains(from)) {
            throw new IllegalArgumentException("`skipNodes`=" + skipNodes + " contains `from`=" + from);
        }
        var distances = new HashMap<N, Tuple<Long, List<GraphEdge<N>>>>();
        distances.put(from, new Tuple<>(0L, new ArrayList<>()));
        var visited = new HashSet<N>();
        visited.add(from);
        dijkstra(new HashSet<>(skipNodes), visited, distances);

        var res = new HashMap<N, GraphPath<N>>();
        for (var entry : distances.entrySet()) {
            if (entry.getValue()._2.isEmpty())
                continue;
            res.put(entry.getKey(), new GraphPath<>(entry.getValue()._2));
        }
        return res;
    }

    private static <N extends GraphNode<N>> void dijkstra(Set<N> skipNodes,
                                                          Set<N> visited,
                                                          Map<N, Tuple<Long, List<GraphEdge<N>>>> distances) {
        for (var from : visited) {
            long len = distances.get(from)._1;
            for (var edge : from.edges.values()) {
                var edgeTo = edge.to;
                if (skipNodes.contains(edgeTo)) {
                    continue;
                }
                var totalLen = len + edge.distance;
                var ls = new ArrayList<>(distances.get(from)._2);
                ls.add(edge);
                if (!distances.containsKey(edgeTo) || distances.get(edgeTo)._1 > totalLen) {
                    distances.put(edgeTo, new Tuple<>(totalLen, ls));
                }
            }
        }
        N nextNode = null;
        long nextLen = 0;
        for (var entry : distances.entrySet()) {
            if (visited.contains(entry.getKey())) {
                continue;
            }
            var l = entry.getValue()._1;
            if (nextNode == null) {
                nextNode = entry.getKey();
                nextLen = l;
            } else {
                if (nextLen > l) {
                    nextNode = entry.getKey();
                    nextLen = l;
                }
            }
        }
        if (nextNode == null) {
            return;
        }
        visited.add(nextNode);
        dijkstra(skipNodes, visited, distances);
    }
}
