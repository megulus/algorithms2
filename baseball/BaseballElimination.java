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
    private BaseballData divisionData;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        this.divisionData = new BaseballData(filename);
    }

    private void printTeamsData() {
        int numTeams = this.divisionData.numTeams;
        for (int i = 0; i < numTeams; i++) {
            StdOut.println();
            TeamData data = this.divisionData.teamData.get(i);
            StdOut.println("team name " + data.name());
            StdOut.println(
                    " wins: " + data.wins() + " losses: " + data.losses() + " remaining games: "
                            + data.remaining());
            StdOut.println("remaining games against: ");
            for (int j = 0; j < numTeams; j++) {
                StdOut.print(
                        "  " + this.divisionData.allTeams.get(j) + ": " + data.gamesAgainst(j));
            }
            StdOut.println();
        }
    }

    // number of teams
    // public int numberOfTeams() {}

    // all teams
    // public Iterable<String> teams() {}

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
        division.printTeamsData();
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

    private class BaseballData {
        private int numTeams;
        private ArrayList<String> allTeams = new ArrayList<>();
        private HashMap<Integer, TeamData> teamData = new HashMap<>();

        public BaseballData(String filename) {
            In in = new In(filename);
            this.numTeams = Integer.parseInt(in.readLine());
            int teamNumber = 0;
            while (!in.isEmpty()) {
                String[] line = in.readLine().split("\\s+");
                this.allTeams.add(line[0]);
                this.teamData.put(teamNumber, new TeamData(teamNumber, line, this.numTeams));
                teamNumber++;
            }
        }


    }

    private class TeamData {
        private String name;
        private int number;
        private int wins;
        private int losses;
        private int remaining;
        private HashMap<Integer, Integer> remainingAgainst = new HashMap<>();

        public TeamData(int number, String[] line, int numTeams) {
            this.name = line[0];
            this.number = number;
            this.wins = Integer.parseInt(line[1]);
            this.losses = Integer.parseInt(line[2]);
            this.remaining = Integer.parseInt(line[3]);
            int next = 4;
            for (int i = 0; i < numTeams; i++) {
                remainingAgainst.put(i, Integer.parseInt(line[next]));
                next++;
            }
        }

        public int number() {
            return this.number;
        }

        public String name() {
            return this.name;
        }

        public int wins() {
            return this.wins;
        }

        public int losses() {
            return this.losses;
        }

        public int remaining() {
            return this.remaining;
        }

        public int gamesAgainst(int teamNumber) {
            return this.remainingAgainst.get(teamNumber);
        }
    }
}
