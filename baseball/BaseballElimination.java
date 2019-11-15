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
    // private VertexTracker vertexTracker;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        this.divisionData = new DivisionData(filename);

        // this.flowNetwork = new FlowNetwork((this.divisionData.numberOfTeams() * 2) + 2);
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
    public boolean isEliminated(String team) {
        int teamNumber = this.divisionData.allTeams().indexOf(team);
        Team eliminated = this.divisionData.numberToTeamMap().get(teamNumber);
        VertexTracker vertexTracker = new VertexTracker(this.divisionData, eliminated);


        return false;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    // public Iterable<String> certificateOfElimination(String team) {}

    public static void main(String[] args) {
        BaseballElimination be = new BaseballElimination(args[0]);
        int numberOfTeams = be.numberOfTeams();
        StdOut.println("numberOfTeams: " + numberOfTeams);


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

    // HELPER CLASSES BELOW

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

            return v.type.equals(this.type) && ((v.team1.equals(this.team1) && v.team2
                    .equals(this.team2)) || (v.team1.equals(this.team2) && v.team2
                    .equals(this.team1)));
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
        private HashMap<Vertex, Integer> VtoI;
        private HashMap<Integer, Vertex> ItoV;
        private int n; // number of teams
        private int totalVertices;
        private Team eliminationTeam;
        private ArrayList<Vertex> allVertices = new ArrayList<>();
        private DivisionData divisionData;
        private int currentVertexNumber = 0;


        public VertexTracker(DivisionData divisionData, Team isEliminated) {
            this.divisionData = divisionData;
            this.n = this.divisionData.numberOfTeams();
            this.totalVertices = calculateTotalVertices();
            this.eliminationTeam = isEliminated;
            addVertices();
        }


        public void addVertices() {
            while (this.currentVertexNumber < pascal() + n) {

                if (this.currentVertexNumber == 0) {
                    Vertex v = new Vertex(0, Type.START, null, null);
                    allVertices.add(v);
                    this.currentVertexNumber += 1;
                }

                if (this.currentVertexNumber >= 1 && this.currentVertexNumber <= pascal()) {
                    for (int j = 0; j < n; j++) {
                        for (int k = j + 1; k < n; k++) {
                            if (isValidMatchup(j, k)) {
                                Team team1 = this.divisionData.numberToTeamMap().get(j);
                                Team team2 = this.divisionData.numberToTeamMap().get(k);
                                allVertices
                                        .add(new Vertex(this.currentVertexNumber, Type.GAME, team1,
                                                        team2));
                                this.currentVertexNumber += 1;
                            }
                        }
                    }
                }

                if (this.currentVertexNumber >= pascal() + 1
                        && this.currentVertexNumber <= pascal() + (
                        n - 1)) {
                    for (int j = 0; j < n; j++) {
                        if (j != this.eliminationTeam.number()) {
                            Team team = this.divisionData.numberToTeamMap().get(j);
                            allVertices.add(new Vertex(this.currentVertexNumber, Type.TEAM, team,
                                                       null));
                            this.currentVertexNumber += 1;
                        }
                    }
                }

                if (this.currentVertexNumber == pascal() + n) {
                    allVertices.add(new Vertex(this.currentVertexNumber, Type.END, null, null));
                }
            }
        }

        private boolean isValidMatchup(int first, int second) {
            return first != this.eliminationTeam.number() && second != this.eliminationTeam
                    .number();
        }

        private int pascal() {
            return (((this.n - 1) * (this.n - 2)) / 2);
        }

        private int calculateTotalVertices() {
            return pascal() + (this.n - 1) + 2;
        }

        public int getTotalVertices() {
            return this.totalVertices;
        }

        private void printVertices() {
            StdOut.println("elimination team: " + this.eliminationTeam.name() + " team number: "
                                   + this.eliminationTeam.number());
            for (Vertex v : this.allVertices) {
                StdOut.println("vertex number: " + v.number + " type: " + v.type);
                if (v.type == Type.GAME) {
                    StdOut.println(
                            "  team1: " + v.team1.number() + " " + v.team1.name() + " team2: "
                                    + v.team2
                                    .number() + " " + v.team2.name());
                }
                if (v.type == Type.TEAM) {
                    StdOut.println("  team: " + v.team1.number() + " " + v.team1.name());
                }
            }
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

        public HashMap<Integer, Team> numberToTeamMap() {
            return this.numberToTeamMap;
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
