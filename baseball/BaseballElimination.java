/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;

public class BaseballElimination {

    private HashMap<Integer, int[]> remainingGames = new HashMap<>();
    private HashMap<Integer, int[]> currentRecord = new HashMap<>();
    private String[] teamNumToName;
    private int numTeams;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        In in = new In(filename);
        this.numTeams = Integer.parseInt(in.readLine());
        this.teamNumToName = new String[this.numTeams];
        int teamNumber = 0;
        while (!in.isEmpty()) {
            String[] line = in.readLine().split("\\s+");
            String name = line[0];
            int[] remainingLineup = new int[this.numTeams];
            int[] record = new int[3];
            teamNumToName[teamNumber] = name;
            record[0] = Integer.parseInt(line[1]);
            record[1] = Integer.parseInt(line[2]);
            record[2] = Integer.parseInt(line[3]);
            int next = 4;
            for (int i = 0; i < this.numTeams; i++) {
                remainingLineup[i] = Integer.parseInt(line[next]);
                next++;
            }
            this.remainingGames.put(teamNumber, remainingLineup);
            this.currentRecord.put(teamNumber, record);
            teamNumber++;
        }
    }

    private void testDataInput() {
        for (int i = 0; i < this.numTeams; i++) {
            StdOut.println();
            StdOut.println("team name: " + this.teamNumToName[i]);
            int[] record = currentRecord.get(i);
            int[] lineup = remainingGames.get(i);
            StdOut.println(" wins: " + record[0] + " losses: " + record[1] + " remaining games: "
                                   + record[2]);
            StdOut.println(" remaining games against: ");
            for (int j = 0; j < lineup.length; j++) {
                StdOut.print("  " + this.teamNumToName[j] + ": " + lineup[j]);
            }
            StdOut.println();
        }
        StdOut.println("all teams:");
        for (String team : teams()) StdOut.print(team + " ");
        StdOut.println();
    }

    // number of teams
    public int numberOfTeams() {
        return this.numTeams;
    }

    // all teams
    public Iterable<String> teams() {
        ArrayList<String> teams = new ArrayList<>();
        for (int i = 0; i < this.numTeams; i++) {
            teams.add(this.teamNumToName[i]);
        }
        return teams;
    }

    // number of wins for given team
    // public int wins(String team) {}

    // number of losses for given team
    // public int losses(String team) {}

    // number of remaining games for given team
    // public int remaining(String team) {}

    // number of remaining games between team1 and team2
    // public int against(String team1, String team2) {}

    // is given team eliminated?
    // public boolean isEliminated(String team) {}

    // subset R of teams that eliminates given team; null if not eliminated
    // public Iterable<String> certificateOfElimination(String team) {}

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        division.testDataInput();
        // for (String team : division.teams()) {
        //     if (division.isEliminated(team)) {
        //         StdOut.print(team + " is eliminated by the subset R = { ");
        //         for (String t : division.certificateOfElimination(team)) {
        //             StdOut.print(t + " ");
        //         }
        //         StdOut.println("}");
        //     }
        //     else {
        //         StdOut.println(team + " is not eliminated");
        //     }
        // }
    }
}
