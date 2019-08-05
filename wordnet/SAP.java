/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdOut;

public class SAP {

    private Digraph G;
    private ModifiedBFS mbfs;

    public SAP(Digraph G) {
        if (G == null)
            throw new IllegalArgumentException("must provide a digraph argument to constructor");
        this.G = G;
        this.mbfs = new ModifiedBFS(this.G);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(Integer v, Integer w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("arguments to length cannot be null");
        return mbfs.sap(v, w);
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if none exists;
    public int ancestor(int v, int w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("arguments to ancestor cannot be null");
        return mbfs.ancestor(v, w);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("arguments to length cannot be null");
        int shortestPathSoFar = Integer.MAX_VALUE;
        for (Integer vertexA : v) {
            if (vertexA == null) throw new IllegalArgumentException("vertex cannot be null");
            for (Integer vertexB : w) {
                if (vertexB == null) throw new IllegalArgumentException("vertex cannot be null");
                int pathLength = mbfs.sap(vertexA, vertexB);
                if (pathLength < shortestPathSoFar) {
                    shortestPathSoFar = pathLength;
                }
            }
        }
        return shortestPathSoFar;
    }

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null || w == null)
            throw new IllegalArgumentException("arguments to ancestor cannot be null");
        int bestAncestorSoFar = -1;
        int shortestPathSoFar = Integer.MAX_VALUE;
        for (Integer vertexA : v) {
            if (vertexA == null) throw new IllegalArgumentException("vertex cannot be null");
            for (Integer vertexB : w) {
                if (vertexB == null) throw new IllegalArgumentException("vertex cannot be null");
                int pathLength = mbfs.sap(vertexA, vertexB);
                if (pathLength < shortestPathSoFar) {
                    shortestPathSoFar = pathLength;
                    bestAncestorSoFar = mbfs.ancestor(vertexA, vertexB);
                }
            }
        }
        return bestAncestorSoFar;
    }


    public static void main(String[] args) {
        In in = new In(args[0]);
        int v = Integer.parseInt(args[1]);
        int w = Integer.parseInt(args[2]);
        Digraph G = new Digraph(in);
        StdOut.println(" digraph: " + G.toString());
        SAP sap = new SAP(G);
        int length = sap.length(v, w);
        int ancestor = sap.ancestor(v, w);
        StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
    }

    private class ModifiedBFS {
        private static final int INFINITY = Integer.MAX_VALUE;

        private Digraph digraph;

        public ModifiedBFS(Digraph digraph) {
            this.digraph = digraph;
        }

        public int ancestor(int v, int w) {
            return bfs(v, w)[0];
        }

        public int sap(int v, int w) {
            return bfs(v, w)[1];
        }

        private int[] bfs(int v, int w) {
            validateVertex(v);
            validateVertex(w);
            Queue<Integer> qA = new Queue<>();
            Queue<Integer> qB = new Queue<>();

            int ancestor = -1;
            int sap = -1;

            boolean[] markedA = new boolean[digraph.V()];
            boolean[] markedB = new boolean[digraph.V()];
            int[] distToA = new int[digraph.V()];
            int[] distToB = new int[digraph.V()];
            for (int i = 0; i < digraph.V(); i++) {
                distToA[i] = INFINITY;
                distToB[i] = INFINITY;
            }


            markedA[v] = true;
            distToA[v] = 0;
            markedB[w] = true;
            distToB[w] = 0;
            qA.enqueue(v);
            qB.enqueue(w);
            while (!qA.isEmpty()) {
                int currentAVertex = qA.dequeue();
                for (int parentVertex : digraph.adj(currentAVertex)) {
                    if (!markedA[parentVertex]) {
                        distToA[parentVertex] = distToA[currentAVertex] + 1;
                        markedA[parentVertex] = true;
                        qA.enqueue(parentVertex);
                    }
                }
            }
            int shortestSoFar = INFINITY;
            while (!qB.isEmpty()) {

                int currentBVertex = qB.dequeue();
                for (int parentVertex : digraph.adj(currentBVertex)) {
                    if (!markedB[parentVertex]) {
                        distToB[parentVertex] = distToB[currentBVertex] + 1;
                        markedB[parentVertex] = true;
                        qB.enqueue(parentVertex);
                    }
                }
            }

            for (int i = 0; i < digraph.V(); i++) {
                if (markedA[i] && markedB[i]) {
                    int currentDistance = distToA[i] + distToB[i];
                    if (currentDistance < shortestSoFar) {
                        shortestSoFar = currentDistance;
                        ancestor = i;
                        sap = shortestSoFar;
                    }
                }

            }

            return new int[] { ancestor, sap };
        }

        private void validateVertex(Integer s) {
            if (s == null) throw new IllegalArgumentException("vertex cannot be null");
            if (s < 0 || s >= this.digraph.V()) throw new IllegalArgumentException(
                    "vertex " + s + " is not between 0 and " + (this.digraph.V() - 1));
        }

    }

}
