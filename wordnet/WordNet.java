/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */


import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class WordNet {
    private int root;
    private HashMap<String, Set<Integer>> nounToVertices = new HashMap<>();
    private HashMap<Integer, String> vertexToSynset = new HashMap<>();
    private Digraph digraph;
    private SAP sap;

    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null)
            throw new IllegalArgumentException("arguments to constructor cannot be null");
        In synIn = new In(synsets);
        In hypIn = new In(hypernyms);
        int synsetCount = 0;
        while (!synIn.isEmpty()) {
            String line = synIn.readLine();
            String[] output = line.split(",");
            int vertex = Integer.parseInt(output[0]);
            String synset = output[1];
            String[] words = output[1].split(" ");
            for (String word : words) {
                Set<Integer> verticesForWord = nounToVertices.get(word);
                if (verticesForWord == null) {
                    verticesForWord = new HashSet<Integer>();
                    nounToVertices.put(word, verticesForWord);
                }
                verticesForWord.add(vertex);
            }
            vertexToSynset.put(vertex, synset);
            synsetCount++;
        }
        digraph = new Digraph(synsetCount);
        while (!hypIn.isEmpty()) {
            String line = hypIn.readLine();
            String[] output = line.split(",");
            int v = Integer.parseInt(output[0]);
            for (int i = 1; i < output.length; i++) {
                // create edge v -> w, i.e., from v to its hypernym(s) output[i]
                digraph.addEdge(v, Integer.parseInt(output[i]));
            }
            // else if (output.length == 1) this.root = v;
        }
        // check whether valid roooted DAG: TODO - call Topological hasOrder() method if I can get it to work
        int outdegreeZeroCount = 0;
        for (int i = 0; i < synsetCount; i++) {
            int outdegree = digraph.outdegree(i);
            if (outdegree == 0) {
                this.root = i;
                outdegreeZeroCount++;
            }
        }
        if (outdegreeZeroCount != 1)
            throw new IllegalArgumentException("input to WordNet must be a valid rooted DAG");
        sap = new SAP(this.digraph);
        StdOut.println(digraph.toString());
        StdOut.println("vertexToSynset " + digraph.V());
        StdOut.println("edges " + digraph.E());
        StdOut.println("root = " + this.root);
        StdOut.println("outdegree root " + digraph.outdegree(root));
        StdOut.println("indegree root " + digraph.indegree(root));
        StdOut.println("nounToVertices " + nouns());
    }

    public Iterable<String> nouns() {
        return this.nounToVertices.keySet();
    }

    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException("isNoun() argument cannot be null");
        if (this.nounToVertices.get(word) != null) return true;
        return false;
    }


    // return distance between nounA and nounB
    public int distance(String nounA, String nounB) {
        if (nounA == null || nounB == null)
            throw new IllegalArgumentException("arguments to distance function cannot be null");
        if (!isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException(
                "arguments to distance function must be WordNet nounToVertices");
        Set<Integer> verticesForNounA = nounToVertices.get(nounA);
        Set<Integer> verticesForNounB = nounToVertices.get(nounB);
        return sap.length(verticesForNounA, verticesForNounB);
    }

    // return a synset (second field of sysets.txt( that is the common acestor
    // of nounA and nounB in a shortest ancestral path
    public String sap(String nounA, String nounB) {
        Set<Integer> verticesForNounA = nounToVertices.get(nounA);
        Set<Integer> verticesForNounB = nounToVertices.get(nounB);
        int bestAncestorSoFar = sap.ancestor(verticesForNounA, verticesForNounB);
        return vertexToSynset.get(bestAncestorSoFar);
    }

    public static void main(String[] args) {
        String file1 = args[0];
        String file2 = args[1];
        WordNet wn = new WordNet(file1, file2);
        StdOut.println(wn.sap("o", "c"));
        StdOut.println(wn.distance("d", "e"));
    }

}
