/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;

public class BaseballElimination {
    private DivisionData divisionData;
    private FlowNetwork flowNetwork;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        this.divisionData = new DivisionData(filename);
        this.flowNetwork = new FlowNetwork((this.divisionData.numberOfTeams() * 2) + 2);
    }

    // number of teams
    public int numberOfTeams() {
        return this.divisionData.numberOfTeams();
    }

    // all teams
    public Iterable<String> teams() {
        return this.divisionData.allTeams();
    }

    // number of wins for given team
    public int wins(String team) {
        return divisionData.wins(team);
    }

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
        BaseballElimination be = new BaseballElimination(args[0]);
        int numberOfTeams = be.numberOfTeams();
        StdOut.println("numberOfTeams: " + numberOfTeams);
        StdOut.println("running DivisionData class printDivisionData() method:");
        be.divisionData.printDivisionData();


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

    enum Type {
        START, GAME, TEAM, END
    }

    private class Vertex {
        int number;
        Type type;
        Team team1 = null;
        Team team2 = null;

        public Vertex(int number, Type type, Team team1, Team team2) {
            this.number = number;
            this.type = type;
            if (team1 != null) this.team1 = team1;
            if (team2 != null) this.team2 = team2;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Vertex)) return false;

            Vertex v = (Vertex) o;

            return v.type.equals(this.type) && v.team1.equals(this.team1) && v.team2
                    .equals(this.team2);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * (result + this.type.hashCode());
            result = 31 * (result + this.team1.hashCode());
            result = 31 * (result + this.team2.hashCode());
            return result;
        }

    }

    private class VertexTracker {
        HashMap<Vertex, Integer> VtoI;
        HashMap<Integer, Vertex> ItoV;
        int n;
        int totalVertices;

        public VertexTracker(int numberOfTeams) {
            this.n = numberOfTeams;
            this.totalVertices = getTotalVertices();
        }

        public int getTotalVertices() {
            return (((this.n - 1) * (this.n - 2)) / 2) + (this.n - 1);
        }

        // translate from vertex to integer

        // translate from integer to vertex
    }

    private class DivisionData {
        private int numTeams;
        private ArrayList<String> allTeams = new ArrayList<>();
        private HashMap<Integer, Team> numberToTeamMap = new HashMap<>();

        public DivisionData(String filename) {
            In in = new In(filename);
            this.numTeams = Integer.parseInt(in.readLine());
            int teamNumber = 0;
            while (!in.isEmpty()) {
                String[] line = in.readLine().split("\\s+");
                this.allTeams.add(line[0]);
                this.numberToTeamMap.put(teamNumber, new Team(teamNumber, line, this.numTeams));
                teamNumber++;
            }
        }

        public int numberOfTeams() {
            return this.numTeams;
        }

        public ArrayList<String> allTeams() {
            return this.allTeams;
        }

        public int wins(String name) {
            Team team = numberToTeamMap.get(this.allTeams.indexOf(name));
            return team.wins();
        }

        private void printDivisionData() {
            for (int i = 0; i < this.allTeams.size(); i++) {
                Team team = this.numberToTeamMap.get(i);
                StdOut.println(
                        " team name: " + team.name() + " wins: " + team.wins() + " losses: " + team
                                .losses());
                StdOut.println("  games remaining against: ");
                for (int j = 0; j < this.allTeams.size(); j++) {
                    String opponent = this.allTeams.get(j);
                    StdOut.println("   " + opponent + ": " + team.gamesAgainst(j));
                }
                StdOut.println();
            }
        }

    }

    private class Team {
        private String name;
        private int number;
        private int wins;
        private int losses;
        private int remaining;
        private HashMap<Integer, Integer> remainingAgainst = new HashMap<>();

        public Team(int number, String[] line, int numTeams) {
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
