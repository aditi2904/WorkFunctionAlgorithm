import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Main {

    public static final int startVertex = 1;
    public static final String defaultConfiguration = "123";
    public static int graphArray[][];
    public static int noofVertex = 9;
    public static ArrayList<String> serverConfigurations = new ArrayList<>();
    public static int greedyOutput[][];
    public static int workFunctionOutput[][];
    public static int totalDistance = 0;
    public static ArrayList<Edge> edges = new ArrayList<>();

    public static void main(String[] args) {
        // write your code here
        try {
            generateRandomEdges();
            graphArray = new int[10][10];

            for(int i = 0; i<graphArray.length; i++) {
                for(int j = 0; j<graphArray[0].length; j++) {
                    graphArray[i][j] = 0;
                }
            }

            for(int ii = 0; ii<edges.size(); ii++) {
                int u = edges.get(ii).getU();
                int v = edges.get(ii).getV();
                if(u != v) {
                    graphArray[u][v] = 1;
                    graphArray[v][u] = 1;
                }
            }

            System.out.println("Done");
            // calculate the shortest distances of array.
            int[][] distanceArray = computeDistances(graphArray);
            for (int i = 1; i < distanceArray.length; i++) {
                for (int j = 1; j < distanceArray[0].length; j++) {
                    System.out.print(distanceArray[i][j] + " ");
                }
                System.out.print("\n");
            }

            // Generate random input requests.
            ArrayList<Integer> request = generateInputRequest(noofVertex, 50);

            int[] vertexArray = new int[noofVertex];
            for (int i = 0; i < noofVertex; i++) {
                vertexArray[i] = i + 1;
            }
            boolean[] b = new boolean[noofVertex];

            // Generate possible configurations.
            generateIntialConfigurations(vertexArray, 4, 0, 0, b);

            // compute greedy method
            greedyOutput = new int[50][serverConfigurations.size()];
            for (int i = 0; i < serverConfigurations.size(); i++) {
                computeGreedy(distanceArray, request, serverConfigurations.get(i));
            }

            calculateWorkFunction(request,distanceArray);

            // To calculate the opt value;
            generateOptValueOfTheWorkFunction();

        } catch (Exception e) {
            System.out.println("Cannot load the file.");
            e.printStackTrace();
            return;
        }
    }

    public static void generateRandomEdges() {
        Random r = new Random();
        for(int i = 0; i<200; i++) {
            edges.add(new Edge(r.nextInt(9) + 1,r.nextInt(9) + 1));
        }
    }

    /**
     * Function to calculate shortest distances.
     *
     * @param graphArray
     * @return
     */
    public static int[][] computeDistances(int[][] graphArray) {

        int distanceArray[][] = new int[graphArray.length][graphArray[0].length];
        Arrays.fill(distanceArray[0], 1);

        for (int i = 0; i < graphArray.length; i++) {
            int[] distance = runDijkstra(i, graphArray);
            for (int j = 0; j < distance.length; j++) {
                distanceArray[i][j] = distance[j];
            }
        }
        return distanceArray;
    }

    /**
     * Dijkstra Algorithm to calculate shortest distances.
     *
     * @param source
     * @param graphArray
     * @return
     */
    public static int[] runDijkstra(int source, int[][] graphArray) {
        int V = graphArray.length;
        int dist[] = new int[V];
        Boolean sptSet[] = new Boolean[V];
        for (int i = 1; i < V; i++) {
            dist[i] = Integer.MAX_VALUE;
            sptSet[i] = false;
        }
        dist[source] = 0;
        for (int count = 0; count < V - 1; count++) {
            int u = minDistance(dist, sptSet);
            sptSet[u] = true;
            for (int v = 1; v < V; v++) {

                if (!sptSet[v] && graphArray[u][v] != 0 && dist[u] != Integer.MAX_VALUE && dist[u] + graphArray[u][v] < dist[v]) {
                    dist[v] = dist[u] + graphArray[u][v];
                }
            }
        }
        return dist;
    }

    /**
     * Helper function to calculate shortest distances.
     *
     * @param dist
     * @param sptSet
     * @return
     */
    public static int minDistance(int dist[], Boolean sptSet[]) {
        int V = dist.length;
        // Initialize min value
        int min = Integer.MAX_VALUE, min_index = -1;

        for (int v = 1; v < V; v++)
            if (sptSet[v] == false && dist[v] <= min) {
                min = dist[v];
                min_index = v;
            }

        return min_index;
    }

    /**
     * To generate random input requests.
     *
     * @param noofVertices
     * @param noOfRequests
     * @return
     */
    public static ArrayList<Integer> generateInputRequest(int noofVertices, int noOfRequests) {

        ArrayList<Integer> requests = new ArrayList<>();
        int max = noofVertices;
        int min = 1;
        int range = max - min + 1;
        for (int i = 0; i < noOfRequests; i++) {
            requests.add((int) (Math.random() * range) + min);
        }
        return requests;
    }

    /**
     * To generate all possible server configurations.
     *
     * @param vertexArray
     * @param configurationlength
     * @param start
     * @param currLen
     * @param used
     */
    public static void generateIntialConfigurations(int[] vertexArray, int configurationlength, int start
            , int currLen, boolean[] used) {

        if (currLen == configurationlength) {
            String combination = "";
            for (int i = 0; i < vertexArray.length; i++) {
                if (used[i] == true) {
                    combination = combination + Integer.toString(vertexArray[i]);
                }
            }
            System.out.println(combination.trim());
            serverConfigurations.add(combination.trim());
            return;
        }
        if (start == vertexArray.length) {
            return;
        }

        used[start] = true;
        generateIntialConfigurations(vertexArray, configurationlength, start + 1, currLen + 1, used);

        used[start] = false;
        generateIntialConfigurations(vertexArray, configurationlength, start + 1, currLen, used);
    }

    public static void computeGreedy(int[][] distanceArray, ArrayList<Integer> requests, String intialConfiguration) {

        ArrayList<Integer> serverDistances = new ArrayList<>();
        totalDistance = 0;
        for (int i = 0; i < requests.size(); i++) {

            int serverDistance = 9999999;

            for (int j = 0; j < intialConfiguration.length(); j++) {
                String singleServer = intialConfiguration.substring(j, j + 1);
                String remaningServerString = intialConfiguration.substring(j + 1, intialConfiguration.length());

                if (serverDistance > distanceArray[Integer.parseInt(singleServer)][requests.get(i)]) {
                    serverDistance = distanceArray[Integer.parseInt(singleServer)][requests.get(i)];

                    if (!intialConfiguration.contains(Integer.toString(requests.get(i)))) {
                        intialConfiguration = Integer.toString(requests.get(i)) + remaningServerString;
                    }
                }
            }

            totalDistance += serverDistance;
            greedyOutput[i][0] = totalDistance;

            System.out.println("Request " + i + "=" + requests.get(i));
            System.out.println("Initial Configuration has changed to = " + intialConfiguration);
            System.out.println("Cycle distance is = " + totalDistance);
            System.out.println("------------------------------------------------------------------------------------------");
        }
        System.out.println("******************************************************************************************");
    }

    public static void calculateWorkFunction(ArrayList<Integer> requests,int[][] distanceArray) {

        workFunctionOutput = new int[requests.size()+1][serverConfigurations.size()];
        String initialConfiguration = serverConfigurations.get(0);

        for(int i = 0; i<workFunctionOutput[0].length; i++) {
            workFunctionOutput[0][i] = calculateServerDistancesForFirstRow(initialConfiguration,serverConfigurations.get(i)
                    , distanceArray);
        }

        for(int i = 0; i<requests.size(); i++) {
            for(int j = 0; j<workFunctionOutput[0].length; j++) {
                workFunctionOutput[i+1][j] = findPossibleServersAndReturnMinimum(i+1,j,requests.get(i),distanceArray);
            }
        }

        printTheOutputToTheFile(requests);
    }

    public static int calculateServerDistancesForFirstRow(String initialConfiguration,String currentServerConfiguration
            ,int[][] distanceArray) {

        ArrayList<LinkedHashMap> serverCombinations = new ArrayList<>();

        for(int i = 0; i<initialConfiguration.length(); i++) {

            LinkedHashMap<String,Integer> serverDistances = new LinkedHashMap<>();
            for(int j = 0; j<currentServerConfiguration.length(); j++) {
                if(!initialConfiguration.substring(i,i+1).equalsIgnoreCase(currentServerConfiguration.substring(j,j+1))) {

                    String si1 = currentServerConfiguration.substring(j,j+1);
                    String sj1 = initialConfiguration.substring(i,i+1);

                    if(!initialConfiguration.contains(si1) &&
                            !currentServerConfiguration.contains(sj1)) {

                        String s1 = initialConfiguration.substring(i,i+1);
                        String s2 = currentServerConfiguration.substring(j,j+1);

                        serverDistances.put(s1+s2,distanceArray[Integer.parseInt(s1)][Integer.parseInt(s2)]);
                    }
                }
            }
            if(!serverDistances.isEmpty()) {
                serverCombinations.add(serverDistances);
            }
        }

        ArrayList<Integer> finalWeights = new ArrayList<>();
        if(!serverCombinations.isEmpty()) {
            applyRecursionToFindTheMinimumWeight(serverCombinations,0,new ArrayList<String>(),finalWeights,0);
            if(!finalWeights.isEmpty()) {
                Collections.sort(finalWeights);
                System.out.println("The Elements in the Array list are = " + Arrays.toString(finalWeights.toArray()));
                return finalWeights.get(0);
            }
        } else {
            System.out.println("Entered the else loop and returning Zero.");
            return 0;
        }

        return -1;
    }

    public static void applyRecursionToFindTheMinimumWeight(ArrayList<LinkedHashMap> serverCombinations,int k,ArrayList<String> recursivePath
            ,ArrayList<Integer> finalWeights,int minimumWeight) {

        if(k == serverCombinations.size()) {
            finalWeights.add(minimumWeight);
            k = k - 1;
            minimumWeight -= (int) serverCombinations.get(k).get(recursivePath.get(k));
            recursivePath.remove(k);
            return;
        } else {
            Set<String>  keys = serverCombinations.get(k).keySet();
            for(String key : keys) {

                if(recursivePath.isEmpty()) {
                    recursivePath.add(key);
                    applyRecursionToFindTheMinimumWeight(serverCombinations,k+1,recursivePath,finalWeights,
                            minimumWeight + (int) serverCombinations.get(k).get(key));
                } else {
                    if(!isElementPresentInPath(recursivePath,key)) {
                        recursivePath.add(key);
                        applyRecursionToFindTheMinimumWeight(serverCombinations,k+1,recursivePath,finalWeights,
                                minimumWeight + (int) serverCombinations.get(k).get(key));

                    }
                }
            }

            k = k - 1;
            if(!recursivePath.isEmpty()) {
                minimumWeight -= (int) serverCombinations.get(k).get(recursivePath.get(k));
                recursivePath.remove(k);
            }
        }
    }

    public static boolean isElementPresentInPath(ArrayList<String> recursivePath,String key) {

        for(int j = 0; j<recursivePath.size(); j++) {
            String s1 = recursivePath.get(j).substring(1,2);
            if(key.substring(1,2).equalsIgnoreCase(s1)) {
                return true;
            }
        }
        return false;
    }

    public static int findPossibleServersAndReturnMinimum(int i,int j,int currentRequest,int[][] distanceArray) {

        String currentServerConfiguration = serverConfigurations.get(j);
        if(currentServerConfiguration.contains(Integer.toString(currentRequest))) {
            return workFunctionOutput[i-1][j];
        } else {
            ArrayList<Integer> possibleIntermediateServersWeights = new ArrayList<>();

            for(int k = 0; k<serverConfigurations.size(); k++) {
                if(serverConfigurations.get(k) != currentServerConfiguration && serverConfigurations.get(k)
                        .contains(Integer.toString(currentRequest))) {
                    int previousWeight = workFunctionOutput[i-1][k];
                    int calculatedWeight = calculateServerDistancesForFirstRow(currentServerConfiguration,
                            serverConfigurations.get(k),distanceArray);
                    int totalWeight = calculatedWeight + previousWeight;
                    possibleIntermediateServersWeights.add(totalWeight);
                }
            }

            if(!possibleIntermediateServersWeights.isEmpty()) {
                Collections.sort(possibleIntermediateServersWeights);
                return possibleIntermediateServersWeights.get(0);
            }
        }
        return -1;
    }

    public static void generateOptValueOfTheWorkFunction() {

        int minElement = 9999999;
        for(int i = 0; i<workFunctionOutput[0].length; i++) {
            if(workFunctionOutput[workFunctionOutput.length-1][i] < minElement) {
                minElement = workFunctionOutput[workFunctionOutput.length-1][i];
            }
        }

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("The minimum value of opt is = " + minElement);
    }

    public static void printTheOutputToTheFile(ArrayList<Integer> requests) {

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("Output.txt"))) {
            writer.write("    ");

            for(int k = 0; k<serverConfigurations.size(); k++) {
                writer.write(serverConfigurations.get(k));
                writer.write("  ");
            }
            writer.newLine();

            for(int i = 0; i<workFunctionOutput.length; i++) {

                if( i == 0) {
                    writer.write("N = ");
                } else {
                    writer.write(Integer.toString(requests.get(i-1)) + " = ");
                }

                for(int j = 0; j<workFunctionOutput[0].length; j++) {
                    writer.write(Integer.toString(workFunctionOutput[i][j]));
                    writer.write("    ");
                }
                writer.newLine();
                writer.write("----------------------");
                writer.newLine();
            }

        } catch (Exception e) {
            System.out.println("Cannot write to the file");
            e.printStackTrace();
            return;
        }
    }

}
