/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        return this.divisionData.teamNames();
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
        // before creating a flow network for the team in question,
        // we first determine whether the team is trivially eliminated:
        if (isTriviallyEliminated(team)) {
            StdOut.println(team + " is trivially eliminated");
            return true;
        }
        StdOut.println("flow network for team " + team);
        FlowNetwork flowNetwork = buildFlowNetwork(team);
        StdOut.println(flowNetwork.toString());
        return false;
    }

    private boolean isTriviallyEliminated(String teamName) {
        Team team = this.divisionData.getTeam(teamName);
        int wx = team.wins();
        int rx = team.remaining();
        for (Team i : this.divisionData.opponents(teamName)) {
            int wi = i.wins();
            if (wx + rx < wi) {
                return true;
            }
        }
        return false;
    }


    // subset R of teams that eliminates given team; null if not eliminated
    // public Iterable<String> certificateOfElimination(String team) {}

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        int numberOfTeams = division.numberOfTeams();
        StdOut.println("numberOfTeams: " + numberOfTeams);
        // print all division data
        // division.divisionData.printDivisionData();


        for (String t : division.teams()) {
            division.isEliminated(t);
            // if (division.isEliminated(team)) {
            //     StdOut.print(team + " is eliminated by the subset R = { ");
            //     for (String t : division.certificateOfElimination(team)) {
            //         StdOut.print(t + " ");
            //     }
            //     StdOut.println("}");
            // }
            // else {
            //     StdOut.println(team + " is not eliminated");
            // }
        }
    }

    // PRIVATE HELPER METHODS FOR BaseballElimination CLASS

    // build a FlowNetwork for the given elimination team
    private FlowNetwork buildFlowNetwork(String eliminationTeam) {
        int teamNumber = this.divisionData.teamNames().indexOf(eliminationTeam);
        Team eliminated = this.divisionData.numberToTeamMap().get(teamNumber);
        VertexTracker vertexTracker = new VertexTracker(this.divisionData, eliminated);
        int totalVertices = vertexTracker.getTotalVertices();
        Vertex start = vertexTracker.getVerticesByType(Type.START).get(0);
        Vertex end = vertexTracker.getVerticesByType(Type.END).get(0);
        List<Vertex> gameVertices = vertexTracker.getVerticesByType(Type.GAME);
        List<Vertex> teamVertices = vertexTracker.getVerticesByType(Type.TEAM);
        HashMap<Integer, Vertex> teamNumberToTeamVertexMap = vertexTracker.getTeamVertexMap();
        FlowNetwork fn = new FlowNetwork(totalVertices);
        for (Vertex g : gameVertices) {
            FlowEdge startToGame = new FlowEdge(start.number(), g.number(),
                                                g.team1().gamesAgainst(g.team2()));
            // get the two vertices for the game matchup represented by vertex g:
            Vertex teamVertex1 = teamNumberToTeamVertexMap.get(g.team1().number());
            Vertex teamVertex2 = teamNumberToTeamVertexMap.get(g.team2().number());
            FlowEdge gameToTeam1 = new FlowEdge(g.number(), teamVertex1.number(),
                                                Double.POSITIVE_INFINITY);
            FlowEdge gameToTeam2 = new FlowEdge(g.number(), teamVertex2.number(),
                                                Double.POSITIVE_INFINITY);
            fn.addEdge(startToGame);
            fn.addEdge(gameToTeam1);
            fn.addEdge(gameToTeam2);
        }
        // add FlowEdge from each team vertex (w) to end vertex (t)
        int wx = eliminated.wins();
        int rx = eliminated.remaining();
        for (Vertex i : teamVertices) {
            int wi = i.team1().wins();
            // capacity, = Wx + Rx - Wi, where x = elimination team
            StdOut.println("wins x: " + wx + " remaining x: " + rx + " wins i: " + wi);
            double capacity = wx + rx - wi;
            StdOut.println("capacity " + capacity);
            FlowEdge teamToEnd = new FlowEdge(i.number(), end.number(), capacity);
            fn.addEdge(teamToEnd);
        }
        return fn;
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

        public int number() {
            return this.number;
        }

        public Type type() {
            return this.type;
        }

        public Team team1() {
            return this.team1;
        }

        public Team team2() {
            return this.team2;
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
        private HashMap<Integer, Vertex> teamVertexMap = new HashMap<>();


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
                    allVertices.add(new Vertex(0, Type.START, null, null));
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
                            Vertex t = new Vertex(this.currentVertexNumber, Type.TEAM, team,
                                                  null);
                            allVertices.add(t);
                            teamVertexMap.put(team.number(), t);
                            this.currentVertexNumber += 1;
                        }
                    }
                }

                if (this.currentVertexNumber == pascal() + n) {
                    allVertices.add(new Vertex(this.currentVertexNumber, Type.END, null, null));
                }
            }
        }

        // TODO: is there a way to make this immutable?
        public HashMap<Integer, Vertex> getTeamVertexMap() {
            return this.teamVertexMap;
        }

        // get vertices by type
        public List<Vertex> getVerticesByType(Type type) {
            ArrayList<Vertex> subset = new ArrayList<>();
            for (Vertex v : this.allVertices) {
                if (v.type == type) {
                    subset.add(v);
                }
            }
            return Collections.unmodifiableList(subset);
        }

        public List<Vertex> getAllVertices() {
            return Collections.unmodifiableList(this.allVertices);
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
        private HashMap<Integer, Team> teamNumberToTeamMap = new HashMap<>();
        private HashMap<String, Integer> teamNameToNumberMap = new HashMap<>();

        public DivisionData(String filename) {
            In in = new In(filename);
            this.numTeams = Integer.parseInt(in.readLine());
            int teamNumber = 0;
            while (!in.isEmpty()) {
                String[] line = in.readLine().split("\\s+");
                this.allTeams.add(line[0]);
                this.teamNumberToTeamMap.put(teamNumber, new Team(teamNumber, line, this.numTeams));
                this.teamNameToNumberMap.put(line[0], teamNumber);
                teamNumber++;
            }
        }

        public int numberOfTeams() {
            return this.numTeams;
        }

        public ArrayList<String> teamNames() {
            return this.allTeams;
        }

        public HashMap<Integer, Team> numberToTeamMap() {
            return this.teamNumberToTeamMap;
        }

        public int getTeamNumber(String teamName) {
            return this.teamNameToNumberMap.get(teamName);
        }

        public Team getTeam(String teamName) {
            int teamNumber = this.teamNameToNumberMap.get(teamName);
            return this.teamNumberToTeamMap.get(teamNumber);
        }

        public ArrayList<Team> opponents(String teamName) {
            ArrayList<Team> opponents = new ArrayList<>();
            for (String name : this.allTeams) {
                if (name != teamName) {
                    opponents.add(this.getTeam(name));
                }
            }
            return opponents;
        }

        public int wins(String name) {
            Team team = teamNumberToTeamMap.get(this.allTeams.indexOf(name));
            return team.wins();
        }

        public void printDivisionData() {
            for (int i = 0; i < this.allTeams.size(); i++) {
                Team team = this.teamNumberToTeamMap.get(i);
                StdOut.println(
                        " team name: " + team.name() + " wins: " + team.wins() + " losses: " + team
                                .losses());
                StdOut.println("  games remaining against: ");
                for (int j = 0; j < this.allTeams.size(); j++) {
                    Team opponent = this.teamNumberToTeamMap.get(j);
                    StdOut.println("   " + opponent.name() + ": " + team.gamesAgainst(opponent));
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

        public int gamesAgainst(Team team) {
            int teamNumber = team.number();
            return this.remainingAgainst.get(teamNumber);
        }
    }
}
