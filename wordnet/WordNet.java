/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.HashMap;

public class WordNet {
    private int root;
    //private ArrayList<String> nouns = new ArrayList<>();
    private HashMap<String, Integer> nouns = new HashMap<>();

    public WordNet(String synsets, String hypernyms) {
        if (synsets == null || hypernyms == null)
            throw new IllegalArgumentException("arguments to constructor cannot be null");
        In synIn = new In(synsets);
        In hypIn = new In(hypernyms);
        int countWords = 0;
        while (!synIn.isEmpty()) {
            String line = synIn.readLine();
            String[] output = line.split(",");
            int v = Integer.parseInt(output[0]);
            String synset = output[1];
            nouns.put(synset, v);
            countWords++;
        }
        Digraph d = new Digraph(countWords);
        while (!hypIn.isEmpty()) {
            String line = hypIn.readLine();
            String[] output = line.split(",");
            int v = Integer.parseInt(output[0]);
            int w = Integer.parseInt(output[1]);
            d.addEdge(v, w);
        }
        StdOut.println(d.toString());
    }

    public Iterable<String> nouns() {
        return (Iterable<String>) this.nouns.keySet().iterator();
    }

    public boolean isNoun(String word) {
        if (word == null) throw new IllegalArgumentException("isNoun() argument cannot be null");
        if (this.nouns.get(word) != null) return true;
        return false;
    }

    public static void main(String[] args) {
        String file1 = args[0];
        String file2 = args[1];
        WordNet wn = new WordNet(file1, file2);
    }

}
