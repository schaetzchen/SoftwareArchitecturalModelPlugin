package plugindata.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Graph {
    // A user define class to represent a graph.
    // A graph is an array of adjacency lists.
    // Size of array will be V (number of vertices
    // in graph)
    int V;
    LinkedList<Integer>[] adjListArray;

    // constructor
    public Graph(int V) {
        this.V = V;
        // define the size of array as
        // number of vertices
        adjListArray = new LinkedList[V];

        // Create a new list for each vertex
        // such that adjacent nodes can be stored

        for (int i = 0; i < V; i++) {
            adjListArray[i] = new LinkedList<Integer>();
        }
    }

    // Adds an edge to an undirected graph
    public void addEdge(int src, int dest) {
        // Add an edge from src to dest.
        adjListArray[src].add(dest);

        // Since graph is undirected, add an edge from dest
        // to src also
        adjListArray[dest].add(src);
    }

    void DFSUtil(int v, boolean[] visited, ArrayList<Integer> instance) {
        // Mark the current node as visited and print it
        visited[v] = true;
        instance.add(v);
//        System.out.print(v + " ");
        // Recur for all the vertices
        // adjacent to this vertex
        for (int x : adjListArray[v]) {
            if (!visited[x]) DFSUtil(x, visited, instance);
        }
    }

    // todo test?
    public boolean DFSUtil(int x, int y, boolean[] visited) {

        visited[x] = true;

        for (int v : adjListArray[x])
            if (x == y)
                return true;
            else
                return DFSUtil(v, y, visited);
        return false;
    }

    public Set<ArrayList<Integer>> connectedComponents() {
        // Mark all the vertices as not visited
        boolean[] visited = new boolean[V];

        Set<ArrayList<Integer>> componentInstances = new HashSet<>();
        for (int v = 0; v < V; ++v) {
            if (!visited[v]) {

                ArrayList<Integer> instance = new ArrayList<>();
                DFSUtil(v, visited, instance);
                componentInstances.add(instance);
            }
        }

        return componentInstances;
    }

    public int getNumberOfVertices() {
        return V;
    }

    public LinkedList<Integer> getAdjacentVertices(int x) {
        return adjListArray[x];
    }
}