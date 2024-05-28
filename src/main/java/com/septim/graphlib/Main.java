package com.septim.graphlib;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        Graph graph = new Graph();
        graph.N = 7;
        int start = 0;
        graph.edges(
                0, 1,
                0, 2,
                1, 2,
                1, 3,
                2, 1,
                2, 3,
                2, 4,
                3, 4,
                4, 3,
                4, 5,
                5, 6
        );
        int[] weights = new int[]{
                10,
                5,
                2,
                1,
                3,
                9,
                2,
                4,
                6,
                7,
                8
        };
        graph.set_perms(
                new int[]{0, 1, 2, 3, 4, 5, 6},
                new int[]{3, 1, 2, 0, 4, 5, 6}
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
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left
        };
        graph.setEdgeSides(sides);


        System.out.println("Running Dijkstra algorithm with arguments\n");
        Dijkstra dijkstra = new Dijkstra(graph, start, weights);
        int[] distances = dijkstra.run();
        // Print the shortest distances from the source node to all other nodes
        System.out.println("Shortest distances from node " + dijkstra.start + " to all other nodes:");
        for (int i = 0; i < dijkstra.graph.N; i++) {
            System.out.println("Node " + i + ": " + distances[i]);
        }

        System.out.println("\nEDNING DIJKSTRA\n");

        Dijkstra dijkstra1 = new Dijkstra();
        dijkstra1.run();

        System.out.println("\nEDNING DIJKSTRA\n");
        System.out.println("Running Prims algorithm with arguments\n");

        Prims prims1 = new Prims(graph, start, weights);

        List<Integer> mst1 = prims1.run();

        System.out.println("Minimum spanning tree");

        Prims prims2 = new Prims();
        prims2.run();


        System.out.println("-------------------\n");


        Graph graph2 = new Graph();
        graph2.N = 8;
        int start2 = 0;
        graph2.edges(
                0, 1,
                0, 2,
                0, 3,
                0, 4,
                0, 5,
                0, 6,
                0, 7,
                1, 2,
                1, 3,
                1, 4,
                1, 5,
                1, 6,
                1, 7,
                2, 3,
                2, 4,
                2, 5,
                2, 6,
                2, 7,
                3, 4,
                3, 5,
                3, 6,
                3, 7,
                4, 5,
                4, 6,
                4, 7,
                5, 6,
                5, 7,
                6, 7
        );

        int[] weights2 = new int[]{
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                14,
                15,
                16,
                17,
                18,
                19,
                20,
                21,
                22,
                23,
                24,
                25,
                26,
                27,
                28
        };

        graph2.set_perms(
                new int[]{0, 1, 2, 3, 4, 5, 6, 7},
                new int[]{3, 1, 2, 0, 4, 5, 6, 7}
        );

        Graph.Side[] sides2 = new Graph.Side[]{
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right,
                Graph.Side.left,
                Graph.Side.right
        };
        graph2.setEdgeSides(sides2);
        Prims prims3 = new Prims(graph2, start2, weights2);
        prims3.run();

    }
}