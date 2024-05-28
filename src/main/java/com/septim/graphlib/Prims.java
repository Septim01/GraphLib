package com.septim.graphlib;

import com.diogonunes.jcolor.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Prims {

    /*
        * The Callback interface defines a single method, call, which takes two integers as arguments and returns void.
     */
    public interface Callback {
        void call(int currVertex, int prevVertex) throws IOException;
    }

    public Callback callback = null;

    public Graph graph;
    public int start;
    public int[] weights;

    /*
        * The constructor initializes the graph, the start vertex, and the edge weights.
        * If no parameters are provided, the default values are used.
     */
    public Prims() {
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
    public Prims(Graph graph, int start, int[] weights) {
        this.graph = graph;
        this.start = start;
        this.weights = weights;
    }

    public List<Integer> run() throws IOException {

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
     * The run_impl method executes the Prims algorithm on the graph and returns the minimum spanning tree.
     */
    private List<Integer> run_impl(Graph graph, int start, int[] weights) throws IOException {


        int n = graph.N;
        boolean[] visited = new boolean[n];
        List<Integer> mst = new ArrayList<>();

        record Node(int id, int distance, int edge) implements Comparable<Node> {

            /*
             * The compareTo method compares two nodes based on their distances.
             * The method returns a negative value if this node has a smaller distance than the other node, a positive value if this node has a larger distance than the other node, and zero if the distances are equal.
             */
            @Override
            public int compareTo(Node other) {
                return Integer.compare(this.distance, other.distance);
            }
        }

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.offer(new Node(start, 0, -1));

        graph.vertex_colors = new HashMap<>();
        graph.edge_colors = new HashMap<>();


        while (!pq.isEmpty()) {
            Node node = pq.poll();
            int u = node.id;
            int distU = node.distance;

            if (visited[u]){
                continue;
            }
            visited[u] = true;

            if (node.edge == -1)
                mst.add(node.edge);

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
                if(!visited[v]){
                    pq.offer(new Node(v, distanceThroughU, i));
                }

            }
            callback.call(u, node.edge);

        }
        return mst;
    }

    /*
     * The main method creates an instance of the Prims class and runs the Prims algorithm.
     */

    public static void main(String[] args) throws IOException {
        Prims prims = new Prims();
        List<Integer> mst = prims.run();
    }
}
