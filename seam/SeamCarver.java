/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;

public class SeamCarver {

    private Picture picture;
    private double[][] energy;
    private int[][] color;
    private int height;
    private int width;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException(
                "must provide a Picture object to the SeamCarver constructor");
        this.picture = new Picture(picture);
        this.height = this.picture.height();
        this.width = this.picture.width();
        this.energy = new double[height][width];
        this.color = new int[height][width];
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                color[row][col] = picture.getRGB(col, row);
            }
        }
        calculateEnergyMatrix();
    }

    private void calculateEnergyMatrix() {
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                energy[row][col] = energy(col, row);
            }
        }
    }

    // current picture
    public Picture picture() {
        return new Picture(this.picture);
    }

    // width of current picture
    public int width() {
        return this.width;
    }

    // height of current picture
    public int height() {
        return this.height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) return 1000;
        // int rgbXPlusOne = picture.getRGB(x + 1, y);
        // int rgbXMinusOne = picture.getRGB(x - 1, y);
        // int rgbYPlusOne = picture.getRGB(x, y + 1);
        // int rgbYMinusOne = picture.getRGB(x, y - 1);
        // StdOut.println("energy: x " + x + " y " + y + " height " + height() + " width " + width());
        int rgbXPlusOne = this.color[y][x + 1];
        int rgbXMinusOne = this.color[y][x - 1];
        int rgbYPlusOne = this.color[y + 1][x];
        int rgbYMinusOne = this.color[y - 1][x];
        return Math.sqrt(gradientSquared(rgbXPlusOne, rgbXMinusOne) + gradientSquared(rgbYPlusOne,
                                                                                      rgbYMinusOne));
    }

    private double gradientSquared(int rgb0, int rgb1) {
        return (Math.pow(r(rgb0) - r(rgb1), 2) + Math.pow(g(rgb0) - g(rgb1), 2) + Math
                .pow(b(rgb0) - b(rgb1), 2));
    }

    private int r(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    private int g(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private int b(int rgb) {
        return (rgb >> 0) & 0xFF;
    }

    private double[][] transposeEnergyMatrix(double[][] energyMatrix, int originalHeight,
                                             int originalWidth) {
        int newWidth = originalHeight;
        int newHeight = originalWidth;
        double[][] transposedMatrix = new double[newHeight][newWidth];
        for (int row = 0; row < newHeight; row++) {
            for (int col = 0; col < newWidth; col++) {
                transposedMatrix[row][col] = energyMatrix[col][row];
            }
        }
        return transposedMatrix;
    }

    private int[][] transposeColorMatrix(int[][] colorMatrix, int originalHeight,
                                         int originalWidth) {
        StdOut.println("input matrix height " + colorMatrix.length + " input width "
                               + colorMatrix[0].length + " originalHeight " + originalHeight
                               + " originalWidth " + originalWidth);
        int newWidth = originalHeight;
        int newHeight = originalWidth;
        int[][] transposedMatrix = new int[newHeight][newWidth];
        for (int row = 0; row < newHeight; row++) {
            for (int col = 0; col < newWidth; col++) {
                transposedMatrix[row][col] = colorMatrix[col][row];
            }
        }
        return transposedMatrix;
    }


    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        double[][] transposedEnergyMatrix = transposeEnergyMatrix(this.energy, this.height,
                                                                  this.width);
        return findSeamHelper(transposedEnergyMatrix, this.height, this.width);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findSeamHelper(this.energy, this.width, this.height);
    }


    private int[] findSeamHelper(double[][] matrix, int width, int height) {
        Dijkstra dijkstra = new Dijkstra(matrix, width, height);
        int[] seam = dijkstra.shortestPath();
        return seam;
    }

    private ArrayList<Integer> adjacent(int col, int width) {
        if (col < 0) throw new IllegalArgumentException("column cannot be less than zero");
        ArrayList<Integer> reachable = new ArrayList<>();
        if (col - 1 >= 0) reachable.add(col - 1);
        reachable.add(col);
        if (col + 1 < width) reachable.add(col + 1);
        return reachable;
    }

    private int[][] shiftColorMatrix(int[][] matrix, int[] seam, int width, int height) {
        int[][] tempColor = new int[height][width];
        for (int row = 0; row < matrix.length; row++) {
            int[] pixelRow = matrix[row];
            int elim = seam[row];
            for (int col = 0; col < pixelRow.length; col++) {
                if (col < elim) tempColor[row][col] = matrix[row][col];
                else if (col > elim) tempColor[row][col - 1] = matrix[row][col];
            }
        }
        return tempColor;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        int newHeight = this.height - 1;
        int[][] colorT = transposeColorMatrix(this.color, this.height, this.width);
        int[][] tempColorT = shiftColorMatrix(colorT, seam, newHeight, this.width);
        int[][] colorTT = transposeColorMatrix(tempColorT, this.width, newHeight);
        this.color = colorTT;
        double[][] energyT = transposeEnergyMatrix(this.energy, this.height, this.width);
        double[][] tempEnergyT = seamRemovalHelper(energyT, seam, newHeight, this.width);
        double[][] energyTT = transposeEnergyMatrix(tempEnergyT, this.width, newHeight);
        this.energy = energyTT;
        this.height = newHeight;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        int newWidth = this.width - 1;
        this.width = newWidth;
        int[][] tempColor = shiftColorMatrix(this.color, seam, this.width, this.height);
        this.color = tempColor;
        double[][] tempEnergy = seamRemovalHelper(this.energy, seam, this.width, this.height);
        this.energy = tempEnergy;
    }

    private double[][] seamRemovalHelper(double[][] energyMatrix, int[] seam, int width,
                                         int height) {
        double[][] tempEnergy = new double[height][width];
        for (int x = 0; x < energyMatrix.length; x++) {
            double[] energyRow = energyMatrix[x];
            int elim = seam[x];
            for (int y = 0; y < energyRow.length; y++) {
                if (y < elim) {
                    if (y == elim - 1) {
                        tempEnergy[x][y] = energy(y, x);
                    }
                    else {
                        tempEnergy[x][y] = energyMatrix[x][y];
                    }
                }
                else if (y > elim) {
                    if (y == elim + 1) {
                        tempEnergy[x][y - 1] = energy(y - 1, x);
                    }
                    else {
                        tempEnergy[x][y - 1] = energyMatrix[x][y];
                    }
                }
            }
        }
        return tempEnergy;
    }

    //  unit testing (optional)
    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);
        StdOut.printf("image is %d pixels wide by %d pixels high.\n", picture.width(),
                      picture.height());

        SeamCarver sc = new SeamCarver(picture);

        StdOut.printf("Printing energy calculated for each pixel.\n");

        for (int row = 0; row < sc.height(); row++) {
            for (int col = 0; col < sc.width(); col++)
                StdOut.printf("%9.2f ", sc.energy(col, row));
            StdOut.println();
        }

        StdOut.printf("Printing energy energy matrix.\n");

        double[][] energy = sc.energy;

        for (int row = 0; row < sc.height(); row++) {
            for (int col = 0; col < sc.width(); col++)
                StdOut.printf("%9.2f ", energy[row][col]);
            StdOut.println();
        }

        StdOut.printf("Printing transposed energy matrix.\n");

        double[][] transposedEnergy = sc.transposeEnergyMatrix(sc.energy, sc.height, sc.width);

        for (int row = 0; row < sc.width(); row++) {
            for (int col = 0; col < sc.height(); col++)
                // StdOut.printf("%9.0f ", sc.energy(col, row));
                StdOut.printf("%9.2f ", transposedEnergy[row][col]);
            StdOut.println();
        }

    }


    private class Dijkstra {

        private Edge[] edgeTo;
        private double[] distTo;
        private IndexMinPQ<Double> pq;
        private int top = 0;
        private int bottom;
        private int width;
        private int height;
        private double[][] energy;
        private int[] shortestPath;

        public Dijkstra(double[][] matrix, int width, int height) {
            this.energy = matrix;
            this.edgeTo = new Edge[height + 2];
            this.distTo = new double[height + 2];
            this.pq = new IndexMinPQ<Double>((width * height) + 2);
            this.bottom = height + 1;
            this.width = width;
            this.height = height;
            this.shortestPath = new int[this.height];

            for (int v = 0; v < bottom; v++) {
                distTo[v] = Double.POSITIVE_INFINITY;
            }
            distTo[top] = 0.0;

            // initialize row
            int nextRow = 0;

            pq.insert(top, 0.0);
            while (!pq.isEmpty()) {
                int row = pq.delMin();
                int col = edgeTo[row] != null ? edgeTo[row].w().col() : 0;
                for (Edge e : adj(new Vertex(nextRow, col))) {
                    StdOut.println(
                            "next adjacent edge from: " + e.v().row() + " " + e.v().col() + " to "
                                    + e.w().row() + " " + e.w().col());
                    relax(e);
                }
                nextRow++;
            }

        }

        public int[] shortestPath() {
            for (int row = 1; row < this.height; row++) {
                StdOut.println("row " + row + " height " + this.height + " edgeTo length "
                                       + this.edgeTo.length + " shortestPath length "
                                       + this.shortestPath.length);
                StdOut.println("this.edgeTo[row] " + this.edgeTo[row]);
                this.shortestPath[row] = this.edgeTo[row].w().col();
            }
            return this.shortestPath;
        }

        // return adjacent columns
        public ArrayList<Edge> adj(Vertex v) {
            int rowFrom = v.row();
            int colFrom = v.col();
            int row = rowFrom + 1;
            ArrayList<Edge> adjacent = new ArrayList<>();
            if (rowFrom < -1 || rowFrom >= this.bottom) return adjacent;
            else if (rowFrom == -1) {
                for (int i = 0; i < this.width; i++) {
                    adjacent.add(new Edge(v, new Vertex(row, i)));
                }
                return adjacent;
            }
            else if (row == this.height) {
                adjacent.add(new Edge(v, new Vertex(this.bottom, 0)));
                return adjacent;
            }
            else {
                if (colFrom - 1 >= 0) adjacent.add(new Edge(v, new Vertex(row, colFrom - 1)));
                adjacent.add(new Edge(v, new Vertex(row, colFrom)));
                if (colFrom + 1 < width) adjacent.add(new Edge(v, new Vertex(row, colFrom + 1)));
            }
            return adjacent;
        }

        public void relax(Edge e) {
            Vertex v = e.v();
            Vertex w = e.w();
            if (distTo[w.row()] > distTo[v.row()] + e.weight()) {
                StdOut.println(
                        "relaxing w.row() " + w.row() + " distTo[w.row()] " + distTo[w.row()]);
                distTo[w.row()] = distTo[v.row()] + e.weight();
                edgeTo[w.row()] = e;
                if (pq.contains(w.row())) pq.decreaseKey(w.row(), distTo[w.row()]);
                else pq.insert(w.row(), distTo[w.row()]);
            }
        }

        private class Edge {
            private Vertex v; // from
            private Vertex w; // to
            private double weight;

            public Edge(Vertex from, Vertex to) {
                this.v = from;
                this.w = to;
                if (this.w.row() == Dijkstra.this.bottom) {
                    this.weight = 0.0;
                }
                else {
                    this.weight = Dijkstra.this.energy[this.w.row()][this.w.col()];
                }
            }

            public Vertex v() {
                return this.v;
            }

            public Vertex w() {
                return this.w;
            }

            public double weight() {
                return this.weight;
            }
        }

        private class Vertex {
            private int row;
            private int col;

            public Vertex(int row, int col) {
                this.row = row;
                this.col = col;
            }

            public int row() {
                return this.row;
            }

            public int col() {
                return this.col;
            }

        }

    }

}
