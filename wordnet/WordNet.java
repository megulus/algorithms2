/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Topological;

import java.util.HashMap;

public class WordNet {
    private int root;
    //private ArrayList<String> nouns = new ArrayList<>();
    private HashMap<String, Integer> nouns = new HashMap<>();
    private HashMap<Integer, String> vertices = new HashMap<>();
    private Digraph d;

    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null)
            throw new IllegalArgumentException("arguments to constructor cannot be null");
        In synIn = new In(synsets);
        In hypIn = new In(hypernyms);
        int synsetCount = 0;
        while (!synIn.isEmpty()) {
            String line = synIn.readLine();
            String[] output = line.split(",");
            int v = Integer.parseInt(output[0]);
            String synset = output[1];
            nouns.put(synset, v); // rong. Need to space separate output[1] to get nouns
            vertices.put(v, synset);
            synsetCount++;
        }
        d = new Digraph(synsetCount);
        while (!hypIn.isEmpty()) {
            String line = hypIn.readLine();
            String[] output = line.split(",");
            int v = Integer.parseInt(output[0]);
            for (int i = 1; i < output.length; i++) {
                // create edge v -> w, i.e., from v to its hypernym(s) output[i]
                d.addEdge(v, Integer.parseInt(output[i]));
            }
            // else if (output.length == 1) this.root = v;
        }
        // check whether valid roooted DAG: TODO - call Topological hasOrder() method if I can get it to work
        int outdegreeZeroCount = 0;
        for (int i = 0; i < synsetCount; i++) {
            int outdegree = d.outdegree(i);
            if (outdegree == 0) {
                this.root = i;
                outdegreeZeroCount++;
            }
        }
        if (outdegreeZeroCount != 1)
            throw new IllegalArgumentException("input to WordNet must be a valid rooted DAG");

        // StdOut.println(d.toString());
        StdOut.println("vertices " + d.V());
        StdOut.println("edges " + d.E());
        StdOut.println("root = " + this.root);
        StdOut.println("outdegree root " + d.outdegree(root));
        StdOut.println("indegree root " + d.indegree(root));
    }

    public Iterable<String> nouns() {
        return this.nouns.keySet();
    }

    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException("isNoun() argument cannot be null");
        if (this.nouns.get(word) != null) return true;
        return false;
    }

    // public int distance(String nounA, String nounB) {
    //     if (nounA == null || nounB == null)
    //         throw new IllegalArgumentException("arguments to distance function cannot be null");
    //     if (!isNoun(nounA) || !isNoun(nounB)) throw new IllegalArgumentException(
    //             "arguments to distance function must be WordNet nouns");
    //
    // }

    public String sap(String nounA, String nounB) {
        Topological topo = new Topological(this.d);
        StdOut.println("topo " + topo.hasOrder());
        StdOut.println("rank 4 " + topo.rank(4));
        return "nagpoke";
    }

    public static void main(String[] args) {
        String file1 = args[0];
        String file2 = args[1];
        WordNet wn = new WordNet(file1, file2);
        wn.sap("entity", "thing");
    }

}
