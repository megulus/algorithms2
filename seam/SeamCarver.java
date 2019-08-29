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
    private SeamCarverHelper verticalCarver, horizontalCarver;

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

        this.verticalCarver = new SeamCarverHelper(this.color, this.energy);
        this.horizontalCarver = new SeamCarverHelper(transposeColorMatrix(this.color),
                                                     transposeEnergyMatrix(this.energy));
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
        if (this.picture != null) return new Picture(this.picture);
        else {
            this.picture = new Picture(this.width, this.height);
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    this.picture.setRGB(col, row, this.color[row][col]);
                }
            }
        }
        return this.picture;
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
    public double energy(int col, int row) {
        if (col < 0 || row < 0 || col > this.width - 1 || row > this.height - 1)
            throw new IllegalArgumentException(
                    "x and y must be greater than 0, less than width, height respectively");
        if (col == 0 || col == width - 1 || row == 0 || row == height - 1) return 1000;
        int rgbXPlusOne = this.color[row][col + 1];
        int rgbXMinusOne = this.color[row][col - 1];
        int rgbYPlusOne = this.color[row + 1][col];
        int rgbYMinusOne = this.color[row - 1][col];
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

    private double[][] transposeEnergyMatrix(double[][] energyMatrix) {
        int newNumberCols
                = energyMatrix.length; // orig rows == orig height == new width == new cols
        int newNumberRows
                = energyMatrix[0].length; // orig cols == orig width == new height == new rows
        double[][] transposedMatrix = new double[newNumberRows][newNumberCols];
        for (int row = 0; row < newNumberRows; row++) {
            for (int col = 0; col < newNumberCols; col++) {
                transposedMatrix[row][col] = energyMatrix[col][row];
            }
        }
        return transposedMatrix;
    }

    private int[][] transposeColorMatrix(int[][] colorMatrix) {
        int newNumberCols = colorMatrix.length;
        int newNumberRows = colorMatrix[0].length;
        int[][] transposedMatrix = new int[newNumberRows][newNumberCols];
        for (int row = 0; row < newNumberRows; row++) {
            for (int col = 0; col < newNumberCols; col++) {
                transposedMatrix[row][col] = colorMatrix[col][row];
            }
        }
        return transposedMatrix;
    }


    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        double[][] transposedEnergyMatrix = transposeEnergyMatrix(this.energy);
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
        if (seam == null) throw new IllegalArgumentException("must provide a seam to remove");
        if (seam.length != this.width)
            throw new IllegalArgumentException("seam is the wrong length");
        if (this.height <= 1)
            throw new IllegalArgumentException("cannot remove horizonal seam when width <= 1");
        this.picture = null;
        int targetHeight = this.height - 1;
        int[][] colorT = transposeColorMatrix(this.color);
        int[][] tempColorT = shiftColorMatrix(colorT, seam, targetHeight, this.width);
        int[][] colorTT = transposeColorMatrix(tempColorT);
        double[][] energyT = transposeEnergyMatrix(this.energy);
        this.color = tempColorT;
        // temp:
        this.height = this.width;
        this.width = targetHeight;
        double[][] tempEnergyT = seamRemovalHelper(energyT, seam);
        this.width = this.height; // i.e., back to original width
        this.height = targetHeight;
        double[][] energyTT = transposeEnergyMatrix(tempEnergyT);
        this.color = colorTT;
        this.energy = energyTT;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) throw new IllegalArgumentException("must provide a seam to remove");
        if (seam.length != this.height)
            throw new IllegalArgumentException("seam is the wrong length");
        if (this.width <= 1)
            throw new IllegalArgumentException("cannot remove vertical seam when height <= 1");
        this.picture = null;
        this.width--;
        this.color = shiftColorMatrix(this.color, seam, this.width, this.height);
        this.energy = seamRemovalHelper(this.energy, seam);
    }

    private double[][] seamRemovalHelper(double[][] energyMatrix, int[] seam) {
        double[][] tempEnergy = new double[this.height][this.width];
        int previousElimCol = seam[0];
        for (int row = 0; row < energyMatrix.length; row++) {
            double[] energyRow = energyMatrix[row];
            int elimCol = seam[row];
            // if (elimCol < 0 || elimCol >= energyMatrix.length)
            //     throw new IllegalArgumentException("invalid seam - entry out of range");
            if (Math.abs(elimCol - previousElimCol) > 1)
                throw new IllegalArgumentException("invalid seam");
            for (int col = 0; col < energyRow.length; col++) {
                if (col < elimCol) {
                    if (col == elimCol - 1) {
                        tempEnergy[row][col] = energy(col, row);
                    }
                    else {
                        tempEnergy[row][col] = energyMatrix[row][col];
                    }
                }
                else if (col > elimCol) {
                    if (col == elimCol + 1) {
                        tempEnergy[row][col - 1] = energy(col - 1, row);
                    }
                    else {
                        tempEnergy[row][col - 1] = energyMatrix[row][col];
                    }
                }
            }
            previousElimCol = elimCol;
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

    private static class SeamCarverHelper {
        public SeamCarverHelper(int[][] color, double[][] energy) {

        }

        public int[] findSeam() {
        }

        public void removeSeam(int[] seam) {

        }

        public int[][] getColorMatrix() {
            // needed because the SeamCarverHelper won't
        }

        public double[][] getEnergyMatrix() {

        }

        private int numRows() {
        }

        private int numCols() {

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
