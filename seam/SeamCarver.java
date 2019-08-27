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


    private int[] findSeamHelper(double[][] matrix, int numColumns, int numRows) {
        Dijkstra dijkstra = new Dijkstra(matrix, numColumns, numRows);
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

        public Dijkstra(double[][] matrix, int width, int height) {
            this.energy = matrix;
            this.edgeTo = new Edge[width * height + 2];
            this.distTo = new double[width * height + 2];
            this.pq = new IndexMinPQ<Double>((width * height) + 2);
            this.bottom = width * height + 1;
            this.width = width;
            this.height = height;

            for (int v = 0; v < this.distTo.length; v++) {
                distTo[v] = Double.POSITIVE_INFINITY;
            }
            distTo[top] = 0.0;

            pq.insert(top, 0.0);
            while (!pq.isEmpty()) {
                int vertex = pq.delMin();
                for (Edge e : this.adj(vertex)) {
                    relax(e);
                }
            }

        }

        private int vertexNum(int row, int col) {
            return (this.width * row) + col + 1;
        }

        private int rowForVertex(int vertexNum) {
            if (vertexNum <= 0 || vertexNum > (this.width * this.height)) {
                throw new IllegalArgumentException("grump");
            }
            return (vertexNum - 1) / this.width;
        }

        private int colForVertex(int vertexNum) {
            if (vertexNum <= 0 || vertexNum > (this.width * this.height)) {
                throw new IllegalArgumentException("grump");
            }
            return (vertexNum - 1) % this.width;

        }

        public int[] shortestPath() {
            int[] result = new int[this.height];
            Edge e = this.edgeTo[this.bottom];
            while (e.v() != this.top) {
                int currentRow = this.rowForVertex(e.v());
                int currentColumn = this.colForVertex(e.v());
                result[currentRow] = currentColumn;
                e = this.edgeTo[e.v()];
            }
            return result;
        }

        // return adjacent vertices
        public ArrayList<Edge> adj(int vertexNum) {
            ArrayList<Edge> adjacent = new ArrayList<>();
            if (vertexNum == this.top) {
                for (int i = 0; i < this.width; i++) {
                    adjacent.add(new Edge(vertexNum, this.vertexNum(0, i)));
                }
            }
            else if (vertexNum != this.bottom) {
                if (this.rowForVertex(vertexNum) == this.height - 1) {
                    adjacent.add(new Edge(vertexNum, this.bottom));
                }
                else {
                    int row = this.rowForVertex(vertexNum);
                    int col = this.colForVertex(vertexNum);
                    if (col > 0) {
                        adjacent.add(new Edge(vertexNum, this.vertexNum(row + 1, col - 1)));
                    }
                    adjacent.add(new Edge(vertexNum, this.vertexNum(row + 1, col)));
                    if (col < this.width - 1) {
                        adjacent.add(new Edge(vertexNum, this.vertexNum(row + 1, col + 1)));
                    }
                }
            }
            return adjacent;
        }

        public void relax(Edge e) {
            int v = e.v();
            int w = e.w();
            double distanceThroughV = distTo[v] + e.weight();
            if (distTo[w] > distanceThroughV) {
                distTo[w] = distanceThroughV;
                edgeTo[w] = e;
                if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
                else pq.insert(w, distTo[w]);
            }
            else if (distanceThroughV == distTo[w]) {
                Edge oldEdge = edgeTo[w];
                if (v < oldEdge.v()) {
                    edgeTo[w] = e;
                }
            }
        }

        private String vertAsString(int vertexNum) {
            if (vertexNum == this.top) {
                return "top (" + vertexNum + ")";
            }
            if (vertexNum == this.bottom) {
                return "bottom (" + vertexNum + ")";
            }
            return "(" + this.rowForVertex(vertexNum) + ", " + this.colForVertex(vertexNum) + ") - "
                    +
                    vertexNum;
        }

        private class Edge {
            private int v; // from
            private int w; // to
            private double weight;

            public Edge(int from, int to) {
                this.v = from;
                this.w = to;
                if (this.w == width * height + 1) {
                    this.weight = 0.0;
                }
                else {
                    this.weight = Dijkstra.this.energy[rowForVertex(this.w)][colForVertex(this.w)];
                }
            }

            public int v() {
                return this.v;
            }

            public int w() {
                return this.w;
            }

            public double weight() {
                return this.weight;
            }
        }

    }

}
