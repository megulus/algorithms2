/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {

    private WordNet wordnet;

    public Outcast(WordNet wordnet) {
        if (wordnet == null) throw new IllegalArgumentException(
                "must provide a WordNet object to Outcast constructor");
        this.wordnet = wordnet;
    }

    // given an array of WordNet nouns, return an outcast;
    public String outcast(String[] nouns) {
        if (nouns == null) throw new IllegalArgumentException("argument to outcast cannot be null");
        int outcastSoFar = -1;
        int longestDistanceSoFar = 0;
        for (int i = 0; i < nouns.length; i++) {
            int currentNounDistance = 0;
            for (int j = 0; j < nouns.length; j++) {
                if (!wordnet.isNoun(nouns[i]) || !wordnet.isNoun(nouns[j]))
                    throw new IllegalArgumentException("arguments must be nouns");
                int distance = wordnet.distance(nouns[i], nouns[j]);
                currentNounDistance += distance;
            }
            if (currentNounDistance > longestDistanceSoFar) {
                longestDistanceSoFar = currentNounDistance;
                outcastSoFar = i;
            }
        }
        return nouns[outcastSoFar];
    }

    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}
