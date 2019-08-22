/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;

public class SeamCarver {

    private Picture picture;
    private double[][] energy;
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
        calculateEnergyMatrix();
    }

    private void calculateEnergyMatrix() {
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                // StdOut.println("col: " + col + " row " + row);
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
        return this.picture.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x == 0 || x == width() - 1 || y == 0 || y == height() - 1) return 1000;
        int rgbXPlusOne = picture.getRGB(x + 1, y);
        int rgbXMinusOne = picture.getRGB(x - 1, y);
        int rgbYPlusOne = picture.getRGB(x, y + 1);
        int rgbYMinusOne = picture.getRGB(x, y - 1);
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

    private double[][] transposeMatrix() {
        int newWidth = this.height;
        int newHeight = this.width;
        double[][] transposedMatrix = new double[newHeight][newWidth];
        for (int row = 0; row < newHeight; row++) {
            for (int col = 0; col < newWidth; col++) {
                transposedMatrix[row][col] = this.energy[col][row];
            }
        }
        return transposedMatrix;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        double[][] transposedEnergyMatrix = transposeMatrix();
        return findSeamHelper(transposedEnergyMatrix, this.height, this.width);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        return findSeamHelper(this.energy, this.width, this.height);
    }

    private int[] findSeamHelper(double[][] matrix, int width, int height) {
        double minSeamEnergy = Double.POSITIVE_INFINITY;
        int[] seam = new int[height];
        for (int col = 0; col < width; col++) {
            int[] newSeam = new int[height];
            newSeam[0] = col;
            double previousSeamEnergy = matrix[0][col];
            double seamEnergy = previousSeamEnergy;
            int row = 1;
            int pathFrom = col;
            while (row < height) {
                ArrayList<Integer> reachableCols = reachableColumns(pathFrom, width);
                seamEnergy = previousSeamEnergy + matrix[row][reachableCols.get(0)];
                newSeam[row] = reachableCols.get(0);
                pathFrom = reachableCols.get(0);
                for (int reachableCol : reachableCols) {
                    double pathCandidateEnergy = previousSeamEnergy + matrix[row][reachableCol];
                    if (pathCandidateEnergy < seamEnergy) {
                        seamEnergy = previousSeamEnergy + matrix[row][reachableCol];
                        newSeam[row] = reachableCol;
                        pathFrom = reachableCol;
                    }
                }
                row++;
                previousSeamEnergy = seamEnergy;
            }
            if (seamEnergy < minSeamEnergy) {
                minSeamEnergy = seamEnergy;
                seam = newSeam;
            }
        }
        return seam;
    }

    private ArrayList<Integer> reachableColumns(int col, int width) {
        if (col < 0) throw new IllegalArgumentException("column cannot be less than zero");
        ArrayList<Integer> reachable = new ArrayList<>();
        if (col - 1 >= 0) reachable.add(col - 1);
        reachable.add(col);
        if (col + 1 < width) reachable.add(col + 1);
        return reachable;
    }

    // remove horizontal seam from current picture
    // public void removeHorizontalSeam(int[] seam)

    // remove vertical seam from current picture
    // public void removeVerticalSeam(int[] seam)

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

        double[][] transposedEnergy = sc.transposeMatrix();

        for (int row = 0; row < sc.width(); row++) {
            for (int col = 0; col < sc.height(); col++)
                // StdOut.printf("%9.0f ", sc.energy(col, row));
                StdOut.printf("%9.2f ", transposedEnergy[row][col]);
            StdOut.println();
        }

    }
}
