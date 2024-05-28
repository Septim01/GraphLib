package com.septim.graphlib;

import com.diogonunes.jcolor.Attribute;

import java.util.ArrayList;
import java.util.Map;

import static com.diogonunes.jcolor.Ansi.colorize;

/**
 * Represents a graph and provides methods to visualize it in a grid layout.
 */

public class Graph {
    public int N; // number of vertices
    public int[][] edges; // edges as pairs of vertices

    public Side[] edgeSides;

    public Map<Integer, Attribute> edge_colors = Map.of(); // colors for edges
    public Map<Integer, Attribute> vertex_colors = Map.of(); // colors for vertices


    /**
     * Sets the edges of the graph.
     *
     * @param vertices pairs of vertices representing the edges
     */
    public void edges(int... vertices) {
        assert vertices.length % 2 == 0 : "number of vertices must be even";
        edges = new int[vertices.length / 2][2];
        for (int i = 0, j = 0; j < vertices.length; i++, j += 2) {
            edges[i][0] = vertices[j];
            edges[i][1] = vertices[j + 1];
        }
    }

    public void setEdgeSides(Side[] edgeSides) {
        this.edgeSides = edgeSides;
    }

    /**
     * perm_x, perm_y - are used for displaying the graph in grid
     * <br/>
     * each vertex has its own row/column, therefore permutation in x/y axes defines the grid visualization of the graph
     */
    protected int[] perm_x, perm_y;
    /**
     * {@field r_perm_x}/r_perm_y are inverse permutations, calculated from perm_x/perm_y
     */
    protected int[] r_perm_x, r_perm_y;


    /**
     * Sets the permutations for the x and y axes.
     *
     * @param perm_x permutation for the x axis
     * @param perm_y permutation for the y axis
     */
    public void set_perms(int[] perm_x, int[] perm_y) {
        this.perm_x = perm_x;
        this.perm_y = perm_y;
        this.r_perm_x = new int[perm_x.length];
        this.r_perm_y = new int[perm_y.length];

        for (int i = 0; i < perm_x.length; i++)
            r_perm_x[perm_x[i]] = i;
        for (int i = 0; i < perm_y.length; i++)
            r_perm_y[perm_y[i]] = i;
    }

    /**
     * edge is always displayed as L shape
     * <br/>
     * defines on which side should the L shape be\
     */
    public enum Side {left, right}

    /**
     * edge is always displayed as L shape
     */
    public void print_graph() {
        int[] needed_size = calculate_needed_size(edgeSides);
        int size = 0;
        for (int vertex_size : needed_size)
            size += vertex_size;

        Perms perms = calculate_sized_perms(needed_size, size);
        R_Perms r_perms = calculate_sized_inverse_perms(size, perms.p_x, perms.p_y);

        Cell[][] grid = get_grid(perms.p_x, perms.p_y);
        apply_edges_to_grid(grid, edgeSides, r_perms.r_p_x, r_perms.r_p_y);

        print_grid(grid);
    }

    // -------------------------------------------------------------------------------------------------------------


    /**
     * Calculates the needed size for each vertex.
     * @param sides specifies for each edge on which side should the L shape be
     * @return the needed size for each vertex
     */
    protected int[] calculate_needed_size(Side[] sides) {
        int[][] counts = new int[N][4];
        for (int i = 0; i < edges.length; i++) {
            var edge = edges[i];
            int a, b;
            if (r_perm_x[edge[0]] < r_perm_x[edge[1]]) {
                a = edges[i][0];
                b = edges[i][1];
            } else {
                a = edges[i][1];
                b = edges[i][0];
            }
            var side = sides[i];
//			if (side == Side.left) {
//				counts[b][VertexSide.left.ordinal()] += 1;
//				if (r_perm_y[a] < r_perm_y[b])
//					counts[a][VertexSide.bottom.ordinal()] += 1;
//				else
//					counts[a][VertexSide.top.ordinal()] += 1;
//			} else {
//				counts[a][VertexSide.right.ordinal()] += 1;
//				if (r_perm_y[a] < r_perm_y[b])
//					counts[b][VertexSide.top.ordinal()] += 1;
//				else
//					counts[b][VertexSide.bottom.ordinal()] += 1;
//			}
            if (side == Side.left && r_perm_y[a] < r_perm_y[b]) {
                // a ·
                // ╰─b
                counts[a][Direction.bottom.ordinal()] += 1;
                counts[b][Direction.left.ordinal()] += 1;
            } else if (side == Side.left && r_perm_y[a] > r_perm_y[b]) {
                // ╭─b
                // a ·
                counts[a][Direction.top.ordinal()] += 1;
                counts[b][Direction.left.ordinal()] += 1;
            } else if (side == Side.right && r_perm_y[a] < r_perm_y[b]) {
                // a─╮
                // · b
                counts[a][Direction.right.ordinal()] += 1;
                counts[b][Direction.top.ordinal()] += 1;
            } else if (side == Side.right && r_perm_y[a] > r_perm_y[b]) {
                // · b
                // a─╯
                counts[a][Direction.right.ordinal()] += 1;
                counts[b][Direction.bottom.ordinal()] += 1;
            }
        }

        int[] needed_size = new int[N];
        for (int i = 0; i < N; i++) {
            needed_size[i] = 1;
            for (int j = 0; j < 4; j++)
                needed_size[i] = Math.max(needed_size[i], counts[i][j]);
        }
        return needed_size;
    }


    /**
     * Calculates the permutations for the x and y axes with the specified size.
     */
    protected record Perms(int[] p_x, int[] p_y) {}


    /**
     * Calculates the permutations for the x and y axes with the specified size.
     *
     * @param needed_size the needed size for each vertex
     * @param size        the size of the grid
     * @return the permutations for the x and y axes
     */
    protected Perms calculate_sized_perms(int[] needed_size, int size) {
        int[] p_x = new int[size], p_y = new int[size];
        for (int i = 0, jx = 0, jy = 0; i < N; i++) {
            for (int ji = 0; ji < needed_size[perm_x[i]]; ji++, jx++)
                p_x[jx] = perm_x[i];
            for (int ji = 0; ji < needed_size[perm_y[i]]; ji++, jy++)
                p_y[jy] = perm_y[i];
        }
        return new Perms(p_x, p_y);
    }


    /**
     * Calculates the inverse permutations for the x and y axes with the specified size.
     */
    protected record R_Perms(ArrayList<Integer>[] r_p_x, ArrayList<Integer>[] r_p_y) {}

    /**
     * Calculates the inverse permutations for the x and y axes with the specified size.
     *
     * @param size the size of the grid
     * @param p_x  the permutation for the x axis
     * @param p_y  the permutation for the y axis
     * @return the inverse permutations for the x and y axes
     */
    protected R_Perms calculate_sized_inverse_perms(int size, int[] p_x, int[] p_y) {
        //noinspection unchecked
        ArrayList<Integer>[] r_p_x = new ArrayList[N];
        //noinspection unchecked
        ArrayList<Integer>[] r_p_y = new ArrayList[N];

        for (int i = 0; i < size; i++) {
            if (r_p_x[p_x[i]] == null)
                r_p_x[p_x[i]] = new ArrayList<>();
            if (r_p_y[p_y[i]] == null)
                r_p_y[p_y[i]] = new ArrayList<>();
            r_p_x[p_x[i]].add(i);
            r_p_y[p_y[i]].add(i);
        }

        return new R_Perms(r_p_x, r_p_y);
    }


    /**
     * Applies the edges to the grid.
     *
     * @param grid  the grid
     * @param sides specifies for each edge on which side should the L shape be
     * @param r_p_x the inverse permutation for the x axis
     * @param r_p_y the inverse permutation for the y axis
     */
    protected void apply_edges_to_grid(
            Cell[][] grid,
            Side[] sides,
            ArrayList<Integer>[] r_p_x,
            ArrayList<Integer>[] r_p_y
    ) {
        int[][] counts = new int[N][4];
        for (int i = 0; i < edges.length; i++) {
            var edge = edges[i];
            int a, b;
            if (r_perm_x[edge[0]] < r_perm_x[edge[1]]) {
                a = edges[i][0];
                b = edges[i][1];
            } else {
                a = edges[i][1];
                b = edges[i][0];
            }
            var side = sides[i];
            int ax, ay, bx, by;
            if (side == Side.left && r_perm_y[a] < r_perm_y[b]) {
                // a ·
                // ╰─b
                ax = r_p_x[a].get(counts[a][Direction.bottom.ordinal()]);
                ay = r_p_y[a].getLast();
                bx = r_p_x[b].getFirst();
                by = r_p_y[b].get(counts[b][Direction.left.ordinal()]);

                for (int y = ay + 1; y < by; y++)
                    grid[ax][y] = Cell.set(grid[ax][y], '│', i);
                grid[ax][by] = Cell.set(grid[ax][by], '╰', i);
                for (int x = ax + 1; x < bx; x++)
                    grid[x][by] = Cell.set(grid[x][by], '─', i);

                counts[a][Direction.bottom.ordinal()] += 1;
                counts[b][Direction.left.ordinal()] += 1;
            } else if (side == Side.left && r_perm_y[a] > r_perm_y[b]) {
                // ╭─b
                // a ·
                ax = r_p_x[a].get(counts[a][Direction.top.ordinal()]);
                ay = r_p_y[a].getFirst();
                bx = r_p_x[b].getFirst();
                by = r_p_y[b].get(counts[b][Direction.left.ordinal()]);

                for (int y = ay - 1; y > by; y--)
                    grid[ax][y] = Cell.set(grid[ax][y], '│', i);
                grid[ax][by] = Cell.set(grid[ax][by], '╭', i);
                for (int x = ax + 1; x < bx; x++)
                    grid[x][by] = Cell.set(grid[x][by], '─', i);

                counts[a][Direction.top.ordinal()] += 1;
                counts[b][Direction.left.ordinal()] += 1;
            } else if (side == Side.right && r_perm_y[a] < r_perm_y[b]) {
                // a─╮
                // · b
                ax = r_p_x[a].getLast();
                ay = r_p_y[a].get(counts[a][Direction.right.ordinal()]);
                bx = r_p_x[b].get(counts[b][Direction.top.ordinal()]);
                by = r_p_y[b].getFirst();

                for (int y = by - 1; y > ay; y--)
                    grid[bx][y] = Cell.set(grid[bx][y], '│', i);
                grid[bx][ay] = Cell.set(grid[bx][ay], '╮', i);
                for (int x = ax + 1; x < bx; x++)
                    grid[x][ay] = Cell.set(grid[x][ay], '─', i);

                counts[a][Direction.right.ordinal()] += 1;
                counts[b][Direction.top.ordinal()] += 1;
            } else if (side == Side.right && r_perm_y[a] > r_perm_y[b]) {
                // · b
                // a─╯
                ax = r_p_x[a].getLast();
                ay = r_p_y[a].get(counts[a][Direction.right.ordinal()]);
                bx = r_p_x[b].get(counts[b][Direction.bottom.ordinal()]);
                by = r_p_y[b].getLast();

                for (int y = by + 1; y < ay; y++)
                    grid[bx][y] = Cell.set(grid[bx][y], '│', i);
                grid[bx][ay] = Cell.set(grid[bx][ay], '╯', i);
                for (int x = ax + 1; x < bx; x++)
                    grid[x][ay] = Cell.set(grid[x][ay], '─', i);

                counts[a][Direction.right.ordinal()] += 1;
                counts[b][Direction.bottom.ordinal()] += 1;
            }
        }
    }


    /**
     * Represents a cell in the grid.
     */
    protected enum Direction {top, left, right, bottom}

    protected interface Cell {

        /**
         * Sets the cell.
         *
         * @param cell the cell
         * @param c    the character
         * @param edge the edge
         * @return the cell
         */
        static Cell set(Cell cell, char c, int edge) {
            if (cell == null) {
                cell = new Path(c, edge);
            } else if (cell instanceof Path path) {
                if ((c == '│' && path.c == '─') ||
                        (c == '─' && path.c == '│'))
                    path.intersection(edge);
                else assert false : "cant combine `" + path.c + "` with `" + c + "`";
            }
            return cell;
        }

        /**
         * Checks if two cells connect horizontally.
         *
         * @param a the first cell
         * @param b the second cell
         * @return true if the cells connect horizontally, false otherwise
         */
        static boolean connects_horizontally(Cell a, Cell b) {
            if (a == null || b == null) return false;
            if (a instanceof Node && b instanceof Node) return false;
            return a.connects_from(Direction.right) && b.connects_from(Direction.left);
        }
        boolean connects_from(Direction dir);

        /**
         * Returns the edge between two cells.
         *
         * @param a the first cell
         * @param b the second cell
         * @return the edge between the cells
         */
        static int edge_horizontally_between(Cell a, Cell b) {
            if (a instanceof Path path)
                return path.edge_from[Direction.right.ordinal()];
            if (b instanceof Path path)
                return path.edge_from[Direction.left.ordinal()];
            assert false : "no edge between " + a + " and " + b;
            return 0;
        }
    }

    protected static class Node implements Cell {
        int vertex; // vertex number
        Node(int vertex) {
            this.vertex = vertex;
        }
        @Override
        public boolean connects_from(Direction dir) {
            return true;
        }
        @Override
        public String toString() {
            return String.valueOf(vertex);
        }
    }

    protected static class Path implements Cell {
        //┼─│╭╮╯╰
        char c;
        int[] edge_from = new int[4];

        /**
         * Creates a new path.
         *
         * @param c    the character
         * @param edge the edge
         */
        Path(char c, int edge) {
            this.c = c;
            for (var dir : Direction.values())
                if (connects_from(dir))
                    edge_from[dir.ordinal()] = edge;
        }

        /**
         * Intersects the path with an edge.
         *
         * @param edge the edge
         */
        void intersection(int edge) {
            switch (c) {
                case '│' -> { // left-right
                    edge_from[Direction.left.ordinal()] = edge;
                    edge_from[Direction.right.ordinal()] = edge;
                }
                case '─' -> { // top-bottom
                    edge_from[Direction.top.ordinal()] = edge;
                    edge_from[Direction.bottom.ordinal()] = edge;
                }
                default -> {
                    assert false : "can't intersect `" + c + "`";
                }
            }
            c = '┼';
        }

        /**
         * Checks if the path connects from a direction.
         *
         * @param dir the direction
         * @return true if the path connects from the direction, false otherwise
         */
        @Override
        public boolean connects_from(Direction dir) {
            if (dir == Direction.top && "┼│╯╰".contains(String.valueOf(c)))
                return true;
            if (dir == Direction.left && "┼─╮╯".contains(String.valueOf(c)))
                return true;
            if (dir == Direction.bottom && "┼│╭╮".contains(String.valueOf(c)))
                return true;
            if (dir == Direction.right && "┼─╭╰".contains(String.valueOf(c)))
                return true;
            return false;
        }

        /**
         * Returns the string representation of the path.
         *
         * @return the string representation of the path
         */
        @Override
        public String toString() {
            return String.valueOf(c);
        }

        /**
         * Returns the edge of the path.
         *
         * @return the edge of the path
         */
        public Integer get_edge() {
            if (c == '┼') {
                if (connects_from(Direction.top)) return edge_from[Direction.top.ordinal()];
                if (connects_from(Direction.bottom)) return edge_from[Direction.bottom.ordinal()];
                return null;
            }
            for (var dir : Direction.values())
                if (connects_from(dir))
                    return edge_from[dir.ordinal()];
            assert false : "Path with no edge, should not happen `" + c + "`";
            return 0;
        }
    }


    /**
     * Returns the grid.
     *
     * @param perm_x the permutation for the x axis
     * @param perm_y the permutation for the y axis
     * @return the grid
     */
    protected Cell[][] get_grid(int[] perm_x, int[] perm_y) {
        int w, h;
        w = perm_x.length;
        h = perm_y.length;
        var grid = new Cell[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (perm_x[x] == perm_y[y])
                    grid[x][y] = new Node(perm_x[x]);
            }
        }
        return grid;
    }

    /**
     * Prints the grid.
     */
    public void print_grid() {
        Cell[][] grid = get_grid(perm_x, perm_y);
        print_grid(grid);
    }

    /**
     * Prints the grid.
     *
     * @param grid the grid
     */
    protected void print_grid(Cell[][] grid) {
        int w = grid.length, h = grid[0].length;
        StringBuilder sb = new StringBuilder();
        StringBuilder colored = new StringBuilder();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (x != 0) {
                    if (Cell.connects_horizontally(grid[x - 1][y], grid[x][y])) {
                        int edge = Cell.edge_horizontally_between(grid[x - 1][y], grid[x][y]);
                        sb.append(colorize("─", edge_colors.getOrDefault(edge, Attribute.NONE())));
                    } else
                        sb.append(' ');
                }
                if (grid[x][y] == null)
                    sb.append('·');
                else if (grid[x][y] instanceof Path path) {
                    Integer edge = path.get_edge();
                    Attribute attr = Attribute.NONE();
                    if (edge != null && edge_colors.containsKey(edge))
                        attr = edge_colors.get(edge);
                    sb.append(colorize(path.toString(), attr));
                } else if (grid[x][y] instanceof Node node) {
                    sb.append(colorize(
                            node.toString(),
                            vertex_colors.getOrDefault(node.vertex, Attribute.NONE())
                    ));
                } else { // this will never be used
                    sb.append(grid[x][y]);
                }
            }
            sb.append('\n');
        }
        System.out.println(sb);
    }

    private static void nop() {}

    public static void main(String[] args) {
        var g = new Graph();
        g.N = 4;

        g.edges(
                0, 2,
                0, 1,
                0, 3,
                3, 2,
                3, 1
        );
        g.set_perms(
                new int[]{3, 0, 2, 1},
                new int[]{3, 1, 2, 0}
        );

        g.print_grid();


//		g.edge_colors = new HashMap<>();
//		g.edge_colors.put(2, Attribute.TEXT_COLOR(255, 0, 0));
//		g.edge_colors.put(4, Attribute.TEXT_COLOR(0, 255, 0));

        g.edge_colors = Map.of(
                2, Attribute.TEXT_COLOR(255, 0, 0),
                4, Attribute.TEXT_COLOR(0, 255, 0)
        );
        g.vertex_colors = Map.of(
                3, Attribute.TEXT_COLOR(5, 100, 30)
        );

        System.out.println();
//		System.out.println();
//		g.print_graph(new Side[]{
//			Side.right,
//			Side.right,
//			Side.left,
//			Side.left,
//			Side.right,
//		});
        nop();
    }
}