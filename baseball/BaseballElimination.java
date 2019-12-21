/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BaseballElimination {
    private final DivisionData divisionData;
    private final HashMap<String, VertexTracker> teamVertexTracker = new HashMap<>();
    private final HashMap<String, FordFulkerson> teamFordFulkerson = new HashMap<>();
    private final HashMap<String, Boolean> triviallyEliminated = new HashMap<>();
    private final HashMap<String, ArrayList<String>> eliminationSets = new HashMap<>();


    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        this.divisionData = new DivisionData(filename);
        ArrayList<String> allTeamNames = this.divisionData.teamNames();
        for (String teamName : allTeamNames) {
            if (!isTriviallyEliminated(teamName)) {
                triviallyEliminated.put(teamName, false);
                buildFlowNetwork(teamName);
            }
            else {
                triviallyEliminated.put(teamName, true);
                calculateEliminationSet(teamName);
            }
        }
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
        if (!this.divisionData.teamNames().contains(team)) {
            throw new IllegalArgumentException("Invalid team name");
        }
        return divisionData.wins(team);
    }

    // number of losses for given team
    public int losses(String team) {
        if (!this.divisionData.teamNames().contains(team)) {
            throw new IllegalArgumentException("Invalid team name");
        }
        Team t = this.divisionData.getTeam(team);
        return t.losses();
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (!this.divisionData.teamNames().contains(team)) {
            throw new IllegalArgumentException("Invalid team name");
        }
        Team t = this.divisionData.getTeam(team);
        return t.remaining();
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (!this.divisionData.teamNames().contains(team1) || !this.divisionData.teamNames()
                                                                                .contains(team2)) {
            throw new IllegalArgumentException("Invalid team name");
        }
        Team t1 = this.divisionData.getTeam(team1);
        int team2Number = this.divisionData.getTeamNumber(team2);
        return t1.gamesAgainst(team2Number);
    }

    // is given team eliminated?
    public boolean isEliminated(String teamName) {
        if (!this.divisionData.teamNames().contains(teamName)) {
            throw new IllegalArgumentException("Invalid team name");
        }
        boolean isTriviallyEliminated = this.triviallyEliminated.get(teamName);
        if (isTriviallyEliminated) {
            return true;
        }
        else if (this.eliminationSets.containsKey(teamName)) {
            return true;

        }
        return false;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (!this.divisionData.teamNames().contains(team)) {
            throw new IllegalArgumentException("Invalid team name");
        }
        ArrayList<String> cert = this.eliminationSets.get(team);
        if (isValidCertificate(cert, team)) {
            return cert;
        }
        return null;
    }

    // a(R) = [w(R) + g(R)] / |R|
    // in a valid certificate, a(R) > max # games that eliminated team can win
    private boolean isValidCertificate(ArrayList<String> certificate, String teamName) {
        if (certificate == null || certificate.isEmpty()) {
            return false;
        }
        int wR = 0;
        int gR = 0;
        int r = certificate.size();
        Team elim = this.divisionData.getTeam(teamName);
        int maxElimWins = elim.wins() + elim.remaining();
        for (int i = 0; i < certificate.size(); i++) {
            Team teamI = this.divisionData.getTeam(certificate.get(i));
            wR += teamI.wins();
            for (int j = i + 1; j < certificate.size(); j++) {
                Team teamJ = this.divisionData.getTeam(certificate.get(j));
                gR += teamI.gamesAgainst(teamJ.number());
            }
        }
        double aR = ((wR + gR)) * 1.0 / r;
        if (aR > maxElimWins) {
            return true;
        }
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

    private void calculateEliminationSet(String teamName) {
        boolean isTriviallyEliminated = this.triviallyEliminated.get(teamName);
        if (isTriviallyEliminated) {
            calculateTrivialEliminationSet(teamName);
        }
        else {
            calculateFordFulkersonEliminationSet(teamName);
        }
    }

    private void calculateTrivialEliminationSet(String teamName) {
        Team team = this.divisionData.getTeam(teamName);
        ArrayList<String> eliminationSet = new ArrayList<>();
        int wx = team.wins();
        int rx = team.remaining();
        for (Team i : this.divisionData.opponents(teamName)) {
            int wi = i.wins();
            if (wx + rx < wi) {
                eliminationSet.add(i.name());
            }
        }
        if (isValidCertificate(eliminationSet, teamName)) {
            this.eliminationSets.put(teamName, eliminationSet);
        }

    }

    private void calculateFordFulkersonEliminationSet(String teamName) {
        FordFulkerson fordFulkerson = this.teamFordFulkerson.get(teamName);
        VertexTracker vertexTracker = this.teamVertexTracker.get(teamName);
        List<Vertex> teamVertices = vertexTracker.getVerticesByType(Type.TEAM);
        ArrayList<String> mincut = new ArrayList<>();
        for (Vertex v : teamVertices) {
            if (fordFulkerson.inCut(v.number())) {
                mincut.add(v.team1().name());
            }
        }
        if (isValidCertificate(mincut, teamName)) {
            this.eliminationSets.put(teamName, mincut);
        }
    }


    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        int numberOfTeams = division.numberOfTeams();
        StdOut.println("numberOfTeams: " + numberOfTeams);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }

    // PRIVATE HELPER METHODS FOR BaseballElimination CLASS

    // build a FlowNetwork for the given elimination team
    // capacity, = Wx + Rx - Wi, where x = elimination team
    // Intuition: capacity here is the maximum wins that
    // team i can have and still allow x to win division -
    // i.e., it's a bottleneck
    private void buildFlowNetwork(String eliminationTeam) {
        int teamNumber = this.divisionData.teamNames().indexOf(eliminationTeam);
        Team eliminated = this.divisionData.numberToTeamMap().get(teamNumber);
        VertexTracker vertexTracker = new VertexTracker(this.divisionData, eliminated);
        this.teamVertexTracker.put(eliminationTeam, vertexTracker);
        int totalVertices = vertexTracker.getTotalVertices();
        Vertex start = vertexTracker.getVerticesByType(Type.START).get(0);
        Vertex end = vertexTracker.getVerticesByType(Type.END).get(0);
        List<Vertex> gameVertices = vertexTracker.getVerticesByType(Type.GAME);
        List<Vertex> teamVertices = vertexTracker.getVerticesByType(Type.TEAM);
        HashMap<Integer, Vertex> teamNumberToTeamVertexMap = vertexTracker.getTeamVertexMap();
        FlowNetwork fn = new FlowNetwork(totalVertices);

        for (Vertex g : gameVertices) {
            FlowEdge startToGame = new FlowEdge(start.number(), g.number(),
                                                g.team1().gamesAgainst(g.team2().number()));
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
            double capacity = wx + rx - wi;
            FlowEdge teamToEnd = new FlowEdge(i.number(), end.number(), capacity);
            fn.addEdge(teamToEnd);
        }
        FordFulkerson ff = new FordFulkerson(fn, start.number(), end.number());
        this.teamFordFulkerson.put(eliminationTeam, ff);
        calculateEliminationSet(eliminationTeam);
    }

    // HELPER CLASSES BELOW

    private enum Type {
        START, GAME, TEAM, END
    }

    private class Vertex {
        private int number;
        private Type type;
        private Team team1 = null;
        private Team team2 = null;

        public Vertex(int number, Type type, Team team1, Team team2) {
            this.number = number;
            this.type = type;
            if (team1 != null) this.team1 = team1;
            if (team2 != null) this.team2 = team2;
        }

        public int number() {
            return this.number;
        }

        public Team team1() {
            return this.team1;
        }

        public Team team2() {
            return this.team2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (!(obj.getClass().equals(this.getClass()))) return false;

            Vertex v = (Vertex) obj;

            return v.type == this.type && ((v.team1.equals(this.team1) && v.team2
                    .equals(this.team2)) || (v.team1.equals(this.team2) && v.team2
                    .equals(this.team1)));
        }

        @Override
        public int hashCode() {
            int result = 17;
            result += 31 * (result + this.type.hashCode());
            result += 31 * (result + this.team1.hashCode());
            result += 31 * (result + this.team2.hashCode());
            return result;
        }

    }

    private class VertexTracker {
        private final int n; // number of teams
        private final int totalVertices;
        private final Team eliminationTeam;
        private final ArrayList<Vertex> allVertices = new ArrayList<>();
        private final DivisionData divisionData;
        private int currentVertexNumber = 0;
        private final HashMap<Integer, Vertex> teamVertexMap = new HashMap<>();


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

    }

    private class DivisionData {
        private final int numTeams;
        private final ArrayList<String> allTeams = new ArrayList<>();
        private final HashMap<Integer, Team> teamNumberToTeamMap = new HashMap<>();
        private final HashMap<String, Integer> teamNameToNumberMap = new HashMap<>();

        public DivisionData(String filename) {
            In in = new In(filename);
            this.numTeams = Integer.parseInt(in.readLine());
            int teamNumber = 0;
            while (!in.isEmpty()) {
                String line = in.readLine().trim();
                // StdOut.println("line: " + line);
                String[] lineSplit = line.split("\\s+");
                this.allTeams.add(lineSplit[0]);
                this.teamNumberToTeamMap
                        .put(teamNumber, new Team(teamNumber, lineSplit, this.numTeams));
                this.teamNameToNumberMap.put(lineSplit[0], teamNumber);
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
                if (!name.equals(teamName)) {
                    opponents.add(this.getTeam(name));
                }
            }
            return opponents;
        }

        public int wins(String name) {
            Team team = teamNumberToTeamMap.get(this.allTeams.indexOf(name));
            return team.wins();
        }

    }

    private class Team {
        private final String name;
        private final int number;
        private final int wins;
        private final int losses;
        private final int remaining;
        private final HashMap<Integer, Integer> remainingAgainst = new HashMap<>();

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
