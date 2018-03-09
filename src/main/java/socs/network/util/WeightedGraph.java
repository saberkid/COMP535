package socs.network.util;

import java.io.*;
import java.util.*;

//public class WeighedGraph{
//
//    private int[][] mMatrix;
//    private static final int INF = Integer.MAX_VALUE;
//    
//    private List<Vertex> vertexs;
//
//
//    public WeighedGraph(){
//    }
//}


//public class Dijkstra {
//   private static final .Edge[] GRAPH = {
//      new Graph.Edge("a", "b", 7),
//      new Graph.Edge("a", "c", 9),
//      new Graph.Edge("a", "f", 14),
//      new Graph.Edge("b", "c", 10),
//      new Graph.Edge("b", "d", 15),
//      new Graph.Edge("c", "d", 11),
//      new Graph.Edge("c", "f", 2),
//      new Graph.Edge("d", "e", 6),
//      new Graph.Edge("e", "f", 9),
//   };
//   private static final String START = "a";
//   private static final String END = "e";
// 
//   public static void main(String[] args) {
//      Graph g = new Graph(GRAPH);
//      g.dijkstra(START);
//      g.printPath(END);
//   }
//}
 
public class WeightedGraph {


    private Map<String, Vertex> graph; // mapping of vertex names to Vertex objects, built from a set of Edges
	/** One edge of the graph (only used by Graph constructor) */

    public Map<String, Vertex> getGraph() {
        return graph;
    }
	public static class Edge {
		public final String v1, v2;
		public final int dist;
		public Edge(String v1, String v2, int dist) {
			this.v1 = v1;
			this.v2 = v2;
			this.dist = dist;
		}
	}
 	public static Vertex findVertex(ArrayList<Vertex> list, String routerIp){
	    Vertex found = null;
		for(Vertex v: list){
		    if(v.name.equals(routerIp)) {
                found = v;
                break;
            }
		}
        return found;

	}
	/** One vertex of the graph, complete with mappings to neighbouring vertices */
	public static class Vertex implements Comparable<Vertex>{
		public final String name;
		public int dist = Integer.MAX_VALUE; // MAX_VALUE assumed to be infinity
		public Vertex previous = null;
		public final Map<Vertex, Integer> neighbours = new HashMap<>();
		
		public Vertex(String name){
			this.name = name;
		}
 
		private void printPath(){
			if (this == this.previous){
				System.out.printf("%s", this.name);
			}
			else if (this.previous == null){
				System.out.printf("%s(unreached)", this.name);
			}
			else{
				this.previous.printPath();
				System.out.printf(" ->(%d) %s", this.dist, this.name);
			}
		}
 
		public int compareTo(Vertex other){
			if (dist == other.dist)
				return name.compareTo(other.name);
 
			return Integer.compare(dist, other.dist);
		}
 
		@Override public String toString(){
			return "(" + name + ", " + dist + ")";
		}
	}
 
   /** Builds a graph from a set of edges
    * @param edges*/
	public WeightedGraph(ArrayList<Edge> edges) {
		graph = new HashMap<>();
 
		//one pass to find all vertices
		for (Edge e : edges) {
			if (!graph.containsKey(e.v1)) graph.put(e.v1, new Vertex(e.v1));
			if (!graph.containsKey(e.v2)) graph.put(e.v2, new Vertex(e.v2));
		}
 
		//another pass to set neighbouring vertices
		for (Edge e : edges) {
			graph.get(e.v1).neighbours.put(graph.get(e.v2), e.dist);
			System.out.println("Hello");
		}
   }
 
	/** Implementation of dijkstra's algorithm using a binary heap. */
	public void dijkstra(String startName) {
		if (!graph.containsKey(startName)) {
			System.err.printf("Graph doesn't contain start vertex \"%s\"\n", startName);
			return;
		}
		final Vertex source = graph.get(startName);
		NavigableSet<Vertex> q = new TreeSet<>();
 
		// set-up vertices
		for (Vertex v : graph.values()) {
			v.previous = v == source ? source : null;
			v.dist = v == source ? 0 : Integer.MAX_VALUE;
			q.add(v);
		}
  
		Vertex u, v;
		while (!q.isEmpty()) {
			u = q.pollFirst(); // vertex with shortest distance (first iteration will return source)
			if (u.dist == Integer.MAX_VALUE) break; // we can ignore u (and any other remaining vertices) since they are unreachable
 
			//look at distances to each neighbour
			for (Map.Entry<Vertex, Integer> a : u.neighbours.entrySet()) {
				v = a.getKey(); //the neighbour in this iteration
				final int alternateDist = u.dist + a.getValue();
				if (alternateDist < v.dist) { // shorter path to neighbour found
					q.remove(v);
					v.dist = alternateDist;
					v.previous = u;
					q.add(v);
				} 
			}
		}
	}
 
	/** Prints a path from the source to the specified vertex */
	public void printPath(String endName) {
		if (!graph.containsKey(endName)) {
			System.err.printf("Graph doesn't contain end vertex \"%s\"\n", endName);
			return;
		}
 
		graph.get(endName).printPath();
		System.out.println();
	}
}