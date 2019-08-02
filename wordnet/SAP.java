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
        this.G = G;
        this.mbfs = new ModifiedBFS(this.G);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        return mbfs.sap(v, w);
    }

    // a common ancestor of v and w that participates in a shortest ancestral path; -1 if none exists;
    public int ancestor(int v, int w) {
        return mbfs.ancestor(v, w);
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    // public int length(Iterable<Integer> v, Iterable<Integer> w)

    // a common ancestor that participates in shortest ancestral path; -1 if no such path
    // public int ancestor(Iterable<Integer> v, Iterable<Integer> w)


    public static void main(String[] args) {
        In in = new In(args[0]);
        int v = Integer.parseInt(args[1]);
        int w = Integer.parseInt(args[2]);
        Digraph G = new Digraph(in);
        StdOut.println(" G: " + G.toString());
        // Topological topo = new Topological(G);
        // StdOut.println("topo hasOrder() " + topo.hasOrder());
        // StdOut.println("topo sort " + topo.order());
        SAP sap = new SAP(G);
        int length = sap.length(v, w);
        int ancestor = sap.ancestor(v, w);
        StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
    }

    private class ModifiedBFS {
        private static final int INFINITY = Integer.MAX_VALUE;

        private boolean[] markedA;
        private boolean[] markedB;

        private int[] distToA;
        private int[] distToB;

        private int ancestor = -1;
        private int sap = -1;

        private Digraph G;

        public ModifiedBFS(Digraph G) {
            this.G = G;
            markedA = new boolean[G.V()];
            markedB = new boolean[G.V()];
            distToA = new int[G.V()];
            distToB = new int[G.V()];
            for (int i = 0; i < G.V(); i++) {
                distToA[i] = INFINITY;
                distToB[i] = INFINITY;
            }
        }

        public int ancestor(int v, int w) {
            bfs(v, w);
            return this.ancestor;
        }

        public int sap(int v, int w) {
            bfs(v, w);
            return this.sap;
        }

        private void bfs(int v, int w) {
            validateVertex(v);
            validateVertex(w);
            Queue<Integer> qA = new Queue<>();
            Queue<Integer> qB = new Queue<>();
            markedA[v] = true;
            distToA[v] = 0;
            markedB[w] = true;
            distToB[w] = 0;
            qA.enqueue(v);
            qB.enqueue(w);
            while (!qA.isEmpty()) {
                int a = qA.dequeue();
                for (int n : G.adj(a)) {
                    if (!markedA[n]) {
                        distToA[n] = distToA[a] + 1;
                        markedA[n] = true;
                        qA.enqueue(n);
                    }
                }
            }
            int shortestSoFar = INFINITY;
            while (!qB.isEmpty()) {

                int b = qB.dequeue();
                for (int m : G.adj(b)) {
                    if (!markedB[m]) {
                        distToB[m] = distToB[b] + 1;
                        markedB[m] = true;
                        if (markedA[m]) {
                            if (distToA[m] + distToB[m] < shortestSoFar) {
                                shortestSoFar = distToA[m] + distToB[m];
                                this.ancestor = m;
                                this.sap = shortestSoFar;
                            }
                        }
                        qB.enqueue(m);
                    }
                }
            }
        }

        private void validateVertex(int s) {
            if (s < 0 || s >= this.G.V()) throw new IllegalArgumentException(
                    "vertex " + s + " is not between 0 and " + (this.G.V() - 1));
        }

    }

}
