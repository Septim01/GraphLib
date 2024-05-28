package com.septim.graphlib;

import com.diogonunes.jcolor.Attribute;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Dijkstra {

    /*
     * The Callback interface is used to define a callback method that is called during the execution of the Dijkstra algorithm.
     * The callback method is called for each vertex that is visited during the execution of the algorithm.
     * The callback method takes two parameters: the current vertex and the previous vertex.
     */
    public interface Callback {
        void call(int vertex, int edge) throws IOException;
    }

    public Callback callback = null;

    record Node(int id, int distance, int from) implements Comparable<Node> {

        /*
         * The compareTo method compares two nodes based on their distances.
         * The method returns a negative value if this node has a smaller distance than the other node, a positive value if this node has a larger distance than the other node, and zero if the distances are equal.
         */
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    Graph graph;
    int start;
    int[] weights;

    /*
        * The constructor initializes the graph, the start vertex, and the edge weights.
        * If no parameters are provided, the default values are used.
     */
    public Dijkstra() {
        graph = new Graph();
        graph.N = 5;
        start = 0;
        graph.edges(
                0, 1,
                0, 2,
                1, 2,
                1, 3,
                2, 1,
                2, 3,
                2, 4,
                3, 4,
                4, 3
        );
        weights = new int[]{
                10,
                5,
                2,
                1,
                3,
                9,
                2,
                4,
                6
        };
        graph.set_perms(
                new int[]{0, 1, 2, 3, 4},
                new int[]{3, 1, 2, 0, 4}
        );

        Graph.Side[] sides = new Graph.Side[]{
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left
        };
        graph.setEdgeSides(sides);
    }

    /*
     * The constructor initializes the graph, the start vertex, and the edge weights based on the provided parameters.
     * If user provides arguments to the constructor, the graph, the start vertex, and the edge weights are set to the provided values.
     */
    public Dijkstra(Graph graph, int start, int[] weights) {
        this.graph = graph;
        this.start = start;
        this.weights = weights;
    }


/*
    * The run method executes the Dijkstra algorithm on the graph and returns the shortest distances from the start vertex to all other vertices.
 */
    public int[] run() throws IOException {
        if (callback == null) {
            callback = (int now, int prev) -> {
                graph.vertex_colors.put(now, Attribute.TEXT_COLOR(255, 0, 0));
                graph.edge_colors.put(prev, Attribute.TEXT_COLOR(0, 0, 255));
                graph.print_graph();

                System.in.read();
            };


        }

        return run_impl(graph, start, weights);
    }
    /*
        * The run_impl method is the implementation of the Dijkstra algorithm.
     */
    private int[] run_impl(Graph graph, int start, int[] weights) throws IOException {


        int n = graph.N;
        int[] distances = new int[n];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[start] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.offer(new Node(start, 0, -1));

        graph.vertex_colors = new HashMap<>();
        graph.edge_colors = new HashMap<>();


        while (!pq.isEmpty()) {
            Node node = pq.poll();
            int u = node.id;
            int distU = node.distance;

            if (distances[u] < distU)
                continue;

            for (int i = 0; i < graph.edges.length; i++) {
                if (graph.edges[i][0] != u && graph.edges[i][1] != u) continue;
                int v = -1;
                if (graph.edges[i][0] == u) {
                    v = graph.edges[i][1];
                }
                if (graph.edges[i][1] == u) {
                    v = graph.edges[i][0];
                }
                ;
                int weightUV = weights[i];
                int distanceThroughU = distU + weightUV;

                if (distanceThroughU < distances[v]) {
                    distances[v] = distanceThroughU;
                    pq.offer(new Node(v, distanceThroughU, i));
                }
            }
            callback.call(u, node.from);

        }
        return distances;
    }
    /*
        * The main method creates an instance of the Dijkstra class and runs the Dijkstra algorithm on the graph.
        * It is created for user to run the Dijkstra algorithm with default values and understand how the algorithm works.
     */
    public static void main(String[] args) throws IOException {
        Dijkstra dijkstra = new Dijkstra();
        int[] distances = dijkstra.run();
        // Print the shortest distances from the source node to all other nodes
        System.out.println("Shortest distances from node " + dijkstra.start + " to all other nodes:");
        for (int i = 0; i < dijkstra.graph.N; i++) {
            System.out.println("Node " + i + ": " + distances[i]);
        }
    }
}

