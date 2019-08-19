/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Picture;

public class SeamCarver {

    Picture picture;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException(
                "must provide a Picture object to the SeamCarver constructor");
        this.picture = new Picture(picture);
    }

    // current picture
    public Picture picture() {
        return new Picture(this.picture);
    }

    // width of current picture
    public int width() {
        return this.picture.width();
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

    // sequence of indices for horizontal seam
    // public int[] findHorizontalSeam()

    // sequence of indices for vertical seam
    // public int[] findVerticalSeam()

    // remove horizontal seam from current picture
    // public void removeHorizontalSeam(int[] seam)

    // remove vertical seam from current picture
    // public void removeVerticalSeam(int[] seam)

    //  unit testing (optional)
    public static void main(String[] args) {

    }
}
